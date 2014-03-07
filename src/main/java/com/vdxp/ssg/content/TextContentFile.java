package com.vdxp.ssg.content;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.Deque;

public abstract class TextContentFile extends ContentFile {

	private String text;

	public TextContentFile(final String name) {
		super(name);
	}

	public String getText() {
		return text;
	}

	public void setText(final String text) {
		this.text = text;
	}

	@Override
	public InputStream getContents() {
		return new ByteArrayInputStream(text.getBytes());
	}

	@Override
	protected void accept(final ContentVisitor visitor, final Deque<ContentNode> parents) {
		visitor.visit(this, new ArrayDeque<ContentNode>(parents));
	}

}
