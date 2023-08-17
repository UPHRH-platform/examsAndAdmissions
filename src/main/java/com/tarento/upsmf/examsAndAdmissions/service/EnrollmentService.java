package com.tarento.upsmf.examsAndAdmissions.service;

import com.tarento.upsmf.examsAndAdmissions.model.Enrollment;
import org.springframework.stereotype.Service;

import java.util.List;


public interface EnrollmentService {
    public List<Enrollment> getInstitute();

    public List<Enrollment> getInstitute(Integer enrollmentId);
}
