package com.tarento.upsmf.examsAndAdmissions.controller;

import com.tarento.upsmf.examsAndAdmissions.model.Institute;
import com.tarento.upsmf.examsAndAdmissions.model.ResponseDto;
import com.tarento.upsmf.examsAndAdmissions.model.dto.ApprovalRejectionDTO;
import com.tarento.upsmf.examsAndAdmissions.model.dto.InstituteUserDto;
import com.tarento.upsmf.examsAndAdmissions.service.DispatchTrackerService;
import com.tarento.upsmf.examsAndAdmissions.service.InstituteService;
import com.tarento.upsmf.examsAndAdmissions.util.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;

import java.util.Map;
import java.util.Optional;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/institutes")
public class InstituteController {

    @Autowired
    private DispatchTrackerService dispatchTrackerService;
    private final InstituteService instituteService;
    ResponseDto response = new ResponseDto();

    @Autowired
    public InstituteController(InstituteService instituteService) {
        this.instituteService = instituteService;
    }

    @PostMapping("/create")
    public ResponseEntity<ResponseDto> createInstitute(@RequestBody Institute institute) {
        Institute result = instituteService.createInstitute(institute);
        return FeeController.handleSuccessResponse(result);
    }

    @PutMapping("/{id}/update")
    public ResponseEntity<ResponseDto> updateInstitute(@PathVariable Long id, @RequestBody Institute updatedInstitute) {
        Institute result = instituteService.updateInstitute(id, updatedInstitute);
        return FeeController.handleSuccessResponse(result);
    }

    @PostMapping("/verify")
    public ResponseEntity<?> updateVerificationStatus(@RequestBody ApprovalRejectionDTO dto) {
        Institute result = instituteService.updateVerificationStatus(dto);
        return FeeController.handleSuccessResponse(result);
    }

    @PutMapping("/{id}/mark-not-allowed")
    public ResponseEntity<?> markNotAllowedForExamCentre(@PathVariable Long id) {
        Institute result = instituteService.markNotAllowedForExamCentre(id);
        return FeeController.handleSuccessResponse(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getInstituteById(@PathVariable String id) {
        try {
            Optional<Institute> institute = instituteService.getInstituteById(id);
            if (institute.isPresent()) {
                return FeeController.handleSuccessResponse(institute);
            } else {
                response.setResponseCode(Constants.NOT_FOUND);
                response.getResult().put("message", "Institute not found.");
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return FeeController.handleErrorResponse(e);
        }
    }

    @PostMapping("/dispatchUpload")
    public ResponseEntity<?> uploadDispatchProof(
            @RequestParam Long examCycleId,
            @RequestParam Long examId,
            @RequestParam MultipartFile dispatchProofFile,
            @RequestParam LocalDate dispatchDate) {
        ResponseDto response = new ResponseDto();
        try {
            ResponseDto responseData = dispatchTrackerService.uploadDispatchProof(examCycleId, examId, dispatchProofFile, dispatchDate);
            responseData.put("responseCode", Constants.SUCCESSFUL);
            responseData.put("message", "Dispatch proof uploaded successfully.");
            return new ResponseEntity<>(responseData, response.getResponseCode());
        } catch (IOException e) {
            response.put("responseCode", Constants.INTERNAL_SERVER_ERROR);
            response.put("message", "Error uploading dispatch proof.");
            return new ResponseEntity<>(response, response.getResponseCode());
        }
    }

    @GetMapping("/dispatchList")
    public ResponseEntity<?> getDispatchList(
            @RequestParam Long examCycleId,
            @RequestParam Long examId) {

        ResponseDto responseData = dispatchTrackerService.getDispatchList(examCycleId, examId);

        if (responseData != null) {
            response.put("responseCode", Constants.SUCCESSFUL);
            response.put("dispatchList", response.getResult());
            return new ResponseEntity<>(responseData, response.getResponseCode());
        } else {
            response.put("responseCode", Constants.NOT_FOUND);
            response.put("message", "No dispatch records found.");
            return new ResponseEntity<>(response, response.getResponseCode());
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ResponseDto> getInstituteByUser(@PathVariable("userId") String userId) {
        try {
            List<Institute> instituteList = instituteService.getInstituteByUserId(userId);
            return FeeController.handleSuccessResponse(instituteList);
        } catch (Exception e) {
            return FeeController.handleErrorResponse(e);
        }
    }

    @PostMapping("/assign/user")
    public ResponseEntity<ResponseDto> addInstituteUserMapping(@RequestBody InstituteUserDto instituteUserDto) {
        try {
            Boolean isAdded = instituteService.addInstituteUserMapping(instituteUserDto);
            return FeeController.handleSuccessResponse(isAdded);
        } catch (Exception e) {
            return FeeController.handleErrorResponse(e);
        }
    }
}
