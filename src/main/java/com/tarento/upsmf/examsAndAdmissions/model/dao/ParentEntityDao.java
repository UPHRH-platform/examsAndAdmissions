package com.tarento.upsmf.examsAndAdmissions.model.dao;

import com.tarento.upsmf.examsAndAdmissions.util.QueryUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = QueryUtils.Table.PARENT_NODE)
@Table(name = QueryUtils.Table.PARENT_NODE)
public class ParentEntityDao {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;

	@Column
	private String type;

	@Column(name = QueryUtils.TableFields.PARENT_ID)
	private int parentId;

	@Column
	private String child;

	@Column
	private String status;

	@JoinColumn(name = QueryUtils.TableFields.PARENT_MAP_ID)
	@OneToMany(fetch = FetchType.EAGER)
	private List<ChildEntityDao> children;

}
