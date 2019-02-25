package org.tms.utilities;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tms.DashboardController;

public class Utils {
	final static Logger log = LoggerFactory.getLogger(Utils.class);
	public static String getLevelOfService(Double value) {
		String los = "";
		if (value >= 0 && value <= 100)
			los = "A";
		else if (value > 100 && value <= 200)
			los = "B";
		else if (value > 200 && value <= 300)
			los = "C";
		else if (value > 300 && value <= 400)
			los = "D";
		else if (value > 400 && value <= 500)
			los = "E";
		else if (value > 500)
			los = "F";
		else
			los = "Unmeasurable";
		return los;
	}
	
	public static JSONObject dateToJSON(String dateString) {
		// String converted = convertDateToIso(dateString);
		JSONObject json = new JSONObject();
		json.put("__type", "Date");
		json.put("iso", dateString);
		return json;
	}

	public static String dateToISOFormat(String dateString) {
		DateFormat simpleDateFormat1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		DateFormat simpleDateFormat2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:dd'Z'");
		Date date = new Date();
		try {
			date = simpleDateFormat1.parse(dateString);
		} catch (ParseException ex) {
			log.error("Error in date parsing");
		}
		return simpleDateFormat2.format(date);
	}
}
