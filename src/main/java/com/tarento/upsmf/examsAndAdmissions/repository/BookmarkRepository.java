package com.tarento.upsmf.examsAndAdmissions.repository;

import org.springframework.data.repository.CrudRepository;

import com.tarento.upsmf.examsAndAdmissions.model.dao.BookmarkDao;

public interface BookmarkRepository extends CrudRepository<BookmarkDao, Long> {

}
