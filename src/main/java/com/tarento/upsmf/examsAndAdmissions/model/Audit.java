package com.tarento.upsmf.examsAndAdmissions.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Audit {
	private Object changedFrom;
	private Object changedTo;
	private String field;
	private String action;
}
