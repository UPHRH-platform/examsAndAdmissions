package com.tarento.upsmf.examsAndAdmissions.service;

import com.tarento.upsmf.examsAndAdmissions.model.ExamCenter;
import com.tarento.upsmf.examsAndAdmissions.model.dto.ExamCenterDTO;
import com.tarento.upsmf.examsAndAdmissions.model.dto.InstituteDTO;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ExamCenterMapper {
    public ExamCenterDTO toDTO(ExamCenter examCenter) {
        ExamCenterDTO dto = new ExamCenterDTO();
        dto.setId(examCenter.getId());
        dto.setName(examCenter.getName());
        dto.setAddress(examCenter.getAddress());
        dto.setApprovalStatus(examCenter.getApprovalStatus());
        dto.setIpAddress(examCenter.getIpAddress());
        dto.setExamCycle(examCenter.getExamCycle().getId());
        dto.setRemarks(examCenter.getRemarks());
        dto.setDistrict(examCenter.getDistrict());
        dto.setInstituteCode(examCenter.getInstituteCode());
        dto.setAllowedForExamCentre(examCenter.isAllowedForExamCentre());
        dto.setAlternateExamCenterAssigned(examCenter.getAlternateExamCenterAssigned());
        return dto;
    }

    public List<ExamCenterDTO> toDTOs(List<ExamCenter> examCenters) {
        return examCenters.stream().map(this::toDTO).collect(Collectors.toList());
    }
}
