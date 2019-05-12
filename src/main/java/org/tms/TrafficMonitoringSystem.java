
package org.tms;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import org.opencv.core.Core;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tms.db.TaskExecutor;
import org.tms.utilities.GlobalObjects;
import org.tms.utilities.UsersPreferences;

/**
 *
 * @author Windows
 */
public class TrafficMonitoringSystem extends Application {
	@Override
	public void start(Stage stage) throws Exception {
		final Logger log = LoggerFactory.getLogger(TrafficMonitoringSystem.class);
		/*
		 * FXMLLoader loader = new FXMLLoader(getClass().getResource("Main.fxml"));
		 * loader.setController(new MainController()); BorderPane mainPane =
		 * loader.load();
		 */
                Font.loadFont(getClass().getResourceAsStream("/resources/Styles/Calibre Medium.otf"), 14);
                if(UsersPreferences.getInstance().getPreference().get("sessionToken", null) == null){
                    GlobalObjects.getInstance().openNewWindow("Login.fxml", "Login", StageStyle.DECORATED);
                    return;
                }
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		log.info("native library loaded");
		Parent root = FXMLLoader.load(getClass().getResource("/fxml/Dashboard.fxml"));
		log.info("load dashboard");
		stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
			@Override
			public void handle(WindowEvent event) {
				GlobalObjects.getInstance().shutdownScheduledExecutor(GlobalObjects.getInstance().grabber);
				GlobalObjects.getInstance().shutdownScheduledExecutor(GlobalObjects.getInstance().timer);
				GlobalObjects.getInstance().stopCamera(GlobalObjects.getInstance().videoCapture);
				if (UsersPreferences.getInstance().getPreference().getBoolean("rememberpassword", false) == false) {
					event.consume();
					TaskExecutor.getInstance()
							.logout(UsersPreferences.getInstance().getPreference().get("sessionToken", null));
					TaskExecutor.getInstance().getMyTask().setOnSucceeded(new EventHandler<WorkerStateEvent>() {
						@Override
						public void handle(WorkerStateEvent event) {
							UsersPreferences.getInstance().clearPreference();
							Platform.exit();
							System.exit(0);
						}
					});
				} else {
					Platform.exit();
					System.exit(0);
				}
			}
		});
		
		Scene scene = new Scene(root);
		stage.setScene(scene);
		stage.setTitle("Traffic Monitoring System");
		stage.show();
	}

	/**
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {
		launch(args);
	}

}
