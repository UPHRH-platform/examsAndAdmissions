package com.tarento.upsmf.examsAndAdmissions.service;

import com.tarento.upsmf.examsAndAdmissions.model.ApiResponse;
import com.tarento.upsmf.examsAndAdmissions.model.FeeManage;
import org.springframework.stereotype.Service;

@Service
public interface FeeService {
    public ApiResponse addFeeDetails(FeeManage feeManage);
    public Integer calculateFee(Integer noOfExams, Integer examFeeAmount);
    public ApiResponse updateFeeDetails(FeeManage feeManage, Integer id);
    public ApiResponse  fetchAllFeeDetails();
    public ApiResponse fetchFeeDetailsById(Integer id);
}
