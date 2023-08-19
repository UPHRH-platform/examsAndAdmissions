package com.tarento.upsmf.examsAndAdmissions.repository;

import javax.transaction.Transactional;

import com.tarento.upsmf.examsAndAdmissions.model.dao.ChildEntityDao;
import com.tarento.upsmf.examsAndAdmissions.util.QueryUtils;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChildEntityRepository extends CrudRepository<ChildEntityDao, Long> {

	@Transactional
	@Modifying
	@Query("DELETE FROM " + QueryUtils.Table.CHILD_NODE + " WHERE parent_map_id = ?1")
	void deleteByMapId(Integer mapId);

}
