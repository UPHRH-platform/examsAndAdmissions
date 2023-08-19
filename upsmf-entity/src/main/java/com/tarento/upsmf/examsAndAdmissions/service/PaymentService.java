package com.tarento.upsmf.examsAndAdmissions.service;

import com.tarento.upsmf.examsAndAdmissions.model.dao.Payment;
import com.tarento.upsmf.examsAndAdmissions.model.ResponseDto;
import org.springframework.stereotype.Service;

@Service
public interface PaymentService {
    public ResponseDto makePayment(Payment payment);
}