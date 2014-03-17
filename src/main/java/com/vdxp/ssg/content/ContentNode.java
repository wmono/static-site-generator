package com.vdxp.ssg.content;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class ContentNode {

	private ContentNode parent;

	private final Map<String, Object> data = new HashMap<String, Object>();

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

	public void putData(final Map<String, Object> data) {
		this.data.putAll(data);
	}

	public void putData(final String key, final Object value) {
		this.data.put(key, value);
	}

	public Map<String, Object> getData() {
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

	public String getRelativePath(final ContentNode target) {
		final ArrayDeque<String> path = new ArrayDeque<String>();
		final ContentNode commonParent = getNearestCommonParent(this, target);
		if (commonParent == null) {
			throw new IllegalArgumentException("Content nodes do not have a common parent");
		}

		ContentNode current;

		current = target;
		while (current != null && current != commonParent) {
			path.addFirst(current.getName());
			current = current.getParent();
		}

		current = this.getParent();
		while (current != null && current != commonParent) {
			path.addFirst("..");
			current = current.getParent();
		}

		return Joiner.on('/').join(path);
	}

	public Deque<ContentNode> getAllParents() {
		final ArrayDeque<ContentNode> allParents = new ArrayDeque<ContentNode>();

		ContentNode current = this;
		while (current != null) {
			allParents.addFirst(current);
			current = current.getParent();
		}

		return allParents;
	}

	public static ContentNode getNearestCommonParent(final ContentNode first, final ContentNode second) {
		final Deque<ContentNode> firstParents = first.getAllParents();
		final Deque<ContentNode> secondParents = second.getAllParents();

		ContentNode nearestCommonParent = null;
		while (!firstParents.isEmpty() && firstParents.peekFirst() == secondParents.peekFirst()) {
			nearestCommonParent = firstParents.removeFirst();
			secondParents.removeFirst();
		}

		return nearestCommonParent;
	}
}
