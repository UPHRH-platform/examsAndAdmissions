package com.tarento.upsmf.examsAndAdmissions.model.dao;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString(includeFieldNames = true)
@Builder
@Table(name = "exam_cycle")
public class ExamCycle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer examCycleId;

    private String examCycleName;

    @ManyToOne
    @JoinColumn(name = "course_id")
    private Course course;

    private LocalDate startDate;
    private LocalDate endDate;
    private String createdBy;
    private LocalDateTime createdOn;
    private String lastUpdatedBy;
    private LocalDateTime lastUpdatedOn;
    private String status;
    private boolean obsolete;

    // Constructors, getters, setters, and other methods
}

