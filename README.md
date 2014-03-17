# Static Site Generator

It's another static site generator. I wrote it to suit my needs. There is no
plugin mechanism. There is no configuration. You may find it useful. More
likely, you will not.

### Aren't there enough of these things already?

Yes, but I'm picky. A thorough investigation of
[existing static site generators](http://staticsitegenerators.net/) led me
to the conclusion that [Wintersmith](http://wintersmith.io/) and
[Assemble](http://assemble.io/) are both very good, but not *quite* what I
want to use for my site. The main stumbling block for both was it was very
cumbersome to generate pages containing content from multiple files, like
what you might expect to see on the front page of a Wordpress or Drupal
blog site.

### So what does this one do?

Some features include:

- Use Markdown for content and Handlebars for layout
- Generates blog pages showing multiple snippets, with navigation
- Different source directories can be processed independently and merged at the end
- Templates are processed at the last minute, so models are most similar to the output file
- Dates can be entered in many different formats (uses [Natty](http://natty.joestelmach.com/))
- Binary files are copied without being held in memory

Have a look at the [Driver](src/main/java/com/vdxp/ssg/Driver.java) for a
more concrete idea of what this does.

The default (i.e. hard-coded) setup requires three directories under the
current working directory:
- `site`
- `blog`
- `layout`

Any files with specific extensions will be recognized as text files, and
their contents will be manipulated by the content tree operations that
follow. Any other files will be considered binary files, and their contents
will only be read from disk when it comes time to copy them to the output
directory.

Any text files containing
[YAML Front Matter](http://jekyllrb.com/docs/frontmatter/)
will have that section parsed into the data field of the content tree node
and then stripped off.

Any content nodes with a `layout` key in the data field will be passed
through the named layout file. The layout file will find the original
contents in the `contents` key.

Files named `.md` will be converted from Markdown to HTML.

Files names `.hbs` will be processed as Handlebars templates.

Files contained in `site` will be copied as they are to the output directory.

Files contained in `blog` will be copied as they are to the output
directory. Additionally, any text files in this directory will also be
copied into a series of files named index.html, page/2/index.html,
page/3/index.html, etc., containing three text files each.

### How do I run it?

1. Generate the distribution zip file:
   ```shell
   $ mvn package
   ```

2. Unzip `target/static-site-generator-0.0.0.BUILD-SNAPSHOT.zip` somewhere
and execute the JAR file.

### This is too confusing! Do you have any samples?

Check back in a day or two.

### This looks half-baked!

Read the first paragraph again.
