package com.vdxp.ssg.content;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.List;

public class ContentDirectory extends ContentNode {

	private final List<ContentNode> children = new ArrayList<ContentNode>();

	public ContentDirectory(final String name) {
		super(name);
	}

	public void addChild(final ContentNode child) {
		child.setParent(this);
		this.children.add(child);
	}

	public void removeChild(final ContentNode child) {
		if (child.getParent() != this) {
			throw new IllegalStateException("That child node does not belong to this directory");
		}

		final boolean removed = children.remove(child);
		if (!removed) {
			throw new IllegalStateException("That child node did not belong to this directory");
		}

		child.clearParent();
	}

	public Collection<ContentNode> getChildren() {
		return new ArrayList<ContentNode>(children);
	}

	public void merge(final ContentDirectory donor) {
		if (donor.getParent() != null) {
			throw new IllegalStateException("Donor ContentDirectory must not be attached to a content tree");
		}

		for (final ContentNode child : donor.children) {
			child.clearParent();
			child.setParent(this);
		}
		children.addAll(donor.children);
		donor.children.clear();
	}

	@Override
	protected void accept(final ContentVisitor visitor, final Deque<ContentNode> parents) {
		visitor.visit(this, new ArrayDeque<ContentNode>(parents));

		final Deque<ContentNode> childParents = new ArrayDeque<ContentNode>(parents);
		childParents.add(this);
		for (final ContentNode child : children) {
			child.accept(visitor, childParents);
		}
	}

}
