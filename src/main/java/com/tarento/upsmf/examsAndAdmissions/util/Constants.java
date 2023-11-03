package com.tarento.upsmf.examsAndAdmissions.util;

import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class Constants {

    // General
    public static final String MESSAGE = "message";
    public static final HttpStatus SUCCESSFUL = HttpStatus.OK;
    public static final String RESPONSE = "response";
    public static final String FAILUREMESSAGE = "failure message";
    public static final String SUCCESSMESSAGE = "Successfully stores the data";
    public static final String LOCAL_BASE_PATH = "/tmp/";
    public static final String FAILED = "Failed";
    public static final String API_USER_BULK_UPLOAD = "api.user.bulk.upload";
    public static final String API_FILE_UPLOAD = "api.file.upload";
    public static final String API_FILE_DOWNLOAD = "api.file.download";
    public static final String API_GET_DISPATCH_LIST = "api.institute.dispatchTracker";
    public static final String NAME = "name";
    public static final String URL = "url";
    public static final String CONTENT_TYPE = "Content-Type";
    public static final String APPLICATION_JSON = "application/json";
    public static final String API_VERSION_1 = "1.0";
    public static final LocalDate LAST_DATE_TO_UPLOAD = LocalDate.of(2023, 9, 25);
    public static final int CLIENT_ERROR = 400;
    public static final int SERVER_ERROR = 500;
    public static final String UNAUTHORIZED = "Unauthorized";


    //Exam
    public static final String API_EXAM_ADD = "api.exam.add";
    public static final String API_EXAM_GET_ALL = "api.exam.get";
    public static final String API_EXAM_GET_BY_ID = "api.exam.getById";
    public static final String API_EXAM_DELETE = "api.exam.delete";
    public static final String API_EXAM_UPDATE = "api.exam.update";
    public static final String API_EXAM_RESTORE = "api.exam.restore";
    public static final String API_EXAM_PUBLISH_RESULTS = "API_EXAM_PUBLISH_RESULTS";
    public static final String API_RESULTS_GET_BY_INSTITUTE_AND_CYCLE = "api.results.getByInstituteAndCycle";
    public static final String API_EXAM_CYCLE_MANAGE_RESULTS = "api.exam.cycle.manage.results";
    public static final String API_SINGLE_EXAM_MARK="api.single.exam.mark";
    public static final String API_EXAMS_FOR_EXAM_CYCLE = "api.exam.for.exam.cycle";
    public static final String API_DELETE_FINAL_MARKS = "api.delete.finalMarks";


    // ExamCycle
    public static final String API_EXAM_CYCLE_ADD = "api.examCycle.add";
    public static final String API_EXAM_CYCLE_GET_ALL = "api.examCycle.getAll";
    public static final String API_EXAM_CYCLE_GET_BY_ID = "api.examCycle.getById";
    public static final String API_EXAM_CYCLE_DELETE = "api.examCycle.delete";
    public static final String API_EXAM_CYCLE_UPDATE = "api.examCycle.update";
    public static final String API_EXAM_CYCLE_RESTORE = "api.examCycle.restore";
    public static final String API_EXAM_CYCLE_ADD_EXAMS = "api.examCycle.addExams";
    public static final String API_EXAM_CYCLE_REMOVE_EXAM = "api.examCycle.removeExam";
    public static final String API_EXAM_CYCLE_PUBLISH = "api.examCycle.publish";
    public static final String API_EXAM_CYCLE_SEARCH = "api.examCycle.search";
    public static final String API_FETCH_EXAM_CYCLE_BY_NAME = "api.fetch.examCycleByName";

    public static final String API_DISPATCH_PROOF_PREVIEW = "api.dispatch.preview";
    public static final String API_DISPATCH_STATUS_BY_EXAM_AND_CENTER = "API_DISPATCH_STATUS_BY_EXAM_AND_CENTER";
    public static final String API_DISPATCH_STATUS_FOR_ALL_INSTITUTES = "API_DISPATCH_STATUS_FOR_ALL_INSTITUTES";

    //Question Paper
    public static final String API_QUESTION_PAPER_GET_ALL = "api.questionPaper.get";
    public static final String API_QUESTION_PAPER_GET_BY_ID = "api.questionPaper.getById";
    public static final String API_QUESTION_PAPER_DELETE = "api.questionPaper.delete";
    public static final String API_QUESTION_PAPER_PREVIEW = "api.questionPaper.preview";
    public static final String API_QUESTION_PAPER_DOWNLOAD = "api.questionPaper.download";
    public static final String API_QUESTION_PAPER_UPLOAD = "api.questionPaper.upload";

    //Subject
    public static final String API_SUBJECT_GET_ALL = "api.subject.get";
    public static final String API_SUBJECT_GET_BY_ID = "api.subject.getById";
    public static final String API_SUBJECT_DELETE = "api.subject.delete";
    public static final String API_SUBJECT_CREATE = "api.subject.create";

    //InstituteCourseMapping
    public static final String API_INSTITUTE_COURSE_MAPPING_GET_ALL = "api.instituteCourseMapping.get";
    public static final String API_INSTITUTE_COURSE_MAPPING_GET_BY_ID = "api.instituteCourseMapping.getById";
    public static final String API_INSTITUTE_COURSE_MAPPING_DELETE = "api.instituteCourseMapping.delete";
    public static final String API_INSTITUTE_COURSE_MAPPING_CREATE = "api.instituteCourseMapping.create";

    //CourseSubjectMapping
    public static final String API_COURSE_SUBJECT_MAPPING_GET_ALL = "api.courseSubjectMapping.get";
    public static final String API_COURSE_SUBJECT_MAPPING_GET_BY_ID = "api.courseSubjectMapping.getById";
    public static final String API_COURSE_SUBJECT_MAPPING_DELETE = "api.courseSubjectMapping.delete";
    public static final String API_COURSE_SUBJECT_MAPPING_CREATE = "api.courseSubjectMapping.create";



    // API Identifiers for StudentService
    public static final String API_ENROLL_STUDENT = "api.student.enroll";
    public static final String API_GET_FILTERED_STUDENTS = "api.student.getFiltered";
    public static final String API_GET_STUDENT_BY_ID = "api.student.getById";
    public static final String API_UPDATE_STUDENT = "api.student.update";
    public static final String API_UPDATE_STUDENT_STATUS_TO_CLOSED = "api.student.updateStatusToClosed";
    public static final String API_GET_STUDENTS_PENDING_FOR_21_DAYS = "api.student.getPendingFor21Days";
    public static final String API_VERIFY_STUDENT = "api.student.verify";
    public static final String API_FIND_BY_VERIFICATION_STATUS = "api.student.findByVerificationStatus";
    public static final String API_FIND_BY_KEYCLOAK_ID = "api.student.findByKeycloakId";
    public static final String API_DELETE_STUDENT = "api.student.delete";



    // API Identifiers for StudentResultService
    public static final String API_IMPORT_INTERNAL_MARKS_FROM_EXCEL = "api.studentResult.importInternalMarks";
    public static final String API_IMPORT_EXTERNAL_MARKS_FROM_EXCEL = "api.studentResult.importExternalMarks";
    public static final String API_GET_STUDENT_RESULT_BY_ID = "api.studentResult.getById";
    public static final String API_GET_ALL_STUDENT_RESULTS = "api.studentResult.getAll";
    public static final String API_FETCH_STUDENT_BY_ENROLLMENT_NUMBER = "api.studentResult.fetchStudentByEnrollment";
    public static final String API_FETCH_COURSE_BY_NAME = "api.studentResult.fetchCourseByName";
    public static final String API_FETCH_EXAM_BY_NAME = "api.studentResult.fetchExamByName";
    public static final String API_PUBLISH_RESULTS_FOR_COURSE_WITHIN_CYCLE = "api.studentResult.publishForCourse";
    public static final String API_FIND_BY_ENROLLMENT_NUMBER_AND_ExamCycle = "api.studentResult.findByEnrollmentAndExamCycle";
    public static final String API_UPDATE_RESULT_AFTER_RETOTALLING = "api.studentResult.updateAfterRetotalling";
    public static final String API_GET_RESULTS_BY_EXAM_CYCLE_AND_EXAM_GROUPED_BY_INSTITUTE = "api.studentResult.getByExamCycleAndGroupedByInstitute";
    public static final String API_BULK_UPLOAD_RESULTS = "api.studentResult.bulkUpload";
    public static final String API_PARSE_EXCEL="api.parse.excel";

    // API Identifiers for ExamCenterService
    public static final String API_GET_VERIFIED_EXAM_CENTERS = "api.examCenter.getVerifiedInDistrict";
    public static final String API_ASSIGN_ALTERNATE_EXAM_CENTER = "api.examCenter.assignAlternate";
    public static final String API_UPDATE_CCTV_STATUS = "api.examCenter.updateCCTVStatus";
    public static final String API_GET_EXAM_CENTERS_BY_STATUS = "api.examCenter.getByStatus";
    public static final String API_GET_ALL_EXAM_CENTERS = "api.examCenter.getAll";
    public static final String API_GET_EXAM_CENTERS_BY_EXAM_CYCLE = "api.examCenter.getByExamCycle";
    public static final String API_GET_VERIFIED_EXAM_CENTER = "api.examcenter.getVerified";
    public static final String API_GET_EXAM_CENTER_STATUS = "API_GET_EXAM_CENTER_STATUS";



    // API Identifiers for RetotallingService
    public static final String API_REQUEST_RETOTALLING = "api.retotalling.requestRetotalling";
    public static final String API_GET_ALL_PENDING_REQUESTS = "api.retotalling.getAllPendingRequests";

    public static final String API_ATTENDANCE_BY_EXAM_CYCLE_ID="api.attendance.getByExamCycleId";

    // API Identifiers for StudentExamRegistrationService
    public static final String API_REGISTER_STUDENTS_FOR_EXAMS = "api.studentExamRegistration.registerStudentsForExams";
    public static final String API_GET_ALL_REGISTRATIONS = "api.studentExamRegistration.getAllRegistrations";
    public static final String API_GET_ALL_REGISTRATIONS_BY_EXAM_CYCLE = "api.studentExamRegistration.getAllRegistrationsByExamCycle";
    public static final String API_VERIFIED_NOT_REGISTERED = "API_VERIFIED_NOT_REGISTERED";

    public static final String API_DISPATCH_GET_FOR_ADMIN = "api.dispatch.get.for.admin";
    public static final String API_DISPATCH_GET_FOR_INSTITUTE = "api.dispatch.get.for.institute";
    // Payment
    public static final String API_PAYMENT_ADD = "api.payment.add";
    public static final String EXAM_CYCLE_ID = "examCycleId";
    public static final String EXAM_DATE = "examDate";
    public static final String COURSE_ID = "course_id";
    public static final String MODIFIED_BY = "modifiedBy";
    public static final String DATE_MODIFIED_ON = "modifiedOn";
    public static final String TOTAL_MARKS = "totalMarks";
    public static final String EXAM_CYCLE_NAME = "examCycleName";
    public static final String COURSE_NAME = "courseName";
    public static final String COURSE_CODE = "courseCode";
    public static final String DESCRIPTION = "description";
    public static final String EXAM_NAME = "examName";
    public static final String ID = "id";
    public static final String EXAM_START_TIME = "examStartTime";
    public static final HttpStatus NOT_FOUND = HttpStatus.NOT_FOUND;
    public static final String API_EXAM_FIND_BY_CYCLE = "Find Exams by ExamCycle ID";
  

    public static final String INTERNAL_SERVER_ERROR = "internal server error";
    public static final String API_UPLOAD_DISPATCH_DETAILS = "api.upload.dispatchDetails";
  
    public static final String API_HALLTICKET_GET = "api.hallticket.get";
    public static final String API_HALLTICKET_GET_BY_ID = "api.hallticket.get.id";
    public static final String API_HALLTICKET_REQUEST_DATA_CORRECTION = "api.hallticket.requestDataCorrection";
    public static final String API_HALLTICKET_GET_ALL_DATA_CORRECTION_REQUESTS = "api.hallticket.getAllDataCorrectionRequests";
    public static final String API_HALLTICKET_APPROVE_DATA_CORRECTION = "api.hallticket.approveDataCorrection";
    public static final String API_HALLTICKET_REJECT_DATA_CORRECTION = "api.hallticket.rejectDataCorrection";
    public static final String API_HALLTICKET_GET_PENDING_DATA = "api.hallticket.getPendingData";
    public static final String API_HALLTICKET_DOWNLOAD_PROOF = "api.hallticket.downloadProof";
    public static final String API_HALLTICKET_GET_PROOF_URL_BY_REQUEST = "api.hallticket.getProofUrl";
    public static final String API_GENERATE_AND_SAVE_HALL_TICKETS_FOR_MULTIPLE_STUDENTS = "api.hallticket.generateAndSaveForMultipleStudents";
    public static final String API_HALLTICKET_GET_DETAILS_BY_STUDENT_AND_EXAM_CYCLE = "api.hallticket.getDetailsByStudentAndExamCycle";


    public static final String API_EXAMCYCLE_BULK_UPLOAD = "api.examCycle.bulkUpload";
    public static final String API_MARK_NOT_ALLOWED ="api.institute.markNotAllowed";
    public static final String API_UPDATE_VERIFICATION_STATUS ="api.institute.updateVerificationStatus";
    public static final String API_CREATE_INSTITUTE = "api.institute.create";
    public static final String API_GET_INSTITUTE_BY_ID = "api.institute.getById";
    public static final String API_UPDATE_INSTITUTE = "api.institute.update";
    public static final String SUCCESS = "success";
    public static final HttpStatus ERROR = HttpStatus.INTERNAL_SERVER_ERROR;
    public static final String INVALID_REQUEST_ERROR_MESSAGE = "Invalid Request.";
    public static final String MISSING_SEARCH_PARAM_COURSE_ID = "Missing Search Param Course ID.";
    public static final String MISSING_SEARCH_PARAM_START_ACADEMIC_YEAR = "Missing Search Param Start Academic Year.";
    public static final String MISSING_SEARCH_PARAM_END_ACADEMIC_YEAR = "Missing Search Param End Academic Year.";

    public interface Exception {
        String EXCEPTION_METHOD = "Exception in method %s : %s";
        String GET_NODE_ERROR = "Unable to get the node";
    }

    public static class ResponseCodes {
        private ResponseCodes() {
        }

        public static final int UNAUTHORIZED_ID = 401;
        public static final int SUCCESS_ID = 200;
        public static final int FAILURE_ID = 320;
        public static final String UNAUTHORIZED = "Invalid credentials. Please try again.";
        public static final String PROCESS_FAIL = "Process failed, Please try again.";
        public static final String SUCCESS = "success";
    }

    public static class ServiceRepositories {
        private ServiceRepositories() {
        }

        public static final String NOTIFICATION_UTIL = "notificationUtil";
    }

    public interface Parameters {
        String ID = "id";
        String AUTHORIZATION = "Authorization";
        String X_USER_TOKEN = "x-authenticated-user-token";
        String RESPONSE = "response";
        String IS_DETAIL = "isDetail";
        String PARENT_ID = "parentId";
        String CHILD = "child";
        String NAME = "name";
        String DESCRIPTION = "description";
        String STATUS = "status";
        String SOURCE = "source";
        String LEVEL = "level";
        String DATA = "data";
        String RESULT = "result";
        String ROOT_ORG = "rootOrg";
        String ORG = "org";
        String USER_ID = "userId";

        String COMPETENCY = "COMPETENCY";
        String KNOWLEDGERESOURCE = "KNOWLEDGERESOURCE";
        String COMPETENCIESLEVEL = "COMPETENCIESLEVEL";
        String COMPETENCYAREA = "COMPETENCYAREA";

        String ANONYMOUS = "Anonymous";
        String UNAUTHORIZED = "Unauthorized";
        String DOT_SEPARATOR = ".";
        String SHA_256_WITH_RSA = "SHA256withRSA";
        String SUB = "sub";
        String ISS = "iss";
        String EXP = "exp";
        String KID = "kid";
    }

    public interface Actions {
        String CREATE = "CREATED";
        String UPDATE = "UPDATED";
        String REMOVE = "REMOVED";
    }

    public interface WorkflowState {
        String SERVICE = "entity";
        String INITIATE = "INITIATE";
        String UNVERIFIED = "UNVERIFIED";
        String DRAFT = "DRAFT";
    }

    public static final String IDENTIFIER = "identifier";
    public static final String FILE_NAME = "fileName";
    public static final String GCP_FILE_NAME = "gcpFileName";
    public static final String GCP_FILE_NAME_QUERY = "SELECT gcp_file_name FROM public.question_paper WHERE id=?;";
    public static final String FILE_PATH = "filePath";
    public static final String DATE_CREATED_ON = "dateCreatedOn";
    public static final String DATE_UPDATE_ON = "dateUpdatedOn";
    public static final String INITIATED_CAPITAL = "INITIATED";
    public static final String STATUS = "status";
    public static final String COMMENT = "comment";
    public static final String CREATED_BY = "createdBy";

    public static final String EMAIL = "email";

    public static final String FIRST_NAME = "first_name";

    public static final String UUID = "wid";

    public static final String ROOT_ORG_CONSTANT = "rootOrg";

    public static final List<String> USER_DEFAULT_FIELDS = Collections
            .unmodifiableList(Arrays.asList(UUID, FIRST_NAME, EMAIL));

    public static final String CSV = "csv";
    public static final String EXCEL = "excel";
}