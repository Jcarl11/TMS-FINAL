package org.tms.entities;

public class RawDataEntity {
    public String id;
    public int count;
    public double speed;
    public String ts;
    public String facility;
    public String facilityType;

    public RawDataEntity(String id, int count, double speed, String ts, String facility, String facilityType) {
        this.id = id;
        this.count = count;
        this.speed = speed;
        this.ts = ts;
        this.facility = facility;
        this.facilityType = facilityType;
    }
}
