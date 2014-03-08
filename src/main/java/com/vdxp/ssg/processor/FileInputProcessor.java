package com.vdxp.ssg.processor;

import com.vdxp.ssg.content.BinaryContentFile;
import com.vdxp.ssg.content.ContentDirectory;
import com.vdxp.ssg.content.ContentFile;
import com.vdxp.ssg.content.TextContentFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;

public class FileInputProcessor {

	private static final Logger log = LoggerFactory.getLogger(FileInputProcessor.class);

	private static final String[] textFileExtensions = {
			".hbs",
			".html",
			".md",
			".txt",
	};

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
			if (entry.isFile()) {
				if (entry.getName().startsWith(".")) {
					log.debug("Skipping dotfile {}", entry);
					continue;
				}
				log.debug("Loading {}", entry);
				final ContentFile entryFile = makeContentFile(entry);
				contentDirectory.addChild(entryFile);
			}
			if (entry.isDirectory()) {
				final ContentDirectory entryDirectory = readDirectory(entry);
				contentDirectory.addChild(entryDirectory);
			}
		}

		return contentDirectory;
	}

	public ContentFile makeContentFile(final File file) {
		if (fileHasTextExtension(file)) {
			return new FileInputTextContentFile(file);
		} else {
			return new FileInputBinaryContentFile(file);
		}
	}

	private static String getBasename(final File file) {
		final String filename = file.getName();
		final int dotPosition = filename.indexOf('.');
		if (dotPosition == -1) {
			return filename;
		} else {
			return filename.substring(0, dotPosition);
		}
	}

	private static String[] getExtensions(final File file) {
		final String filename = file.getName();
		final String[] filenameParts = filename.split("\\.");

		final String[] extensions = new String[filenameParts.length - 1];
		System.arraycopy(filenameParts, 1, extensions, 0, filenameParts.length - 1);
		return extensions;
	}

	private static boolean fileHasTextExtension(final File file) {
		final String filename = file.getName();
		final int dotPosition = filename.lastIndexOf('.');
		if (dotPosition == -1) {
			return false;
		}

		final String extension = filename.substring(dotPosition);
		for (final String textFileExtension : textFileExtensions) {
			if (textFileExtension.equals(extension)) {
				return true;
			}
		}
		return false;
	}

	public class FileInputBinaryContentFile extends BinaryContentFile {
		private final File sourceFile;

		public FileInputBinaryContentFile(final File sourceFile) {
			super(FileInputProcessor.getBasename(sourceFile), FileInputProcessor.getExtensions(sourceFile));
			this.sourceFile = sourceFile;
		}

		public File getSourceFile() {
			return sourceFile;
		}

		@Override
		public String getSource() {
			return "Binary file " + sourceFile.getPath();
		}

		@Override
		public InputStream getContents() throws IOException {
			return new FileInputStream(sourceFile);
		}
	}

	public class FileInputTextContentFile extends TextContentFile {
		private final File sourceFile;

		public FileInputTextContentFile(final File sourceFile) {
			super(FileInputProcessor.getBasename(sourceFile), FileInputProcessor.getExtensions(sourceFile));
			this.sourceFile = sourceFile;
			setText(readSourceFile());
		}

		public File getSourceFile() {
			return sourceFile;
		}

		@Override
		public String getSource() {
			return "Text file " + sourceFile.getPath();
		}

		private String readSourceFile() {
			final FileReader reader = makeFileReader(sourceFile);
			if (reader == null) {
				return "";
			}

			final StringBuilder sb = new StringBuilder();
			final char[] buf = new char[8192];

			try {
				int length = 0;
				while ((length = reader.read(buf)) != -1) {
					sb.append(buf, 0, length);
				}
				return sb.toString();
			} catch (final IOException e) {
				log.error("Could not read file {}", sourceFile.getAbsolutePath(), e);
			} finally {
				try {
					reader.close();
				} catch (final IOException e) {
					log.error("Could not close file {}", sourceFile.getAbsoluteFile(), e);
				}
			}
			return "";
		}

		private FileReader makeFileReader(final File file) {
			try {
				return new FileReader(file);
			} catch (final FileNotFoundException e) {
				log.error("Could not open file {}", file.getAbsolutePath(), e);
				return null;
			}
		}
	}
}
