package com.tarento.upsmf.examsAndAdmissions.model;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter

public class ResponseDto {


    private String id;
    private String ver;
	private String ts;
    private ResponseParams params;
    private HttpStatus responseCode;
    private int responseCodeNumeric;
    private Map<String, Object> data = new HashMap<>();  // Renamed from response to data
    private ErrorDetails error; // Error details

    public static class ErrorDetails {
        private String code;
        private String message;
        private String details;

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getDetails() {
            return details;
        }

        public void setDetails(String details) {
            this.details = details;
        }
    }

    public int getResponseCodeNumeric() {
        return responseCodeNumeric;
    }

    public void setResponseCodeNumeric(int responseCodeNumeric) {
        this.responseCodeNumeric = responseCodeNumeric;
    }
    @JsonIgnore
    private transient Map<String, Object> response = new HashMap<>();

    public ResponseDto() {
        this.ver = "v1";
        this.ts = String.valueOf(new Timestamp(System.currentTimeMillis()));
        this.params = new ResponseParams();
    }
    public ResponseDto(HttpStatus status, ResponseParams params) {
        this.ver = "v1";
        this.ts = String.valueOf(new Timestamp(System.currentTimeMillis()));
        this.params = params;
        this.responseCode = status;
    }

    public ResponseDto(String id) {
        this();
        this.id = id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setVer(String ver) {
        this.ver = ver;
    }

	public void setTs(String ts) {
        this.ts = ts;
    }

    public void setParams(ResponseParams params) {
        this.params = params;
    }

    public void setResponseCode(HttpStatus responseCode) {
        this.responseCode = responseCode;
        this.responseCodeNumeric = responseCode.value();
    }
    public Map<String, Object> getResult() {
        return response;
    }

    public void setResult(Map<String, Object> result) {
        response = result;
    }
/*
    public Object get(String key) {
        return response.get(key);
    }

    public void put(String key, Object vo) {
        response.put(key, vo);
    }

    public void putAll(Map<String, Object> map) {
        response.putAll(map);
    }

    public boolean containsKey(String key) {
        return response.containsKey(key);
    }*/
    /*public Map<String, Object> getData() { // Renamed from getResult to getData
        return data;
    }

    public void setData(Map<String, Object> data) { // Renamed from setResult to setData
        this.data = data;
    }*/

    public Object get(String key) {
        return response.get(key);
    }

    public void put(String key, Object vo) {
        response.put(key, vo);
    }

    public void putAll(Map<String, Object> map) {
        response.putAll(map);
    }

    public boolean containsKey(String key) {
        return response.containsKey(key);
    }

    public ErrorDetails getError() {
        return error;
    }

    public void setError(ErrorDetails error) {
        this.error = error;
    }
    public void setMessage(String s) {
    }
}