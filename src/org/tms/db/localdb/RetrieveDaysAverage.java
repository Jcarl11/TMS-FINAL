package org.tms.db.localdb;

import org.tms.entities.Report1Entity;
import org.tms.utilities.Day;
import org.tms.utilities.GlobalObjects;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import org.asynchttpclient.AsyncCompletionHandler;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.Dsl;
import org.asynchttpclient.ListenableFuture;
import org.json.JSONArray;
import org.json.JSONObject;
import org.asynchttpclient.Response;

public class RetrieveDaysAverage extends DBOperations {
	AsyncHttpClient asyncHttpClient = Dsl.asyncHttpClient();
	private HashMap<Day, String> result = new HashMap<>();
	private ArrayList<Report1Entity> reportList = new ArrayList<>();
	private PreparedStatement statement = null;
	private Connection connection = null;
	private ResultSet resultSet = null;

	@Override
	public void retrieve(Day day) {
		try {
			initializeDB();
			statement = getStatement();
			connection = getConnection();
			resultSet = getResultSet();
			String command = "SELECT avg(COUNT) 'AVERAGE',TIMESTAMP,DAY FROM RAWDATA WHERE DAY = '" + day.toString()
					+ "';";
			statement = connection.prepareStatement(command);
			resultSet = statement.executeQuery();
			if (resultSet.next()) {
				String average = resultSet.getString("AVERAGE");
				if (average != null)
					result.put(day, average);
				else
					result.put(day, "0.0");
			}
		} catch (SQLException sQLException) {
			sQLException.printStackTrace();
		} finally {
			closeConnection();
		}
	}

	public void retrieveReport(Day day) {
		try {
			initializeDB();
			statement = getStatement();
			connection = getConnection();
			resultSet = getResultSet();
			String command = "SELECT avg(COUNT) 'AVERAGE',TIMESTAMP,DAY FROM RAWDATA WHERE DAY = '" + day.toString()
					+ "';";
			statement = connection.prepareStatement(command);
			resultSet = statement.executeQuery();
			if (resultSet.next()) {
				Report1Entity entity = new Report1Entity();
				String average = resultSet.getString("AVERAGE");
				String timestamp = resultSet.getString("TIMESTAMP");
				String DAY = resultSet.getString("DAY");
				if (average != null) {
					entity.setAverage(average);
					entity.setDay(DAY);
					entity.setTimestamp(GlobalObjects.getInstance().convertDateToIso(timestamp));
					reportList.add(entity);
				} else
					reportList.add(new Report1Entity("0.0", "1970-01-01T00:00:00Z", day.toString()));
			}
		} catch (SQLException sQLException) {
			sQLException.printStackTrace();
		} finally {
			closeConnection();
		}

	}

	public Response insert(ArrayList<Report1Entity> data) {
		JSONObject dataParams = new JSONObject();
		JSONArray array = new JSONArray();
		for (Report1Entity records : data) {
			array.put(GlobalObjects.getInstance().buildParameter2(records));
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

	public ArrayList<Report1Entity> getReportList() {
		return reportList;
	}

	public HashMap<Day, String> getResult() {
		return result;
	}
}
