
package org.tms.db.localdb;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tms.utilities.Day;

import LocalDatabase.DBOperations;

import java.sql.*;
import java.util.HashMap;

public abstract class DBOperations {
	private String DATABASE_NAME = "jdbc:sqlite:TMS.db";
	private PreparedStatement statement = null;
	private Connection connection = null;
	private ResultSet resultSet = null;
	final Logger log = LoggerFactory.getLogger(DBOperations.class);
	
	protected void initializeDB() {
		try {
			connection = DriverManager.getConnection(DATABASE_NAME);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void createDB() {
		try {
			initializeDB();
			String command = "CREATE TABLE IF NOT EXISTS RAWDATA (ID INTEGER PRIMARY KEY AUTOINCREMENT, COUNT INT, AVG_SPEED DECIMAL(4,2), TIMESTAMP DATE, FACILITY VARCHAR(20), FACILITY_TYPE VARCHAR(20));";
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
			String command = "CREATE TABLE IF NOT EXISTS RAWDATA (ID INTEGER PRIMARY KEY AUTOINCREMENT, COUNT INT, AVG_SPEED DECIMAL(4,2), TIMESTAMP DATE, FACILITY VARCHAR(20), FACILITY_TYPE VARCHAR(20));";
			statement = connection.prepareStatement(command);
			int result = statement.executeUpdate();
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			closeConnection();
		}

	}

	public void insert(String count, String avgSpeed, String timeStamp, String day, String facility, String facilityType) {
		try {
			log.info("insert data");
			initializeDB();
			String command = "insert into rawdata(COUNT,AVG_SPEED,TIMESTAMP,FACILITY,FACILITY_TYPE)values(?,?,?,?,?);";
			statement = connection.prepareStatement(command);
			statement.setString(1, count);
			statement.setDouble(2, Double.parseDouble(avgSpeed));
			statement.setString(3, timeStamp);
			statement.setString(4, day);
			statement.setString(5, facility);
			statement.setString(6, facilityType);
			statement.executeUpdate();
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
			if (statement != null)
				statement.close();
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
