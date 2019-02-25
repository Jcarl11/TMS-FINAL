package org.tms.entities;

public class LevelOfServiceEntity {
	public String hour;
	public String facility;
	public String facilityType;
	public double avgVolume;
	public String lvlOfService;
	
	public LevelOfServiceEntity(String hour, double avgVolume, String facility, String facilityType, String lvlOfService) {
		this.hour = hour;
		this.avgVolume = avgVolume;
		this.facility = facility;
		this.facilityType = facilityType;
		this.lvlOfService = lvlOfService;
	}
}
