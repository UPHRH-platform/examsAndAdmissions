package com.tarento.upsmf.examsAndAdmissions.service;

import com.tarento.upsmf.examsAndAdmissions.enums.ApprovalStatus;
import com.tarento.upsmf.examsAndAdmissions.model.*;
import com.tarento.upsmf.examsAndAdmissions.model.dto.DataCorrectionRequest;
import com.tarento.upsmf.examsAndAdmissions.model.dto.PendingDataDto;
import com.tarento.upsmf.examsAndAdmissions.repository.*;
import com.tarento.upsmf.examsAndAdmissions.util.Constants;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
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
    public ResponseDto getHallTicket(Long id, String dateOfBirth) {
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

        Optional<StudentExamRegistration> studentOptional = studentExamRegistrationRepository.findByIdAndStudent_DateOfBirth(id, localDate);

        if (!studentOptional.isPresent()) {
            setErrorResponse(response, "STUDENT_NOT_FOUND", "No student record found for the provided details.", HttpStatus.NOT_FOUND);
            return response;
        }

        StudentExamRegistration registration = studentOptional.get();
        byte[] hallTicketData;
        try {
            hallTicketData = generateHallTicket(registration);
            response.put(Constants.MESSAGE, Constants.SUCCESSMESSAGE);
            // Assuming you want to encode the PDF as a base64 string for the response
            response.put(Constants.RESPONSE, Base64.getEncoder().encodeToString(hallTicketData));
            response.setResponseCode(HttpStatus.OK);
        } catch (Exception e) {
            // Handle any exception that might occur during hall ticket generation
            setErrorResponse(response, "HALL_TICKET_GENERATION_ERROR", "Error generating hall ticket", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return response;
    }

    public ResponseDto requestHallTicketDataCorrection(Long studentId, String correctionDetails, @RequestParam("file") MultipartFile proof) throws IOException {
        ResponseDto response = new ResponseDto(Constants.API_HALLTICKET_REQUEST_DATA_CORRECTION);
        DataCorrectionRequest request = new DataCorrectionRequest();
        request.setStudentId(studentId);
        request.setRequestedCorrection(correctionDetails);
        request.setStatus("NEW");
        if (proof != null && !proof.isEmpty()) {
            String path = studentService.storeFile(proof);
            request.setProofAttachmentPath(path);
        }
        dataCorrectionRequestRepository.save(request);
        response.put(Constants.MESSAGE, Constants.SUCCESSMESSAGE);
        response.put(Constants.RESPONSE, request);
        response.setResponseCode(HttpStatus.OK);
        return response;
    }

    public ResponseDto getAllDataCorrectionRequests() {
        ResponseDto response = new ResponseDto(Constants.API_HALLTICKET_GET_ALL_DATA_CORRECTION_REQUESTS);
        List<DataCorrectionRequest> requests = dataCorrectionRequestRepository.findAll();
        if (!requests.isEmpty()) {
            response.put(Constants.MESSAGE, Constants.SUCCESSFUL);
            response.put(Constants.RESPONSE, requests);
            response.setResponseCode(HttpStatus.OK);
        } else {
            setErrorResponse(response, "NO_DATA_CORRECTION_REQUESTS", "No data correction requests found.", HttpStatus.NOT_FOUND);
        }
        return response;
    }

    public ResponseDto approveDataCorrection(Long requestId) {
        ResponseDto response = new ResponseDto(Constants.API_HALLTICKET_APPROVE_DATA_CORRECTION);
        DataCorrectionRequest request = dataCorrectionRequestRepository.findById(requestId).orElse(null);
        if (request != null) {
            request.setStatus("APPROVED");
            dataCorrectionRequestRepository.save(request);
            response.put(Constants.MESSAGE, Constants.SUCCESSMESSAGE);
            response.put(Constants.RESPONSE, request);
            response.setResponseCode(HttpStatus.OK);
        } else {
            setErrorResponse(response, "REQUEST_NOT_FOUND", "Request not found.", HttpStatus.NOT_FOUND);
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

        List<Predicate> predicates = new ArrayList<>();

        if (courseId != null) {
            predicates.add(criteriaBuilder.equal(registrationRoot.get("exam").get("course").get("id"), courseId));
        }
        if (examCycleId != null) {
            predicates.add(criteriaBuilder.equal(registrationRoot.get("examCycle").get("id"), examCycleId));
        }
        if (instituteId != null) {
            predicates.add(criteriaBuilder.equal(registrationRoot.get("institute").get("id"), instituteId));
        }

        criteriaQuery.where(predicates.toArray(new Predicate[0]));

        return entityManager.createQuery(criteriaQuery).getResultList();
    }
    private PendingDataDto toPendingDataDto(StudentExamRegistration registration) {
        PendingDataDto dto = new PendingDataDto();
        dto.setFirstName(registration.getStudent().getFirstName());
        dto.setLastName(registration.getStudent().getSurname());
        dto.setCourseName(registration.getExam().getCourse().getCourseName());
        dto.setStudentEnrollmentNumber(registration.getStudent().getEnrollmentNumber());
        dto.setRegistrationDate(registration.getRegistrationDate());
        dto.setStatus(registration.getStatus());
        dto.setRemarks(registration.getRemarks());
        dto.setFeesPaid(registration.isFeesPaid());
        return dto;
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
}
