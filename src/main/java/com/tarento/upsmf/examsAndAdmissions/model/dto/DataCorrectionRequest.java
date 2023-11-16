package com.tarento.upsmf.examsAndAdmissions.model.dto;

import com.tarento.upsmf.examsAndAdmissions.model.Exam;
import com.tarento.upsmf.examsAndAdmissions.model.ExamCycle;
import com.tarento.upsmf.examsAndAdmissions.model.Student;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class DataCorrectionRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "student_id")
    private Student student;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "exam_cycle_id")
    private ExamCycle examCycle;
    private String requestedCorrection;
    private String status;
    private String rejectionReason;
    @Column(name = "proof_attachment_path")
    private String proofAttachmentPath;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "exam_id")
    private Exam exam;  // Associating with Exam
    private String updatedFirstName;
    private String updatedLastName;
    private LocalDate updatedDOB;
    private String updatedCourseYear;

}
