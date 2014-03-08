package com.vdxp.ssg.content;

import com.google.common.collect.ImmutableMap;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class ContentNode {

	private String name;
	private ContentNode parent;

	private final Map data = new HashMap();

	public ContentNode(final String name) {
		this.name = name;
	}

	protected abstract void accept(final ContentVisitor visitor, List<ContentNode> parents);

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public ContentNode getParent() {
		return parent;
	}

	public void setParent(final ContentNode parent) {
		if (this.parent != null) {
			throw new IllegalStateException("This content node already has a parent");
		}

		this.parent = parent;
	}

	public void clearParent() {
		this.parent = null;
	}

	@SuppressWarnings("rawtypes")
	public void putData(final Map data) {
		this.data.putAll(data);
	}

	@SuppressWarnings("rawtypes")
	public Map getData() {
		return ImmutableMap.copyOf(data);
	}

	public void accept(final ContentVisitor visitor) {
		final List<ContentNode> parents = Collections.emptyList();
		accept(visitor, parents);
	}

}
