package com.vdxp.ssg.processor;

import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.io.StringTemplateSource;
import com.github.jknack.handlebars.io.TemplateSource;
import com.vdxp.ssg.content.BinaryContentFile;
import com.vdxp.ssg.content.ContentDirectory;
import com.vdxp.ssg.content.ContentNode;
import com.vdxp.ssg.content.ContentVisitor;
import com.vdxp.ssg.content.TextContentFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HandlebarsLayoutProcessor {

	private static final Logger log = LoggerFactory.getLogger(HandlebarsLayoutProcessor.class);

	public void process(final ContentNode contentTree, final ContentNode layoutContentTree) {
		contentTree.accept(new HandlebarsLayoutVisitor(layoutContentTree));
	}

	private static class HandlebarsLayoutVisitor implements ContentVisitor {

		private final Handlebars handlebars;

		private final ContentNode layoutContentTree;

		public HandlebarsLayoutVisitor(final ContentNode layoutContentTree) {
			this.handlebars = new Handlebars();
			this.layoutContentTree = layoutContentTree;
		}

		@Override
		public void visit(final TextContentFile contentFile, final List<ContentNode> parents) {
			final String layout = computeLayoutForContent(contentFile);
			if (layout != null) {
				applyLayouts(contentFile, layout);
			}
		}

		private static String computeLayoutForContent(final TextContentFile contentFile) {
			ContentNode nextNode = contentFile;

			while (nextNode != null) {
				final Object nextLayout = nextNode.getData().get("layout");
				if (nextLayout instanceof String) {
					return (String) nextLayout;
				}
				nextNode = nextNode.getParent();
			}
			return null;
		}

		private void applyLayouts(final TextContentFile content, final String layoutPath) {
			if (layoutPath == null) {
				return;
			}

			final TextContentFile templateContentFile = getTemplateByPath(layoutPath, layoutContentTree);
			if (templateContentFile == null) {
				return;
			}

			final Map<String, Object> contentContextMap = new HashMap<String, Object>();
			contentContextMap.put("content", content.getText());
			final Context context = Context.newBuilder(content.getData()).combine(contentContextMap).build();

			try {
				log.debug("Applying template {} to {}", templateContentFile, content);

				final TemplateSource layoutTemplateSource = new StringTemplateSource(layoutPath, templateContentFile.getText());
				final Template layoutTemplate = handlebars.compile(layoutTemplateSource);

				final String newText = layoutTemplate.apply(context);
				content.setText(newText);

				final String nextTemplate = computeLayoutForContent(templateContentFile);
				applyLayouts(content, nextTemplate);
			} catch (final IOException e) {
				log.warn("Could not apply template {} to {}", templateContentFile, content, e);
			}
		}

		private static TextContentFile getTemplateByPath(final String path, final ContentNode contentTree) {
			final ContentNode contentNode = contentTree.getPath(path);
			if (contentNode == null) {
				log.warn("Template path {} not found in content tree", path);
				return null;
			}
			if (!(contentNode instanceof TextContentFile)) {
				log.warn("Template path {} found but is not a template: {}", path, contentNode);
				return null;
			}
			return (TextContentFile) contentNode;
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
