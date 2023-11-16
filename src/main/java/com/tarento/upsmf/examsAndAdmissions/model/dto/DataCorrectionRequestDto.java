package com.tarento.upsmf.examsAndAdmissions.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DataCorrectionRequestDto {
    private Long id;
    private String updatedFirstName;
    private String updatedLastName;
    private LocalDate updatedDOB;
    private String updatedCourseYear;
    private String status;
    private String proofAttachmentPath;
}
