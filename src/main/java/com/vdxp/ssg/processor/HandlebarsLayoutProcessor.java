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

	public void process(final ContentDirectory contentTree, final ContentDirectory layoutContentTree) {
		contentTree.accept(new HandlebarsLayoutVisitor(layoutContentTree));
	}

	private static class HandlebarsLayoutVisitor implements ContentVisitor {

		private final Handlebars handlebars;

		private final ContentDirectory layoutContentTree;

		public HandlebarsLayoutVisitor(final ContentDirectory layoutContentTree) {
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
					log.debug("Next layout for {} is {}", contentFile, nextLayout);
					return (String) nextLayout;
				}
				nextNode = nextNode.getParent();
			}
			log.debug("No more layouts for {}", contentFile);
			return null;
		}

		private void applyLayouts(final TextContentFile content, final String layoutPath) {
			if (layoutPath == null) {
				return;
			}

			final TextContentFile layoutContentFile = getLayoutByPath(layoutPath, layoutContentTree);
			if (layoutContentFile == null) {
				log.warn("Could not apply layout {} to {}", layoutPath, content);
				return;
			}

			final Map<String, Object> contentContextMap = new HashMap<String, Object>();
			contentContextMap.put("content", content.getText());
			final Context context = Context.newBuilder(content.getData()).combine(contentContextMap).build();

			try {
				log.debug("Applying layout {} to {}", layoutContentFile, content);

				final TemplateSource layoutTemplateSource = new StringTemplateSource(layoutPath, layoutContentFile.getText());
				final Template layoutTemplate = handlebars.compile(layoutTemplateSource);

				final String newText = layoutTemplate.apply(context);
				content.setText(newText);

				final String nextLayoutPath = computeLayoutForContent(layoutContentFile);
				applyLayouts(content, nextLayoutPath);
			} catch (final IOException e) {
				log.warn("Could not apply layout {} to {}", layoutContentFile, content, e);
			}
		}

		private static TextContentFile getLayoutByPath(final String layoutPath, final ContentDirectory contentTree) {
			final ContentNode contentNode = contentTree.getPath(layoutPath);
			if (contentNode == null) {
				log.warn("Layout path {} not found in content tree", layoutPath);
				return null;
			}
			if (!(contentNode instanceof TextContentFile)) {
				log.warn("Layout path {} found but is not a layout: {}", layoutPath, contentNode);
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
