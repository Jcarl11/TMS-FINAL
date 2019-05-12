package org.tms.utilities;

import org.tms.entities.Report1Entity;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXSnackbar;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.json.JSONObject;
import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;

public class GlobalObjects {
	private GlobalObjects() {
	}

	private static GlobalObjects instance = null;

	public static GlobalObjects getInstance() {
		if (instance == null)
			instance = new GlobalObjects();
		return instance;
	}

	public ScheduledExecutorService timer, grabber, computer;
	public VideoCapture videoCapture;
	public static String APP_ID = "nGEoEK8re6EaVhzGCv7CMx4Jan1hELA1iu1wRxSd";
	public static String REST_API_KEY = "c2tBFbHdVBaJkbqwXgQle1fziX3wXKl02cdG1wSa";
	public static String IRREVOCABLE_SESSION = "1";
	public static String URL_FILE = "https://project.back4app.io/files/";
	public static String URL = "https://project.back4app.io/classes/";
	public static String URL_BASE = "https://project.back4app.io/";

	public Image mat2Image(Mat frame) {
		try {
			return SwingFXUtils.toFXImage(matToBufferedImage(frame), null);
		} catch (Exception e) {
			System.err.println("Cannot convert the Mat obejct: " + e);
			return null;
		}
	}

	public BufferedImage matToBufferedImage(Mat original) {
		BufferedImage image = null;
		int width = original.width(), height = original.height(), channels = original.channels();
		byte[] sourcePixels = new byte[width * height * channels];
		original.get(0, 0, sourcePixels);

		if (original.channels() > 1) {
			image = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
		} else {
			image = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
		}
		final byte[] targetPixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
		System.arraycopy(sourcePixels, 0, targetPixels, 0, sourcePixels.length);

		return image;
	}

	public void stopCamera(VideoCapture videoCapture) {
		if (videoCapture != null) {
			if (videoCapture.isOpened())
				videoCapture.release();
		}

	}

	public void shutdownScheduledExecutor(ScheduledExecutorService timer) {
		if (timer != null && !timer.isShutdown()) {
			try {
				timer.shutdown();
				timer.awaitTermination(33, TimeUnit.MILLISECONDS);
			} catch (InterruptedException e) {
				System.err.println(e);
			}
		}
	}

	public String showChooserDialog(String... acceptableFileTypes) {
		String path = new String();
		JFileChooser chooser = new JFileChooser();
		chooser.setMultiSelectionEnabled(false);
		chooser.setFileFilter(new FileNameExtensionFilter("Files", acceptableFileTypes));
		chooser.setCurrentDirectory(new File("D:\\JOEY\\Capstone"));
		chooser.showOpenDialog(null);
		File selectedFile = chooser.getSelectedFile();
		if (selectedFile != null) {
			path = selectedFile.getAbsolutePath();
		}
		return path;
	}

	public void showMessage(String message, Pane container) {
		JFXSnackbar snackbar = new JFXSnackbar(container);
		snackbar.show(message.trim(), 3000);
	}

	public void openNewWindow(String fileName, String title, StageStyle stageStyle) {
		try {
			Parent root = FXMLLoader.load(getClass().getResource("/fxml/" + fileName));
			Stage stage = new Stage();
			stage.initModality(Modality.APPLICATION_MODAL);
			stage.initStyle(stageStyle);
			stage.setTitle(title);
			stage.setScene(new Scene(root));
			stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
				@Override
				public void handle(WindowEvent event) {
					if (fileName == "Login.fxml") {
						Platform.exit();
						System.exit(0);
					}
				}
			});
			stage.show();
		} catch (IOException iOException) {
			iOException.printStackTrace();
		}
	}

	public JSONObject buildSignUpUser(String username, String password, String email) {
		JSONObject json = new JSONObject();
		json.put("username", username);
		json.put("password", password);
		json.put("email", email);
		return json;
	}

	public void bindBtnNProgress(Object btn, ProgressIndicator progressIndicator, ReadOnlyBooleanProperty property) {
		if (btn instanceof JFXButton) {
			JFXButton button = (JFXButton) btn;
			button.disableProperty().unbind();
			progressIndicator.visibleProperty().unbind();
			button.disableProperty().bind(property);
			progressIndicator.visibleProperty().bind(property);
		} else if (btn instanceof Button) {
			Button button = (Button) btn;
			button.disableProperty().unbind();
			progressIndicator.visibleProperty().unbind();
			button.disableProperty().bind(property);
			progressIndicator.visibleProperty().bind(property);
		} else if (btn instanceof Hyperlink) {
			Hyperlink button = (Hyperlink) btn;
			button.disableProperty().unbind();
			progressIndicator.visibleProperty().unbind();
			button.disableProperty().bind(property);
			progressIndicator.visibleProperty().bind(property);
		}
	}

	public String categorizeLOS(String value) {
		String los = null;
		double valueConverted = Double.valueOf(value);
		if (valueConverted >= 0 && valueConverted <= 10)
			los = "A";
		else if (valueConverted >= 11 && valueConverted <= 20)
			los = "B";
		else if (valueConverted >= 21 && valueConverted <= 30)
			los = "C";
		else if (valueConverted >= 31 && valueConverted <= 40)
			los = "D";
		else if (valueConverted >= 41 && valueConverted <= 50)
			los = "E";
		else if (valueConverted >= 51)
			los = "F";
		else
			los = "Unmeasurable";
		return los;
	}

	public JSONObject buildParameter(String date, String facility, String facilityType, String LOS) {
		JSONObject json = new JSONObject();
		JSONObject json2 = new JSONObject();
		json2.put("method", "POST");
		json2.put("path", "/classes/Data");
		json2.put("body", json);
		json.put("TIMESTAMP", dateFormat(date));
		json.put("Facility", facility);
		json.put("FacilityType", facilityType);
		json.put("LOS", LOS);
		return json2;
	}

	public JSONObject buildParameter2(Report1Entity data) {
		JSONObject json = new JSONObject();
		JSONObject json2 = new JSONObject();
		json2.put("method", "POST");
		json2.put("path", "/classes/Data2");
		json2.put("body", json);
		json.put("TIMESTAMP", dateFormat(data.getTimestamp()));
		json.put("AVERAGE", data.getAverage());
		json.put("DAY", data.getDay());
		return json2;
	}

	public JSONObject dateFormat(String dateString) {
		// String converted = convertDateToIso(dateString);
		JSONObject json = new JSONObject();
		json.put("__type", "Date");
		json.put("iso", dateString);
		return json;
	}

	public String convertDateToIso(String dateString) {
		DateFormat simpleDateFormat1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		DateFormat simpleDateFormat2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:dd'Z'");
		Date date = new Date();
		try {
			date = simpleDateFormat1.parse(dateString);
		} catch (ParseException ex) {
			Logger.getLogger(GlobalObjects.class.getName()).log(Level.SEVERE, null, ex);
		}
		return simpleDateFormat2.format(date);
	}
}
