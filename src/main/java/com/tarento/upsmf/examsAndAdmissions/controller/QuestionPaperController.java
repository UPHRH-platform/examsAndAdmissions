package com.tarento.upsmf.examsAndAdmissions.controller;

import com.tarento.upsmf.examsAndAdmissions.model.QuestionPaper;
import com.tarento.upsmf.examsAndAdmissions.model.ResponseDto;
import com.tarento.upsmf.examsAndAdmissions.service.AttachmentService;
import com.tarento.upsmf.examsAndAdmissions.service.QuestionPaperService;
import com.tarento.upsmf.examsAndAdmissions.util.Constants;
import com.tarento.upsmf.examsAndAdmissions.util.HandleResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/questionPaper")
public class QuestionPaperController {

    @Autowired
    private QuestionPaperService questionPaperService;

    @Autowired
    private AttachmentService attachmentService;

    @GetMapping("/download/{questionPaperId}")
    public ResponseEntity<ResponseDto> downloadFile(@PathVariable Long questionPaperId) {
        try {
            ResponseDto response = attachmentService.downloadQuestionPaper(questionPaperId);
            return new ResponseEntity<>(response, response.getResponseCode());
        } catch (Exception e) {
            return HandleResponse.handleErrorResponse(e);
        }
    }

    @PostMapping("/upload")
    public ResponseEntity<ResponseDto> upload(Long examCycleId, Long examId, @RequestAttribute(Constants.Parameters.USER_ID) String createdBy, MultipartFile file) {
        try {
            ResponseDto response = attachmentService.upload(examCycleId, examId, createdBy, file);
            return new ResponseEntity<>(response, response.getResponseCode());
        } catch (Exception e) {
            return HandleResponse.handleErrorResponse(e);
        }
    }

    @GetMapping("/list")
    public ResponseEntity<ResponseDto> getAllQuestionPapers(@RequestParam(required = true) Long examCycleId, Long examId) {
        try {
            ResponseDto response = questionPaperService.getAllQuestionPapers(examCycleId, examId);
            return new ResponseEntity<>(response, response.getResponseCode());
        } catch (Exception e) {
            return HandleResponse.handleErrorResponse(e);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseDto> getQuestionPaperById(@PathVariable Long id) {
        try {
            ResponseDto response = questionPaperService.getQuestionPaperById(id);
            return new ResponseEntity<>(response, response.getResponseCode());
        } catch (Exception e) {
            return HandleResponse.handleErrorResponse(e);
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ResponseDto> deleteQuestionPaper(@PathVariable Long id) {
        try {
            ResponseDto response = attachmentService.deleteQuestionPaper(id);
            return new ResponseEntity<>(response, response.getResponseCode());
        } catch (Exception e) {
            return HandleResponse.handleErrorResponse(e);
        }
    }

    @GetMapping("/preview/{questionPaperId}")
    public ResponseEntity<ResponseDto> getPreviewUrl(@PathVariable Long questionPaperId) {
        try {
            ResponseDto response = attachmentService.getPreviewUrl(questionPaperId);
            return new ResponseEntity<>(response, response.getResponseCode());
        } catch (Exception e) {
            return HandleResponse.handleErrorResponse(e);
        }
    }
    @GetMapping("/examCycle/{examCycleId}")
    public ResponseEntity<ResponseDto> getAllQuestionPapersByExamCycle(@PathVariable Long examCycleId) {
        ResponseDto response = questionPaperService.getAllQuestionPapersByExamCycleId(examCycleId);
        return new ResponseEntity<>(response, response.getResponseCode());
    }
}
