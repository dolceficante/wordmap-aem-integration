package com.wordmap.core.models;

import java.util.ArrayList;
import java.util.List;

public class Schema {
	
	public Schema(String name) {
		this.name = name;
		fields = new ArrayList<Field>();
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public List<Field> getFields() {
		return fields;
	}
	public void setFields(List<Field> fields) {
		this.fields = fields;
	}
	private String name;
	private List<Field> fields;
	
	public void addField(Field f) {
		fields.add(f);
	}
}
