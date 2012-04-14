package se.droidfactory.mitthem.helpers;

import java.util.Calendar;
import java.util.HashMap;

import android.app.Application;
import android.content.Context;

import se.droidfactory.mitthem.R;

public class DateNameHelper {
	
	/**
	 * Singleton pattern
	 */
	private static DateNameHelper singleton;
	public static DateNameHelper getInstance() {
		if (singleton == null) {
			singleton = new DateNameHelper();
		}
		return singleton;
	}
	

	public HashMap<Integer, String> weekDayNames;
	public HashMap<Integer, String> monthNames;

	public void initialize(final Context c) {
		this.weekDayNames = new HashMap<Integer, String>() {
			{
				put(Calendar.MONDAY, c.getString(R.string.weekday_name_monday));
				put(Calendar.TUESDAY, c.getString(R.string.weekday_name_tuesday));
				put(Calendar.WEDNESDAY, c.getString(R.string.weekday_name_wednesday));
				put(Calendar.THURSDAY, c.getString(R.string.weekday_name_thursday));
				put(Calendar.FRIDAY, c.getString(R.string.weekday_name_friday));
				put(Calendar.SATURDAY, c.getString(R.string.weekday_name_saturday));
				put(Calendar.SUNDAY, c.getString(R.string.weekday_name_sunday));
			}
		};

		int[] monthNameNumbers = { Calendar.JANUARY, Calendar.FEBRUARY, Calendar.MARCH, Calendar.APRIL, Calendar.MAY,
				Calendar.JUNE, Calendar.JULY, Calendar.AUGUST, Calendar.SEPTEMBER, Calendar.OCTOBER, Calendar.NOVEMBER,
				Calendar.DECEMBER };

		this.monthNames = new HashMap<Integer, String>();
		for (int i : monthNameNumbers) {
			this.monthNames.put(i, c.getResources().getStringArray(R.array.month_names)[i]);
		}
	}

}
