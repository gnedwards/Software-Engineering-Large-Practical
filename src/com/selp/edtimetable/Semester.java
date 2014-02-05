package com.selp.edtimetable;

import java.util.Calendar;
/**Returns the current semester, based on date from phone settings**/
public class Semester {
	
	private int defaultSem;

	public Semester() {
		
		Calendar calendar = Calendar.getInstance();
		if (calendar.get(Calendar.MONTH) >= 6
				&& calendar.get(Calendar.MONTH) <= 11) {
			//if month is between July and December inclusive: semester 1
			defaultSem = 0;
		} else {
			//if month is between January and June inclusive: semester 2
			defaultSem = 1;
		}
	}
	 Object s;
	public int getDefaultSem() {

		return defaultSem;
	}
	
	 
	
}
