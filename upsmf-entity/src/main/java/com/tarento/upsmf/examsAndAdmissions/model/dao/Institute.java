package com.tarento.upsmf.examsAndAdmissions.model.dao;

import lombok.*;

import javax.persistence.*;

@Entity
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString(includeFieldNames = true)
@Builder
@Table(name = "institute")
public class Institute {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer instituteId;

    private String instituteName;
    private String instituteAddress;
    private String institutePOCP;
    private String instituteContactNumber;
    private String instituteEmail;

    @ManyToOne
    @JoinColumn(name = "exam_cycle_id")
    private ExamCycle examCycle;

    private boolean obsolete;

    // Constructors, getters, setters, and other methods
}