package com.tarento.upsmf.examsAndAdmissions.repository;

import com.tarento.upsmf.examsAndAdmissions.model.dao.EntityDao;
import org.springframework.data.repository.CrudRepository;

public interface EntityRepository extends CrudRepository<EntityDao, Integer>, CustomRepository<EntityDao> {

}
