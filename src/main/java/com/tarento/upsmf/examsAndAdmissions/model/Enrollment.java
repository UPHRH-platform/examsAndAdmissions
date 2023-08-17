package com.tarento.upsmf.examsAndAdmissions.model;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(includeFieldNames = true)
@Builder
@Table(name = "enrollment")
public class Enrollment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer enrollmentid;
    private String centerCode;
    private String centerName;
    private String courseCode;
    private String courseName;
    private String session;
    private String examBatch;
    private LocalDate admissionDate;
    private String firstName;
    private String surname;
    private String motherName;
    private String fatherName;
    private LocalDate dateOfBirth;
    private String gender;
    private String caste;
    private String category;
    private String intermediatePassedBoard;
    private String intermediateSubjects;
    private Double intermediatePercentage;
    private String mobileNo;
    private String emailId;
    private String aadhaarNo;
    private String address;
    private String pinCode;
    private String country;
    private String state;
    private String district;
    @Lob
    private byte[] highSchoolMarksheet;
    @Lob
    private byte[] highSchoolCertificate;
    @Lob
    private byte[] intermediateMarksheet;
    @Lob
    private byte[] intermediateCertificate;
    private String highSchoolRollNo;
    private Integer highSchoolYearOfPassing;
    private String intermediateRollNo;
    private Integer intermediateYearOfPassing;

    public void setHighSchoolMarksheet(byte[] highSchoolMarksheet) {
        this.highSchoolMarksheet = highSchoolMarksheet;
    }

    public byte[] getHighSchoolCertificate() {
        return highSchoolCertificate;
    }

    public void setHighSchoolCertificate(byte[] highSchoolCertificate) {
        this.highSchoolCertificate = highSchoolCertificate;
    }

    public byte[] getIntermediateMarksheet() {
        return intermediateMarksheet;
    }

    public void setIntermediateMarksheet(byte[] intermediateMarksheet) {
        this.intermediateMarksheet = intermediateMarksheet;
    }

    public byte[] getIntermediateCertificate() {
        return intermediateCertificate;
    }

    public void setIntermediateCertificate(byte[] intermediateCertificate) {
        this.intermediateCertificate = intermediateCertificate;
    }
}