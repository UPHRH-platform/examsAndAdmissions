package com.tarento.upsmf.examsAndAdmissions.model.dto;

import com.tarento.upsmf.examsAndAdmissions.enums.ResultStatus;
import lombok.*;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@Getter
@Setter
public class StudentResultDTO {

    private String firstName;
    private String lastName;
    private String enrollmentNumber;
    private LocalDate dateOfBirth;
    private String courseName;
    private String courseYear;
    private Date publishedDate;
    private List<ExamDetailsDTO> examDetails;
}
