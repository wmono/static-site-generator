package com.vdxp.ssg.content;

import com.google.common.collect.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ContentDirectory extends ContentNode {

	private static final Logger log = LoggerFactory.getLogger(ContentDirectory.class);

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

	private ContentNode getChildByName(final String name) {
		for (final ContentNode child : children) {
			if (name.equals(child.getName())) {
				return child;
			}
		}
		return null;
	}

	public ContentNode getPath(final String path) {
		final String[] pathParts = path.split("/", 2);
		if (pathParts.length == 0) {
			log.warn("Attempting to get empty path", new Exception());
			return null;
		}
		if (pathParts.length == 1) {
			return getChildByName(pathParts[0]);
		}
		if (pathParts.length == 2) {
			final ContentNode child = getChildByName(pathParts[0]);

			if (child instanceof ContentDirectory) {
				return ((ContentDirectory) child).getPath(pathParts[1]);
			} else {
				return null;
			}
		}
		throw new IndexOutOfBoundsException("split returned too many elements");
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
