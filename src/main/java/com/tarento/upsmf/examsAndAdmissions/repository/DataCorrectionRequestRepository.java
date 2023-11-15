package com.tarento.upsmf.examsAndAdmissions.repository;

import com.tarento.upsmf.examsAndAdmissions.model.dto.DataCorrectionRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DataCorrectionRequestRepository extends JpaRepository<DataCorrectionRequest, Long> {
    String getProofAttachmentPathById(Long requestId);

    List<DataCorrectionRequest> getByExamCycleId(Optional<Long> examCycleId);
}
