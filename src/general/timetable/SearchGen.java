package general.timetable;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.lang3.StringUtils;

import user.timetable.SelectCourses;
import user.timetable.TimetableUser;

import com.selp.edtimetable.CreateDialog;
import com.selp.edtimetable.R;
import com.selp.edtimetable.IndexLabels.Course;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import app.startup.Data;
/**Search screen for the general timetable, allowing user to press course to see more information**/
public class SearchGen extends Activity {
	
	private ArrayList<String> tempCourses2;
	private ArrayList<String> tempCourseCodes2;
	private EditText etSearch;
	private ArrayAdapter<String> adapter;
	private LayoutInflater inflater;
	private ArrayList<String> courses;
	private ArrayList<String> courseCodes;
	private ListView lvCourses;
	private ArrayList<String> courseCodeList;
	private HashMap<String, String[]> courseDetails;
	private String TAG = "SearchGen";
	private Context context;
	private Button bFinishSearch;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search);
		tempCourses2 = new ArrayList<String>();
		tempCourseCodes2 = new ArrayList<String>();

		courseCodeList = Data.courseCodeList;
		courseDetails = Data.courseDetails;
		context = this;
		inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		courses = new ArrayList<String>();
		courseCodes = new ArrayList<String>();
		//attempt to get the full course name for every course, otherwise use acronym
		for (String C : courseCodeList) {
			courseCodes.add(C);
			if (courseDetails.containsKey(C)) {
				courses.add(C + " (" + courseDetails.get(C)[Course.FULL_NAME] + ")");
			} else {
				courses.add(C);
			}
		}
		
		adapter = new ArrayAdapter<String>(this, R.layout.select_course_item,
				courses) {

			@Override
			public View getView(final int position, View convertView,
					ViewGroup parent) {
				
				View view = inflater.inflate(R.layout.select_course_item,
						null);
				TextView course = (TextView) view
						.findViewById(R.id.courseCode);
				CheckBox checkBox = (CheckBox) view
						.findViewById(R.id.checkBox);
				checkBox.setVisibility(View.GONE);
				course.setText(courses.get(position));
			
				return view;
			}
		};
		//show list of courses
		lvCourses = (ListView) findViewById(R.id.lvSelectedCourses);
		lvCourses.setAdapter(adapter);
		lvCourses.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int position, long arg3) {
				inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				CreateDialog.getDialog(context, position, inflater, courseCodes);
				Log.i(TAG, "Dialog created");
			}
		});
		etSearch = (EditText) findViewById(R.id.etSearch);
		etSearch.setSingleLine();
		etSearch.addTextChangedListener(new TextWatcher() {

			@Override
			public void afterTextChanged(Editable s) {
				 // Do nothing. 
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				 // Do nothing. 
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {

				if (tempCourses2.size() > 0) {
					//reset course variables to include info about all courses if previous search
					courses.clear();
					courses.addAll(tempCourses2);
					courseCodes.clear();
					courseCodes.addAll(tempCourseCodes2);

				} else {
					//save info for all courses in temp variables if first search since activity launch
					tempCourses2.addAll(courses);
					tempCourseCodes2.addAll(courseCodes);

				}
				
				ArrayList<String> tempCourses = new ArrayList<String>();
				ArrayList<String> tempCourseCodes = new ArrayList<String>();
				//find courses which match what the user types in
				for (int i = 0; i < courses.size(); i++) {
					if (StringUtils.containsIgnoreCase(courses.get(i), etSearch
							.getText().toString())) {
						tempCourses.add(courses.get(i));
						tempCourseCodes.add(courseCodes.get(i));
					}
				}
				//make course variables only contain the matching courses
				courses.clear();
				courses.addAll(tempCourses);
				courseCodes.clear();
				courseCodes.addAll(tempCourseCodes);

				adapter.notifyDataSetChanged();
				Log.i(TAG, "search results updated");
			}

		});
		bFinishSearch = (Button) findViewById(R.id.bFinishSearch);
		bFinishSearch.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				startActivity(new Intent(getBaseContext(),TimetableGen.class));
			}
			
		});

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.search_gen_menu, menu);
		File savedSelectedCourses = new File(getFilesDir(),
				"savedSelectedCourses.txt");
		MenuItem item = menu.findItem(R.id.UserTableSearch);
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
		case R.id.UserTableSearch:
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
}
