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
@Entity(name = QueryUtils.Table.CHILD_NODE)
@Table(name = QueryUtils.Table.CHILD_NODE)
public class ChildEntityDao {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;

	@Column(name = QueryUtils.TableFields.PARENT_MAP_ID)
	private int parentMapId;

	@Column(name = QueryUtils.TableFields.CHILD_ID)
	private int childId;
}
