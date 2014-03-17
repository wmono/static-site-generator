package com.vdxp.ssg.processor;

import com.google.common.base.Strings;
import com.vdxp.ssg.content.BinaryContentFile;
import com.vdxp.ssg.content.ContentDirectory;
import com.vdxp.ssg.content.ContentNode;
import com.vdxp.ssg.content.ContentVisitor;
import com.vdxp.ssg.content.TextContentFile;

import java.util.List;

public class RootPathProcessor {

	public void process(final ContentNode contentTree) {
		contentTree.accept(new RootPathVisitor());
	}

	private class RootPathVisitor implements ContentVisitor {

		@Override
		public void visit(final ContentDirectory contentDirectory, final List<ContentNode> parents) {
		/* Do nothing */
		}

		@Override
		public void visit(final BinaryContentFile contentFile, final List<ContentNode> parents) {
		/* Do nothing */
		}

		@Override
		public void visit(final TextContentFile contentFile, final List<ContentNode> parents) {
			final String rootPath = Strings.repeat("../", parents.size() - 1);
			contentFile.putData("rootPath", rootPath);
		}

	}
}
