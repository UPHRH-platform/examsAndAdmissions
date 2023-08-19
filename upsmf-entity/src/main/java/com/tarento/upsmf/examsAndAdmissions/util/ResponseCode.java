package com.tarento.upsmf.examsAndAdmissions.util;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ResponseCode {

	SUCCESS(ResponseMessage.Key.SUCCESS_MESSAGE, ResponseMessage.Message.SUCCESS_MESSAGE,
			ResponseMessage.Code.SUCCESS),
	FAILED(ResponseMessage.Key.FAILURE_MESSAGE, ResponseMessage.Message.FAILURE_MESSAGE,
			ResponseMessage.Code.FAILURE), 
	UNAUTHORIZED(ResponseMessage.Key.UNAUTHORIZED_USER, ResponseMessage.Message.UNAUTHORIZED_USER,
			ResponseMessage.Code.UNAUTHORIZED), 
	TOKEN_MISSING(ResponseMessage.Key.BAD_REQUEST, ResponseMessage.Message.TOKEN_MISSING,
			ResponseMessage.Code.BAD_REQUEST), 
	CREATE_FAILED(ResponseMessage.Key.FAILURE_MESSAGE, ResponseMessage.Message.CREATE_ERROR_MESSAGE,
			ResponseMessage.Code.FAILURE),
	UPLOAD_FAILED(ResponseMessage.Key.FAILURE_MESSAGE, ResponseMessage.Message.UPLOAD_ERROR_MESSAGE,
			ResponseMessage.Code.FAILURE),
	DELETE_FAILED(ResponseMessage.Key.FAILURE_MESSAGE, ResponseMessage.Message.UPLOAD_ERROR_MESSAGE,
			ResponseMessage.Code.FAILURE),
	GET_FAILED(ResponseMessage.Key.FAILURE_MESSAGE, ResponseMessage.Message.GET_ERROR_MESSAGE,
			ResponseMessage.Code.FAILURE),
	MAPPING_FAILED(ResponseMessage.Key.FAILURE_MESSAGE, ResponseMessage.Message.MAPPING_ERROR_MESSAGE,
			ResponseMessage.Code.FAILURE);

	private final HttpStatus responseCode;
	private final String errorCode;
	private final String errorMessage;

	ResponseCode(String errorCode, String errorMessage, int responseCode) {
		this.errorCode = errorCode;
		this.errorMessage = errorMessage;
		this.responseCode = HttpStatus.valueOf(responseCode);
	}

}
