package com.tarento.upsmf.examsAndAdmissions.service;

import com.tarento.upsmf.examsAndAdmissions.model.dao.EntityDao;

public interface AuditService {

	public void addEntityAudit(EntityDao oldObj, EntityDao updatedObj);

}
