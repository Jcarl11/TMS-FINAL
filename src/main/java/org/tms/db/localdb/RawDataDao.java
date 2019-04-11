package org.tms.db.localdb;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.ConnectException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.UUID;

public class RawDataDao extends BaseDao {
    private final Logger log = LoggerFactory.getLogger(BaseDao.class);
    public RawDataDao() throws ConnectException {
        super();
    }

    public RawDataDao(Connection conn) {
        super(conn);
    }
    public void insert(int count, double avgSpeed, String timeStamp, String facility, String facilityType) {
        try {
            log.info("insert data");

            DecimalFormat df = new DecimalFormat("##.00");
            avgSpeed = Double.valueOf(df.format(avgSpeed));
            log.debug("avgSpeed: " + avgSpeed);
            String insertRawDataSQL = "insert into rawdata(ID,COUNT,SPEED,TIMESTAMP,FACILITY,FACILITY_TYPE)values(?,?,?,?,?,?);";
            prepareStatement(insertRawDataSQL);

            String rawDataId = UUID.randomUUID().toString();

            setString(1, rawDataId);
            setInt(2, count);
            setDouble(3, avgSpeed);
            setString(4, timeStamp);
            setString(5, facility);
            setString(6, facilityType);

            executeUpdate();

            int st = executeUpdate();

            log.debug(" executeUpdate st : " + st);

            String insertSyncTableSQL = "insert into sync(RAW_DATA_ID, SYNCED)values(?,?);";
            prepareStatement(insertSyncTableSQL);

            setString(1, rawDataId);
            setInt(2, 0);

            int st2 = executeUpdate();

            log.debug(" executeUpdate st : " + st2);


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
