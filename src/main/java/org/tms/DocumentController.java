package org.tms;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tms.db.localdb.TrafficLevelOfServiceDAO;
import org.tms.entities.LevelOfServiceEntity;
import org.tms.model.jfxbeans.LevelOfServiceEntityFX;

import java.net.ConnectException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;


public class DocumentController implements Initializable {

    final Logger log = LoggerFactory.getLogger(DocumentController.class);

    @FXML
    private Text startText;
    @FXML
    private Text endText;
    @FXML
    private TableView byVolumeTableView;
    @FXML
    private TableView bySpeedTableView;

    @Override
    public void initialize(URL url, ResourceBundle rb) {

    }

    public void initDocument(String start, String end) {
        startText.setText(start);
        endText.setText(end);
        retrieveVolumeReport();
        retrieveSpeedReport();
    }

    public void retrieveVolumeReport() {
        byVolumeTableView.getColumns().clear();
        byVolumeTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        ArrayList<LevelOfServiceEntity> levelOfServiceEntityList = new ArrayList<LevelOfServiceEntity>();
        TrafficLevelOfServiceDAO trafficLevelOfServiceDAO = null;
        try {
            trafficLevelOfServiceDAO = new TrafficLevelOfServiceDAO();
        } catch (ConnectException e) {
            e.printStackTrace();
        }
        levelOfServiceEntityList = trafficLevelOfServiceDAO.getTopTrafficVolume(startText.getText(), endText.getText(), 5);

        ArrayList<LevelOfServiceEntityFX> levelOfServiceEntityListFX = new ArrayList<LevelOfServiceEntityFX>();
        levelOfServiceEntityList.forEach(levelOfServiceEntity -> {
            levelOfServiceEntityListFX.add(new LevelOfServiceEntityFX(
                    Double.toString(levelOfServiceEntity.volume), Double.toString(levelOfServiceEntity.avgSpeed), levelOfServiceEntity.facility));
        });
        ObservableList<LevelOfServiceEntityFX> tableData =
                FXCollections.observableArrayList(levelOfServiceEntityListFX);

        TableColumn facilityCol = new TableColumn("Facility");
        TableColumn volumeCol = new TableColumn("Volume");
        TableColumn avgSpeedCol = new TableColumn("Average speed");

        byVolumeTableView.getColumns().addAll(facilityCol, volumeCol, avgSpeedCol);

        facilityCol.setCellValueFactory(cellData -> ((TableColumn.CellDataFeatures<LevelOfServiceEntityFX, Object>)cellData).getValue().facilityProperty());

        volumeCol.setCellValueFactory(cellData -> ((TableColumn.CellDataFeatures<LevelOfServiceEntityFX, Object>)cellData).getValue().volumeProperty());
        avgSpeedCol.setCellValueFactory(cellData -> ((TableColumn.CellDataFeatures<LevelOfServiceEntityFX, Object>)cellData).getValue().avgSpeedProperty());

        byVolumeTableView.setItems(tableData);

    }

    public void retrieveSpeedReport() {
        bySpeedTableView.getColumns().clear();
        bySpeedTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        ArrayList<LevelOfServiceEntity> levelOfServiceEntityList = new ArrayList<LevelOfServiceEntity>();
        TrafficLevelOfServiceDAO trafficLevelOfServiceDAO = null;
        try {
            trafficLevelOfServiceDAO = new TrafficLevelOfServiceDAO();
        } catch (ConnectException e) {
            e.printStackTrace();
        }
        levelOfServiceEntityList = trafficLevelOfServiceDAO.getTopTrafficSpeed(startText.getText(), endText.getText(), 5);

        ArrayList<LevelOfServiceEntityFX> levelOfServiceEntityListFX = new ArrayList<LevelOfServiceEntityFX>();
        levelOfServiceEntityList.forEach(levelOfServiceEntity -> {
            levelOfServiceEntityListFX.add(new LevelOfServiceEntityFX(
                    Double.toString(levelOfServiceEntity.volume), Double.toString(levelOfServiceEntity.avgSpeed), levelOfServiceEntity.facility));
        });
        ObservableList<LevelOfServiceEntityFX> tableData =
                FXCollections.observableArrayList(levelOfServiceEntityListFX);

        TableColumn facilityCol = new TableColumn("Facility");
        TableColumn volumeCol = new TableColumn("Volume");
        TableColumn avgSpeedCol = new TableColumn("Average speed");

        bySpeedTableView.getColumns().addAll(facilityCol, avgSpeedCol, volumeCol );

        facilityCol.setCellValueFactory(cellData -> ((TableColumn.CellDataFeatures<LevelOfServiceEntityFX, Object>)cellData).getValue().facilityProperty());

        volumeCol.setCellValueFactory(cellData -> ((TableColumn.CellDataFeatures<LevelOfServiceEntityFX, Object>)cellData).getValue().volumeProperty());
        avgSpeedCol.setCellValueFactory(cellData -> ((TableColumn.CellDataFeatures<LevelOfServiceEntityFX, Object>)cellData).getValue().avgSpeedProperty());

        bySpeedTableView.setItems(tableData);

    }
}
