package com.vdxp.ssg.processor;

import com.google.common.collect.ImmutableList;
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

public class BlogPagesGeneratorProcessor {

	private static final Logger log = LoggerFactory.getLogger(BlogPagesGeneratorProcessor.class);

	private final Options options;

	public BlogPagesGeneratorProcessor() {
		this.options = new Options();
	}

	public BlogPagesGeneratorProcessor(final Options options) {
		this.options = options;
	}

	public void process(final ContentDirectory contentTree) {
		final List<TextContentFile> contentPages = getPages(contentTree);
		final List<BlogPageContentFile> blogPages = generateBlogPages(contentPages, options);

		insertBlogPages(blogPages, contentTree, options);
		generateNavigationLinks(blogPages);
		populatePageDataMap(blogPages, options);
	}

	private static List<TextContentFile> getPages(final ContentNode contentTree) {
		final BlogPageCollectionVisitor visitor = new BlogPageCollectionVisitor();
		contentTree.accept(visitor);
		return visitor.getPages();
	}

	private static List<BlogPageContentFile> generateBlogPages(final List<TextContentFile> contentPages, final Options options) {
		final ArrayList<BlogPageContentFile> blogPages = new ArrayList<BlogPageContentFile>();

		Collections.sort(contentPages, new ContentNodeDateComparator());

		final int numPages = divideRoundUp(contentPages.size(), options.numberOfPostsPerPage);
		for (int pageNumber = 1; pageNumber <= numPages; pageNumber++) {
			/* pageNum is 1=indexed for human consumption */
			final int minIndex = (pageNumber - 1) * options.numberOfPostsPerPage;
			final int maxIndex = Math.min(pageNumber * options.numberOfPostsPerPage, contentPages.size());
			final List<TextContentFile> pageSlice = contentPages.subList(minIndex, maxIndex);
			blogPages.add(new BlogPageContentFile(pageNumber, pageSlice));
		}

		return blogPages;
	}

	private static void insertBlogPages(final List<BlogPageContentFile> blogPages, final ContentDirectory target, final Options options) {
		for (final BlogPageContentFile blogPage : blogPages) {
			final String pagePath = options.getPagePath(blogPage.pageNumber);

			log.debug("Putting {} into {}", blogPage, pagePath);

			final String pageBasename;
			final ContentDirectory pageDirectory;
			final int pagePathLastSlash = pagePath.lastIndexOf('/');
			if (pagePathLastSlash == -1) {
				pageBasename = pagePath;
				pageDirectory = target;
			} else {
				pageBasename = pagePath.substring(pagePathLastSlash + 1);
				pageDirectory = (ContentDirectory) target.getPath(pagePath.substring(0, pagePathLastSlash), true);
			}

			blogPage.setBasename(pageBasename);
			pageDirectory.addChild(blogPage);
		}
	}

	private static void generateNavigationLinks(final List<BlogPageContentFile> blogPages) {
		for (int i = 0; i < blogPages.size(); i++) {
			final BlogPageContentFile page = blogPages.get(i);

			if (i > 0) {
				page.setPreviousUrl(page.getRelativePath(blogPages.get(i - 1)));
			}
			if (i < blogPages.size() - 1) {
				page.setNextUrl(page.getRelativePath(blogPages.get(i + 1)));
			}
		}
	}

	private static void populatePageDataMap(final List<BlogPageContentFile> blogPages, final Options options) {
		for (final BlogPageContentFile page : blogPages) {
			final HashMap<String, Object> pageData = new HashMap<String, Object>();
			final HashMap<String, Object> blogData = new HashMap<String, Object>();
			pageData.put("blogPage", blogData);
			pageData.put("layout", options.layout);

			final List<HashMap<String, Object>> blogPagePostsList = new ArrayList<HashMap<String, Object>>();
			for (final TextContentFile post : page.getContentPages()) {
				final HashMap<String, Object> postData = new HashMap<String, Object>();
				postData.put("text", post.getText());
				postData.put("link", page.getRelativePath(post));
				postData.putAll(post.getData());
				blogPagePostsList.add(postData);
			}
			blogData.put("posts", blogPagePostsList);
			blogData.put("next", page.getNextUrl());
			blogData.put("previous", page.getPreviousUrl());

			page.putData(pageData);
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

	public static class Options {
		public final int numberOfPostsPerPage;
		public final String firstPagePattern;
		public final String pagePattern;
		public final String layout;

		private static final int defaultNumberOfPostsPerPage = 3;
		private static final String defaultFirstPagePattern = "index";
		private static final String defaultPagePattern = "page/%d/index";
		private static final String defaultLayout = "blogPage.hbs";

		public Options() {
			this(defaultNumberOfPostsPerPage);
		}

		public Options (final int numberOfPostsPerPage) {
			this(numberOfPostsPerPage, defaultPagePattern);
		}

		public Options (final int numberOfPostsPerPage, final String pagePattern) {
			this(numberOfPostsPerPage, pagePattern, defaultFirstPagePattern);
		}

		public Options (final int numberOfPostsPerPage, final String pagePattern, final String firstPagePattern) {
			this(numberOfPostsPerPage, pagePattern, firstPagePattern, defaultLayout);
		}

		public Options(final int numberOfPostsPerPage, final String pagePattern, final String firstPagePattern, final String layout) {
			this.numberOfPostsPerPage = numberOfPostsPerPage;
			this.pagePattern = pagePattern;
			this.firstPagePattern = firstPagePattern;
			this.layout = layout;
		}

		private String getPagePath(final int pageNumber) {
			if (pageNumber == 1) {
				return String.format(firstPagePattern, pageNumber);
			} else {
				return String.format(pagePattern, pageNumber);
			}
		}
	}

	private static class BlogPageContentFile extends TextContentFile {
		private final int pageNumber;
		private final List<TextContentFile> contentPages;
		private String nextUrl = null;
		private String previousUrl = null;

		public BlogPageContentFile(final int pageNumber, final List<TextContentFile> contentPages) {
			super(".unnamed-blog-page-" + pageNumber, "html");
			this.pageNumber = pageNumber;
			this.contentPages = contentPages;
		}

		@Override
		public String getSource() {
			return "Generated blog page " + pageNumber;
		}

		public int getPageNumber() {
			return pageNumber;
		}

		public List<TextContentFile> getContentPages() {
			return ImmutableList.copyOf(contentPages);
		}

		public String getNextUrl() {
			return nextUrl;
		}

		public void setNextUrl(final String nextUrl) {
			this.nextUrl = nextUrl;
		}

		public String getPreviousUrl() {
			return previousUrl;
		}

		public void setPreviousUrl(final String previousUrl) {
			this.previousUrl = previousUrl;
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
