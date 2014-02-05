package app.startup;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.selp.edtimetable.IndexLabels.Building;
import com.selp.edtimetable.IndexLabels.Course;
import com.selp.edtimetable.IndexLabels.Lecture;

import android.util.SparseArray;
/**Takes parsed XMLs and extracts information and puts them in global static data structures 
 * so that lecture and course information can be accessed throughout the app
 **/
public class Data {
	
	public static HashMap<String, String[]> courseDetails;
	public static HashMap<String, String> roomDetails;
	public static HashMap<String, String[]> buildingDetails;
	public static SparseArray<ArrayList<String[]>> lectureDetails;
	public static ArrayList<Set<String>> possibleFilters;
	private Set<String> possibleYears;
	private Set<String> possiblePoints;
	private Set<String> possibleLevels;
	public static ArrayList<String> courseCodeList;

	public Data(Document timetableParsed, Document coursesParsed,
			Document venuesParsed) {

		courseDetails = new HashMap<String, String[]>();
		lectureDetails = new SparseArray<ArrayList<String[]>>();
		roomDetails = new HashMap<String, String>();
		buildingDetails = new HashMap<String, String[]>();
		courseCodeList = new ArrayList<String>();
		timetableParsed.getDocumentElement().normalize();
		coursesParsed.getDocumentElement().normalize();
		venuesParsed.getDocumentElement().normalize();

		NodeList semester = timetableParsed.getElementsByTagName("semester");
		NodeList list = coursesParsed.getElementsByTagName("course");
		NodeList rooms = venuesParsed.getElementsByTagName("room");
		NodeList buildings = venuesParsed.getElementsByTagName("building");
		possibleYears = new HashSet<String>();
		possiblePoints = new HashSet<String>();
		possibleLevels = new HashSet<String>();

		// Populating a details array for each course c from parsed courses.xml.
		// The details array is inserted into hash map - its key is the acronym
		// of course c.
		for (int c = 0; c < list.getLength(); c++) {
			Element course = (Element) list.item(c);
			String[] details = new String[13];
			details[Course.FULL_NAME] = ""
					+ course.getElementsByTagName("name").item(0)
							.getTextContent();
			details[Course.URL] = ""
					+ course.getElementsByTagName("url").item(0)
							.getTextContent();
			details[Course.DRPS] = ""
					+ course.getElementsByTagName("drps").item(0)
							.getTextContent();
			details[Course.EUCLID] = ""
					+ course.getElementsByTagName("euclid").item(0)
							.getTextContent();
			details[Course.AI] = course.getElementsByTagName("ai").item(0)
					.getTextContent();
			details[Course.CG] = course.getElementsByTagName("cg").item(0)
					.getTextContent();
			details[Course.CS] = course.getElementsByTagName("cs").item(0)
					.getTextContent();
			details[Course.SE] = course.getElementsByTagName("se").item(0)
					.getTextContent();
			details[Course.LEVEL] = ""
					+ course.getElementsByTagName("level").item(0)
							.getTextContent();
			// noting that the current level is a possible value to filter on
			possibleLevels.add(course.getElementsByTagName("level").item(0)
					.getTextContent());
			details[Course.POINTS] = ""
					+ course.getElementsByTagName("points").item(0)
							.getTextContent();
			// noting that the current points is a possible value to filter on
			possiblePoints.add(course.getElementsByTagName("points").item(0)
					.getTextContent());
			details[Course.YEAR] = ""
					+ course.getElementsByTagName("year").item(0)
							.getTextContent();
			details[Course.DELIVERY_PERIOD] = ""
					+ course.getElementsByTagName("deliveryperiod").item(0)
							.getTextContent();
			details[Course.LECTURER] = ""
					+ course.getElementsByTagName("lecturer").item(0)
							.getTextContent();
			courseDetails.put(course.getElementsByTagName("acronym").item(0)
					.getTextContent(), details);
			courseCodeList.add(course.getElementsByTagName("acronym").item(0)
					.getTextContent());
		}
		// for each room r from the parsed venues.xml the room
		// the full name of the course is inserted into hash map roomDetails at
		// key
		for (int r = 0; r < rooms.getLength(); r++) {
			Element room = (Element) rooms.item(r);
			roomDetails.put(room.getElementsByTagName("name").item(0)
					.getTextContent(), room.getElementsByTagName("description")
					.item(0).getTextContent());
		}

		// Populating a details array for each course b from parsed
		// building.xml.
		// The details array is inserted into hash map buildingDetails - its key
		// is the acronym of building b.
		for (int b = 0; b < buildings.getLength(); b++) {
			Element building = (Element) buildings.item(b);
			String[] details = new String[2];
			details[Building.DESCRIPTION] = building.getElementsByTagName("description").item(0)
					.getTextContent();
			details[Building.MAP] = building.getElementsByTagName("map").item(0)
					.getTextContent();
			buildingDetails.put(building.getElementsByTagName("name").item(0)
					.getTextContent(), details);

		}
		// Each lecture has a details array, extracted from parsed
		// timetable.xml.
		// The current list of lectures is inserted into hash map lectureDetails
		// at
		// key d, the day of those lectures.
		for (int s = 0; s < semester.getLength(); s++) {
			Element sem = (Element) semester.item(s);
			Element week = (Element) sem.getElementsByTagName("week").item(0);
			NodeList days = week.getElementsByTagName("day");
			for (int d = 0; d < days.getLength(); d++) {
				Element day = (Element) days.item(d);
				NodeList times = day.getElementsByTagName("time");
				ArrayList<String[]> classes = new ArrayList<String[]>();
				if (lectureDetails.indexOfKey(d) >= 0) {
					classes = lectureDetails.get(d);
				}
				for (int t = 0; t < times.getLength(); t++) {
					Element time = (Element) times.item(t);
					NodeList lectures = time.getElementsByTagName("lecture");
					for (int l = 0; l < lectures.getLength(); l++) {
						Element lecture = (Element) lectures.item(l);
						Element years = (Element) lecture.getElementsByTagName(
								"years").item(0);
						Element venue = (Element) lecture.getElementsByTagName(
								"venue").item(0);
						String[] details = new String[9];
						details[Lecture.DAY] = "" + day.getAttribute("name");
						details[Lecture.START_TIME] = "" + time.getAttribute("start");
						details[Lecture.FINISH_TIME] = "" + time.getAttribute("finish");
						details[Lecture.CODE] = ""
								+ lecture.getElementsByTagName("course")
										.item(0).getTextContent();
						if (!courseCodeList.contains(details[3])) {
							courseCodeList.add(details[3]);
						}
						details[Lecture.SEM] = "" + sem.getAttribute("number");
						details[Lecture.ROOM] = ""
								+ venue.getElementsByTagName("room").item(0)
										.getTextContent();
						details[Lecture.BUILDING] = ""
								+ venue.getElementsByTagName("building")
										.item(0).getTextContent();
						String ys = "";
						int length;
						for (int y = 0; y < (length = years
								.getElementsByTagName("year").getLength()); y++) {
							// adding the current year y to the list of possible
							// years to filter on
							possibleYears.add(years
									.getElementsByTagName("year").item(y)
									.getTextContent());
							// cumulating a string containing the possible years
							// the current lecture's course can be
							// taken in
							if (y == length - 1) {
								ys += ""
										+ years.getElementsByTagName("year")
												.item(y).getTextContent();
							} else {
								ys += ""
										+ years.getElementsByTagName("year")
												.item(y).getTextContent()
										+ ", ";
							}
						}
						details[Lecture.YEARS] = ys;
						details[Lecture.COMMENT] = lecture.getElementsByTagName("comment")
								.item(0).getTextContent();
						
						classes.add(details);
					}

				}
				lectureDetails.put(d, classes);
			}
		}
		
		possibleFilters = new ArrayList<Set<String>>();
		possibleFilters.add(possibleYears);
		possibleFilters.add(possiblePoints);
		possibleFilters.add(possibleLevels);
		
	}
}
