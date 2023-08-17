package com.tarento.upsmf.examsAndAdmissions.service.impl;

import com.tarento.upsmf.examsAndAdmissions.model.Enrollment;
import com.tarento.upsmf.examsAndAdmissions.repository.EnrollmentRepository;
import com.tarento.upsmf.examsAndAdmissions.service.EnrollmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
@Service
public class EnrollmentServiceImpl implements EnrollmentService {
    @Autowired
    EnrollmentRepository enrollmentRepository;
    @Override
    public List<Enrollment> getInstitute() {
        return enrollmentRepository.findAll();
    }
    @Override
    public List<Enrollment> getInstitute(Integer enrollmentId) {
        return enrollmentRepository.findAllById(Collections.singleton(enrollmentId));
    }
}
