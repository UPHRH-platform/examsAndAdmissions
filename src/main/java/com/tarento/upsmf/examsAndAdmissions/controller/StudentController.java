package com.tarento.upsmf.examsAndAdmissions.controller;

import com.tarento.upsmf.examsAndAdmissions.model.Student;
import com.tarento.upsmf.examsAndAdmissions.model.VerificationStatus;
import com.tarento.upsmf.examsAndAdmissions.model.dto.StudentDto;
import com.tarento.upsmf.examsAndAdmissions.service.StudentService;
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
public class StudentController {

    @Autowired
    private StudentService studentService;

    @PostMapping("/add")
    public ResponseEntity<Student> addStudent(@ModelAttribute @Valid StudentDto studentDto) {
        try {
            Student addedStudent = studentService.enrollStudent(studentDto);
            return ResponseEntity.ok(addedStudent);
        } catch (IOException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/findAll")
    public ResponseEntity<List<Student>> getAllStudents() {
        List<Student> students = studentService.getAllStudents();
        if (students != null && !students.isEmpty()) {
            return ResponseEntity.ok(students);
        } else {
            return ResponseEntity.noContent().build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Student> getStudentById(@PathVariable Long id) {
        Optional<Student> student = studentService.getStudentById(id);
        return student.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Student> updateStudent(@PathVariable Long id,@ModelAttribute @Valid StudentDto studentDto) {
        try {
            Student updatedStudent = studentService.updateStudent(id, studentDto);
            return ResponseEntity.ok(updatedStudent);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStudent(@PathVariable Long id) {
        try {
            studentService.deleteStudent(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{studentId}/verify")
    public ResponseEntity<Student> verifyStudent(@PathVariable Long studentId, @RequestParam("status") VerificationStatus status) {
        Student student = studentService.findById(studentId);
        student.setVerificationStatus(status);
        studentService.save(student);
        return ResponseEntity.ok(student);
    }
    @GetMapping("/pending-verification")
    public ResponseEntity<List<Student>> getStudentsPendingVerification() {
        List<Student> students = studentService.findByVerificationStatus(VerificationStatus.PENDING);
        return ResponseEntity.ok(students);
    }


}