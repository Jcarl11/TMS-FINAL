package org.tms.db.localdb;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tms.DashboardController;
import org.tms.entities.Report1Entity;
import org.tms.utilities.Day;
import org.tms.utilities.GlobalObjects;

public class TrafficSpeedDAO extends DBOperations {
	final Logger log = LoggerFactory.getLogger(TrafficSpeedDAO.class);
	
	private PreparedStatement statement = null;
	private Connection connection = null;
	private ResultSet resultSet = null;
	
	HashMap<Double, String> avgSpeedHashMap = new HashMap<Double, String>();

	public HashMap<Double, String> getAvgSpeed() {
		try {
			
			log.info("retrieving db data");
			initializeDB();
			statement = getStatement();
			connection = getConnection();
			resultSet = getResultSet();
			String command = "select round(avg(count), 2) AVG_SPEED, strftime('%m-%d', timestamp) DATE from rawdata group by strftime('%m-%d', timestamp);";
			statement = connection.prepareStatement(command);
			resultSet = statement.executeQuery();
			
			if (resultSet.next()) {
				Double average = resultSet.getDouble("AVG_SPEED");
				String date = resultSet.getString("DATE");
				avgSpeedHashMap.put(average, date);
			}
			
			log.info("finished retrieving data");
			
		} catch (SQLException sqlException) {
			sqlException.printStackTrace();
		} finally {
			closeConnection();
		}
		
		return avgSpeedHashMap;

	}

}
