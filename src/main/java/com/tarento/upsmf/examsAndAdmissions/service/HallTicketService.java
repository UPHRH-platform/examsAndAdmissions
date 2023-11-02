package com.tarento.upsmf.examsAndAdmissions.service;

import com.google.cloud.storage.Blob;
import com.tarento.upsmf.examsAndAdmissions.enums.DocumentType;
import com.tarento.upsmf.examsAndAdmissions.enums.HallTicketStatus;
import com.tarento.upsmf.examsAndAdmissions.model.*;
import com.tarento.upsmf.examsAndAdmissions.model.dto.DataCorrectionRequest;
import com.tarento.upsmf.examsAndAdmissions.model.dto.DataCorrectionRequestDto;
import com.tarento.upsmf.examsAndAdmissions.model.dto.DataCorrectionResponseDto;
import com.tarento.upsmf.examsAndAdmissions.model.dto.PendingDataDto;
import com.tarento.upsmf.examsAndAdmissions.repository.*;
import com.tarento.upsmf.examsAndAdmissions.util.Constants;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityManager;
import javax.persistence.criteria.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Slf4j
public class HallTicketService {

    @Autowired
    HallTicketRepository hallTicketRepository;
    @Autowired
    DataCorrectionRequestRepository dataCorrectionRequestRepository;
    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private StudentExamRegistrationRepository studentExamRegistrationRepository;

    @Autowired
    private ExamCenterRepository examCenterRepository;

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private ExamRepository examRepository;

    @Autowired
    private EntityManager entityManager;
    @Autowired
    private StudentService studentService;
    @Autowired
    private FileStorageService fileStorageService;

    public ResponseDto generateAndSaveHallTicketsForMultipleStudents(List<Long> studentRegistrationIds) {
        ResponseDto responseDto = new ResponseDto(Constants.API_GENERATE_AND_SAVE_HALL_TICKETS_FOR_MULTIPLE_STUDENTS);
        int successCount = 0;

        for (Long id : studentRegistrationIds) {
            try {
                Optional<StudentExamRegistration> registrationOptional = studentExamRegistrationRepository.findById(id);
                if (registrationOptional.isEmpty()) {
                    log.warn("No registration found for student ID: {}", id);
                    continue;
                }

                StudentExamRegistration registration = registrationOptional.get();
                byte[] hallTicketData = generateHallTicket(registration);
                if (hallTicketData.length == 0) {
                    log.warn("Hall ticket data is empty for student ID: {}", id);
                    continue;
                }

                MultipartFile hallTicket = new ByteArrayMultipartFile(hallTicketData, "hallticket_" + id + ".pdf");
                if (hallTicket.isEmpty()) {
                    log.warn("Converted MultipartFile is empty for student ID: {}", id);
                    continue;
                }

                String path = fileStorageService.storeFile(hallTicket, DocumentType.HALL_TICKET);
                if (path == null) {
                    setErrorResponse(responseDto, "STORAGE_ERROR", "Failed to store hall ticket for student ID: " + id, HttpStatus.INTERNAL_SERVER_ERROR);
                    return responseDto;
                } else {
                    successCount++;
                    registration.setHallTicketPath(path);
                    registration.setHallTicketStatus(HallTicketStatus.GENERATED);
                    // Setting the generation date here
                    registration.setHallTicketGenerationDate(LocalDate.now());
                    studentExamRegistrationRepository.save(registration);
                }

            } catch (Exception e) {
                log.error("Error processing hall ticket for student ID: " + id, e);
                setErrorResponse(responseDto, "HALLTICKET_GENERATION_ERROR", "Failed to generate hall ticket for student ID: " + id, HttpStatus.INTERNAL_SERVER_ERROR);
                return responseDto;
            }
        }
        if (successCount == studentRegistrationIds.size()) {
            responseDto.put(Constants.MESSAGE, "Hall tickets generated and saved successfully for all students.");
            responseDto.setResponseCode(HttpStatus.OK);
        } else if (successCount > 0) {
            setErrorResponse(responseDto, "PARTIAL_SUCCESS", "Hall tickets generated for " + successCount + " out of " + studentRegistrationIds.size() + " students.", HttpStatus.PARTIAL_CONTENT);
        } else {
            setErrorResponse(responseDto, "HALLTICKET_GENERATION_ERROR", "Failed to generate and save hall tickets for all students.", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return responseDto;
    }
    public ResponseDto getHallTicketForStudent(Long id, String dateOfBirth) {
        ResponseDto response = new ResponseDto(Constants.API_HALLTICKET_GET);

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        Date dob;
        LocalDate localDate;
        try {
            dob = formatter.parse(dateOfBirth);
            localDate = dob.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        } catch (ParseException e) {
            setErrorResponse(response, "INVALID_DATE_FORMAT", "Invalid date format", HttpStatus.BAD_REQUEST);
            return response;
        }

        Optional<StudentExamRegistration> registrationOptional = studentExamRegistrationRepository.findByIdAndStudent_DateOfBirth(id, localDate);
        if (registrationOptional.isEmpty()) {
            setErrorResponse(response, "STUDENT_NOT_FOUND", "No student record found for the provided details.", HttpStatus.NOT_FOUND);
            return response;
        }

        StudentExamRegistration registration = registrationOptional.get();
        String hallTicketPath = registration.getHallTicketPath();
        if (hallTicketPath == null || hallTicketPath.isEmpty()) {
            setErrorResponse(response, "HALL_TICKET_NOT_GENERATED", "Hall ticket not generated for this student.", HttpStatus.NOT_FOUND);
            return response;
        }
/*
        // Check if the hall ticket exists in Google Cloud Storage
        Blob blob = storage.get(BlobId.of("YOUR_BUCKET_NAME", hallTicketPath));
        if (blob == null || !blob.exists()) {
            setErrorResponse(response, "HALL_TICKET_NOT_FOUND_IN_STORAGE", "Hall ticket not found in storage.", HttpStatus.NOT_FOUND);
            return response;
        }

        String hallTicketUrl = blob.getMediaLink();  // Get the public URL of the stored hall ticket*/
        response.put(Constants.MESSAGE, Constants.SUCCESSMESSAGE);
        response.put(Constants.RESPONSE, hallTicketPath);  // Sending the hall ticket as a resource
        response.setResponseCode(HttpStatus.OK);
        return response;
    }

    public ResponseDto getHallTicketForStudentByStudentIdAndExamCycleId(Long id, Long examCycleId) {
        ResponseDto response = new ResponseDto(Constants.API_HALLTICKET_GET_BY_ID);

        List<StudentExamRegistration> registrationOptional = studentExamRegistrationRepository.findAllByStudent_IdAndExam_Id(id, examCycleId);
        if (registrationOptional.isEmpty()) {
            setErrorResponse(response, "STUDENT_NOT_FOUND", "No student record found for the provided details.", HttpStatus.NOT_FOUND);
            return response;
        }

        List<String> hallTickets = new ArrayList<>();
        registrationOptional.stream().filter(path -> !path.getHallTicketPath().isBlank()).forEach(ticket -> hallTickets.add(ticket.getHallTicketPath()));
        if (hallTickets == null || hallTickets.isEmpty()) {
            setErrorResponse(response, "HALL_TICKET_NOT_GENERATED", "Hall ticket not generated for this student.", HttpStatus.NOT_FOUND);
            return response;
        }

        response.put(Constants.MESSAGE, Constants.SUCCESSMESSAGE);
        response.put(Constants.RESPONSE, hallTickets);  // Sending the hall ticket as a resource
        response.setResponseCode(HttpStatus.OK);
        return response;
    }

    public ResponseDto getHallTicketBlobResourcePath(Long id, String dateOfBirth) throws Exception {
        ResponseDto hallTicketResponse = getHallTicketForStudent(id, dateOfBirth);

        if (hallTicketResponse.getResponseCode().is2xxSuccessful()) {
            String hallTicketPath = (String) hallTicketResponse.getResult().get(Constants.RESPONSE);

            Blob blob = fileStorageService.getBlobFromGCP(hallTicketPath);
            if (blob == null || !blob.exists()) {
                log.error("Blob for hall ticket not found in GCS for path: {}", hallTicketPath);
                ResponseDto response = new ResponseDto(Constants.API_HALLTICKET_GET);
                ResponseDto.setErrorResponse(response, "BLOB_NOT_FOUND", "Error fetching hall ticket from storage.", HttpStatus.INTERNAL_SERVER_ERROR);
                return response;
            }

            // Generate a signed URL for direct download
            URL signedUrl = blob.signUrl(15, TimeUnit.MINUTES); // This URL will be valid for 15 minutes
            hallTicketResponse.getResult().put(Constants.RESPONSE, signedUrl);  // use the signed URL
            return hallTicketResponse;

        } else if (hallTicketResponse.getResponseCode() == HttpStatus.NOT_FOUND) {
            return hallTicketResponse;  // Directly return the 404 response if the record isn't found
        } else {
            ResponseDto response = new ResponseDto(Constants.API_HALLTICKET_GET);
            ResponseDto.setErrorResponse(response, "REQUEST_ERROR", "Error processing request: " + hallTicketResponse.getError().getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            return response;
        }
    }

    public ResponseDto getHallTicketBlobResourcePathByStudentId(Long studentId, Long examCycleId) throws Exception {
        ResponseDto hallTicketResponse = getHallTicketForStudentByStudentIdAndExamCycleId(studentId, examCycleId);

        if (hallTicketResponse.getResponseCode().is2xxSuccessful()) {
            List<String> hallTicketPaths = (List) hallTicketResponse.getResult().get(Constants.RESPONSE);
            List<URL> signedUrls = new ArrayList<>();
            hallTicketPaths.stream().forEach(ticket -> {
                try {
                    Blob blob = fileStorageService.getBlobFromGCP(ticket);
                    if (blob != null && blob.exists()) {
                        // Generate a signed URL for direct download
                        URL signedUrl = blob.signUrl(15, TimeUnit.MINUTES);
                        signedUrls.add(signedUrl);
                    }
                } catch (IOException | URISyntaxException e) {
                    log.error("Error in converting to blob");
                }
            });
            if (signedUrls.isEmpty()) {
                ResponseDto response = new ResponseDto(Constants.API_HALLTICKET_GET_BY_ID);
                ResponseDto.setErrorResponse(response, "BLOB_NOT_FOUND", "Error fetching hall ticket from storage.", HttpStatus.INTERNAL_SERVER_ERROR);
                return response;
            }
            hallTicketResponse.getResult().put(Constants.RESPONSE, signedUrls);  // use the signed URL
            return hallTicketResponse;

        } else if (hallTicketResponse.getResponseCode() == HttpStatus.NOT_FOUND) {
            return hallTicketResponse;  // Directly return the 404 response if the record isn't found
        } else {
            ResponseDto response = new ResponseDto(Constants.API_HALLTICKET_GET_BY_ID);
            ResponseDto.setErrorResponse(response, "REQUEST_ERROR", "Error processing request: " + hallTicketResponse.getError().getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            return response;
        }
    }

    public ResponseDto requestHallTicketDataCorrection(Long studentId,
                                                       String updatedFirstName,
                                                       String updatedLastName,
                                                       LocalDate updatedDOB,
                                                       @RequestParam("file") MultipartFile proof) throws IOException {
        ResponseDto response = new ResponseDto(Constants.API_HALLTICKET_REQUEST_DATA_CORRECTION);

        try {
            Optional<Student> optionalStudent = studentRepository.findById(studentId);

            if (!optionalStudent.isPresent()) {
                setErrorResponse(response, "INVALID_STUDENT_ID", "Invalid Student ID", HttpStatus.BAD_REQUEST);
                return response;
            }

            if (proof == null || proof.isEmpty()) {
                setErrorResponse(response, "PROOF_MISSING", "Proof attachment is required", HttpStatus.BAD_REQUEST);
                return response;
            }

            DataCorrectionRequest request = new DataCorrectionRequest();
            request.setStudent(optionalStudent.get());
            if (updatedFirstName != null) {
                request.setUpdatedFirstName(updatedFirstName);
            }
            if (updatedLastName != null) {
                request.setUpdatedLastName(updatedLastName);
            }
            if (updatedDOB != null) {
                request.setUpdatedDOB(updatedDOB);
            }
            request.setStatus("NEW");

            String path = studentService.storeFile(proof);
            request.setProofAttachmentPath(path);

            dataCorrectionRequestRepository.save(request);
            DataCorrectionRequestDto responseDto = toDto(request);
            response.put(Constants.MESSAGE, Constants.SUCCESSMESSAGE);
            response.put(Constants.RESPONSE, responseDto);
            response.setResponseCode(HttpStatus.OK);
        } catch (Exception e) {
            setErrorResponse(response, "DATA_CORRECTION_ERROR", "Error during data correction request: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            log.error("Error during data correction request", e);  // Assuming you have a logger set up.
        }
        return response;
    }

    private DataCorrectionRequestDto toDto(DataCorrectionRequest request) {
        DataCorrectionRequestDto dto = new DataCorrectionRequestDto();
        dto.setId(request.getId());
        dto.setUpdatedFirstName(request.getUpdatedFirstName());
        dto.setUpdatedLastName(request.getUpdatedLastName());
        dto.setUpdatedDOB(request.getUpdatedDOB());
        dto.setStatus(request.getStatus());
        dto.setProofAttachmentPath(request.getProofAttachmentPath());
        return dto;
    }

    public ResponseDto getAllDataCorrectionRequests() {
        ResponseDto response = new ResponseDto(Constants.API_HALLTICKET_GET_ALL_DATA_CORRECTION_REQUESTS);
        List<DataCorrectionRequest> requests = dataCorrectionRequestRepository.findAll();
        List<Map<String, Object>> formattedRequests = new ArrayList<>();

        for (DataCorrectionRequest request : requests) {
            Map<String, Object> formattedRequest = new HashMap<>();
            formattedRequest.put("id", request.getId());
            formattedRequest.put("requestedCorrection", request.getRequestedCorrection());
            formattedRequest.put("status", request.getStatus());
            formattedRequest.put("rejectionReason", request.getRejectionReason());
            formattedRequest.put("proofAttachmentPath", request.getProofAttachmentPath());

            if (request.getStudent() != null) {
                Student student = request.getStudent();
                formattedRequest.put("firstName", student.getFirstName());
                formattedRequest.put("lastName", student.getSurname());  // changed from 'surName'
                formattedRequest.put("enrollmentNumber", student.getEnrollmentNumber());

                StudentExamRegistration registration = studentExamRegistrationRepository.findByStudent(student);
                if (registration != null) {
                    Map<String, Object> examCycleData = new HashMap<>();
                    ExamCycle examCycle = registration.getExamCycle();

                    examCycleData.put("id", examCycle.getId());
                    examCycleData.put("examCyclename", examCycle.getExamCycleName());
                    examCycleData.put("startDate", examCycle.getStartDate());
                    examCycleData.put("endDate", examCycle.getEndDate());
                    examCycleData.put("createdBy", examCycle.getCreatedBy());
                    examCycleData.put("modifiedBy", examCycle.getModifiedBy());
                    examCycleData.put("status", examCycle.getStatus());
                    examCycleData.put("obsolete", examCycle.getObsolete());

                    List<Map<String, Object>> examsData = new ArrayList<>();
                    List<Exam> exams = examRepository.findByExamCycleId(examCycle.getId());
                    for (Exam exam : exams) {
                        Map<String, Object> examData = new HashMap<>();

                        examData.put("examName", exam.getExamName());
                        examData.put("examDate", exam.getExamDate());
                        examData.put("startTime", exam.getStartTime());
                        examData.put("endTime", exam.getEndTime());
                        examData.put("createdBy", exam.getCreatedBy());
                        examData.put("modifiedBy", exam.getModifiedBy());
                        examData.put("isResultsPublished", exam.getIsResultsPublished());
                        examData.put("obsolete", exam.getObsolete());

                        examsData.add(examData);
                    }

                    examCycleData.put("exams", examsData);
                    formattedRequest.put("examCycle", examCycleData);
                }
            }

            formattedRequests.add(formattedRequest);
        }

        if (!formattedRequests.isEmpty()) {
            response.put(Constants.MESSAGE, Constants.SUCCESSFUL);
            response.put(Constants.RESPONSE, formattedRequests);
            response.setResponseCode(HttpStatus.OK);
        } else {
            ResponseDto.setErrorResponse(response, "NO_DATA_CORRECTION_REQUESTS", "No data correction requests found.", HttpStatus.NOT_FOUND);
        }

        return response;
    }

    public ResponseDto approveDataCorrection(Long requestId) {
        ResponseDto response = new ResponseDto(Constants.API_HALLTICKET_APPROVE_DATA_CORRECTION);

        try {
            DataCorrectionRequest request = dataCorrectionRequestRepository.findById(requestId).orElse(null);
            if (request != null) {
                // 1. Fetch the associated student details and update
                Student student = request.getStudent();
                student.setFirstName(request.getUpdatedFirstName());
                student.setSurname(request.getUpdatedLastName());
                student.setDateOfBirth(request.getUpdatedDOB());
                studentRepository.save(student);

                // 2. Regenerate the hall ticket with the updated details
                Optional<StudentExamRegistration> registrationOptional = Optional.ofNullable(studentExamRegistrationRepository.findByStudent(student));
                if (registrationOptional.isPresent()) {
                    StudentExamRegistration registration = registrationOptional.get();
                    byte[] hallTicketData = generateHallTicket(registration);
                    if (hallTicketData.length > 0) {
                        MultipartFile hallTicket = new ByteArrayMultipartFile(hallTicketData, "hallticket_updated_" + student.getId() + ".pdf");
                        String path = fileStorageService.storeFile(hallTicket, DocumentType.HALL_TICKET);
                        if (path != null) {
                            registration.setHallTicketPath(path);
                            registration.setHallTicketStatus(HallTicketStatus.UPDATED); // Assuming there's an UPDATED status
                            studentExamRegistrationRepository.save(registration);
                        }
                    }
                }

                request.setStatus("APPROVED");
                dataCorrectionRequestRepository.save(request);

                // Convert saved request to DTO
                DataCorrectionResponseDto responseDto = new DataCorrectionResponseDto();
                responseDto.setId(request.getId());
                responseDto.setStudentId(request.getStudent().getId());
                responseDto.setStatus(request.getStatus());
                responseDto.setProofAttachmentPath(request.getProofAttachmentPath());
                responseDto.setUpdatedFirstName(request.getUpdatedFirstName());
                responseDto.setUpdatedLastName(request.getUpdatedLastName());
                responseDto.setUpdatedDOB(request.getUpdatedDOB().toString());

                response.put(Constants.MESSAGE, Constants.SUCCESSMESSAGE);
                response.put(Constants.RESPONSE, responseDto);
                response.setResponseCode(HttpStatus.OK);
            } else {
                String errorMessage = String.format("Data correction request with ID %d could not be found. Please check if the ID is correct and try again.", requestId);
                setErrorResponse(response, "REQUEST_NOT_FOUND", errorMessage, HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            setErrorResponse(response, "APPROVAL_ERROR", "Error during approval process: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            log.error("Error during data correction approval", e);  // Assuming you have a logger set up.
        }
        return response;
    }

    public ResponseDto rejectDataCorrection(Long requestId, String rejectionReason) {
        ResponseDto response = new ResponseDto(Constants.API_HALLTICKET_REJECT_DATA_CORRECTION);
        DataCorrectionRequest request = dataCorrectionRequestRepository.findById(requestId).orElse(null);
        if (request != null) {
            request.setStatus("REJECTED");
            request.setRejectionReason(rejectionReason);
            dataCorrectionRequestRepository.save(request);
            response.put(Constants.MESSAGE, Constants.SUCCESSMESSAGE);
            response.put(Constants.RESPONSE, request);
            response.setResponseCode(HttpStatus.OK);
        } else {
            setErrorResponse(response, "REQUEST_NOT_FOUND", "Request not found.", HttpStatus.NOT_FOUND);
        }
        return response;
    }

    public ResponseDto getPendingDataForHallTickets(Long courseId, Long examCycleId, Long instituteId) {
        ResponseDto response = new ResponseDto(Constants.API_HALLTICKET_GET_PENDING_DATA);
        List<StudentExamRegistration> registrations = fetchPendingDataForHallTickets(courseId, examCycleId, instituteId);
        if (!registrations.isEmpty()) {
            List<PendingDataDto> pendingDataDtos = registrations.stream().map(this::toPendingDataDto).collect(Collectors.toList());
            response.put(Constants.MESSAGE, Constants.SUCCESSFUL);
            response.put(Constants.RESPONSE, pendingDataDtos);
            response.setResponseCode(HttpStatus.OK);
        } else {
            setErrorResponse(response, "NO_PENDING_HALLTICKETS", "No pending hall tickets found for the provided filters.", HttpStatus.NOT_FOUND);
        }
        return response;
    }

    public ResponseDto downloadProofForDataCorrectionRequest(Long requestId) {
        ResponseDto response = new ResponseDto(Constants.API_HALLTICKET_DOWNLOAD_PROOF);
        DataCorrectionRequest request = dataCorrectionRequestRepository.findById(requestId).orElse(null);
        if (request != null) {
            String proofPath = request.getProofAttachmentPath();
            Resource file = loadProofAsStreamResource(proofPath);
            response.put(Constants.MESSAGE, Constants.SUCCESSMESSAGE);
            response.put(Constants.RESPONSE, file);
            response.setResponseCode(HttpStatus.OK);
        } else {
            setErrorResponse(response, "PROOF_NOT_FOUND", "Proof not found for the request.", HttpStatus.NOT_FOUND);
        }
        return response;
    }

    public ResponseDto getProofUrlByRequestId(Long requestId) {
        ResponseDto response = new ResponseDto(Constants.API_HALLTICKET_GET_PROOF_URL_BY_REQUEST);
        DataCorrectionRequest request = dataCorrectionRequestRepository.findById(requestId).orElse(null);
        if (request != null) {
            String proofUrl = request.getProofAttachmentPath();
            response.put(Constants.MESSAGE, Constants.SUCCESSMESSAGE);
            response.put(Constants.RESPONSE, proofUrl);
            response.setResponseCode(HttpStatus.OK);
        } else {
            setErrorResponse(response, "PROOF_URL_NOT_FOUND", "Proof URL not found for the request.", HttpStatus.NOT_FOUND);
        }
        return response;
    }

    public void setErrorResponse(ResponseDto response, String code, String message, HttpStatus status) {
        ResponseDto.ErrorDetails errorDetails = new ResponseDto.ErrorDetails();
        errorDetails.setCode(code);
        errorDetails.setMessage(message);
        response.setError(errorDetails);
        response.setResponseCode(status);
    }
    private byte[] generateHallTicket(StudentExamRegistration registration) {
        PDDocument document = new PDDocument();
        // Get the exam cycle for the registration
        ExamCycle examCycle = registration.getExamCycle();
        if (examCycle == null) {
            throw new RuntimeException("Exam cycle not found for the registration.");
        }
        // Get all exams within the same exam cycle
        List<Exam> examsInCycle = examRepository.findByExamCycleId(examCycle.getId());

        if (examsInCycle == null || examsInCycle.isEmpty()) {
            throw new RuntimeException("No exams found in the exam cycle.");
        }

        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);

        try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
            // Title: Hall Ticket
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 20);
            contentStream.beginText();
            contentStream.newLineAtOffset(50, 750);
            contentStream.showText("Hall Ticket");
            contentStream.endText();

            // Student Details
            contentStream.setFont(PDType1Font.HELVETICA, 14);
            contentStream.beginText();
            contentStream.newLineAtOffset(50, 700);
            contentStream.showText("Name: " + registration.getStudent().getFirstName() + " " + registration.getStudent().getSurname());
            contentStream.newLineAtOffset(0, -20);
            contentStream.showText("Exam Enrollment Number: " + registration.getStudent().getEnrollmentNumber());
            contentStream.newLineAtOffset(0, -20);
            contentStream.showText("Date of Birth: " + registration.getStudent().getDateOfBirth());
            contentStream.newLineAtOffset(0, -20);
            contentStream.endText();

            int yOffset = 620;

            // Exam Details for each exam
            for (Exam exam : examsInCycle) {
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 14);
                contentStream.beginText();
                contentStream.newLineAtOffset(50, yOffset);
                contentStream.showText("Exam: " + exam.getExamName());
                yOffset -= 20;
                contentStream.newLineAtOffset(0, -20);
                contentStream.showText("Date: " + exam.getExamDate());
                yOffset -= 20;
                contentStream.newLineAtOffset(0, -20);
                contentStream.showText("Time: " + exam.getStartTime() + " - " + exam.getEndTime());
                yOffset -= 30; // Additional space between exams
                contentStream.endText();
            }

        } catch (IOException e) {
            throw new RuntimeException("Error generating hall ticket", e);
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            document.save(baos);
            document.close();
        } catch (IOException e) {
            throw new RuntimeException("Error saving hall ticket to byte array", e);
        }

        return baos.toByteArray();
    }
    private List<StudentExamRegistration> fetchPendingDataForHallTickets(Long courseId, Long examCycleId, Long instituteId) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<StudentExamRegistration> criteriaQuery = criteriaBuilder.createQuery(StudentExamRegistration.class);
        Root<StudentExamRegistration> registrationRoot = criteriaQuery.from(StudentExamRegistration.class);

        registrationRoot.fetch("examCycle", JoinType.INNER);
        registrationRoot.fetch("exam", JoinType.INNER);

        Join<StudentExamRegistration, ExamCycle> examCycleJoin = registrationRoot.join("examCycle");
        Join<StudentExamRegistration, Exam> examJoin = registrationRoot.join("exam");

        List<Predicate> predicates = new ArrayList<>();

        if (courseId != null) {
            predicates.add(criteriaBuilder.equal(examJoin.get("course").get("id"), courseId));
        }
        if (examCycleId != null) {
            predicates.add(criteriaBuilder.equal(examCycleJoin.get("id"), examCycleId));
        }
        if (instituteId != null) {
            predicates.add(criteriaBuilder.equal(registrationRoot.get("institute").get("id"), instituteId));
        }

        criteriaQuery.where(predicates.toArray(new Predicate[0]));

        return entityManager.createQuery(criteriaQuery).getResultList();
    }

    private PendingDataDto toPendingDataDto(StudentExamRegistration registration) {
        PendingDataDto dto = new PendingDataDto();

        // ... (existing fields mapping)
        dto.setId(registration.getId()); // setting the ID
        dto.setFirstName(registration.getStudent().getFirstName());
        dto.setLastName(registration.getStudent().getSurname());
        dto.setDob(registration.getStudent().getDateOfBirth()); // Assuming there's a dob field in Student
        dto.setCourseName(registration.getExam().getCourse().getCourseName());
        dto.setCourseYear(registration.getExam().getCourse().getCourseYear()); // Assuming there's a courseYear field in Course
        dto.setStudentEnrollmentNumber(registration.getStudent().getEnrollmentNumber());
        dto.setRegistrationDate(registration.getRegistrationDate());
        dto.setStatus(registration.getStatus());
        dto.setRemarks(registration.getRemarks());
        dto.setHallTicketStatus(registration.getHallTicketStatus());
        if (registration.getExamCenter() != null) {
            dto.setExamCenterName(registration.getExamCenter().getName());
        }
        dto.setFeesPaid(registration.isFeesPaid()); // Assuming there's a method to determine if fees are paid
        dto.setAttendancePercentage(computeAttendancePercentage(registration)); // Assuming there's a method to get attendance

        // Mapping ExamCycle details
        PendingDataDto.ExamCycleDetails examCycleDetails = new PendingDataDto.ExamCycleDetails();
        examCycleDetails.setId(registration.getExamCycle().getId());
        examCycleDetails.setName(registration.getExamCycle().getExamCycleName());
        examCycleDetails.setStartDate(registration.getExamCycle().getStartDate());
        examCycleDetails.setEndDate(registration.getExamCycle().getEndDate());

        // Fetch the exams associated with the exam cycle ID from the repository
        Long examCycleId = registration.getExamCycle().getId();
        List<Exam> examsForCycle = examRepository.findByExamCycleId(examCycleId);

        // Now, map these exams to your DTO
        List<PendingDataDto.ExamDetails> examDetailsList = examsForCycle.stream().map(exam -> {
            PendingDataDto.ExamDetails examDetails = new PendingDataDto.ExamDetails();
            examDetails.setExamName(exam.getExamName());
            examDetails.setExamDate(exam.getExamDate());
            examDetails.setStartTime(String.valueOf(exam.getStartTime()));
            examDetails.setEndTime(String.valueOf(exam.getEndTime()));
            //... map other relevant details
            return examDetails;
        }).collect(Collectors.toList());

        examCycleDetails.setExams(examDetailsList);
        dto.setExamCycle(examCycleDetails);

        return dto;
    }
    private double computeAttendancePercentage(StudentExamRegistration registration) {
        // Fetch the student enrollment number from the associated student
        String studentEnrollmentNumber = registration.getStudent().getEnrollmentNumber();

        // Check if an attendance record exists for this student enrollment number
        if (!attendanceRepository.existsByStudentEnrollmentNumber(studentEnrollmentNumber)) {
            return 0.0;
        }

        // Fetch the attendance record for the student
        AttendanceRecord attendanceRecord = attendanceRepository.findByStudentEnrollmentNumber(studentEnrollmentNumber);

        // Compute the attendance percentage based on the details in the attendance record
        double daysAttended = attendanceRecord.getPresentDays(); // Assuming this method exists
        double totalDays = attendanceRecord.getNumberOfWorkingDays(); // Assuming this method exists

        double attendancePercentage = (daysAttended / totalDays) * 100;

        return attendancePercentage;
    }

    private InputStreamResource loadProofAsStreamResource(String proofUrl) {
        RestTemplate restTemplate = new RestTemplate();

        ResponseEntity<InputStreamResource> response = restTemplate.exchange(
                proofUrl, HttpMethod.GET, null, InputStreamResource.class);

        if (response.getStatusCode() != HttpStatus.OK) {
            throw new RuntimeException("Failed to fetch proof: " + proofUrl);
        }

        return response.getBody();
    }

        public ResponseDto getDetailsByStudentIdAndExamCycleId(Long studentId, Long examCycleId) {
            ResponseDto response = new ResponseDto(Constants.API_HALLTICKET_GET_DETAILS_BY_STUDENT_AND_EXAM_CYCLE);

            Optional<StudentExamRegistration> optionalRegistration =
                    studentExamRegistrationRepository.findByStudentIdAndExamCycleId(studentId, examCycleId);

            if (!optionalRegistration.isPresent()) {
                ResponseDto.setErrorResponse(response, "NOT_FOUND", "No data found for given student ID and exam cycle ID.", HttpStatus.NOT_FOUND);
                return response;
            }

            StudentExamRegistration registration = optionalRegistration.get();

            Map<String, Object> formattedData = new HashMap<>();

            // Student details
            Student student = registration.getStudent();
            formattedData.put("firstName", student.getFirstName());
            formattedData.put("lastName", student.getSurname());
            formattedData.put("enrollmentNumber", student.getEnrollmentNumber());
            formattedData.put("dateOfBirth", student.getDateOfBirth());
            if(student.getCourse() != null) {
                formattedData.put("courseName", student.getCourse().getCourseName());
                formattedData.put("courseYear", student.getCourse().getCourseYear());
            }
            // Add Hall Ticket details
            if(registration.getHallTicketStatus() != null) {
                formattedData.put("hallTicketStatus", registration.getHallTicketStatus().toString());  // assuming HallTicketStatus is an enum
            }

            if(registration.getHallTicketGenerationDate() != null) {
                formattedData.put("hallTicketGenerationDate", registration.getHallTicketGenerationDate());
            }

            // Exam cycle details
            Map<String, Object> examCycleData = new HashMap<>();
            ExamCycle examCycle = registration.getExamCycle();

            examCycleData.put("id", examCycle.getId());
            examCycleData.put("examCyclename", examCycle.getExamCycleName());
            examCycleData.put("startDate", examCycle.getStartDate());
            examCycleData.put("endDate", examCycle.getEndDate());
            examCycleData.put("createdBy", examCycle.getCreatedBy());
            examCycleData.put("modifiedBy", examCycle.getModifiedBy());
            examCycleData.put("status", examCycle.getStatus());
            examCycleData.put("obsolete", examCycle.getObsolete());
            // Exams details
            List<Exam> exams = examRepository.findByExamCycleId(examCycle.getId());
            List<Map<String, Object>> examsData = new ArrayList<>();

            for (Exam exam : exams) {
                Map<String, Object> examData = new HashMap<>();

                examData.put("examName", exam.getExamName());
                examData.put("examDate", exam.getExamDate());
                examData.put("startTime", exam.getStartTime());
                examData.put("endTime", exam.getEndTime());
                examData.put("createdBy", exam.getCreatedBy());
                examData.put("modifiedBy", exam.getModifiedBy());
                examData.put("isResultsPublished", exam.getIsResultsPublished());
                examData.put("obsolete", exam.getObsolete());

                examsData.add(examData);
            }

            // Add examsData list inside examCycleData
            examCycleData.put("exams", examsData);

            formattedData.put("examCycle", examCycleData);

            response.put(Constants.RESPONSE, formattedData);
            response.setResponseCode(HttpStatus.OK);

            return response;
        }
}
