package com.vdxp.ssg;

import com.vdxp.ssg.content.ContentDirectory;
import com.vdxp.ssg.content.ContentNode;
import com.vdxp.ssg.processor.BlogPagesGeneratorProcessor;
import com.vdxp.ssg.processor.DateParsingProcessor;
import com.vdxp.ssg.processor.FileInputProcessor;
import com.vdxp.ssg.processor.FileOutputProcessor;
import com.vdxp.ssg.processor.HandlebarsLayoutProcessor;
import com.vdxp.ssg.processor.MarkdownProcessor;
import com.vdxp.ssg.processor.RootPathProcessor;
import com.vdxp.ssg.processor.SplitReadMoreProcessor;
import com.vdxp.ssg.processor.YamlFrontMatterProcessor;

import java.io.IOException;

public class Driver {

	public static void main(final String[] args) throws IOException {
		final ContentDirectory layout = new FileInputProcessor().readContentRoot("layout");
		new YamlFrontMatterProcessor().process(layout);

		final ContentDirectory site = new FileInputProcessor().readContentRoot("site");
		new YamlFrontMatterProcessor().process(site);
		new MarkdownProcessor().process(site);

		final ContentDirectory blog = new FileInputProcessor().readContentRoot("blog");

		for (final ContentNode node : blog.getChildren()) {
			node.putData("layout", "blog.hbs");
		}

		new YamlFrontMatterProcessor().process(blog);
		new DateParsingProcessor("date").process(blog);
		new MarkdownProcessor().process(blog);
		new SplitReadMoreProcessor().process(blog);
		new BlogPagesGeneratorProcessor(new BlogPagesGeneratorProcessor.Options(3)).process(blog);

		final ContentDirectory target = new ContentDirectory("target");
		target.merge(site);
		target.merge(blog);

		new RootPathProcessor().process(target);
		new HandlebarsLayoutProcessor().process(target, layout);
		new FileOutputProcessor().writeContentRoot(target);
	}

}
