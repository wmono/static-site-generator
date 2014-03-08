package com.vdxp.ssg.processor;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.io.FileTemplateLoader;
import com.github.jknack.handlebars.io.StringTemplateSource;
import com.github.jknack.handlebars.io.TemplateLoader;
import com.github.jknack.handlebars.io.TemplateSource;
import com.vdxp.ssg.content.BinaryContentFile;
import com.vdxp.ssg.content.ContentDirectory;
import com.vdxp.ssg.content.ContentNode;
import com.vdxp.ssg.content.ContentVisitor;
import com.vdxp.ssg.content.TextContentFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

public class HandlebarsLayoutProcessor {

	private static final Logger log = LoggerFactory.getLogger(HandlebarsLayoutProcessor.class);

	private final Handlebars handlebars;
	private final TemplateLoader layoutTemplateLoader;

	public HandlebarsLayoutProcessor() {
		// TODO ContentNodeTemplateLoader
		layoutTemplateLoader = new FileTemplateLoader("layout", "");
		this.handlebars = new Handlebars();
	}

	public void process(final ContentNode contentTree) {
		final Deque<LayoutRequest> queue;
		final HandlebarsLayoutInitializeQueueVisitor visitor = new HandlebarsLayoutInitializeQueueVisitor();
		contentTree.accept(visitor);
		queue = visitor.getQueue();

		log.debug("Initial layout queue: {}", queue);

		while (!queue.isEmpty()) {
			final LayoutRequest request = queue.removeFirst();
			final Deque<LayoutRequest> newRequests = applyLayout(request);
			queue.addAll(newRequests);
		}
	}

	private Deque<LayoutRequest> applyLayout(final LayoutRequest request) {
		final Deque<LayoutRequest> queue = new ArrayDeque<LayoutRequest>();

		try {
			handlebars.with(new BodyPartialTemplateLoader(request.content.getText()), layoutTemplateLoader);
			final Template template = handlebars.compile(request.layout);
			final String output = template.apply(request.content.getData());
			request.content.setText(output);
		} catch (final IOException e) {
			log.error("Could not apply template {} to {}", request.layout, request.content, e);
		}

		return queue;
	}

	private static class HandlebarsLayoutInitializeQueueVisitor implements ContentVisitor {

		private final Deque<LayoutRequest> queue = new ArrayDeque<LayoutRequest>();

		public Deque<LayoutRequest> getQueue() {
			return queue;
		}

		@Override
		public void visit(final TextContentFile contentFile, final List<ContentNode> parents) {
			String layout = null;
			for (final ContentNode parent : parents) {
				layout = getNextLayout(parent, layout);
			}
			layout = getNextLayout(contentFile, layout);

			if (layout != null) {
				queue.addLast(new LayoutRequest(contentFile, layout));
			}
		}

		private static String getNextLayout(final ContentNode node, final String previousLayout) {
			final Object layout = node.getData().get("layout");
			if (layout instanceof String) {
				return (String) layout;
			}
			return previousLayout;
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

	private static class LayoutRequest {
		public final TextContentFile content;
		public final String layout;

		private LayoutRequest(final TextContentFile content, final String layout) {
			this.content = content;
			this.layout = layout;
		}

		@Override
		public String toString() {
			return this.content.toString() + "->" + this.layout;
		}
	}

	private static class BodyPartialTemplateLoader implements TemplateLoader {

		private static final IOException notFound = new IOException();

		private final String body;

		public BodyPartialTemplateLoader(final String body) {
			this.body = body;
		}

		@Override
		public TemplateSource sourceAt(final String location) throws IOException {
			if (location.equals("body")) {
				return new StringTemplateSource("body", body);
			} else {
				throw notFound;
			}
		}

		@Override
		public String resolve(final String location) {
			return location;
		}

		@Override
		public String getPrefix() {
			return "";
		}

		@Override
		public String getSuffix() {
			return "";
		}
	}

}
