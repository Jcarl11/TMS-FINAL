package org.tms.entities;

public class LevelOfServiceEntity {
	public String hour;
	public String facility;
	public String facilityType;
	public int volume;
	public double avgSpeed;
	public String lvlOfService;
	
	public LevelOfServiceEntity(String hour, int volume, double avgSpeed, String facility, String facilityType, String lvlOfService) {
		this.hour = hour;
		this.volume = volume;
		this.avgSpeed = avgSpeed;
		this.facility = facility;
		this.facilityType = facilityType;
		this.lvlOfService = lvlOfService;
	}
	public LevelOfServiceEntity(int volume, double avgSpeed, String facility, String facilityType) {
		this.volume = volume;
		this.avgSpeed = avgSpeed;
		this.facility = facility;
		this.facilityType = facilityType;
	}
}
