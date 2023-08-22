package com.tarento.upsmf.examsAndAdmissions.service;

import java.util.List;
import java.util.Map;

import com.tarento.upsmf.examsAndAdmissions.model.Bookmark;
import com.tarento.upsmf.examsAndAdmissions.model.EntityRelation;
import com.tarento.upsmf.examsAndAdmissions.model.EntityVerification;
import com.tarento.upsmf.examsAndAdmissions.model.SearchObject;
import com.tarento.upsmf.examsAndAdmissions.model.UserProfile;
import com.tarento.upsmf.examsAndAdmissions.model.dao.EntityDao;

public interface EntityService {

	public EntityDao addUpdateEntity(EntityDao entityDao, String userId);

	public EntityDao getEntityById(Integer id, SearchObject searchObject);

	public Boolean addEntityRelation(EntityRelation entityRelation);

	Boolean bookmarkEntity(Bookmark bookmarkEntityNode, String userId);

	public void addEntities(List<EntityDao> entityList);

	public List<EntityDao> getAllDataNodes(SearchObject searchObject);

	Boolean deleteEntity(Integer id);

	Boolean addFeedback(Map<String, Object> feedbackDocument);

	public Boolean reviewEntity(EntityVerification entityVerification, UserProfile userProfile);

}
