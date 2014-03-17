package com.vdxp.ssg.processor;

import com.vdxp.ssg.content.BinaryContentFile;
import com.vdxp.ssg.content.ContentDirectory;
import com.vdxp.ssg.content.ContentNode;
import com.vdxp.ssg.content.ContentVisitor;
import com.vdxp.ssg.content.TextContentFile;

import java.util.List;

public class SplitReadMoreProcessor {

	public void process(final ContentDirectory contentTree) {
		contentTree.accept(new SplitReadMoreVisitor());
	}

	private static class SplitReadMoreVisitor implements ContentVisitor {

		@Override
		public void visit(final TextContentFile contentFile, final List<ContentNode> parents) {
			final String contentFileText = contentFile.getText();

			final int readMoreIndex = getReadMoreIndex(contentFileText);
			if (readMoreIndex == -1) {
				return;
			}

			final String snippet = contentFileText.substring(0, readMoreIndex);
			contentFile.putData("snippet", snippet);
		}

		@Override
		public void visit(final ContentDirectory contentDirectory, final List<ContentNode> parents) {
			/* Do nothing */
		}

		@Override
		public void visit(final BinaryContentFile contentFile, final List<ContentNode> parents) {
			/* Do nothing */
		}

		private static int getReadMoreIndex(final String text) {
			int readMoreIndex = text.indexOf("<!--more-->");
			if (readMoreIndex == -1) {
				readMoreIndex = text.indexOf("<!-- more -->");
			}
			return readMoreIndex;
		}
	}
}
