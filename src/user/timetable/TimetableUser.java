package user.timetable;

import general.timetable.TimetableGen;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;


import com.selp.edtimetable.R;
import com.selp.edtimetable.SelectedCourses;
import com.selp.edtimetable.IndexLabels.Building;
import com.selp.edtimetable.IndexLabels.Course;
import com.selp.edtimetable.IndexLabels.Lecture;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;
import app.startup.Data;
/**Displays the user timetable in an expandable list**/
public class TimetableUser extends Activity {
	private ArrayList<String> selectedCourses;
	private HashMap<String, String[]> courseDetails;
	private HashMap<String, String> roomDetails;
	private HashMap<String, String[]> buildingDetails;
	private SparseArray<ArrayList<String[]>> lectureDetails;
	private ArrayList<ArrayList<String>> children;
	private ArrayList<ArrayList<String>> childTitles;
	private ArrayList<String> time;
	private ArrayList<String> courseName;
	private ArrayList<String> venue;
	private HashSet<String> occurredBefore;
	private HashMap<String, Integer> occurences;
	private LayoutInflater inflater;
	private String TAG = "TimetableUser", message;
	private Context context, baseContext;
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_user_timetable);
		
		File savedSelectedCourses = new File(getFilesDir(),
				"savedSelectedCourses.txt");
		context = this;
		baseContext = getBaseContext();
		//get selected courses from savedSelectedCourses file
		selectedCourses = SelectedCourses
				.getSelectedCourses(savedSelectedCourses, message, context, baseContext);
		courseDetails = Data.courseDetails;
		roomDetails = Data.roomDetails;
		buildingDetails = Data.buildingDetails;
		lectureDetails = Data.lectureDetails;
		occurredBefore = new HashSet<String>();
		time = new ArrayList<String>();
		courseName = new ArrayList<String>();
		this.venue = new ArrayList<String>();
		childTitles = new ArrayList<ArrayList<String>>();
		children = new ArrayList<ArrayList<String>>();
		occurences = new HashMap<String, Integer>();
		inflater = (LayoutInflater) getBaseContext().getSystemService(
				Context.LAYOUT_INFLATER_SERVICE);
		for (int d = 0; d < lectureDetails.size(); d++) {
		//loop through days	
			for (int i = 0; i < lectureDetails.get(d).size(); i++) {
			//loop through times
				if (selectedCourses.contains(lectureDetails.get(d).get(i)[Lecture.CODE])) {
					/*--------------------parent of expandableListCourses ---------------------------*/
					
					//if the current course also occurs at another time, add to occurredBefore set
					if (time.contains(lectureDetails.get(d).get(i)[Lecture.DAY] + " "
							+ lectureDetails.get(d).get(i)[Lecture.START_TIME] + "-"
							+ lectureDetails.get(d).get(i)[Lecture.FINISH_TIME])) {
						occurredBefore.add(lectureDetails.get(d).get(i)[Lecture.DAY]
								+ " " + lectureDetails.get(d).get(i)[Lecture.START_TIME] + "-"
								+ lectureDetails.get(d).get(i)[Lecture.FINISH_TIME]);
					}
					//add time
					time.add(lectureDetails.get(d).get(i)[Lecture.DAY] + " "
							+ lectureDetails.get(d).get(i)[Lecture.START_TIME] + "-"
							+ lectureDetails.get(d).get(i)[Lecture.FINISH_TIME]);
					//add course name
					if (courseDetails
							.containsKey(lectureDetails.get(d).get(i)[Lecture.CODE])) {
						courseName.add(courseDetails.get(lectureDetails.get(d)
								.get(i)[Lecture.CODE])[Course.FULL_NAME]);
					} else {
						courseName.add(lectureDetails.get(d).get(i)[Lecture.CODE]);
					}
					//add room
					String header = "";
					if (roomDetails
							.containsKey(lectureDetails.get(d).get(i)[Lecture.ROOM])) {
						header += roomDetails
								.get(lectureDetails.get(d).get(i)[Lecture.ROOM]) + ", ";
					} else {
						header += lectureDetails.get(d).get(i)[Lecture.ROOM] + ", ";
					}
					//add building
					if (buildingDetails.containsKey(lectureDetails.get(d)
							.get(i)[Lecture.BUILDING])) {
						header += buildingDetails.get(lectureDetails.get(d)
								.get(i)[Lecture.BUILDING])[Building.DESCRIPTION];
					} else {
						header += lectureDetails.get(d).get(i)[Lecture.BUILDING];
					}
					venue.add(header);
					
					/*------------------------child of expandableListCourses ----------------------*/
					ArrayList<String> details = new ArrayList<String>();
					ArrayList<String> title = new ArrayList<String>();
					//add map
					title.add("Map");
					if (buildingDetails.containsKey(lectureDetails.get(d)
							.get(i)[Lecture.BUILDING])) {
						details.add("<a href=\""
								+ buildingDetails.get(lectureDetails.get(d)
										.get(i)[Lecture.BUILDING])[Building.MAP]
								+ "\">"
								+ buildingDetails.get(lectureDetails.get(d)
										.get(i)[Lecture.BUILDING])[Building.MAP] + "</a>");
					} else {
						details.add("<FONT COLOR = red>Unknown location! Refer to course webpage/lecturer</FONT>");
					}
					
					if (courseDetails
							.containsKey(lectureDetails.get(d).get(i)[Lecture.CODE])) {
						title.add("Code");
						//add code
						details.add(lectureDetails.get(d).get(i)[Lecture.CODE]);
						//add URL
						if (courseDetails.get(lectureDetails.get(d).get(i)[Lecture.CODE])[Course.URL] != null) {
							title.add("Course Webpage");
							details.add("<a href=\""
									+ courseDetails.get(lectureDetails.get(d)
											.get(i)[Lecture.CODE])[Course.URL]
									+ "\">"
									+ courseDetails.get(lectureDetails.get(d)
											.get(i)[Lecture.CODE])[Course.URL] + "</a>");
						}
						//add DRPS
						if (courseDetails.get(lectureDetails.get(d).get(i)[Lecture.CODE])[Course.DRPS] != null) {
							title.add("DRPS Webpage");
							details.add("<a href=\""
									+ courseDetails.get(lectureDetails.get(d)
											.get(i)[Lecture.CODE])[Course.DRPS]
									+ "\">"
									+ courseDetails.get(lectureDetails.get(d)
											.get(i)[Lecture.CODE])[Course.DRPS] + "</a>");
						}
						//add Euclid Code
						if (courseDetails.get(lectureDetails.get(d).get(i)[Lecture.CODE])[Course.EUCLID] != null) {
							title.add("Euclid Code");
							details.add("Euclid code: "
									+ courseDetails.get(lectureDetails.get(d)
											.get(i)[Lecture.CODE])[Course.EUCLID]);
						}
						//add degree
						boolean degProg = false;
						for (int j = 4; j < 8; j++) {
							if (!courseDetails
									.get(lectureDetails.get(d).get(i)[Lecture.CODE])[j]
									.equals("")) {
								degProg = true;
							}
						}
						
						if (degProg) {
							title.add("Degree Programmes:");
							String degrees = "";
							if (!courseDetails
									.get(lectureDetails.get(d).get(i)[Lecture.CODE])[Course.AI]
									.equals("")) {
								degrees += "Artificial Intelligence <br/>";
							}
							if (!courseDetails
									.get(lectureDetails.get(d).get(i)[Lecture.CODE])[Course.CG]
									.equals("")) {
								degrees += "Cognitive Science <br/>";
							}
							if (!courseDetails
									.get(lectureDetails.get(d).get(i)[Lecture.CODE])[Course.CS]
									.equals("")) {
								degrees += "Computer Science <br/>";
							}
							if (!courseDetails
									.get(lectureDetails.get(d).get(i)[Lecture.CODE])[Course.SE]
									.equals("")) {
								degrees += "Software Engineering <br/>";
							}
							details.add(degrees);
						}
						//add level
						if (courseDetails.get(lectureDetails.get(d).get(i)[Lecture.CODE])[Course.LEVEL] != null) {
							title.add("Level");
							details.add(courseDetails.get(lectureDetails.get(d)
									.get(i)[Lecture.CODE])[Course.LEVEL]);
						}
						//add points
						if (courseDetails.get(lectureDetails.get(d).get(i)[Lecture.CODE])[Course.POINTS] != null) {
							title.add("Points");
							details.add(courseDetails.get(lectureDetails.get(d)
									.get(i)[Lecture.CODE])[Course.POINTS]);
						}
						//add year normally taken
						if (courseDetails.get(lectureDetails.get(d).get(i)[Lecture.CODE])[Course.YEAR] != null) {
							title.add("Year Normally Taken");
							details.add(courseDetails.get(lectureDetails.get(d)
									.get(i)[Lecture.CODE])[Course.YEAR]);
						}
					}
					//add years can be taken
					if (!lectureDetails.get(d).get(i)[Lecture.YEARS].equals("")) {
						title.add("Years can be Taken");
						details.add(lectureDetails.get(d).get(i)[Lecture.YEARS]);
					}
					//add period
					if (courseDetails
							.containsKey(lectureDetails.get(d).get(i)[Lecture.CODE])) {
						if (courseDetails.get(lectureDetails.get(d).get(i)[Lecture.CODE])[Course.DELIVERY_PERIOD]
								.equals("YR")) {
							title.add("Period");
							details.add("Full year");
						} else if (courseDetails.get(lectureDetails.get(d).get(
								i)[Lecture.CODE])[Course.DELIVERY_PERIOD].equals("FLEX")) {
							title.add("Period");
							details.add("Flexible");
						} else if (courseDetails.get(lectureDetails.get(d).get(
								i)[Lecture.CODE])[Course.DELIVERY_PERIOD].equals("S1")) {
							title.add("Period");
							details.add("Semester 1");
						} else if (courseDetails.get(lectureDetails.get(d).get(
								i)[Lecture.CODE])[Course.DELIVERY_PERIOD].equals("S2")) {
							title.add("Period");
							details.add("Semester 2");
						} else {
							title.add("Period");
							details.add(courseDetails.get(lectureDetails.get(d)
									.get(i)[Lecture.CODE])[Course.DELIVERY_PERIOD]);
						}
					} else {
						title.add("Period");
						details.add("Semester " + lectureDetails.get(d)
								.get(i)[Lecture.SEM]);
					}
					//add lecturer
					if (courseDetails
								.containsKey(lectureDetails.get(d).get(i)[Lecture.CODE])) {
						if (courseDetails.get(lectureDetails.get(d).get(i)[Lecture.CODE])[Course.LECTURER] != null) {
							title.add("Lecturer");
							details.add(courseDetails.get(lectureDetails.get(d)
									.get(i)[Lecture.CODE])[Course.LECTURER]);
						}
					}
					//add comment
					if (!lectureDetails.get(d).get(i)[Lecture.COMMENT].equals("")) {
						title.add("Comment");
						details.add(lectureDetails.get(d).get(i)[Lecture.COMMENT]);
					}
					childTitles.add(title);
					children.add(details);
				}
			}
		}
		Log.i(TAG, "finished populating arraylists for scheduled courses");
		HashSet<String> timeSet = new HashSet<String>(time);

		for (String C : timeSet) {
			occurences.put(C, Collections.frequency(time, C));
		}
		
		for (String C : courseDetails.keySet()) {
		//loop though all courses to find non timetabled ones
			if (selectedCourses.contains(C) && !timetabledCourse(C)) {
				/*---------------------parent of expandableListView -----------------------*/
				//add time
				time.add("[Not scheduled]");
				//add course name
				courseName.add(courseDetails.get(C)[Course.FULL_NAME]);
				//add venue
				venue.add("");
				/*-------------------- child of expandableListView -----------------------*/
				ArrayList<String> details = new ArrayList<String>();
				ArrayList<String> title = new ArrayList<String>();
				//add map
				title.add("Map");
				details.add("<FONT COLOR = red>Location not specified!<FONT>");
				//add code
				title.add("Code");
				details.add(C);
				//add URL
				if (courseDetails.get(C)[Course.URL] != null) {
					title.add("Course Page");
					details.add("<a href=\"" + courseDetails.get(C)[Course.URL] + "\">"
							+ courseDetails.get(C)[Course.URL] + "</a>");
				}
				//add DRPS
				if (courseDetails.get(C)[Course.DRPS] != null) {
					title.add("DRPS page");
					details.add("<a href=\"" + courseDetails.get(C)[Course.DRPS] + "\">"
							+ courseDetails.get(C)[Course.DRPS] + "</a>");
				}
				//add Euclid code
				if (courseDetails.get(C)[Course.EUCLID] != null) {
					title.add("Euclid code");
					details.add(courseDetails.get(C)[Course.EUCLID]);
				}
				//add degree programme
				boolean degProg = false;
				for (int j = 4; j < 8; j++) {
					if (!courseDetails.get(C)[j].equals("")) {
						degProg = true;
					}
				}
				if (degProg) {
					title.add("Degree Programmes:");
					String degrees = "";
					if (!courseDetails.get(C)[Course.AI].equals("")) {
						degrees += "Artificial Intelligence <br/>";
					}
					if (!courseDetails.get(C)[Course.CG].equals("")) {
						degrees += "Cognitive Science <br/>";
					}
					if (!courseDetails.get(C)[Course.CS].equals("")) {
						degrees += "Computer Science <br/>";
					}
					if (!courseDetails.get(C)[Course.SE].equals("")) {
						degrees += "Software Engineering <br/>";
					}
					details.add(degrees);
				}
				//add level
				if (courseDetails.get(C)[Course.LEVEL] != null) {
					title.add("Level");
					details.add(courseDetails.get(C)[Course.LEVEL]);
				}
				//add points
				if (courseDetails.get(C)[Course.POINTS] != null) {
					title.add("Points");
					details.add(courseDetails.get(C)[Course.POINTS]);
				}
				//add year normally taken
				if (courseDetails.get(C)[Course.YEAR] != null) {
					title.add("Year normally taken");
					details.add(courseDetails.get(C)[Course.YEAR]);
				}
				//add period
				if (courseDetails.get(C)[Course.DELIVERY_PERIOD].equals("YR")) {
					title.add("Period");
					details.add("Full year");
				} else if (courseDetails.get(C)[Course.DELIVERY_PERIOD].equals("FLEX")) {
					title.add("Period");
					details.add("Flexible");
				} else if (courseDetails.get(C)[Course.DELIVERY_PERIOD].equals("S1")) {
					title.add("Period");
					details.add("Semester 1");
				} else if (courseDetails.get(C)[Course.DELIVERY_PERIOD].equals("S2")) {
					title.add("Period");
					details.add("Semester 2");
				} else {
					title.add("Period");
					details.add(courseDetails.get(C)[Course.DELIVERY_PERIOD]);
				}
				//add lecturer
				if (courseDetails.get(C)[Course.DELIVERY_PERIOD] != null) {
					title.add("Lecturer");
					details.add(courseDetails.get(C)[Course.DELIVERY_PERIOD]);
				}
				childTitles.add(title);
				children.add(details);
			}
		}
		Log.i(TAG, "finished populating arraylists for non scheduled courses");
		final ExpandableListView expandableListCourses = (ExpandableListView) findViewById(R.id.expandableListViewCourses);
		
		expandableListCourses.setAdapter(new ExpandableListAdapter() {

			@Override
			public Object getChild(int groupPosition, int childPosition) {
				return null;
			}

			@Override
			public long getChildId(int groupPosition, int childPosition) {
				return 0;
			}

			@Override
			public View getChildView(int groupPosition, int childPosition,
					boolean isLastChild, View convertView, ViewGroup parent) {

				ViewHolderChild viewHolderChild;
				View view = null;
				if (convertView == null) {
					view = inflater.inflate(R.layout.other_detail_item,
							null);
					viewHolderChild = new ViewHolderChild();
					viewHolderChild.tvTitle = (TextView) view
							.findViewById(R.id.tvFilters);
					viewHolderChild.tvSubtitle = (TextView) view
							.findViewById(R.id.tvSubtitle);
					view.setTag(viewHolderChild);
				} else {
					view = convertView;
					viewHolderChild = (ViewHolderChild) view.getTag();
				}
				viewHolderChild.tvTitle.setTextSize(20);

				viewHolderChild.tvSubtitle.setTextSize(20);
				viewHolderChild.tvSubtitle.setMovementMethod(LinkMovementMethod
						.getInstance());

				viewHolderChild.tvTitle.setText(childTitles.get(groupPosition)
						.get(childPosition));

				viewHolderChild.tvSubtitle.setText(Html.fromHtml(children.get(
						groupPosition).get(childPosition)));
				return view;
			}

			@Override
			public int getChildrenCount(int groupPosition) {
				return children.get(groupPosition).size();
			}

			@Override
			public Object getGroup(int groupPosition) {
				return groupPosition;
			}

			@Override
			public int getGroupCount() {
				return time.size();
			}
			@Override
			public long getGroupId(int groupPosition) {
				return groupPosition;
			}

			@Override
			public View getGroupView(int groupPosition, boolean isExpanded,
					View convertView, ViewGroup parent) {

				ViewHolderParent viewHolderParent;
				View view = inflater.inflate(R.layout.parent, null);
				viewHolderParent = new ViewHolderParent();
				viewHolderParent.tvTime = (TextView) view
						.findViewById(R.id.tvTime);
				viewHolderParent.tvCourseName = (TextView) view
						.findViewById(R.id.tvCourseName);
				viewHolderParent.tvVenue = (TextView) view
						.findViewById(R.id.tvVenue);
				view.setTag(viewHolderParent);
				viewHolderParent.tvTime.setText(time.get(groupPosition));
				viewHolderParent.tvTime.setTextColor(Color.YELLOW);
				viewHolderParent.tvTime.setBackgroundResource(R.color.darkBlue);
				viewHolderParent.tvCourseName
						.setBackgroundResource(R.color.darkBlue);
				viewHolderParent.tvVenue.setBackgroundResource(R.color.darkBlue);
				if (!time.get(groupPosition).startsWith("[Not")) {
					if (occurences.get(time.get(groupPosition)) > 1) {
						viewHolderParent.tvTime.setBackgroundColor(Color.RED);
						viewHolderParent.tvCourseName
								.setBackgroundColor(Color.RED);
						viewHolderParent.tvVenue.setBackgroundColor(Color.RED);
					}
				}
				viewHolderParent.tvCourseName.setText(courseName
						.get(groupPosition));
				viewHolderParent.tvCourseName.setTextColor(Color.GREEN);
				viewHolderParent.tvCourseName.setPadding(55, 0, 0, 5);
				viewHolderParent.tvVenue.setText(venue.get(groupPosition));
				viewHolderParent.tvVenue.setTextColor(Color.WHITE);
				viewHolderParent.tvVenue.setPadding(55, 0, 0, 0);
				return view;
			}

			@Override
			public boolean hasStableIds() {		
				return false;
			}

			@Override
			public boolean isChildSelectable(int groupPosition,
					int childPosition) {		
				return true;
			}

			@Override
			public boolean areAllItemsEnabled() {		
				return false;
			}

			@Override
			public long getCombinedChildId(long arg0, long arg1) {		
				return 0;
			}

			@Override
			public long getCombinedGroupId(long arg0) {
				return 0;
			}
		

			@Override
			public boolean isEmpty() {		
				return false;
			}

			@Override
			public void onGroupCollapsed(int arg0) {
				//Do nothing
			}

			@Override
			public void onGroupExpanded(int arg0) {
				//Do nothing
			}

			@Override
			public void registerDataSetObserver(DataSetObserver arg0) {
				//Do nothing
			}

			@Override
			public void unregisterDataSetObserver(DataSetObserver arg0) {
				//Do nothing
			}

		});

		if(occurredBefore.isEmpty()) {
		//if clashing course display warning toast
			Toast.makeText(getBaseContext(), "No clashes!", Toast.LENGTH_SHORT)
			.show();
		} else {
			Toast.makeText(getBaseContext(), "Some courses selected clash!",
					Toast.LENGTH_SHORT).show();
		}
	}

	private boolean timetabledCourse(String course) {
		boolean found = false;
		for (int d = 0; d < lectureDetails.size(); d++) {
			for (int i = 0; i < lectureDetails.get(d).size(); i++) {
				if (course.equals(lectureDetails.get(d).get(i)[3])) {
					found = true;
					Log.i(TAG,"timetabled course found");
					break;
				}
			}
		}
		return found;
	}

	private class ViewHolderChild {
		TextView tvTitle;
		TextView tvSubtitle;
	}

	private class ViewHolderParent {
		TextView tvTime;
		TextView tvCourseName;
		TextView tvVenue;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.timetable_user_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case R.id.Edit:
			startActivity(new Intent(getBaseContext(), SelectCourses.class));
			return true;
		case R.id.GenTable:
			startActivity(new Intent(getBaseContext(), TimetableGen.class));
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onBackPressed() {
		//Do nothing
	}

}
