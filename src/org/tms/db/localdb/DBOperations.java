
package org.tms.db.localdb;

import org.tms.utilities.Day;
import java.sql.*;
import java.util.HashMap;

public abstract class DBOperations {
	private String DATABASE_NAME = "jdbc:sqlite:LocalStorage.db";
	private PreparedStatement statement = null;
	private Connection connection = null;
	private ResultSet resultSet = null;

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
			String command = "CREATE TABLE IF NOT EXISTS RAWDATA" + "(COUNT VARCHAR(20)," + "AVG_SPEED DOUBLE(20,2)," + "TIMESTAMP DATETIME,"
					+ "DAY VARCHAR(50)," + "FACILITY VARCHAR(50)," + "FACILTYTYPE VARCHAR(50));";
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
			String command = "CREATE TABLE IF NOT EXISTS RAWDATA" + "(COUNT VARCHAR(20)," + "AVG_SPEED DOUBLE(20,2)," + "TIMESTAMP DATETIME,"
					+ "DAY VARCHAR(50)," + "FACILITY VARCHAR(50)," + "FACILITYTYPE VARCHAR(50));";
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
			initializeDB();
			String command = "insert into RAWDATA(COUNT,AVG_SPEED,TIMESTAMP,DAY,FACILITY,FACILTYTYPE)values(?,?,?,?,?,?);";
			statement = connection.prepareStatement(command);
			statement.setString(1, count);
			statement.setDouble(2, Double.parseDouble(avgSpeed));
			statement.setString(3, timeStamp);
			statement.setString(4, day);
			statement.setString(5, facility);
			statement.setString(6, facilityType);
			statement.executeUpdate();
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
