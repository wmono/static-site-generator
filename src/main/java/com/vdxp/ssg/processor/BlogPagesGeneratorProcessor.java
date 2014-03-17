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

	public ContentNode process(final ContentNode contentTree) {
		final List<TextContentFile> pages = getPages(contentTree);
		return generateBlogPages(pages);
	}

	private static List<TextContentFile> getPages(final ContentNode contentTree) {
		final BlogPageCollectionVisitor visitor = new BlogPageCollectionVisitor();
		contentTree.accept(visitor);
		return visitor.getPages();
	}

	private ContentNode generateBlogPages(final List<TextContentFile> posts) {
		final ContentDirectory blogPages = new ContentDirectory("blog");

		Collections.sort(posts, new ContentNodeDateComparator());

		final int numPages = divideRoundUp(posts.size(), options.numberOfPostsPerPage);
		for (int pageNum = 1; pageNum <= numPages; pageNum++) {
			/* pageNum is 1=indexed for human consumption */
			final int minIndex = (pageNum - 1) * options.numberOfPostsPerPage;
			final int maxIndex = Math.min(pageNum * options.numberOfPostsPerPage, posts.size());
			final List<TextContentFile> pageSlice = posts.subList(minIndex, maxIndex);
			blogPages.addChild(generateBlogPage(pageSlice, pageNum, numPages)); // TODO place in tree
		}

		return blogPages;
	}

	private TextContentFile generateBlogPage(final List<TextContentFile> posts, final int pageNumber, final int numberOfPages) {
		// FIXME This makes assumptions about what the Driver will do with the ContentNode that is produced
		final String thisUrl = options.getPagePath(pageNumber);
		final String previousUrl = (pageNumber > 1) ? relativePath(thisUrl, options.getPagePath(pageNumber - 1)) : null;
		final String nextUrl = (pageNumber < numberOfPages) ? relativePath(thisUrl, options.getPagePath(pageNumber + 1)) : null;

		final List<Map<String, Object>> pagesContentList = new ArrayList<Map<String, Object>>();
		for (final TextContentFile page : posts) {
			final Map<String, Object> pageContentMap = new HashMap<String, Object>();
			pageContentMap.put("text", page.getText());
			pageContentMap.putAll(page.getData());
			pagesContentList.add(pageContentMap);
		}

		final Map<String, Object> blogPageData = new HashMap<String, Object>();
		blogPageData.put("thisUrl", thisUrl);
		blogPageData.put("previousUrl", previousUrl);
		blogPageData.put("nextUrl", nextUrl);
		blogPageData.put("posts", pagesContentList);

		final Map<String, Object> data = new HashMap<String, Object>();
		data.put("blogPage", blogPageData);
		data.put("layout", "blogPage.hbs"); // TODO parametrize

		final BlogPageContentFile blogPage = new BlogPageContentFile(pageNumber, "index-" + pageNumber, "html"); // FIXME
		blogPage.putData(data);
		log.debug("Generated {}", blogPage);
		return blogPage;
	}

	private static String relativePath(final String currentPath, final String targetPath) {
		// TODO
		return "(not implemented)";
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

	private static int divideRoundUp(final int numerator, final int denominator) {
		return (numerator + denominator - 1) / denominator;
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

		final int pageNumber;

		public BlogPageContentFile(final int pageNumber, final String basename, final String... extensions) {
			super(basename, extensions);
			this.pageNumber = pageNumber;
		}

		@Override
		public String getSource() {
			return "Generated blog page " + pageNumber;
		}

	}

}
