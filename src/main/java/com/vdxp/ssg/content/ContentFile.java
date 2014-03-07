package com.vdxp.ssg.content;

import java.util.ArrayDeque;
import java.util.Deque;

public abstract class ContentFile extends ContentNode {

	private String content;

	public ContentFile(final String name) {
		super(name);
	}

	public abstract String getSource();

	@Override
	protected void accept(final ContentVisitor visitor, final Deque<ContentNode> parents) {
		visitor.visit(this, new ArrayDeque<ContentNode>(parents));
	}

	public String getContent() {
		return content;
	}

	public void setContent(final String content) {
		this.content = content;
	}

}
