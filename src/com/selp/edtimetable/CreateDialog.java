package com.selp.edtimetable;

import java.util.ArrayList;
import java.util.HashMap;


import com.selp.edtimetable.IndexLabels.Building;
import com.selp.edtimetable.IndexLabels.Course;
import com.selp.edtimetable.IndexLabels.Lecture;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import app.startup.Data;
/**Dialog that shows lecture information in top listview and course information in bottom listview
 **/
public class CreateDialog {

	public static void getDialog(Context context, int position,
			final LayoutInflater inflater, ArrayList<String> courseCodes) {
		
		class ViewHolderLectures {
			TextView tvTime;
			TextView tvVenue;
			TextView tvMap;
			TextView tvComment;
		}

		class ViewHolderOtherDetails {
			TextView tvTitle;
			TextView tvSubtitle;
		}
		HashMap<String, String[]> courseDetails;
		HashMap<String, String> roomDetails;
	    HashMap<String, String[]> buildingDetails;
		SparseArray<ArrayList<String[]>> lectureDetails;
		String TAG = "CreateDialog";
		AlertDialog showCourseDetails = null;
		AlertDialog.Builder showCourseDetailsBuilder = new AlertDialog.Builder(
				context);
		View lecturesView = inflater.inflate(R.layout.course_details_dialog,
				null);
		courseDetails = Data.courseDetails;
		roomDetails = Data.roomDetails;
		buildingDetails = Data.buildingDetails;
		lectureDetails = Data.lectureDetails;
		ListView lvLectures = (ListView) lecturesView
				.findViewById(R.id.lvLectures);
		final ArrayList<String> time = new ArrayList<String>();
		final ArrayList<String> location = new ArrayList<String>();
		final ArrayList<String> map = new ArrayList<String>();
		final ArrayList<String> comment = new ArrayList<String>();
		//populating the titles and items in top listview in dialog, showing lectures
		String semester = "";
		for (int d = 0; d < lectureDetails.size(); d++) {
		//loop through days
			for (int i = 0; i < lectureDetails.get(d).size(); i++) {
			//loop through times
				if (courseCodes.get(position).equals(
						lectureDetails.get(d).get(i)[Lecture.CODE])) {
					semester = lectureDetails.get(d).get(i)[Lecture.SEM];
					//add day & time
					time.add(lectureDetails.get(d).get(i)[Lecture.DAY] + " "
							+ lectureDetails.get(d).get(i)[Lecture.START_TIME] + "-"
							+ lectureDetails.get(d).get(i)[Lecture.FINISH_TIME]);
					String text = "";
					//add room
					if (roomDetails
							.containsKey(lectureDetails.get(d).get(i)[Lecture.ROOM])) {
						text += roomDetails
								.get(lectureDetails.get(d).get(i)[Lecture.ROOM]) + ", ";
					} else {
						text += lectureDetails.get(d).get(i)[Lecture.ROOM] + ", ";
					}
					//add building
					if (buildingDetails.containsKey(lectureDetails.get(d)
							.get(i)[Lecture.BUILDING])) {
						text += buildingDetails.get(lectureDetails.get(d)
								.get(i)[Lecture.BUILDING])[Building.DESCRIPTION];
					} else {
						text += lectureDetails.get(d).get(i)[Lecture.BUILDING];
					}
					location.add(text);
					//add map
					if (buildingDetails.containsKey(lectureDetails.get(d)
							.get(i)[Lecture.BUILDING])) {
						map.add("Map: <a href=\""
								+ buildingDetails.get(lectureDetails.get(d)
										.get(i)[Lecture.BUILDING])[Building.MAP]
								+ "\">"
								+ buildingDetails.get(lectureDetails.get(d)
										.get(i)[Lecture.BUILDING])[Building.MAP] + "</a>");
					} else {
						map.add("Map: Unknown location! Refer to course webpage/lecturer");
					}
					//add comment
					if (!lectureDetails.get(d).get(i)[Lecture.COMMENT].equals("")) {
						comment.add("Comment: "
								+ lectureDetails.get(d).get(i)[Lecture.COMMENT]);
					} else {
						comment.add("");
					}
				}
			}
		}
		Log.i(TAG,"arraylists for lectures filled successfully");
		lvLectures.setAdapter(new ArrayAdapter<String>(context,
				R.layout.lecture_item, time) {

			@Override
			public View getView(int position, View convertView, ViewGroup parent) {

				ViewHolderLectures viewHolderLectures;
				View view;
				if (convertView == null) {
					viewHolderLectures = new ViewHolderLectures();
					view = inflater.inflate(R.layout.lecture_item, null);
					viewHolderLectures.tvTime = (TextView) view
							.findViewById(R.id.tvTime);
					viewHolderLectures.tvVenue = (TextView) view
							.findViewById(R.id.tvVenue);
					viewHolderLectures.tvMap = (TextView) view
							.findViewById(R.id.tvMap);
					viewHolderLectures.tvComment = (TextView) view
							.findViewById(R.id.tvComment);
					view.setTag(viewHolderLectures);
				} else {
					view = convertView;
					viewHolderLectures = (ViewHolderLectures) view
							.getTag();
				}
				viewHolderLectures.tvMap.setMovementMethod(LinkMovementMethod
						.getInstance());

				viewHolderLectures.tvTime.setText(time.get(position));
				viewHolderLectures.tvVenue.setText(location.get(position));
				viewHolderLectures.tvMap.setText(Html.fromHtml(map
						.get(position)));
				if (map.get(position).startsWith("Unknown")) {
					viewHolderLectures.tvMap.setTextColor(Color.RED);
				}
				viewHolderLectures.tvComment.setText(comment.get(position));
				return view;
			}

		});
		Log.i(TAG,"lectures listview created successfully");
		TextView tvNoLectures = (TextView) lecturesView
				.findViewById(R.id.tvNoLectures);
		if (time.isEmpty()) {
			tvNoLectures.setVisibility(View.VISIBLE);
			Log.i("CreateDialog", "test");
			tvNoLectures.setText("No scheduled lectures!");
			tvNoLectures.setTextColor(Color.RED);
			lvLectures.setVisibility(View.GONE);
		} else {
			tvNoLectures.setVisibility(View.GONE);
			lvLectures.setVisibility(View.VISIBLE);
		}
		
		//populating the titles and items in bottom listview in dialog, showing course details
		final ArrayList<String> title = new ArrayList<String>();
		final ArrayList<String> subtitle = new ArrayList<String>();
		
		if (courseDetails.containsKey(courseCodes.get(position))) {
			//add URL
			if (courseDetails.get(courseCodes.get(position))[Course.URL] != null) {
				title.add("Course page");
				subtitle.add("<a href=\""
						+ courseDetails.get(courseCodes.get(position))[Course.URL]
						+ "\">"
						+ courseDetails.get(courseCodes.get(position))[Course.URL]
						+ "</a>");
			}
			//add DRPS
			if (courseDetails.get(courseCodes.get(position))[Course.DRPS] != null) {
				title.add("DRPS page");
				subtitle.add("<a href=\""
						+ courseDetails.get(courseCodes.get(position))[Course.DRPS]
						+ "\">"
						+ courseDetails.get(courseCodes.get(position))[Course.DRPS]
						+ "</a>");
			}
			//add EUCLID CODE
			if (courseDetails.get(courseCodes.get(position))[Course.EUCLID] != null) {
				title.add("Euclid code");
				subtitle.add(courseDetails.get(courseCodes.get(position))[Course.EUCLID]);
			}
			boolean degProg = false;
			for (int j = 4; j < 8; j++) {
				if (!courseDetails.get(courseCodes.get(position))[j].equals("")) {
					degProg = true;
				}
			}
			//add degree programmes
			if (degProg) {
				title.add("Degree Programme");
				String degrees = "";
				if (!courseDetails.get(courseCodes.get(position))[Course.AI].equals("")) {
					degrees += "Artificial Intelligence <br/>";
				}
				if (!courseDetails.get(courseCodes.get(position))[Course.CG].equals("")) {
					degrees += "Cognitive Science <br/>";
				}
				if (!courseDetails.get(courseCodes.get(position))[Course.CS].equals("")) {
					degrees += "Computer Science <br/>";
				}
				if (!courseDetails.get(courseCodes.get(position))[Course.SE].equals("")) {
					degrees += "Software Engineering <br/>";
				}
				if (degrees.equals("")) {
					degrees = "Not listed under any degree programme";
				}
				subtitle.add(degrees);
			}
			//add level
			if (courseDetails.get(courseCodes.get(position))[Course.LEVEL] != null) {
				title.add("Level");
				subtitle.add(courseDetails.get(courseCodes.get(position))[8]);
			}
			//add points
			if (courseDetails.get(courseCodes.get(position))[Course.POINTS] != null) {
				title.add("Points");
				subtitle.add(courseDetails.get(courseCodes.get(position))[Course.POINTS]);
			}
			//add year normally taken
			if (courseDetails.get(courseCodes.get(position))[Course.YEAR] != null) {
				title.add("Year normally taken");
				subtitle.add(courseDetails.get(courseCodes.get(position))[Course.YEAR]);
			}
		}
		//add years can be taken
		outerloop: for (int e = 0; e < lectureDetails.size(); e++) {
			for (int i = 0; i < lectureDetails.get(e).size(); i++) {
				if (courseCodes.get(position).equals(
						lectureDetails.get(e).get(i)[Lecture.CODE])) {
					title.add("Years can be taken");
					subtitle.add(lectureDetails.get(e).get(i)[Lecture.YEARS]);
					break outerloop;
				}
			}
		}
		//add period
		if (courseDetails.containsKey(courseCodes.get(position))) {

			if (courseDetails.get(courseCodes.get(position))[Course.DELIVERY_PERIOD].equals("YR")) {
				title.add("Period");
				subtitle.add("full year");
			} else if (courseDetails.get(courseCodes.get(position))[Course.DELIVERY_PERIOD]
					.equals("FLEX")) {
				title.add("Period");
				subtitle.add("flexible");
			} else if (courseDetails.get(courseCodes.get(position))[Course.DELIVERY_PERIOD]
					.equals("S1")) {
				title.add("Period");
				subtitle.add("Semester 1");
			} else if (courseDetails.get(courseCodes.get(position))[Course.DELIVERY_PERIOD]
					.equals("S2")) {
				title.add("Period");
				subtitle.add("Semester 2");
			} else {
				title.add("Period");
				subtitle.add(courseDetails.get(courseCodes.get(position))[Course.DELIVERY_PERIOD]);
			}
		} else {
			title.add("Period");
			subtitle.add("Semester " +semester);
		}
		//add lecturer
		if (courseDetails.containsKey(courseCodes.get(position))) {
			if (courseDetails.get(courseCodes.get(position))[Course.LECTURER] != null) {
				title.add("Lecturer");
				subtitle.add(courseDetails.get(courseCodes.get(position))[12]);
			}
		}
		Log.i(TAG,"arraylists for course details filled successfully");
		ListView lvOtherDetails = (ListView) lecturesView
				.findViewById(R.id.lvOtherDetails);
		lvOtherDetails.setAdapter(new ArrayAdapter<String>(context,
				R.layout.other_detail_item, title) {

			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				ViewHolderOtherDetails viewHolderOtherDetails;
				View view = null;
				if (convertView == null) {
					viewHolderOtherDetails = new ViewHolderOtherDetails();
					view = inflater.inflate(R.layout.other_detail_item,
							null);
					viewHolderOtherDetails.tvTitle = (TextView) view
							.findViewById(R.id.tvFilters);
					viewHolderOtherDetails.tvSubtitle = (TextView) view
							.findViewById(R.id.tvSubtitle);
					view.setTag(viewHolderOtherDetails);
				} else {
					view = convertView;
					viewHolderOtherDetails = (ViewHolderOtherDetails) view
							.getTag();
				}

				viewHolderOtherDetails.tvTitle.setText(title.get(position));
				viewHolderOtherDetails.tvSubtitle
						.setMovementMethod(LinkMovementMethod.getInstance());

				viewHolderOtherDetails.tvSubtitle.setText(Html
						.fromHtml(subtitle.get(position)));

				return view;
			}

		});
		Log.i(TAG,"course details listview created successfully");
		TextView tvNoDetails = (TextView) lecturesView
				.findViewById(R.id.tvNoDetails);
		if (title.isEmpty()) {
			tvNoDetails.setVisibility(View.VISIBLE);
			tvNoDetails.setText("No course details provided!");
			tvNoDetails.setTextColor(Color.RED);
			lvOtherDetails.setVisibility(View.GONE);
		} else {
			tvNoDetails.setVisibility(View.GONE);
			lvOtherDetails.setVisibility(View.VISIBLE);
		}

		showCourseDetailsBuilder.setView(lecturesView);
		showCourseDetailsBuilder.setPositiveButton("OK", null);
		showCourseDetails = showCourseDetailsBuilder.create();
		View titleView = inflater.inflate(R.layout.dialog_title, null);
		TextView tvTitle = (TextView) titleView
				.findViewById(R.id.tvDialogTitle);
		if (courseDetails.containsKey(courseCodes.get(position))) {
			tvTitle.setText(courseDetails.get(courseCodes.get(position))[0]);
		} else {
			tvTitle.setText(courseCodes.get(position));
		}
		showCourseDetails.setCustomTitle(titleView);
		showCourseDetails.show();
		Log.i(TAG,"dialog created successfully");
		
		
	}

	
}
