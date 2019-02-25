package org.tms.model.jfxbeans;

import javafx.beans.property.SimpleStringProperty;

public class LevelOfServiceEntityFX {
    private final SimpleStringProperty hour;
    private final SimpleStringProperty avgVolume;
    private final SimpleStringProperty facility;
    private final SimpleStringProperty facilityType;
    private final SimpleStringProperty lvlOfService;

    public LevelOfServiceEntityFX(String hour, String avgVolume, String facility, String facilityType, String lvlOfService) {
        this.hour = new SimpleStringProperty(hour);
        this.avgVolume = new SimpleStringProperty(avgVolume);
        this.facility = new SimpleStringProperty(facility);
        this.facilityType = new SimpleStringProperty(facilityType);
        this.lvlOfService = new SimpleStringProperty(lvlOfService);
    }

    public String getHour() {
        return hour.get();
    }

    public void setHour(String hour) {
    	this.hour.set(hour);
    }

    public String getAvgVolume() {
        return avgVolume.get();
    }

    public void setAvgVolume(String avgVolume) {
    	this.avgVolume.set(avgVolume);
    }

    public String getFacility() {
        return facility.get();
    }

    public void setFacility(String facility) {
        this.facility.set(facility);
    }
    
    public String getFacilityType() {
        return facilityType.get();
    }

    public void setFacilityType(String facilityType) {
        this.facilityType.set(facilityType);
    }
    
    public String getLvlOfService() {
        return lvlOfService.get();
    }

    public void setLvlOfService(String lvlOfService) {
        this.lvlOfService.set(lvlOfService);
    }
}
