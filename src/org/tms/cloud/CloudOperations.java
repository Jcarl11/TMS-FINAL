package org.tms.cloud;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

import org.asynchttpclient.AsyncCompletionHandler;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.Dsl;
import org.asynchttpclient.ListenableFuture;
import org.asynchttpclient.Response;
import org.json.JSONArray;
import org.json.JSONObject;
import org.tms.entities.Report1Entity;
import org.tms.utilities.Day;
import org.tms.utilities.GlobalObjects;

public class CloudOperations {
	AsyncHttpClient asyncHttpClient = Dsl.asyncHttpClient();
	private HashMap<Day, String> result = new HashMap<>();
	private ArrayList<Report1Entity> reportList = new ArrayList<>();
	private PreparedStatement statement = null;
	private Connection connection = null;
	private ResultSet resultSet = null;
	
	public Response publishReport(ArrayList<Report1Entity> data) {
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
}
