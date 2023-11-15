package com.tarento.upsmf.examsAndAdmissions.service;

import com.tarento.upsmf.examsAndAdmissions.enums.ApprovalStatus;
import com.tarento.upsmf.examsAndAdmissions.model.AttendanceRecord;
import com.tarento.upsmf.examsAndAdmissions.model.ExamCycle;
import com.tarento.upsmf.examsAndAdmissions.model.ResponseDto;
import com.tarento.upsmf.examsAndAdmissions.model.UploadStatusDetails;
import com.tarento.upsmf.examsAndAdmissions.model.dto.AttendanceRecordDto;
import com.tarento.upsmf.examsAndAdmissions.repository.AttendanceRepository;
import com.tarento.upsmf.examsAndAdmissions.repository.ExamCycleRepository;
import com.tarento.upsmf.examsAndAdmissions.util.Constants;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.codehaus.jettison.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AttendanceService {

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private ExamCycleRepository examCycleRepository;
    @Autowired
    private DataImporterService dataImporterService;
    Map<String, Class<?>> columnConfig = Map.of(
            "Start Date", Date.class,
            "End Date", Date.class,
            "Start Time", Date.class,
            "End Time", Date.class
            // Add other columns and their data types as needed
    );

    public ResponseDto uploadAttendanceRecords(MultipartFile file) throws IOException {
        ResponseDto response = new ResponseDto("API_UPLOAD_ATTENDANCE");
        try {
        XSSFWorkbook workbook = new XSSFWorkbook(file.getInputStream());
        XSSFSheet sheet = workbook.getSheetAt(0);
        List<AttendanceRecord> records = new ArrayList<>();

        for (int i = 1; i < sheet.getPhysicalNumberOfRows(); i++) {
            XSSFRow row = sheet.getRow(i);

            if (isRowEmpty(row)) {
                continue;  // Skip this iteration if the row is empty
            }
            String studentEnrollmentNumber = getStringValue(row.getCell(2));
            String examCycleId = getStringValue(row.getCell(6));
            AttendanceRecord record;

            // If record already exists, fetch it, otherwise create a new one
            if (attendanceRepository.existsByStudentEnrollmentNumberAndExamCycleData(studentEnrollmentNumber,examCycleId)) {
                record = attendanceRepository.findByStudentEnrollmentNumber(studentEnrollmentNumber);
            } else {
                record = new AttendanceRecord();
                record.setStudentEnrollmentNumber(studentEnrollmentNumber);
            }
            record.setFirstName(getStringValue(row.getCell(0)));
            record.setLastName(getStringValue(row.getCell(1)));
            //record.setStudentEnrollmentNumber(getStringValue(row.getCell(2)));
            record.setMothersName(getStringValue(row.getCell(3)));
            record.setFathersName(getStringValue(row.getCell(4)));
            record.setCourseName(getStringValue(row.getCell(5)));

            // Fetch the ExamCycle from the database
            String examCycleName = getStringValue(row.getCell(6));
            ExamCycle examCycle = examCycleRepository.findByExamCycleName(examCycleName);
            record.setExamCycle(examCycle);

            record.setStartDate(getDateValue(row.getCell(7)));
            record.setEndDate(getDateValue(row.getCell(8)));
            record.setNumberOfWorkingDays(getIntValue(row.getCell(9)));
            record.setPresentDays(getIntValue(row.getCell(10)));
            record.setAbsentDays(getIntValue(row.getCell(11)));
            //record.setAttendancePercentage(getDoubleValue(row.getCell(12)));

            records.add(record);
        }

        attendanceRepository.saveAll(records);
        workbook.close();
        response.put(Constants.MESSAGE, "File uploaded and processed successfully.");
        response.setResponseCode(HttpStatus.OK);

    } catch (IOException e) {
        ResponseDto.setErrorResponse(response, "FILE_PROCESSING_FAILED", "Failed to process the file: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
    return response;
    }
    public static boolean isRowEmpty(XSSFRow row) {
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

    private Date getDateValue(XSSFCell cell) {
        if (cell == null) return null;

        switch (cell.getCellType()) {
            case STRING:
                try {
                    // Convert the string to a date using a simple format, adjust format as needed
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd"); // adjust if necessary
                    return sdf.parse(cell.getStringCellValue());
                } catch (ParseException e) {
                    // Log error or handle exception as necessary
                    return null;
                }
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue();
                }
                return null;
            default:
                return null;
        }
    }

    private String getStringValue(XSSFCell cell) {
        if (cell.getCellType() == CellType.STRING) {
            return cell.getStringCellValue();
        } else if (cell.getCellType() == CellType.NUMERIC) {
            return String.valueOf((int) cell.getNumericCellValue());
        }
        return "";
    }

    private int getIntValue(XSSFCell cell) {
        if (cell.getCellType() == CellType.NUMERIC) {
            return (int) cell.getNumericCellValue();
        }
        return 0;
    }

    private double getDoubleValue(XSSFCell cell) {
        if (cell.getCellType() == CellType.NUMERIC) {
            return cell.getNumericCellValue();
        }
        return 0.0;
    }
    public ResponseDto getAllAttendanceRecords() {
        ResponseDto response = new ResponseDto("API_GET_ALL_ATTENDANCE");
        List<AttendanceRecord> records = attendanceRepository.findAll();

        if (records.isEmpty()) {
            ResponseDto.setErrorResponse(response, "NO_RECORDS_FOUND", "No attendance records found", HttpStatus.NOT_FOUND);
        } else {
            response.put(Constants.RESPONSE, records);
            response.setResponseCode(HttpStatus.OK);
        }
        return response;
    }

    public ResponseDto getRecordById(Long id) {
        ResponseDto response = new ResponseDto("API_GET_RECORD_BY_ID");
        AttendanceRecord record = attendanceRepository.findById(id).orElse(null);

        if (record == null) {
            ResponseDto.setErrorResponse(response, "RECORD_NOT_FOUND", "Attendance record not found", HttpStatus.NOT_FOUND);
        } else {
            response.put(Constants.RESPONSE, record);
            response.setResponseCode(HttpStatus.OK);
        }
        return response;
    }

    public ResponseDto saveRecord(AttendanceRecord record) {
        ResponseDto response = new ResponseDto("API_SAVE_RECORD");
        try {
            AttendanceRecord savedRecord = attendanceRepository.save(record);
            response.put(Constants.RESPONSE, savedRecord);
            response.setResponseCode(HttpStatus.OK);
        } catch (Exception e) {
            ResponseDto.setErrorResponse(response, "SAVE_FAILED", "Failed to save the attendance record: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return response;
    }
    public ResponseDto approveStudent(Long id) {
        ResponseDto response = new ResponseDto("API_APPROVE_STUDENT");
        AttendanceRecord record = attendanceRepository.findById(id).orElse(null);

        if (record == null) {
            ResponseDto.setErrorResponse(response, "RECORD_NOT_FOUND", "Attendance record not found", HttpStatus.NOT_FOUND);
        } else {
            record.setApprovalStatus(ApprovalStatus.APPROVED);
            attendanceRepository.save(record);
            response.put(Constants.MESSAGE, "Student approved successfully.");
            response.setResponseCode(HttpStatus.OK);
        }

        return response;
    }

    public ResponseDto rejectStudent(Long id, String reason) {
        ResponseDto response = new ResponseDto("API_REJECT_STUDENT");
        AttendanceRecord record = attendanceRepository.findById(id).orElse(null);

        if (record == null) {
            ResponseDto.setErrorResponse(response, "RECORD_NOT_FOUND", "Attendance record not found", HttpStatus.NOT_FOUND);
        } else {
            record.setApprovalStatus(ApprovalStatus.REJECTED);
            record.setRejectionReason(reason);
            attendanceRepository.save(record);
            response.put(Constants.MESSAGE, "Student rejected successfully with reason: " + reason);
            response.setResponseCode(HttpStatus.OK);
        }

        return response;
    }

    public ResponseDto processBulkAttendanceUpload(MultipartFile file, String fileType) {
        ResponseDto response = new ResponseDto("API_BULK_UPLOAD_ATTENDANCE");

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
                    return ResponseDto.setErrorResponse(response, "UNSUPPORTED_FILE_TYPE", "Unsupported file type", HttpStatus.BAD_REQUEST);
            }

            List<AttendanceRecord> dtoList = dataImporterService.convertJsonToDtoList(jsonArray, AttendanceRecord.class);
            List<AttendanceRecord> savedEntities = dataImporterService.saveDtoListToPostgres(dtoList, attendanceRepository);

            if (!savedEntities.isEmpty()) {
                String message = String.format("Bulk attendance upload: %d records saved.", savedEntities.size());
                response.put(Constants.MESSAGE, message);
                response.put("savedRecords", savedEntities); // Add the saved records to the response
                response.setResponseCode(HttpStatus.OK);
            } else {
                return ResponseDto.setErrorResponse(response, "FILE_PROCESSING_FAILED", "No records were saved.", HttpStatus.INTERNAL_SERVER_ERROR);
            }

        } catch (Exception e) {
            return ResponseDto.setErrorResponse(response, "INTERNAL_ERROR", "An unexpected error occurred: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return response;
    }
    public ResponseDto getByExamCycleId(Long ExamCycleId) {
        ResponseDto response = new ResponseDto(Constants.API_ATTENDANCE_BY_EXAM_CYCLE_ID);
        String examCycleName = examCycleRepository.getExamCycleNameById(ExamCycleId);
        List<AttendanceRecord> records = attendanceRepository.findByExamCycleData(examCycleName);
        if (records.isEmpty()) {
            ResponseDto.setErrorResponse(response, "NO_RECORDS_FOUND", "No attendance records found", HttpStatus.NOT_FOUND);
        } else {
            List<AttendanceRecordDto> dtoList = records.stream()
                    .map(this::toDto)
                    .collect(Collectors.toList());
            response.put(Constants.RESPONSE, dtoList);
            response.setResponseCode(HttpStatus.OK);
        }
        return response;
    }

    private AttendanceRecordDto toDto(AttendanceRecord record) {
        AttendanceRecordDto dto = new AttendanceRecordDto();
        dto.setId(record.getId());
        dto.setFirstName(record.getFirstName());
        dto.setLastName(record.getLastName());
        dto.setStudentEnrollmentNumber(record.getStudentEnrollmentNumber());
        dto.setMothersName(record.getMothersName());
        dto.setFathersName(record.getFathersName());
        dto.setCourseName(record.getCourseName());
        dto.setExamCycleData(record.getExamCycleData());
        dto.setStartDate(record.getStartDate());
        dto.setEndDate(record.getEndDate());
        dto.setRejectionReason(record.getRejectionReason());
        dto.setApprovalStatus(record.getApprovalStatus());
        dto.setNumberOfWorkingDays(record.getNumberOfWorkingDays());
        dto.setPresentDays(record.getPresentDays());
        dto.setAbsentDays(record.getAbsentDays());
        dto.setAttendancePercentage(record.getAttendancePercentage());
        return dto;
    }

}
