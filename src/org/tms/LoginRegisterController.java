package org.tms;

import org.tms.db.TaskExecutor;
import org.tms.utilities.GlobalObjects;
import org.tms.utilities.UsersPreferences;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXTextField;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javax.swing.JOptionPane;
import org.asynchttpclient.Response;
import org.json.JSONObject;

public class LoginRegisterController implements Initializable {
	@FXML
	private AnchorPane anchorpane_register, anchorpane_login;
	@FXML
	private JFXTextField register_textfield_email, register_textfield_username, login_textfield_username;
	@FXML
	private JFXPasswordField register_textfield_password, register_textfield_repassword, login_textfield_password;
	@FXML
	private JFXButton register_button_submit, login_button_login;
	@FXML
	private ProgressIndicator register_progress, login_progress;
	@FXML
	private CheckBox login_checkbox;

	@Override
	public void initialize(URL url, ResourceBundle rb) {

	}

	@FXML
	private void register_button_submitOnClick(ActionEvent event) {
		if (!registerHasNullFields()) {
			if (register_textfield_password.getText().trim().equals(register_textfield_repassword.getText().trim())) {
				String username = register_textfield_username.getText().trim();
				String email = register_textfield_email.getText().trim();
				String password = register_textfield_password.getText().trim();
				TaskExecutor.getInstance().registerUser(username, password, email);
				GlobalObjects.getInstance().bindBtnNProgress(register_button_submit, register_progress,
						TaskExecutor.getInstance().getMyTask().runningProperty());
				TaskExecutor.getInstance().getMyTask().setOnSucceeded(new EventHandler<WorkerStateEvent>() {
					@Override
					public void handle(WorkerStateEvent event) {
						GlobalObjects.getInstance().showMessage("A confirmation email was sent to your email",
								anchorpane_register);
					}
				});
				TaskExecutor.getInstance().getMyTask().setOnFailed(new EventHandler<WorkerStateEvent>() {
					@Override
					public void handle(WorkerStateEvent event) {
						GlobalObjects.getInstance().showMessage("Error", anchorpane_register);
					}
				});

			} else
				GlobalObjects.getInstance().showMessage("Password does not match", anchorpane_register);
		} else
			GlobalObjects.getInstance().showMessage("Don't leave null fields", anchorpane_register);
	}

	@FXML
	private void login_button_loginOnClick(ActionEvent event) {
		if (!loginHasNullFields()) {
			String username = login_textfield_username.getText().trim();
			String password = login_textfield_password.getText().trim();
			TaskExecutor.getInstance().loginUser(username, password);
			GlobalObjects.getInstance().bindBtnNProgress(login_button_login, login_progress,
					TaskExecutor.getInstance().getMyTask().runningProperty());
			TaskExecutor.getInstance().getMyTask().setOnSucceeded(new EventHandler<WorkerStateEvent>() {
				@Override
				public void handle(WorkerStateEvent event) {
					Response response = (Response) TaskExecutor.getInstance().getMyTask().getValue();
					JSONObject jsonResponse = new JSONObject(response.getResponseBody());
					if (response.getStatusCode() == 200) {
						if (jsonResponse.getBoolean("emailVerified") == true) {
							if (login_checkbox.isSelected())
								jsonResponse.put("rememberpassword", true);
							else
								jsonResponse.put("rememberpassword", false);
							UsersPreferences.getInstance().userData(jsonResponse);
							try {
								new TrafficMonitoringSystem().start(new Stage());
							} catch (Exception ex) {
								Logger.getLogger(LoginRegisterController.class.getName()).log(Level.SEVERE, null, ex);
							}
							((Stage) login_button_login.getScene().getWindow()).close();
						} else {
							TaskExecutor.getInstance().logout(jsonResponse.getString("sessionToken"));
							TaskExecutor.getInstance().getMyTask().setOnSucceeded(new EventHandler<WorkerStateEvent>() {
								@Override
								public void handle(WorkerStateEvent event) {
									Response response = (Response) TaskExecutor.getInstance().getMyTask().getValue();
									System.out.println(response.getStatusCode());
									if (response.getStatusCode() == 200)
										GlobalObjects.getInstance().showMessage("Please verify your email to continue",
												anchorpane_login);
									else
										GlobalObjects.getInstance().showMessage("Failed", anchorpane_login);
								}
							});

						}
					} else
						GlobalObjects.getInstance().showMessage("Wrong username/password", anchorpane_login);
				}
			});
		} else
			GlobalObjects.getInstance().showMessage("Don't leave null fields", anchorpane_login);
	}

	private boolean registerHasNullFields() {
		boolean hasNull = true;
		if (!register_textfield_email.getText().isEmpty() && !register_textfield_username.getText().isEmpty()
				&& !register_textfield_password.getText().isEmpty()
				&& !register_textfield_repassword.getText().isEmpty())
			hasNull = false;

		return hasNull;
	}

	private boolean loginHasNullFields() {
		boolean hasNull = true;
		if (!login_textfield_username.getText().trim().isEmpty()
				&& !login_textfield_password.getText().trim().isEmpty())
			hasNull = false;

		return hasNull;
	}
}
