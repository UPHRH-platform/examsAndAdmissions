package com.tarento.upsmf.examsAndAdmissions.service;

import java.util.List;
import java.util.Map;

import com.tarento.upsmf.examsAndAdmissions.model.WfRequest;

public interface WorkflowService {

	Boolean workflowTransition(WfRequest wfRequest);

	List<Map<String, Object>> getworkflowAction(String state);

}
