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
	public static String getLevelOfService(double value) {
		String los = "";
		if (value >= 40)
			los = "EXCELLENT";
		else if (value >= 30 && value < 40)
			los = "GOOD";
		else if (value >= 20 && value < 30)
			los = "FAIR";
		else if (value >= 15 && value < 20)
			los = "PASSED";
		else if (value < 15)
			los = "FAILED";
		else
			los = "INVALID";
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
