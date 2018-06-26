package com.wordmap.core.models;

import java.util.ArrayList;
import java.util.List;

public class Field {
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<Value> getValues() {
		return values;
	}

	public void setValues(List<Value> values) {
		this.values = values;
	}

	private String name;
	private List<Value> values;
	
	public Field(String name) {
		this.name = name;
		values = new ArrayList<Value>();
	}
	
	public void addValue(Value value) {
		values.add(value);
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	private String id;

}
