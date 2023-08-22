package com.tarento.upsmf.examsAndAdmissions.controller;

import com.tarento.upsmf.examsAndAdmissions.model.Exam;
import com.tarento.upsmf.examsAndAdmissions.service.ExamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/exam")
public class ExamController {

    @Autowired
    private ExamService examService;

    @PostMapping("/create")
    public ResponseEntity<?> createExam(@RequestBody Exam exam) {
        try {
            Exam createdExam = examService.createExam(exam);
            return new ResponseEntity<>(createdExam, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to create Exam.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/list")
    public ResponseEntity<?> getAllExams() {
        try {
            List<Exam> exams = examService.getAllExams();
            if (exams.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
            return new ResponseEntity<>(exams, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to fetch all Exams.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getExamById(@PathVariable Long id) {
        try {
            Exam exam = examService.getExamById(id);
            if (exam == null) {
                return new ResponseEntity<>("Exam not found with ID: " + id, HttpStatus.NOT_FOUND);
            }
            return new ResponseEntity<>(exam, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to fetch Exam with ID: " + id, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteExam(@PathVariable Long id) {
        try {
            examService.deleteExam(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to delete Exam with ID: " + id, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateExam(@PathVariable Long id, @RequestBody Exam exam) {
        try {
            Exam updatedExam = examService.updateExam(id, exam);
            if (updatedExam == null) {
                return new ResponseEntity<>("Exam not found with ID: " + id, HttpStatus.NOT_FOUND);
            }
            return new ResponseEntity<>(updatedExam, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to update Exam with ID: " + id, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    // Restore a soft-deleted exam
    @PutMapping("/{id}/restore")
    public ResponseEntity<?> restoreExam(@PathVariable Long id) {
        try {
            examService.restoreExam(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to restore Exam with ID: " + id, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}