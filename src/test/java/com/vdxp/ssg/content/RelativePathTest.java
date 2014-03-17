package com.vdxp.ssg.content;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class RelativePathTest {

	ContentNode general1;
	ContentNode general2;
	ContentNode generalRoot;

	ContentNode sibling1;
	ContentNode sibling2;
	ContentNode siblingParent;

	ContentNode disjoint1;
	ContentNode disjoint2;

	@Test
	public void testNearestCommonParentOfSiblings() {
		assertThat(siblingParent, is(ContentNode.getNearestCommonParent(sibling1, sibling2)));
		assertThat(siblingParent, is(ContentNode.getNearestCommonParent(sibling2, sibling1)));
	}

	@Test
	public void testNearestCommonParentOfDisjoint() {
		assertThat(null, is(ContentNode.getNearestCommonParent(disjoint1, disjoint2)));
		assertThat(null, is(ContentNode.getNearestCommonParent(disjoint2, disjoint1)));
	}

	@Test
	public void testRelativePathOfSiblings() {
		assertThat("s2", is(sibling1.getRelativePath(sibling2)));
		assertThat("s1", is(sibling2.getRelativePath(sibling1)));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testRelativePathOfDisjoint1() {
		disjoint1.getRelativePath(disjoint2);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testRelativePathOfDisjoint2() {
		disjoint2.getRelativePath(disjoint1);
	}

	@Test
	public void testGeneral() {
		assertThat("../../../right1/g2", is(general1.getRelativePath(general2)));
		assertThat("../left1/left2/left3/g1", is(general2.getRelativePath(general1)));
	}

	@Before
	public void setupSiblings() {
		final ContentNode c1 = new ContentDirectory("c1");
		final ContentNode c2 = new ContentDirectory("c2");
		final TestTextContentFile s1 = new TestTextContentFile("s1");
		final TestTextContentFile s2 = new TestTextContentFile("s2");

		c2.setParent(c1);
		s1.setParent(c2);
		s2.setParent(c2);

		sibling1 = s1;
		sibling2 = s2;
		siblingParent = c2;
	}

	@Before
	public void setupDisjoint() {
		disjoint1 = new ContentDirectory("disjoint1");
		disjoint2 = new ContentDirectory("disjoint2");
	}

	@Before
	public void setupGeneral() {
		final ContentNode root = new ContentDirectory("target");
		final ContentNode left1 = new ContentDirectory("left1");
		final ContentNode left2 = new ContentDirectory("left2");
		final ContentNode left3 = new ContentDirectory("left3");
		final ContentNode leftLeaf = new TestTextContentFile("g1");
		final ContentNode right1 = new ContentDirectory("right1");
		final ContentNode rightLeaf = new TestTextContentFile("g2");

		leftLeaf.setParent(left3);
		left3.setParent(left2);
		left2.setParent(left1);
		left1.setParent(root);
		rightLeaf.setParent(right1);
		right1.setParent(root);

		general1 = leftLeaf;
		general2 = rightLeaf;
		generalRoot = root;
	}

	private class TestTextContentFile extends com.vdxp.ssg.content.TextContentFile {
		public TestTextContentFile(final String basename, final String... extensions) {
			super(basename, extensions);
		}

		@Override
		public String getSource() {
			return "Test file " + getBasename();
		}
	}
}
