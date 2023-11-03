package com.tarento.upsmf.examsAndAdmissions.model.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ExamDetailsDTO {
    private String examName;
    private Integer internalMarks;
    private Integer externalMarks;
    private Integer totalMarks;
    private String grade;
    private String result;
    private String status;
    private Long examId;
}
