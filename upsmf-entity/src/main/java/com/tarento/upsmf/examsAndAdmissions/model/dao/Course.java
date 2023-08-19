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
@Table(name = "course")
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer courseId;

    private String courseName;
    private Integer courseYear;

    @ManyToOne
    @JoinColumn(name = "institute_id")
    private Institute institute;

    private Integer totalNoOfSeats;
    private boolean obsolete;

    // Constructors, getters, setters, and other methods
}