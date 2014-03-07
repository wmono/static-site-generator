package com.vdxp.ssg.content;

import java.util.Deque;

public interface ContentVisitor {
	/** Called by each content directory node. Do not modify the content tree structure. */
	public void visit(final ContentDirectory contentDirectory, final Deque<ContentNode> parents);
	/** Called by each binary content file node. Do not modify the content tree structure. */
	public void visit(final BinaryContentFile contentFile, final Deque<ContentNode> parents);
	/** Called by each text content file node. Do not modify the content tree structure. */
	public void visit(final TextContentFile contentFile, final Deque<ContentNode> parents);
}
