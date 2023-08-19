package com.tarento.upsmf.examsAndAdmissions.controller;

import com.tarento.upsmf.examsAndAdmissions.model.dao.Payment;
import com.tarento.upsmf.examsAndAdmissions.model.ResponseDto;
import com.tarento.upsmf.examsAndAdmissions.service.PaymentService;
import com.tarento.upsmf.examsAndAdmissions.util.PathRoutes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PaymentController {

    @Autowired
    PaymentService paymentService;

    @PostMapping(value = PathRoutes.Endpoints.MAKE_PAYMENT)
    public ResponseEntity<?> payment(@RequestBody Payment payment){
        ResponseDto response = paymentService.makePayment(payment);
        return new ResponseEntity<>(response, response.getResponseCode());
    }
}
