package com.vdxp.ssg.content;

import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ContentDirectory extends ContentNode {

	private final List<ContentNode> children = new ArrayList<ContentNode>();

	private String name;

	public ContentDirectory(final String name) {
		this.name = name;
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

	@Override
	protected ContentNode getChildByName(final String name) {
		for (final ContentNode child : children) {
			if (name.equals(child.getName())) {
				return child;
			}
		}
		return null;
	}

	public Collection<ContentNode> getChildren() {
		return ImmutableList.copyOf(children);
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
	protected void accept(final ContentVisitor visitor, final List<ContentNode> parents) {
		visitor.visit(this, ImmutableList.copyOf(parents));

		List<ContentNode> childParents = new ArrayList<ContentNode>(parents);
		childParents.add(this);
		childParents = ImmutableList.copyOf(childParents);

		for (final ContentNode child : children) {
			child.accept(visitor, childParents);
		}
	}

	@Override
	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

}
