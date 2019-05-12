/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.tms;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXProgressBar;
import com.jfoenix.controls.JFXTextField;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.asynchttpclient.Response;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tms.db.TaskExecutor;
import org.tms.utilities.GlobalObjects;
import org.tms.utilities.UsersPreferences;

/**
 * FXML Controller class
 *
 * @author JOEY
 */
public class LoginController implements Initializable {
    final Logger log = LoggerFactory.getLogger(LoginController.class);
    @FXML
    private JFXTextField login_username;
    @FXML
    private JFXPasswordField login_password;
    @FXML
    private JFXButton login_submit;
    @FXML
    private JFXButton login_register;
    @FXML
    private JFXProgressBar login_progressbar;
    @FXML
    private AnchorPane login_anchorpane;
    @FXML
    private JFXCheckBox login_checkbox;
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        log.info("Login page started...");
    }    

    @FXML
    private void submitClicked(ActionEvent event) {
        
        if(!validateField(login_username) | !validatePasswordField(login_password)){
            return;
        }
        log.info("login process starting...");
        String username = login_username.getText().trim();
        String password = login_password.getText().trim();
        TaskExecutor.getInstance().loginUser(username, password);
        GlobalObjects.getInstance().bindBtnNProgress(login_submit, 
                login_progressbar, 
                TaskExecutor.getInstance().getMyTask().runningProperty());
        
        TaskExecutor.getInstance().getMyTask().setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent event) {
                Response response = (Response) TaskExecutor.getInstance().getMyTask().getValue();
                    JSONObject jsonResponse = new JSONObject(response.getResponseBody());
                    log.debug("Login response code: " + String.valueOf(response.getStatusCode()));
                    log.debug("RESPONSE BODY: " + response.toString());
                    if(response.getStatusCode() == 200) {
                        if (jsonResponse.getBoolean("emailVerified") == true) {
                                if (login_checkbox.isSelected())
                                        jsonResponse.put("rememberpassword", true);
                                else
                                        jsonResponse.put("rememberpassword", false);
                                UsersPreferences.getInstance().userData(jsonResponse);
                                try {
                                        new TrafficMonitoringSystem().start(new Stage());
                                } catch (Exception ex) {
                                        java.util.logging.Logger
                                                .getLogger(LoginController.class.getName())
                                                .log(Level.SEVERE, null, ex);
                                }
                                ((Stage) login_username.getScene().getWindow()).close();
                        } else {
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        TaskExecutor.getInstance().logout(jsonResponse.getString("sessionToken"));
                                TaskExecutor.getInstance().getMyTask().setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                                        @Override
                                        public void handle(WorkerStateEvent event) {
                                                Response response = (Response) TaskExecutor.getInstance().getMyTask().getValue();
                                                log.debug("logout response code: " + String.valueOf(response.getStatusCode()));
                                                if (response.getStatusCode() == 200){
                                                        
                                                        alert.setTitle("Oops");
                                                        alert.setHeaderText("Unconfirmed email");
                                                        alert.setContentText("Please verify your email to continue");
                                                        alert.show();
                                                } else {
                                                        alert.setTitle("Oops");
                                                        alert.setHeaderText("Something went wrong");
                                                        alert.setContentText("Operation Failed");
                                                        alert.show();
                                                }
                                        }
                                });
                        }
                    } else if(jsonResponse.getInt("code") == 101){
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Error");
                        alert.setHeaderText("Wrong credentials");
                        alert.setContentText("Invalid Username/Password");
                        alert.show();
                    }
            }
        });
    }

    @FXML
    private void registerClicked(ActionEvent event) {
        GlobalObjects.getInstance().openNewWindow("Register.fxml", "Register", StageStyle.DECORATED);
        ((Stage) login_register.getScene().getWindow()).close();
    }
    
    private boolean validateField(JFXTextField field){
        String value = field.getText().trim();
        if(value.isEmpty()){
            field.setUnFocusColor(Color.RED);
            return false;
        } else {
            field.setUnFocusColor(Color.BLACK);
            return true;
        }
    }
    private boolean validatePasswordField(JFXPasswordField field){
        String value = field.getText().trim();
        if(value.isEmpty()){
            field.setUnFocusColor(Color.RED);
            return false;
        } else {
            field.setUnFocusColor(Color.BLACK);
            return true;
        }
    }
    
}
