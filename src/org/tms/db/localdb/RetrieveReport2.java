
package org.tms.db.localdb;

import org.tms.entities.RecordEntity;
import org.tms.utilities.Day;
import org.tms.utilities.GlobalObjects;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import org.asynchttpclient.AsyncCompletionHandler;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.Dsl;
import org.asynchttpclient.ListenableFuture;
import org.asynchttpclient.Response;
import org.json.JSONArray;
import org.json.JSONObject;

public class RetrieveReport2 extends DBOperations {
	private ArrayList<RecordEntity> recordEntityList = new ArrayList<>();
	AsyncHttpClient asyncHttpClient = Dsl.asyncHttpClient();
	private PreparedStatement statement = null;
	private Connection connection = null;
	private ResultSet resultSet = null;

	public void retrieveValue(int hour) {
		try {
			initializeDB();
			RecordEntity recordEntity = new RecordEntity();
			statement = getStatement();
			connection = getConnection();
			resultSet = getResultSet();
			String command = "SELECT AVG(COUNT) AS 'AVERAGE', FACILITY, FACILITY_TYPE FROM RAWDATA WHERE strftime('%H',TIMESTAMP) = '"
					+ hour + "' ";
			statement = connection.prepareStatement(command);
			resultSet = statement.executeQuery();
			while (resultSet.next()) {
				String average = resultSet.getString("AVERAGE");
				if (average != null) {
					recordEntity.setTime(String.valueOf(hour));
					recordEntity.setFacility(resultSet.getString("FACILITY"));
					recordEntity.setFacilityType(resultSet.getString("FACILITY_TYPE"));
					recordEntity.setLevelOfService(GlobalObjects.getInstance().categorizeLOS(average));
					recordEntityList.add(recordEntity);
				}

			}
		} catch (SQLException sQLException) {
			sQLException.printStackTrace();
		} finally {
			closeConnection();
		}
	}

	public void retrieveValue_COMPLETETIME(int hour) {
		try {
			initializeDB();
			statement = getStatement();
			connection = getConnection();
			resultSet = getResultSet();
			String command = "SELECT AVG(COUNT) AS 'AVERAGE', TIMESTAMP, FACILITY, FACILITY_TYPE FROM RAWDATA WHERE strftime('%H',TIMESTAMP) = '"
					+ hour + "' ";
			statement = connection.prepareStatement(command);
			resultSet = statement.executeQuery();
			while (resultSet.next()) {
				RecordEntity recordEntity = new RecordEntity();
				String average = resultSet.getString("AVERAGE");
				String timestamp = resultSet.getString("TIMESTAMP");
				if (average != null) {
					// recordEntity.setTime(String.valueOf(hour));
					recordEntity.setTime(GlobalObjects.getInstance().convertDateToIso(timestamp));
					recordEntity.setFacility(resultSet.getString("FACILITY"));
					recordEntity.setFacilityType(resultSet.getString("FACILITY_TYPE"));
					recordEntity.setLevelOfService(GlobalObjects.getInstance().categorizeLOS(average));
					recordEntityList.add(recordEntity);
				}

			}
		} catch (SQLException sQLException) {
			sQLException.printStackTrace();
		} finally {
			closeConnection();
		}
	}

	public Response insert(ArrayList<RecordEntity> data) {
		JSONObject dataParams = new JSONObject();
		JSONArray array = new JSONArray();
		for (RecordEntity records : data) {
			if (records.isNull() == false) {
				String date = records.getTime();
				String facility = records.getFacility();
				String facilityType = records.getFacilityType();
				String LOS = records.getLevelOfService();
				array.put(GlobalObjects.getInstance().buildParameter(date, facility, facilityType, LOS));
			}
		}
		dataParams.put("requests", array);
		ListenableFuture<Response> lf = asyncHttpClient.preparePost(GlobalObjects.URL_BASE + "batch")
				.addHeader("X-Parse-Application-Id", GlobalObjects.APP_ID)
				.addHeader("X-Parse-REST-API-Key", GlobalObjects.REST_API_KEY)
				.addHeader("Content-Type", "application/json").setBody(dataParams.toString())
				.execute(new AsyncCompletionHandler<Response>() {
					@Override
					public Response onCompleted(Response rspns) throws Exception {
						return rspns;
					}
				});
		Response response = null;
		try {
			response = lf.get();
		} catch (InterruptedException interruptedException) {
			interruptedException.printStackTrace();
		} catch (ExecutionException executionException) {
			executionException.printStackTrace();
		}
		return response;
	}

	public ArrayList<RecordEntity> getResult() {
		return recordEntityList;
	}
}
