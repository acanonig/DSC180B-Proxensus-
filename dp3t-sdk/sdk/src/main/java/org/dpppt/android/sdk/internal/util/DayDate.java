package org.dpppt.android.sdk.internal.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import com.google.gson.annotations.JsonAdapter;

@JsonAdapter(DayDateJsonAdapter.class)
public class DayDate {
	private static final SimpleDateFormat dayDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);

	static {
		dayDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
	}

	private long timestampRepresentation;


	public DayDate(String dayDate) throws ParseException {
		synchronized (dayDateFormat) {
			timestampRepresentation = convertToDay(dayDateFormat.parse(dayDate).getTime());
		}
	}

	public String formatAsString() {
		synchronized (dayDateFormat) {
			return dayDateFormat.format(new Date(timestampRepresentation));
		}
	}


	private long convertToDay(long time) {
		Calendar calendar = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
		calendar.setTimeInMillis(time);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		return calendar.getTimeInMillis();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		DayDate dayDate = (DayDate) o;
		return timestampRepresentation == dayDate.timestampRepresentation;
	}

	@Override
	public int hashCode() {
		return Objects.hash(timestampRepresentation);
	}
}
