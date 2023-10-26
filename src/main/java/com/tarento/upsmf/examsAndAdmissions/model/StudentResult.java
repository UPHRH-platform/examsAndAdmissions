package com.tarento.upsmf.examsAndAdmissions.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tarento.upsmf.examsAndAdmissions.enums.ResultStatus;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDate;

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

    @Enumerated(EnumType.STRING)
    private ResultStatus status = ResultStatus.ENTERED;

    private boolean published;
    private boolean internalMarkFlag;
    private boolean finalMarkFlag;
    private boolean revisedFinalMarkFlag;
    private LocalDate lastDateToUploadInternalMarks;
    private long instituteId;

    public StudentResult(Student student, String firstName, String lastName, String enrollmentNumber, Institute institute,
                           String motherName, String fatherName, Course course, String course_name,
                           ExamCycle examCycle, String examCycle_name, Exam exam, String exam_name,
                           Integer internalMarks, Integer passingInternalMarks, Integer internalMarksObtained,
                           Integer practicalMarks, Integer passingPracticalMarks, Integer practicalMarksObtained,
                           Integer otherMarks, Integer passingOtherMarks, Integer otherMarksObtained,
                           Integer externalMarks, Integer passingExternalMarks, Integer externalMarksObtained,
                           Integer totalMarks, Integer passingTotalMarks, Integer totalMarksObtained,
                           String grade, String result, ResultStatus status, boolean published, boolean internalMarkFlag, boolean finalMarkFlag, boolean revisedFinalMarkFlag,LocalDate lastDateToUploadInternalMarks, long instituteId) {

        this.student = student;
        this.firstName = firstName;
        this.lastName = lastName;
        this.enrollmentNumber = enrollmentNumber;
        this.motherName = motherName;
        this.fatherName = fatherName;
        this.course = course;
        this.course_name = course_name;
        this.examCycle = examCycle;
        this.examCycle_name = examCycle_name;
        this.exam = exam;
        this.exam_name = exam_name;
        this.internalMarks = internalMarks;
        this.passingInternalMarks = passingInternalMarks;
        this.internalMarksObtained = internalMarksObtained;
        this.practicalMarks = practicalMarks;
        this.passingPracticalMarks = passingPracticalMarks;
        this.practicalMarksObtained = practicalMarksObtained;
        this.otherMarks = otherMarks;
        this.passingOtherMarks = passingOtherMarks;
        this.otherMarksObtained = otherMarksObtained;
        this.externalMarks = externalMarks;
        this.passingExternalMarks = passingExternalMarks;
        this.externalMarksObtained = externalMarksObtained;
        this.totalMarks = totalMarks;
        this.passingTotalMarks = passingTotalMarks;
        this.totalMarksObtained = totalMarksObtained;
        this.grade = grade;
        this.result = result;
        this.status = status;
        this.published = published;
        this.internalMarkFlag = internalMarkFlag;
        this.finalMarkFlag = finalMarkFlag;
        this.revisedFinalMarkFlag = revisedFinalMarkFlag;
        this.lastDateToUploadInternalMarks=lastDateToUploadInternalMarks;
        this.instituteId=instituteId;
    }
}