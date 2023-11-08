package com.tarento.upsmf.examsAndAdmissions.controller;

import com.tarento.upsmf.examsAndAdmissions.model.*;
import com.tarento.upsmf.examsAndAdmissions.model.dto.ExamDetailsDto;
import com.tarento.upsmf.examsAndAdmissions.model.dto.ResultDisplayDto;
import com.tarento.upsmf.examsAndAdmissions.model.dto.StudentResultDto;
import com.tarento.upsmf.examsAndAdmissions.repository.RetotallingRequestRepository;
import com.tarento.upsmf.examsAndAdmissions.service.RetotallingService;
import com.tarento.upsmf.examsAndAdmissions.service.StudentResultService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/studentResults")
@Slf4j
public class StudentResultController {

    @Autowired
    private StudentResultService studentResultService;
    @Autowired
    private RetotallingService retotallingService;

    @Autowired
    private RetotallingRequestRepository retotallingRequestRepository;

    @PostMapping("/upload/internal")
    public ResponseEntity<ResponseDto> uploadInternalMarks(@RequestParam("file") MultipartFile file) {
        return new ResponseEntity<>(studentResultService.importInternalMarksFromExcel(file), HttpStatus.OK);
    }

    @PostMapping("/upload/external")
    public ResponseEntity<ResponseDto> uploadExternalMarks(@RequestParam("file") MultipartFile file) {
        return new ResponseEntity<>(studentResultService.importExternalMarksFromExcel(file), HttpStatus.OK);
    }

    @PostMapping("/publish")
    public ResponseEntity<ResponseDto> publishExamResults(
            @RequestParam(required = false) Long examCycleId,
            @RequestParam(required = false) Long courseId) {
        return new ResponseEntity<>(studentResultService.publishResultsForCourseWithinCycle(courseId, examCycleId), HttpStatus.OK);
    }

    @GetMapping("/results")
    public ResponseEntity<ResponseDto> viewResults(@RequestParam Long StudentId, @RequestParam Long examCycleId) {
        return new ResponseEntity<>(studentResultService.findByStudentIdAndExamCycleId(StudentId, examCycleId), HttpStatus.OK);
    }

    @PostMapping("/requestRetotalling")
    public ResponseEntity<ResponseDto> requestRetotalling(@RequestBody RetotallingRequest request) {
        return new ResponseEntity<>(retotallingService.requestRetotalling(request), HttpStatus.OK);
    }

    @GetMapping("/retotallingRequests")
    public ResponseEntity<ResponseDto> viewAllRetotallingRequests() {
        return new ResponseEntity<>(retotallingService.getAllPendingRequests(), HttpStatus.OK);
    }

    @PutMapping("/update")
    public ResponseEntity<ResponseDto> updateResultsAfterRetotalling(@RequestBody StudentResult updatedResult) {
        return new ResponseEntity<>(studentResultService.updateResultAfterRetotalling(updatedResult), HttpStatus.OK);
    }

    @PostMapping("/bulkUpload")
    public ResponseEntity<ResponseDto> processBulkResultUploadWithOutExternals(@RequestParam("file") MultipartFile file, @RequestParam("fileType") String fileType, Long instituteId) {
        return new ResponseEntity<>(studentResultService.processBulkResultUpload(file, fileType,instituteId), HttpStatus.OK);
    }

    @PostMapping("/bulkUploadExternal")
    public ResponseEntity<ResponseDto> processBulkResultUploadWithExternals(@RequestParam("file") MultipartFile file, @RequestParam("fileType") String fileType) {
        return new ResponseEntity<>(studentResultService.processBulkResultUploadWithExternals(file, fileType), HttpStatus.OK);
    }

    @PutMapping("/bulkUploadRevised")
    public ResponseEntity<ResponseDto> processBulkResultUploadWithRevisedMarks(@RequestParam("file") MultipartFile file, @RequestParam("fileType") String fileType) {
        return new ResponseEntity<>(studentResultService.processBulkResultUploadWithRevisedMarks(file, fileType), HttpStatus.OK);
    }

    @GetMapping("/manageResults")
    public ResponseEntity<ResponseDto> getExamResultsByExamCycle(
            @RequestParam Long examCycle) {

        ResponseDto response = studentResultService.getExamResultsByExamCycle(examCycle);
        return new ResponseEntity<>(response, response.getResponseCode());
    }

    @GetMapping("/resultsByInstitute")
    public ResponseEntity<ResponseDto> getStudentsExamDetails(
            @RequestParam Long instituteId,
            @RequestParam Long examCycleId) {

        ResponseDto response = studentResultService.getResultsByInstituteAndExamCycle(instituteId, examCycleId);
        return new ResponseEntity<>(response, response.getResponseCode());

    }
    @GetMapping("/examMarksByInstitute")
    public ResponseEntity<?> getMarksByInstitute(
            @RequestParam Long examCycle,
            @RequestParam Long exam,
            @RequestParam Long institute) {

        ResponseDto response = studentResultService.getMarksByInstituteAndExamCycle(examCycle, exam, institute);

        return new ResponseEntity<>(response, response.getResponseCode());
    }

    @GetMapping("/resultUploadStatusForInstitute")
    public ResponseEntity<ResponseDto> getExamsForExamCycleAndUploadStatus(@RequestParam Long examCycleId, @RequestParam Long instituteId) {
        ResponseDto response = studentResultService.getExamsForExamCycleAndUploadStatusForInstitute(examCycleId,instituteId);
        return new ResponseEntity<>(response, HttpStatus.valueOf(response.getResponseCode().value()));
    }
    @DeleteMapping("/deleteFinalMarks")
    public ResponseEntity<ResponseDto> deleteFinalMarksByCriteria(
            @RequestParam Long examCycleId,
            @RequestParam Long instituteId) {

        ResponseDto response = studentResultService.deleteExternalMarks(examCycleId, instituteId);
        return new ResponseEntity<>(response, HttpStatus.valueOf(response.getResponseCode().value()));
    }
}