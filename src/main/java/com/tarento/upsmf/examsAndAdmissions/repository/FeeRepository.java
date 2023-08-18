package com.tarento.upsmf.examsAndAdmissions.repository;

import com.tarento.upsmf.examsAndAdmissions.model.FeeManage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface FeeRepository extends JpaRepository<FeeManage,Integer> {
    @Query(value = "SELECT * FROM fee_details f LEFT JOIN exam_details e ON f.fee_id = e.fee_id WHERE f.fee_id = :id", nativeQuery = true)
    Optional<FeeManage> findByIdWithExamsNative(@Param("id") Integer id);
}
