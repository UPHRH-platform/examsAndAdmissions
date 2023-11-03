package com.tarento.upsmf.examsAndAdmissions.service;

import com.tarento.upsmf.examsAndAdmissions.model.QuestionPaper;
import com.tarento.upsmf.examsAndAdmissions.model.ResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

public interface AttachmentService {

    ResponseDto downloadQuestionPaper(Long questionPaperId);

    ResponseDto getPreviewUrl(Long questionPaperId);

    ResponseDto deleteQuestionPaper(Long id);

    ResponseDto upload(Long examCycleId, Long examId, String createdBy, MultipartFile file);
}
