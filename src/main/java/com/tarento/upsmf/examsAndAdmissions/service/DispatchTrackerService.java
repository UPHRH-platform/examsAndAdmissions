package com.tarento.upsmf.examsAndAdmissions.service;

import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.storage.*;
import com.tarento.upsmf.examsAndAdmissions.model.*;
import com.tarento.upsmf.examsAndAdmissions.repository.DispatchTrackerRepository;
import com.tarento.upsmf.examsAndAdmissions.repository.ExamCycleRepository;
import com.tarento.upsmf.examsAndAdmissions.repository.ExamRepository;
import com.tarento.upsmf.examsAndAdmissions.util.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityNotFoundException;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

@Service
@Slf4j
public class DispatchTrackerService {

    @Autowired
    private DispatchTrackerRepository dispatchTrackerRepository;

    @Autowired
    private ExamRepository examRepository;

    @Autowired
    private ExamCycleRepository examCycleRepository;

    @Value("${gcp.bucket.folder.name}")
    private String gcpFolderName;
    @Value("${gcp.bucket.name}")
    private String gcpBucketName;
    @Value("${gcp.client.id}")
    private String gcpClientId;
    @Value("${gcp.client.email}")
    private String gcpClientEmail;
    @Value("${gcp.pkcs.key}")
    private String gcpPkcsKey;
    @Value("${gcp.private.key.id}")
    private String gcpPrivateKeyId;
    @Value("${gcp.project.id}")
    private String gcpProjectId;
    public DispatchTracker uploadDispatchProof(Long examCycleId, Long examId, MultipartFile dispatchProofFile, LocalDate dispatchDate) throws IOException {
        ResponseDto response = new ResponseDto(Constants.API_UPLOAD_DISPATCH_DETAILS);
        ExamCycle examCycle = examCycleRepository.findById(examCycleId).orElseThrow(() -> new EntityNotFoundException("Exam cycle not found"));
        Exam exam = examRepository.findById(examId).orElseThrow(() -> new EntityNotFoundException("Exam not found"));

        Path filePath = null;
        String fileName = dispatchProofFile.getOriginalFilename();
        filePath = Files.createTempFile(fileName.split("\\.")[0], fileName.split("\\.")[1]);
        dispatchProofFile.transferTo(filePath);

        ServiceAccountCredentials credentials = ServiceAccountCredentials.fromPkcs8(gcpClientId, gcpClientEmail,
                gcpPkcsKey, gcpPrivateKeyId, new ArrayList<String>());
        log.info("credentials created");
        Storage storage = StorageOptions.newBuilder().setProjectId(gcpProjectId).setCredentials(credentials).build().getService();
        log.info("storage object created");
        String gcpFileName = gcpFolderName + "/" + Calendar.getInstance().getTimeInMillis() + "_" + fileName;
        BlobId blobId = BlobId.of(gcpBucketName, gcpFileName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();
        storage.create(blobInfo, new FileInputStream(filePath.toFile()));

        DispatchTracker dispatchTracker = new DispatchTracker();
        dispatchTracker.setExamCycle(examCycle);
        dispatchTracker.setExam(exam);
        dispatchTracker.setDispatchDate(dispatchDate);
        dispatchTracker.setDispatchProofFileLocation(gcpFileName);

        dispatchTrackerRepository.save(dispatchTracker);
        return dispatchTracker;
    }

    public List<DispatchTracker> getDispatchList(Long examCycleId, Long examId) {
        return dispatchTrackerRepository.findByExamCycleIdAndExamId(examCycleId, examId);
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
    public ResponseDto getPreviewUrl(Long dispatchTrackerId) {
        ResponseDto response = new ResponseDto(Constants.API_DISPATCH_PROOF_PREVIEW);
        //Query to get gcpFileName
        DispatchTracker dispatchTracker = dispatchTrackerRepository.findById(dispatchTrackerId).orElse(null);
        assert dispatchTracker != null;
        String fileName = dispatchTracker.getDispatchProofFileLocation();
        try {
            Blob blob = getBlob(fileName);
            if (blob != null) {
                response.put(Constants.MESSAGE, Constants.SUCCESSFUL);
                response.put(Constants.RESPONSE, blob.getMediaLink());
                response.setResponseCode(HttpStatus.OK);
            } else {
                log.info("Unable to retrieve the PDF file from GCS ");
                response.put(Constants.MESSAGE, "Unable to retrieve the PDF file from GCS");
                response.put(Constants.RESPONSE, Constants.MESSAGE);
                response.setResponseCode(HttpStatus.NOT_FOUND);
            }
        } catch (IOException e) {
            log.error("Failed to preview the file: " + fileName + ", Exception: ", e);
            response.put(Constants.MESSAGE, "Failed to preview the file");
            response.put(Constants.RESPONSE, Constants.MESSAGE);
            response.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return response;
    }
}
