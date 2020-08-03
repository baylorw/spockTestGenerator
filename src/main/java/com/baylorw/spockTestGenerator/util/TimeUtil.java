package com.baylorw.spockTestGenerator.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public final class TimeUtil
{
	private static final Logger LOGGER = LoggerFactory.getLogger(TimeUtil.class);

	//--- Values from Location API (timeZoneId field).
	//--- Some people might find these useful, possibly in their tests.
	//--- Everyone except Arizona is in a DT (daylight savings), not ST, zone - CDT = Central Daylight savings Time.
	//--- Arizona doesn't follow daylight savings time.
	public static final String UTC_TIMEZONE_ID = "UTC";
	public static final String EDT_TIMEZONE_ID = "US/Eastern";
	public static final String CDT_TIMEZONE_ID = "US/Central";
	public static final String MDT_TIMEZONE_ID = "US/Mountain";
	public static final String ARIZONA_TIMEZONE_ID = "US/Arizona";
	public static final String PDT_TIMEZONE_ID = "US/Pacific";
	public static final String AKST_TIMEZONE_ID = "US/Alaska";  // Alaska
	public static final String HAST_TIMEZONE_ID = "US/Hawaii";  // Hawaii

	private static final String E2_DATE_FORMAT_STRING = "MM-dd-yy HH:mm";


	//--- Hide the constructor to quiet CheckStyle's complaints about utility classes.
	private TimeUtil()
	{
	}


	public static Date add(Date date, long numberOfDays, long numberOfHours, long numberOfMinutes,
	                       long numberOfSeconds, long numberOfMilliseconds)
	{
		Instant dateAsInstant = date.toInstant();
		dateAsInstant = dateAsInstant.plus(Duration.ofDays(numberOfDays));
		dateAsInstant = dateAsInstant.plus(Duration.ofHours(numberOfHours));
		dateAsInstant = dateAsInstant.plus(Duration.ofMinutes(numberOfMinutes));
		dateAsInstant = dateAsInstant.plus(Duration.ofSeconds(numberOfSeconds));
		dateAsInstant = dateAsInstant.plus(Duration.ofMillis(numberOfMilliseconds));
		Date newDate = Date.from(dateAsInstant);
		return newDate;
	}

	public static Date addDays(Date date, int numberOfDays)
	{
		return add(date, numberOfDays, 0, 0, 0, 0);
	}

	public static Date addHours(Date date, int numberOfHours)
	{
		return add(date, 0, numberOfHours, 0, 0, 0);
	}

	public static Date addMinutes(Date date, int numberOfMinutes)
	{
		return add(date, 0, 0, numberOfMinutes, 0, 0);
	}

	/**
	 * Returns whether two dates are within X milliseconds of each other. Could be before or after.
	 *
	 * @param one
	 * @param two
	 * @param withinMilliseconds How many milliseconds the two dates can differ and be considered similar.
	 *                           Value is inclusive: 1:00:05 and 1:00:04 are considered equalish.
	 * @return
	 */
	public static boolean areEqualish(Date one, Date two, int withinMilliseconds)
	{
		long oneInMilliseconds = one.getTime();
		long twoInMilliseconds = two.getTime();
		long difference = Math.abs(oneInMilliseconds - twoInMilliseconds);
		return difference <= withinMilliseconds;
	}

	public static ZonedDateTime changeTimeZoneButNotTime(ZonedDateTime dateTime, String timeZoneId)
	{
		//--- Remove old time zone...
		LocalDateTime timeWithoutTimeZone = dateTime.toLocalDateTime();

		//--- ...add the new one.
		ZoneId zoneId = ZoneId.of(timeZoneId);
		ZonedDateTime newDateTime = timeWithoutTimeZone.atZone(zoneId);

		return newDateTime;
	}

	/**
	 * Convert a Date to a String.
	 * <p>
	 * Format (case matters):
	 * Date: MM dd yyyy  E (day name)
	 * Time: hh (am/pm) HH (24-hour) mm ss SSS (ms) n (ns)  a (am/pm)
	 * Misc: O (time zone + offset) V (time zone ID) Z (hours offset) z (time zone name)
	 * <p>
	 * There are lots of others (day of year, quarter, etc.):
	 * https://docs.oracle.com/javase/8/docs/api/java/time/format/DateTimeFormatter.html
	 *
	 * @param dateTime
	 * @param format
	 * @return
	 */
	public static String format(Date dateTime, String format)
	{
		DateFormat dateFormatter = new SimpleDateFormat(format);
		String formattedString = dateFormatter.format(dateTime);
		return formattedString;
	}

	/**
	 * Convert a ZonedDateTime to a String.
	 * <p>
	 * Format (case matters):
	 * Date: MM dd yyyy  E (day name)
	 * Time: hh (am/pm) HH (24-hour) mm ss SSS (ms) n (ns)  a (am/pm)
	 * Misc: O (time zone + offset) V (time zone ID) Z (hours offset) z (time zone name)
	 * <p>
	 * There are lots of others (day of year, quarter, etc.):
	 * https://docs.oracle.com/javase/8/docs/api/java/time/format/DateTimeFormatter.html
	 *
	 * @param dateTime
	 * @param format
	 * @return
	 */
	public static String format(ZonedDateTime dateTime, String format)
	{
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
		String formattedString = dateTime.format(formatter);
		return formattedString;
	}

	/**
	 * Return the start and end times for a given day. Returns dates rather than Java's newer date classes.
	 *
	 * @param day
	 * @param timeZoneName
	 * @return
	 */
	public static Pair<Date, Date> getDateBounds(LocalDate day, String timeZoneName)
	{
		LocalDateTime startOfDay = day.atStartOfDay();
		LocalDateTime endOfDay = day.atTime(LocalTime.MAX);
		ZonedDateTime startInTimeZone = TimeUtil.toZonedDateTime(startOfDay, timeZoneName);
		ZonedDateTime endInTimeZone = TimeUtil.toZonedDateTime(endOfDay, timeZoneName);
		Date startOfDayAsDate = TimeUtil.toDate(startInTimeZone);
		Date endOfDayAsDate = TimeUtil.toDate(endInTimeZone);

		return new Pair<>(startOfDayAsDate, endOfDayAsDate);
	}

	public static LocalDateTime getLocalTimeAtDifferentTimeZone(ZonedDateTime dateTime, String timeZoneId)
	{
		ZonedDateTime zonedLocalDateTime = getTimeAtDifferentTimeZone(dateTime, timeZoneId);
		LocalDateTime localDateTime = zonedLocalDateTime.toLocalDateTime();
		return localDateTime;
	}

	/**
	 * Given a date and time in one time zone, find the time at a different time zone and convert that from a
	 * ZonedDateTime (represented as date time + time zone) to Date (represented as milliseconds from Jan 1, 1970).
	 * <p>
	 * Useful for saving store local time to the database.
	 *
	 * @param dateTime
	 * @param timeZoneId
	 * @return
	 */
	public static Date getLocalTimeAtDifferentTimeZoneAsDate(ZonedDateTime dateTime, String timeZoneId)
	{
		LocalDateTime localDateTime = getLocalTimeAtDifferentTimeZone(dateTime, timeZoneId);
		Date date = toDate(localDateTime);
		return date;
	}

	/**
	 * Given a time in one time zone, return the equivalent time in a different time zone.
	 * Ex: Pass in 4pm CDT and new time zone PDT, returns 2pm PDT.
	 *
	 * @param dateTime
	 * @param timeZoneName
	 * @return
	 */
	public static ZonedDateTime getTimeAtDifferentTimeZone(ZonedDateTime dateTime, String timeZoneName)
	{
		ZoneId zoneId = ZoneId.of(timeZoneName);
		ZonedDateTime zonedDateTime = getTimeAtDifferentTimeZone(dateTime, zoneId);
		return zonedDateTime;
	}

	public static ZonedDateTime getTimeAtDifferentTimeZone(ZonedDateTime dateTime, ZoneId timeZone)
	{
		ZonedDateTime zonedDateTime = dateTime.withZoneSameInstant(timeZone);
		return zonedDateTime;
	}

	public static ZonedDateTime getTimeAtSystemTimeZone(ZonedDateTime dateTime)
	{
		ZoneId zoneId = ZoneId.systemDefault();
		ZonedDateTime zonedDateTime = getTimeAtDifferentTimeZone(dateTime, zoneId);
		return zonedDateTime;
	}

	public static ZonedDateTime getTimeAtUtc(ZonedDateTime dateTime)
	{
		ZonedDateTime utcTime = getTimeAtDifferentTimeZone(dateTime, UTC_TIMEZONE_ID);
		return utcTime;
	}

	/**
	 * Return whether a date is on or between two dates. Time is ignored. The range is from the start of start (midnight)
	 * to the end of end (11:59pm).
	 *
	 * @param date
	 * @param start
	 * @param end
	 * @return
	 */
	public static boolean isBetweenDaysInclusive(Date date, Date start, Date end)
	{
		//--- Start at the beginning of the first day (midnight of the previous day).
		Date startDay = removeTime(start);
		//--- End at the end of the last day. This is technically the start of the next day.
		Date dayAfterEnd = addDays(end, 1);
		Date endDay = removeTime(dayAfterEnd);

		//--- within: start <= date <= end
		return (onOrAfter(date, startDay) && onOrBefore(date, endDay));
	}

	public static boolean onOrAfter(Date dateOfInterest, Date referencePoint)
	{
		return (dateOfInterest.equals(referencePoint) || dateOfInterest.after(referencePoint));
	}

	public static boolean onOrBefore(Date dateOfInterest, Date referencePoint)
	{
		return (dateOfInterest.equals(referencePoint) || dateOfInterest.before(referencePoint));
	}

	public static Date removeTime(Date date)
	{
		Instant dateAsInstant = toInstant(date);
		LocalDate localDate = LocalDateTime.ofInstant(dateAsInstant, ZoneId.systemDefault()).toLocalDate();
		ZonedDateTime zonedDateWithoutTime = localDate.atStartOfDay(ZoneId.systemDefault());
		Instant dateWithoutTimeAsInstant = zonedDateWithoutTime.toInstant();
		Date dateWithoutTime = Date.from(dateWithoutTimeAsInstant);
		return dateWithoutTime;
	}

	public static Date toDate(LocalDate localDate)
	{
		LocalDateTime ldt = localDate.atStartOfDay();
		return toDate(ldt);
	}

	public static Date toDate(LocalDateTime localDateTime)
	{
		ZoneId localZoneId = ZoneId.systemDefault();
		Date date = Date.from(localDateTime.atZone(localZoneId).toInstant());

		return date;
	}

	public static Date toDate(ZonedDateTime zonedDateTime)
	{
		Instant dateAsInstant = zonedDateTime.toInstant();
		Date dateAsDate = Date.from(dateAsInstant);
		return dateAsDate;
	}

	public static Date toDate(String dateString, String format) throws ParseException
	{
		Date date = new SimpleDateFormat(format).parse(dateString);
		return date;
	}

	public static Instant toInstant(Date date)
	{
		return date.toInstant();
	}

	public static LocalDate toLocalDate(String dateString, String datePattern)
	{
		try
		{
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern(datePattern);
			LocalDate date = LocalDate.parse(dateString, formatter);
			return date;
		} catch (Exception e)
		{
			return null;
		}
	}

	public static LocalDateTime toLocalDateTime(String dateTimeString, String dateTimePattern)
	{
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(dateTimePattern);
		LocalDateTime date = LocalDateTime.parse(dateTimeString, formatter);
		return date;
	}

	/**
	 * Given a Date (which represents time in UTC), return a LocateDateTime (which represents the time with no
	 * time zone, it is always assumed to be local time). The day and time will be the same as what's displayed
	 * when printing the date. For example, if it's 3pm CST, it's 9pm UTC. The local date time will be 3pm, not 9.
	 * <p>
	 * Ex:
	 * Date now = new Date;  // 3:00pm in Minneapolis (Tue Oct 29 15:00:00 CDT 2019)
	 * toLocalDateTime(now); // 3:00pm (2019-10-29T15:00:00.000)
	 *
	 * @param date
	 * @return
	 */
	public static LocalDateTime toLocalDateTime(Date date)
	{
		LocalDateTime localDateTime = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
		return localDateTime;
	}

	public static ZonedDateTime toZonedDateTime(LocalDateTime dateTime, String timeZoneName)
	{
		ZoneId zoneId = ZoneId.of(timeZoneName);
		ZonedDateTime zonedDateTime = dateTime.atZone(zoneId);
		return zonedDateTime;
	}

	/**
	 * Convert a Date to a ZonedDateTime. Ignores the original timezone and makes it that time in the specified time zone.
	 * Ex: If you pass in 4pm your time and time zone PST, get back a ZonedDateTime of 4pm PST.
	 *
	 * @param dateTime
	 * @param timeZoneName
	 * @return
	 */
	public static ZonedDateTime createZonedDateTime(Date dateTime, String timeZoneName)
	{
		//--- Remove time zone information.
		LocalDateTime timeWithoutTimezone = toLocalDateTime(dateTime);

		//--- Now add the one we want. This will *NOT* change the time, unlike several other methods i tried.
		ZonedDateTime zonedDateTime = toZonedDateTime(timeWithoutTimezone, timeZoneName);

		return zonedDateTime;
	}
}
