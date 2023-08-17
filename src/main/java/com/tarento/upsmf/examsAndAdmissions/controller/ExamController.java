package com.tarento.upsmf.examsAndAdmissions.controller;

import com.tarento.upsmf.examsAndAdmissions.model.Enrollment;
import com.tarento.upsmf.examsAndAdmissions.service.EnrollmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
@RequestMapping("/exam")
public class ExamController {

    @Autowired
    EnrollmentService enrollmentService;

    @GetMapping("admin/examCycle/institute")
    public List<Enrollment> getInstitute(){
        return enrollmentService.getInstitute();
    }
    @GetMapping("admin/examCycle/institute/{enrollmentid}")
    public List<Enrollment> getInstitute(@RequestParam Integer enrollmentid){
        return enrollmentService.getInstitute(enrollmentid);
    }
}
