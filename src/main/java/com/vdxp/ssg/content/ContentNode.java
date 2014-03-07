package com.vdxp.ssg.content;

import java.util.Collections;
import java.util.List;

public abstract class ContentNode {

	private String name;
	private ContentNode parent;

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

	public void accept(final ContentVisitor visitor) {
		final List<ContentNode> parents = Collections.emptyList();
		accept(visitor, parents);
	}

}
