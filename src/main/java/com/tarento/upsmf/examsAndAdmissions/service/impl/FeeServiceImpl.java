package com.tarento.upsmf.examsAndAdmissions.service.impl;

import com.tarento.upsmf.examsAndAdmissions.Utils.Constants;
import com.tarento.upsmf.examsAndAdmissions.model.ApiResponse;
import com.tarento.upsmf.examsAndAdmissions.model.FeeManage;
import com.tarento.upsmf.examsAndAdmissions.repository.FeeRepository;
import com.tarento.upsmf.examsAndAdmissions.service.FeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class FeeServiceImpl implements FeeService {
    @Autowired
    FeeRepository feeRepository;

    @Override
    public ApiResponse addFeeDetails(FeeManage feeManage) {
        ApiResponse response = new ApiResponse(Constants.API_FEE_DETAILS_ADD);

        // Calculate noOfExams based on the number of selected options
        int noOfExams = feeManage.getExams().size();

        // Calculate the fee amount based on noOfExams and exam fee amount (replace with actual fee calculation)
        int examFeeAmount = 100; // Example exam fee amount
        int feeAmount = calculateFee(noOfExams, examFeeAmount);

        // Set the calculated values in the FeeManage object
        feeManage.setNoOfExams(noOfExams);
        feeManage.setFeeAmount(feeAmount);

        // Save the FeeManage object
        try {
            FeeManage result = feeRepository.save(feeManage);

            response.put(Constants.MESSAGE, Constants.SUCCESSFUL);
            response.put(Constants.RESPONSE, result);
            response.setResponseCode(HttpStatus.OK);
        } catch (Exception e) {
            response.put(Constants.MESSAGE, "Error saving fee details");
            response.put(Constants.RESPONSE, Constants.FAILUREMESSAGE);
            response.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return response;
    }
    @Override
    public ApiResponse updateFeeDetails(FeeManage updatedFeeManage, Integer id) {
        ApiResponse response = new ApiResponse(Constants.API_FEE_DETAILS_UPDATE);

        // Validate the updatedFeeManage data before proceeding
        if (updatedFeeManage.getFullName() == null || updatedFeeManage.getFullName().isEmpty()) {
            response.put(Constants.MESSAGE, "Full name is required");
            response.put(Constants.RESPONSE, Constants.FAILUREMESSAGE);
            response.setResponseCode(HttpStatus.BAD_REQUEST);
            return response;
        }

        // Retrieve the existing FeeManage entity by ID from the database
        Optional<FeeManage> existingFeeManageOptional = feeRepository.findById(id);

        if (existingFeeManageOptional.isPresent()) {
            FeeManage existingFeeManage = existingFeeManageOptional.get();

            // Update the relevant fields with the new values
            existingFeeManage.setFullName(updatedFeeManage.getFullName());
            existingFeeManage.setExams(updatedFeeManage.getExams());
            // Update other fields as needed

            // Save the updated entity back to the database
            FeeManage updatedFee = feeRepository.save(existingFeeManage);

            response.put(Constants.MESSAGE, Constants.SUCCESSFUL);
            response.put(Constants.RESPONSE, Constants.SUCCESSMESSAGE);
            response.setResponseCode(HttpStatus.OK);
        } else {
            response.put(Constants.MESSAGE, "Fee details with ID " + id + " not found");
            response.put(Constants.RESPONSE, Constants.FAILUREMESSAGE);
            response.setResponseCode(HttpStatus.NOT_FOUND);
        }

        return response;
    }
    @Override
    public ApiResponse  fetchAllFeeDetails() {
        List<FeeManage> feeDetails = feeRepository.findAll();
        ApiResponse response = new ApiResponse(Constants.API_FEE_DETAILS_FETCH_ALL);
        response.put(Constants.MESSAGE, Constants.SUCCESSFUL);
        response.put(Constants.RESPONSE, feeDetails);
        response.setResponseCode(HttpStatus.OK);
        return response;
    }
    @Override
    public ApiResponse fetchFeeDetailsById(Integer id) {
        Optional<FeeManage> feeManageOptional = feeRepository.findByIdWithExamsNative(id);

        ApiResponse response = new ApiResponse(Constants.API_FEE_DETAILS_FETCH_BY_ID);

        if (feeManageOptional.isPresent()) {
            FeeManage feeDetails = feeManageOptional.get();

            response.put(Constants.MESSAGE, Constants.SUCCESSFUL);
            response.put(Constants.RESPONSE, feeDetails);
            response.setResponseCode(HttpStatus.OK);
        } else {
            response.put(Constants.MESSAGE, "Fee details with ID " + id + " not found");
            response.put(Constants.RESPONSE, Constants.FAILUREMESSAGE);
            response.setResponseCode(HttpStatus.NOT_FOUND);
        }

        return response;
    }
    @Override
    public Integer calculateFee(Integer noOfExams, Integer examFeeAmount) {
        return noOfExams * examFeeAmount;
    }
}