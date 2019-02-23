package org.tms.utilities;

import org.tms.entities.RecordEntity;
import java.util.ArrayList;
import java.util.HashMap;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class InitializeReport2 {
	private TableView<RecordEntity> tableView = new TableView<>();

	public void populateTable(ArrayList<RecordEntity> list) {
		initializeTable();
		tableView.getItems().addAll(list);
	}

	private void initializeTable() {
		TableColumn time = new TableColumn("Time");
		TableColumn facility = new TableColumn("Facility");
		TableColumn facilityType = new TableColumn("Facility Type");
		TableColumn los = new TableColumn("Level Of Service");

		tableView.getColumns().add(time);
		tableView.getColumns().add(facility);
		tableView.getColumns().add(facilityType);
		tableView.getColumns().add(los);

		time.setCellValueFactory(new PropertyValueFactory<RecordEntity, String>("time"));
		facility.setCellValueFactory(new PropertyValueFactory<RecordEntity, String>("facility"));
		facilityType.setCellValueFactory(new PropertyValueFactory<RecordEntity, String>("facilityType"));
		los.setCellValueFactory(new PropertyValueFactory<RecordEntity, String>("levelOfService"));
	}

	public void setTableView(TableView<RecordEntity> tableView) {
		this.tableView = tableView;
	}
}
