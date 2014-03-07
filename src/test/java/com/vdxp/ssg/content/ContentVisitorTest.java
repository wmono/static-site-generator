package com.vdxp.ssg.content;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Deque;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertThat;

public class ContentVisitorTest {

	@Test
	public void test() {
		final TestVisitor visitor = new TestVisitor();

		final ContentDirectory root = new ContentDirectory("root");
		final ContentDirectory dir1 = new ContentDirectory("dir1");
		final ContentDirectory dir2 = new ContentDirectory("dir2");
		final ContentDirectory dir3 = new ContentDirectory("dir3");
		final ContentFile file1 = new DummyTextContentFile("file1");
		final ContentFile file2 = new DummyBinaryContentFile("file2");
		final ContentFile file3 = new DummyTextContentFile("file3");
		root.addChild(file1);
		root.addChild(dir1);
		dir1.addChild(dir2);
		dir2.addChild(dir3);
		dir2.addChild(file2);
		dir3.addChild(file3);
		root.accept(visitor);

		final String out = visitor.out.toString();
		assertThat(out, containsString("\n/root/file1\n"));
		assertThat(out, containsString("\n/root/dir1 (dir)\n"));
		assertThat(out, containsString("\n/root/dir1/dir2 (dir)\n"));
		assertThat(out, containsString("\n/root/dir1/dir2/dir3 (dir)\n"));
		assertThat(out, containsString("\n/root/dir1/dir2/file2 (bin)\n"));
		assertThat(out, containsString("\n/root/dir1/dir2/dir3/file3\n"));
	}

	private class TestVisitor implements ContentVisitor {

		public final StringBuilder out = new StringBuilder();

		public TestVisitor() {
			out.append("\n");
		}

		@Override
		public void visit(final TextContentFile contentFile, final Deque<ContentNode> parents) {
			out.append(parentNames(parents));
			out.append("/");
			out.append(contentFile.getName());
			out.append("\n");
		}

		@Override
		public void visit(final BinaryContentFile contentFile, final Deque<ContentNode> parents) {
			out.append(parentNames(parents));
			out.append("/");
			out.append(contentFile.getName());
			out.append(" (bin)");
			out.append("\n");
		}

		@Override
		public void visit(final ContentDirectory contentDirectory, final Deque<ContentNode> parents) {
			out.append(parentNames(parents));
			out.append("/");
			out.append(contentDirectory.getName());
			out.append(" (dir)");
			out.append("\n");
		}

		private String parentNames(final Deque<ContentNode> parents) {
			final StringBuilder sb = new StringBuilder();
			for (final ContentNode node : parents) {
				sb.append("/");
				sb.append(node.getName());
			}
			return sb.toString();
		}
	}

	private class DummyTextContentFile extends TextContentFile {
		public DummyTextContentFile(final String name) {
			super(name);
			setText("dummy");
		}

		@Override
		public String getSource() {
			return "Dummy file";
		}
	}

	private class DummyBinaryContentFile extends BinaryContentFile {
		public DummyBinaryContentFile(final String name) {
			super(name);
		}

		@Override
		public String getSource() {
			return "Dummy file";
		}

		@Override
		public InputStream getContents() throws IOException {
			return new ByteArrayInputStream("dummy".getBytes());
		}
	}
}
