package com.tarento.upsmf.examsAndAdmissions.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tarento.upsmf.examsAndAdmissions.enums.ApprovalStatus;
import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "exam_center")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class ExamCenter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String address;
    @Enumerated(EnumType.STRING)
    private ApprovalStatus approvalStatus = ApprovalStatus.PENDING;
    //private int maxCapacity;
    //private Integer totalSeats;
    //private Integer occupiedSeats = 0; // default to 0
    @ManyToOne
    @JoinColumn(name = "institute_id")
    private Institute institute;

    @ManyToOne
    @JoinColumn(name = "exam_cycle_id")
    private ExamCycle examCycle;

    private String ipAddress;
    private String remarks;
    private boolean allowedForExamCentre;
    private String district;
    private String instituteCode;
    @Column(name = "alternate_exam_center_assigned", nullable = false, columnDefinition = "boolean default false")
    private Boolean alternateExamCenterAssigned = false;
    @OneToOne
    @JoinColumn(name = "alternate_exam_center_id")
//    @JsonIgnore // Ignore this field during JSON serialization
    private ExamCenter alternateExamCenter;

    @Override
    public String toString() {
        return "ExamCenter{" +
                "id=" + id +
                ", name='" + name + '\'' +
                // ... (other fields)
                ", alternateExamCenter=" + (alternateExamCenter != null ? alternateExamCenter.getId() : null) + // Handle alternateExamCenter to break the cyclic reference
                '}';
    }
}
