package com.tarento.upsmf.examsAndAdmissions.repository;

import com.tarento.upsmf.examsAndAdmissions.model.AttendanceRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AttendanceRepository extends JpaRepository<AttendanceRecord, Long> {
    boolean existsByStudentEnrollmentNumberAndExamCycleData(String studentEnrollmentNumber, String examCycleName);
    AttendanceRecord findByStudentEnrollmentNumber(String studentEnrollmentNumber);
    List<AttendanceRecord> findByExamCycleId(Long examCycleId);

    List<AttendanceRecord> findByExamCycleData(String examCycleName);
}