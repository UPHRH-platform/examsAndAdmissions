package com.tarento.upsmf.examsAndAdmissions.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tarento.upsmf.examsAndAdmissions.enums.ResultStatus;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.Date;

@Entity
@Table(name = "student_results")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class StudentResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "student_id")
    private Student student;
    @JsonProperty("First Name")
    private String firstName;
    @JsonProperty("Last Name")
    private String lastName;
    @JsonProperty("Enrolment Number")
    private String enrollmentNumber;
    @JsonProperty("Mother's Name")
    private String motherName;
    @JsonProperty("Father's Name")
    private String fatherName;

    @ManyToOne
    @JoinColumn(name = "course_id")
    private Course course;
    @JsonProperty("Course")
    private String course_name;

    @ManyToOne
    @JoinColumn(name = "exam_cycle_id")
    private ExamCycle examCycle;
    @JsonProperty("Exam Cycle")
    private String examCycle_name;

    @ManyToOne
    @JoinColumn(name = "exam_id")
    private Exam exam;
    @JsonProperty("Exam")
    private String exam_name;

    @JsonProperty("Internal Marks")
    private Integer internalMarks;
    @JsonProperty("Passing Internal Marks")
    private Integer passingInternalMarks;
    @JsonProperty("Internal Marks Obtained")
    private Integer internalMarksObtained;
    @JsonProperty("Practical Marks")
    private Integer practicalMarks;
    @JsonProperty("Passing Practical Marks")
    private Integer passingPracticalMarks;
    @JsonProperty("Practical Marks Obtained")
    private Integer practicalMarksObtained;
    @JsonProperty("Other Marks")
    private Integer otherMarks;
    @JsonProperty("Passing Other Marks")
    private Integer passingOtherMarks;
    @JsonProperty("Other Marks Obtained")
    private Integer otherMarksObtained;
    @JsonProperty("External Marks")
    private Integer externalMarks;
    @JsonProperty("Passing External Marks")
    private Integer passingExternalMarks;
    @JsonProperty("External Marks Obtained")
    private Integer externalMarksObtained;
    @JsonProperty("Total Marks")
    private Integer totalMarks;
    @JsonProperty("Passing Total Marks")
    private Integer passingTotalMarks;
    @JsonProperty("Total Marks Obtained")
    private Integer totalMarksObtained;
    @JsonProperty("Grade")
    private String grade;
    @JsonProperty("Result")
    private String result;
    private Date publishedDate;

    @Enumerated(EnumType.STRING)
    private ResultStatus status = ResultStatus.ENTERED;

    private boolean published;
    private boolean internalMarkFlag;
    private boolean finalMarkFlag;
    private boolean revisedFinalMarkFlag;
    private LocalDate lastDateToUploadInternalMarks;
    private long instituteId;
}