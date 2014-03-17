package com.vdxp.ssg.processor;

import com.joestelmach.natty.DateGroup;
import com.joestelmach.natty.Parser;
import com.vdxp.ssg.content.BinaryContentFile;
import com.vdxp.ssg.content.ContentDirectory;
import com.vdxp.ssg.content.ContentNode;
import com.vdxp.ssg.content.ContentVisitor;
import com.vdxp.ssg.content.TextContentFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class DateParsingProcessor {

	private static final Logger log = LoggerFactory.getLogger(DateParsingProcessor.class);

	private final Parser natty = new Parser();
	private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy HH:mm Z");

	private final String[] dataKeyNames;

	public DateParsingProcessor(final String... dataKeyNames) {
		if (dataKeyNames.length > 0) {
			this.dataKeyNames = dataKeyNames;
		} else {
			this.dataKeyNames = new String[] {"data"};
		}
	}

	public void process(final ContentNode contentNode) {
		contentNode.accept(new DateParsingVisitor());
	}

	private class DateParsingVisitor implements ContentVisitor {

		private void visit(final ContentNode contentNode, final List<ContentNode> parents) {
			for (final String key : dataKeyNames) {
				final Object value = contentNode.getData().get(key);
				if (value == null) {
					continue;
				}

				final Date parsedDate = parseDateString(value);
				if (parsedDate == null) {
					log.debug("Could not parse {} in {} as a date", value, contentNode);
					continue;
				}

				final long rawDate = parsedDate.getTime();
				final String formattedDate = dateFormat.format(parsedDate);
				log.debug("Parsed {} in {} as {}", value, contentNode, formattedDate);
				contentNode.putData(key + "_raw", rawDate);
				contentNode.putData(key + "_formatted", formattedDate);
			}
		}

		private Date parseDateString(final Object inputDate) {
			if (!(inputDate instanceof String)) {
				return null;
			}

			final List<DateGroup> parsedDateGroups = natty.parse((String) inputDate);
			if (parsedDateGroups.isEmpty()) {
				return null;
			}

			final DateGroup parsedDateGroup = parsedDateGroups.get(0);
			final List<Date> parsedDates = parsedDateGroup.getDates();
			if (parsedDates.isEmpty()) {
				return null;
			}

			final Date parsedDate = parsedDates.get(0);
			log.debug("Date {} parsed as {}", inputDate, parsedDate);
			return parsedDate;
		}

		@Override
		public void visit(final ContentDirectory contentDirectory, final List<ContentNode> parents) {
			visit((ContentNode) contentDirectory, parents);
		}

		@Override
		public void visit(final BinaryContentFile contentFile, final List<ContentNode> parents) {
			visit((ContentNode) contentFile, parents);
		}

		@Override
		public void visit(final TextContentFile contentFile, final List<ContentNode> parents) {
			visit((ContentNode) contentFile, parents);
		}
	}
}

