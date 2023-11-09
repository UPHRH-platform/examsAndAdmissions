package com.tarento.upsmf.examsAndAdmissions.repository;

import com.tarento.upsmf.examsAndAdmissions.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface StudentExamRegistrationRepository extends JpaRepository<StudentExamRegistration, Long> {
    Optional<StudentExamRegistration> findByStudentAndExam(Student student, Exam exam);
    List<StudentExamRegistration> findByStudentIdInAndExamIdIn(Set<Long> studentIds, Set<Long> examIds);
    List<StudentExamRegistration> findByExamCycleId(Long examCycleId);
    List<StudentExamRegistration> findByStudentAndExamCycle(Student student, ExamCycle examCycle);
    List<Exam> findExamsByStudent(Student student);
    long countByExamCenter(ExamCenter center);

    Optional<StudentExamRegistration> findByIdAndStudent_DateOfBirth(Long id, LocalDate localDate);

    @Query(value = "SELECT * from student_exam_registration where student_id=:id and exam_cycle_id=:examCycleId", nativeQuery = true)
    List<StudentExamRegistration> findAllByStudent_IdAndExam_Id(Long id, Long examCycleId);

    List<StudentExamRegistration> findByExamCenter(ExamCenter originalExamCenter);

    List<StudentExamRegistration> findByInstituteIdAndExamCenterIsNull(Long instituteId);

    List<StudentExamRegistration> findByExamCenterInstituteId(Long instituteId);

    List<StudentExamRegistration> findByInstitute(Institute unverifiedInstitute);
    List<StudentExamRegistration> findByIsFeesPaidAndStatus(boolean isFeesPaid, String status);


    @Modifying
    @Query(value = "update student_exam_registration set remarks=:remarks, status=:status, is_fees_paid =:is_fees_paid " +
            "where student_id =:studentId and institute_id=:institute_id " +
            "and exam_cycle_id=:exam_cycle_id and exam_id=:exam_id", nativeQuery = true)
    void updateExamFeeByStudentId(@Param("is_fees_paid") Boolean isFeesPaid, @Param("status") String status,
                                  @Param("remarks") String remarks, @Param("studentId") Long studentId,
                                  @Param("institute_id") Long instituteId, @Param("exam_cycle_id") Long examCycleId,
                                  @Param("exam_id") Long examId);

    List<StudentExamRegistration> findByExamCenterIsNullAndInstitute(Institute institute);

    StudentExamRegistration findByStudent(Student student);

    Optional<StudentExamRegistration> findByStudentIdAndExamCycleId(Long studentId, Long examCycleId);

    List<StudentExamRegistration> findByStudentIdInAndExamCycleIdIn(Set<Long> studentIds, ArrayList<Long> longs);

    List<StudentExamRegistration> findByExamCycleIdAndInstituteId(Long examCycleId, Long instituteId);

    @Query("SELECT s FROM Student s WHERE s.verificationStatus = com.tarento.upsmf.examsAndAdmissions.enums.VerificationStatus.VERIFIED AND s.institute.id = :instituteId AND NOT EXISTS (SELECT ser FROM StudentExamRegistration ser WHERE ser.student.id = s.id AND ser.examCycle.id = :examCycleId)")
    List<Student> findVerifiedStudentsNotRegisteredForExamCycleByInstitute(Long examCycleId, Long instituteId);
    StudentExamRegistration getByStudentId(Long id);

    StudentExamRegistration getByStudentIdAndExamCycleId(Long studentId, Long examCycleId);
}
