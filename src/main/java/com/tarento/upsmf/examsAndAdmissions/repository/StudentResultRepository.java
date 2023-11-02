package com.tarento.upsmf.examsAndAdmissions.repository;

import com.tarento.upsmf.examsAndAdmissions.model.Exam;
import com.tarento.upsmf.examsAndAdmissions.model.ExamCycle;
import com.tarento.upsmf.examsAndAdmissions.model.StudentExamRegistration;
import com.tarento.upsmf.examsAndAdmissions.model.StudentResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface StudentResultRepository extends JpaRepository<StudentResult, Long> {
    StudentResult findByStudent_EnrollmentNumber(String enrolmentNumber);

    List<StudentResult> findByCourse_IdAndExam_ExamCycleIdAndPublished(Long courseId, Long examCycleId, boolean b);

//    List<StudentResult> findByStudent_EnrollmentNumberAndStudent_DateOfBirthAndExamCycle_IdAndPublished(String enrollmentNumber, LocalDate dateOfBirth, Long examCycleId, boolean published);

    boolean existsByEnrollmentNumberAndFirstNameAndLastName(String enrollmentNumber,String firstName, String lastName);

    List<StudentResult> findByExamCycleAndExam(Long examCycle, Exam exam);

    List<StudentResult> findByExamCycleId(Long examCycle);

    List<StudentResult> findByExam(Exam exam);

    List<StudentResult> findByStudent_Institute_IdAndExamCycle_IdAndExam_id(Long instituteId, Long examCycleId, Long examId);

    @Query("SELECT sr FROM StudentResult sr WHERE sr.examCycle.id = :examCycle AND sr.exam.id = :exam AND sr.student.institute.id = :institute")
    List<StudentResult> findByExamCycleAndExamAndInstitute(@Param("examCycle") Long examCycle, @Param("exam") Long exam, @Param("institute") Long institute);

    Optional<StudentResult> findByExamIdAndStudentId(Long examId, Long studentId);
    @Modifying
    @Transactional
    @Query(value = "UPDATE student_results SET external_marks = null FROM exam, students WHERE student_results.exam_cycle_id = exam.exam_cycle_id AND student_results.student_id = students.id AND exam.exam_cycle_id = :examCycleId AND students.institute_id = :instituteId", nativeQuery = true)
    int setExternalMarksToNull(@Param("examCycleId") Long examCycleId, @Param("instituteId") Long instituteId);

    @Query("SELECT sr FROM StudentResult sr WHERE sr.firstName = :firstName AND sr.lastName = :lastName AND sr.enrollmentNumber = :enrollmentNumber")
    StudentResult findByFirstNameAndLastNameAndEnrollmentNumber(
            @Param("firstName") String firstName,
            @Param("lastName") String lastName,
            @Param("enrollmentNumber") String enrollmentNumber
    );

    Optional<StudentResult> findByExamId(Long id);

    @Query("SELECT sr FROM StudentResult sr WHERE sr.exam_name = :examName AND sr.instituteId = :instituteId")
    List<StudentResult> findByExamNameAndInstituteId(@Param("examName") String examName, @Param("instituteId") Long instituteId);

    List<StudentResult> findByExamExamNameAndInstituteId(String examName, Long instituteId);

    List<StudentResult> findByStudent_EnrollmentNumberAndExamCycle_IdAndPublished(String enrollmentNumber, Long examCycleId, boolean b);

    @Query("SELECT sr FROM StudentResult sr WHERE sr.examCycle_name = :examCycleName AND sr.exam_name = :examName AND sr.instituteId = :institute")
    List<StudentResult> findByExamCycleNameAndExamNameAndInstitute(String examCycleName, String examName, Long institute);

    @Query("SELECT sr FROM StudentResult sr WHERE sr.examCycle_name = :examCycleName")
    List<StudentResult> findByExamCycleName(String examCycleName);
}

