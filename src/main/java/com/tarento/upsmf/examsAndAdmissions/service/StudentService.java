package com.tarento.upsmf.examsAndAdmissions.service;

import com.tarento.upsmf.examsAndAdmissions.model.Course;
import com.tarento.upsmf.examsAndAdmissions.model.Student;
import com.tarento.upsmf.examsAndAdmissions.model.VerificationStatus;
import com.tarento.upsmf.examsAndAdmissions.model.dto.StudentDto;
import com.tarento.upsmf.examsAndAdmissions.repository.CourseRepository;
import com.tarento.upsmf.examsAndAdmissions.repository.StudentRepository;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@PropertySource("classpath:application.properties")
public class StudentService {

    private static final Logger logger = LoggerFactory.getLogger(StudentService.class);
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    private final ModelMapper modelMapper;

    @Value("${file.storage.path}")
    private String storagePath;

    @Autowired
    public StudentService(StudentRepository studentRepository,CourseRepository courseRepository) {
        this.studentRepository = studentRepository;
        this.modelMapper = new ModelMapper();
        this.courseRepository= courseRepository;
        configureModelMapper();
    }

    private void configureModelMapper() {
        modelMapper.typeMap(StudentDto.class, Student.class).addMappings(mapper -> {
            mapper.skip(Student::setId);
        });
    }

    private String storeFile(MultipartFile file) throws IOException {
        String filename = UUID.randomUUID().toString() + "_" + System.currentTimeMillis() + "_" + file.getOriginalFilename();
        Path path = Paths.get(storagePath, filename);
        Files.copy(file.getInputStream(), path);
        return path.toString();
    }
        @Transactional
        public Student enrollStudent(StudentDto studentDto) throws IOException {
            Student student = modelMapper.map(studentDto, Student.class);
            Course dbCourse = courseRepository.findByCourseCode(studentDto.getCourseCode());
            if (dbCourse == null) {
                throw new RuntimeException("Course with code " + studentDto.getCourseCode() + " not found in the database");
            }

            student.setCourse(dbCourse);
/*
            if (dbCourse.getAvailableSeats() == null) {
                throw new RuntimeException("Seat information not set for course: " + dbCourse.getCourseName());
            }

            if (dbCourse.getAvailableSeats() <= 0) {
                throw new RuntimeException("No seats available for course: " + dbCourse.getCourseName());
            }

            dbCourse.setAvailableSeats(dbCourse.getAvailableSeats() - 1);
            courseRepository.save(dbCourse);*/

            // Generate provisional enrollment number
            String provisionalNumber = generateProvisionalNumber(student);
            student.setProvisionalEnrollmentNumber(provisionalNumber);

            // Set initial verification status to PENDING

            student.setHighSchoolMarksheetPath(storeFile(studentDto.getHighSchoolMarksheet()));
            student.setHighSchoolCertificatePath(storeFile(studentDto.getHighSchoolCertificate()));
            student.setIntermediateMarksheetPath(storeFile(studentDto.getIntermediateMarksheet()));
            student.setIntermediateCertificatePath(storeFile(studentDto.getIntermediateCertificate()));
            student.setVerificationStatus(VerificationStatus.PENDING);

            return studentRepository.save(student);
        }
    private String generateProvisionalNumber(Student student) {
        return student.getCourse().getCourseCode() + "-" + UUID.randomUUID().toString();
    }
    public List<Student> getAllStudents() {
        return (List<Student>) studentRepository.findAll();
    }

    public Optional<Student> getStudentById(Long id) {
        return studentRepository.findById(id);
    }
    public Student updateStudent(Long id, StudentDto studentDto) throws IOException {
        Student existingStudent = studentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Student not found for ID: " + id));

        if (studentDto.getHighSchoolMarksheet() != null) {
            deleteFile(existingStudent.getHighSchoolMarksheetPath());
            existingStudent.setHighSchoolMarksheetPath(storeFile(studentDto.getHighSchoolMarksheet()));
        }

        if (studentDto.getHighSchoolCertificate() != null) {
            deleteFile(existingStudent.getHighSchoolCertificatePath());
            existingStudent.setHighSchoolCertificatePath(storeFile(studentDto.getHighSchoolCertificate()));
        }

        if (studentDto.getIntermediateMarksheet() != null) {
            deleteFile(existingStudent.getIntermediateMarksheetPath());
            existingStudent.setIntermediateMarksheetPath(storeFile(studentDto.getIntermediateMarksheet()));
        }

        if (studentDto.getIntermediateCertificate() != null) {
            deleteFile(existingStudent.getIntermediateCertificatePath());
            existingStudent.setIntermediateCertificatePath(storeFile(studentDto.getIntermediateCertificate()));
        }

        modelMapper.map(studentDto, existingStudent);

        return studentRepository.save(existingStudent);
    }

    private void deleteFile(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return;
        }

        Path path = Paths.get(filePath);
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            // Handle the exception, e.g., logging or throwing it further
            throw new RuntimeException("Error deleting file: " + filePath, e);
        }
    }


    public void deleteStudent(Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Student not found for ID: " + id));

        // Delete associated files
        deleteFile(student.getHighSchoolMarksheetPath());
        deleteFile(student.getHighSchoolCertificatePath());
        deleteFile(student.getIntermediateMarksheetPath());
        deleteFile(student.getIntermediateCertificatePath());

        // Delete student record
        studentRepository.deleteById(id);
    }

    public Student findById(Long id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Student with ID " + id + " not found"));
    }
    public Student updateVerificationStatus(Student student, VerificationStatus status) {
        student.setVerificationStatus(status);
        return studentRepository.save(student);
    }
    public List<Student> findByVerificationStatus(VerificationStatus status) {
        return studentRepository.findByVerificationStatus(status);
    }

    public Student save(Student student) {
        return studentRepository.save(student);
    }
}
