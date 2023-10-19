package com.tarento.upsmf.examsAndAdmissions.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tarento.upsmf.examsAndAdmissions.enums.ApprovalStatus;
import com.tarento.upsmf.examsAndAdmissions.exception.ValidationException;
import com.tarento.upsmf.examsAndAdmissions.model.AttendanceRecord;
import com.tarento.upsmf.examsAndAdmissions.model.ExamUploadData;
import com.tarento.upsmf.examsAndAdmissions.model.StudentResult;
import com.tarento.upsmf.examsAndAdmissions.model.UploadStatusDetails;
import com.tarento.upsmf.examsAndAdmissions.repository.*;
import com.tarento.upsmf.examsAndAdmissions.util.DataValidation;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

@Service
public class DataImporterService {
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private StudentResultRepository studentResultRepository;
    @Autowired
    private AttendanceRepository attendanceRepository;
    @Autowired
    private ExamEntityRepository examCycleRepository;

    public JSONArray excelToJson(MultipartFile excelFile) throws IOException, JSONException {
        try (InputStream inputStream = excelFile.getInputStream();
             Workbook workbook = new XSSFWorkbook(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0);
            List<String> columnNames = StreamSupport.stream(sheet.getRow(0).spliterator(), false)
                    .map(Cell::getStringCellValue)
                    .collect(Collectors.toList());

            JSONArray jsonArray = StreamSupport.stream(sheet.spliterator(), false)
                    .skip(1) // Skip the header row
                    .map(row -> {
                        JSONObject jsonObject = new JSONObject();
                        IntStream.range(0, columnNames.size())
                                .forEach(columnIndex -> {
                                    Cell currentCell = row.getCell(columnIndex);
                                    String columnName = columnNames.get(columnIndex);

                                    try {
                                        if (currentCell != null) { // Add this null check
                                            switch (currentCell.getCellType()) {
                                                case STRING:
                                                    jsonObject.put(columnName, currentCell.getStringCellValue());
                                                    break;
                                                case NUMERIC:
                                                    if (DateUtil.isCellDateFormatted(currentCell)) {
                                                        jsonObject.put(columnName, currentCell.getDateCellValue());
                                                    } else {
                                                        jsonObject.put(columnName, currentCell.getNumericCellValue());
                                                    }
                                                    break;
                                                case BOOLEAN:
                                                    jsonObject.put(columnName, currentCell.getBooleanCellValue());
                                                    break;
                                                case BLANK:
                                                    jsonObject.put(columnName, "");
                                                    break;
                                                default:
                                                    jsonObject.put(columnName, "");
                                            }
                                        } else {
                                            jsonObject.put(columnName, ""); // Handle null cells
                                        }
                                    } catch (JSONException e) {
                                        System.out.println("JsonError");
                                        // Handle JSONException if needed
                                    }
                                });
                        return jsonObject;
                    })
                    .collect(Collectors.collectingAndThen(Collectors.toList(), JSONArray::new));

            return jsonArray;
        }
    }

    public JSONArray csvToJson(MultipartFile csvFile, Map<String, Class<?>> columnConfig) throws IOException {
        try (Reader reader = new InputStreamReader(csvFile.getInputStream());
             CSVParser csvParser = CSVFormat.DEFAULT.withHeader().parse(reader)) {

            List<Map<String, Object>> records = csvParser.getRecords().stream()
                    .map(record -> {
                        Map<String, Object> map = new HashMap<>();
                        for (Map.Entry<String, String> entry : record.toMap().entrySet()) {
                            String columnName = entry.getKey();
                            String columnValue = entry.getValue();
                            Class<?> columnType = columnConfig.get(columnName);

                            if (columnType == Date.class) {
                                // Parse dates or times based on the column type
                                SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
                                SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");

                                try {
                                    if (columnName.equals("Start Date") || columnName.equals("End Date")) {
                                        Date date = dateFormat.parse(columnValue);
                                        map.put(columnName, date);
                                    } else if (columnName.equals("Start Time") || columnName.equals("End Time")) {
                                        Date time = timeFormat.parse(columnValue);
                                        map.put(columnName, time);
                                    } else {
                                        // Handle other cases if needed
                                    }
                                } catch (ParseException e) {
                                    e.printStackTrace(); // Handle parsing exceptions
                                    map.put(columnName, null);
                                }
                            } else {
                                // Handle other columns as strings or based on their data types
                                map.put(columnName, columnValue);
                            }
                        }
                        return map;
                    })
                    .collect(Collectors.toList());

            try {
                return new JSONArray(objectMapper.writeValueAsString(records));
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
    }


    public <T> List<T> convertJsonToDtoList(JSONArray jsonArray, Class<T> dtoClass) {
        try {
            System.out.println("JSON data before deserialization: " + jsonArray.toString());
            List<T> dtoList = objectMapper.readValue(jsonArray.toString(), objectMapper.getTypeFactory().constructCollectionType(List.class, dtoClass));
            System.out.println("DTOs after deserialization: " + dtoList);
            return dtoList;
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert JSON to DTO list: " + e.getMessage(), e);
        }
    }

    public Boolean saveDtoListToPostgres(List<ExamUploadData> dtoList, ExamEntityRepository repository) {
        try {
            List<ExamUploadData> entityList = convertExamDtoListToEntities(dtoList);
            repository.saveAll(entityList);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public UploadStatusDetails saveDtoListToPostgres(List<AttendanceRecord> dtoList, AttendanceRepository repository) {
        int total = dtoList.size();
        int uploaded = 0;
        int skipped = 0;

        List<AttendanceRecord> entityList = new ArrayList<>();

        for (AttendanceRecord dto : dtoList) {
            if (isDtoEffectivelyEmpty(dto)) {
                skipped++;
                continue;
            }

            if (!checkIfDataExists(dto)) {
                AttendanceRecord entity = getAttendanceRecord(dto);

                entityList.add(entity);
                uploaded++;
            } else {
                skipped++;
            }
        }

        try {
            repository.saveAll(entityList);
            return new UploadStatusDetails(total, uploaded, skipped, true, null);
        } catch (Exception e) {
            return new UploadStatusDetails(total, uploaded, skipped, false, e.getMessage());
        }
    }

    private static AttendanceRecord getAttendanceRecord(AttendanceRecord dto) {
        AttendanceRecord entity = new AttendanceRecord();

        entity.setFirstName(dto.getFirstName());
        entity.setLastName(dto.getLastName());
        entity.setStudentEnrollmentNumber(dto.getStudentEnrollmentNumber());
        entity.setMothersName(dto.getMothersName());
        entity.setFathersName(dto.getFathersName());
        entity.setCourseName(dto.getCourseName());
        entity.setExamCycleData(dto.getExamCycleData());
        entity.setStartDate(dto.getStartDate());
        entity.setEndDate(dto.getEndDate());
        entity.setNumberOfWorkingDays(dto.getNumberOfWorkingDays());
        entity.setPresentDays(dto.getPresentDays());
        entity.setAbsentDays(dto.getAbsentDays());
        entity.setAttendancePercentage(dto.getAttendancePercentage());
        return entity;
    }

    public boolean convertResultDtoListToEntities(List<StudentResult> dtoList, StudentResultRepository repository) throws ValidationException {
        List<StudentResult> entityList = new ArrayList<>();
        boolean isValid = true;
        List<String> validationErrors = new ArrayList<>();

        for (StudentResult dto : dtoList) {
            boolean isDuplicate = checkIfDataExists(dto);

            if (!isDuplicate) {
                if (!DataValidation.isFirstNameValid(dto.getFirstName())) {
                    validationErrors.add("- First Name is invalid: " + dto.getFirstName());
                }
                if (!DataValidation.isLastNameValid(dto.getLastName())) {
                    validationErrors.add("- Last Name is invalid: " + dto.getLastName());
                }
                if (!DataValidation.isEnrollmentNumberValid(dto.getEnrollmentNumber())) {
                    validationErrors.add("- Enrollment Number is invalid: " + dto.getEnrollmentNumber());
                }
                if (!DataValidation.isMotherNameValid(dto.getMotherName())) {
                    validationErrors.add("- Mother's Name is invalid: " + dto.getMotherName());
                }
                if (!DataValidation.isFatherNameValid(dto.getFatherName())) {
                    validationErrors.add("- Father's Name is invalid: " + dto.getFatherName());
                }
                if (!DataValidation.isCourseNameValid(dto.getCourse_name())) {
                    validationErrors.add("- Course Name is invalid: " + dto.getCourse_name());
                }
                if (!DataValidation.isExamCycleValid(dto.getExamCycle_name())) {
                    validationErrors.add("- Exam Cycle is invalid: " + dto.getExamCycle_name());
                }
                if (!DataValidation.isExamValid(dto.getExam_name())) {
                    validationErrors.add("- Exam is invalid: " + dto.getExam_name());
                }
                if (!DataValidation.isMarksValid(dto.getInternalMarks())) {
                    validationErrors.add("- Internal Marks is invalid: " + dto.getInternalMarks());
                }
                if (!DataValidation.isPassingMarksValid(dto.getPassingInternalMarks())) {
                    validationErrors.add("- Passing Internal Marks is invalid: " + dto.getPassingInternalMarks());
                }
                if (!DataValidation.isMarksValid(dto.getInternalMarksObtained())) {
                    validationErrors.add("- Internal Marks Obtained is invalid: " + dto.getInternalMarksObtained());
                }
                if (!DataValidation.isMarksValid(dto.getPracticalMarks())) {
                    validationErrors.add("- Practical Marks is invalid: " + dto.getPracticalMarks());
                }
                if (!DataValidation.isPassingMarksValid(dto.getPassingPracticalMarks())) {
                    validationErrors.add("- Passing Practical Marks is invalid: " + dto.getPassingPracticalMarks());
                }
                if (!DataValidation.isPassingMarksValid(dto.getPracticalMarksObtained())) {
                    validationErrors.add("- Practical Marks Obtained is invalid: " + dto.getPracticalMarksObtained());
                }
                if (!DataValidation.isMarksValid(dto.getOtherMarks())) {
                    validationErrors.add("- Other Marks is invalid: " + dto.getOtherMarks());
                }
                if (!DataValidation.isPassingMarksValid(dto.getPassingOtherMarks())) {
                    validationErrors.add("- Passing Other Marks is invalid: " + dto.getPassingOtherMarks());
                }
                if (!DataValidation.isMarksValid(dto.getOtherMarksObtained())) {
                    validationErrors.add("- Other Marks Obtained is invalid: " + dto.getOtherMarksObtained());
                }
                if (!DataValidation.isMarksValid(dto.getTotalMarks())) {
                    validationErrors.add("- Total Marks is invalid: " + dto.getTotalMarks());
                }
                if (!DataValidation.isPassingMarksValid(dto.getPassingTotalMarks())) {
                    validationErrors.add("- Passing Total Marks is invalid: " + dto.getPassingTotalMarks());
                }
                if (!DataValidation.isMarksValid(dto.getTotalMarksObtained())) {
                    validationErrors.add("- Total Marks Obtained is invalid: " + dto.getTotalMarksObtained());
                }
                if (!DataValidation.isGradeValid(dto.getGrade())) {
                    validationErrors.add("- Grade is invalid: " + dto.getGrade());
                }
                if (!DataValidation.isResultValid(dto.getResult())) {
                    validationErrors.add("- Result is invalid: " + dto.getResult());
                }

                if (!validationErrors.isEmpty()) {
                    isValid = false;
                } else {
                    StudentResult entity = getStudentResult(dto);
                    entityList.add(entity);
                }

                StudentResult entity = getStudentResult(dto);
                entityList.add(entity);
            }
        }
        if (!validationErrors.isEmpty()) {
            StringBuilder errorMessage = new StringBuilder("Validation failed. The following fields contain invalid values:\n");

            for (String validationError : validationErrors) {
                errorMessage.append(validationError).append("\n");
            }
            throw new ValidationException(isValid, errorMessage.toString());
        }
        repository.saveAll(entityList);

        return isValid;
    }

    private List<ExamUploadData> convertExamDtoListToEntities(List<ExamUploadData> dtoList) {
        List<ExamUploadData> entityList = new ArrayList<>();
        List<String> validationErrors = new ArrayList<>();

        for (ExamUploadData dto : dtoList) {
            boolean isDuplicate = checkIfDataExists(dto);

            if (!isDuplicate) {
                if (!DataValidation.isExamCycleValid(dto.getExamcycleName())) {
                    validationErrors.add("- Exam Cycle Name is invalid: " + dto.getExamcycleName());
                }
                if (!DataValidation.isCourseNameValid(dto.getCourse())) {
                    validationErrors.add("- Course is invalid: " + dto.getCourse());
                }
                if (!DataValidation.isDateValid(dto.getStartDate())) {
                    validationErrors.add("- Start Date is invalid: " + dto.getStartDate());
                }
                if (!DataValidation.isDateValid(dto.getEndDate())) {
                    validationErrors.add("- End Date is invalid: " + dto.getEndDate());
                }
                if (!DataValidation.isExamValid(dto.getExamName())) {
                    validationErrors.add("- Exam Name is invalid: " + dto.getExamName());
                }
                if (!DataValidation.isDateValid(dto.getDate())) {
                    validationErrors.add("- Date is invalid: " + dto.getDate());
                }
//                if (!DataValidation.isTimeFormatValid(String.valueOf(dto.getStartTime()))) {
//                    validationErrors.add("- Start Time is invalid: " + dto.getStartTime());
//                }
//                if (!DataValidation.isTimeFormatValid(String.valueOf(dto.getEndTime()))) {
//                    validationErrors.add("- End Time is invalid: " + dto.getEndTime());
//                }
                if (!DataValidation.isMarksBetweenOneAndHundred(dto.getMaximumMarks())) {
                    validationErrors.add("- Maximum Marks should be between 1 and 100: " + dto.getMaximumMarks());
                }

                if (validationErrors.isEmpty()) {
                    ExamUploadData entity = getExamUploadData(dto);
                    entityList.add(entity);
                } else {
                    throw new IllegalArgumentException("Validation failed. The following fields contain invalid values:\n" + String.join("\n", validationErrors));
                }
            }
        }
        return entityList;
    }


    private static ExamUploadData getExamUploadData(ExamUploadData dto) {
        ExamUploadData entity = new ExamUploadData();

        entity.setExamcycleName(dto.getExamcycleName());
        entity.setCourse(dto.getCourse());
        entity.setStartDate(dto.getStartDate());
        entity.setEndDate(dto.getEndDate());
        entity.setExamName(dto.getExamName());
        entity.setDate(dto.getDate());
        entity.setStartTime(dto.getStartTime());
        entity.setEndTime(dto.getEndTime());
        entity.setMaximumMarks(dto.getMaximumMarks());
        return entity;
    }


    private List<AttendanceRecord> convertAttendanceDtoListToEntities(List<AttendanceRecord> dtoList) {
        List<AttendanceRecord> entityList = new ArrayList<>();

        for (AttendanceRecord dto : dtoList) {
            if (isDtoEffectivelyEmpty(dto)) {
                continue;
            }
            boolean isDuplicate = checkIfDataExists(dto);

            if (!isDuplicate) {
                AttendanceRecord entity = new AttendanceRecord();

                // Validate and set fields
                if (DataValidation.isFirstNameValid(dto.getFirstName())) {
                    entity.setFirstName(dto.getFirstName());
                } else {
                    throw new IllegalArgumentException("First Name is invalid.");
                }

                if (DataValidation.isLastNameValid(dto.getLastName())) {
                    entity.setLastName(dto.getLastName());
                } else {
                    throw new IllegalArgumentException("Last Name is invalid.");
                }

                if (DataValidation.isEnrollmentNumberValid(dto.getStudentEnrollmentNumber())) {
                    entity.setStudentEnrollmentNumber(dto.getStudentEnrollmentNumber());
                } else {
                    throw new IllegalArgumentException("Enrollment Number is invalid.");
                }

                if (DataValidation.isMotherNameValid(dto.getMothersName())) {
                    entity.setMothersName(dto.getMothersName());
                } else {
                    throw new IllegalArgumentException("Mother's Name is invalid.");
                }

                if (DataValidation.isFatherNameValid(dto.getFathersName())) {
                    entity.setFathersName(dto.getFathersName());
                } else {
                    throw new IllegalArgumentException("Father's Name is invalid.");
                }

                if (DataValidation.isCourseNameValid(dto.getCourseName())) {
                    entity.setCourseName(dto.getCourseName());
                } else {
                    throw new IllegalArgumentException("Course Name is invalid.");
                }

                if (DataValidation.isExamCycleValid(dto.getExamCycleData())) {
                    entity.setExamCycleData(dto.getExamCycleData());
                } else {
                    throw new IllegalArgumentException("Exam Cycle Data is invalid.");
                }

                if (DataValidation.isDateValid(dto.getStartDate())) {
                    entity.setStartDate(dto.getStartDate());
                } else {
                    throw new IllegalArgumentException("Start Date is invalid.");
                }

                if (DataValidation.isDateValid(dto.getEndDate())) {
                    entity.setEndDate(dto.getEndDate());
                } else {
                    throw new IllegalArgumentException("End Date is invalid.");
                }

                if (DataValidation.isNumberOfWorkingDaysValid(dto.getNumberOfWorkingDays())) {
                    entity.setNumberOfWorkingDays(dto.getNumberOfWorkingDays());
                } else {
                    throw new IllegalArgumentException("Number of Working Days is invalid.");
                }

                if (DataValidation.isPresentDaysValid(dto.getPresentDays())) {
                    entity.setPresentDays(dto.getPresentDays());
                } else {
                    throw new IllegalArgumentException("Present Days is invalid.");
                }

                if (DataValidation.isAbsentDaysValid(dto.getAbsentDays())) {
                    entity.setAbsentDays(dto.getAbsentDays());
                } else {
                    throw new IllegalArgumentException("Absent Days is invalid.");
                }

                if (DataValidation.isAttendancePercentageValid(dto.getAttendancePercentage())) {
                    entity.setAttendancePercentage(dto.getAttendancePercentage());
                } else {
                    throw new IllegalArgumentException("Attendance Percentage is invalid.");
                }

                entityList.add(entity);
            }
        }

        return entityList;
    }

    private boolean isDtoEffectivelyEmpty(AttendanceRecord dto) {
        return
                // Checking for String fields
                (dto.getFirstName() == null || dto.getFirstName().trim().isEmpty()) &&
                        (dto.getLastName() == null || dto.getLastName().trim().isEmpty()) &&
                        (dto.getStudentEnrollmentNumber() == null || dto.getStudentEnrollmentNumber().trim().isEmpty()) &&
                        (dto.getMothersName() == null || dto.getMothersName().trim().isEmpty()) &&
                        (dto.getFathersName() == null || dto.getFathersName().trim().isEmpty()) &&
                        (dto.getCourseName() == null || dto.getCourseName().trim().isEmpty()) &&
                        (dto.getExamCycleData() == null || dto.getExamCycleData().trim().isEmpty()) &&

                        // Checking for Date or Object fields
                        (dto.getStartDate() == null) &&
                        (dto.getEndDate() == null) &&
                        (dto.getExamCycle() == null) &&   // Assuming `ExamCycle` is an object or date field
                        (dto.getRejectionReason() == null) &&  // Assuming `RejectionReason` is an object or date field

                        // Checking for Enum fields (assuming ApprovalStatus is an Enum)
                        (dto.getApprovalStatus() == ApprovalStatus.PENDING) && // Assuming PENDING is the default status

                        // Checking for numeric fields
                        (dto.getNumberOfWorkingDays() == 0) &&
                        (dto.getPresentDays() == 0) &&
                        (dto.getAbsentDays() == 0) &&
                        (dto.getAttendancePercentage() == 0.0);
    }

    private static StudentResult getStudentResult(StudentResult dto) {
        StudentResult entity = new StudentResult();

        entity.setFirstName(dto.getFirstName());
        entity.setLastName(dto.getLastName());
        entity.setEnrollmentNumber(dto.getEnrollmentNumber());
        entity.setMotherName(dto.getMotherName());
        entity.setFatherName(dto.getFatherName());
        entity.setCourse_name(dto.getCourse_name());
        entity.setExamCycle_name(dto.getExamCycle_name());
        entity.setExam_name(dto.getExam_name());
        entity.setInternalMarks(dto.getInternalMarks());
        entity.setPassingInternalMarks(dto.getPassingInternalMarks());
        entity.setInternalMarksObtained(dto.getInternalMarksObtained());
        entity.setPracticalMarks(dto.getPracticalMarks());
        entity.setPassingPracticalMarks(dto.getPassingPracticalMarks());
        entity.setPracticalMarksObtained(dto.getPracticalMarksObtained());
        entity.setOtherMarks(dto.getOtherMarks());
        entity.setPassingOtherMarks(dto.getPassingOtherMarks());
        entity.setOtherMarksObtained(dto.getOtherMarksObtained());
//        entity.setExternalMarks(dto.getExternalMarks());
//        entity.setPassingExternalMarks(dto.getPassingExternalMarks());
//        entity.setExternalMarksObtained(dto.getExternalMarksObtained());
        entity.setTotalMarks(dto.getTotalMarks());
        entity.setPassingTotalMarks(dto.getPassingTotalMarks());
        entity.setTotalMarksObtained(dto.getTotalMarksObtained());
        entity.setGrade(dto.getGrade());
        entity.setResult(dto.getResult());
        return entity;
    }

    private boolean checkIfDataExists(StudentResult dto) {
        return studentResultRepository.existsByEnrollmentNumber(dto.getEnrollmentNumber());
    }

    private boolean checkIfDataExists(AttendanceRecord dto) {
        return attendanceRepository.existsByStudentEnrollmentNumber(dto.getStudentEnrollmentNumber());
    }

    private boolean checkIfDataExists(ExamUploadData dto) {
        Boolean result = examCycleRepository.findByCourseAndExamcycleName(dto.getCourse(), dto.getExamcycleName());
        return result != null && result; // Return true only if the result is not null and true.
    }

}