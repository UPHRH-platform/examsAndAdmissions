package com.tarento.upsmf.examsAndAdmissions.controller;

import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.tarento.upsmf.examsAndAdmissions.model.RejectionReasonRequest;
import com.tarento.upsmf.examsAndAdmissions.model.ResponseDto;
import com.tarento.upsmf.examsAndAdmissions.model.dto.DataCorrectionRequest;
import com.tarento.upsmf.examsAndAdmissions.service.HallTicketService;
import com.tarento.upsmf.examsAndAdmissions.util.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api/v1/admin/hallticket")
@Slf4j
public class HallTicketController {

    @Autowired
    HallTicketService hallTicketService;

    @PostMapping("/generateHallTickets")
    public ResponseEntity<ResponseDto> generateHallTicketsForMultipleStudents(@RequestBody List<Long> studentRegistrationIds) throws IOException {
        ResponseDto responseDto = hallTicketService.generateAndSaveHallTicketsForMultipleStudents(studentRegistrationIds);
        return ResponseEntity.status(responseDto.getResponseCode()).body(responseDto);
    }
    @GetMapping("/downloadHallTicket")
    public ResponseEntity<ResponseDto> downloadStudentHallTicket(@RequestParam Long id, @RequestParam String dateOfBirth) {
        ResponseDto response;
        try {
            response = hallTicketService.getHallTicketBlobResourcePath(id, dateOfBirth);
        } catch (Exception e) {
            log.error("Error fetching hall ticket for student ID: {} with Date of Birth: {}", id, dateOfBirth, e);
            response = new ResponseDto(Constants.API_HALLTICKET_GET);
            ResponseDto.setErrorResponse(response, "GENERAL_ERROR", e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return ResponseEntity.status(response.getResponseCode()).body(response);
    }

    @GetMapping("/examCycle")
    public ResponseEntity<ResponseDto> downloadStudentHallTicketByStudentId(@RequestParam Long id, @RequestParam Long examCycleId) {
        ResponseDto response;
        try {
            response = hallTicketService.getHallTicketBlobResourcePathByStudentId(id, examCycleId);
        } catch (Exception e) {
            log.error("Error fetching hall ticket for student ID: {}", id, e);
            response = new ResponseDto(Constants.API_HALLTICKET_GET);
            ResponseDto.setErrorResponse(response, "GENERAL_ERROR", e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return ResponseEntity.status(response.getResponseCode()).body(response);
    }

    private Resource extractResourceFromResponseDto(ResponseDto responseDto) {
        String base64EncodedData = (String) responseDto.get(Constants.RESPONSE);

        if (base64EncodedData == null || base64EncodedData.isEmpty()) {
            throw new RuntimeException("No data found in ResponseDto");
        }

        byte[] hallTicketData = Base64.getDecoder().decode(base64EncodedData);
        return new ByteArrayResource(hallTicketData);
    }

    @PostMapping("/dataCorrection/request")
    public ResponseEntity<ResponseDto> requestDataCorrection(
            @RequestParam("studentId") Long studentId,
            @RequestParam("examCycleId") Long examCycleId,
            @RequestParam(value = "updatedFirstName", required = false) Optional<String> updatedFirstNameOpt,
            @RequestParam(value = "updatedLastName", required = false) Optional<String> updatedLastNameOpt,
            @RequestParam(value = "updatedDOB", required = false) Optional<String> dobOpt, // Taking as string for optional handling
            @RequestParam(value = "updatedCourseYear",required = false) Optional<String> courseYear,
            @RequestParam("proof") MultipartFile proof) throws IOException {

        // Convert Optional<String> to actual value or null if not present
        String updatedFirstName = updatedFirstNameOpt.orElse(null);
        String updatedLastName = updatedLastNameOpt.orElse(null);
        LocalDate updatedDOB = dobOpt.isPresent() ? LocalDate.parse(dobOpt.get()) : null;
        String updatedCourseYear = courseYear.orElse(null);

        ResponseDto responseDto = hallTicketService.requestHallTicketDataCorrection(studentId,examCycleId, updatedFirstName, updatedLastName, updatedDOB,updatedCourseYear, proof);
        return ResponseEntity.status(responseDto.getResponseCode().value()).body(responseDto);
    }


    @GetMapping("/dataCorrection/requests")
    public ResponseEntity<ResponseDto> viewDataCorrectionRequests(@RequestParam(value = "examCycleId", required = false) Optional<Long> examCycleId) {
        ResponseDto responseDto = hallTicketService.getAllDataCorrectionRequests(examCycleId);
        return ResponseEntity.status(responseDto.getResponseCode().value()).body(responseDto);
    }

    @PostMapping("/dataCorrection/{requestId}/approve")
    public ResponseEntity<ResponseDto> approveDataCorrection(@PathVariable Long requestId) {
        ResponseDto responseDto = hallTicketService.approveDataCorrection(requestId);
        return ResponseEntity.status(responseDto.getResponseCode().value()).body(responseDto);
    }

    @PostMapping("/dataCorrection/{requestId}/reject")
    public ResponseEntity<ResponseDto> rejectDataCorrection(
            @PathVariable Long requestId,
            @RequestBody RejectionReasonRequest rejectionReasonRequest) {

        ResponseDto responseDto = hallTicketService.rejectDataCorrection(requestId, rejectionReasonRequest.getRejectionReason());
        return ResponseEntity.status(responseDto.getResponseCode().value()).body(responseDto);
    }

    @GetMapping("/hallTicketRegistrationDetails")
    public ResponseEntity<ResponseDto> getPendingData(
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) Long examCycleId,
            @RequestParam(required = false) Long instituteId) {
        ResponseDto responseDto = hallTicketService.getPendingDataForHallTickets(courseId, examCycleId, instituteId);
        return ResponseEntity.status(responseDto.getResponseCode().value()).body(responseDto);
    }

    @GetMapping("/downloadProof/{requestId}")
    public ResponseEntity<ResponseDto> downloadProof(@PathVariable Long requestId) {
        ResponseDto responseDto = hallTicketService.getProofUrlByRequestId(requestId);
        return ResponseEntity.status(responseDto.getResponseCode().value()).body(responseDto);
    }
    @GetMapping("/hallTicketDetail")
    public ResponseDto getDetailsByStudentIdAndExamCycleId(
            @RequestParam("studentId") Long studentId,
            @RequestParam("examCycleId") Long examCycleId) {

        return hallTicketService.getDetailsByStudentIdAndExamCycleId(studentId, examCycleId);
    }
}