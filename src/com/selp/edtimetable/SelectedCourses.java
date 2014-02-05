package com.selp.edtimetable;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import android.content.Context;
import android.util.Log;
/**Returns a list of selected courses read from savedSelectedCourses.txt, so it retains the user's
 * timetable even after closing the app**/
public class SelectedCourses {

	public static ArrayList<String> getSelectedCourses(File savedSelectedCourses, String message, 
			Context context, Context baseContext) {
		String TAG = "SelectedCourses";
		ArrayList<String> selectedCourses = new ArrayList<String>();
		if (savedSelectedCourses.length() > 0) {
			BufferedReader in = null;
			try {
				in = new BufferedReader(new FileReader(savedSelectedCourses));
				String str = "";
				//read selected courses from file savedSelectedCourses into selectedCourses
				while ((str = in.readLine()) != null) {
					selectedCourses.add(str);
				}
				in.close();
				Log.i(TAG,"savedSelectedCourses read from and closed");
			} catch (FileNotFoundException e) {
				Log.e(TAG,"File IO: savedFilters not found");
				new ErrorDialog().show(message, context, baseContext);
				e.printStackTrace();
			} catch (IOException e) {
				Log.e(TAG,"File IO: problem writing to savedSelectedCourses");
				new ErrorDialog().show(message, context, baseContext);
				e.printStackTrace();
			} 
		}
		return selectedCourses;
	}
}
