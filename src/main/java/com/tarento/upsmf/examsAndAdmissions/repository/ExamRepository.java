package com.tarento.upsmf.examsAndAdmissions.repository;

import com.tarento.upsmf.examsAndAdmissions.model.Exam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface ExamRepository extends JpaRepository<Exam, Long> {

    List<Exam> findByObsolete(Integer value);

    Optional<Exam> findByIdAndObsolete(Long id, Integer value);

    Optional<Exam> findByExamName(String examName);

    Optional<Exam> findByExamNameAndExamDateAndStartTimeAndEndTime(String examName, LocalDate examDate, LocalTime startTime, LocalTime endTime);
    
    Optional<Exam> findByExamCycleIdAndObsolete(Long examCycleId, Integer value);
    List<Exam> findByExamCycleId(Long examCycleId);
    List<Exam> findAllByExamCycleIdAndObsolete(Long examCycleId, Integer value);

    @Query("SELECT e FROM Exam e JOIN StudentExamRegistration ser ON e.id = ser.exam.id WHERE ser.student.id = :studentId AND e.examCycleId = :examCycleId")
    List<Exam> findRegisteredExamsForStudentInCycle(Long studentId, Long examCycleId);
    @Query("SELECT e.examName FROM Exam e WHERE e.id = :exam")
    String getExamNameById(Long exam);
}
