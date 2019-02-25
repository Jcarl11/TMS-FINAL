package org.tms.model.jfxbeans;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;

public class LevelOfServiceEntityFX {
    private final SimpleStringProperty hour;
    private final SimpleStringProperty volume;
    private final SimpleStringProperty avgSpeed;
    private final SimpleStringProperty facility;
    private final SimpleStringProperty facilityType;
    private final SimpleStringProperty lvlOfService;

    public LevelOfServiceEntityFX(String hour, String volume, String avgSpeed, String facility, String facilityType, String lvlOfService) {
        this.hour = new SimpleStringProperty(hour);
        this.volume = new SimpleStringProperty(volume);
        this.avgSpeed = new SimpleStringProperty(avgSpeed);
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

    public String getVolume() {
        return volume.get();
    }

    public void setVolume(String volume) {
    	this.volume.set(volume);
    }
    
    public String getAvgSpeed() {
        return avgSpeed.get();
    }

    public void setAvgSpeed(String avgSpeed) {
    	this.avgSpeed.set(avgSpeed);
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
    
    public SimpleStringProperty hourProperty() {
		return hour;
	}
    
    public SimpleStringProperty volumeProperty() {
		return volume;
	}
    
    public SimpleStringProperty avgSpeedProperty() {
		return avgSpeed;
	}
    
    public SimpleStringProperty facilityProperty() {
		return facility;
	}
    
    public SimpleStringProperty facilityTypeProperty() {
		return facilityType;
	}
    
    public SimpleStringProperty lvlOfServiceProperty() {
		return lvlOfService;
	}
}
