package com.wordmap.core.models;

import java.util.ArrayList;
import java.util.List;

public class TaxonomyNode {
	
	public TaxonomyNode() {
		children = new ArrayList<TaxonomyNode>();
	}
	
	public TaxonomyNode(TaxonomyNode parent) {
		this.parent = parent;
		children = new ArrayList<TaxonomyNode>();
	}	

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public TaxonomyNode getParent() {
		return parent;
	}

	public void setParent(TaxonomyNode parent) {
		this.parent = parent;
	}

	public List<TaxonomyNode> getChildren() {
		return children;
	}

	public void setChildren(List<TaxonomyNode> children) {
		this.children = children;
	}

	private int id;
	private String name;
	private TaxonomyNode parent;
	
	private List<TaxonomyNode> children;
	
	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append("{\"id\": " + id + ", \"name\": \"" + name + "\", ");
		buf.append("\"children\" : [ ");
		for (TaxonomyNode node : children) {
			buf.append(node.toString() + " ");
		}
		buf.append("] }");
		return buf.toString();
	}

	public void addChild(TaxonomyNode childNode) {
		children.add(childNode);
	}
}
