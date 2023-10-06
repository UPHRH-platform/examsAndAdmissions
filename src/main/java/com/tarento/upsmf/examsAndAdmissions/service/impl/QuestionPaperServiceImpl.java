package com.tarento.upsmf.examsAndAdmissions.service.impl;

import com.tarento.upsmf.examsAndAdmissions.model.QuestionPaper;
import com.tarento.upsmf.examsAndAdmissions.model.ResponseDto;
import com.tarento.upsmf.examsAndAdmissions.repository.QuestionPaperRepository;
import com.tarento.upsmf.examsAndAdmissions.service.QuestionPaperService;
import com.tarento.upsmf.examsAndAdmissions.util.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class QuestionPaperServiceImpl implements QuestionPaperService {
    @Autowired
    private QuestionPaperRepository questionPaperRepository;
    private final Logger logger = LoggerFactory.getLogger(getClass().getName());

    @Override
    public ResponseDto getAllQuestionPapers(Long examCycleId, Long examId) {
        ResponseDto response = new ResponseDto(Constants.API_QUESTION_PAPER_GET_ALL);
        logger.info("Fetching all Question papers...");
        List<QuestionPaper> questionPapers = questionPaperRepository.findAll();
        if (questionPapers.isEmpty()) {
            response.put(Constants.MESSAGE, "Getting Error in fetching Question papers details");
            response.setResponseCode(HttpStatus.NOT_FOUND);
        } else {
            Boolean filterFlag = false;
            List<QuestionPaper> questionPaperList = new ArrayList<>();
            for (int i = 0; i < questionPapers.size(); i++) {
                QuestionPaper questionPaper = questionPapers.get(i);
                if (questionPaper.getExamCycleId().equals(examCycleId) && questionPaper.getExam().getId().equals(examId)) {
                    filterFlag = true;
                    questionPaperList.add(questionPaper);
                } else {
                    response.put(Constants.MESSAGE, "Data is not there related to filters applied");
                    response.setResponseCode(HttpStatus.NOT_FOUND);
                }
            }
            if (filterFlag) {
                response.put(Constants.MESSAGE, Constants.SUCCESSFUL);
                response.put(Constants.RESPONSE, questionPaperList);
                response.setResponseCode(HttpStatus.OK);
            }
        }
        return response;
    }

    @Override
    public ResponseDto getQuestionPaperById(Long id) {
        ResponseDto response = new ResponseDto(Constants.API_QUESTION_PAPER_GET_BY_ID);
        Optional<QuestionPaper> questionPaperOptional = questionPaperRepository.findById(id);
        if (questionPaperOptional.isPresent()) {
            QuestionPaper questionPaper = questionPaperOptional.get();
            if (questionPaper.getObsolete() == 0) {
                response.put(Constants.MESSAGE, Constants.SUCCESSFUL);
                response.put(Constants.RESPONSE, questionPaper);
                response.setResponseCode(HttpStatus.OK);
            } else {
                response.put(Constants.MESSAGE, "Question paper id is deleted(Obsolete is not equal to zero)");
                response.setResponseCode(HttpStatus.NOT_FOUND);
            }
        } else {
            response.put(Constants.MESSAGE, "Getting Error in fetching Question paper details by id");
            response.setResponseCode(HttpStatus.NOT_FOUND);
        }
        return response;
    }

}
