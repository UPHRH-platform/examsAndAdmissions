package com.tarento.upsmf.examsAndAdmissions.service.impl;

import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.storage.*;
import com.tarento.upsmf.examsAndAdmissions.model.*;
import com.tarento.upsmf.examsAndAdmissions.repository.CourseRepository;
import com.tarento.upsmf.examsAndAdmissions.repository.ExamCycleRepository;
import com.tarento.upsmf.examsAndAdmissions.repository.ExamRepository;
import com.tarento.upsmf.examsAndAdmissions.repository.QuestionPaperRepository;
import com.tarento.upsmf.examsAndAdmissions.service.AttachmentService;
import com.tarento.upsmf.examsAndAdmissions.util.Constants;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Timestamp;
import java.time.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;


@Service
@Slf4j
public class AttachmentServiceImpl implements AttachmentService {

    @Value("${gcp.config.file.path}")
    private String gcpConfigFilePath;

    @Value("${gcp.bucket.name}")
    private String gcpBucketName;

    @Value("${gcp.bucket.folder.name}")
    private String gcpFolderName;

    @Value("${gcp.max.file.size}")
    private String gcpMaxFileSize;

    @Value("${gcp.project.id}")
    private String gcpProjectId;

    @Value("${gcp.client.id}")
    private String gcpClientId;

    @Value("${gcp.client.email}")
    private String gcpClientEmail;

    @Value("${gcp.pkcs.key}")
    private String gcpPkcsKey;

    @Value("${gcp.private.key.id}")
    private String gcpPrivateKeyId;

    @Value("${gcp.sub.folder.path}")
    private String subFolderPath;

    @Value("${question.paper.total.marks}")
    private Long totalMarks;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private QuestionPaperRepository questionPaperRepository;

    @Autowired
    private ExamRepository examRepository;

    @Autowired
    private ExamCycleRepository examCycleRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Override
    public ResponseDto downloadQuestionPaper(Long questionPaperId) {
        ResponseDto response = new ResponseDto(Constants.API_QUESTION_PAPER_DOWNLOAD);
        //Query to get gcpFileName
        QuestionPaper questionPaperDetails = questionPaperRepository.findById(questionPaperId).orElse(null);
        LocalDate examDate = questionPaperDetails.getExamDate();
        LocalTime examTime = questionPaperDetails.getExamStartTime();
        String fileName = questionPaperDetails.getGcpFileName();
        try {
            LocalDate currentDate = LocalDate.now(ZoneId.of("Asia/Kolkata"));

            // Check if the current date is the same as the exam date
            if (currentDate.getYear() == examDate.getYear() &&
                    currentDate.getMonthValue() == examDate.getMonthValue() &&
                    currentDate.getDayOfMonth() == examDate.getDayOfMonth()) {
                ZoneId zoneId = ZoneId.of("Asia/Kolkata");
                ZonedDateTime currentTime = ZonedDateTime.now(zoneId);
                LocalTime currentLocalTime = currentTime.toLocalTime();
                // Calculate the time difference

                Duration duration = Duration.between(currentLocalTime, examTime);

                // Check if the difference is less than 30 minutes
                if (duration.toMinutes() < 30) {
                    // Set the local file path where you want to save the downloaded file
                    Blob blob = getBlob(fileName);
                    if (blob != null) {
                        log.info("File url for downloading : " + blob.getMediaLink());
                        response.put(Constants.MESSAGE, Constants.SUCCESSFUL);
                        response.put(Constants.RESPONSE, blob.getMediaLink());
                        response.setResponseCode(HttpStatus.OK);
                    } else {
                        log.info("File not found in the bucket: " + fileName);
                        response.put(Constants.MESSAGE, "Unable to retrieve the PDF file from GCS");
                        response.setResponseCode(HttpStatus.NOT_FOUND);
                    }
                } else {
                    response.put(Constants.MESSAGE, "Institutes will not be allow to view the question paper on their portal 30 minutes before an exam");
                    response.setResponseCode(HttpStatus.UNAUTHORIZED);
                }
            } else {
                response.put(Constants.MESSAGE, "Today is not the exam date.");
                response.setResponseCode(HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception e) {
            log.error("Failed to read the downloaded file: " + fileName + ", Exception: ", e);
            response.put(Constants.MESSAGE, e.getMessage());
            response.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return response;
    }

    private Blob getBlob(String fileName) throws IOException {
        ServiceAccountCredentials credentials = ServiceAccountCredentials.fromPkcs8(gcpClientId, gcpClientEmail,
                gcpPkcsKey, gcpPrivateKeyId, new ArrayList<String>());

        // Initialize Google Cloud Storage client
        Storage storage = StorageOptions.newBuilder()
                .setProjectId(gcpProjectId)
                .setCredentials(credentials)
                .build()
                .getService();

        // Create BlobId for the object you want to download
        BlobId blobId = BlobId.of(gcpBucketName, fileName);

        // Download the object
        Blob blob = storage.get(blobId);
        return blob;
    }

    @Override
    public ResponseDto getPreviewUrl(Long questionPaperId) {
        ResponseDto response = new ResponseDto(Constants.API_QUESTION_PAPER_PREVIEW);
        //Query to get gcpFileName
        QuestionPaper questionPaperDetails = questionPaperRepository.findById(questionPaperId).orElse(null);
        String fileName = questionPaperDetails.getGcpFileName();
        try {
            Blob blob = getBlob(fileName);
            if (blob != null) {
                response.put(Constants.MESSAGE, Constants.SUCCESSFUL);
                response.put(Constants.RESPONSE, blob.getMediaLink());
                response.setResponseCode(HttpStatus.OK);
            } else {
                log.info("Unable to retrieve the PDF file from GCS ");
                response.put(Constants.MESSAGE, "Unable to retrieve the PDF file from GCS");
                response.setResponseCode(HttpStatus.NOT_FOUND);
            }
        } catch (IOException e) {
            log.error("Failed to preview the file: " + fileName + ", Exception: ", e);
            response.put(Constants.MESSAGE, "Failed to preview the file");
            response.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return response;
    }

    @Override
    public ResponseDto upload(Long examCycleId, Long examId, String createdBy, MultipartFile file) {
        ResponseDto response = new ResponseDto(Constants.API_QUESTION_PAPER_UPLOAD);
        Path filePath = null;
        try {
            // validate file
            String fileName = file.getOriginalFilename();
            filePath = Files.createTempFile(fileName.split("\\.")[0], fileName.split("\\.")[1]);
            file.transferTo(filePath);
            validateFile(filePath);
            // create credentials
            ServiceAccountCredentials credentials = ServiceAccountCredentials.fromPkcs8(gcpClientId, gcpClientEmail,
                    gcpPkcsKey, gcpPrivateKeyId, new ArrayList<String>());
            log.info("credentials created");
            Storage storage = StorageOptions.newBuilder().setProjectId(gcpProjectId).setCredentials(credentials).build().getService();
            log.info("storage object created");
            String gcpFileName = gcpFolderName + "/" + Calendar.getInstance().getTimeInMillis() + "_" + fileName;
            BlobId blobId = BlobId.of(gcpBucketName, gcpFileName);
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();
            storage.create(blobInfo, new FileInputStream(filePath.toFile()));

            //Get Details from other table
            Exam examDetails = examRepository.findByIdAndObsolete(examId, 0).orElse(null);
            LocalDate examDate = null;
            String examName = null;
            LocalTime examStartTime = null;
            if (examDetails != null) {
                examDate = examDetails.getExamDate();
                examName = examDetails.getExamName();
                examStartTime = examDetails.getStartTime();
            }

            ExamCycle examCycleDetails = examCycleRepository.findByIdAndObsolete(examCycleId, 0).orElse(null);
            String examCycleName = null;
            Long courseId = null;
            if (examCycleDetails != null) {
                examCycleName = examCycleDetails.getExamCycleName();
                courseId = examCycleDetails.getCourse().getId();
            }

            Course courseDetails = null;
            if (courseId != null) {
                courseDetails = courseRepository.findById(courseId).orElse(null);
            }
            String courseName = null;
            if (courseDetails != null) {
                courseName = courseDetails.getCourseName();
            }

            Map<String, Object> uploadedFile = new HashMap<>();
            uploadedFile.put(Constants.FILE_NAME, fileName);
            uploadedFile.put(Constants.GCP_FILE_NAME, gcpFileName);
            uploadedFile.put(Constants.EXAM_CYCLE_ID, examCycleId);
            uploadedFile.put(Constants.EXAM_CYCLE_NAME, examCycleName);
            uploadedFile.put(Constants.EXAM_DATE, examDate);
            uploadedFile.put(Constants.EXAM_START_TIME, examStartTime);
            uploadedFile.put(Constants.COURSE_NAME, courseName);
            uploadedFile.put(Constants.EXAM_NAME, examName);
            uploadedFile.put(Constants.CREATED_BY, createdBy);
            uploadedFile.put(Constants.DATE_CREATED_ON, new Timestamp(System.currentTimeMillis()));
            uploadedFile.put(Constants.MODIFIED_BY, createdBy);
            uploadedFile.put(Constants.DATE_MODIFIED_ON, new Timestamp(System.currentTimeMillis()));
            uploadedFile.put(Constants.TOTAL_MARKS, totalMarks);

            QuestionPaper uploadData = new QuestionPaper();
            uploadData.setExam(examDetails);
            uploadData.setExamCycle(examCycleDetails);
            uploadData.setCourse(courseDetails);
            uploadData.setFileName(fileName);
            uploadData.setGcpFileName(gcpFileName);
            uploadData.setExamDate(examDate);
            uploadData.setExamStartTime(examStartTime);
            uploadData.setExamCycleId(examCycleId);
            uploadData.setExamCycleName(examCycleName);
            uploadData.setCourseName(courseName);
            uploadData.setExamName(examName);
            uploadData.setCreatedOn(new Timestamp(System.currentTimeMillis()));
            uploadData.setModifiedOn(new Timestamp(System.currentTimeMillis()));
            uploadData.setCreatedBy(createdBy);
            uploadData.setModifiedBy(createdBy);
            uploadData.setTotalMarks(totalMarks);

            questionPaperRepository.save(uploadData);
            //Getting question paper id
            uploadedFile.put(Constants.ID, uploadData.getId());
            response.getParams().setStatus(Constants.SUCCESS);
            response.setResponseCode(HttpStatus.OK);
            response.put(Constants.MESSAGE, "Question paper uploaded successfully");
            response.put(Constants.RESPONSE, uploadedFile);
        } catch (IOException e) {
            log.error("Error while uploading attachment", e);
            setErrorData(response,
                    String.format("Error while uploading file.", e.getMessage()));
        } finally {
            if (filePath != null) {
                try {
                    Files.delete(filePath);
                } catch (IOException e) {
                    log.error("Unable to delete temp file", e);
                }
            }
        }
        return response;
    }

    @Override
    public ResponseDto deleteQuestionPaper(Long id) {
        ResponseDto response = new ResponseDto(Constants.API_QUESTION_PAPER_DELETE);
        try {
            QuestionPaper questionPaperDetails = questionPaperRepository.findById(id).orElse(null);
            String fileName = questionPaperDetails.getGcpFileName();
            ServiceAccountCredentials credentials = ServiceAccountCredentials.fromPkcs8(gcpClientId, gcpClientEmail,
                    gcpPkcsKey, gcpPrivateKeyId, new ArrayList<String>());

            // Initialize Google Cloud Storage client
            Storage storage = StorageOptions.newBuilder()
                    .setProjectId(gcpProjectId)
                    .setCredentials(credentials)
                    .build()
                    .getService();

            // Create BlobId for the object you want to download
            BlobId blobId = BlobId.of(gcpBucketName, fileName);
            QuestionPaper questionPaper = questionPaperRepository.findById(id).orElse(null);
            if (questionPaper != null && blobId != null) {
                questionPaper.setObsolete(1);
                storage.delete(blobId);
                questionPaperRepository.save(questionPaper);
                response.put(Constants.MESSAGE, Constants.SUCCESSFUL);
                response.put(Constants.RESPONSE, "Question paper id is deleted successfully");
                response.setResponseCode(HttpStatus.OK);
            } else {
                log.warn("questionPaper with ID: {} not found for deletion!", id);
                response.put(Constants.MESSAGE, "questionPaper with id not found for deletion!");
                response.setResponseCode(HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            response.put(Constants.MESSAGE, "Exception occurred during deleting the questionPaper id");
            response.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return response;
    }

    private void setErrorData(ResponseDto response, String errMsg) {
        response.getParams().setStatus(Constants.FAILED);
        response.getParams().setErrmsg(errMsg);
        response.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private boolean validateFile(Path path) throws IOException {
        if (Files.isExecutable(path)) {
            throw new RuntimeException("Invalid file");
        }
        Tika tika = new Tika();
        String fileExt = tika.detect(path);
        if (fileExt.equalsIgnoreCase("application/pdf")) {
            return true;
        } else if (fileExt.startsWith("image")) {
            return true;
        }
        throw new RuntimeException("Invalid file type. Supported files are PDF and Images.");
    }
}
