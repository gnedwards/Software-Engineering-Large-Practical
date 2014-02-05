package app.startup;

import general.timetable.TimetableGen;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.selp.edtimetable.ErrorDialog;
import com.selp.edtimetable.R;
import user.timetable.TimetableUser;

import android.app.Activity;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;
/**Downloads and parses data, displays warnings if there are problems **/
public class Splash extends Activity {

	private Document timetableParsed;
	private Document coursesParsed;
	private Document venuesParsed;
	private ProgressBar spinner;
	private boolean isConnection;
	private Context context;
	private boolean noData;
	private String TAG = "Splash";
	private String message;
	private Context baseContext;
	public static Data data;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash);
		spinner = (ProgressBar) findViewById(R.id.spinner);		
		context = this;
		baseContext = getBaseContext();
		isConnection = true;
		DownloadData downloadData = new DownloadData();
		downloadData.execute();
		
	}

	private class DownloadData extends AsyncTask<Context, Object, Boolean> {


		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			//indicate loading
			spinner.setVisibility(View.VISIBLE);
		}

		@Override
		protected void onProgressUpdate(Object... values) {
			super.onProgressUpdate(values);
		}

		@Override
		protected Boolean doInBackground(Context... arg0) {
			URL[] url = new URL[3];
			File[] file = new File[3];
			//try to download each xml in turn if connection
			if (isConnection) {
				try {
					url[0] = new URL("http://www.inf.ed.ac.uk/teaching/courses/selp/xml/timetable.xml");
					url[1] = new URL("http://www.inf.ed.ac.uk/teaching/courses/selp/xml/courses.xml");
					url[2] = new URL("http://www.inf.ed.ac.uk/teaching/courses/selp/xml/venues.xml");
				} catch (MalformedURLException e) {
					Log.e(TAG,"Url malformed");
					message = "Unable to download necessary files.";
					isConnection = false;
					e.printStackTrace();
				}

				String[] fileName = { "timetable.xml", "courses.xml",
						"venues.xml" };
				try {
					for (int i = 0; i < 3; i++) {
						URLConnection urlConnection = url[i].openConnection();
						urlConnection.connect();
						file[i] = new File(getFilesDir(), fileName[i]);
						InputStream inputStream = urlConnection
								.getInputStream();
						FileOutputStream fileOutput = new FileOutputStream(
								file[i]);
						byte[] fileBuffer = new byte[90000];
						int bufferLength = 0;
						while ((bufferLength = inputStream.read(fileBuffer)) > 0) {
							fileOutput.write(fileBuffer, 0, bufferLength);
						}
						inputStream.close();
						fileOutput.flush();
						fileOutput.close();
					}
				} catch (MalformedURLException e) {
					Log.e(TAG,"Url malformed");
					message = "Unable to download necessary files. ";
					e.printStackTrace();
					isConnection = false;
					
				} catch (IOException e) {
					e.printStackTrace();
					Log.e(TAG,"Failed to put downloaded data into file");
					message = "Unable to download necessary files. Check your connection!";	
					isConnection = false;
				}
				Log.i(TAG,"Download finished");
			}if (!isConnection) {
				//if no internet connection and create files, 
				file[0] = new File(getFilesDir(), "timetable.xml");
				file[1] = new File(getFilesDir(), "courses.xml");
				file[2] = new File(getFilesDir(), "venues.xml");
				//if no internet connection or previous files
				noData = !(file[0].exists() && file[1].exists() && file[2]
						.exists());
			}

			if (!noData) {
				//parse files
				DocumentBuilderFactory dbf = DocumentBuilderFactory
						.newInstance();
				DocumentBuilder db;
				try {
					db = dbf.newDocumentBuilder();
					timetableParsed = db.parse(file[0]);
					coursesParsed = db.parse(file[1]);
					venuesParsed = db.parse(file[2]);
					Log.i(TAG,"parsing finished");
				} catch (ParserConfigurationException e) {
					Log.e(TAG,"Parser configuration");
					message = "Problem with files on server.";
					noData = true;
					e.printStackTrace();
				} catch (SAXException e) {
					Log.e(TAG,"XML malformed");		
					message = "Problem with files on server.";
					noData = true;
					e.printStackTrace();
				} catch (IOException e) {
					Log.e(TAG,"File IO when parsing");
					message = "Problem with files on server.";
					noData = true;
					e.printStackTrace();
				}
				data = new Data(timetableParsed, coursesParsed, venuesParsed);
				Log.i(TAG,"data structures filled");
			}
			return true;
		}

		@Override
		protected void onPostExecute(Boolean doPostExecute) {
			super.onPostExecute(doPostExecute);
			spinner.setVisibility(View.INVISIBLE);
			//clear previously saved filters
			File savedFiltersUser = new File(getFilesDir(), "savedFiltersUser.txt");
			File savedFiltersGen = new File(getFilesDir(), "savedFiltersGen.txt");
			File[] savedFilters = {savedFiltersUser,savedFiltersGen};
			for (File f: savedFilters) {
				PrintWriter writer = null;
				try {
					writer = new PrintWriter(f);
					writer.print("");
					writer.close();
				} catch (FileNotFoundException e) {
					Log.e(TAG,"Filters files not found");
					message = "Required files missing";
					new ErrorDialog().show(message, context, baseContext);
					e.printStackTrace();
				}
			}
			if (!isConnection && !noData) {
				Toast.makeText(
						context,
						"No internet connection! Using outdated data previously downloaded.",
						Toast.LENGTH_LONG).show();
			}

			if (!noData) {			
				File savedSelectedCourses = new File(getFilesDir(),
						"savedSelectedCourses.txt");
				if (savedSelectedCourses.length() > 0) {
					startActivity(new Intent(getBaseContext(),
							TimetableUser.class));
				} else {
					startActivity(new Intent(getBaseContext(),
							TimetableGen.class));
				}
			} else {
				//if any previous exceptions:
				message = "No internet connection or previously saved data. Check your connection!";
				new ErrorDialog().show(message, context, baseContext);;
			}

		}

	}

	@Override
	public void onBackPressed() {
		//Do nothing
	}

}
