package general.timetable;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;


import org.apache.commons.io.FileUtils;

import user.timetable.SelectCourses;
import user.timetable.TimetableUser;

import com.selp.edtimetable.ErrorDialog;
import com.selp.edtimetable.FilteredCourses;
import com.selp.edtimetable.R;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import app.startup.Data;
/**Filters screen for the general timetable, showing filters previously selected by reading from
 * savedFiltersGen.txt
 */
public class FiltersGen extends Activity {
	
	private Button yearFilterButton;
	private Button degreeFilterButton;
	private Button pointsFilterButton;
	private Button levelFilterButton;
	private Button clearFilters;
	private Button applyFilters;
	private AlertDialog yearFilterDialog;
	private AlertDialog degreeFilterDialog;
	private AlertDialog pointsFilterDialog;
	private AlertDialog levelFilterDialog;
	private Context context;
	private boolean[] yearsSelected;
	private boolean[] savedYearsSelected;
	private boolean[] degreesSelected;
	private boolean[] pointsSelected;
	private boolean[] levelsSelected;
	private boolean[] savedDegreesSelected;
	private boolean[] savedPointsSelected;
	private boolean[] savedLevelsSelected;
	private String[] years;
	private String[] degrees = { "Artificial Intelligence",
			"Cognitive Science", "Computer Science", "Software Engineering" };
	private String[] points;
	private String[] levels;
	private File savedFilters;
	private TextView tvYearFilters;
	private TextView tvDegreeFilters;
	private TextView tvPointsFilters;
	private TextView tvLevelFilters;
	private ArrayList<Set<String>> possibleFilters;
	private FilteredCourses fc;
	private String message = "", TAG = "FiltersGen";
	private boolean valid;
	private Context baseContext;
	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_filters_gen);
		tvYearFilters = (TextView) findViewById(R.id.tvYearFilters);
		tvYearFilters.setMovementMethod(new ScrollingMovementMethod());
		tvDegreeFilters = (TextView) findViewById(R.id.tvDegreeFilters);
		tvPointsFilters = (TextView) findViewById(R.id.tvCreditsFilters);
		tvPointsFilters.setMovementMethod(new ScrollingMovementMethod());
		tvLevelFilters = (TextView) findViewById(R.id.tvLevelFilters);
		tvLevelFilters.setMovementMethod(new ScrollingMovementMethod());
		savedFilters = new File(getFilesDir(), "savedFiltersGen.txt");
		context = this;
		possibleFilters = Data.possibleFilters;
		savedYearsSelected = new boolean[possibleFilters.get(0).size()];
		savedDegreesSelected = new boolean[4];
		savedPointsSelected = new boolean[possibleFilters.get(1).size()];
		savedLevelsSelected = new boolean[possibleFilters.get(2).size()];
		baseContext = getBaseContext();
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
		Log.i(TAG,"finished sorting filters");
		if (savedFilters.length() > 0) {
			//reading savedFilters file to acquire the previously saved filters
			BufferedReader in = null;
			try {
				in = new BufferedReader(new FileReader(savedFilters));
				Log.i(TAG,"inputStream opened successfully");
				String str;
				int i = 0;
				while ((str = in.readLine()) != null) {
					if (i < years.length) {
						if (str.equals("true")) {
							savedYearsSelected[i] = true;
						}
					} else if (i >= years.length && i < (years.length + 4)) {
						if (str.equals("true")) {
							savedDegreesSelected[i - years.length] = true;
						}
					} else if (i >= years.length + 4
							&& i < years.length + 4 + points.length) {
						if (str.equals("true")) {
							savedPointsSelected[i - (years.length + 4)] = true;
						}
					} else if (i >= years.length + 4 + points.length) {
						if (str.equals("true")) {
							savedLevelsSelected[i
									- (years.length + 4 + points.length)] = true;
						}
					}
					i++;
				}		
				in.close();
				Log.i(TAG, "file savedFilters read from and closed");
			} catch (FileNotFoundException e) {
				Log.e(TAG,"File IO: savedFilters not found");
				message = "Problem with files on your device.";		
				new ErrorDialog().show(message, context, baseContext);
				e.printStackTrace();
			} catch (IOException e) {
				Log.e(TAG,"File IO: problem reading from savedFilters");
				message = "Problem with files on your device.";		
				new ErrorDialog().show(message, context, baseContext);
				e.printStackTrace();
			}

		}

		displayCurrentFilters();

		yearsSelected = new boolean[years.length];
		degreesSelected = new boolean[4];
		pointsSelected = new boolean[points.length];
		levelsSelected = new boolean[levels.length];
		context = this;
		//make dialog for year filters when yearFilterButton pressed
		yearFilterButton = (Button) findViewById(R.id.bYearFilter);
		yearFilterButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				yearsSelected = savedYearsSelected.clone();
				AlertDialog.Builder builder = new AlertDialog.Builder(context);
				builder.setTitle("Select Year(s) to Filter");
				builder.setMultiChoiceItems(years, yearsSelected,
						new DialogInterface.OnMultiChoiceClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int position, boolean isChecked) {
								yearsSelected[position] = isChecked;
							}
						});
				builder.setPositiveButton("OK",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int arg1) {
								boolean[] tempSelected = savedYearsSelected
										.clone();
								savedYearsSelected = yearsSelected.clone();
								saveState();
								if (!valid) {
									//if no results for filters, reset filters
									savedYearsSelected = tempSelected.clone();
									for (int i = 0; i < possibleFilters.get(0)
											.size(); i++) {
										((AlertDialog) dialog).getListView()
												.setItemChecked(i,
														savedYearsSelected[i]);
									}
								}
								displayCurrentFilters();
							}
						});
				builder.setNegativeButton("Cancel",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int arg1) {
								for (int i = 0; i < possibleFilters.get(0)
										.size(); i++) {
									((AlertDialog) dialog).getListView()
											.setItemChecked(i,
													savedYearsSelected[i]);
								}
							}
						});
				yearFilterDialog = builder.create();
				yearFilterDialog.show();
			}
		});
		//make dialog for degree filters when yearFilterButton pressed
		degreeFilterButton = (Button) findViewById(R.id.bDegree);
		degreeFilterButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				degreesSelected = savedDegreesSelected.clone();
				AlertDialog.Builder builder = new AlertDialog.Builder(context);
				builder.setTitle("Select Degree(s) to Filter");
				builder.setMultiChoiceItems(degrees, degreesSelected,
						new DialogInterface.OnMultiChoiceClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int position, boolean isChecked) {
								degreesSelected[position] = isChecked;
							}
						});
				builder.setPositiveButton("OK",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int arg1) {
								boolean[] tempSelected = savedDegreesSelected
										.clone();
								savedDegreesSelected = degreesSelected.clone();
								saveState();
								if (!valid) {
									//if no results for filters, reset filters
									savedDegreesSelected = tempSelected.clone();
									for (int i = 0; i < 4; i++) {
										((AlertDialog) dialog)
												.getListView()
												.setItemChecked(i,
														savedDegreesSelected[i]);
									}
								}
								displayCurrentFilters();
							}
						});
				builder.setNegativeButton("Cancel",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int arg1) {
								for (int i = 0; i < 4; i++) {
									((AlertDialog) dialog).getListView()
											.setItemChecked(i,
													savedDegreesSelected[i]);
								}
							}

						});
				degreeFilterDialog = builder.create();
				degreeFilterDialog.show();
			}
		});
		//make dialog for year points when pointsFilterButton pressed
		pointsFilterButton = (Button) findViewById(R.id.bPoints);
		pointsFilterButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				pointsSelected = savedPointsSelected.clone();
				AlertDialog.Builder builder = new AlertDialog.Builder(context);
				builder.setTitle("Select point(s) to Filter");
				builder.setMultiChoiceItems(points, pointsSelected,
						new DialogInterface.OnMultiChoiceClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int position, boolean isChecked) {
								pointsSelected[position] = isChecked;
							}

						});
				builder.setPositiveButton("OK",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int arg1) {
								boolean[] tempSelected = savedPointsSelected
										.clone();
								savedPointsSelected = pointsSelected.clone();
								saveState();
								if (!valid) {
									//if no results for filters, reset filters
									savedPointsSelected = tempSelected.clone();
									for (int i = 0; i < possibleFilters.get(1)
											.size(); i++) {
										((AlertDialog) dialog)
												.getListView()
												.setItemChecked(i,
														savedPointsSelected[i]);
									}
								}
								displayCurrentFilters();
							}
						});
				builder.setNegativeButton("Cancel",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int arg1) {
								for (int i = 0; i < possibleFilters.get(1)
										.size(); i++) {
									((AlertDialog) dialog).getListView()
											.setItemChecked(i,
													savedPointsSelected[i]);
								}
							}
						});
				pointsFilterDialog = builder.create();
				pointsFilterDialog.show();
			}
		});
		//make dialog for year levels when levelsFilterButton pressed
		levelFilterButton = (Button) findViewById(R.id.bLevel);
		levelFilterButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				levelsSelected = savedLevelsSelected.clone();
				AlertDialog.Builder builder = new AlertDialog.Builder(context);
				builder.setTitle("Select level(s) to Filter");
				builder.setMultiChoiceItems(levels, levelsSelected,
						new DialogInterface.OnMultiChoiceClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int position, boolean isChecked) {
								levelsSelected[position] = isChecked;
							}

						});
				builder.setPositiveButton("OK",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int arg1) {
								boolean[] tempSelected = savedLevelsSelected
										.clone();
								savedLevelsSelected = levelsSelected.clone();
								saveState();
								if (!valid) {
								//if no results for filters, reset filters
									savedLevelsSelected = tempSelected.clone();
									for (int i = 0; i < possibleFilters.get(2)
											.size(); i++) {
										((AlertDialog) dialog).getListView()
												.setItemChecked(i,
														savedLevelsSelected[i]);
									}
								}
								displayCurrentFilters();
							}
						});
				builder.setNegativeButton("Cancel",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int arg1) {
								for (int i = 0; i < possibleFilters.get(2)
										.size(); i++) {
									((AlertDialog) dialog).getListView()
											.setItemChecked(i,
													savedLevelsSelected[i]);
								}
							}
						});
				levelFilterDialog = builder.create();
				levelFilterDialog.show();
			}
		});
		clearFilters = (Button) findViewById(R.id.bClearFilters);
		clearFilters.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				clearFilters();
				startActivity(new Intent(getBaseContext(), FiltersGen.class));
			}

		});

		applyFilters = (Button) findViewById(R.id.bApplyFilters);
		applyFilters.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				startActivity(new Intent(getBaseContext(), TimetableGen.class));
			}

		});

	}

	private void saveState() {
		//save the previously saved filters to a temp file
		File temp = new File(getFilesDir(), "tempGen.txt");
		try {
			FileUtils.copyFile(savedFilters, temp);
			Log.i(TAG,"savedFilters copied to temp");
		} catch (IOException e) {
			Log.e(TAG,"File IO problem: copying file savedFilters to tempGen");
			message = "Problem with files on your device.";		
			new ErrorDialog().show(message, context, baseContext);
			e.printStackTrace();
		}
		//update the file to contain the filters currently applied
		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(new FileWriter(savedFilters));

			for (int i = 0; i < savedYearsSelected.length; i++) {
				bw.write("" + savedYearsSelected[i]);
				bw.newLine();
			}
			for (int i = 0; i < 4; i++) {
				bw.write("" + savedDegreesSelected[i]);
				bw.newLine();
			}
			for (int i = 0; i < savedPointsSelected.length; i++) {
				bw.write("" + savedPointsSelected[i]);
				bw.newLine();
			}
			for (int i = 0; i < savedLevelsSelected.length; i++) {
				bw.write("" + savedLevelsSelected[i]);
				bw.newLine();
			}
			bw.flush();
			bw.close();
			Log.i(TAG,"File written to and closed");
		} catch (IOException e) {
			Log.e(TAG,"File IO problem: problem writing to savedFilters");
			message = "Problem with files on your device.";		
			new ErrorDialog().show(message, context, baseContext);
			e.printStackTrace();
		}
		//attain the results if the current filters are applied
		try {
			fc = new FilteredCourses(possibleFilters, savedFilters, context);
		} catch (FileNotFoundException e) {
			Log.e(TAG,"File IO: savedFilters not found");
			message = "Problem with files on your device.";		
			new ErrorDialog().show(message, context, baseContext);
			e.printStackTrace();
		} catch (IOException e) {
			Log.e(TAG,"File IO: problem with savedFilters");
			message = "Problem with files on your device.";		
			new ErrorDialog().show(message, context, baseContext);
			e.printStackTrace();
		}
		//if no results for the current filters, revert to previous filters file (temp) and display message
		if (fc.getFilteredCourses().isEmpty()) {
			try {
				FileUtils.copyFile(temp, savedFilters);
				Log.i(TAG,"invalid filters, temp copied to savedFilters");
			} catch (IOException e) {
				Log.e(TAG,"problem copying file tempGen to savedFilters");
				message = "Problem with files on your device.";		
				new ErrorDialog().show(message, context, baseContext);
				e.printStackTrace();
			}
			Toast.makeText(getBaseContext(),
					"No results after that change filter(s)!",
					Toast.LENGTH_SHORT).show();
			valid = false;
		} else {
			valid = true;
		}
		temp.delete();


	}

	private void displayCurrentFilters() {
		String displayFilters = "";
		//display all the year filters selected
		for (int i = 0; i < savedYearsSelected.length; i++) {
			if (savedYearsSelected[i])
				displayFilters += "" + years[i] + "\n";
		}
		if (displayFilters.length() == 0) {
			tvYearFilters.setText("(No filters selected)");
			tvYearFilters.setTextColor(Color.GRAY);
		} else {
			tvYearFilters.setText(displayFilters);
			tvYearFilters.setTextColor(Color.BLACK);
		}
		displayFilters = "";
		//display all the degree filters selected
		for (int i = 0; i < 4; i++) {
			if (savedDegreesSelected[i])
				displayFilters += "" + degrees[i] + "\n";
		}
		if (displayFilters.length() == 0) {
			tvDegreeFilters.setText("(No filters selected)");
			tvDegreeFilters.setTextColor(Color.GRAY);
		} else {
			tvDegreeFilters.setText(displayFilters);
			tvDegreeFilters.setTextColor(Color.BLACK);
		}
		displayFilters = "";
		//display all the points filters selected
		for (int i = 0; i < savedPointsSelected.length; i++) {
			if (savedPointsSelected[i])
				displayFilters += "" + points[i] + "\n";
		}
		if (displayFilters.length() == 0) {
			tvPointsFilters.setText("(No filters selected)");
			tvPointsFilters.setTextColor(Color.GRAY);
		} else {
			tvPointsFilters.setText(displayFilters);
			tvPointsFilters.setTextColor(Color.BLACK);
		}
		displayFilters = "";
		//display all the level filters selected
		for (int i = 0; i < savedLevelsSelected.length; i++) {
			if (savedLevelsSelected[i])
				displayFilters += "" + levels[i] + "\n";
		}
		if (displayFilters.length() == 0) {
			tvLevelFilters.setText("(No filters selected)");
			tvLevelFilters.setTextColor(Color.GRAY);
		} else {
			tvLevelFilters.setText(displayFilters);
			tvLevelFilters.setTextColor(Color.BLACK);
		}
		Log.i(TAG,"filters displayed");
	}

	private void clearFilters() {
		savedYearsSelected = new boolean[possibleFilters.get(0).size()];
		savedDegreesSelected = new boolean[4];
		savedPointsSelected = new boolean[possibleFilters.get(1).size()];
		savedLevelsSelected = new boolean[possibleFilters.get(2).size()];
		saveState();

	}

	@Override
	public void onBackPressed() {
		//do nothing
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.filters_gen_menu, menu);
		File savedSelectedCourses = new File(getFilesDir(),
				"savedSelectedCourses.txt");
		MenuItem item = menu.findItem(R.id.UserTableFilters);
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
		case R.id.UserTableFilters:
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
