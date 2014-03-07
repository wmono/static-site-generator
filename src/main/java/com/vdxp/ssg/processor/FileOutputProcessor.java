package com.vdxp.ssg.processor;

import com.vdxp.ssg.content.ContentDirectory;
import com.vdxp.ssg.content.ContentFile;
import com.vdxp.ssg.content.ContentNode;
import com.vdxp.ssg.content.ContentVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Deque;

public class FileOutputProcessor {

	private static final Logger log = LoggerFactory.getLogger(FileOutputProcessor.class);

	public void writeContentRoot(final ContentNode contentNode) {
		contentNode.accept(new FileOutputVisitor());
	}

	public class FileOutputVisitor implements ContentVisitor {

		@Override
		public void visit(final ContentDirectory contentDirectory, final Deque<ContentNode> parents) {
			final String filePath = makeFilePath(parents, contentDirectory);
			final File file = new File(filePath);
			if (file.exists()) {
				if (!file.isDirectory()) {
					log.error("The path {} already exists but is not a directory.", file.getAbsolutePath());
				}
				return;
			}

			final boolean result = file.mkdir();
			if (!result) {
				log.warn("Could not create directory {}", file.getAbsolutePath());
			}
		}

		@Override
		public void visit(final ContentFile contentFile, final Deque<ContentNode> parents) {
			final String filePath = makeFilePath(parents, contentFile);
			final File file = new File(filePath);
			if (file.exists() && !file.isFile()) {
				log.error("The path {} already exists but is not a file.", file.getAbsolutePath());
				return;
			}

			final FileOutputStream output;

			try {
				output = new FileOutputStream(file);
			} catch (final FileNotFoundException e) {
				log.error("Could not open file {}", file.getAbsolutePath(), e);
				return;
			}

			try {
				output.write(contentFile.getContent().getBytes());
				output.close();
			} catch (final IOException e) {
				log.error("Could not write file {}", file.getAbsolutePath(), e);
			} finally {
				try {
					output.close();
				} catch (final IOException e) {
					log.error("Could not close file {}", file.getAbsolutePath(), e);
				}
			}
		}

		private String makeFilePath(final Collection<ContentNode> nodes, final ContentNode leaf) {
			final StringBuilder path = new StringBuilder();
			for (final ContentNode node : nodes) {
				path.append(File.separator);
				path.append(node.getName());
			}
			if (leaf != null) {
				path.append(File.separator);
				path.append(leaf.getName());
			}

			return path.toString().substring(File.separator.length());
		}
	}

}
