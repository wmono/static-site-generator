package com.vdxp.ssg.content;

import java.util.List;

public abstract class BinaryContentFile extends ContentFile {

	public BinaryContentFile(final String basename, final String... extensions) {
		super(basename, extensions);
	}

	@Override
	protected void accept(final ContentVisitor visitor, final List<ContentNode> parents) {
		visitor.visit(this, parents);
	}

}
