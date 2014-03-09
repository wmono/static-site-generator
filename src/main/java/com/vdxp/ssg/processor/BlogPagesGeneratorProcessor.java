package com.vdxp.ssg.processor;

import com.joestelmach.natty.DateGroup;
import com.joestelmach.natty.Parser;
import com.vdxp.ssg.content.BinaryContentFile;
import com.vdxp.ssg.content.ContentDirectory;
import com.vdxp.ssg.content.ContentNode;
import com.vdxp.ssg.content.ContentVisitor;
import com.vdxp.ssg.content.TextContentFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class BlogPagesGeneratorProcessor {

	public ContentNode process(final ContentNode contentTree) {
		final List<TextContentFile> pages = getPages(contentTree);
		return generateBlogPages(pages);
	}

	private static List<TextContentFile> getPages(final ContentNode contentTree) {
		final BlogPageCollectionVisitor visitor = new BlogPageCollectionVisitor();
		contentTree.accept(visitor);
		return visitor.getPages();
	}

	private static ContentNode generateBlogPages(final List<TextContentFile> pages) {
		Collections.sort(pages, new ContentNodeDateComparator());
		return null;
	}

	private static class ContentNodeDateComparator implements Comparator<ContentNode> {

		private static final Logger log = LoggerFactory.getLogger(ContentNodeDateComparator.class);

		private final Parser natty = new Parser();

		@Override
		public int compare(final ContentNode left, final ContentNode right) {
			return -getDateMillis(left).compareTo(getDateMillis(right));
		}

		private Long getDateMillis(final ContentNode node) {
			final long defaultDateMillis = System.currentTimeMillis();

			final Object nodeDate = node.getData().get("date");
			if (!(nodeDate instanceof String)) {
				log.debug("Unrecognized date {} in {}", nodeDate, node);
				return defaultDateMillis;
			}

			final List<DateGroup> parsedDateGroups = natty.parse((String) nodeDate);
			if (parsedDateGroups.isEmpty()) {
				log.debug("Unrecognized date {} in {}", nodeDate, node);
				return defaultDateMillis;
			}

			final DateGroup parsedDateGroup = parsedDateGroups.get(0);
			final List<Date> parsedDates = parsedDateGroup.getDates();
			if (parsedDates.isEmpty()) {
				log.debug("Unrecognized date {} in {}", nodeDate, node);
				return defaultDateMillis;
			}

			final Date parsedDate = parsedDates.get(0);
			log.debug("Date {} in {} parsed as {}", nodeDate, node, parsedDate);
			return parsedDate.getTime();
		}
	}

	private static class BlogPageCollectionVisitor implements ContentVisitor {
		private final List<TextContentFile> pages = new ArrayList<TextContentFile>();

		@Override
		public void visit(final TextContentFile contentFile, final List<ContentNode> parents) {
			pages.add(contentFile);
		}

		@Override
		public void visit(final BinaryContentFile contentFile, final List<ContentNode> parents) {
			/* Do nothing */
		}

		@Override
		public void visit(final ContentDirectory contentDirectory, final List<ContentNode> parents) {
			/* Do nothing */
		}

		public List<TextContentFile> getPages() {
			return pages;
		}
	}

}
