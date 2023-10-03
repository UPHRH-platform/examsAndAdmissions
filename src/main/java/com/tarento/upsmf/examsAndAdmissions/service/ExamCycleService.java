package com.tarento.upsmf.examsAndAdmissions.service;

import com.tarento.upsmf.examsAndAdmissions.enums.ExamCycleStatus;
import com.tarento.upsmf.examsAndAdmissions.model.*;
import com.tarento.upsmf.examsAndAdmissions.repository.*;
import com.tarento.upsmf.examsAndAdmissions.util.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import com.tarento.upsmf.examsAndAdmissions.model.dto.ExamCycleDTO;

import javax.validation.ConstraintViolationException;

@Service
@Slf4j
public class ExamCycleService {

    @Autowired
    private ExamCycleRepository repository;
    @Autowired
    private ExamRepository examRepository;
    @Autowired
    private InstituteRepository instituteRepository;
    @Autowired
    private ExamCenterRepository examCenterRepository;
    @Autowired
    private CourseRepository courseRepository;

    public ResponseDto createExamCycle(ExamCycle examCycle, String userId) {
        ResponseDto response = new ResponseDto(Constants.API_EXAM_CYCLE_ADD);
        try {
            ExamCycle existingExamCycle = repository.findByExamCycleNameAndCourseAndStartDateAndEndDate(
                    examCycle.getExamCycleName(),
                    examCycle.getCourse(),
                    examCycle.getStartDate(),
                    examCycle.getEndDate()
            );

            if (existingExamCycle != null) {
                response.put(Constants.MESSAGE, "ExamCycle with the same details already exists.");
                response.put(Constants.RESPONSE, existingExamCycle);
                response.setResponseCode(HttpStatus.CONFLICT);
                return response;
            }

            log.info("Creating new ExamCycle: {}", examCycle);
            Course course = courseRepository.findById(examCycle.getCourse().getId())
                    .orElseThrow(() -> new RuntimeException("Course not found"));

            examCycle.setObsolete(0);
            examCycle.setCreatedOn(LocalDateTime.now());
            examCycle.setStatus(ExamCycleStatus.DRAFT);
            examCycle.setCreatedBy(userId);
            examCycle.setCourse(course);
            examCycle = repository.save(examCycle);

            List<Institute> allInstitutes = instituteRepository.findAll();

            // Register each institute as a potential exam center for this exam cycle
            for (Institute institute : allInstitutes) {
                ExamCenter examCenter = new ExamCenter();
                examCenter.setInstitute(institute);
                examCenter.setExamCycle(examCycle);
                examCenter.setVerified(null); // marking as pending
                examCenter.setDistrict(institute.getDistrict());
                examCenter.setAddress(institute.getAddress());
                examCenter.setName(institute.getInstituteName());
                examCenterRepository.save(examCenter);
            }

            response.put(Constants.MESSAGE, Constants.SUCCESSMESSAGE);
            response.put(Constants.RESPONSE, examCycle);
            response.setResponseCode(HttpStatus.CREATED);
        } catch (Exception e) {
            log.error("Error while creating ExamCycle", e);
            response.put(Constants.MESSAGE, "An error occurred while creating the ExamCycle.");
            response.put(Constants.RESPONSE, Constants.FAILUREMESSAGE);
            response.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return response;
    }

    public ResponseDto getAllExamCycles() {
        ResponseDto response = new ResponseDto(Constants.API_EXAM_CYCLE_GET_ALL);
        List<ExamCycle> examCycles = repository.findByObsolete(0);
        if (!examCycles.isEmpty()) {
            response.put(Constants.MESSAGE, Constants.SUCCESSFUL);
            response.put(Constants.RESPONSE, examCycles);
            response.setResponseCode(HttpStatus.OK);
        } else {
            response.put(Constants.MESSAGE, "No active ExamCycles found.");
            response.put(Constants.RESPONSE, Constants.FAILUREMESSAGE);
            response.setResponseCode(HttpStatus.NOT_FOUND);
        }
        return response;
    }

    public ResponseDto getExamCycleById(Long id) {
        ResponseDto response = new ResponseDto(Constants.API_EXAM_CYCLE_GET_BY_ID);
        Optional<ExamCycle> examCycleOptional = repository.findByIdAndObsolete(id, 0);
        if (examCycleOptional.isPresent()) {
            ExamCycle examCycle = examCycleOptional.get();
            response.put(Constants.MESSAGE, Constants.SUCCESSFUL);
            response.put(Constants.RESPONSE, examCycle);
            response.setResponseCode(HttpStatus.OK);
        } else {
            response.put(Constants.MESSAGE, "ExamCycle not found.");
            response.put(Constants.RESPONSE, Constants.FAILUREMESSAGE);
            response.setResponseCode(HttpStatus.NOT_FOUND);
        }
        return response;
    }
    public ResponseDto updateExamCycle(Long id, ExamCycle updatedExamCycle, String userId) {
        ResponseDto response = new ResponseDto(Constants.API_EXAM_CYCLE_UPDATE);

        // 1. Check if the exam cycle exists.
        ExamCycle existingExamCycle = repository.findByIdAndObsolete(id, 0).orElse(null);
        if (existingExamCycle == null) {
            response.put(Constants.MESSAGE, "ExamCycle not found.");
            response.put(Constants.RESPONSE, Constants.FAILUREMESSAGE);
            response.setResponseCode(HttpStatus.NOT_FOUND);
            return response;
        }

        // 2. Check if the course exists.
        Course course = courseRepository.findById(updatedExamCycle.getCourse().getId()).orElse(null);
        if (course == null) {
            response.put(Constants.MESSAGE, "Course with code " + updatedExamCycle.getCourse().getId() + " not found.");
            response.put(Constants.RESPONSE, Constants.FAILUREMESSAGE);
            response.setResponseCode(HttpStatus.BAD_REQUEST);
            return response;
        }

        try {
            existingExamCycle.setExamCycleName(updatedExamCycle.getExamCycleName());
            existingExamCycle.setCourse(updatedExamCycle.getCourse());
            existingExamCycle.setStartDate(updatedExamCycle.getStartDate());
            existingExamCycle.setEndDate(updatedExamCycle.getEndDate());
            existingExamCycle.setModifiedBy(userId);
            existingExamCycle.setModifiedOn(LocalDateTime.now());
            repository.save(existingExamCycle);

            response.put(Constants.MESSAGE, Constants.SUCCESSMESSAGE);
            response.put(Constants.RESPONSE, existingExamCycle);
            response.setResponseCode(HttpStatus.OK);
            return response;

        } catch (DataIntegrityViolationException e) {
            if (e.getCause() instanceof ConstraintViolationException) {
                response.put(Constants.MESSAGE, "Invalid reference in data. Please check foreign key references.");
                response.put(Constants.RESPONSE, Constants.FAILUREMESSAGE);
                response.setResponseCode(HttpStatus.BAD_REQUEST);
                return response;
            }
            throw e;  // re-throw the exception if it's not the one we're handling
        }
    }

    public ResponseDto deleteExamCycle(Long id) {
        ResponseDto response = new ResponseDto(Constants.API_EXAM_CYCLE_DELETE);
        ExamCycle examCycle = repository.findById(id).orElse(null);
        if (examCycle != null) {
            examCycle.setObsolete(1);
            repository.save(examCycle);
            response.put(Constants.MESSAGE, "ExamCycle soft-deleted successfully.");
            response.put(Constants.RESPONSE, Constants.SUCCESSMESSAGE);
            response.setResponseCode(HttpStatus.OK);
        } else {
            response.put(Constants.MESSAGE, "ExamCycle not found for deletion.");
            response.put(Constants.RESPONSE, Constants.FAILUREMESSAGE);
            response.setResponseCode(HttpStatus.NOT_FOUND);
        }
        return response;
    }

    public ResponseDto restoreExamCycle(Long id) {
        ResponseDto response = new ResponseDto(Constants.API_EXAM_CYCLE_RESTORE);
        ExamCycle examCycle = repository.findById(id).orElse(null);
        if (examCycle != null && examCycle.getObsolete() == 1) {
            examCycle.setObsolete(0);
            repository.save(examCycle);
            response.put(Constants.MESSAGE, "ExamCycle restored successfully.");
            response.put(Constants.RESPONSE, Constants.SUCCESSMESSAGE);
            response.setResponseCode(HttpStatus.OK);
        } else {
            response.put(Constants.MESSAGE, "ExamCycle not found or not marked for restoration.");
            response.put(Constants.RESPONSE, Constants.FAILUREMESSAGE);
            response.setResponseCode(HttpStatus.NOT_FOUND);
        }
        return response;
    }
    public ResponseDto addExamsToCycle(Long id, List<Exam> exams, String userId) {
        ResponseDto response = new ResponseDto(Constants.API_EXAM_CYCLE_ADD_EXAMS);
        ExamCycle examCycle = repository.findById(id).orElse(null);
        if (examCycle == null) {
            response.put(Constants.MESSAGE, "ExamCycle not found.");
            response.put(Constants.RESPONSE, Constants.FAILUREMESSAGE);
            response.setResponseCode(HttpStatus.NOT_FOUND);
            return response;
        }

        for (Exam exam : exams) {
            Optional<Exam> existingExam = examRepository.findByExamNameAndExamDateAndStartTimeAndEndTime(
                    exam.getExamName(), exam.getExamDate(), exam.getStartTime(), exam.getEndTime());
            if (existingExam.isPresent()) {
                response.put(Constants.MESSAGE, "Exam already exists with same details: " + exam.getExamName());
                response.put(Constants.RESPONSE, Constants.FAILUREMESSAGE);
                response.setResponseCode(HttpStatus.CONFLICT);
                return response;
            }

            Course course = courseRepository.findById(exam.getCourse().getId()).orElse(null);
            if (course == null) {
                response.put(Constants.MESSAGE, "Course not found with ID: " + exam.getCourse().getId());
                response.put(Constants.RESPONSE, Constants.FAILUREMESSAGE);
                response.setResponseCode(HttpStatus.NOT_FOUND);
                return response;
            }

            exam.setCourse(course);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
            exam.setStartTime(LocalTime.parse(exam.getStartTime().format(formatter)));
            exam.setEndTime(LocalTime.parse(exam.getEndTime().format(formatter)));
            exam.setCreatedBy(userId);
            exam.setCreatedOn(LocalDateTime.now());
            exam.setExamCycleId(examCycle.getId());
            examRepository.save(exam);
        }

        response.put(Constants.MESSAGE, Constants.SUCCESSMESSAGE);
        response.put(Constants.RESPONSE, examCycle);
        response.setResponseCode(HttpStatus.OK);
        return response;
    }
    public ResponseDto removeExamFromCycle(Long id, Exam exam) {
        ResponseDto response = new ResponseDto(Constants.API_EXAM_CYCLE_REMOVE_EXAM);
        ExamCycle examCycle = repository.findById(id).orElse(null);

        if (examCycle == null) {
            response.put(Constants.MESSAGE, "ExamCycle not found.");
            response.put(Constants.RESPONSE, Constants.FAILUREMESSAGE);
            response.setResponseCode(HttpStatus.NOT_FOUND);
            return response;
        }

        if (exam.getExamCycleId() == null || !exam.getExamCycleId().equals(examCycle.getId())) {
            response.put(Constants.MESSAGE, "The provided exam is not associated with the given ExamCycle.");
            response.put(Constants.RESPONSE, Constants.FAILUREMESSAGE);
            response.setResponseCode(HttpStatus.BAD_REQUEST);
            return response;
        }

        exam.setExamCycleId(null); // Remove the association of exam with the exam cycle
        examRepository.save(exam);

        response.put(Constants.MESSAGE, Constants.SUCCESSMESSAGE);
        response.put(Constants.RESPONSE, examCycle);
        response.setResponseCode(HttpStatus.OK);
        return response;
    }

    public ResponseDto publishExamCycle(Long id) {
        ResponseDto response = new ResponseDto(Constants.API_EXAM_CYCLE_PUBLISH);
        Optional<ExamCycle> optionalExamCycle = repository.findById(id);
        if (!optionalExamCycle.isPresent()) {
            response.put(Constants.MESSAGE, "ExamCycle not found.");
            response.put(Constants.RESPONSE, Constants.FAILUREMESSAGE);
            response.setResponseCode(HttpStatus.NOT_FOUND);
            return response;
        }
        ExamCycle examCycle = optionalExamCycle.get();
        examCycle.setStatus(ExamCycleStatus.PUBLISHED);
        repository.save(examCycle);
        response.put(Constants.MESSAGE, Constants.SUCCESSMESSAGE);
        response.put(Constants.RESPONSE, examCycle);
        response.setResponseCode(HttpStatus.OK);
        return response;
    }

    public ExamCycleDTO toDTO(ExamCycle examCycle) {
        ExamCycleDTO dto = new ExamCycleDTO();
        dto.setId(examCycle.getId());
        dto.setExamCycleName(examCycle.getExamCycleName());

        // Set Course related fields
        if (examCycle.getCourse() != null) {
            dto.setCourseId(examCycle.getCourse().getId());
            dto.setCourseCode(examCycle.getCourse().getCourseCode());
            dto.setCourseName(examCycle.getCourse().getCourseName());
        }

        dto.setStartDate(examCycle.getStartDate());
        dto.setEndDate(examCycle.getEndDate());
        dto.setCreatedBy(examCycle.getCreatedBy());
        dto.setCreatedOn(examCycle.getCreatedOn());
        dto.setModifiedBy(examCycle.getModifiedBy());
        dto.setModifiedOn(examCycle.getModifiedOn());
        dto.setStatus(examCycle.getStatus());
        dto.setObsolete(examCycle.getObsolete());

        return dto;
    }
}