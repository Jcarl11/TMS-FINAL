package org.tms.db.localdb;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tms.entities.AvgSpeedEntity;
import org.tms.model.Period;

import java.net.ConnectException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class TrafficSpeedDAO extends BaseDao {
	public TrafficSpeedDAO() throws ConnectException {
		super();
	}

	public TrafficSpeedDAO(Connection conn) {
		super(conn);
	}

	final Logger log = LoggerFactory.getLogger(TrafficSpeedDAO.class);
	
	private ArrayList<AvgSpeedEntity> responseList = new ArrayList<AvgSpeedEntity>();
	
	public ArrayList<AvgSpeedEntity> getAvgSpeed(String period) {
		try {
			
			log.info("retrieving db data");
			String command = "";
			String last7DaysFilter = " where timestamp between (SELECT DATETIME('now', '-7 day')) and (SELECT date('now', '1 day')) ";
			String last30DaysFilter = " where timestamp between (SELECT DATETIME('now', '-30 day')) and (SELECT date('now', '1 day')) ";
			log.debug(Period.fromValue(period).toString());
			switch (Period.fromValue(period)) {
			case LAST_7_DAYS:
				command = "select round(avg(SPEED), 2) AVG_SPEED, strftime('%m-%d', timestamp) DATE from rawdata" + last7DaysFilter + "group by strftime('%m-%d', timestamp) order by timestamp asc;";
				break;
			case LAST_30_DAYS:
				command = "select round(avg(SPEED), 2) AVG_SPEED, strftime('%m-%d', timestamp) DATE from rawdata" + last30DaysFilter + "group by strftime('%m-%d', timestamp) order by timestamp asc;";
				break;
			case ALL:
				command = "select round(avg(SPEED), 2) AVG_SPEED, strftime('%m-%d', timestamp) DATE from rawdata group by strftime('%m-%d', timestamp) order by timestamp asc;";
			default:
				break;
			}
			
			log.debug("command: " + command);
			prepareStatement(command);
			ResultSet rs = executeQuery();
			
			while (rs.next()) {
				Double average = rs.getDouble("AVG_SPEED");
				String date = rs.getString("DATE");
				
				responseList.add(new AvgSpeedEntity(average, date));
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
