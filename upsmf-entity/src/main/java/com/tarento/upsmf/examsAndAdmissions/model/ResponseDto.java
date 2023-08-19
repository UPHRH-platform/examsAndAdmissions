package com.tarento.upsmf.examsAndAdmissions.model;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

import lombok.Getter;
import org.springframework.http.HttpStatus;


public class ResponseDto {

    private String id;
    private String ver;
    @Getter
	private String ts;
    private ResponseParams params;
    private HttpStatus responseCode;

    private transient Map<String, Object> response = new HashMap<>();

    public ResponseDto() {
        this.ver = "v1";
        this.ts = String.valueOf(new Timestamp(System.currentTimeMillis()));
        this.params = new ResponseParams();
    }

    public ResponseDto(String id) {
        this();
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getVer() {
        return ver;
    }

    public void setVer(String ver) {
        this.ver = ver;
    }

	public void setTs(String ts) {
        this.ts = ts;
    }

    public ResponseParams getParams() {
        return params;
    }

    public void setParams(ResponseParams params) {
        this.params = params;
    }

    public HttpStatus getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(HttpStatus responseCode) {
        this.responseCode = responseCode;
    }

    public Map<String, Object> getResult() {
        return response;
    }

    public void setResult(Map<String, Object> result) {
        response = result;
    }

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
}