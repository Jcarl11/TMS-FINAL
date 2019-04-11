package org.tms.db.localdb;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tms.entities.RawDataEntity;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class CloudOperationsDAO extends BaseDao {
    final Logger log = LoggerFactory.getLogger(CloudOperationsDAO.class);

    public CloudOperationsDAO() throws Exception {
        super();
    }

    public CloudOperationsDAO(Connection conn) {
        super(conn);
    }


    public ArrayList<String> getIDsToSync() {
        ArrayList<RawDataEntity> responseList = new ArrayList<RawDataEntity>();
        ArrayList<String> idList = new ArrayList<>();
        try {

            log.info("retrieving db data");
            String command = "select RAW_DATA_ID from sync where synced = 0;";

            log.debug("command: " + command);

            prepareStatement(command);
            ResultSet rs = executeQuery();

            while (rs.next()) {
                String id = rs.getString("RAW_DATA_ID");
                idList.add(id);
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

        return idList;
    }
    public RawDataEntity getRawData(String idToSync) throws SQLException{

        RawDataEntity rawDataEntity = null;
        log.info("retrieving db data");
        try {
            String command = "select * from RAWDATA where id = '" + idToSync + "' ;";

            log.debug("command: " + command);
            prepareStatement(command);


            ResultSet rs = executeQuery();
            if (rs.next()) {
                String id = rs.getString("ID");
                int count = rs.getInt("COUNT");
                Double speed = rs.getDouble("SPEED");
                String ts = rs.getString("TIMESTAMP");
                String facility = rs.getString("FACILITY");
                String facilityType = rs.getString("FACILITY_TYPE");
                rawDataEntity = new RawDataEntity(id, count, speed, ts, facility, facilityType);

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

        return rawDataEntity;

    }

    public void pullSyncUpdateDB(String id) throws SQLException {

        try {
            log.info("update sync table");
            String updateSyncTableSQL = "update sync set synced = 1 where raw_data_id = ?;";

            prepareStatement(updateSyncTableSQL);
            setString(1, id);

            executeUpdate();

            log.debug("sync table updated");

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

    }

}
