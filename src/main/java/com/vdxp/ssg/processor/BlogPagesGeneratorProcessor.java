package com.vdxp.ssg.processor;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BlogPagesGeneratorProcessor {

	private static final Logger log = LoggerFactory.getLogger(BlogPagesGeneratorProcessor.class);

	private final Options options;

	public BlogPagesGeneratorProcessor() {
		this.options = new Options();
	}

	public BlogPagesGeneratorProcessor(final Options options) {
		this.options = options;
	}

	public ContentDirectory process(final ContentNode contentTree) {
		final List<TextContentFile> contentPages = getPages(contentTree);
		final List<BlogPageContentFile> blogPages = generateBlogPages(contentPages);

		final ContentDirectory content = new ContentDirectory("blog");
		for (final BlogPageContentFile page : blogPages) {
			content.addChild(page);
		}

		return content;
	}

	private static List<TextContentFile> getPages(final ContentNode contentTree) {
		final BlogPageCollectionVisitor visitor = new BlogPageCollectionVisitor();
		contentTree.accept(visitor);
		return visitor.getPages();
	}

	private List<BlogPageContentFile> generateBlogPages(final List<TextContentFile> contentPages) {
		final ArrayList<BlogPageContentFile> blogPages = new ArrayList<BlogPageContentFile>();

		Collections.sort(contentPages, new ContentNodeDateComparator());

		final int numPages = divideRoundUp(contentPages.size(), options.numberOfPostsPerPage);
		for (int pageNumber = 1; pageNumber <= numPages; pageNumber++) {
			/* pageNum is 1=indexed for human consumption */
			final int minIndex = (pageNumber - 1) * options.numberOfPostsPerPage;
			final int maxIndex = Math.min(pageNumber * options.numberOfPostsPerPage, contentPages.size());
			final List<TextContentFile> pageSlice = contentPages.subList(minIndex, maxIndex);
			blogPages.add(generateBlogPage(pageSlice, pageNumber));
		}

		return blogPages;
	}

	private static BlogPageContentFile generateBlogPage(final List<TextContentFile> posts, final int pageNumber) {
		final List<Map<String, Object>> pageContentList = new ArrayList<Map<String, Object>>();
		for (final TextContentFile post : posts) {
			final Map<String, Object> postContentMap = new HashMap<String, Object>();
			postContentMap.put("text", post.getText());
			postContentMap.putAll(post.getData());
			pageContentList.add(postContentMap);
		}

		final Map<String, Object> blogPageData = new HashMap<String, Object>();
		blogPageData.put("posts", pageContentList);

		final Map<String, Object> pageData = new HashMap<String, Object>();
		pageData.put("blogPage", blogPageData);
		pageData.put("layout", "blogPage.hbs");

		final BlogPageContentFile blogPage = new BlogPageContentFile(pageNumber);
		blogPage.putData(pageData);
		log.debug("Generated {}", blogPage);
		return blogPage;
	}

	private static class BlogPageCollectionVisitor implements ContentVisitor {
		private final List<TextContentFile> pages = new ArrayList<TextContentFile>();

		@Override
		public void visit(final TextContentFile contentFile, final List<ContentNode> parents) {
			log.debug("{} needs a home", contentFile);
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

	public static class Options {
		public final int numberOfPostsPerPage;
		public final String firstPagePattern;
		public final String pagePattern;

		private static final int defaultNumberOfPostsPerPage = 3;
		private static final String defaultFirstPagePattern = "index";
		private static final String defaultPagePattern = "page/%d/index";

		public Options() {
			this(defaultNumberOfPostsPerPage, defaultPagePattern, defaultFirstPagePattern);
		}

		public Options (final int numberOfPostsPerPage) {
			this(numberOfPostsPerPage, defaultPagePattern, defaultFirstPagePattern);
		}

		public Options (final int numberOfPostsPerPage, final String pagePattern) {
			this(numberOfPostsPerPage, pagePattern, defaultFirstPagePattern);
		}

		public Options(final int numberOfPostsPerPage, final String pagePattern, final String firstPagePattern) {
			this.numberOfPostsPerPage = numberOfPostsPerPage;
			this.pagePattern = pagePattern;
			this.firstPagePattern = firstPagePattern;
		}

		private String getPagePath(final int pageNumber) {
			if (pageNumber == 0) {
				return String.format(firstPagePattern, pageNumber);
			} else {
				return String.format(pagePattern, pageNumber);
			}
		}
	}

	private static class BlogPageContentFile extends TextContentFile {
		private final int pageNumber;

		public BlogPageContentFile(final int pageNumber) {
			super(".unnamed-blog-page-" + pageNumber, "html");
			this.pageNumber = pageNumber;
		}

		@Override
		public String getSource() {
			return "Generated blog page " + pageNumber;
		}
	}

	private static class ContentNodeDateComparator implements Comparator<ContentNode> {
		@Override
		public int compare(final ContentNode left, final ContentNode right) {
			final Object leftDateObject = left.getData().get("date_raw");
			final Object rightDateObject = right.getData().get("date_raw");
			final Long leftDate = leftDateObject instanceof Long ? (Long) leftDateObject : Long.MAX_VALUE;
			final Long rightDate = rightDateObject instanceof Long ? (Long) rightDateObject : Long.MAX_VALUE;
			return rightDate.compareTo(leftDate);
		}
	}

	private static int divideRoundUp(final int numerator, final int denominator) {
		return (numerator + denominator - 1) / denominator;
	}

}
