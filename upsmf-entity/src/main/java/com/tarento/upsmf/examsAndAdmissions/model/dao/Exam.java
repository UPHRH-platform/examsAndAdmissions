package com.tarento.upsmf.examsAndAdmissions.model.dao;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString(includeFieldNames = true)
@Builder
@Table(name = "exam")
public class Exam {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer examId;

    private String examName;

    @ManyToOne
    @JoinColumn(name = "exam_cycle_id")
    private ExamCycle examCycle;

    private LocalDateTime examDate;
    private String createdBy;
    private LocalDateTime createdOn;
    private String lastUpdatedBy;
    private LocalDateTime lastUpdatedOn;
    private boolean obsolete;

    // Constructors, getters, setters, and other methods
}

