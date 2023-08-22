package com.tarento.upsmf.examsAndAdmissions.repository;


import com.tarento.upsmf.examsAndAdmissions.model.dao.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment,Integer> {
    @Query(value = "SELECT * FROM fee_details f LEFT JOIN exam_details e ON f.fee_id = e.fee_id WHERE f.fee_id = :id", nativeQuery = true)
    Optional<Payment> findByIdWithExamsNative(@Param("id") Integer id);
}
