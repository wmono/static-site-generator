package com.vdxp.ssg.content;

import java.util.ArrayDeque;
import java.util.Deque;

public abstract class BinaryContentFile extends ContentFile {

	public BinaryContentFile(final String name) {
		super(name);
	}

	@Override
	protected void accept(final ContentVisitor visitor, final Deque<ContentNode> parents) {
		visitor.visit(this, new ArrayDeque<ContentNode>(parents));
	}

}
