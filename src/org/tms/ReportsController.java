package org.tms;

import org.tms.db.TaskExecutor;
import org.tms.entities.AvgSpeedEntity;
import org.tms.entities.VolumeEntity;
import org.tms.entities.LevelOfServiceEntity;
import org.tms.entities.RecordEntity;
import org.tms.model.Period;
import org.tms.model.jfxbeans.LevelOfServiceEntityFX;
import org.tms.db.localdb.DBOperations;
import org.tms.db.localdb.TrafficVolumeDAO;
import org.tms.db.localdb.RetrieveReport2;
import org.tms.db.localdb.TrafficLevelOfServiceDAO;
import org.tms.db.localdb.TrafficSpeedDAO;
import org.tms.utilities.Day;
import org.tms.utilities.GlobalObjects;
import org.tms.utilities.InitializeReport2;
import com.jfoenix.controls.JFXButton;

import java.awt.List;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
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
import javafx.geometry.Insets;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import org.asynchttpclient.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReportsController implements Initializable {
	final Logger log = LoggerFactory.getLogger(ReportsController.class);
	RetrieveReport2 retrieveReport = new RetrieveReport2();
	TrafficVolumeDAO trafficVolumeDAO = new TrafficVolumeDAO();
	TrafficSpeedDAO trafficSpeedDAO = new TrafficSpeedDAO();
	
	@FXML
	private TableView<RecordEntity> tableview_report;
	@FXML
	private AnchorPane mainAnchorPain;
	@FXML
	private TabPane mainTabPane;
	@FXML
	private JFXButton refreshButton, publishButton;
	@FXML
	private ProgressIndicator progress_reports;
	@FXML
	private TableView<LevelOfServiceEntityFX> lvlOfServiceTableView;
	@FXML
	private Tab volumeTab;
	@FXML
	private Tab lvlOfServiceTab;
	@FXML
	private Tab avgSpeedTab;
	@FXML
	private ComboBox<String> avgVolComboBox;
	@FXML
	private AnchorPane avgVolChartAnchorPane;
	@FXML
	private ComboBox<String> avgSpdComboBox;
	@FXML
	private AnchorPane avgSpdChartAnchorPane;
	@FXML
	private DatePicker lvlOfServiceDatePicker;
	
	@Override
	public void initialize(URL url, ResourceBundle rb) {
		
		initVolumeReport();
		initLevelsOfServiceReport();
		initAvgSpeedReport();
	}

	@FXML
	private void handleRefreshButtonAction(ActionEvent event) {
		log.debug("active tab: " + mainTabPane.getSelectionModel().getSelectedIndex());
		int activeTab = mainTabPane.getSelectionModel().getSelectedIndex();
		if (activeTab == 0) {
			generateVolumeChart();			
		} else if (activeTab == 1) {
			generateLevelsOfServiceTable();
		} else if (activeTab == 2) {
			generateAvgSpeedChart();
		}
	}

	@FXML
	private void handlePublishButtonAction(ActionEvent event) {
		TaskExecutor.getInstance().publishReports();
		GlobalObjects.getInstance().bindBtnNProgress(publishButton, progress_reports,
				TaskExecutor.getInstance().getMyTask().runningProperty());
		TaskExecutor.getInstance().getMyTask().setOnSucceeded(new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle(WorkerStateEvent event) {
				GlobalObjects.getInstance().showMessage("Publish done", mainAnchorPain);
				Response response = (Response) TaskExecutor.getInstance().getMyTask().getValue();
			}
		});
		TaskExecutor.getInstance().getMyTask().setOnFailed(new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle(WorkerStateEvent event) {
				GlobalObjects.getInstance().showMessage("Error", mainAnchorPain);
			}
		});
	}
	
	@FXML
	private void handleLvlOfServiceDatePickerAction() {
		log.debug("DatePicker: " + lvlOfServiceDatePicker.getValue().toString());
		
		generateLevelsOfServiceTable();
	}
	
	@FXML
	private void handleAvgVolComboBoxAction(ActionEvent event) throws IOException {
		
		log.debug("Period: " + avgVolComboBox.getValue());
		generateVolumeChart();
		
	}
	
	@FXML
	private void handleAvgSpdComboBoxAction(ActionEvent event) throws IOException {
		log.debug("Period: " + avgSpdComboBox.getValue());
		generateAvgSpeedChart();

	}
	
	public void initVolumeReport() {
		log.info("entry");
		
		ObservableList<String> options = 
			    FXCollections.observableArrayList(Period.ALL.getPeriod(), Period.LAST_7_DAYS.getPeriod(), Period.LAST_30_DAYS.getPeriod());
		avgVolComboBox.getItems().addAll(options);
		avgVolComboBox.setValue(options.get(1));
		generateVolumeChart();
		
		log.info("exit");
	}
	
	public void initLevelsOfServiceReport() {
		lvlOfServiceDatePicker.setValue(LocalDate.now());
		log.debug("DatePicker: " + lvlOfServiceDatePicker.getValue().toString());
		generateLevelsOfServiceTable();
	}
	
	public void initAvgSpeedReport() {
		log.info("entry");
				
		ObservableList<String> options = 
			    FXCollections.observableArrayList(Period.ALL.getPeriod(), Period.LAST_7_DAYS.getPeriod(), Period.LAST_30_DAYS.getPeriod());
		avgSpdComboBox.getItems().addAll(options);
		avgSpdComboBox.setValue(options.get(1));
		
		generateAvgSpeedChart();
	
		log.info("exit");
	}
	
	public void generateVolumeChart() {
		ArrayList<VolumeEntity> volumeEntityList = new ArrayList<VolumeEntity>();
		trafficVolumeDAO = new TrafficVolumeDAO();
		XYChart.Series<Double, String> series = new XYChart.Series<Double, String>();
		volumeEntityList = trafficVolumeDAO.getVolumePerHour(avgVolComboBox.getValue());
		
		//Defining the y axis   
		avgVolChartAnchorPane.getChildren().clear();
		CategoryAxis xAxis = new CategoryAxis (); 
		xAxis.setLabel("Daily"); 		
		
		NumberAxis yAxis = new NumberAxis(0, 10000, 10); 
		yAxis.setLabel("Number of Vehicles"); 
		LineChart lineChart = new LineChart(xAxis, yAxis);
		lineChart.setPrefWidth(700);
		lineChart.setPrefHeight(370);

		//Prepare XYChart.Series objects by setting data 
		series.setName("Volume of vehicles per day");
		volumeEntityList.forEach(volumeEntity -> {
			log.debug("-----------------------");
			log.debug("date: " + volumeEntity.date + " volume: " + volumeEntity.volume);
			series.getData().add(new XYChart.Data(volumeEntity.date, volumeEntity.volume)); 
		});

		//Setting the data to Line chart  
		lineChart.getData().clear();
		lineChart.getData().add(series);        

		//Creating a Group object  
		Group avgVolChartGroup = new Group(lineChart); 
		
		// Add to anchor pane container
		avgVolChartAnchorPane.getChildren().add(avgVolChartGroup);
	
	}
		
	public void generateAvgSpeedChart() {
		ArrayList<AvgSpeedEntity> avgSpeedEntityList = new ArrayList<AvgSpeedEntity>();
		trafficSpeedDAO = new TrafficSpeedDAO();
		XYChart.Series<Double, String> series = new XYChart.Series<Double, String>();
		avgSpeedEntityList = trafficSpeedDAO.getAvgSpeed(avgSpdComboBox.getValue());
		ArrayList<String> categories = new ArrayList<String>();
		
		//Defining the y axis   
		avgSpdChartAnchorPane.getChildren().clear();
		CategoryAxis xAxis = new CategoryAxis (); 
		xAxis.setLabel("Daily"); 		
		
		NumberAxis yAxis = new NumberAxis(0, 100, 20); 
		yAxis.setLabel("Average Speed"); 
		LineChart lineChart = new LineChart(xAxis, yAxis);
		lineChart.setPrefWidth(700);
		lineChart.setPrefHeight(370);

		avgSpeedEntityList.forEach(avgSpeedEntity -> {
			categories.add(avgSpeedEntity.date); 
		});
		
		log.debug("categories length: " + categories.size());

		//Prepare XYChart.Series objects by setting data 
		series.setName("Average speed of vehicles per day");
		avgSpeedEntityList.forEach(avgSpeedEntity -> {
			log.debug("-----------------------");
			log.debug("date: " + avgSpeedEntity.date + " avg: " + avgSpeedEntity.avg);
			series.getData().add(new XYChart.Data(avgSpeedEntity.date, avgSpeedEntity.avg)); 
		});

		//Setting the data to Line chart  
		lineChart.getData().clear();
		lineChart.getData().add(series);        

		//Creating a Group object  
		Group avgSpdChartGroup = new Group(lineChart); 
		
		// Add to anchor pane container
		avgSpdChartAnchorPane.getChildren().add(avgSpdChartGroup);
	}
	
	public void generateLevelsOfServiceTable() {
		log.info("entry");
		
		// clear table
		lvlOfServiceTableView.getColumns().clear();
		ArrayList<LevelOfServiceEntity> levelOfServiceEntityList = new ArrayList<LevelOfServiceEntity>();
		TrafficLevelOfServiceDAO trafficLevelOfServiceDAO = new TrafficLevelOfServiceDAO();
		levelOfServiceEntityList = trafficLevelOfServiceDAO.getVolumePerHour(lvlOfServiceDatePicker.getValue().toString());
		
		ArrayList<LevelOfServiceEntityFX> levelOfServiceEntityListFX = new ArrayList<LevelOfServiceEntityFX>();
		levelOfServiceEntityList.forEach(levelOfServiceEntity -> {
			levelOfServiceEntityListFX.add(new LevelOfServiceEntityFX(levelOfServiceEntity.hour, 
					Double.toString(levelOfServiceEntity.volume), Double.toString(levelOfServiceEntity.avgSpeed), levelOfServiceEntity.facility, levelOfServiceEntity.facilityType, levelOfServiceEntity.lvlOfService)); 
		});
		ObservableList<LevelOfServiceEntityFX> tableData =
				FXCollections.observableArrayList(levelOfServiceEntityListFX);

		TableColumn hourCol = new TableColumn("Hour");
        TableColumn facilityCol = new TableColumn("Facility");
        TableColumn facilityTypeCol = new TableColumn("Facility type");
        TableColumn volumeCol = new TableColumn("Volume");
        TableColumn avgSpeedCol = new TableColumn("Average speed");
        TableColumn lvlOfServiceCol = new TableColumn("Level of service");
        
        lvlOfServiceTableView.getColumns().addAll(hourCol, facilityCol, facilityTypeCol, volumeCol, avgSpeedCol, lvlOfServiceCol);
        
//        hourCol.setCellValueFactory(
//				new PropertyValueFactory<LevelOfServiceEntity,String>("hour")
//				);
//        facilityCol.setCellValueFactory(
//				new PropertyValueFactory<LevelOfServiceEntity,String>("facility")
//				);
//        facilityTypeCol.setCellValueFactory(
//				new PropertyValueFactory<LevelOfServiceEntity,String>("facilityType")
//				);
//        volumeCol.setCellValueFactory(
//				new PropertyValueFactory<LevelOfServiceEntity,String>("volume")
//				);
//        lvlOfServiceCol.setCellValueFactory(
//				new PropertyValueFactory<LevelOfServiceEntity,String>("lvlOfService")
//				);
        
        hourCol.setCellValueFactory(cellData -> ((TableColumn.CellDataFeatures<LevelOfServiceEntityFX, Object>)cellData).getValue().hourProperty());
        volumeCol.setCellValueFactory(cellData -> ((TableColumn.CellDataFeatures<LevelOfServiceEntityFX, Object>)cellData).getValue().volumeProperty());
        avgSpeedCol.setCellValueFactory(cellData -> ((TableColumn.CellDataFeatures<LevelOfServiceEntityFX, Object>)cellData).getValue().avgSpeedProperty());
        facilityCol.setCellValueFactory(cellData -> ((TableColumn.CellDataFeatures<LevelOfServiceEntityFX, Object>)cellData).getValue().facilityProperty());
        facilityTypeCol.setCellValueFactory(cellData -> ((TableColumn.CellDataFeatures<LevelOfServiceEntityFX, Object>)cellData).getValue().facilityTypeProperty());
        lvlOfServiceCol.setCellValueFactory(cellData -> ((TableColumn.CellDataFeatures<LevelOfServiceEntityFX, Object>)cellData).getValue().lvlOfServiceProperty());
        lvlOfServiceCol.setCellFactory(column -> {
            return new TableCell<LevelOfServiceEntity, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);

                    // Style all dates in March with a different color.
                    log.debug("item: " + item);
                    if (item == null || empty) {
						setText(null);
						setStyle("");
                    } else {
                    	setText(item);
                    	 if (item == "FAILED") {
                         	setTextFill(Color.RED);
                         	//                            setStyle("-fx-background-color: red");
                         } else if (item == "EXCELLENT"){
                         	setTextFill(Color.BLUE);
                         	//                            setStyle("-fx-background-color: lightgreen");
                         } else {
                        	 setTextFill(Color.GREEN);
                         }
                    }
                   
                }
            };
        });
        
        lvlOfServiceTableView.setItems(tableData);
        
// 
//        final VBox vbox = new VBox();
//        vbox.setSpacing(5);
//        vbox.setPadding(new Insets(10, 0, 0, 10));
//        vbox.getChildren().addAll(label, lvlOfServiceTableView);
		
		// Add to anchor pane container;
		log.info("exit");
	}
}
