
package org.tms.db;

import org.tms.entities.RecordEntity;
import org.tms.db.localdb.RetrieveDaysAverage;
import org.tms.db.localdb.RetrieveReport2;
import org.tms.utilities.Day;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import javafx.concurrent.Task;
import org.asynchttpclient.Response;

public class TaskExecutor {
	Task<?> myTask;
	DatabaseOperation databaseOperation = new DatabaseOperation();

	private TaskExecutor() {
	}

	private static TaskExecutor instance = null;

	public static TaskExecutor getInstance() {
		if (instance == null)
			instance = new TaskExecutor();
		return instance;
	}

	public void registerUser(String username, String password, String email) {
		myTask = new Task<Response>() {
			@Override
			protected Response call() throws Exception {
				CompletableFuture<Response> completableFuture = CompletableFuture
						.supplyAsync(() -> databaseOperation.registerUser(username, password, email))
						.thenApply(data -> databaseOperation.logoutUser(data));
				return completableFuture.get();
			}
		};
		new Thread(myTask).start();
	}

	public void loginUser(String username, String password) {
		myTask = new Task<Response>() {
			@Override
			protected Response call() throws Exception {
				return databaseOperation.retrieveUser(username, password);
			}
		};
		new Thread(myTask).start();
	}

	public void logout(String sessionToken) {
		myTask = new Task<Response>() {
			@Override
			protected Response call() throws Exception {
				return databaseOperation.logoutUser(sessionToken);
			}
		};
		new Thread(myTask).start();
	}

	public void publishReports() {
		myTask = new Task<Response>() {
			@Override
			protected Response call() throws Exception {
				RetrieveReport2 retrieveReport = new RetrieveReport2();
				RetrieveDaysAverage retrieveDaysAverage = new RetrieveDaysAverage();
				CompletableFuture<Response> completableFuture = CompletableFuture
						.runAsync(() -> retrieveReport.retrieveValue_COMPLETETIME(1))
						.thenRun(() -> retrieveReport.retrieveValue_COMPLETETIME(2))
						.thenRun(() -> retrieveReport.retrieveValue_COMPLETETIME(3))
						.thenRun(() -> retrieveReport.retrieveValue_COMPLETETIME(4))
						.thenRun(() -> retrieveReport.retrieveValue_COMPLETETIME(5))
						.thenRun(() -> retrieveReport.retrieveValue_COMPLETETIME(6))
						.thenRun(() -> retrieveReport.retrieveValue_COMPLETETIME(7))
						.thenRun(() -> retrieveReport.retrieveValue_COMPLETETIME(8))
						.thenRun(() -> retrieveReport.retrieveValue_COMPLETETIME(9))
						.thenRun(() -> retrieveReport.retrieveValue_COMPLETETIME(10))
						.thenRun(() -> retrieveReport.retrieveValue_COMPLETETIME(11))
						.thenRun(() -> retrieveReport.retrieveValue_COMPLETETIME(12))
						.thenRun(() -> retrieveReport.retrieveValue_COMPLETETIME(13))
						.thenRun(() -> retrieveReport.retrieveValue_COMPLETETIME(14))
						.thenRun(() -> retrieveReport.retrieveValue_COMPLETETIME(15))
						.thenRun(() -> retrieveReport.retrieveValue_COMPLETETIME(16))
						.thenRun(() -> retrieveReport.retrieveValue_COMPLETETIME(17))
						.thenRun(() -> retrieveReport.retrieveValue_COMPLETETIME(18))
						.thenRun(() -> retrieveReport.retrieveValue_COMPLETETIME(19))
						.thenRun(() -> retrieveReport.retrieveValue_COMPLETETIME(20))
						.thenRun(() -> retrieveReport.retrieveValue_COMPLETETIME(21))
						.thenRun(() -> retrieveReport.retrieveValue_COMPLETETIME(22))
						.thenRun(() -> retrieveReport.retrieveValue_COMPLETETIME(23))
						.thenRun(() -> retrieveDaysAverage.retrieveReport(Day.Monday))
						.thenRun(() -> retrieveDaysAverage.retrieveReport(Day.Tuesday))
						.thenRun(() -> retrieveDaysAverage.retrieveReport(Day.Wednesday))
						.thenRun(() -> retrieveDaysAverage.retrieveReport(Day.Thursday))
						.thenRun(() -> retrieveDaysAverage.retrieveReport(Day.Friday))
						.thenRun(() -> retrieveDaysAverage.retrieveReport(Day.Saturday))
						.thenRun(() -> retrieveDaysAverage.retrieveReport(Day.Sunday))
						.thenRun(() -> retrieveDaysAverage.insert(retrieveDaysAverage.getReportList()))
						.thenApply(data -> retrieveReport.insert(retrieveReport.getResult()));
				Response response = null;
				try {
					response = completableFuture.get();
				} catch (InterruptedException interruptedException) {
					interruptedException.printStackTrace();
				} catch (ExecutionException executionException) {
					executionException.printStackTrace();
				}
				return response;
			}
		};
		new Thread(myTask).start();
	}

	/*
	 * public void publishReport2() { myTask = new Task<Response>() {
	 * 
	 * @Override protected Response call() throws Exception {
	 * CompletableFuture<Void> cf = CompletableFuture .runAsync(()->) return; } };
	 * new Thread(myTask).start(); }
	 */
	public Task<?> getMyTask() {
		return myTask;
	}
}
