package com.tarento.upsmf.examsAndAdmissions.model.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class ProcessedResultDto {
    private boolean hasInternalMarks;
    private boolean hasFinalMarks;
    private boolean hasRevisedFinalMarks;
    private boolean hasPublished;
    private String instituteName;
    private Long instituteId;
    private String course;
}
