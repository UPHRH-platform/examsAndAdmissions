package com.tarento.upsmf.examsAndAdmissions.service;

import com.tarento.upsmf.examsAndAdmissions.model.Exam;
import com.tarento.upsmf.examsAndAdmissions.repository.ExamRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExamService {

    private static final Logger logger = LoggerFactory.getLogger(ExamService.class);

    @Autowired
    private ExamRepository repository;

    public Exam createExam(Exam exam) {
        logger.info("Creating new Exam: {}", exam);
        exam.setObsolete(0);
        return repository.save(exam);
    }

    public List<Exam> getAllExams() {
        logger.info("Fetching all active Exams...");
        return repository.findByObsolete(0);
    }

    public Exam getExamById(Long id) {
        logger.info("Fetching Exam by ID: {}", id);
        return repository.findByIdAndObsolete(id, 0).orElse(null);
    }

    public Exam updateExam(Long id, Exam updatedExam) {
        logger.info("Updating Exam with ID: {}", id);
        Exam existingExam = repository.findById(id).orElse(null);
        if (existingExam != null) {
            existingExam.setExamCycleId(updatedExam.getExamCycleId());
            existingExam.setExamDate(updatedExam.getExamDate());

            // Update auditing metadata from the payload
            existingExam.setModifiedBy(updatedExam.getModifiedBy());
            existingExam.setModifiedOn(updatedExam.getModifiedOn());

            // Soft delete or status flag, if you want to allow it from the payload:
            existingExam.setObsolete(updatedExam.getObsolete());

            return repository.save(existingExam);
        }
        logger.warn("Exam with ID: {} not found!", id);
        return null;
    }



    public void deleteExam(Long id) {
        logger.info("Soft-deleting Exam with ID: {}", id);
        Exam exam = repository.findById(id).orElse(null);
        if (exam != null) {
            exam.setObsolete(1);
            repository.save(exam);
        } else {
            logger.warn("Exam with ID: {} not found for deletion!", id);
        }
    }

    public void restoreExam(Long id) {
        logger.info("Restoring soft-deleted Exam with ID: {}", id);
        Exam exam = repository.findById(id).orElse(null);
        if (exam != null && exam.getObsolete() == 1) {
            exam.setObsolete(0);
            repository.save(exam);
        } else {
            logger.warn("Exam with ID: {} not found for restoration!", id);
        }
    }
}