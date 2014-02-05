package user.timetable;

import general.timetable.TimetableGen;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;

import org.apache.commons.lang3.StringUtils;

import com.selp.edtimetable.CreateDialog;
import com.selp.edtimetable.ErrorDialog;
import com.selp.edtimetable.R;
import com.selp.edtimetable.SelectedCourses;
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
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
/**Search screen for the user timetable, allowing user to press course to see more information
 * and select it for their timetable**/
public class SearchUser extends Activity {
	
	private ArrayList<String> tempCourses2;
	private ArrayList<String> tempCourseCodes2;
	private EditText etSearch;
	private ArrayAdapter<String> adapter;
	private LayoutInflater inflater;
	private ArrayList<String> courses;
	private ArrayList<String> courseCodes;
	private ArrayList<Boolean> checkedVals;
	private File savedSelectedCourses;
	private ArrayList<String> selectedCourses;
	private ListView lvCourses;
	private ArrayList<Boolean> tempCheckedVals2;
	private Context context, baseContext;
	private String TAG = "SearchUser", message;
	private Button bFinishSearch;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_search);
		tempCourses2 = new ArrayList<String>();
		tempCourseCodes2 = new ArrayList<String>();
		tempCheckedVals2 = new ArrayList<Boolean>();
		inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		courses = SelectCourses.courses;
		courseCodes = SelectCourses.courseCodes;
		checkedVals = SelectCourses.checkedVals;
		savedSelectedCourses = new File(getFilesDir(),
				"savedSelectedCourses.txt");
		selectedCourses = SelectCourses.selectedCourses;
		context = this;
		baseContext = getBaseContext();
		adapter = new ArrayAdapter<String>(this, R.layout.select_course_item,
				courses) {
			private user.timetable.SearchUser.ViewHolder viewHolder;

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
				viewHolder.checkBox.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						//get the contents of selectedCourses arraylist, to avoid duplication
						selectedCourses = SelectedCourses
								.getSelectedCourses(savedSelectedCourses, message, context, baseContext);
						PrintWriter writer = null;
						try {
							writer = new PrintWriter(savedSelectedCourses);	
							if (((CheckBox) v).isChecked()) {
								//if checkbox checked, add selected course to selectedCourses arraylist
								checkedVals.set(position, true);
								if (!selectedCourses.contains(courseCodes
										.get(position))) {
									selectedCourses.add(courseCodes.get(position));
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
							//clear savedSelectedCourses file 
							writer.print("");
							writer.close();
							Log.i(TAG, "savedSelectedCourses contents wiped");
							BufferedWriter bw = null;
							//write course codes selected to savedSelectedCourses unless already contained
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
							Log.e(TAG,"File IO: problem writing to savedSelectedCourses");
							message = "Problem with files on your device.";		
							new ErrorDialog().show(message, context, baseContext);
							e.printStackTrace();
						}
					}
				});
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
				CreateDialog.getDialog(context, position, inflater,
						courseCodes);
				Log.i(TAG, "Dialog created");

			}
		});
		etSearch = (EditText) findViewById(R.id.etSearch);
		etSearch.setSingleLine();
		etSearch.addTextChangedListener(new TextWatcher() {

			@Override
			public void afterTextChanged(Editable s) {
				//do nothing
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				//do nothing
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
					checkedVals.clear();
					checkedVals.addAll(tempCheckedVals2);

				} else {
					//save info for all courses in temp variables if first search since activity launch
					tempCourses2.addAll(courses);
					tempCourseCodes2.addAll(courseCodes);
					tempCheckedVals2.addAll(checkedVals);

				}
				
				ArrayList<String> tempCourses = new ArrayList<String>();
				ArrayList<String> tempCourseCodes = new ArrayList<String>();
				ArrayList<Boolean> tempCheckedVals = new ArrayList<Boolean>();
				//find courses which match what the user types in
				for (int i = 0; i < courses.size(); i++) {
					if (StringUtils.containsIgnoreCase(courses.get(i), etSearch
							.getText().toString())) {
						tempCourses.add(courses.get(i));
						tempCourseCodes.add(courseCodes.get(i));
						tempCheckedVals.add(checkedVals.get(i));
					}
				}
				//make course variables only contain the matching courses
				courses.clear();
				courses.addAll(tempCourses);
				courseCodes.clear();
				courseCodes.addAll(tempCourseCodes);
				checkedVals.clear();
				checkedVals.addAll(tempCheckedVals);
				//ensure checkboxes display ticks for each selected course by updating checkedVals
				for (String C : courseCodes) {
					checkedVals.set(courseCodes.indexOf(C),
							selectedCourses.contains(C));
				}

				adapter.notifyDataSetChanged();
				Log.i(TAG, "search results updated");
			}

		});
		bFinishSearch = (Button) findViewById(R.id.bFinishSearch);
		bFinishSearch.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				startActivity(new Intent(getBaseContext(),SelectCourses.class));
			}
			
		});

	}

	private class ViewHolder {
		TextView courseCode;
		CheckBox checkBox;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.search_user_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case R.id.GenTableSearch:
			startActivity(new Intent(getBaseContext(), TimetableGen.class));
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
}
