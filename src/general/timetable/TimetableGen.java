package general.timetable;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import user.timetable.SelectCourses;
import user.timetable.TimetableUser;

import com.selp.edtimetable.ErrorDialog;
import com.selp.edtimetable.FilteredCourses;
import com.selp.edtimetable.R;
import com.selp.edtimetable.Semester;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.Spinner;
import android.widget.TextView;
import app.startup.Data;
/**Displays the general timetable in an expandable list, allowing the user to change the semester showing**/
public class TimetableGen extends Activity {
	
	private HashMap<String, String[]> courseDetails;
	private HashMap<String, String> roomDetails;
	private HashMap<String, String[]> buildingDetails;
	private SparseArray<ArrayList<String[]>> lectureDetails;
	private ArrayList<ArrayList<String>> children;
	private ArrayList<ArrayList<String>> childTitles;
	private ArrayList<String> time;
	private ArrayList<String> courseName;
	private ArrayList<String> venue;
	private ArrayList<Set<String>> possibleFilters;
	private Set<String> filteredCourses;
	private int defaultSem;
	private ArrayList<String> courseCodes;
	private String[] semesters = { "Semester 1", "Semester 2", "All Semesters" };
	private LayoutInflater inflater;
	private Context context, baseContext;
	private String message = "", TAG = "TimetableGen";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_general_timetable);
		inflater = (LayoutInflater) getBaseContext().getSystemService(
				Context.LAYOUT_INFLATER_SERVICE);
		courseDetails = Data.courseDetails;
		roomDetails = Data.roomDetails;
		buildingDetails = Data.buildingDetails;
		lectureDetails = Data.lectureDetails;
		possibleFilters = Data.possibleFilters;
		courseCodes = new ArrayList<String>();
		time = new ArrayList<String>();
		courseName = new ArrayList<String>();
		venue = new ArrayList<String>();
		context = this;
		baseContext = getBaseContext();
		childTitles = new ArrayList<ArrayList<String>>();
		children = new ArrayList<ArrayList<String>>();
		//get courses with filters applied
		try {
			FilteredCourses fc = new FilteredCourses(possibleFilters, new File(
					getFilesDir(), "savedFiltersGen.txt"), context);
			filteredCourses = fc.getFilteredCourses();
		} catch (FileNotFoundException e) {
			Log.e(TAG,"savedFiltersGen.txt not found");
			message = "Problem with files on your device.";		
			new ErrorDialog().show(message, context, baseContext);
			e.printStackTrace();
		} catch (IOException e) {
			Log.e(TAG,"File IO problem with savedFiltersGen.txt");
			message = "Problem with files on your device.";		
			new ErrorDialog().show(message, context, baseContext);
			e.printStackTrace();
		}
		
		//get current semester
		Semester sem = new Semester();
		defaultSem = sem.getDefaultSem();
		//get courses in current semester, with filters applied
		populateSelector(defaultSem);

		final ExpandableListView expandableListCourses = (ExpandableListView) findViewById(R.id.expandableListViewCoursesGen);
		final CustomExpandableListAdapter adapter = new CustomExpandableListAdapter();
		expandableListCourses.setAdapter(adapter);
		expandableListCourses.requestLayout();
		Log.i(TAG,"expandableListCourses created");
		//set up spinner for selecting the semester of the courses to be displayed in expandableListCourses
		Spinner selectSem = (Spinner) findViewById(R.id.spSelectSemGen);
		ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
				getBaseContext(), R.layout.tv_sem, semesters);
		selectSem.setAdapter(arrayAdapter);
		arrayAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		selectSem
				.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
					//
					@Override
					public void onItemSelected(AdapterView<?> arg0, View arg1,
							int selectedSem, long arg3) {
						populateSelector(selectedSem);
						adapter.notifyDataSetChanged();
						expandableListCourses.setAdapter(adapter);
						Log.i(TAG, "expandableListCourses updated according to semester");
					}

					@Override
					public void onNothingSelected(AdapterView<?> arg0) {
						//do nothing
					}

				});
	}

	private boolean timetabledCourse(String course) {
		//if course found in lectureDetails then it's a timetabled course
		boolean found = false;
		for (int d = 0; d < lectureDetails.size(); d++) {
			for (int i = 0; i < lectureDetails.get(d).size(); i++) {
				if (course.equals(lectureDetails.get(d).get(i)[Lecture.CODE])) {
					found = true;
					Log.i(TAG,"timetabled course found");
					break;
				}
			}
		}
		return found;
	}

	private void populateSelector(int selectedSem) {
		courseCodes.clear();
		switch (selectedSem) {
		//get semester 1 courses, with filters applied
		case 0:
			for (String C : filteredCourses) {
				if (courseDetails.containsKey(C)) {
				//if course acronym has extension display it alongside the code
					if (courseDetails.get(C)[Course.DELIVERY_PERIOD].equals("S1")
							|| courseDetails.get(C)[Course.DELIVERY_PERIOD].equals("YR")) {
						if (!courseCodes.contains(C)) {
							courseCodes.add(C);
						}
					}
				} else {
				//otherwise just display code
					for (int d = 0; d < lectureDetails.size(); d++) {
						for (int i = 0; i < lectureDetails.get(d).size(); i++) {
							if (lectureDetails.get(d).get(i)[Lecture.CODE].equals(C)
									&& lectureDetails.get(d).get(i)[Lecture.SEM]
											.equals("1")) {
								if (!courseCodes.contains(C)) {
									courseCodes.add(C);
								}
							}
						}
					}
				}
			}
			Log.i(TAG,"semester 1 courses retrieved");
			break;

		case 1:

			for (String C : filteredCourses) {
				if (courseDetails.containsKey(C)) {
				//if course acronym has extension display it alongside the code	
					if (courseDetails.get(C)[Course.DELIVERY_PERIOD].equals("S2")			
							|| courseDetails.get(C)[Course.DELIVERY_PERIOD].equals("YR")) {
						if (!courseCodes.contains(C)) {
							courseCodes.add(C);
						}
					}
				} else {
				//otherwise just display code
					for (int d = 0; d < lectureDetails.size(); d++) {
						for (int i = 0; i < lectureDetails.get(d).size(); i++) {
							if (lectureDetails.get(d).get(i)[Lecture.CODE].equals(C)
									&& lectureDetails.get(d).get(i)[Lecture.SEM]
											.equals("2")) {
								if (!courseCodes.contains(C)) {
									courseCodes.add(C);
								}
							}
						}
					}
				}

			}
			Log.i(TAG,"semester 2 courses retrieved");
			break;
		case 2:
			for (String C : filteredCourses) {
//				if (!courseCodes.contains(C)) {
					courseCodes.add(C);
//				}
			}
			Log.i(TAG,"courses in either semester retrieved");
			break;
		}
		time.clear();
		courseName.clear();
		venue.clear();
		childTitles.clear();
		children.clear();
		
		for (int d = 0; d < lectureDetails.size(); d++) {
		//loop through days
			for (int i = 0; i < lectureDetails.get(d).size(); i++) {
			//loop through times
				if (courseCodes.contains(lectureDetails.get(d).get(i)[Lecture.CODE])) {
					/*-------------- Parent of expandableListCourses --------------------*/
					//add day & time
					time.add(lectureDetails.get(d).get(i)[Lecture.DAY] + " "
							+ lectureDetails.get(d).get(i)[Lecture.START_TIME] + "-"
							+ lectureDetails.get(d).get(i)[Lecture.FINISH_TIME]);
					//add course name
					if (courseDetails
							.containsKey(lectureDetails.get(d).get(i)[Lecture.CODE])) {
						courseName.add(courseDetails.get(lectureDetails.get(d)
								.get(i)[Lecture.CODE])[0]);
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
					/*-------------- Child of expandableListCourses --------------------*/
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
						//add code
						title.add("Code");
						details.add(lectureDetails.get(d).get(i)[Lecture.CODE]);
						if (courseDetails.get(lectureDetails.get(d).get(i)[Lecture.CODE])[Course.URL] != null) {
							title.add("Course Webpage");
							details.add("<a href=\""
									+ courseDetails.get(lectureDetails.get(d)
											.get(i)[Lecture.CODE])[Course.URL]
									+ "\">"
									+ courseDetails.get(lectureDetails.get(d)
								
											.get(i)[Lecture.CODE])[Course.URL] + "</a>");
						}
						//add DRPS page
						if (courseDetails.get(lectureDetails.get(d).get(i)[Lecture.CODE])[Course.DRPS] != null) {
							title.add("DRPS Webpage");
							details.add("<a href=\""
									+ courseDetails.get(lectureDetails.get(d)
											.get(i)[Lecture.CODE])[Course.DRPS]
									+ "\">"
									+ courseDetails.get(lectureDetails.get(d)
											.get(i)[Lecture.CODE])[Course.DRPS] + "</a>");
						}
						//add EUCLID code
						if (courseDetails.get(lectureDetails.get(d).get(i)[Lecture.CODE])[Course.EUCLID] != null) {
							title.add("Euclid Code");
							details.add(courseDetails.get(lectureDetails.get(d)
											.get(i)[Lecture.CODE])[Course.EUCLID]);
						}
						//add degree programme
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
						if (courseDetails.get(lectureDetails.get(d).get(i)[Lecture.CODE])[Course.LEVEL] != null) {
							title.add("Points");
							details.add(courseDetails.get(lectureDetails.get(d)
									.get(i)[Lecture.CODE])[Course.LEVEL]);
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
		/*-----------------add non-scheduled courses------------------------------*/
		for (String C : courseDetails.keySet()) {
			/*-----------------parent of expandableListCourses----------------------*/
			if (courseCodes.contains(C) && !timetabledCourse(C)) {
				time.add("[Not scheduled]");
				courseName.add(courseDetails.get(C)[Course.FULL_NAME]);
				venue.add("");
				/*-----------------child of expandableListCourses----------------------*/
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
				if (courseDetails.get(C)[Course.LECTURER] != null) {
					title.add("Lecturer");
					details.add(courseDetails.get(C)[Course.LECTURER]);
				}
				childTitles.add(title);
				children.add(details);
				
			}
		}
		Log.i(TAG, "finished populating arraylists for non-scheduled courses");
	}

	private class CustomExpandableListAdapter extends BaseExpandableListAdapter {

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
			viewHolderParent.tvTime.setBackgroundResource(R.color.darkBlue);
			viewHolderParent.tvCourseName
					.setBackgroundResource(R.color.darkBlue);
			viewHolderParent.tvVenue.setBackgroundResource(R.color.darkBlue);
			viewHolderParent.tvTime.setText(time.get(groupPosition));
			viewHolderParent.tvTime.setTextColor(Color.YELLOW);
		
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
		public boolean isChildSelectable(int groupPosition, int childPosition) {
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
			//do nothing
		}

		@Override
		public void onGroupExpanded(int arg0) {
			//do nothing

		}

		@Override
		public int getChildTypeCount() {
			return 2;
		}

		@Override
		public void registerDataSetObserver(DataSetObserver arg0) {
			//do nothing
		}

		@Override
		public void unregisterDataSetObserver(DataSetObserver arg0) {
			//do nothing
		}
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
		getMenuInflater().inflate(R.menu.timetable_gen_menu, menu);
		File savedSelectedCourses = new File(getFilesDir(),
				"savedSelectedCourses.txt");
		MenuItem item = menu.findItem(R.id.UserTable);
		if (savedSelectedCourses.length() > 0) {
			item.setTitle("Your timetable");
		} else {
			item.setTitle("Create Timetable");
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.FiltersGen:
			startActivity(new Intent(getBaseContext(), FiltersGen.class));
			return true;
		case R.id.SearchGen:
			startActivity(new Intent(getBaseContext(), SearchGen.class));
			return true;
		case R.id.UserTable:
			File savedSelectedCourses = new File(getFilesDir(),
					"savedSelectedCourses.txt");
			if (savedSelectedCourses.length() > 0) {				
				startActivity(new Intent(getBaseContext(), TimetableUser.class));
			} else {	
				startActivity(new Intent(getBaseContext(), SelectCourses.class));
			}
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onBackPressed() {
		//do nothing
	}

}
