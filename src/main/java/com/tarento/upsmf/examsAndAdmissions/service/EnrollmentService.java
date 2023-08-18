package com.tarento.upsmf.examsAndAdmissions.service;

import com.tarento.upsmf.examsAndAdmissions.model.ApiResponse;
import org.springframework.stereotype.Service;

@Service
public interface EnrollmentService {
    public ApiResponse getInstitute();

    public ApiResponse getInstitute(Integer enrollmentId);
}
