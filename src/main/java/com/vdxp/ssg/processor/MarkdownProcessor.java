package com.vdxp.ssg.processor;

import com.google.common.collect.ImmutableList;
import com.vdxp.ssg.content.BinaryContentFile;
import com.vdxp.ssg.content.ContentDirectory;
import com.vdxp.ssg.content.ContentNode;
import com.vdxp.ssg.content.ContentVisitor;
import com.vdxp.ssg.content.TextContentFile;
import org.pegdown.Extensions;
import org.pegdown.PegDownProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Deque;
import java.util.List;

public class MarkdownProcessor {

	private static final Logger log = LoggerFactory.getLogger(MarkdownProcessor.class);

	public static final List<String> markdownExtensions = ImmutableList.of("md", "markdown");

	private final PegDownProcessor pegdown;

	public MarkdownProcessor() {
		final int pegdownOptions = Extensions.AUTOLINKS | Extensions.FENCED_CODE_BLOCKS | Extensions.STRIKETHROUGH | Extensions.TABLES;
		pegdown = new PegDownProcessor(pegdownOptions);
	}

	public void process(final ContentNode content) {
		final MarkdownVisitor visitor = new MarkdownVisitor();
		content.accept(visitor);
	}

	private class MarkdownVisitor implements ContentVisitor {

		// TODO reschedule processor if this is set
		private boolean reschedule = false;

		public boolean getReschedule() {
			return reschedule;
		}

		@Override
		public void visit(final TextContentFile contentFile, final List<ContentNode> parents) {
			final Deque<String> extensions = contentFile.getExtensions();

			if (Collections.disjoint(extensions, markdownExtensions)) {
				log.debug("Skipping {} ({})", contentFile, extensions);
				return;
			}
			if (!markdownExtensions.contains(extensions.peekLast())) {
				log.debug("Rescheduling {} ({})", contentFile, extensions);
				reschedule = true;
				return;
			}

			log.debug("Converting {} to HTML", contentFile);
			contentFile.setText(pegdown.markdownToHtml(contentFile.getText()));
			extensions.removeLast();
			extensions.addLast("html");
		}

		@Override
		public void visit(final ContentDirectory contentDirectory, final List<ContentNode> parents) {
			/* Do nothing */
		}

		@Override
		public void visit(final BinaryContentFile contentFile, final List<ContentNode> parents) {
			/* Do nothing */
		}

	}
}
