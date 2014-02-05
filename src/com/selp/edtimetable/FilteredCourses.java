package com.selp.edtimetable;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;


import com.selp.edtimetable.IndexLabels.Course;
import com.selp.edtimetable.IndexLabels.Lecture;

import android.content.Context;

import android.util.Log;
import android.util.SparseArray;
import app.startup.Data;
/**Retrives a list of courses which match the current filters applied in either the general or user 
 * timetables
 **/
public class FilteredCourses {
	private HashMap<String, String[]> courseDetails;
	private SparseArray<ArrayList<String[]>> lectureDetails;
	private boolean[] yearFilters;
	private boolean[] degreeFilters;
	private boolean[] pointsFilters;
	private boolean[] levelFilters;
	private boolean yearFilterApplied;
	private boolean degreeFilterApplied;
	private boolean pointsFilterApplied;
	private boolean levelFilterApplied;
	private ArrayList<String> yearFilteredCourses;
	private ArrayList<String> degreeFilteredCourses;
	private ArrayList<String> pointsFilteredCourses;
	private ArrayList<String> levelFilteredCourses;
	private String[] years;
	private String[] points;
	private String[] levels;
	private Set<String> filteredCourses;
	private ArrayList<String> courseCodeList;
	private String TAG = "FilteredCourses";
	
	public FilteredCourses(ArrayList<Set<String>> possibleFilters,
			File savedFilters, Context context) throws FileNotFoundException, IOException {
		courseDetails = Data.courseDetails;
		lectureDetails = Data.lectureDetails;
		courseCodeList = Data.courseCodeList;
		
		//translating the possible string filters into integers so they appear sorted
		int[] yearsInt = new int[possibleFilters.get(0).size()];
		years = new String[possibleFilters.get(0).size()];
		int a = 0;
		for (String Y : possibleFilters.get(0)) {
			yearsInt[a] = Integer.parseInt(Y);
			a++;
		}
		Arrays.sort(yearsInt);
		for (int i = 0; i < yearsInt.length; i++) {
			years[i] = "" + yearsInt[i];
		}

		int[] pointsInt = new int[possibleFilters.get(1).size()];
		points = new String[possibleFilters.get(1).size()];
		a = 0;
		for (String Y : possibleFilters.get(1)) {
			pointsInt[a] = Integer.parseInt(Y);
			a++;
		}
		Arrays.sort(pointsInt);
		for (int i = 0; i < pointsInt.length; i++) {
			points[i] = "" + pointsInt[i];
		}

		int[] levelsInt = new int[possibleFilters.get(2).size()];
		levels = new String[possibleFilters.get(2).size()];
		a = 0;
		for (String Y : possibleFilters.get(2)) {
			levelsInt[a] = Integer.parseInt(Y);
			a++;
		}
		Arrays.sort(levelsInt);
		for (int i = 0; i < levelsInt.length; i++) {
			levels[i] = "" + levelsInt[i];
		}
		Log.i(TAG, "Filters sorted");
		yearFilters = new boolean[years.length];
		degreeFilters = new boolean[4];
		pointsFilters = new boolean[points.length];
		levelFilters = new boolean[levels.length];

		
		if (savedFilters.length() > 0) {
			//reading savedFilters file to acquire the previously saved filters
			BufferedReader in = null;		
			in = new BufferedReader(new FileReader(savedFilters));
			String str;
			int k = 0;
			yearFilterApplied = false;
			degreeFilterApplied = false;
			pointsFilterApplied = false;
			levelFilterApplied = false;
			
				while ((str = in.readLine()) != null) {
					if (k < years.length) {
						if (str.equals("true")) {
							yearFilterApplied = true;
							yearFilters[k] = true;
						}
					}
					if (k >= years.length && k < years.length + 4) {
						if (str.equals("true")) {
							degreeFilterApplied = true;
							degreeFilters[k - years.length] = true;
						}
					}
					if (k >= years.length + 4
							&& k < years.length + 4 + points.length) {
						if (str.equals("true")) {
							pointsFilterApplied = true;
							pointsFilters[k - (years.length + 4)] = true;
						}
					}
					if (k >= years.length + 4 + points.length) {
						if (str.equals("true")) {
							levelFilterApplied = true;
							levelFilters[k
									- (years.length + 4 + points.length)] = true;
						}
					}
					k++;
				}
				
			
				in.close();
			
		}
		//adding all courses which are in the years selected to arraylist yearFilteredCourses
		yearFilteredCourses = new ArrayList<String>();
		if (yearFilterApplied) {
			for (int i = 0; i < years.length; i++) {
				if (yearFilters[i]) {
					for (String C : courseCodeList) {
						for (int d = 0; d < lectureDetails.size(); d++) {
							for (int j = 0; j < lectureDetails.get(d).size(); j++) {
								if (lectureDetails.get(d).get(j)[Lecture.YEARS] != null) {
									if ((lectureDetails.get(d).get(j)[Lecture.CODE]
											.equals(C) && lectureDetails.get(d)
											.get(j)[Lecture.YEARS].contains(years[i]))) {
										yearFilteredCourses.add(C);
									}
								}
							}
						}
					}
				}
			}
		} 
		Log.i(TAG,"year filtered courses attained");
		//adding all courses which are of the degrees selected to arraylist degreeFilteredCourses		
		degreeFilteredCourses = new ArrayList<String>();
		if (degreeFilterApplied) {
			if (degreeFilters[0]) {
				for (String C : courseDetails.keySet()) {
					if (courseDetails.get(C)[Course.AI].equals("AI")) {
						degreeFilteredCourses.add(C);
					}
				}
			}
			if (degreeFilters[1]) {
				for (String C : courseDetails.keySet()) {
					if (courseDetails.get(C)[Course.CG].equals("CG")) {
						degreeFilteredCourses.add(C);
					}
				}
			}
			if (degreeFilters[2]) {
				for (String C : courseDetails.keySet()) {
					if (courseDetails.get(C)[Course.CS].equals("CS")) {
						degreeFilteredCourses.add(C);
					}
				}
			}
			if (degreeFilters[3]) {
				for (String C : courseDetails.keySet()) {
					if (courseDetails.get(Course.SE)[7].equals("SE")) {
						degreeFilteredCourses.add(C);
					}
				}
			}
		}
		Log.i(TAG,"degree filtered courses attained");
		//adding all courses whose points levels are one of the selected to arraylist degreeFilteredCourses	
		pointsFilteredCourses = new ArrayList<String>();
		if (pointsFilterApplied) {
			for (int i = 0; i < points.length; i++) {
				if (pointsFilters[i]) {
					for (String C : courseDetails.keySet()) {
						if (courseDetails.get(C)[Course.POINTS].equals(points[i])) {
							pointsFilteredCourses.add(C);
						}
					}
				}
			}
		}
		Log.i(TAG,"points filtered courses attained");
		//adding all courses whose level is one of the selected to arraylist degreeFilteredCourses	
		levelFilteredCourses = new ArrayList<String>();
		if (levelFilterApplied) {
			for (int i = 0; i < levels.length; i++) {
				if (levelFilters[i]) {
					for (String C : courseDetails.keySet()) {
						if (courseDetails.get(C)[Course.LEVEL].equals(levels[i])) {
							levelFilteredCourses.add(C);
						}
					}
				}
			}

		}
		Log.i(TAG,"level filtered courses attained");
		//applying multi-filters using set intersection to acquire a final set of courses filteredCourses
		filteredCourses = new TreeSet<String>();
		Set<String> yearFilteredSet = new TreeSet<String>(yearFilteredCourses);
		Set<String> degreeFilteredSet = new TreeSet<String>(
				degreeFilteredCourses);
		Set<String> pointsFilteredSet = new TreeSet<String>(
				pointsFilteredCourses);
		Set<String> levelFilteredSet = new TreeSet<String>(levelFilteredCourses);
		int noAppliedFilters = 0;
		if (yearFilterApplied) {
			noAppliedFilters++;
			if (noAppliedFilters == 1) {
				filteredCourses = yearFilteredSet;
			}
		}
		if (degreeFilterApplied) {
			noAppliedFilters++;
			if (noAppliedFilters == 1) {
				filteredCourses = degreeFilteredSet;
			} else {
				filteredCourses.retainAll(degreeFilteredSet);
			}
		}
		if (pointsFilterApplied) {
			noAppliedFilters++;
			if (noAppliedFilters == 1) {
				filteredCourses = pointsFilteredSet;
			} else {
				filteredCourses.retainAll(pointsFilteredSet);
			}
		}
		if (levelFilterApplied) {
			noAppliedFilters++;
			if (noAppliedFilters == 1) {
				filteredCourses = levelFilteredSet;
			} else {
				filteredCourses.retainAll(levelFilteredSet);
			}
		}
		Log.i(TAG,"multifilters applied");
		// If no filters applied add all courses to the filteredCourses list
		if (!(yearFilterApplied || degreeFilterApplied || pointsFilterApplied || levelFilterApplied)) {
			filteredCourses.addAll(courseCodeList);
			Log.i(TAG,"no filters applied");
		}

	}

	public Set<String> getFilteredCourses() {
		return filteredCourses;

	}
}
