package com.tarento.upsmf.examsAndAdmissions.model.dao;

import com.tarento.upsmf.examsAndAdmissions.util.QueryUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = QueryUtils.Table.BOOKMARK)
@Table(name = QueryUtils.Table.BOOKMARK)
public class BookmarkDao {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;

	@Column(name = QueryUtils.TableFields.NODE_ID)
	private int nodeId;

	@Column(name = QueryUtils.TableFields.USER_ID)
	private String userId;

}
