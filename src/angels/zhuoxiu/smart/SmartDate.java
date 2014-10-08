package angels.zhuoxiu.smart;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SmartDate {
	static final SimpleDateFormat DEFAULT_TIME_FORMAT = new SimpleDateFormat("HH:mm", Locale.getDefault());
	static final SimpleDateFormat DEFAULT_DATE_FORMAT = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
	static final int MILLISECONDS_IN_SECOND = 1000;
	static final int SECONDS_IN_MINTE = 60;
	static final int SECONDS_IN_HOUR = SECONDS_IN_MINTE * 60;
	static final int SECONDS_IN_DAY = SECONDS_IN_HOUR * 24;

	Date date;
	public int second, minute, hour, day;

	/**
	 * Date to compare
	 */
	SmartDate compareDate;

	public SmartDate(Date date) {
		setDate(date);
	}

	public void setDate(Date date) {
		this.date = date;
		second = (int) (date.getTime() / MILLISECONDS_IN_SECOND);
		minute = second / SECONDS_IN_MINTE;
		hour = second / SECONDS_IN_HOUR;
		day = second / SECONDS_IN_DAY;
	}

	public Date getDate() {
		return date;
	}

	public void setCompareDate(Date compareDate) {
		this.compareDate = new SmartDate(compareDate);
	}

	protected SmartDate getCompateDate() {
		return compareDate != null ? compareDate : new SmartDate(new Date());
	}

	/**
	 * Get the Day difference to the {@linkplain #compareDate}, which can be set by {@link #setCompareDate(Date)}.
	 * If no {@linkplain #compareDate} is set, it will compare with the current date.
	 * @return Day difference to the {@linkplain #compareDate}. Positive value means it is after and negative means it is before.
	 */
	public int getDayDiff() {
		return day - getCompateDate().day;
	}

	/**
	 * Absolute value of {@linkplain #getDayDiff()}
	 * @return Absolute value of day difference.
	 */
	public int getDayDiffAbs() {
		return Math.abs(getDayDiff());
	}

	/**
	 * Get the Hour difference to the {@linkplain #compareDate}, which can be set by {@link #setCompareDate(Date)}.
	 * If no {@linkplain #compareDate} is set, it will compare with the current date.
	 * @return Hour difference to the {@linkplain #compareDate}. Positive value means it is after and negative means it is before.
	 */
	public int getHourDiff() {
		return hour - getCompateDate().hour;
	}

	/**
	 * Absolute value of {@linkplain #getHourDiff()}
	 * @return Absolute value of hour difference.
	 */
	public int getHourDiffAbs() {
		return Math.abs(getHourDiff());
	}

	/**
	 * Get the Minute difference to the {@linkplain #compareDate}, which can be set by {@link #setCompareDate(Date)}.
	 * If no {@linkplain #compareDate} is set, it will compare with the current date.
	 * @return Minute difference to the {@linkplain #compareDate}. Positive value means it is after and negative means it is before.
	 */
	public int getMinuteDiff() {
		return minute - getCompateDate().minute;
	}

	public int getMinuteDiffAbs() {
		return Math.abs(getMinuteDiff());
	}

	public int getSecDiff() {
		return second - getCompateDate().second;
	}

	public int getSecDiffAbs() {
		return Math.abs(getSecDiff());
	}

	/**
	 * 
	 * @return
	 */
	public String getSmartDateString() {
		setCompareDate(new Date());
		String smartString = new String();
		if (getDayDiffAbs() == 0) {
			if (getHourDiffAbs() == 0) {
				if (getMinuteDiff() == 0) {
					if (getSecDiff() == 0) {
						smartString = "just now!";
					} else {
						smartString = getSecDiffAbs() + " second " + (getSecDiff() < 0 ? "ago" : "later");
					}
				} else {
					smartString = getMinuteDiffAbs() + " minute " + (getMinuteDiff() < 0 ? "ago" : "later");
				}
			} else if (getHourDiffAbs() <= 3) {
				int minDiffInHour = getMinuteDiffAbs() % 60;
				smartString = getHourDiffAbs() + " hour " + (getHourDiff() < 0 ? "ago" : "later");
			} else {
				smartString = DEFAULT_TIME_FORMAT.format(date) + " " + "today";
			}
		} else if (getDayDiffAbs() == 1) {
			smartString = DEFAULT_TIME_FORMAT.format(date) + " " + "yesterday";
		} else {
			smartString = DEFAULT_TIME_FORMAT.format(date) + " " + DEFAULT_DATE_FORMAT.format(date);
		}
		return smartString;
	}
}
