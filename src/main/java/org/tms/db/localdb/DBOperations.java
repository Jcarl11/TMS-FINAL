
package org.tms.db.localdb;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tms.utilities.Day;

import java.sql.*;
import java.text.DecimalFormat;
import java.util.UUID;

public abstract class DBOperations {
	private String DATABASE_NAME = "jdbc:sqlite:TMS.db";
	private PreparedStatement statement = null;
	private Connection connection = null;
	private ResultSet resultSet = null;
	final Logger log = LoggerFactory.getLogger(DBOperations.class);
	
	protected void initializeDB() {
		try {
			connection = DriverManager.getConnection(DATABASE_NAME);
			log.debug("db init ok");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void createDB() {
		try {
			initializeDB();
			String command = "CREATE TABLE IF NOT EXISTS RAWDATA (ID INTEGER PRIMARY KEY AUTOINCREMENT, COUNT INT, SPEED DECIMAL(4,2), TIMESTAMP DATE, FACILITY VARCHAR(20), FACILITY_TYPE VARCHAR(20));";
			statement = connection.prepareStatement(command);
			int result = statement.executeUpdate();
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			closeConnection();
		}

	}

	public void addColumn() {
		try {
			initializeDB();
			String command = "CREATE TABLE IF NOT EXISTS RAWDATA (ID INTEGER PRIMARY KEY AUTOINCREMENT, COUNT INT, SPEED DECIMAL(4,2), TIMESTAMP DATE, FACILITY VARCHAR(20), FACILITY_TYPE VARCHAR(20));";
			statement = connection.prepareStatement(command);
			int result = statement.executeUpdate();
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			closeConnection();
		}

	}

	public void insert(int count, double avgSpeed, String timeStamp, String facility, String facilityType) {
		try {
			log.info("insert data");

			DecimalFormat df = new DecimalFormat("##.00");
			avgSpeed = Double.valueOf(df.format(avgSpeed));
			log.debug("avgSpeed: " + avgSpeed);
			initializeDB();
			String insertRawDataSQL = "insert into rawdata(ID,COUNT,SPEED,TIMESTAMP,FACILITY,FACILITY_TYPE)values(?,?,?,?,?,?);";
			String insertSyncTableSQL = "insert into sync(RAW_DATA_ID, SYNCED)values(?,?);";
			String rawDataId = UUID.randomUUID().toString();

			PreparedStatement rawDataStmt = connection.prepareStatement(insertRawDataSQL);
			rawDataStmt.setString(1, rawDataId);
			rawDataStmt.setInt(2, count);
			rawDataStmt.setDouble(3, avgSpeed);
			rawDataStmt.setString(4, timeStamp);
			rawDataStmt.setString(5, facility);
			rawDataStmt.setString(6, facilityType);
			rawDataStmt.executeUpdate();

			log.debug("rawdata table updated");

			PreparedStatement syncTableStmt = connection.prepareStatement(insertSyncTableSQL);
			syncTableStmt.setString(1, rawDataId);
			syncTableStmt.setInt(2, 0);

			syncTableStmt.executeUpdate();

			log.debug("sync table updated");

			log.info("finished inserting data");
		} catch (SQLException sQLException) {
			sQLException.printStackTrace();
		} finally {
			closeConnection();
		}

	}

	public void retrieve(Day day) {
	}

	public void update() {

	}

	public void delete() {

	}

	public void closeConnection() {
		try {
			if (connection != null)
				connection.close();
			if (resultSet != null)
				resultSet.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public PreparedStatement getStatement() {
		return statement;
	}

	public Connection getConnection() {
		return connection;
	}

	public ResultSet getResultSet() {
		return resultSet;
	}
}
