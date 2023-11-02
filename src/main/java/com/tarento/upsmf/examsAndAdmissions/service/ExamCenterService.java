package com.tarento.upsmf.examsAndAdmissions.service;

import com.tarento.upsmf.examsAndAdmissions.enums.ApprovalStatus;
import com.tarento.upsmf.examsAndAdmissions.model.*;
import com.tarento.upsmf.examsAndAdmissions.model.dto.CCTVStatusUpdateDTO;
import com.tarento.upsmf.examsAndAdmissions.model.dto.ExamCenterDTO;
import com.tarento.upsmf.examsAndAdmissions.repository.ExamCenterRepository;
import com.tarento.upsmf.examsAndAdmissions.repository.ExamCycleRepository;
import com.tarento.upsmf.examsAndAdmissions.repository.InstituteRepository;
import com.tarento.upsmf.examsAndAdmissions.repository.StudentExamRegistrationRepository;
import com.tarento.upsmf.examsAndAdmissions.util.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.*;

@Service
@Slf4j
public class ExamCenterService {

    @Autowired
    private InstituteRepository instituteRepository;
    @Autowired
    private StudentExamRegistrationRepository studentExamRegistrationRepository;
    @Autowired
    private ExamCenterRepository examCenterRepository;
    @Autowired
    private ExamCycleRepository examCycleRepository;
    @Autowired
    private ExamCenterMapper examCenterMapper;

    public ResponseDto getVerifiedExamCentersInDistrict(String district) {
        ResponseDto response = new ResponseDto(Constants.API_GET_VERIFIED_EXAM_CENTERS);
        List<ExamCenter> examCenters = examCenterRepository.findByDistrictAndApprovalStatus(district, ApprovalStatus.APPROVED);
        if (!examCenters.isEmpty()) {
            List<ExamCenterDTO> examCenterDTOs = examCenterMapper.toDTOs(examCenters);
            response.put(Constants.MESSAGE, "Successful.");
            response.put(Constants.RESPONSE, examCenterDTOs);
            response.setResponseCode(HttpStatus.OK);
        } else {
            ResponseDto.setErrorResponse(response, "NO_CENTERS_FOUND", "No verified exam centers found for the given district.", HttpStatus.NOT_FOUND);
        }
        return response;
    }
    @Transactional
    public ResponseDto assignAlternateExamCenter(Long unverifiedExamCenterId, Long alternateExamCenterId) {
        ResponseDto response = new ResponseDto("API_ASSIGN_ALTERNATE_EXAM_CENTER");

        try {
            // Fetch the unverified exam center
            ExamCenter unverifiedExamCenter = examCenterRepository.findById(unverifiedExamCenterId)
                    .orElseThrow(() -> new EntityNotFoundException("Unverified Exam Center not found"));

            // Fetch the alternate exam center
            ExamCenter alternateExamCenter = examCenterRepository.findById(alternateExamCenterId)
                    .orElseThrow(() -> new EntityNotFoundException("Alternate Exam Center not found"));

            // Ensure both the exam centers belong to the same district
            if (!unverifiedExamCenter.getDistrict().equals(alternateExamCenter.getDistrict())) {
                throw new IllegalArgumentException("Unverified and Alternate Exam Centers do not belong to the same district.");
            }

            // Fetch all student registrations where the exam center is null
            List<StudentExamRegistration> affectedRegistrations = studentExamRegistrationRepository.findByExamCenterIsNullAndInstitute(unverifiedExamCenter.getInstitute());

            // Update the exam center for these registrations
            for (StudentExamRegistration registration : affectedRegistrations) {
                registration.setExamCenter(alternateExamCenter);
                registration.setAlternateExamCenterAssigned(true);  // This is the new change
            }

            // Set the alternate exam center for the unverified exam center
            unverifiedExamCenter.setAlternateExamCenter(alternateExamCenter);
            unverifiedExamCenter.setAlternateExamCenterAssigned(true);
            examCenterRepository.save(unverifiedExamCenter);

            // Save the updated registrations
            List<StudentExamRegistration> updatedRegistrations = studentExamRegistrationRepository.saveAll(affectedRegistrations);

            response.put("message", "Alternate Exam Center assigned successfully.");
            response.put(Constants.RESPONSE, updatedRegistrations);
            response.setResponseCode(HttpStatus.OK);

        } catch (EntityNotFoundException e) {
            ResponseDto.setErrorResponse(response, "NOT_FOUND", e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (IllegalArgumentException e) {
            ResponseDto.setErrorResponse(response, "INVALID_ARGUMENT", e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            ResponseDto.setErrorResponse(response, "GENERAL_ERROR", "An unexpected error occurred: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return response;
    }

    public ResponseDto updateCCTVStatus(Long examCenterId, CCTVStatusUpdateDTO updateDTO) {
        ResponseDto response = new ResponseDto(Constants.API_UPDATE_CCTV_STATUS);
        ExamCenter center = examCenterRepository.findById(examCenterId).orElse(null);
        if (center == null) {
            ResponseDto.setErrorResponse(response, "CENTER_NOT_FOUND", "Exam center not found.", HttpStatus.NOT_FOUND);
            return response;
        }

        center.setIpAddress(updateDTO.getIpAddress());
        center.setRemarks(updateDTO.getRemarks());
        center.setApprovalStatus(updateDTO.getApprovalStatus());
        ExamCenter updatedCenter = examCenterRepository.save(center);

        // Convert the updated center to DTO (assuming you have a method for this conversion)
        ExamCenterDTO updatedCenterDTO = examCenterMapper.toDTO(updatedCenter);

        response.put(Constants.MESSAGE, "CCTV status updated successfully.");
        response.put(Constants.RESPONSE, updatedCenterDTO); // Return the updated center in the response
        response.setResponseCode(HttpStatus.OK);
        return response;
    }

    private ExamCenter convertInstituteToExamCenter(Institute institute, ExamCycle examCycle) {
        ExamCenter center = new ExamCenter();
        center.setExamCycle(examCycle);
        center.setInstitute(institute);
        center.setName(institute.getInstituteName());
        center.setAddress(institute.getAddress());
        center.setDistrict(institute.getDistrict());
        center.setApprovalStatus(ApprovalStatus.APPROVED);
        return center;
    }

    public ResponseDto getExamCentersByStatus(Long examCycleId, Boolean isVerifiedStatus) {
        ResponseDto response = new ResponseDto(Constants.API_GET_EXAM_CENTERS_BY_STATUS);
        ExamCycle examCycle = examCycleRepository.findById(examCycleId).orElse(null);
        if (examCycle != null) {
            List<ExamCenter> examCenters = examCenterRepository.findByExamCycleAndApprovalStatus(examCycle, ApprovalStatus.APPROVED);
            if (!examCenters.isEmpty()) {
                response.put(Constants.MESSAGE, "Successful.");
                response.put(Constants.RESPONSE, examCenters);
                response.setResponseCode(HttpStatus.OK);
            } else {
                ResponseDto.setErrorResponse(response, "NO_CENTERS_FOUND", "No exam centers found for the given criteria.", HttpStatus.NOT_FOUND);
            }
        } else {
            ResponseDto.setErrorResponse(response, "EXAM_CYCLE_NOT_FOUND", "Exam cycle not found.", HttpStatus.NOT_FOUND);
        }
        return response;
    }
    public ResponseDto getAllExamCenters() {
        ResponseDto response = new ResponseDto(Constants.API_GET_ALL_EXAM_CENTERS);
        List<ExamCenter> examCenters = examCenterRepository.findAll();

        if (!examCenters.isEmpty()) {
            List<ExamCenterDTO> examCenterDTOs = examCenterMapper.toDTOs(examCenters);
            response.put(Constants.MESSAGE, "Successful.");
            response.put(Constants.RESPONSE, examCenterDTOs);
            response.setResponseCode(HttpStatus.OK);
        } else {
            ResponseDto.setErrorResponse(response, "NO_CENTERS_FOUND", "No exam centers found.", HttpStatus.NOT_FOUND);
        }

        return response;
    }
    public ResponseDto getExamCentersByExamCycle(Long examCycleId) {
        ResponseDto response = new ResponseDto(Constants.API_GET_EXAM_CENTERS_BY_EXAM_CYCLE);
        ExamCycle examCycle = examCycleRepository.findById(examCycleId).orElse(null);
        if (examCycle != null) {
            List<ExamCenter> examCenters = examCenterRepository.findByExamCycle(examCycle);
            if (!examCenters.isEmpty()) {
                List<ExamCenterDTO> dtos = examCenterMapper.toDTOs(examCenters);
                response.put(Constants.MESSAGE, "Successful.");
                response.put(Constants.RESPONSE, dtos); // Put DTOs in the response
                response.setResponseCode(HttpStatus.OK);
            } else {
                ResponseDto.setErrorResponse(response, "NO_CENTERS_FOUND", "No exam centers found for the given exam cycle.", HttpStatus.NOT_FOUND);
            }
        } else {
            ResponseDto.setErrorResponse(response, "EXAM_CYCLE_NOT_FOUND", "Exam cycle not found.", HttpStatus.NOT_FOUND);
        }
        return response;
    }
    public ResponseDto getVerifiedCenterByInstituteCode(String instituteCode) {
        ResponseDto response = new ResponseDto(Constants.API_GET_VERIFIED_EXAM_CENTER);

        Optional<ExamCenter> examCenterOpt = examCenterRepository.findByInstituteCodeAndApprovalStatus(instituteCode, ApprovalStatus.APPROVED);

        if (examCenterOpt.isPresent()) {
            ExamCenterDTO examCenterDTO = examCenterMapper.toDTO(examCenterOpt.get());
            response.put(Constants.MESSAGE, "Successful.");
            response.put(Constants.RESPONSE, examCenterDTO);
            response.setResponseCode(HttpStatus.OK);
        } else {
            ResponseDto.setErrorResponse(response, "NO_VERIFIED_CENTER_FOUND", "No verified exam center found for the provided institute code.", HttpStatus.NOT_FOUND);
        }

        return response;
    }
    public ResponseDto getExamCenterStatus(Long examCycleId, Long examCenterId) {
        ResponseDto response = new ResponseDto("API_GET_EXAM_CENTER_STATUS");

        Optional<ExamCenter> examCenterOpt = examCenterRepository.findByExamCycleIdAndId(examCycleId, examCenterId);

        if (examCenterOpt.isPresent()) {
            ExamCenterDTO examCenterDTO = examCenterMapper.toDTO(examCenterOpt.get());

            // Check for null value in approvalStatus property
            if (examCenterDTO.getApprovalStatus() == null) {
                ResponseDto.setErrorResponse(response, "DATA_INCONSISTENCY", "Data inconsistency found. Please check the database.", HttpStatus.INTERNAL_SERVER_ERROR);
                return response;
            }

            Map<String, String> innerResponse = new HashMap<>();
            innerResponse.put("ApprovalStatus", examCenterDTO.getApprovalStatus().toString());

            List<Map<String, String>> innerResponseList = new ArrayList<>();
            innerResponseList.add(innerResponse);

            response.put(Constants.MESSAGE, "Successful.");
            response.put(Constants.RESPONSE, innerResponseList);
            response.setResponseCode(HttpStatus.OK);
        } else {
            ResponseDto.setErrorResponse(response, "NO_EXAM_CENTER_FOUND", "No exam center found for the provided exam cycle and exam center ID.", HttpStatus.NOT_FOUND);
        }

        return response;
    }
}
