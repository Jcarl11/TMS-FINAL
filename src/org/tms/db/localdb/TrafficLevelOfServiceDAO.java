package org.tms.db.localdb;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tms.entities.LevelOfServiceEntity;
import org.tms.model.Period;
import org.tms.utilities.Utils;

public class TrafficLevelOfServiceDAO extends DBOperations {
final Logger log = LoggerFactory.getLogger(TrafficSpeedDAO.class);
	
	private PreparedStatement statement = null;
	private Connection connection = null;
	private ResultSet resultSet = null;
	private ArrayList<LevelOfServiceEntity> responseList = new ArrayList<LevelOfServiceEntity>();
	
	public ArrayList<LevelOfServiceEntity> getVolumePerHour(String period) {
		try {
			
			log.info("retrieving db data");
			initializeDB();
			statement = getStatement();
			connection = getConnection();
			resultSet = getResultSet();
			String command = "select strftime('%H', timestamp) HOUR, SUM(COUNT) VOLUME, round(AVG(SPEED), 2) AVG_SPEED, FACILITY, FACILITY_TYPE from rawdata "
					+ "where strftime('%Y-%m-%d', timestamp) = '" + period + "' group by HOUR order by timestamp asc;";
			log.debug("command: " + command);
			statement = connection.prepareStatement(command);
			resultSet = statement.executeQuery();
			
			while (resultSet.next()) {
				String hour = resultSet.getString("HOUR");
				int volume = resultSet.getInt("VOLUME");
				double avgSpeed = resultSet.getDouble("AVG_SPEED");
				String facility = resultSet.getString("FACILITY");
				String facilityType = resultSet.getString("FACILITY_TYPE");
				String lvlOfService = Utils.getLevelOfService(avgSpeed);
				responseList.add(new LevelOfServiceEntity(hour, volume, avgSpeed, facility, facilityType, lvlOfService));
			}
			
			log.info("finished retrieving data");
			
		} catch (SQLException sqlException) {
			sqlException.printStackTrace();
		} finally {
			closeConnection();
		}
		
		return responseList;

	}
}
