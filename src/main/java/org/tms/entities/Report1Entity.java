package org.tms.entities;

import org.tms.utilities.Day;

public class Report1Entity {

	public Report1Entity(String average, String timestamp, String day) {
		this.average = average;
		this.timestamp = timestamp;
		this.day = day;
	}

	public String getAverage() {
		return average;
	}

	public void setAverage(String average) {
		this.average = average;
	}

	public String getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

	public String getDay() {
		return day;
	}

	public void setDay(String day) {
		this.day = day;
	}

	private String average;
	private String timestamp;
	private String day;

	public Report1Entity() {
	}

}
