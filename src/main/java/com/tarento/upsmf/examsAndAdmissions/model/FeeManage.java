package com.tarento.upsmf.examsAndAdmissions.model;

import lombok.*;

import javax.persistence.*;
import java.util.List;

@Entity
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString(includeFieldNames = true)
@Builder
@Table(name = "fee_details")
public class FeeManage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer feeId;

    private String fullName;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "fee_id")
    private List<Exam> exams;

    private Integer noOfExams;
    private Integer feeAmount;

    // Constructors, getters, setters
}
