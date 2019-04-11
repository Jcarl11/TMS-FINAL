package org.tms.db.localdb;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.ConnectException;
import java.sql.*;

public class BaseDao {
    private String DATABASE_NAME = "jdbc:sqlite:TMS.db";

    private Connection connection = null;
    private PreparedStatement preparedStatement = null;
//    private ResultSet resultSet = null;


    private final Logger log = LoggerFactory.getLogger(BaseDao.class);

    protected BaseDao() throws ConnectException {
        try {
            connection = DriverManager.getConnection(DATABASE_NAME);
            log.debug("DB connected");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    protected BaseDao(Connection connection) {
        this.connection = connection;
    }

    protected void prepareStatement(String sql) throws SQLException {
        preparedStatement = connection.prepareStatement(sql);
    }

    protected void setString(int index, String value) throws SQLException {
        preparedStatement.setString(index, value);
    }

    protected void setInt(int index, int value) throws SQLException {
        preparedStatement.setInt(index, value);
    }

    protected void setDouble(int index, double value) throws SQLException {
        preparedStatement.setDouble(index, value);
    }

    protected int executeUpdate() throws SQLException {
        return preparedStatement.executeUpdate();
    }

    protected ResultSet executeQuery() throws SQLException {
        return preparedStatement.executeQuery();
    }

    protected void closeResources() throws SQLException {
        if (preparedStatement != null) {
            preparedStatement.close();
            preparedStatement = null;
        }

    }

    public void closeConnection() throws SQLException {
        connection.close();
        connection = null;
    }
}

