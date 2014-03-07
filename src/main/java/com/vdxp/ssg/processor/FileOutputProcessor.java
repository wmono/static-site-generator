package com.vdxp.ssg.processor;

import com.vdxp.ssg.content.BinaryContentFile;
import com.vdxp.ssg.content.ContentDirectory;
import com.vdxp.ssg.content.ContentFile;
import com.vdxp.ssg.content.ContentNode;
import com.vdxp.ssg.content.ContentVisitor;
import com.vdxp.ssg.content.TextContentFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
		public void visit(final TextContentFile contentFile, final Deque<ContentNode> parents) {
			visit((ContentFile) contentFile, parents);
		}

		@Override
		public void visit(final BinaryContentFile contentFile, final Deque<ContentNode> parents) {
			visit((ContentFile) contentFile, parents);
		}

		public void visit(final ContentFile contentFile, final Deque<ContentNode> parents) {
			final String filePath = makeFilePath(parents, contentFile);
			final File file = new File(filePath);
			if (file.exists() && !file.isFile()) {
				log.error("The path {} already exists but is not a file.", file.getAbsolutePath());
				return;
			}

			log.debug("Writing {} to {}", contentFile.getSource(), filePath);

			final InputStream input = makeInputStream(contentFile);
			final FileOutputStream output = makeFileOutputStream(file);

			if (input == null || output == null) {
				return;
			}

			final byte[] buf = new byte[8192];

			try {
				int length = 0;
				while ((length = input.read(buf)) != -1) {
					output.write(buf, 0, length);
				}
			} catch (final IOException e) {
				log.error("Could not write file {}", file.getAbsolutePath(), e);
			} finally {
				try {
					input.close();
				} catch (final IOException e) {
					log.error("Could not close input {}", contentFile.getSource(), e);
				}
				try {
					output.close();
				} catch (final IOException e) {
					log.error("Could not close output file {}", file.getAbsolutePath(), e);
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

		private InputStream makeInputStream(final ContentFile file) {
			try {
				return file.getContents();
			} catch (final IOException e) {
				log.error("Could not open input {}", file.getSource(), e);
				return null;
			}
		}

		private FileOutputStream makeFileOutputStream(final File file) {
			try {
				return new FileOutputStream(file);
			} catch (final FileNotFoundException e) {
				log.error("Could not open file {}", file.getAbsolutePath(), e);
				return null;
			}
		}
	}

}
