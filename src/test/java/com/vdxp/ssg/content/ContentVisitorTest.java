package com.vdxp.ssg.content;

import org.junit.Test;

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
		final ContentFile file1 = new DummyContentFile("file1");
		final ContentFile file2 = new DummyContentFile("file2");
		final ContentFile file3 = new DummyContentFile("file3");
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
		assertThat(out, containsString("\n/root/dir1/dir2/file2\n"));
		assertThat(out, containsString("\n/root/dir1/dir2/dir3/file3\n"));
	}

	private class TestVisitor implements ContentVisitor {

		public final StringBuilder out = new StringBuilder();

		public TestVisitor() {
			out.append("\n");
		}

		@Override
		public void visit(final ContentFile contentFile, final Deque<ContentNode> parents) {
			out.append(parentNames(parents));
			out.append("/");
			out.append(contentFile.getName());
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

	private class DummyContentFile extends ContentFile {
		public DummyContentFile(final String name) {
			super(name);
		}

		@Override
		public String getSource() {
			return "Dummy file";
		}
	}
}
