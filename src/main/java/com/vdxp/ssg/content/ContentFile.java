package com.vdxp.ssg.content;

import java.io.IOException;
import java.io.InputStream;

public abstract class ContentFile extends ContentNode {

	public ContentFile(final String name) {
		super(name);
	}

	public abstract String getSource();
	public abstract InputStream getContents() throws IOException;

}
