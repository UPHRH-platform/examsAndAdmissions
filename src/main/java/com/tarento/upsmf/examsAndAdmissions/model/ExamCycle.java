package com.tarento.upsmf.examsAndAdmissions.model;

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
@Table(name = "examCycleDetails")
public class ExamCycle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer examCycleId;
    private String examCycleName;
    private LocalDateTime startDate;
    private LocalDateTime endDate;

    @ManyToOne
    @JoinColumn(name = "cycleId") // Name of the foreign key column in ExamCycle table
    private ExamCycleList examCycleList;

    @ManyToOne
    @JoinColumn(name = "studentId") // Name of the foreign key column in ExamCycle table
    private Student student;
}