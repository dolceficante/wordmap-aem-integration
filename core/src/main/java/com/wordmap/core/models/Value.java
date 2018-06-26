package com.wordmap.core.models;

public class Value {
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	private String id;
	private String text;
	
	public Value(String id, String text) {
		this.id = id;
		this.text = text;
	}

}
