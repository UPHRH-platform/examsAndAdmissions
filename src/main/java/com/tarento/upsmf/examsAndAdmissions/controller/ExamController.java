package com.tarento.upsmf.examsAndAdmissions.controller;

import com.tarento.upsmf.examsAndAdmissions.model.ApiResponse;
import com.tarento.upsmf.examsAndAdmissions.service.EnrollmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
@RestController
@RequestMapping("/v1/exam/admin/examcycle")
public class ExamController {

    @Autowired
    EnrollmentService enrollmentService;

    @GetMapping("/institute")
    public ResponseEntity<?> getInstitute(){
        ApiResponse response =  enrollmentService.getInstitute();
        return new ResponseEntity<>(response, response.getResponseCode());
    }
    @GetMapping("/institute/{enrollmentid}")
    public ResponseEntity<?> getInstitute(@RequestParam Integer enrollmentid){
        ApiResponse response =  enrollmentService.getInstitute(enrollmentid);
        return new ResponseEntity<>(response, response.getResponseCode());
    }
}
