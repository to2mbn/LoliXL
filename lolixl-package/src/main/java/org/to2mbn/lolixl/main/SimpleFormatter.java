package org.to2mbn.lolixl.main;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class SimpleFormatter extends Formatter {

	private Calendar calendar;

	public SimpleFormatter() {
		calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"), Locale.ENGLISH);
	}

	@Override
	public String format(LogRecord record) {
		int year;
		int month;
		int day;
		int hour;
		int minute;
		int second;
		int millis;
		synchronized (calendar) {
			calendar.setTimeInMillis(record.getMillis());
			year = calendar.get(Calendar.YEAR);
			month = calendar.get(Calendar.MONTH);
			day = calendar.get(Calendar.DAY_OF_MONTH);
			hour = calendar.get(Calendar.HOUR_OF_DAY);
			minute = calendar.get(Calendar.MINUTE);
			second = calendar.get(Calendar.SECOND);
			millis = calendar.get(Calendar.MILLISECOND);
		}

		String thrown = "";
		if (record.getThrown() != null) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			pw.print('\n');
			record.getThrown().printStackTrace(pw);
			pw.close();
			thrown = sw.toString();
		}

		String level = record.getLevel().getName();
		String logger = record.getLoggerName();
		String msg = record.getMessage();

		return new StringBuilder(
				4 + 1 + 2 + 1 + 2 + 1 + 2 + 1 + 2 + 1 + 2 + 1 + 3 + 2 + level.length() + 3 + logger.length() + 2 + msg.length() + thrown.length())
						// @formatter:off
						.append(year)	// 4
						.append('-') 	// 1
						.append(month)	// 2
						.append('-')	// 1
						.append(day)	// 2
						.append(' ')	// 1
						.append(hour)	// 2
						.append(':')	// 1
						.append(minute)	// 2
						.append(':')	// 1
						.append(second)	// 2
						.append('.')	// 1
						.append(millis)	// 3
						.append(" [")	// 2
						.append(level)	// level.length()
						.append("] [")	// 3
						.append(logger)	// logger.length()
						.append("] ")	// 2
						.append(msg)	// msg.length()
						.append(thrown)	// thrown.length()
						// @formatter:on
						.toString();
	}
}
