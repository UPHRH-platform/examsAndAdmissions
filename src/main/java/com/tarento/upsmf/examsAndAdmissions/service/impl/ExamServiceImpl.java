package com.tarento.upsmf.examsAndAdmissions.service.impl;

import com.tarento.upsmf.examsAndAdmissions.model.Exam;
import com.tarento.upsmf.examsAndAdmissions.model.ResponseDto;
import com.tarento.upsmf.examsAndAdmissions.repository.ExamRepository;
import com.tarento.upsmf.examsAndAdmissions.service.ExamService;
import com.tarento.upsmf.examsAndAdmissions.util.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

import static com.tarento.upsmf.examsAndAdmissions.model.ResponseDto.setErrorResponse;

@Service
public class ExamServiceImpl implements ExamService {

    private static final Logger logger = LoggerFactory.getLogger(ExamServiceImpl.class);
    @Autowired
    private ExamRepository examRepository;

    @Override
    public ResponseDto createExam(Exam exam, String userId) {
        ResponseDto response = new ResponseDto(Constants.API_EXAM_ADD);
        try {
            logger.info("Creating new Exam: {}", exam);
            exam.setObsolete(0);
            exam.setCreatedOn(LocalDateTime.now());
            exam.setCreatedBy(userId);
            examRepository.save(exam);
            response.put(Constants.MESSAGE, Constants.SUCCESSFUL);
            response.put(Constants.RESPONSE, "Successfully created exam");
            response.setResponseCode(HttpStatus.OK);
        } catch (Exception e) {
            response.put(Constants.MESSAGE, "Error saving exam details");
            response.put(Constants.RESPONSE, Constants.FAILUREMESSAGE);
            response.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return response;
    }
    @Override
    public ResponseDto getAllExams() {
        ResponseDto response = new ResponseDto(Constants.API_EXAM_GET_ALL);
        logger.info("Fetching all active Exams...");
        List<Exam> exams = examRepository.findAll();
        if (exams.isEmpty()) {
            response.put(Constants.MESSAGE, "Error fetching exam details");
            response.put(Constants.RESPONSE, Constants.FAILUREMESSAGE);
            response.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR);
        } else {
            response.put(Constants.MESSAGE, Constants.SUCCESSFUL);
            response.put(Constants.RESPONSE, exams);
            response.setResponseCode(HttpStatus.OK);
        }
        return response;
    }
    @Override
    public ResponseDto getExamById(Long id) {
        ResponseDto response = new ResponseDto(Constants.API_EXAM_GET_BY_ID);
        logger.info("Fetching Exam by ID: {}", id);
        Optional<Exam> examOptional = examRepository.findById(id);
        if (examOptional.isPresent()) {
            Exam exam = examOptional.get();
            if (exam.getObsolete() == 0) {
                response.put(Constants.MESSAGE, Constants.SUCCESSFUL);
                response.put(Constants.RESPONSE, exam);
                response.setResponseCode(HttpStatus.OK);
            } else {
                response.put(Constants.MESSAGE, "Error saving fee details");
                response.put(Constants.RESPONSE, Constants.FAILUREMESSAGE);
                response.setResponseCode(HttpStatus.NOT_FOUND);
            }
        }
            response.put(Constants.MESSAGE, "Error saving fee details");
            response.put(Constants.RESPONSE, Constants.FAILUREMESSAGE);
            response.setResponseCode(HttpStatus.NOT_FOUND);
        return response;
    }
    @Override
    public ResponseDto deleteExam(Long id) {
        ResponseDto response = new ResponseDto(Constants.API_EXAM_DELETE);
        logger.info("Soft-deleting Exam with ID: {}", id);
        try {
            Exam exam = examRepository.findById(id).orElse(null);
            if (exam != null) {
                exam.setObsolete(1);
                examRepository.save(exam);
                response.put(Constants.MESSAGE, Constants.SUCCESSFUL);
                response.put(Constants.RESPONSE, Constants.MESSAGE);
                response.setResponseCode(HttpStatus.NO_CONTENT);
            } else {
                logger.warn("Exam with ID: {} not found for deletion!", id);
                response.put(Constants.MESSAGE, Constants.SUCCESSFUL);
                response.put(Constants.RESPONSE, Constants.MESSAGE);
                response.setResponseCode(HttpStatus.NOT_FOUND);
            }
        }catch (Exception e) {
            response.put(Constants.MESSAGE, Constants.SUCCESSFUL);
            response.put(Constants.RESPONSE, Constants.MESSAGE);
            response.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return response;
    }

    @Override
    public ResponseDto updateExam(Long id, Exam exam, String userId) {
        ResponseDto response = new ResponseDto(Constants.API_EXAM_UPDATE);
        logger.info("Updating Exam with ID: {}", id);
        Exam existingExam = examRepository.findById(id).orElse(null);
        if (existingExam != null) {
            existingExam.setExamCycleId(exam.getExamCycleId());
            existingExam.setExamName(exam.getExamName());
            existingExam.setExamDate(exam.getExamDate());
            existingExam.setStartTime(exam.getStartTime());
            existingExam.setEndTime(exam.getEndTime());
            // Update auditing metadata from the payload
            existingExam.setModifiedBy(userId);
            existingExam.setModifiedOn(LocalDateTime.now());

            // Soft delete or status flag, if you want to allow it from the payload:
            existingExam.setObsolete(exam.getObsolete());

            response.put(Constants.MESSAGE, Constants.SUCCESSFUL);
            response.put(Constants.RESPONSE, exam);
            response.setResponseCode(HttpStatus.OK);
        }
        logger.warn("Exam with ID: {} not found!", id);
        response.put(Constants.MESSAGE, "Exam id not found");
        response.put(Constants.RESPONSE, Constants.FAILUREMESSAGE);
        response.setResponseCode(HttpStatus.NOT_FOUND);
        return response;
    }

    @Override
    public ResponseDto restoreExam(Long id) {
        ResponseDto response = new ResponseDto(Constants.API_EXAM_RESTORE);
        try {
            logger.info("Restoring soft-deleted Exam with ID: {}", id);
            Exam exam = examRepository.findById(id).orElse(null);
            if (exam != null && exam.getObsolete() == 1) {
                exam.setObsolete(0);
                examRepository.save(exam);
                response.put(Constants.MESSAGE, Constants.SUCCESSFUL);
                response.put(Constants.RESPONSE, exam);
                response.setResponseCode(HttpStatus.OK);
            } else {
                logger.warn("Exam with ID: {} not found for restoration!", id);
                response.put(Constants.MESSAGE, "Exam id not found");
                response.put(Constants.RESPONSE, Constants.FAILUREMESSAGE);
                response.setResponseCode(HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            response.put(Constants.MESSAGE, "Exam id not found");
            response.put(Constants.RESPONSE, Constants.FAILUREMESSAGE);
            response.setResponseCode(HttpStatus.NOT_FOUND);
        }
        return response;
    }
    public void publishExamResults(Long examId) {
        Exam exam = examRepository.findById(examId).orElseThrow(() -> new RuntimeException("Exam not found"));
        exam.setIsResultsPublished(true);
        examRepository.save(exam);
    }
    public ResponseDto findByExamCycleId(Long examCycleId) {
        ResponseDto responseDto = new ResponseDto(Constants.API_EXAM_FIND_BY_CYCLE);
        try {
            logger.info("Finding Exams by ExamCycle ID: {}", examCycleId);
            List<Exam> exams = examRepository.findAllByExamCycleIdAndObsolete(examCycleId, 0);

            if (exams != null && !exams.isEmpty()) {
                Map<String, Object> resultData = new HashMap<>();
                resultData.put("response", exams);
                resultData.put("message", "Successfully retrieved exams");
                responseDto.setResult(resultData);
                responseDto.setResponseCode(HttpStatus.OK);
            } else {
                setErrorResponse(responseDto, "NO_EXAMS_FOUND", "No active exams found for the given ExamCycle ID", HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            logger.error("Error while finding Exams by ExamCycle ID: {}", examCycleId, e);
            setErrorResponse(responseDto, "INTERNAL_SERVER_ERROR", "Error retrieving exams", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return responseDto;
    }
}
