package com.vdxp.ssg.processor;

import com.esotericsoftware.yamlbeans.YamlException;
import com.esotericsoftware.yamlbeans.YamlReader;
import com.vdxp.ssg.content.BinaryContentFile;
import com.vdxp.ssg.content.ContentDirectory;
import com.vdxp.ssg.content.ContentNode;
import com.vdxp.ssg.content.ContentVisitor;
import com.vdxp.ssg.content.TextContentFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class YamlFrontMatterProcessor {

	private static final Logger log = LoggerFactory.getLogger(YamlFrontMatterProcessor.class);

	public void process(final ContentNode content) {
		content.accept(new YamlFrontMatterVisitor());
	}

	private class YamlFrontMatterVisitor implements ContentVisitor {

		@Override
		public void visit(final TextContentFile contentFile, final List<ContentNode> parents) {
			final String[] contentParts = contentFile.getText().split("(?m)^---$\\s*", 3);

			if (contentParts.length < 3 || !contentParts[0].isEmpty()) {
				log.debug("Skipping {}: No YFM detected", contentFile);
				return;
			}

			try {
				final YamlReader reader = new YamlReader(contentParts[1]);
				final Object yfmObject = reader.read();

				if (yfmObject instanceof Map) {
					@SuppressWarnings("unchecked")
					final Map<String, Object> data = (Map<String, Object>) yfmObject;

					log.debug("Pushing in YFM in {}: {}", contentFile, data);
					contentFile.putData(data);
					contentFile.setText(contentParts[2]);
				} else {
					log.warn("Ignoring unexpected YFM in {}: {}", contentFile, yfmObject);
				}
			} catch (final YamlException e) {
				log.warn("{} appeared to contain YFM but it couldn't be parsed", contentFile, e);
			}
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
