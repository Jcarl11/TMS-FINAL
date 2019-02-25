package org.tms.db.localdb;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tms.entities.AvgVolumeEntity;
import org.tms.model.Period;

public class TrafficVolumeDAO extends DBOperations {
	final Logger log = LoggerFactory.getLogger(TrafficVolumeDAO.class);
	
	private PreparedStatement statement = null;
	private Connection connection = null;
	private ResultSet resultSet = null;
	private ArrayList<AvgVolumeEntity> responseList = new ArrayList<AvgVolumeEntity>();
	
	public ArrayList<AvgVolumeEntity> getAvgVolume(String period) {
		try {
			
			log.info("retrieving db data");
			initializeDB();
			statement = getStatement();
			connection = getConnection();
			resultSet = getResultSet();
			String command = "";
			String last7DaysFilter = " where timestamp between (SELECT DATETIME('now', '-7 day')) and (SELECT date('now', '1 day')) ";
			String last30DaysFilter = " where timestamp between (SELECT DATETIME('now', '-30 day')) and (SELECT date('now', '1 day')) ";
			log.debug(Period.fromValue(period).toString());
			switch (Period.fromValue(period)) {
			case LAST_7_DAYS:
				command = "select round(avg(COUNT), 2) AVG_VOLUME, strftime('%m-%d', timestamp) DATE from rawdata" + last7DaysFilter + "group by strftime('%m-%d', timestamp) order by timestamp asc;";
				break;
			case LAST_30_DAYS:
				command = "select round(avg(COUNT), 2) AVG_VOLUME, strftime('%m-%d', timestamp) DATE from rawdata" + last30DaysFilter + "group by strftime('%m-%d', timestamp) order by timestamp asc;";
				break;
			case ALL:
				command = "select round(avg(COUNT), 2) AVG_VOLUME, strftime('%m-%d', timestamp) DATE from rawdata group by strftime('%m-%d', timestamp) order by timestamp asc;";
			default:
				break;
			}
			
			log.debug("command: " + command);
			statement = connection.prepareStatement(command);
			resultSet = statement.executeQuery();
			
			while (resultSet.next()) {
				Double average = resultSet.getDouble("AVG_VOLUME");
				String date = resultSet.getString("DATE");
				
				responseList.add(new AvgVolumeEntity(average, date));
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
