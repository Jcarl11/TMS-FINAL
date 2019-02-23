
package org.tms.db;

import org.tms.entities.RecordEntity;
import org.tms.utilities.GlobalObjects;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.asynchttpclient.AsyncCompletionHandler;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.Dsl;
import org.asynchttpclient.ListenableFuture;
import org.asynchttpclient.Response;
import org.json.JSONArray;
import org.json.JSONObject;

public class DatabaseOperation {
	AsyncHttpClient asyncHttpClient = Dsl.asyncHttpClient();
	List<Future<Response>> responses;

	public Response retrieveUser(String username, String password) {
		System.out.println("Initalized");
		ListenableFuture<Response> lf = asyncHttpClient.prepareGet(GlobalObjects.URL_BASE + "login")
				.addHeader("X-Parse-Application-Id", GlobalObjects.APP_ID)
				.setHeader("X-Parse-REST-API-Key", GlobalObjects.REST_API_KEY)
				.setHeader("X-Parse-Revocable-Session", GlobalObjects.IRREVOCABLE_SESSION)
				.addQueryParam("username", username).addQueryParam("password", password)
				.execute(new AsyncCompletionHandler<Response>() {
					@Override
					public Response onCompleted(Response rspns) throws Exception {
						return rspns;
					}
				});
		Response response = null;
		try {
			response = lf.get();
		} catch (InterruptedException ex) {
			Logger.getLogger(DatabaseOperation.class.getName()).log(Level.SEVERE, null, ex);
		} catch (ExecutionException ex) {
			Logger.getLogger(DatabaseOperation.class.getName()).log(Level.SEVERE, null, ex);
		}
		return response;
	}

	public String registerUser(String username, String password, String email) {
		String sessionToken = null;
		ListenableFuture<Response> lf = asyncHttpClient.preparePost(GlobalObjects.URL_BASE + "users")
				.addHeader("X-Parse-Application-Id", GlobalObjects.APP_ID)
				.addHeader("X-Parse-REST-API-Key", GlobalObjects.REST_API_KEY)
				.addHeader("X-Parse-Revocable-Session", GlobalObjects.IRREVOCABLE_SESSION)
				.addHeader("Content-Type", "application/json")
				.setBody(GlobalObjects.getInstance().buildSignUpUser(username, password, email).toString())
				.execute(new AsyncCompletionHandler<Response>() {
					@Override
					public Response onCompleted(Response rspns) throws Exception {
						return rspns;
					}
				});
		Response response = null;
		try {
			response = lf.get();
			if (response.getStatusCode() == 201) {
				JSONObject session = new JSONObject(response.getResponseBody());
				sessionToken = session.getString("sessionToken");
				System.out.println(sessionToken);
			}
		} catch (InterruptedException ex) {
			ex.printStackTrace();
		} catch (ExecutionException ex) {
			ex.printStackTrace();
		}
		return sessionToken;
	}

	public Response logoutUser(String session) {
		ListenableFuture<Response> lf = asyncHttpClient.preparePost(GlobalObjects.getInstance().URL_BASE + "logout")
				.addHeader("X-Parse-Application-Id", GlobalObjects.getInstance().APP_ID)
				.addHeader("X-Parse-REST-API-Key", GlobalObjects.getInstance().REST_API_KEY)
				.addHeader("X-Parse-Session-Token", session).execute(new AsyncCompletionHandler<Response>() {
					@Override
					public Response onCompleted(Response rspns) throws Exception {
						return rspns;
					}
				});
		Response response = null;
		try {
			response = lf.get();
		} catch (InterruptedException ex) {
			ex.printStackTrace();
		} catch (ExecutionException ex) {
			ex.printStackTrace();
		}
		return response;
	}

}
