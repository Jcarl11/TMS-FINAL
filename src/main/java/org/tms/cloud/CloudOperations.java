package org.tms.cloud;

import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClientConfig;
import org.asynchttpclient.Dsl;
import org.asynchttpclient.Response;
import org.json.JSONObject;
import org.slf4j.LoggerFactory;
import org.tms.AppConfiguration;
import org.tms.db.localdb.CloudOperationsDAO;
import org.tms.entities.RawDataEntity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class CloudOperations {
	final org.slf4j.Logger log = LoggerFactory.getLogger(CloudOperations.class);
	AsyncHttpClient asyncHttpClient = Dsl.asyncHttpClient();
    String syncStatus = "";

	public String syncPush(RawDataEntity record) {

		DefaultAsyncHttpClientConfig.Builder clientBuilder = Dsl.config()
				.setConnectTimeout(500);

		AppConfiguration appConfiguration = null;
		try {
			appConfiguration = new AppConfiguration();
		} catch (IOException e) {
			e.printStackTrace();
		}

		JSONObject data = new JSONObject();
		data.put("id", record.id);
		data.put("count", record.count);
		data.put("speed", record.speed);
		data.put("timestamp", record.ts);
		data.put("facility", record.facility);
		data.put("facility_type", record.facilityType);


		log.debug(appConfiguration.APP_VERSION);
//		JSONArray array = new JSONArray();

//		dataParams.put("requests", array);
		log.debug("back4app classes api: " + appConfiguration.URL_CLASSES);
		log.debug("json data to send: "+ data.toString());
		log.info("pushing data to cloud...");



		AsyncHttpClient client = Dsl.asyncHttpClient(clientBuilder);

		Future<Response> whenResponse = asyncHttpClient.preparePost(appConfiguration.URL_CLASSES + "RAWDATA")
				.addHeader("X-Parse-Application-Id", appConfiguration.APP_ID)
				.addHeader("X-Parse-REST-API-Key", appConfiguration.REST_API_KEY)
				.addHeader("Content-Type", "application/json")
				.setBody(data.toString())
				.execute();

		Response response = null;
		try {
			response = whenResponse.get();
		} catch (ExecutionException | InterruptedException e) {
			e.printStackTrace();
			syncStatus = "SYNC_NETWORK_FAILURE";
			log.error("Sync FAILED!");
			return syncStatus;
		}

		log.debug("Response status: "+ response.getStatusText());
		log.info("finished pushing data");
		if (response.getStatusText() == "Created") {
            syncStatus = "SYNC_OK";
        } else {
		    syncStatus = "SYNC_FAILURE";
        }
		return syncStatus;
	}

	public String syncCloud() {
		ArrayList<RawDataEntity> rawDataEntityArrayList = new ArrayList<>();
        ArrayList<String> idList = new ArrayList<>();
		String syncDBStatus = "FAILED";
		int dbUpdatedCount = 0;


        log.info("retrieve db data to sync...");


        log.info("retrieve db data finished");
        idList = dbGetIdsToSync();
        for (String id: idList) {
            rawDataEntityArrayList.add(dbGetRawData(id));
        }
        if (idList.isEmpty()) {
            log.info("Everything is synced");
            syncStatus = "DB_SYNC_OK";
        }
        log.debug("number of data for push sync: " + rawDataEntityArrayList.size());
        for (RawDataEntity record: rawDataEntityArrayList) {
            log.debug("processing: " + record.id);

            String resp = syncPush(record);
            log.debug("CLOUD SYNC STATUS: " + resp);

            if (resp.equals("SYNC_OK")) {
                try {
                    CloudOperationsDAO cloudOperationsDAO = new CloudOperationsDAO();
                    cloudOperationsDAO.pullSyncUpdateDB(record.id);
                } catch ( Exception e) {
                    e.printStackTrace();
                }
                log.info("DB Sync OK");


                dbUpdatedCount++;
            } else {
                log.error("Cloud sync FAILURE!!!");
            }
            log.info("DB Sync finished");
        }

        if (rawDataEntityArrayList.size() == dbUpdatedCount){
            log.info("All record is up to date");
            syncDBStatus = "DB_SYNC_COMPLETE";
        }
        else {
            log.warn(dbUpdatedCount + " records updated out of " + rawDataEntityArrayList.size());
            syncDBStatus = "DB_SYNC_INCOMPLETE";
        }


//		log.debug("status: " + response.getStatusText());
		return syncDBStatus;
	}

	public ArrayList<String> dbGetIdsToSync() {

        ArrayList<String> idList = null;
        try {
            CloudOperationsDAO cloudOperationsDAO = new CloudOperationsDAO();
            cloudOperationsDAO = new CloudOperationsDAO();
            idList = cloudOperationsDAO.getIDsToSync();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return idList;
    }

    public RawDataEntity dbGetRawData(String id) {
        RawDataEntity rawDataEntity = null;
        try {
            CloudOperationsDAO cloudOperationsDAO = new CloudOperationsDAO();
            cloudOperationsDAO = new CloudOperationsDAO();
            rawDataEntity = cloudOperationsDAO.getRawData(id);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rawDataEntity;
    }
}
