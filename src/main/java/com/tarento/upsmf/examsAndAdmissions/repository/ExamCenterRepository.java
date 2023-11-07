package com.tarento.upsmf.examsAndAdmissions.repository;

import com.tarento.upsmf.examsAndAdmissions.enums.ApprovalStatus;
import com.tarento.upsmf.examsAndAdmissions.model.ExamCenter;
import com.tarento.upsmf.examsAndAdmissions.model.ExamCycle;
import com.tarento.upsmf.examsAndAdmissions.model.Institute;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ExamCenterRepository extends JpaRepository<ExamCenter, Long> {

    List<ExamCenter> findByExamCycleAndApprovalStatus(ExamCycle examCycle, ApprovalStatus approvalStatus);

    Optional<ExamCenter> findByInstituteAndExamCycle(Institute institute, ExamCycle examCycle);

    List<ExamCenter> findByExamCycle(ExamCycle examCycle);

    // Since you don't have a verified field in ExamCenter, I'll remove the method that uses it.
     List<ExamCenter> findByDistrictAndApprovalStatus(String district, ApprovalStatus approvalStatus);

    Optional<ExamCenter> findByInstituteCodeAndApprovalStatus(String instituteCode, ApprovalStatus approvalStatus);

    ExamCenter getByInstituteCodeAndExamCycleIdAndApprovalStatus(String instituteCode,Long ExamCycleId, ApprovalStatus approvalStatus);
    List<ExamCenter> findByExamCycle_Id(Long examCycleId);
    Optional<ExamCenter> findByExamCycleIdAndId(Long examCycleId, Long examCenterId);
    List<ExamCenter> findByDistrictAndExamCycleAndApprovalStatus(String district, ExamCycle examCycleId, ApprovalStatus approvalStatus);
    Optional<ExamCenter> getByIdAndExamCycle(Long unverifiedExamCenterId, ExamCycle examCycle);
}
