/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.tms;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXProgressBar;
import com.jfoenix.controls.JFXTextField;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tms.db.TaskExecutor;
import org.tms.utilities.GlobalObjects;

/**
 * FXML Controller class
 *
 * @author JOEY
 */
public class RegisterController implements Initializable {
    final Logger log = LoggerFactory.getLogger(RegisterController.class);
    @FXML
    private JFXTextField register_email;
    @FXML
    private JFXTextField register_username;
    @FXML
    private JFXPasswordField register_password;
    @FXML
    private JFXButton register_submit;
    @FXML
    private JFXButton register_cancel;
    @FXML
    private JFXPasswordField register_repassword;
    @FXML
    private JFXProgressBar register_progress;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        log.info("Register page started...");
    }    

    @FXML
    private void submitClicked(ActionEvent event) {
        if(!validateField(register_email) | !validateField(register_username) | 
                !validatePasswordField(register_password) | !validatePasswordField(register_repassword)){
            return;
        }
        
        if(!isPasswordMatched(register_password, register_repassword)){
            return;
        }
        
        String username = register_username.getText().trim();
        String password = register_password.getText().trim();
        String email = register_email.getText().trim();
        TaskExecutor.getInstance().registerUser(username, password, email);
        GlobalObjects.getInstance().bindBtnNProgress(register_submit, register_progress, TaskExecutor.getInstance().getMyTask().runningProperty());
        TaskExecutor.getInstance().getMyTask().setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent event) {
                GlobalObjects.getInstance().openNewWindow("Login.fxml", "Login", StageStyle.DECORATED);
                ((Stage) register_submit.getScene().getWindow()).close();
            }
        });
    }

    @FXML
    private void cancelClicked(ActionEvent event) {
        GlobalObjects.getInstance().openNewWindow("Login.fxml", "Login", StageStyle.DECORATED);
        ((Stage) register_cancel.getScene().getWindow()).close();
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
    
    private boolean isPasswordMatched(JFXPasswordField field1, JFXPasswordField field2){
        if(!field1.getText().trim().equals(field2.getText().trim())){
            field1.setUnFocusColor(Color.RED);
            field2.setUnFocusColor(Color.RED);
            return false;
        } else {
            field1.setUnFocusColor(Color.BLACK);
            field2.setUnFocusColor(Color.BLACK);
            return true;
        }
    }
    
}
