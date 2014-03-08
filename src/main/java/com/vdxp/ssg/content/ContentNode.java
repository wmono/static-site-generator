package com.vdxp.ssg.content;

import com.google.common.collect.ImmutableMap;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class ContentNode {

	private ContentNode parent;

	@SuppressWarnings("rawtypes")
	private final Map data = new HashMap();

	protected abstract ContentNode getChildByName(final String name);

	protected abstract void accept(final ContentVisitor visitor, List<ContentNode> parents);

	public abstract String getName();

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

	public ContentNode getPath(final String path) {
		final String[] pathParts = path.split("/", 2);
		if (pathParts.length == 0) {
			return null;
		}
		if (pathParts.length == 1) {
			return getChildByName(pathParts[0]);
		}
		if (pathParts.length == 2) {
			final ContentNode child = getChildByName(pathParts[0]);
			if (child != null) {
				return child.getPath(pathParts[1]);
			} else {
				return null;
			}
		}
		throw new IndexOutOfBoundsException("split returned too many elements");
	}

}
