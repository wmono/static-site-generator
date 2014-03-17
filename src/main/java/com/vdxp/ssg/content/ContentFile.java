package com.vdxp.ssg.content;

import com.google.common.base.Joiner;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;

public abstract class ContentFile extends ContentNode {

	private String basename;
	private Deque<String> extensions;

	public ContentFile(final String basename, final String... extensions) {
		this.basename = basename;
		this.extensions = new ArrayDeque<String>(Arrays.asList(extensions));
	}

	public abstract String getSource();
	public abstract InputStream getContents() throws IOException;

	@Override
	public String getName() {
		final StringBuilder sb = new StringBuilder(getBasename());
		if (!extensions.isEmpty()) {
			sb.append('.');
			Joiner.on('.').appendTo(sb, extensions);
		}
		return sb.toString();
	}

	public String getBasename() {
		return basename;
	}

	public void setBasename(final String basename) {
		this.basename = basename;
	}

	public Deque<String> getExtensions() {
		return extensions;
	}

	public void setExtensions(final Deque<String> extensions) {
		this.extensions = extensions;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "{" + getSource() + "}";
	}

}
