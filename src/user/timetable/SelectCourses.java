package user.timetable;

import general.timetable.TimetableGen;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;



import com.selp.edtimetable.CreateDialog;
import com.selp.edtimetable.ErrorDialog;
import com.selp.edtimetable.FilteredCourses;
import com.selp.edtimetable.R;
import com.selp.edtimetable.SelectedCourses;
import com.selp.edtimetable.Semester;
import com.selp.edtimetable.IndexLabels.Course;
import com.selp.edtimetable.IndexLabels.Lecture;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;
import app.startup.Data;
/**Course selection screen showing a  list from which the user can select courses for their timetable. They
 * can press a course for more information. Any course selected shows at the top.
 */
public class SelectCourses extends Activity {
	
	private Button submit;
	private ListView lvCourses;
	private CheckBox cbSelectAll;
	private LayoutInflater inflater;
	private TextView tvSelectedCourses;
	public static ArrayList<String> courses;
	public static ArrayList<String> courseCodes;
	public static ArrayList<Boolean> checkedVals;

	protected HashMap<String, String[]> courseDetails;
	private ViewHolder viewHolder;
	public static ArrayList<String> selectedCourses;
	private Spinner selectSem;
	private String[] semesters = { "Semester 1", "Semester 2", "All Semesters" };
	private ArrayAdapter<String> adapter;
	private ArrayList<Set<String>> possibleFilters;
	private Set<String> filteredCourses;
	private int defaultSem;
	private String displayedCourses;
	private File savedSelectedCourses;
	private Context context, baseContext;
	private String TAG = "SelectCourses", message = "";
	private SparseArray<ArrayList<String[]>> lectureDetails;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_select_courses);
		courseDetails = Data.courseDetails;
		lectureDetails = Data.lectureDetails;
		possibleFilters = Data.possibleFilters;
		savedSelectedCourses = new File(getFilesDir(),
				"savedSelectedCourses.txt");
		inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		tvSelectedCourses = (TextView) findViewById(R.id.tvSelectedCourses);
		tvSelectedCourses.setMovementMethod(new ScrollingMovementMethod());
		cbSelectAll = (CheckBox) findViewById(R.id.selectAll);
		tvSelectedCourses.setTextColor(Color.GRAY);
		tvSelectedCourses.setText("(Select courses below)");
		selectedCourses = new ArrayList<String>();
		courses = new ArrayList<String>();
		courseCodes = new ArrayList<String>();
		checkedVals = new ArrayList<Boolean>();
		selectSem = (Spinner) findViewById(R.id.spSelectSem);
		Semester sem = new Semester();
		defaultSem = sem.getDefaultSem();
		selectSem.setSelection(defaultSem, true);
		ArrayAdapter<String> semAdapter = new ArrayAdapter<String>(
				getBaseContext(), R.layout.tv_sem, semesters);
		semAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		selectSem.setAdapter(semAdapter);
		displayedCourses = "";
		context = this;
		baseContext = getBaseContext();
		//get courses with filters applied
		try {
			FilteredCourses fc = new FilteredCourses(possibleFilters,  new File(
					getFilesDir(), "savedFiltersUser.txt"), context);
			filteredCourses = fc.getFilteredCourses();
			Log.i(TAG,"filteredCourses attained");
		} catch (FileNotFoundException e) {
			Log.e(TAG,"File IO: savedFilters not found");
			message = "Problem with files on your device.";		
			new ErrorDialog().show(message, context, baseContext);
			e.printStackTrace();
		} catch (IOException e) {
			Log.e(TAG,"File IO: problem with savedFiltersUser");
			message = "Problem with files on your device.";		
			new ErrorDialog().show(message, context, baseContext);
			e.printStackTrace();
		}
		
		//get all courses in current semester
		populateSelector(defaultSem);

		adapter = new ArrayAdapter<String>(this, R.layout.select_course_item,
				courses) {
			@Override
			public View getView(final int position, View convertView,
					ViewGroup parent) {
				//setting text and checkbox state of current view
				View view = null;
				if (convertView == null) {
					view = inflater.inflate(R.layout.select_course_item,
							null);
					viewHolder = new ViewHolder();

					viewHolder.courseCode = (TextView) view
							.findViewById(R.id.courseCode);
					viewHolder.checkBox = (CheckBox) view
							.findViewById(R.id.checkBox);
					view.setTag(viewHolder);
				} else {
					view = convertView;
					viewHolder = (ViewHolder) view.getTag();
				}
				viewHolder.courseCode.setText(courses.get(position));
				viewHolder.checkBox.setChecked(checkedVals.get(position));
				
				/**asdasd*/
				viewHolder.checkBox.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						//get the contents of selectedCourses arraylist, to avoid duplication
						selectedCourses = SelectedCourses
								.getSelectedCourses(savedSelectedCourses, message, context, baseContext);
						PrintWriter writer = null;
						try {
							writer = new PrintWriter(savedSelectedCourses);
							//clear the savedSelectedCourses file
							writer.print("");
							writer.close();
							Log.i(TAG, "savedSelectedCourses file wiped");
							if (((CheckBox) v).isChecked()) {
							//if checkbox checked, add selected course to selectedCourses arraylist
								checkedVals.set(position, true);
								if (!selectedCourses.contains(courseCodes
										.get(position))) {
									selectedCourses.add(courseCodes
											.get(position));
								}
							} else {
							//if checkbox checked, remove course from selectedCourses arraylist
								checkedVals.set(position, false);
								if (selectedCourses.contains(courseCodes
										.get(position))) {
									selectedCourses.remove(selectedCourses
											.indexOf(courseCodes.get(position)));
								}
							}
							Collections.sort(selectedCourses);
							BufferedWriter bw = null;
							bw = new BufferedWriter(new FileWriter(
									savedSelectedCourses));

							for (String C : selectedCourses) {
								bw.write(C);
								if (courseCodes.contains(C)) {
									checkedVals.set(courseCodes.indexOf(C),
											true);
								}
								bw.newLine();
							}
							bw.flush();
							bw.close();
							Log.i(TAG, "savedSelectedCourses written to and closed");
						} catch (FileNotFoundException e) {
							Log.e(TAG,"File IO: savedSelectedCourses not found");
							message = "Problem with files on your device.";		
							new ErrorDialog().show(message, context, baseContext);
							e.printStackTrace();
						} catch (IOException e) {
							Log.e(TAG,"File IO: problem writing to selectedCourses");
							message = "Problem with files on your device.";		
							new ErrorDialog().show(message, context, baseContext);
							e.printStackTrace();
						}

						displayedCourses = displayCourses();

						if (displayedCourses.equals("")) {
							tvSelectedCourses.setTextColor(Color.GRAY);
							tvSelectedCourses.setText("(Select courses below)");
						} else {
							tvSelectedCourses.setTextColor(Color.BLACK);
							tvSelectedCourses.setText(displayedCourses);
						}
						resetSelectAllButton();
					}

				});
				return view;
			}

		};
		if (savedSelectedCourses.length() > 0) {
			selectedCourses = SelectedCourses
					.getSelectedCourses(savedSelectedCourses, message, context, baseContext);
			Collections.sort(selectedCourses);
			displayedCourses = displayCourses();
			//show the selected courses in tvSelectedCourses or give warning
			if (displayedCourses.equals("")) {
				tvSelectedCourses.setTextColor(Color.GRAY);
				tvSelectedCourses.setText("(Select courses below)");
			} else {
				tvSelectedCourses.setTextColor(Color.BLACK);
				tvSelectedCourses.setText(displayedCourses);
			}
			for (String C : selectedCourses) {
				if (courseCodes.contains(C)) {
					checkedVals.set(courseCodes.indexOf(C), true);
				}
			}
			adapter.notifyDataSetChanged();
			resetSelectAllButton();
		}
		//show list of courses
		lvCourses = (ListView) findViewById(R.id.lvSelectedCourses);
		lvCourses.setAdapter(adapter);
		lvCourses.setClickable(true);
		lvCourses.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int position, long arg3) {
				inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				CreateDialog.getDialog(context, position, inflater, courseCodes);
			}

		});

		submit = (Button) findViewById(R.id.bSubmitChoices);
		submit.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				//display a warning if no courses selected, otherwise launch new activity
				if (selectedCourses.isEmpty()) {
					Toast.makeText(getBaseContext(), "Select some courses!",
							Toast.LENGTH_SHORT).show();
				} else {
					Intent results = new Intent(getBaseContext(),
							TimetableUser.class);
					startActivity(results);
				}
			}
		});

		cbSelectAll.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				//get selected courses from savedSelectedCourses
				selectedCourses = SelectedCourses
						.getSelectedCourses(savedSelectedCourses, message, context, baseContext);
				if (cbSelectAll.isChecked()) {
					for (int i = 0; i < checkedVals.size(); i++) {
						checkedVals.set(i, true);
						//only add to selectedCourses if not already in savedSelectedCourses
						if (!selectedCourses.contains(courseCodes.get(i))) {
							selectedCourses.add(courseCodes.get(i));
						}
					}
				} else {
					tvSelectedCourses.scrollTo(0, 0);
					for (int i = 0; i < checkedVals.size(); i++) {
						//only remove from selectedCourses if already in savedSelectedCourses
						checkedVals.set(i, false);
						if (selectedCourses.contains(courseCodes.get(i))) {
							selectedCourses.remove(selectedCourses
									.indexOf(courseCodes.get(i)));
						}
					}
				}

				PrintWriter writer = null;
				try {
					writer = new PrintWriter(savedSelectedCourses);
					//clear savedSelectedCourses
					writer.print("");
					writer.close();
					Log.i(TAG,"savedSelectedCourses contents cleared and closed");
				} catch (FileNotFoundException e) {
						Log.e(TAG,"File IO: savedSelectedCourses not found");
						message = "Problem with files on your device.";		
						new ErrorDialog().show(message, context, baseContext);
						e.printStackTrace();
				}
				
				BufferedWriter bw = null;
				try {
					bw = new BufferedWriter(
							new FileWriter(savedSelectedCourses));
					//writing selectedCourses to savedSelectedCourses
					for (String C : selectedCourses) {
						bw.write(C);
						if (courseCodes.contains(C)) {
							checkedVals.set(courseCodes.indexOf(C), true);
						}
						bw.newLine();
					}
					bw.flush();
					bw.close();
					Log.i(TAG,"savedSelectedCourses written to and closed");
				} catch (IOException e) {
					Log.e(TAG,"File IO: problem writing to savedSelectedCourses");
					message = "Problem with files on your device.";		
					new ErrorDialog().show(message, context, baseContext);
					e.printStackTrace();
				}
				//display selected courses in tvSelectedCourses or warning if none
				adapter.notifyDataSetChanged();
				resetSelectAllButton();
				tvSelectedCourses.setText(displayCourses());
				if (displayCourses().equals("")) {
					tvSelectedCourses.setTextColor(Color.GRAY);
					tvSelectedCourses.setText("(Select courses below)");
				} else {
					tvSelectedCourses.setTextColor(Color.BLACK);
				}

			}

		});

		selectSem.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int selectedSem, long arg3) {
				populateSelector(selectedSem);
				//ensure checkboxes display ticks for each selected course by updating checkedVals
				for (String C : courseCodes) {
					if (selectedCourses.contains(C)) {
						checkedVals.set(courseCodes.indexOf(C), true);
					}
				}
				adapter.notifyDataSetChanged();
				resetSelectAllButton();
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				//Do nothing
			}

		});

	}

	private void resetSelectAllButton() {
		boolean allSelected = true;
		//check if there are any courses in Courses arraylist not selected
		for (int i = 0; i < checkedVals.size(); i++) {

			if (!checkedVals.get(i)) {
				allSelected = false;
				break;
			}
		}

		cbSelectAll.setChecked(allSelected);
	}

	private void populateSelector(int selectedSem) {
		courses.clear();
		courseCodes.clear();
		checkedVals.clear();
		switch (selectedSem) {
		//get semester 1 courses, with filters applied
		case 0:
			for (String C : filteredCourses) {
				//if course code C has extension display it alongside the code
				if (courseDetails.containsKey(C)) {
					if (courseDetails.get(C)[Course.DELIVERY_PERIOD].equals("S1")
							|| courseDetails.get(C)[Course.DELIVERY_PERIOD].equals("YR")) {
						courses.add(C + " (" + courseDetails.get(C)[Course.FULL_NAME] + ")");
						courseCodes.add(C);
						checkedVals.add(false);
					}
				} else {
				//otherwise just display code
					for (int d = 0; d < lectureDetails.size(); d++) {
						for (int i = 0; i < lectureDetails.get(d).size(); i++) {
							if (lectureDetails.get(d).get(i)[Lecture.CODE].equals(C)
									&& lectureDetails.get(d).get(i)[Lecture.SEM]
											.equals("1")) {
								if (!courses.contains(C)) {
									courses.add(C);
									courseCodes.add(C);
									checkedVals.add(false);
								}
							}
						}
					}
				}
			}
			Log.i(TAG,"semester 1 courses retrieved");
			break;

		case 1:
		//get semester 2 courses, with filters applied	
			for (String C : filteredCourses) {
				if (courseDetails.containsKey(C)) {
				//if course code C has extension display it alongside the code
					if (courseDetails.get(C)[Course.DELIVERY_PERIOD].equals("S2")
							|| courseDetails.get(C)[Course.DELIVERY_PERIOD].equals("YR")) {
						courses.add(C + " (" + courseDetails.get(C)[Course.FULL_NAME] + ")");
						courseCodes.add(C);
						checkedVals.add(false);
					}
				} else {
				//otherwise just display code
					for (int d = 0; d < lectureDetails.size(); d++) {
						for (int i = 0; i < lectureDetails.get(d).size(); i++) {
							if (lectureDetails.get(d).get(i)[Lecture.CODE].equals(C)
									&& lectureDetails.get(d).get(i)[Lecture.SEM]
											.equals("2")) {
								if (!courses.contains(C)) {
									courses.add(C);
									courseCodes.add(C);
									checkedVals.add(false);
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
				//display the full name
				if (courseDetails.containsKey(C)) {
					courses.add(C + " (" + courseDetails.get(C)[Course.FULL_NAME] + ")");
				} else {
					//otherwise just display code
					courses.add(C);
				}
				courseCodes.add(C);
				checkedVals.add(false);		
			}
			Log.i(TAG,"courses in either semester retrieved");
			break;

		}

	}

	public String displayCourses() {

		String displayedCourses = "";
		BufferedReader in = null;
		ArrayList<String> displayedCoursesList = new ArrayList<String>();
		try {
			in = new BufferedReader(new FileReader(savedSelectedCourses));
			String str = "";			
			//read from savedSelectedCourses into displayedCoursesList
			while ((str = in.readLine()) != null) {
				displayedCoursesList.add(str);
			}
			in.close();
			Log.i(TAG, "savedSelectedCourses read from and closed");
		} catch (FileNotFoundException e) {
			Log.e(TAG,"File IO: savedSelectedCourses not found");
			message = "Problem with files on your device.";		
			new ErrorDialog().show(message, context, baseContext);
			e.printStackTrace();
		} catch (IOException e) {
			Log.e(TAG,"File IO: problem reading from savedSelectedCourses");
			message = "Problem with files on your device.";		
			new ErrorDialog().show(message, context, baseContext);
			e.printStackTrace();
		}
		Collections.sort(displayedCoursesList);
		for (String C : displayedCoursesList) {
			if (courseDetails.containsKey(C)) {
				displayedCourses += C + " (" + courseDetails.get(C)[0] + ")"
						+ "\n";
			} else {
				displayedCourses += C + "\n";
			}
		}
		return displayedCourses;
	}


	private class ViewHolder {
		TextView courseCode;
		CheckBox checkBox;
	}

	@Override
	protected void onResume() {
		super.onResume();
		Bundle savedInstanceState = new Bundle();
		onCreate(savedInstanceState);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.select_courses_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.GenTableSelect:
			startActivity(new Intent(getBaseContext(), TimetableGen.class));
			return true;
		case R.id.FiltersSelect:
			startActivity(new Intent(getBaseContext(), FiltersUser.class));
			return true;
		case R.id.SearchSelect:
			startActivity(new Intent(getBaseContext(), SearchUser.class));
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
