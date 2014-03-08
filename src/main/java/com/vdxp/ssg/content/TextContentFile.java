package com.vdxp.ssg.content;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

public abstract class TextContentFile extends ContentFile {

	private String text;

	public TextContentFile(final String basename, final String... extensions) {
		super(basename, extensions);
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
	protected void accept(final ContentVisitor visitor, final List<ContentNode> parents) {
		visitor.visit(this, parents);
	}

}
