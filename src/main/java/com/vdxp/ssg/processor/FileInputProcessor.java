package com.vdxp.ssg.processor;

import com.vdxp.ssg.content.ContentDirectory;
import com.vdxp.ssg.content.ContentFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class FileInputProcessor {

	private static final Logger log = LoggerFactory.getLogger(FileInputProcessor.class);

	public ContentDirectory readContentRoot(final String contentRootPath) throws IOException {
		final File contentRootFile = new File(contentRootPath);
		if (contentRootFile.isDirectory()) {
			return readDirectory(contentRootFile);
		}
		if (contentRootFile.exists()) {
			throw new IOException("Content root " + contentRootFile.getAbsolutePath() + " is not a directory");
		} else {
			throw new IOException("Content root " + contentRootFile.getAbsolutePath() + " does not exist");
		}
	}

	public ContentDirectory readDirectory(final File directory) {
		final ContentDirectory contentDirectory = new ContentDirectory(directory.getName());

		final File[] directoryEntries = directory.listFiles();
		if (directoryEntries == null) {
			log.error("Unable to read directory {}", directory.getPath());
			return contentDirectory;
		}

		for (final File entry : directoryEntries) {
			try {
				if (entry.isFile()) {
					final ContentFile entryFile = makeContentFile(entry);
					contentDirectory.addChild(entryFile);
				}
				if (entry.isDirectory()) {
					final ContentDirectory entryDirectory = readDirectory(entry);
					contentDirectory.addChild(entryDirectory);
				}
			} catch (final IOException e) {
				log.error("Unable to read content file {}, skipping", entry.getPath(), e);
			}
		}

		return contentDirectory;
	}

	public ContentFile makeContentFile(final File file) throws IOException {
		final ContentFile content = new FileInputContentFile(file);

		final FileReader fileReader = new FileReader(file);
		final StringBuilder sb = new StringBuilder();
		final char[] buf = new char[8192];

		while (fileReader.read(buf) != -1) {
			sb.append(buf);
		}

		content.setContent(sb.toString());
		return content;
	}

	public class FileInputContentFile extends ContentFile {

		private final File sourceFile;

		public FileInputContentFile(final File sourceFile) {
			super(sourceFile.getName());
			this.sourceFile = sourceFile;
		}

		public File getSourceFile() {
			return sourceFile;
		}

		@Override
		public String getSource() {
			return "File " + sourceFile.getPath();
		}

	}

}

