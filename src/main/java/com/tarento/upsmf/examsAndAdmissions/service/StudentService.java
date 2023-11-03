package com.tarento.upsmf.examsAndAdmissions.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.storage.*;
import com.tarento.upsmf.examsAndAdmissions.controller.UserController;
import com.tarento.upsmf.examsAndAdmissions.enums.VerificationStatus;
import com.tarento.upsmf.examsAndAdmissions.model.*;
import com.tarento.upsmf.examsAndAdmissions.model.dto.*;
import com.tarento.upsmf.examsAndAdmissions.repository.CourseRepository;
import com.tarento.upsmf.examsAndAdmissions.repository.InstituteRepository;
import com.tarento.upsmf.examsAndAdmissions.repository.StudentRepository;
import com.tarento.upsmf.examsAndAdmissions.util.Constants;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.*;

@Service
@PropertySource("classpath:application.properties")
@Slf4j
public class StudentService {
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    private final InstituteRepository instituteRepository;
    private final ModelMapper modelMapper;
    @Autowired
    private EntityManager entityManager;

    @Autowired
    private IntegrationService integrationService;

    @Autowired
    private ObjectMapper mapper;

    @Value("${gcp.config.file.path}")
    private String gcpConfigFilePath;

    @Value("${gcp.bucket.name}")
    private String gcpBucketName;

    @Value("${gcp.bucket.folder.name}")
    private String gcpFolderName;

    @Value("${gcp.max.file.size}")
    private String gcpMaxFileSize;

    @Value("${gcp.project.id}")
    private String gcpProjectId;

    @Value("${gcp.client.id}")
    private String gcpClientId;

    @Value("${gcp.client.email}")
    private String gcpClientEmail;

    @Value("${gcp.pkcs.key}")
    private String gcpPkcsKey;

    @Value("${gcp.private.key.id}")
    private String gcpPrivateKeyId;

    @Value("${gcp.sub.folder.path}")
    private String subFolderPath;

    @Value("${file.storage.path}")
    private String storagePath;

    @Autowired
    public StudentService(StudentRepository studentRepository, CourseRepository courseRepository, InstituteRepository instituteRepository) {
        this.studentRepository = studentRepository;
        this.modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setAmbiguityIgnored(true);
        this.courseRepository = courseRepository;
        this.instituteRepository = instituteRepository;
        configureModelMapper();
    }

    private void configureModelMapper() {
        modelMapper.typeMap(StudentDto.class, Student.class).addMappings(mapper -> {
            mapper.skip(Student::setId);
        });
    }

    String storeFile(MultipartFile file) throws IOException {
        Path filePath = null;
        String fileLocation = null;
        try {
            // validate file
            String fileName = file.getOriginalFilename();
            filePath = Files.createTempFile(fileName.split("\\.")[0], fileName.split("\\.")[1]);
            file.transferTo(filePath);
            validateFile(filePath);
            // create credentials
            ServiceAccountCredentials credentials = ServiceAccountCredentials.fromPkcs8(gcpClientId, gcpClientEmail,
                    gcpPkcsKey, gcpPrivateKeyId, new ArrayList<String>());
            log.info("credentials created");
            Storage storage = StorageOptions.newBuilder().setProjectId(gcpProjectId).setCredentials(credentials).build().getService();
            log.info("storage object created");
            String gcpFileName = gcpFolderName + "/" + Calendar.getInstance().getTimeInMillis() + "_" + fileName;
            BlobId blobId = BlobId.of(gcpBucketName, gcpFileName);
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();
            Blob blob = storage.create(blobInfo, new FileInputStream(filePath.toFile()));
            fileLocation = blob.getMediaLink();
        } catch (IOException e) {
            log.error("Error while uploading attachment", e);
        } finally {
            if (filePath != null) {
                try {
                    Files.delete(filePath);
                } catch (IOException e) {
                    log.error("Unable to delete temp file", e);
                }
            }
        }
        return fileLocation;
        /*String filename = UUID.randomUUID().toString() + "_" + System.currentTimeMillis() + "_" + file.getOriginalFilename();
        Path path = Paths.get(storagePath, filename);
        Files.copy(file.getInputStream(), path);
        return path.toString();*/
    }

    @Transactional
    public ResponseDto enrollStudent(StudentDto studentDto) {
        ResponseDto response = new ResponseDto(Constants.API_ENROLL_STUDENT); // Using a constant identifier
        try {
            Student student = modelMapper.map(studentDto, Student.class);

            // Fetching the institute and setting it to the student
            Institute institute = instituteRepository.findByInstituteCode(studentDto.getInstituteCode());
            if (institute == null) {
                ResponseDto.setErrorResponse(response, "INSTITUTE_NOT_FOUND", "Institute with code " + studentDto.getInstituteCode() + " not found in the database", HttpStatus.NOT_FOUND);
                return response;
            }
            student.setInstitute(institute);

            Course dbCourse = courseRepository.findByCourseCode(studentDto.getCourseCode());
            if (dbCourse == null) {
                ResponseDto.setErrorResponse(response, "COURSE_NOT_FOUND", "Course with code " + studentDto.getCourseCode() + " not found in the database", HttpStatus.NOT_FOUND);
                return response;
            }
            student.setCourse(dbCourse);

            // Generate provisional enrollment number
            String provisionalNumber = generateProvisionalNumber(student);
            student.setProvisionalEnrollmentNumber(provisionalNumber);

            // Set initial verification status to PENDING
            student.setHighSchoolMarksheetPath(storeFile(studentDto.getHighSchoolMarksheet()));
            student.setHighSchoolCertificatePath(storeFile(studentDto.getHighSchoolCertificate()));
            student.setIntermediateMarksheetPath(storeFile(studentDto.getIntermediateMarksheet()));
            student.setIntermediateCertificatePath(storeFile(studentDto.getIntermediateCertificate()));
            student.setVerificationStatus(VerificationStatus.PENDING);
            student.setVerificationDate(LocalDate.now());

            student = studentRepository.save(student);

            response.put(Constants.MESSAGE, "Student enrolled successfully");
            response.put(Constants.RESPONSE, student);  // Optionally return the student data
            response.setResponseCode(HttpStatus.OK);
        } catch (IOException e) {
            ResponseDto.setErrorResponse(response, "FILE_STORAGE_ERROR", "Error occurred while storing files: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            ResponseDto.setErrorResponse(response, "GENERAL_ERROR", "An unexpected error occurred: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return response;
    }

    private String generateProvisionalNumber(Student student) {
        return student.getCourse().getCourseCode() + "-" + UUID.randomUUID().toString();
    }

    public ResponseDto getFilteredStudents(Long instituteId, Long courseId, String session, VerificationStatus verificationStatus) {
        ResponseDto response = new ResponseDto(Constants.API_GET_FILTERED_STUDENTS);

        try {
            CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
            CriteriaQuery<Student> criteriaQuery = criteriaBuilder.createQuery(Student.class);
            Root<Student> studentRoot = criteriaQuery.from(Student.class);

            List<Predicate> predicates = new ArrayList<>();

            if (instituteId != null) {
                predicates.add(criteriaBuilder.equal(studentRoot.get("institute").get("id"), instituteId));
            }
            if (courseId != null) {
                predicates.add(criteriaBuilder.equal(studentRoot.get("course").get("id"), courseId));
            }
            if (session != null && !session.trim().isEmpty()) {
                predicates.add(criteriaBuilder.equal(studentRoot.get("session"), session));
            } else if (session != null) {
                predicates.add(criteriaBuilder.isNull(studentRoot.get("session")));
            }
            if (verificationStatus != null) {
                predicates.add(criteriaBuilder.equal(studentRoot.get("verificationStatus"), verificationStatus));
            }

            criteriaQuery.where(predicates.toArray(new Predicate[0]));

            List<Student> students = entityManager.createQuery(criteriaQuery).getResultList();

            if (students.isEmpty()) {
                ResponseDto.setErrorResponse(response, "NO_STUDENTS_FOUND", "No students found with the given criteria.", HttpStatus.NOT_FOUND);
            } else {
                response.put(Constants.MESSAGE, "Students fetched successfully.");
                response.put(Constants.RESPONSE, students);
                response.setResponseCode(HttpStatus.OK);
            }

        } catch (Exception e) {
            ResponseDto.setErrorResponse(response, "GENERAL_ERROR", "An unexpected error occurred: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return response;
    }

    public ResponseDto getStudentById(Long id) {
        ResponseDto response = new ResponseDto(Constants.API_GET_STUDENT_BY_ID);

        try {
            Optional<Student> studentOptional = studentRepository.findById(id);

            if (studentOptional.isPresent()) {
                Student student = studentOptional.get();
                if (student.getInstitute() != null) {
                    student.setInstituteDTO(InstituteDTO.convertToDTO(student.getInstitute()));
                }

                // Ensure you don't send the actual Institute entity in the response.
                student.setInstitute(null);

                response.put(Constants.MESSAGE, "Student fetched successfully.");
                response.put(Constants.RESPONSE, student);
                response.setResponseCode(HttpStatus.OK);
            } else {
                ResponseDto.setErrorResponse(response, "STUDENT_NOT_FOUND", "Student not found with the given ID.", HttpStatus.NOT_FOUND);
            }

        } catch (Exception e) {
            ResponseDto.setErrorResponse(response, "GENERAL_ERROR", "An unexpected error occurred: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return response;
    }

    @Transactional
    public ResponseDto updateStudent(Long id, StudentDto studentDto) {
        ResponseDto response = new ResponseDto(Constants.API_UPDATE_STUDENT);

        try {
            Student existingStudent = studentRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Student not found for ID: " + id));

            if (studentDto.getHighSchoolMarksheet() != null) {
                deleteStudentDocument(existingStudent.getHighSchoolMarksheetPath());
                existingStudent.setHighSchoolMarksheetPath(storeFile(studentDto.getHighSchoolMarksheet()));
            }

            if (studentDto.getHighSchoolCertificate() != null) {
                deleteStudentDocument(existingStudent.getHighSchoolCertificatePath());
                existingStudent.setHighSchoolCertificatePath(storeFile(studentDto.getHighSchoolCertificate()));
            }

            if (studentDto.getIntermediateMarksheet() != null) {
                deleteStudentDocument(existingStudent.getIntermediateMarksheetPath());
                existingStudent.setIntermediateMarksheetPath(storeFile(studentDto.getIntermediateMarksheet()));
            }

            if (studentDto.getIntermediateCertificate() != null) {
                deleteStudentDocument(existingStudent.getIntermediateCertificatePath());
                existingStudent.setIntermediateCertificatePath(storeFile(studentDto.getIntermediateCertificate()));
            }

            modelMapper.map(studentDto, existingStudent);
            existingStudent.setVerificationDate(LocalDate.now());
            existingStudent.setVerificationStatus(VerificationStatus.PENDING);
            Student savedStudent = studentRepository.save(existingStudent);

            response.put(Constants.MESSAGE, "Student updated successfully");
            response.put(Constants.RESPONSE, savedStudent);  // Optionally return the updated student data
            response.setResponseCode(HttpStatus.OK);

        } catch (IOException e) {
            ResponseDto.setErrorResponse(response, "FILE_STORAGE_ERROR", "Error occurred while storing files: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            ResponseDto.setErrorResponse(response, "GENERAL_ERROR", "An unexpected error occurred: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return response;
    }

    @Transactional
    public ResponseDto updateStudentStatusToClosed() {
        ResponseDto response = new ResponseDto(Constants.API_UPDATE_STUDENT_STATUS_TO_CLOSED);

        try {
            LocalDate cutoffDate = LocalDate.now().minusDays(14);
            List<Student> rejectedStudents = studentRepository.findByVerificationDateBeforeAndVerificationStatus(cutoffDate, VerificationStatus.REJECTED);

            log.info("Rejected students found to potentially close: " + rejectedStudents.size());

            List<Student> studentsToUpdate = new ArrayList<>();

            for (Student student : rejectedStudents) {
                student.setVerificationStatus(VerificationStatus.CLOSED);
                studentsToUpdate.add(student);
            }

            List<Student> updatedStudents = studentRepository.saveAll(studentsToUpdate);

            response.put(Constants.MESSAGE, "Students' status updated to CLOSED successfully");
            response.put(Constants.RESPONSE, updatedStudents);  // Optionally return the updated student data
            response.setResponseCode(HttpStatus.OK);

        } catch (Exception e) {
            ResponseDto.setErrorResponse(response, "GENERAL_ERROR", "An unexpected error occurred: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return response;
    }

    public ResponseDto getStudentsPendingForMoreThan21Days(Long courseId, String session) {
        ResponseDto response = new ResponseDto(Constants.API_GET_STUDENTS_PENDING_FOR_21_DAYS);

        try {
            LocalDate twentyOneDaysAgo = LocalDate.now().minusDays(21);
            CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
            CriteriaQuery<Student> criteriaQuery = criteriaBuilder.createQuery(Student.class);
            Root<Student> studentRoot = criteriaQuery.from(Student.class);

            List<Predicate> predicates = new ArrayList<>();

            // Adding the condition for students pending for more than 21 days
            predicates.add(criteriaBuilder.lessThanOrEqualTo(studentRoot.get("enrollmentDate"), twentyOneDaysAgo));
            predicates.add(criteriaBuilder.equal(studentRoot.get("verificationStatus"), VerificationStatus.PENDING));

            if (courseId != null) {
                predicates.add(criteriaBuilder.equal(studentRoot.get("course").get("id"), courseId));
            }
            if (session != null && !session.trim().isEmpty()) {
                predicates.add(criteriaBuilder.equal(studentRoot.get("session"), session));
            } else if (session != null) {
                predicates.add(criteriaBuilder.isNull(studentRoot.get("session")));
            }

            criteriaQuery.where(predicates.toArray(new Predicate[0]));

            List<Student> students = entityManager.createQuery(criteriaQuery).getResultList();

            if (students.isEmpty()) {
//                response.put(Constants.MESSAGE, "No students found with the given criteria.");
                 return ResponseDto.setErrorResponse(response, "NO_PENDING_STUDENTS", "No students found with the given criteria.", HttpStatus.NOT_FOUND);
            } else {
                response.put(Constants.MESSAGE, "Students fetched successfully.");
                response.put(Constants.RESPONSE, students);
                response.setResponseCode(HttpStatus.OK);
            }
        } catch (Exception e) {
            return ResponseDto.setErrorResponse(response, "GENERAL_ERROR", "An unexpected error occurred: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return response;
    }

    public Student findById(Long id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Student with ID " + id + " not found"));
    }

    public Student updateVerificationStatus(Student student, VerificationStatus status) {
        student.setVerificationStatus(status);
        return studentRepository.save(student);
    }

    private String createStudentLoginInKeycloak(Student student) throws Exception {
        if (student.getEmailId() == null || student.getEmailId().isEmpty()) {
            throw new RuntimeException("Email id is mandatory");
        }
        Map<String, String> attributes = new HashMap<>();
        attributes.put("module", "exam");
        attributes.put("departmentName", String.valueOf(-1));
        attributes.put("phoneNumber", student.getMobileNo());
        attributes.put("Role", "exams_student");
        attributes.put("studentId", String.valueOf(student.getId()));

        CreateUserDto createUserDto = CreateUserDto.builder()
                .firstName(student.getFirstName())
                .lastName(student.getSurname())
                .email(student.getEmailId())
                .username(student.getEmailId())
                .attributes(attributes)
                .build();

        ResponseEntity<User> response = integrationService.createUser(createUserDto);
        log.info("Create user Response during verify - {}", response);
        if (response.getStatusCode() == HttpStatus.OK) {
            User userContent = response.getBody();
            return userContent.getId();
        }
        throw new RuntimeException("Exception occurred during creating user in keycloak");
    }

    public ResponseDto verifyStudent(Long studentId, VerificationStatus status, String remarks, String verifierUserId) {
        ResponseDto response = new ResponseDto(Constants.API_VERIFY_STUDENT);

        try {
            Student student = this.findById(studentId);

            if (student == null) {
                ResponseDto.setErrorResponse(response, "STUDENT_NOT_FOUND", "Student not found for ID: " + studentId, HttpStatus.NOT_FOUND);
                return response;
            }

            student.setVerificationStatus(status);
            student.setAdminRemarks(remarks);
            student.setVerificationDate(LocalDate.now());
            student.setVerifiedBy(verifierUserId);

            if (status == VerificationStatus.VERIFIED) {
                String enrollmentNumber = "EN" + LocalDate.now().getYear() + student.getInstitute().getId() + student.getId();
                student.setEnrollmentNumber(enrollmentNumber);
                String keycloakId = createStudentLoginInKeycloak(student);
                student.setKeycloakId(keycloakId);
            } else if (status == VerificationStatus.REJECTED) {
                student.setRequiresRevision(true);
            }

            student = this.save(student);

            response.put(Constants.MESSAGE, "Student verification updated successfully");
            response.put(Constants.RESPONSE, student);
            response.setResponseCode(HttpStatus.OK);
        } catch (Exception e) {
            ResponseDto.setErrorResponse(response, "GENERAL_ERROR", "Exception occurred during verifying the student: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return response;
    }

    public ResponseDto findByVerificationStatus(VerificationStatus status) {
        ResponseDto response = new ResponseDto(Constants.API_FIND_BY_VERIFICATION_STATUS);

        try {
            List<Student> students = studentRepository.findByVerificationStatus(status);

            if (students.isEmpty()) {
                response.put(Constants.MESSAGE, "No students found with the verification status: " + status);
                response.setResponseCode(HttpStatus.NOT_FOUND);
            } else {
                response.put(Constants.MESSAGE, "Students fetched successfully.");
                response.put(Constants.RESPONSE, students);
                response.setResponseCode(HttpStatus.OK);
            }
        } catch (Exception e) {
            ResponseDto.setErrorResponse(response, "GENERAL_ERROR", "Exception occurred while fetching students by verification status: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return response;
    }

    public Student save(Student student) {
        return studentRepository.save(student);
    }

    private boolean validateFile(Path path) throws IOException {
        if (Files.isExecutable(path)) {
            throw new RuntimeException("Invalid file");
        }
        Tika tika = new Tika();
        String fileExt = tika.detect(path);
        if (fileExt.equalsIgnoreCase("application/pdf")) {
            return true;
        } else if (fileExt.startsWith("image")) {
            return true;
        }
        throw new RuntimeException("Invalid file type. Supported files are PDF and Images.");
    }

    public ResponseDto deleteStudent(Long id) {
        ResponseDto response = new ResponseDto(Constants.API_DELETE_STUDENT);

        try {
            Optional<Student> studentOptional = studentRepository.findById(id);

            if (!studentOptional.isPresent()) {
                ResponseDto.setErrorResponse(response, "STUDENT_NOT_FOUND", "Student not found for ID: " + id, HttpStatus.NOT_FOUND);
                return response;
            }

            Student student = studentOptional.get();

            // Initialize GCS Credentials and client
            ServiceAccountCredentials credentials = ServiceAccountCredentials.fromPkcs8(gcpClientId, gcpClientEmail,
                    gcpPkcsKey, gcpPrivateKeyId, new ArrayList<String>());
            Storage storage = StorageOptions.newBuilder()
                    .setProjectId(gcpProjectId)
                    .setCredentials(credentials)
                    .build()
                    .getService();

            // Delete each document from GCS
            for (String path : Arrays.asList(student.getHighSchoolMarksheetPath(), student.getHighSchoolCertificatePath(), student.getIntermediateMarksheetPath(), student.getIntermediateCertificatePath())) {
                BlobId blobId = BlobId.of(gcpBucketName, path);
                storage.delete(blobId);
            }

            // Delete student record or mark it as obsolete based on your requirements
            studentRepository.deleteById(id);

            response.put(Constants.MESSAGE, "Student and associated documents deleted successfully");
            response.setResponseCode(HttpStatus.OK);
        } catch (Exception e) {
            ResponseDto.setErrorResponse(response, "GENERAL_ERROR", "Exception occurred during deleting the student: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return response;
    }

    private void deleteStudentDocument(String path) {
        if (path == null || path.isEmpty()) {
            return;
        }

        try {
            // Initialize GCS Credentials and client
            ServiceAccountCredentials credentials = ServiceAccountCredentials.fromPkcs8(gcpClientId, gcpClientEmail,
                    gcpPkcsKey, gcpPrivateKeyId, new ArrayList<String>());
            Storage storage = StorageOptions.newBuilder()
                    .setProjectId(gcpProjectId)
                    .setCredentials(credentials)
                    .build()
                    .getService();

            BlobId blobId = BlobId.of(gcpBucketName, path);
            storage.delete(blobId);
        } catch (Exception e) {
            log.error("Error deleting document from GCS: " + path, e);
            throw new RuntimeException("Error deleting document: " + path, e);
        }
    }

    public ResponseDto getStudentByKeycloakId(String keycloakId) {
        ResponseDto response = new ResponseDto(Constants.API_FIND_BY_KEYCLOAK_ID);
        try {
            Optional<Student> student = studentRepository.findByKeycloakId(keycloakId);
            if (student.isPresent()) {
                response.put(Constants.MESSAGE, "Student fetched successfully.");
                response.put(Constants.RESPONSE, student);
                response.setResponseCode(HttpStatus.OK);
            } else {
                response.put(Constants.MESSAGE, "No students found with ID : " + keycloakId);
                response.setResponseCode(HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            ResponseDto.setErrorResponse(response, "GENERAL_ERROR", "Exception occurred while fetching student by ID: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return response;
    }
}