package com.vdxp.ssg;

import com.vdxp.ssg.content.ContentDirectory;
import com.vdxp.ssg.content.ContentNode;
import com.vdxp.ssg.processor.FileInputProcessor;
import com.vdxp.ssg.processor.FileOutputProcessor;
import com.vdxp.ssg.processor.HandlebarsLayoutProcessor;
import com.vdxp.ssg.processor.MarkdownProcessor;
import com.vdxp.ssg.processor.YamlFrontMatterProcessor;

import java.io.IOException;

public class Driver {

	public static void main(final String[] args) throws IOException {
		final ContentDirectory target = new ContentDirectory("target");
		final ContentDirectory blog = new FileInputProcessor().readContentRoot("src");
		final ContentDirectory layout = new FileInputProcessor().readContentRoot("layout");

		for (final ContentNode node : blog.getChildren()) {
			node.putData("layout", "blog.hbs");
		}
		target.merge(blog);
		target.addChild(layout);

		new YamlFrontMatterProcessor().process(target);
		new MarkdownProcessor().process(target);
		new HandlebarsLayoutProcessor().process(target);
		new FileOutputProcessor().writeContentRoot(target);
	}

}
