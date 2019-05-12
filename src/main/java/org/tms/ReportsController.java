package org.tms;

import com.jfoenix.controls.JFXButton;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.print.PrinterJob;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.asynchttpclient.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tms.db.TaskExecutor;
import org.tms.db.localdb.RetrieveReport2;
import org.tms.db.localdb.TrafficLevelOfServiceDAO;
import org.tms.db.localdb.TrafficSpeedDAO;
import org.tms.db.localdb.TrafficVolumeDAO;
import org.tms.entities.AvgSpeedEntity;
import org.tms.entities.LevelOfServiceEntity;
import org.tms.entities.RecordEntity;
import org.tms.entities.VolumeEntity;
import org.tms.model.Period;
import org.tms.model.jfxbeans.LevelOfServiceEntityFX;
import org.tms.utilities.GlobalObjects;

import javax.swing.*;
import java.io.IOException;
import java.net.ConnectException;
import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class ReportsController implements Initializable {
	final Logger log = LoggerFactory.getLogger(ReportsController.class);
	RetrieveReport2 retrieveReport = new RetrieveReport2();


	@FXML
	private TableView<RecordEntity> tableview_report;
	@FXML
	private AnchorPane mainAnchorPain;
	@FXML
	private TabPane mainTabPane;
	@FXML
	private JFXButton refreshButton, toPdfButton;
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
	private ComboBox<String> areaComboBox;
	@FXML
	private ComboBox<String> areaComboBox1;
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
	@FXML
	private DatePicker startDatePicker;
	@FXML
	private DatePicker endDatePicker;

	@Override
	public void initialize(URL url, ResourceBundle rb) {

		initVolumeReport();
		initLevelsOfServiceReport();
		initAvgSpeedReport();
		initReport();

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
	private void handleToPDFButtonAction(ActionEvent event) throws IOException {
		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/Document.fxml"));
		Parent root = fxmlLoader.load();
		DocumentController documentController = fxmlLoader.<DocumentController>getController();
		documentController.initDocument(startDatePicker.getValue().toString(), endDatePicker.getValue().toString());
		Scene scene = new Scene(root);
		Stage stage = new Stage();
		stage.setScene(scene);
		stage.show();


//		stage = (Stage) mainAnchorPain.getScene().getWindow();
		PrinterJob job = PrinterJob.createPrinterJob();
		if(job != null){
			job.showPrintDialog(stage); // Window must be your main Stage
			job.printPage(root);
			job.endJob();
		}


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
		TrafficVolumeDAO trafficVolumeDAO = null;
		try {
			trafficVolumeDAO = new TrafficVolumeDAO();
		} catch (ConnectException e) {
			e.printStackTrace();
		}
		ArrayList<String> areaList = new ArrayList<String>();

		areaList = trafficVolumeDAO.getAreaList();

		areaComboBox.getItems().addAll(areaList);
		areaComboBox.setValue(areaList.get(0));
		
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
		TrafficVolumeDAO trafficVolumeDAO = null;
		try {
			trafficVolumeDAO = new TrafficVolumeDAO();
		} catch (ConnectException e) {
			e.printStackTrace();
		}
		ArrayList<String> areaList = new ArrayList<String>();

		areaList = trafficVolumeDAO.getAreaList();

		areaComboBox1.getItems().addAll(areaList);
		areaComboBox1.setValue(areaList.get(0));
				
		ObservableList<String> options = 
			    FXCollections.observableArrayList(Period.ALL.getPeriod(), Period.LAST_7_DAYS.getPeriod(), Period.LAST_30_DAYS.getPeriod());
		avgSpdComboBox.getItems().addAll(options);
		avgSpdComboBox.setValue(options.get(1));
		
		generateAvgSpeedChart();
	
		log.info("exit");
	}

	public void initReport() {
		startDatePicker.setValue(LocalDate.now());
		endDatePicker.setValue(LocalDate.now());
	}
	
	public void generateVolumeChart() {
		TrafficVolumeDAO trafficVolumeDAO = null;
		try {
			trafficVolumeDAO = new TrafficVolumeDAO();
		} catch (ConnectException e) {
			e.printStackTrace();
		}
		ArrayList<VolumeEntity> volumeEntityList = new ArrayList<VolumeEntity>();
		XYChart.Series<Double, String> series = new XYChart.Series<Double, String>();
		volumeEntityList = trafficVolumeDAO.getVolumePerHour(avgVolComboBox.getValue(), areaComboBox.getValue());
		
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
		TrafficSpeedDAO trafficSpeedDAO = null;
		try {
			trafficSpeedDAO = new TrafficSpeedDAO();
		} catch (ConnectException e) {
			e.printStackTrace();
		}

		ArrayList<AvgSpeedEntity> avgSpeedEntityList = new ArrayList<AvgSpeedEntity>();
		XYChart.Series<Double, String> series = new XYChart.Series<Double, String>();
		avgSpeedEntityList = trafficSpeedDAO.getAvgSpeed(avgSpdComboBox.getValue(), areaComboBox1.getValue());
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
		TrafficLevelOfServiceDAO trafficLevelOfServiceDAO = null;
		try {
			trafficLevelOfServiceDAO = new TrafficLevelOfServiceDAO();
		} catch (ConnectException e) {
			e.printStackTrace();
		}
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
