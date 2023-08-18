package com.tarento.upsmf.examsAndAdmissions.controller;

import com.tarento.upsmf.examsAndAdmissions.model.ApiResponse;
import com.tarento.upsmf.examsAndAdmissions.model.FeeManage;
import com.tarento.upsmf.examsAndAdmissions.service.FeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/fee/admin")
public class FeeController {
    @Autowired
    FeeService feeService;

    @PostMapping("/add")
    public ResponseEntity<?> addFeeDetails(@RequestBody FeeManage feeManage) {
        ApiResponse response = feeService.addFeeDetails(feeManage);
        return new ResponseEntity<>(response, response.getResponseCode());
    }
    @PostMapping("/update/{id}")
    public ResponseEntity<?> updateFeeDetails(@RequestBody FeeManage feeManage, @RequestParam Integer id) {
        ApiResponse response = feeService.updateFeeDetails(feeManage, id);
        return new ResponseEntity<>(response, response.getResponseCode());
    }
    @GetMapping("/getFeeDetails")
    public ResponseEntity<?> getFeeDetails(){
        ApiResponse response = feeService.fetchAllFeeDetails();
        return new ResponseEntity<>(response, response.getResponseCode());
    }
    @GetMapping("/getFeeDetails/{feeId}")
    public ResponseEntity<?> getFeeDetailsById(Integer feeId){
        ApiResponse response = feeService.fetchFeeDetailsById(feeId);
        return new ResponseEntity<>(response, response.getResponseCode());
    }
}