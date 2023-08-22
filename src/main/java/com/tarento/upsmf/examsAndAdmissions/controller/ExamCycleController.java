package com.tarento.upsmf.examsAndAdmissions.controller;

import com.tarento.upsmf.examsAndAdmissions.model.ExamCycle;
import com.tarento.upsmf.examsAndAdmissions.service.ExamCycleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/examCycle")
public class ExamCycleController {

    @Autowired
    private ExamCycleService service;

    @PostMapping("/create")
    public ResponseEntity<?> createExamCycle(@RequestBody ExamCycle examCycle) {
        try {
            ExamCycle createdExamCycle = service.createExamCycle(examCycle);
            return new ResponseEntity<>(createdExamCycle, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to create ExamCycle.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @GetMapping("/list")
    public ResponseEntity<?> getAllExamCycles() {
        try {
            List<ExamCycle> examCycles = service.getAllExamCycles();
            if (examCycles.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
            return new ResponseEntity<>(examCycles, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to fetch all ExamCycles.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getExamCycleById(@PathVariable Long id) {
        try {
            ExamCycle examCycle = service.getExamCycleById(id);
            if (examCycle == null) {
                return new ResponseEntity<>("ExamCycle not found with ID: " + id, HttpStatus.NOT_FOUND);
            }
            return new ResponseEntity<>(examCycle, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to fetch ExamCycle with ID: " + id, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateExamCycle(@PathVariable Long id, @RequestBody ExamCycle examCycle) {
        try {
            ExamCycle updatedExamCycle = service.updateExamCycle(id, examCycle);
            if (updatedExamCycle == null) {
                return new ResponseEntity<>("ExamCycle not found with ID: " + id, HttpStatus.NOT_FOUND);
            }
            return new ResponseEntity<>(updatedExamCycle, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to update ExamCycle with ID: " + id, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteExamCycle(@PathVariable Long id) {
        try {
            service.deleteExamCycle(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to delete ExamCycle with ID: " + id, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    // Restore a soft-deleted exam
    @PutMapping("/{id}/restore")
    public ResponseEntity<?> restoreExamCycle(@PathVariable Long id) {
        try {
            service.restoreExamCycle(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to restore ExamCycle with ID: " + id, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}