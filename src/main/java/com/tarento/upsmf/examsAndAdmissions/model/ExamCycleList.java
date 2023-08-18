package com.tarento.upsmf.examsAndAdmissions.model;

import lombok.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString(includeFieldNames = true)
@Builder
@Table(name = "examCycles")
public class ExamCycleList {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer cycleId;

    @OneToMany(mappedBy = "examCycleList", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ExamCycle> examCycleList = new ArrayList<>();
}
