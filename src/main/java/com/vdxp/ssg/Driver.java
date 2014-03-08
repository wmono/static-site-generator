package com.vdxp.ssg;

import com.vdxp.ssg.content.ContentDirectory;
import com.vdxp.ssg.processor.FileInputProcessor;
import com.vdxp.ssg.processor.FileOutputProcessor;
import com.vdxp.ssg.processor.YamlFrontMatterProcessor;

import java.io.IOException;

public class Driver {

	public static void main(final String[] args) throws IOException {
		final ContentDirectory target = new ContentDirectory("target");
		final ContentDirectory blog = new FileInputProcessor().readContentRoot("src");
		target.merge(blog);
		new YamlFrontMatterProcessor().process(target);
		new FileOutputProcessor().writeContentRoot(target);
	}

}
