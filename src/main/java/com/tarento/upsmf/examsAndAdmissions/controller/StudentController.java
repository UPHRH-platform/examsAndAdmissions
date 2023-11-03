package com.tarento.upsmf.examsAndAdmissions.controller;

import com.tarento.upsmf.examsAndAdmissions.model.Student;
import com.tarento.upsmf.examsAndAdmissions.model.ResponseDto;
import com.tarento.upsmf.examsAndAdmissions.enums.VerificationStatus;
import com.tarento.upsmf.examsAndAdmissions.model.dto.StudentDto;
import com.tarento.upsmf.examsAndAdmissions.service.StudentService;
import com.tarento.upsmf.examsAndAdmissions.util.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/students")
@Slf4j
public class StudentController {
    @Autowired
    private StudentService studentService;

    @PostMapping("/add")
    public ResponseEntity<ResponseDto> addStudent(@ModelAttribute @Valid StudentDto studentDto) {
        ResponseDto response = studentService.enrollStudent(studentDto);
        if (HttpStatus.OK.equals(response.getResponseCode())) {
            return new ResponseEntity<>(response, HttpStatus.OK);
        } else {
            return ResponseEntity.status(response.getResponseCode()).body(response);
        }
    }
    @GetMapping("/find")
    public ResponseEntity<ResponseDto> getFilteredStudents(
            @RequestParam(required = false) Long instituteId,
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) String session,
            @RequestParam(required = false) VerificationStatus verificationStatus) {

        ResponseDto response = studentService.getFilteredStudents(instituteId, courseId, session, verificationStatus);
//        return ResponseEntity.status(response.getResponseCode()).body(response);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    @GetMapping("/{id}")
    public ResponseEntity<ResponseDto> getStudentById(@PathVariable Long id) {
        ResponseDto response = studentService.getStudentById(id);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    @PutMapping("/{id}")
    public ResponseEntity<ResponseDto> updateStudent(@PathVariable Long id, @ModelAttribute @Valid StudentDto studentDto) {
        ResponseDto response = studentService.updateStudent(id, studentDto);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    @PutMapping("/closePendingFor14Days")
    public ResponseEntity<ResponseDto> updateStudentStatusToClosed() {
        ResponseDto response = studentService.updateStudentStatusToClosed();
//        return ResponseEntity.status(response.getResponseCode()).body(response);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    @GetMapping("/pendingFor21Days")
    public ResponseEntity<ResponseDto> getStudentsPendingFor21Days(@RequestParam(required = false) Long courseId,
                                                                   @RequestParam(required = false) String session) {
        ResponseDto response = studentService.getStudentsPendingForMoreThan21Days(courseId, session);
        return new ResponseEntity<>(response, response.getResponseCode());
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseDto> deleteStudent(@PathVariable Long id) {
        ResponseDto response = studentService.deleteStudent(id);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    @PutMapping("/{studentId}/verify")
    public ResponseEntity<ResponseDto> verifyStudent(@PathVariable Long studentId, @RequestParam("status") VerificationStatus status, @RequestParam("remarks") String remarks, @RequestAttribute(Constants.Parameters.USER_ID) String userId) {
        ResponseDto response = studentService.verifyStudent(studentId, status, remarks, userId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    @GetMapping("/pendingVerification")
    public ResponseEntity<ResponseDto> getStudentsPendingVerification() {
        ResponseDto response = studentService.findByVerificationStatus(VerificationStatus.PENDING);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/keycloak/{keycloakId}")
    public ResponseEntity<ResponseDto> getStudentByKeycloak(@PathVariable String keycloakId) {
        ResponseDto response = studentService.getStudentByKeycloakId(keycloakId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}