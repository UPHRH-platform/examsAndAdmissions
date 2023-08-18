package com.tarento.upsmf.examsAndAdmissions.service.impl;

import com.tarento.upsmf.examsAndAdmissions.Utils.Constants;
import com.tarento.upsmf.examsAndAdmissions.model.Enrollment;
import com.tarento.upsmf.examsAndAdmissions.model.ApiResponse;
import com.tarento.upsmf.examsAndAdmissions.repository.EnrollmentRepository;
import com.tarento.upsmf.examsAndAdmissions.service.EnrollmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
@Service
public class EnrollmentServiceImpl implements EnrollmentService {
    @Autowired
    EnrollmentRepository enrollmentRepository;
    @Override
    public ApiResponse getInstitute() {
        ApiResponse response = new ApiResponse(Constants.API_ENROLLMENT_READ);
        List<Enrollment> result = enrollmentRepository.findAll();
        response.put(Constants.MESSAGE, Constants.SUCCESSFUL);
        response.put(Constants.RESPONSE, result);
        response.setResponseCode(HttpStatus.OK);
        return response;
    }
    @Override
    public ApiResponse getInstitute(Integer enrollmentId) {
        ApiResponse response = new ApiResponse(Constants.API_ENROLLMENT_READ_USINGID);
        List<Enrollment> result = enrollmentRepository.findAllById(Collections.singleton(enrollmentId));
        response.put(Constants.MESSAGE, Constants.SUCCESSFUL);
        response.put(Constants.RESPONSE, result);
        response.setResponseCode(HttpStatus.OK);
        return response;
    }
}
