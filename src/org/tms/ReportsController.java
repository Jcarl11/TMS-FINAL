package org.tms;

import org.tms.db.TaskExecutor;
import org.tms.entities.AvgSpeedEntity;
import org.tms.entities.RecordEntity;
import org.tms.model.Period;
import org.tms.db.localdb.DBOperations;
import org.tms.db.localdb.RetrieveDaysAverage;
import org.tms.db.localdb.RetrieveReport2;
import org.tms.db.localdb.TrafficSpeedDAO;
import org.tms.utilities.Day;
import org.tms.utilities.GlobalObjects;
import org.tms.utilities.InitializeReport2;
import com.jfoenix.controls.JFXButton;

import java.awt.List;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.NoSuchElementException;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableView;
import javafx.scene.layout.AnchorPane;
import org.asynchttpclient.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReportsController implements Initializable {
	final Logger log = LoggerFactory.getLogger(ReportsController.class);
	RetrieveReport2 retrieveReport = new RetrieveReport2();
	RetrieveDaysAverage db = new RetrieveDaysAverage();
	TrafficSpeedDAO trafficSpeedDao = new TrafficSpeedDAO();
	
	@FXML
	private Tab tab_averagevolume, tab_report2;
	@FXML
	private LineChart<?, ?> chart_averagevolume;
	@FXML
	private NumberAxis axis_y;
	@FXML
	private CategoryAxis axis_x;
	@FXML
	private TableView<RecordEntity> tableview_report;
	@FXML
	private JFXButton button_refresh, button_publish;
	@FXML
	private ProgressIndicator progress_reports;
	@FXML
	private TabPane tabpane_main;
	@FXML
	private AnchorPane anchorpane_main;
	@FXML
	private Tab avgSpeedTab;
	@FXML
	private NumberAxis avgSpeedNumberAxis;
	@FXML
	private CategoryAxis avgSpeedCategoryAxis;
	@FXML
	private LineChart<Double, String> avgSpeedLineChart;
	@FXML
	private ComboBox<String> avgVolComboBox;
	@FXML
	private ComboBox<String> avgSpdComboBox;
	@FXML
	private AnchorPane avgSpdChartAnchorPane;
	
	@Override
	public void initialize(URL url, ResourceBundle rb) {
		CompletableFuture<Void> cf = CompletableFuture.runAsync(() -> db.retrieve(Day.Monday))
				.thenRun(() -> db.retrieve(Day.Tuesday)).thenRun(() -> db.retrieve(Day.Wednesday))
				.thenRun(() -> db.retrieve(Day.Thursday)).thenRun(() -> db.retrieve(Day.Friday))
				.thenRun(() -> db.retrieve(Day.Saturday)).thenRun(() -> db.retrieve(Day.Sunday));
		try {
			cf.get();
		} catch (InterruptedException e) {
			log.error(e.toString());
		} catch (ExecutionException e) {
			log.error(e.toString());
		}
		HashMap<Day, String> result = db.getResult();
		XYChart.Series series = new XYChart.Series();
		series.setName("Volume of cars per day");
		series.getData().add(new XYChart.Data(Day.Monday.toString(), Double.valueOf(result.get(Day.Monday))));
		series.getData().add(new XYChart.Data(Day.Tuesday.toString(), Double.valueOf(result.get(Day.Tuesday))));
		series.getData().add(new XYChart.Data(Day.Wednesday.toString(), Double.valueOf(result.get(Day.Wednesday))));
		series.getData().add(new XYChart.Data(Day.Thursday.toString(), Double.valueOf(result.get(Day.Thursday))));
		series.getData().add(new XYChart.Data(Day.Friday.toString(), Double.valueOf(result.get(Day.Friday))));
		series.getData().add(new XYChart.Data(Day.Saturday.toString(), Double.valueOf(result.get(Day.Saturday))));
		series.getData().add(new XYChart.Data(Day.Sunday.toString(), Double.valueOf(result.get(Day.Sunday))));
		chart_averagevolume.getData().add(series);
		InitializeReport2 report2 = new InitializeReport2();
		report2.setTableView(tableview_report);
		CompletableFuture<Void> completableFuture2 = CompletableFuture.runAsync(() -> retrieveReport.retrieveValue(1))
				.thenRun(() -> retrieveReport.retrieveValue(2)).thenRun(() -> retrieveReport.retrieveValue(3))
				.thenRun(() -> retrieveReport.retrieveValue(4)).thenRun(() -> retrieveReport.retrieveValue(5))
				.thenRun(() -> retrieveReport.retrieveValue(6)).thenRun(() -> retrieveReport.retrieveValue(7))
				.thenRun(() -> retrieveReport.retrieveValue(8)).thenRun(() -> retrieveReport.retrieveValue(9))
				.thenRun(() -> retrieveReport.retrieveValue(10)).thenRun(() -> retrieveReport.retrieveValue(11))
				.thenRun(() -> retrieveReport.retrieveValue(12)).thenRun(() -> retrieveReport.retrieveValue(13))
				.thenRun(() -> retrieveReport.retrieveValue(14)).thenRun(() -> retrieveReport.retrieveValue(15))
				.thenRun(() -> retrieveReport.retrieveValue(16)).thenRun(() -> retrieveReport.retrieveValue(17))
				.thenRun(() -> retrieveReport.retrieveValue(18)).thenRun(() -> retrieveReport.retrieveValue(19))
				.thenRun(() -> retrieveReport.retrieveValue(20)).thenRun(() -> retrieveReport.retrieveValue(21))
				.thenRun(() -> retrieveReport.retrieveValue(22)).thenRun(() -> retrieveReport.retrieveValue(23));
		try {
			completableFuture2.get();
		} catch (InterruptedException e) {
			log.error(e.toString());
		} catch (ExecutionException e) {
			log.error(e.toString());
		}
		report2.populateTable(retrieveReport.getResult());
		
		ObservableList<String> options = 
			    FXCollections.observableArrayList(
			        "All",
			        "Last 7 days",
			        "Last 30 days"
			    );
		avgVolComboBox.getItems().addAll(options);
		avgVolComboBox.setValue(options.get(1));
		
		loadAvgSpeedReport();
	}

	@FXML
	private void button_refreshOnClick(ActionEvent event) {
	}

	@FXML
	private void button_publishOnClick(ActionEvent event) {
		TaskExecutor.getInstance().publishReports();
		GlobalObjects.getInstance().bindBtnNProgress(button_publish, progress_reports,
				TaskExecutor.getInstance().getMyTask().runningProperty());
		TaskExecutor.getInstance().getMyTask().setOnSucceeded(new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle(WorkerStateEvent event) {
				GlobalObjects.getInstance().showMessage("Publish done", anchorpane_main);
				Response response = (Response) TaskExecutor.getInstance().getMyTask().getValue();
			}
		});
		TaskExecutor.getInstance().getMyTask().setOnFailed(new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle(WorkerStateEvent event) {
				GlobalObjects.getInstance().showMessage("Error", anchorpane_main);
			}
		});
	}
	
	@FXML
	private void handleAvgVolComboBoxAction(ActionEvent event) throws IOException {
		
	}
	
	@FXML
	private void handleAvgSpdComboBoxAction(ActionEvent event) throws IOException {
		log.debug("Period: " + avgSpdComboBox.getValue());

		ArrayList<AvgSpeedEntity> avgSpeedEntityList = new ArrayList<AvgSpeedEntity>();
		trafficSpeedDao = new TrafficSpeedDAO();
		XYChart.Series<Double, String> series = new XYChart.Series<Double, String>();
		avgSpeedEntityList = trafficSpeedDao.getAvgSpeed(avgSpdComboBox.getValue());
		ArrayList<String> categories = new ArrayList<String>();
		//Defining the y axis   
		avgSpdChartAnchorPane.getChildren().clear();
		CategoryAxis xAxis = new CategoryAxis (); 
		xAxis.setLabel("Daily"); 		
		
		NumberAxis yAxis = new NumberAxis(0, 100, 20); 
		yAxis.setLabel("Average Speed"); 
		LineChart linechart = new LineChart(xAxis, yAxis);
		linechart.setPrefWidth(700);
		linechart.setPrefHeight(370);

//		avgSpeedCategoryAxis.setCategories(FXCollections.<String>observableArrayList(categories));
		avgSpeedEntityList.forEach(avgSpeedEntity -> {
			categories.add(avgSpeedEntity.date); 
		});
		
//		avgSpeedCategoryAxis.setCategories(FXCollections.<String>observableArrayList(categories));
		log.debug("categories length: " + categories.size());
//		series.getData().forEach(axis -> axis.setXValue((double) 0));
//		series.getData().forEach(axis -> axis.setYValue(""));
		//Prepare XYChart.Series objects by setting data 
		
		series.setName("Average speed of vehicles per day");
		avgSpeedEntityList.forEach(avgSpeedEntity -> {
			log.debug("-----------------------");
			log.debug("date: " + avgSpeedEntity.date + " avg: " + avgSpeedEntity.avg);
			series.getData().add(new XYChart.Data(avgSpeedEntity.date, avgSpeedEntity.avg)); 
		});

		//Setting the data to Line chart  
//		avgSpeedLineChart.getData().clear();
//		avgSpeedLineChart.getData().add(series);
		linechart.getData().clear();
		linechart.getData().add(series);        

		//Creating a Group object  
		//Creating a Group object  
		Group avgSpdChartGroup = new Group(linechart); 

		avgSpdChartAnchorPane.getChildren().add(avgSpdChartGroup);
	}
	
	public void loadAvgSpeedReport() {
		log.info("entry");
				
		ObservableList<String> options = 
			    FXCollections.observableArrayList(Period.ALL.getPeriod(), Period.LAST_7_DAYS.getPeriod(), Period.LAST_30_DAYS.getPeriod());
		avgSpdComboBox.getItems().addAll(options);
		avgSpdComboBox.setValue(options.get(1));
		
		ArrayList<AvgSpeedEntity> avgSpeedEntityList = new ArrayList<AvgSpeedEntity>();
		trafficSpeedDao = new TrafficSpeedDAO();
		avgSpeedEntityList = trafficSpeedDao.getAvgSpeed(avgSpdComboBox.getValue()); 
		ArrayList<String> categories = new ArrayList<String>();
		avgSpeedEntityList.forEach(avgSpeedEntity -> {
			categories.add(avgSpeedEntity.date); 
		});
//		avgSpeedCategoryAxis.setCategories(FXCollections.<String>observableArrayList(categories));
		
//		avgSpeedCategoryAxis.invalidateRange(categories);
		//Prepare XYChart.Series objects by setting data 
		
		//Defining the y axis   
		CategoryAxis xAxis = new CategoryAxis (); 
		xAxis.setLabel("Daily"); 		

		NumberAxis yAxis = new NumberAxis(0, 100, 20);
		yAxis.setLabel("Average Speed"); 
		LineChart linechart = new LineChart(xAxis, yAxis);  
		linechart.setPrefWidth(700);
		linechart.setPrefHeight(370);
		XYChart.Series series = new XYChart.Series();
		series.setName("Average speed of vehicles per day");
		avgSpeedEntityList.forEach(avgSpeedEntity -> {
			series.getData().add(new XYChart.Data(avgSpeedEntity.date, avgSpeedEntity.avg)); 
		});

		//Setting the data to Line chart    
//		avgSpeedLineChart.getData().clear();
//		avgSpeedLineChart.getData().add(series);
		linechart.getData().clear();
		linechart.getData().add(series);        

		//Creating a Group object  
		Group avgSpdChartGroup = new Group(linechart); 

		avgSpdChartAnchorPane.getChildren().add(avgSpdChartGroup);
	
		log.info("exit");
	}
}
