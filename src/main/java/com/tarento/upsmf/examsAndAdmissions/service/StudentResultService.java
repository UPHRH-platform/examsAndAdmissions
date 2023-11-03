package com.tarento.upsmf.examsAndAdmissions.service;

import com.tarento.upsmf.examsAndAdmissions.enums.ResultStatus;
import com.tarento.upsmf.examsAndAdmissions.enums.RetotallingStatus;
import com.tarento.upsmf.examsAndAdmissions.exceptions.CustomException;
import com.tarento.upsmf.examsAndAdmissions.model.*;
import com.tarento.upsmf.examsAndAdmissions.model.dto.*;
import com.tarento.upsmf.examsAndAdmissions.repository.*;
import com.tarento.upsmf.examsAndAdmissions.util.Constants;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.codehaus.jettison.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class StudentResultService {

    @Autowired
    private StudentResultRepository studentResultRepository;
    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private InstituteRepository instituteRepository;
    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private ExamRepository examRepository;
    @Autowired
    private ExamCycleRepository examCycleRepository;
    @Autowired
    private RetotallingRequestRepository retotallingRequestRepository;
    @Autowired
    private DataImporterService dataImporterService;
    @Autowired
    private StudentExamRegistrationRepository studentExamRegistrationRepository;

    Map<String, Class<?>> columnConfig = Map.of(
            "Start Date", Date.class,
            "End Date", Date.class,
            "Start Time", Date.class,
            "End Time", Date.class
            // Add other columns and their data types as needed
    );

    public ResponseDto importInternalMarksFromExcel(MultipartFile file) {
        ResponseDto response = new ResponseDto(Constants.API_IMPORT_INTERNAL_MARKS_FROM_EXCEL);

        try {
            // Parse the Excel file
            List<StudentResult> results = parseExcel(file);

            // For each result, update or create the record with internal marks
            for (StudentResult result : results) {
                Optional<StudentResult> existingResult = Optional.ofNullable(studentResultRepository.findByStudent_EnrollmentNumber(result.getStudent().getEnrollmentNumber()));

                if (existingResult.isPresent()) {
                    StudentResult dbResult = existingResult.get();
                    dbResult.setInternalMarksObtained(result.getInternalMarksObtained());
                    dbResult.setPracticalMarksObtained(result.getPracticalMarksObtained());
                    dbResult.setInternalMarkFlag(true);
                    studentResultRepository.save(dbResult);
                } else {
                    result.setInternalMarkFlag(true);
                    studentResultRepository.save(result);
                }
            }

            response.put(Constants.MESSAGE, "Internal marks imported successfully.");
            response.put(Constants.RESPONSE, "Internal marks for " + results.size() + " students have been imported or updated.");
            response.setResponseCode(HttpStatus.OK);
        }catch (IOException | CustomException e) {
            log.error("Failed to import internal marks from Excel", e);
            ResponseDto.setErrorResponse(response, "IMPORT_FAILED", e.getMessage(), HttpStatus.NOT_FOUND);
        }
        return response;
    }


    public ResponseDto importExternalMarksFromExcel(MultipartFile file) {
        ResponseDto response = new ResponseDto(Constants.API_IMPORT_EXTERNAL_MARKS_FROM_EXCEL);

        try {
            // Parse the Excel file
            List<StudentResult> results = parseExcel(file);

            // For each result, update or create the record with external marks
            for (StudentResult result : results) {
                Optional<StudentResult> existingResult = Optional.ofNullable(studentResultRepository.findByStudent_EnrollmentNumber(result.getStudent().getEnrollmentNumber()));

                if (existingResult.isPresent()) {
                    StudentResult dbResult = existingResult.get();
                    dbResult.setExternalMarks(result.getExternalMarks());
                    dbResult.setPassingExternalMarks(result.getPassingExternalMarks());
                    dbResult.setExternalMarksObtained(result.getExternalMarksObtained());
                    dbResult.setFinalMarkFlag(true);
                    studentResultRepository.save(dbResult);
                } else {
                    result.setFinalMarkFlag(true);
                    studentResultRepository.save(result);
                }
            }

            response.put(Constants.MESSAGE, "External marks imported successfully.");
            response.put(Constants.RESPONSE, "External marks for " + results.size() + " students have been imported or updated.");
            response.setResponseCode(HttpStatus.OK);
        } catch (IOException | CustomException e) {
            log.error("Failed to import external marks from Excel", e);
            ResponseDto.setErrorResponse(response, "IMPORT_FAILED", e.getMessage(), HttpStatus.NOT_FOUND);
        }

        return response;
    }

    private List<StudentResult> parseExcel(MultipartFile file) throws IOException, CustomException {
        List<StudentResult> resultList = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        try (XSSFWorkbook workbook = new XSSFWorkbook(file.getInputStream())) {
            XSSFSheet sheet = workbook.getSheetAt(0);

            Iterator<Row> rowIterator = sheet.iterator();
            if (rowIterator.hasNext()) rowIterator.next(); // skip header row

            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                if (isRowEmpty(row)) {
                    continue;
                }
                StudentResult result = new StudentResult();

                try {
                    ResponseDto studentResponse = fetchStudentByEnrollmentNumber(getStringValue(row.getCell(2)));
                    if (studentResponse.getResponseCode() != HttpStatus.OK) {
                        String errorMessage = String.format("Failed to fetch student details for enrollment number: %s. Server responded with status: %s",
                                getStringValue(row.getCell(2)), studentResponse.getResponseCode());
                        throw new CustomException(errorMessage);
                    }
                    Student studentEntity = (Student) studentResponse.get(Constants.RESPONSE);

                    ResponseDto courseResponse = fetchCourseByName(getStringValue(row.getCell(5)));
                    if (courseResponse.getResponseCode() != HttpStatus.OK) {
                        String errorMessage = String.format("Failed to fetch course details for course name: %s. Server responded with status: %s",
                                getStringValue(row.getCell(5)), courseResponse.getResponseCode());
                        throw new CustomException(errorMessage);
                    }
                    Course courseEntity = (Course) courseResponse.get(Constants.RESPONSE);

                    ResponseDto examCycleResponse = fetchExamCycleByName(getStringValue(row.getCell(6)));
                    if (examCycleResponse.getResponseCode() != HttpStatus.OK) {
                        String errorMessage = String.format("Failed to fetch exam cycle details for cycle name: %s. Server responded with status: %s",
                                getStringValue(row.getCell(6)), examCycleResponse.getResponseCode());
                        throw new CustomException(errorMessage);
                    }
                    ExamCycle examCycleEntity = (ExamCycle) examCycleResponse.get(Constants.RESPONSE);

                    ResponseDto examResponse = fetchExamByName(getStringValue(row.getCell(7)));
                    if (examResponse.getResponseCode() != HttpStatus.OK) {
                        String errorMessage = String.format("Failed to fetch exam details for exam name: %s. Server responded with status: %s",
                                getStringValue(row.getCell(7)), examResponse.getResponseCode());
                        throw new CustomException(errorMessage);
                    }
                    Exam examEntity = (Exam) examResponse.get(Constants.RESPONSE);

                    result.setStudent(studentEntity);
                    result.setCourse(courseEntity);
                    result.setExamCycle(examCycleEntity);
                    result.setExam(examEntity);

                    result.setInternalMarks(getIntegerValue(row.getCell(8)));
                    result.setPassingInternalMarks(getIntegerValue(row.getCell(9)));
                    result.setInternalMarksObtained(getIntegerValue(row.getCell(10)));

                    result.setExternalMarks(getIntegerValue(row.getCell(11)));
                    result.setPassingExternalMarks(getIntegerValue(row.getCell(12)));
                    result.setExternalMarksObtained(getIntegerValue(row.getCell(13)));

                    result.setPracticalMarks(getIntegerValue(row.getCell(14)));
                    result.setPassingPracticalMarks(getIntegerValue(row.getCell(15)));
                    result.setPracticalMarksObtained(getIntegerValue(row.getCell(16)));

                    result.setOtherMarks(getIntegerValue(row.getCell(17)));
                    result.setPassingOtherMarks(getIntegerValue(row.getCell(18)));
                    result.setOtherMarksObtained(getIntegerValue(row.getCell(19)));

                    result.setTotalMarks(getIntegerValue(row.getCell(20)));
                    result.setPassingTotalMarks(getIntegerValue(row.getCell(21)));
                    result.setTotalMarksObtained(getIntegerValue(row.getCell(22)));

                    result.setGrade(getStringValue(row.getCell(23)));
                    result.setResult(getStringValue(row.getCell(24)));

                    if (validateStudentResult(result)) {
                        resultList.add(result);
                    } else {
                        errors.add("Validation failed for enrolment number: " + studentEntity.getEnrollmentNumber());
                    }
                } catch (CustomException ce) {
                    log.error("Custom Exception encountered", ce);
                    errors.add(ce.getMessage()); // This will add your custom error message to the errors list
                }
            }
        } catch (IOException e) {
            log.error("Error reading the Excel file", e);
            throw new CustomException("Failed to read the Excel file", e);
        }

        if (!errors.isEmpty()) {
            String combinedErrorMessage = String.join(", ", errors);
            throw new CustomException("Errors encountered while processing Excel file: " + combinedErrorMessage);
        }
        return resultList;
    }
    public static boolean isRowEmpty(Row row) {
        if (row == null) {
            return true;
        }
        for (int c = row.getFirstCellNum(); c < row.getLastCellNum(); c++) {
            Cell cell = row.getCell(c);
            if (cell != null && cell.getCellType() != CellType.BLANK && StringUtils.isNotBlank(cell.toString())) {
                return false;
            }
        }
        return true;
    }

    public ResponseDto getStudentResult(Long id) {
        ResponseDto response = new ResponseDto(Constants.API_GET_STUDENT_RESULT_BY_ID);

        Optional<StudentResult> resultOpt = studentResultRepository.findById(id);

        if (resultOpt.isPresent()) {
            response.put(Constants.MESSAGE, "Successful.");
            response.put(Constants.RESPONSE, resultOpt.get());
            response.setResponseCode(HttpStatus.OK);
        } else {
            ResponseDto.setErrorResponse(response, "RESULT_NOT_FOUND", "Result not found with ID: " + id, HttpStatus.NOT_FOUND);
        }

        return response;
    }

    public ResponseDto getAllStudentResults() {
        ResponseDto response = new ResponseDto(Constants.API_GET_ALL_STUDENT_RESULTS);
        List<StudentResult> results = studentResultRepository.findAll();

        if (!results.isEmpty()) {
            response.put(Constants.MESSAGE, "Successful.");
            response.put(Constants.RESPONSE, results);
            response.setResponseCode(HttpStatus.OK);
        } else {
            ResponseDto.setErrorResponse(response, "NO_RESULTS_FOUND", "No student results found.", HttpStatus.NOT_FOUND);
        }

        return response;
    }

    public ResponseDto fetchStudentByEnrollmentNumber(String enrollmentNumber) {
        ResponseDto response = new ResponseDto(Constants.API_FETCH_STUDENT_BY_ENROLLMENT_NUMBER);
        Optional<Student> studentOpt = studentRepository.findByEnrollmentNumber(enrollmentNumber);

        if (studentOpt.isPresent()) {
            response.put(Constants.MESSAGE, "Successful.");
            response.put(Constants.RESPONSE, studentOpt.get());
            response.setResponseCode(HttpStatus.OK);
        } else {
            ResponseDto.setErrorResponse(response, "STUDENT_NOT_FOUND", "Student not found with enrollment number: " + enrollmentNumber, HttpStatus.NOT_FOUND);
        }

        return response;
    }


    public ResponseDto fetchCourseByName(String courseName) {
        ResponseDto response = new ResponseDto(Constants.API_FETCH_COURSE_BY_NAME);
        Optional<Course> courseOpt = courseRepository.findByCourseNameIgnoreCase(courseName.trim());

        if (courseOpt.isPresent()) {
            response.put(Constants.MESSAGE, "Successful.");
            response.put(Constants.RESPONSE, courseOpt.get());
            response.setResponseCode(HttpStatus.OK);
        } else {
            ResponseDto.setErrorResponse(response, "COURSE_NOT_FOUND", "Course not found with name: " + courseName, HttpStatus.NOT_FOUND);
        }

        return response;
    }
    public ResponseDto fetchExamCycleByName(String examCycleName) {
        ResponseDto response = new ResponseDto(Constants.API_FETCH_EXAM_CYCLE_BY_NAME);
        Optional<ExamCycle> examCycleOpt = Optional.ofNullable(examCycleRepository.findByExamCycleName(examCycleName));

        if (examCycleOpt.isPresent()) {
            response.put(Constants.MESSAGE, "Successful.");
            response.put(Constants.RESPONSE, examCycleOpt.get());
            response.setResponseCode(HttpStatus.OK);
        } else {
            ResponseDto.setErrorResponse(response, "EXAM_CYCLE_NOT_FOUND", "Exam cycle not found with name: " + examCycleName, HttpStatus.NOT_FOUND);
        }

        return response;
    }
    public ResponseDto fetchExamByName(String examName) {
        ResponseDto response = new ResponseDto(Constants.API_FETCH_EXAM_BY_NAME);
        Optional<Exam> examOpt = examRepository.findByExamName(examName);

        if (examOpt.isPresent()) {
            response.put(Constants.MESSAGE, "Successful.");
            response.put(Constants.RESPONSE, examOpt.get());
            response.setResponseCode(HttpStatus.OK);
        } else {
            ResponseDto.setErrorResponse(response, "EXAM_NOT_FOUND", "Exam not found with name: " + examName, HttpStatus.NOT_FOUND);
        }

        return response;
    }

    private String getStringValue(Cell cell) {
        if (cell == null) return null;
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                return String.valueOf((int) cell.getNumericCellValue());
            default:
                return null;
        }
    }

    public static Integer getIntegerValue(Cell cell) {
        if (cell == null) {
            return null;
        }
        if (cell.getCellType() == CellType.NUMERIC) {
            double numericValue = cell.getNumericCellValue();
            return (int) numericValue;
        } else if (cell.getCellType() == CellType.STRING) {
            try {
                return Integer.parseInt(cell.getStringCellValue());
            } catch (NumberFormatException e) {
                return null;
            }
        } else {
            return null;
        }
    }

    private boolean validateStudentResult(StudentResult result) {
        // Validate mandatory fields
        if (result.getStudent() == null || result.getCourse() == null || result.getExam() == null) {
            return false;
        }

        // Validate marks
        if (isValidMark(result.getInternalMarks())
                || isValidMark(result.getPassingInternalMarks())
                || isValidMark(result.getInternalMarksObtained())
                || isValidMark(result.getExternalMarks())
                || isValidMark(result.getPassingExternalMarks())
                || isValidMark(result.getExternalMarksObtained())
                || isValidMark(result.getPracticalMarks())
                || isValidMark(result.getPassingPracticalMarks())
                || isValidMark(result.getPracticalMarksObtained())
                || isValidMark(result.getOtherMarks())
                || isValidMark(result.getPassingOtherMarks())
                || isValidMark(result.getOtherMarksObtained())
                || isValidMark(result.getTotalMarks())
                || isValidMark(result.getPassingTotalMarks())
                || isValidMark(result.getTotalMarksObtained())) {
            return true;
        }
        return false;
    }

    private static boolean isValidMark(Integer marks) {
        return marks == null || (marks >= 0 && marks <= 100);
    }
    public ResponseDto publishResultsForCourseWithinCycle(Long courseId, Long examCycleId) {
        ResponseDto response = new ResponseDto(Constants.API_PUBLISH_RESULTS_FOR_COURSE_WITHIN_CYCLE);
        List<StudentResult> resultsForCourse = studentResultRepository.findByCourse_IdAndExam_ExamCycleIdAndPublished(courseId, examCycleId, false);

        if (!resultsForCourse.isEmpty()) {
            for (StudentResult result : resultsForCourse) {
                result.setPublished(true);
            }
            studentResultRepository.saveAll(resultsForCourse);
            response.put(Constants.MESSAGE, "Successfully published results.");
            response.setResponseCode(HttpStatus.OK);
        } else {
            ResponseDto.setErrorResponse(response, "NO_RESULTS_FOUND", "No results found for the given course and exam cycle.", HttpStatus.NOT_FOUND);
        }

        return response;
    }

    public ResponseDto findByStudentIdAndExamCycleId(Long studentId, Long examCycleId) {
        ResponseDto response = new ResponseDto(Constants.API_FIND_BY_ENROLLMENT_NUMBER_AND_DOB);
//        List<StudentResult> studentResultList = studentResultRepository.findByStudent_EnrollmentNumberAndExamCycle_IdAndPublished(enrollmentNumber, examCycleId, true);

        String examCycleName = examCycleRepository.getExamCycleNameById(examCycleId);
        String enrollmentNumber = studentRepository.getEnrollmentNumberById(studentId);
        List<StudentResult> studentResultList = studentResultRepository.findByStudent_EnrollmentNumberAndExamCycleNameAndPublished(enrollmentNumber, examCycleName, true);

        if (!studentResultList.isEmpty()) {
            StudentResultDTO studentResultDTO = mapToDTO(studentResultList);

            response.put(Constants.MESSAGE, "Successful.");
            response.put(Constants.RESPONSE, studentResultDTO);
            response.setResponseCode(HttpStatus.OK);
        } else {
            ResponseDto.setErrorResponse(response, "RESULT_NOT_FOUND", "No result found for the given studentId and examCycle.", HttpStatus.NOT_FOUND);
        }

        return response;
    }

    private StudentResultDTO mapToDTO(List<StudentResult> studentResultList) {
        StudentResultDTO dto = new StudentResultDTO();

        if (studentResultList == null || studentResultList.isEmpty()) {
            return dto; // return an empty DTO if the input list is null or empty
        }

        // Set common details using the first entry.
        StudentResult firstResult = studentResultList.get(0);
//        dto.setFirstName(firstResult.getStudent().getFirstName());
        dto.setFirstName(firstResult.getFirstName());
//        dto.setLastName(firstResult.getStudent().getSurname());
        dto.setLastName(firstResult.getLastName());
//        dto.setEnrollmentNumber(firstResult.getStudent().getEnrollmentNumber());
        dto.setEnrollmentNumber(firstResult.getEnrollmentNumber());
        Student student = studentRepository.findByEnrollmentNumber(firstResult.getEnrollmentNumber()).orElseThrow();
//        dto.setDateOfBirth(firstResult.getStudent().getDateOfBirth());
        dto.setDateOfBirth(student.getDateOfBirth());
        if (firstResult.getExamCycle_name() != null) {
            ExamCycle examCycle = examCycleRepository.findByExamCycleName(firstResult.getExamCycle_name());
            if (examCycle.getCourse() != null) {
                dto.setCourseYear(examCycle.getCourse().getCourseYear());
                dto.setCourseName(examCycle.getCourse().getCourseName());
            }
        }

        List<ExamDetailsDTO> examsList = studentResultList.stream()
                .map(studentResult -> {
                    ExamDetailsDTO examDto = new ExamDetailsDTO();
                    examDto.setExamName(studentResult.getExam_name());
                    examDto.setInternalMarks(studentResult.getInternalMarksObtained());
                    examDto.setExternalMarks(studentResult.getExternalMarksObtained());
                    examDto.setTotalMarks(studentResult.getTotalMarksObtained());
                    examDto.setGrade(studentResult.getGrade());
                    examDto.setResult(studentResult.getResult());
                    examDto.setStatus(studentResult.getStatus().name());
                    return examDto;
                })
                .collect(Collectors.toList());

        dto.setExamDetails(examsList);

        return dto;
    }

    public ResponseDto updateResultAfterRetotalling(StudentResult updatedResult) {
        ResponseDto response = new ResponseDto(Constants.API_UPDATE_RESULT_AFTER_RETOTALLING);

        if (updatedResult.getId() == null) {
            ResponseDto.setErrorResponse(response, "INVALID_RESULT_ID", "Updated result must have a valid ID", HttpStatus.BAD_REQUEST);
            return response;
        }

        Optional<StudentResult> existingResultOpt = studentResultRepository.findById(updatedResult.getId());
        if (existingResultOpt.isEmpty()) {
            ResponseDto.setErrorResponse(response, "RESULT_NOT_FOUND", "No StudentResult found with ID " + updatedResult.getId(), HttpStatus.NOT_FOUND);
            return response;
        }

        StudentResult existingResult = existingResultOpt.get();
        existingResult.setInternalMarksObtained(updatedResult.getInternalMarksObtained());
        existingResult.setExternalMarksObtained(updatedResult.getExternalMarksObtained());
        existingResult.setPracticalMarksObtained(updatedResult.getPracticalMarksObtained());
        existingResult.setOtherMarksObtained(updatedResult.getOtherMarksObtained());
        existingResult.setTotalMarksObtained(updatedResult.getTotalMarksObtained());
        existingResult.setGrade(updatedResult.getGrade());
        existingResult.setResult(updatedResult.getResult());
        existingResult.setStatus(ResultStatus.REVALUATED);

        studentResultRepository.save(existingResult);

        Optional<RetotallingRequest> retotallingRequestOpt = retotallingRequestRepository.findByStudentAndExams(existingResult.getStudent(), updatedResult.getExam());
        if (!retotallingRequestOpt.isPresent()) {
            ResponseDto.setErrorResponse(response, "RETOTALLING_REQUEST_NOT_FOUND", "No RetotallingRequest found for student and exam", HttpStatus.NOT_FOUND);
            return response;
        }

        RetotallingRequest retotallingRequest = retotallingRequestOpt.get();
        retotallingRequest.setStatus(RetotallingStatus.COMPLETED);
        retotallingRequestRepository.save(retotallingRequest);

        response.put(Constants.MESSAGE, "Successfully updated result after retotalling.");
        response.setResponseCode(HttpStatus.OK);
        return response;
    }
    public ResponseDto getResultsByExamCycleAndExamGroupedByInstitute(Long examCycle, Exam exam) {
        ResponseDto response = new ResponseDto(Constants.API_GET_RESULTS_BY_EXAM_CYCLE_AND_EXAM_GROUPED_BY_INSTITUTE);

        List<StudentResult> results;
        if (examCycle != null && exam != null) {
            results = studentResultRepository.findByExamCycleAndExam(examCycle, exam);
        } else if (examCycle != null) {
            results = studentResultRepository.findByExamCycleId(examCycle);
        } else if (exam != null) {
            results = studentResultRepository.findByExam(exam);
        } else {
            results = studentResultRepository.findAll();
        }

        if (results.isEmpty()) {
            ResponseDto.setErrorResponse(response, "NO_RESULTS_FOUND", "No results found for the given criteria.", HttpStatus.NOT_FOUND);
            return response;
        }

        // Instead of grouping by institute, create a list of DTOs that correspond to the table rows
        List<ResultDisplayDto> displayResults = results.stream()
                .map(result -> {ResultDisplayDto dto = new ResultDisplayDto();
                    dto.setId(result.getId());
                    dto.setInstituteName(result.getStudent().getInstitute().getInstituteName());
                    dto.setInstitute_id(result.getStudent().getInstitute().getId());
                    dto.setFirstName(result.getFirstName());
                    dto.setLastName(result.getLastName());
                    dto.setEnrollmentNumber(result.getEnrollmentNumber());
                    dto.setMotherName(result.getMotherName());
                    dto.setFatherName(result.getFatherName());

                    dto.setCourseValue(result.getCourse_name());
                    dto.setExamCycleValue(result.getExamCycle_name());
                    dto.setExamValue(result.getExam_name());

                    dto.setInternalMarks(result.getInternalMarks());
                    dto.setPassingInternalMarks(result.getPassingInternalMarks());
                    dto.setInternalMarksObtained(result.getInternalMarksObtained());

                    dto.setPracticalMarks(result.getPracticalMarks());
                    dto.setPassingPracticalMarks(result.getPassingPracticalMarks());
                    dto.setPracticalMarksObtained(result.getPracticalMarksObtained());

                    dto.setOtherMarks(result.getOtherMarks());
                    dto.setPassingOtherMarks(result.getPassingOtherMarks());
                    dto.setOtherMarksObtained(result.getOtherMarksObtained());

                    dto.setExternalMarks(result.getExternalMarks());
                    dto.setPassingExternalMarks(result.getPassingExternalMarks());
                    dto.setExternalMarksObtained(result.getExternalMarksObtained());

                    dto.setTotalMarks(result.getTotalMarks());
                    dto.setPassingTotalMarks(result.getPassingTotalMarks());
                    dto.setTotalMarksObtained(result.getTotalMarksObtained());

                    dto.setGrade(result.getGrade());
                    dto.setResult(result.getResult());
                    dto.setStatus(result.getStatus());
                    dto.setPublished(result.isPublished());

                    return dto;
                })
                .collect(Collectors.toList());

        response.put(Constants.MESSAGE, "Successful.");
        response.put(Constants.RESPONSE, displayResults);
        response.setResponseCode(HttpStatus.OK);
        return response;
    }

    private ExamResultDTO convertToDTO(StudentResult result) {
        ExamResultDTO dto = new ExamResultDTO();

        dto.setInstituteName(result.getStudent().getInstitute().getInstituteName());
        dto.setInstitute_id(result.getStudent().getInstitute().getId());
        dto.setStudentName(result.getFirstName() + " " + result.getLastName());
        dto.setCourseName(result.getCourse().getCourseName());
        dto.setExamName(result.getExam().getExamName());
        dto.setInternalMarks(result.getInternalMarks());

        return dto;
    }
    public ResponseDto processBulkResultUpload(MultipartFile file, String fileType, Long instituteId) {
        ResponseDto response = new ResponseDto(Constants.API_BULK_UPLOAD_RESULTS);

        try {
            JSONArray jsonArray;

            switch (fileType.toLowerCase()) {
                case Constants.CSV:
                    jsonArray = dataImporterService.csvToJson(file, columnConfig);
                    break;
                case Constants.EXCEL:
                    jsonArray = dataImporterService.excelToJson(file);
                    break;
                default:
                    return ResponseDto.setErrorResponse(response, "UNSUPPORTED_FILE_TYPE", "Unsupported file type", HttpStatus.BAD_REQUEST);
            }

            List<StudentResult> dtoList = dataImporterService.convertJsonToDtoList(jsonArray, StudentResult.class);
            ValidationResultDto validationResult = dataImporterService.convertResultDtoListToEntities(dtoList, studentResultRepository, instituteId);

            if (validationResult.isValid()) {
                response.put(Constants.MESSAGE, "Bulk upload success");
                response.put("Data",validationResult.getSavedEntities());
                response.setResponseCode(HttpStatus.OK);
            } else {
                if (!validationResult.getValidationErrors().isEmpty()) {
                    response.put("validationErrors", validationResult.getValidationErrors());
                    response.setResponseCode(HttpStatus.BAD_REQUEST);
                } else {
                    return ResponseDto.setErrorResponse(response, "INTERNAL_ERROR", "An unexpected error occurred.", HttpStatus.INTERNAL_SERVER_ERROR);
                }
            }

            return response; // Return the ResponseDto without using ResponseEntity
        } catch (Exception e) {
            return ResponseDto.setErrorResponse(response, "INTERNAL_ERROR", "An unexpected error occurred.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseDto processBulkResultUploadWithExternals(MultipartFile file, String fileType) {
        ResponseDto response = new ResponseDto(Constants.API_BULK_UPLOAD_RESULTS);

        try {
            JSONArray jsonArray;

            switch (fileType.toLowerCase()) {
                case Constants.CSV:
                    jsonArray = dataImporterService.csvToJson(file,columnConfig);
                    break;
                case Constants.EXCEL:
                    jsonArray = dataImporterService.excelToJson(file);
                    break;
                default:
                    // Handle unsupported file type
                    return ResponseDto.setErrorResponse(response, "UNSUPPORTED_FILE_TYPE", "Unsupported file type", HttpStatus.BAD_REQUEST);
            }
            String[] selectedColumns = { "First Name", "Last Name", "Enrolment Number","External Marks", "Passing External Marks", "External Marks Obtained" };
            JSONArray filteredJsonArray = dataImporterService.filterColumns(jsonArray, selectedColumns);
            List<StudentResult> dtoList = dataImporterService.convertJsonToDtoList(filteredJsonArray, StudentResult.class);
            ValidationResultDto validationResult = dataImporterService.convertResultDtoListToEntitiesExternalMarks(dtoList, studentResultRepository);

            if (validationResult.isValid()) {
                response.put(Constants.MESSAGE, "Bulk upload success");
                response.put("Data",validationResult.getSavedEntities());
                response.setResponseCode(HttpStatus.OK);
            } else {
                if (!validationResult.getValidationErrors().isEmpty()) {
                    response.put("validationErrors", validationResult.getValidationErrors());
                    response.setResponseCode(HttpStatus.BAD_REQUEST);
                } else {
                    return ResponseDto.setErrorResponse(response, "INTERNAL_ERROR", "An unexpected error occurred.", HttpStatus.INTERNAL_SERVER_ERROR);
                }
            }
            return response;
        } catch (Exception e) {
            return ResponseDto.setErrorResponse(response, "INTERNAL_ERROR", "An unexpected error occurred.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    public ResponseDto getResultsByInstituteAndExamCycle(Long instituteId, Long examCycleId) {
        ResponseDto response = new ResponseDto(Constants.API_RESULTS_GET_BY_INSTITUTE_AND_CYCLE);
        String examCycleName = examCycleRepository.getExamCycleNameById(examCycleId);
//        List<StudentResult> results = studentResultRepository.findByInstituteIdAndExamCycleId(instituteId, examCycleId);
        List<StudentResult> results = studentResultRepository.findByInstituteIdAndExamCycleName(instituteId, examCycleName);
        Institute institute = instituteRepository.findById(instituteId).orElseThrow();
        if (!results.isEmpty()) {
            List<ResultDisplayDto> dtos = results.stream()
                    .map(result -> {
                        ResultDisplayDto dto = new ResultDisplayDto();
                        dto.setId(result.getId());
                        dto.setInstituteName(institute.getInstituteName());
                        dto.setInstitute_id(result.getInstituteId());
                        dto.setFirstName(result.getFirstName());
                        dto.setLastName(result.getLastName());
                        dto.setEnrollmentNumber(result.getEnrollmentNumber());
                        dto.setMotherName(result.getMotherName());
                        dto.setFatherName(result.getFatherName());
                        dto.setCourseValue(result.getCourse_name());
                        dto.setExamCycleValue(result.getExamCycle_name());
                        dto.setExamValue(result.getExam_name());
                        dto.setInternalMarks(result.getInternalMarks());
                        dto.setPassingInternalMarks(result.getPassingInternalMarks());
                        dto.setInternalMarksObtained(result.getInternalMarksObtained());
                        dto.setPracticalMarks(result.getPracticalMarks());
                        dto.setPassingPracticalMarks(result.getPassingPracticalMarks());
                        dto.setPracticalMarksObtained(result.getPracticalMarksObtained());
                        dto.setExternalMarks(result.getExternalMarks());
                        dto.setPassingExternalMarks(result.getPassingExternalMarks());
                        dto.setExternalMarksObtained(result.getExternalMarksObtained());
                        dto.setTotalMarks(result.getTotalMarks());
                        dto.setPassingTotalMarks(result.getPassingTotalMarks());
                        dto.setTotalMarksObtained(result.getTotalMarksObtained());
                        dto.setGrade(result.getGrade());
                        dto.setResult(result.getResult());
                        return dto;
                    })
                    .collect(Collectors.toList());

            response.put(Constants.MESSAGE, Constants.SUCCESS);
            response.put(Constants.RESPONSE, dtos);
            response.setResponseCode(HttpStatus.OK);

        } else {
            ResponseDto.setErrorResponse(response, "RESULTS_NOT_FOUND", "No results found for the given institute and exam cycle.", HttpStatus.NOT_FOUND);
        }

        return response;
    }
    public ResponseDto getExamResultsByExamCycle(Long examCycle) {
        ResponseDto response = new ResponseDto(Constants.API_EXAM_CYCLE_MANAGE_RESULTS);
        String examCycleName = examCycleRepository.getExamCycleNameById(examCycle);
        List<StudentResult> results = studentResultRepository.findByExamCycleName(examCycleName);

        if (results == null || results.isEmpty()) {
            ResponseDto.setErrorResponse(response, "RECORD_NOT_FOUND", "No record found for the given exam cycle.", HttpStatus.NOT_FOUND);
            return response;
        }

        Map<Long, ProcessedResultDto> processedResults = getProcessedResults(results);

        response.put(Constants.MESSAGE, "Results fetched successfully.");
        response.put(Constants.RESPONSE, new ArrayList<>(processedResults.values()));
        response.setResponseCode(HttpStatus.OK);
        return response;
    }

    private Map<Long, ProcessedResultDto> getProcessedResults(List<StudentResult> results) {
        Map<Long, ProcessedResultDto> processedResults = new HashMap<>();
        for (StudentResult result : results) {
            Long instituteId = result.getInstituteId();
            Institute institute = instituteRepository.findById(instituteId).orElseThrow();

            ProcessedResultDto instituteResult = processedResults.computeIfAbsent(instituteId, id -> {
                ProcessedResultDto dto = new ProcessedResultDto();
                dto.setInstituteId(instituteId);
                dto.setInstituteName(institute.getInstituteName());
                dto.setCourse(result.getCourse_name());
                return dto;
            });
            instituteResult.setHasInternalMarks(result.isInternalMarkFlag());
            instituteResult.setHasFinalMarks(result.isFinalMarkFlag());
            instituteResult.setHasRevisedFinalMarks(result.isRevisedFinalMarkFlag());
        }
        return processedResults;
    }

    public ResponseDto getMarksByInstituteAndExamCycle(Long examCycle, Long exam, Long institute) {
        ResponseDto response = new ResponseDto(Constants.API_SINGLE_EXAM_MARK);
        String examCycleName = examCycleRepository.getExamCycleNameById(examCycle);
        String examName = examRepository.getExamNameById(exam);
        List<StudentResult> studentResults = studentResultRepository.findByExamCycleNameAndExamNameAndInstitute(examCycleName, examName, institute);

        // If no results found, return a not found response.
        if(studentResults.isEmpty()) {
            ResponseDto.setErrorResponse(response, "RESULTS_NOT_FOUND", "No results found for the provided criteria.", HttpStatus.NOT_FOUND);
            return response;
        }

        List<StudentResultDto> dtos = studentResults.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());

        response.put(Constants.MESSAGE, Constants.SUCCESSMESSAGE);
        response.put(Constants.RESPONSE, dtos);
        response.setResponseCode(HttpStatus.OK);
        return response;
    }

    private StudentResultDto convertToDto(StudentResult result) {
        StudentResultDto dto = new StudentResultDto();
        dto.setFirstName(result.getFirstName());
        dto.setLastName(result.getLastName());
        dto.setCourseName(result.getCourse_name());
        dto.setExam(result.getExam_name());
        dto.setInternalMark(result.getInternalMarksObtained());
        dto.setEnrollmentNumber(result.getEnrollmentNumber());
        return dto;
    }
    public ResponseDto getExamsForExamCycleAndUploadStatusForInstitute(Long examCycleId, Long instituteId) {
        ResponseDto response = new ResponseDto(Constants.API_EXAMS_FOR_EXAM_CYCLE);

        try {
            // Fetch all exams for the exam cycle
            List<Exam> exams = examRepository.findByExamCycleId(examCycleId);
            String examCycleName = examCycleRepository.getExamCycleNameById(examCycleId);
            List<ExamDetailsDto> dtos = exams.stream().map(exam -> {
                ExamDetailsDto dto = new ExamDetailsDto();
                dto.setExamId(exam.getId());
                dto.setExamName(exam.getExamName());

                // Set the last date to upload marks directly from the exam
                dto.setLastDateToUploadInternalMarks(exam.getLastDateToUploadMarks());

                // Now check for student results for this exam and institute
                List<StudentResult> resultsForExam = studentResultRepository.findByExamCycleNameAndInstituteId(examCycleName, instituteId);

                if (!resultsForExam.isEmpty()) {
                    // If we find any student result records, it means internal marks have been uploaded
                    dto.setInternalMarksUploadStatus(true);
                } else {
                    // If no student results, then marks have not been uploaded
                    dto.setInternalMarksUploadStatus(false);
                }

                return dto;
            }).collect(Collectors.toList());

            if (!dtos.isEmpty()) {
                response.put(Constants.MESSAGE, Constants.SUCCESSFUL);
                response.put(Constants.RESPONSE, dtos);
                response.setResponseCode(HttpStatus.OK);
            } else {
                return ResponseDto.setErrorResponse(response, "NO_EXAMS_FOUND", "No exams found for the provided exam cycle.", HttpStatus.NOT_FOUND);
            }

        } catch (Exception e) {
            // Handle any unexpected errors that might occur during the process.
            return ResponseDto.setErrorResponse(response, "INTERNAL_SERVER_ERROR", e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return response;
    }

    public ResponseDto deleteExternalMarks(Long examCycleId, Long instituteId) {
        ResponseDto response = new ResponseDto(Constants.API_DELETE_FINAL_MARKS);

        // Logic to set external marks to null for all students of the given institute for the specified exam cycle
        int updatedCount = studentResultRepository.setExternalMarksToNull(examCycleId, instituteId);

        if(updatedCount == 0) {
            ResponseDto.setErrorResponse(response, "NO_DATA_FOUND", "No external marks found for the provided criteria.", HttpStatus.NOT_FOUND);
            return response;
        }

        response.put(Constants.MESSAGE, "External marks removed successfully for " + updatedCount + " students.");
        response.setResponseCode(HttpStatus.OK);
        return response;
    }
}