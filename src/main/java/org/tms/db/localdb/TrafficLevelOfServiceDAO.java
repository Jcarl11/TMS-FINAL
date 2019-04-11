package org.tms.db.localdb;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tms.entities.LevelOfServiceEntity;
import org.tms.utilities.Utils;

import java.net.ConnectException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class TrafficLevelOfServiceDAO extends BaseDao {
final Logger log = LoggerFactory.getLogger(TrafficSpeedDAO.class);

	private ArrayList<LevelOfServiceEntity> responseList = new ArrayList<LevelOfServiceEntity>();

	public TrafficLevelOfServiceDAO() throws ConnectException {
		super();
	}

	public TrafficLevelOfServiceDAO(Connection conn) {
		super(conn);
	}

	public ArrayList<LevelOfServiceEntity> getVolumePerHour(String period) {
		try {
			
			log.info("retrieving db data");
			String command = "select strftime('%H', timestamp) HOUR, SUM(COUNT) VOLUME, round(AVG(SPEED), 2) AVG_SPEED, FACILITY, FACILITY_TYPE from rawdata "
					+ "where strftime('%Y-%m-%d', timestamp) = '" + period + "' group by HOUR order by timestamp asc;";
			log.debug("command: " + command);
			prepareStatement(command);
			ResultSet rs = executeQuery();
			
			while (rs.next()) {
				String hour = rs.getString("HOUR");
				int volume = rs.getInt("VOLUME");
				double avgSpeed = rs.getDouble("AVG_SPEED");
				String facility = rs.getString("FACILITY");
				String facilityType = rs.getString("FACILITY_TYPE");
				String lvlOfService = Utils.getLevelOfService(avgSpeed);
				responseList.add(new LevelOfServiceEntity(hour, volume, avgSpeed, facility, facilityType, lvlOfService));
			}
			
			log.info("finished retrieving data");

		} catch (SQLException e) {
			log.error("SQLException : " + e.getMessage());
		} finally {
			try {
				log.debug("Closing resources...");
				closeResources();
				closeConnection();
			} catch (Exception e) {
			}
		}
		
		return responseList;

	}
}
