package org.tms;

import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tms.cloud.CloudOperations;
import org.tms.db.localdb.RawDataDao;

import java.io.File;
import java.io.IOException;
import java.net.ConnectException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.*;

import static org.opencv.imgproc.Imgproc.resize;

public class DashboardController {
	
	final Logger log = LoggerFactory.getLogger(DashboardController.class);
    private ImageView bgsImageView = new ImageView();
    
    private volatile boolean loopBreaker = false;
    private boolean onSync = false;
    
    private int areaThreshold = 1700;
    private double imageThreshold = 16;
    private int history = 500;
    private int vehicleSizeThreshold = 20000;
    
    private VideoCapture capture;
    private Mat currentImage = new Mat();
    private VideoProcessor videoProcessor = new MixtureOfGaussianBackground(imageThreshold, history);
    private ImageProcessor imageProcessor = new ImageProcessor();
    private Mat foregroundImage;
    
    private volatile boolean isPaused = true;
    private boolean isStarted = false;
    
    private boolean crossingLine = false;
    private boolean crossingSpeedLine = false;
    
    private volatile String videoPath;
    private double distanceCS = 6.0;
    private double videoFPS;
    private int maxFPS;
    private long oneFrameDuration;
    
    private Point lineCount1;           //new Point(370,200);
    private volatile Point lineCount2;          //new Point(400,280);
    private Point lineSpeed1;           //new Point(460,200);
    private volatile Point lineSpeed2;          //new Point(490,270);
    
    private double timeInSec;
    private int minutes = 1;
    private int second = 0;
    private int whichFrame;
    
    private int cars = 0;
    private int vans = 0;
    private int lorries = 0;
    private int vehicles = 0;
    
    private double sumSpeedCar = 0;
    private double sumSpeedVan = 0;
    private double sumSpeedLorry = 0;
    private double sumSpeedVehicle = 0;

    private int divisorCar = 1;
    private int divisorVan = 1;
    private int divisorLorry = 1;
    private int divisorVehicle = 3;
    
    private int counter = 0;
    private int lastTSM = 0;
    private HashMap<Integer, Integer> speed = new HashMap<Integer, Integer>();
    
    private volatile boolean isProcessInRealTime = true;
    private long startTime;

    
    private Mat foregroundClone;
    
    private boolean mouseListenertIsActive;
    private boolean mouseListenertIsActive2;
    private boolean startDraw;
    private Mat copiedImage;
    
    private volatile boolean isBGSview = false;
    private Mat ImageBGS = new Mat();

    private ExecutorService executor
            = Executors.newSingleThreadExecutor();
    
    //DB operations

    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	DateFormat currentDay = new SimpleDateFormat("EEEE");
	int intervals = 5; // 5seconds
	TimeUnit unit = TimeUnit.SECONDS;
    
	@FXML
	private TextField areaTextField;
	@FXML
	private CheckBox interruptedCheckBox;
	@FXML
	private Button counterLineButton;
	@FXML
	private Button speedLineButton;
	@FXML
	private Button bgsViewButton;
	@FXML
    private Button syncButton;
	@FXML
	private Button chooseFileButton;
	@FXML
	private TextField filePathTextField;
	@FXML
	private ImageView videoContainerImageView;
	@FXML
	private Button signOutButton;
	@FXML
	private Button viewReportButton;
	@FXML
	private Button playPauseButton;
	@FXML
	private Button resetButton;
	@FXML
	private TextField quantityTextField;
	@FXML
	private TextField avgSpeedTextField;
	@FXML
	private TextField realTimeTextField;
	
    @FXML
    public void initialize() {
    	    	
        log.info("initialize window.");
        playPauseButton.setDisable(true);
        counterLineButton.setDisable(true);
        speedLineButton.setDisable(true);
        resetButton.setDisable(true);
        
        log.info("initialize database.");
//        db.createDB();
               
        
    }

	// Event Listener on Button[#counterLineButton].onAction
	@FXML
	public void handleCounterLineButtonAction(ActionEvent event) {
		counterLineButton.setDisable(true);
        speedLineButton.setDisable(true);
        mouseListenertIsActive = true;
        startDraw = false;
        videoContainerImageView.setOnMousePressed(new EventHandler<MouseEvent>() {
        	public void handle(MouseEvent me) {
        		if (mouseListenertIsActive) {
        			call(me.getButton(), new Point(me.getX(), me.getY()));
        		} else if (mouseListenertIsActive2) {
        			call2(me.getButton(), new Point(me.getX(), me.getY()));
        		}
        	} 

        });

        videoContainerImageView.setOnMouseMoved(new EventHandler<MouseEvent>() {
        	public void handle(MouseEvent me) {
        		if (mouseListenertIsActive) {
                    call(me.getButton(), new Point(me.getX(), me.getY()));
                } else if (mouseListenertIsActive2) {
                    call2(me.getButton(), new Point(me.getX(), me.getY()));
                }
        	}
        });
        	}
	// Event Listener on Button[#speedLineButton].onAction
	@FXML
	public void handleSpeedLineButtonAction(ActionEvent event) {
		counterLineButton.setDisable(true);
        speedLineButton.setDisable(true);
        mouseListenertIsActive2 = true;
        startDraw = false;
        videoContainerImageView.setOnMousePressed(new EventHandler<MouseEvent>() {
        	public void handle(MouseEvent me) {
        		if (mouseListenertIsActive) {
        			call(me.getButton(), new Point(me.getX(), me.getY()));
        		} else if (mouseListenertIsActive2) {
        			call2(me.getButton(), new Point(me.getX(), me.getY()));
        		}
        	} 

        });

        videoContainerImageView.setOnMouseMoved(new EventHandler<MouseEvent>() {
        	public void handle(MouseEvent me) {
        		if (mouseListenertIsActive) {
                    call(me.getButton(), new Point(me.getX(), me.getY()));
                } else if (mouseListenertIsActive2) {
                    call2(me.getButton(), new Point(me.getX(), me.getY()));
                }
        	}
        });
	}
	// Event Listener on Button[#bgsViewButton].onAction
	@FXML
	public void handleBgsViewButton(ActionEvent event) {
		
		bgsViewButton.setDisable(true);
        isBGSview = true;
        
		Stage bgsStage = new Stage();
		bgsStage.setTitle("Background Subtractor");
		
		
		Group root = new Group();
        Scene scene = new Scene(root);
//        scene.setFill(Color.BLACK);
        HBox box = new HBox();
       
        root.getChildren().add(box);       
        
		Mat localImage = new Mat(new Size(430, 240), CvType.CV_8UC3, new Scalar(255, 255, 255));        
        
        Image image = SwingFXUtils.toFXImage(imageProcessor.toBufferedImage(localImage), null);
        
        bgsImageView.setImage(image);
        box.getChildren().add(bgsImageView);
        
		// Set position of second window, related to primary window.
        bgsStage.setX(chooseFileButton.getScene().getWindow().getX() + 200);
        bgsStage.setY(chooseFileButton.getScene().getWindow().getY() + 100);
        bgsStage.setWidth(430);
        bgsStage.setHeight(240);
        bgsStage.setScene(scene); 
        bgsStage.show();
        
        bgsStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            public void handle(WindowEvent we) {
                bgsViewButton.setDisable(false);
                isBGSview = false;
            }
        }); 
        
	}
	// Event Listener on Button[#chooseFileButton].onAction
	@FXML
	public void handleChooseFileButtonAction(ActionEvent event) {
		
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Open Resource File");
		fileChooser.getExtensionFilters().addAll(
				new ExtensionFilter("Video Files", "*.avi", "*.mp4", "*.mpg", "*.mov"));
		fileChooser.setInitialDirectory(new File(System.getProperty("user.home"), "Desktop"));
		File selectedFile = fileChooser.showOpenDialog(chooseFileButton.getScene().getWindow());

		if (selectedFile != null) {

			videoPath = selectedFile.getPath();
			filePathTextField.setText(videoPath);
			capture = new VideoCapture(videoPath);
			capture.read(currentImage);
			videoFPS = capture.get(Videoio.CAP_PROP_FPS);
			log.debug("videoFPS: " + videoFPS);
			resize(currentImage, currentImage, new Size(640, 360));
			updateView(currentImage);
			if (videoPath != null) {
				counterLineButton.setDisable(false);
				speedLineButton.setDisable(false);

				resetButton.setDisable(false);
				playPauseButton.setDisable(false);
			}     
		}		

	}
	// Event Listener on JFXButton[#signOutButton].onAction
	@FXML
	public void handleSignOutButtonAction(ActionEvent event) {
		// TODO Autogenerated
	}
	// Event Listener on Button[#viewReportButton].onAction
	@FXML
	public void handleViewReportButtonAction(ActionEvent event) throws IOException {
		Parent root = FXMLLoader.load(getClass().getResource("/fxml/Reports.fxml"));
		Scene scene = new Scene(root);
		Stage stage = new Stage();
		stage.setScene(scene);
		stage.setTitle("Reports");
//		if (UsersPreferences.getInstance().getPreference().get("sessionToken", null) != null)
		log.info("show reports.");
		stage.show();
	}
	// Event Listener on Button[#playPauseButton].onAction
	@FXML
	public void handlePlayPauseButtonAction(ActionEvent event) {
				
		if (lineSpeed2 == null && lineCount2 == null) {
			Alert alert = new Alert(AlertType.WARNING);
			alert.setTitle("Invalid Input");
			alert.setHeaderText("Draw sensor lines");
			alert.setContentText("Please draw counter line and speed line!");

			alert.showAndWait();
		} else {
			if (!isPaused) {
	            isPaused = true;
	            playPauseButton.setText("Play");

	            chooseFileButton.setDisable(false);

	            counterLineButton.setDisable(false);
	            speedLineButton.setDisable(false);

	        } else {
	            isPaused = false;
	            playPauseButton.setText("Pause");

	            maxWaitingFPS();

	            chooseFileButton.setDisable(true);

	            counterLineButton.setDisable(true);
	            speedLineButton.setDisable(true);

	        }
			
			if (!isStarted) {
				log.info("mainLoop");
				loopBreaker = false;
		        Thread mainLoop = new Thread(new Loop());
		        mainLoop.start();
		        isStarted = true;
		        resetButton.setDisable(false);
		        
			}
			
	        
		}
		
	}
	// Event Listener on Button[#resetButton].onAction
	@FXML
	public void handleResetButtonAction(ActionEvent event) {
		Alert alert = new Alert(AlertType.CONFIRMATION);
		alert.setTitle("Reset Video");
		alert.setHeaderText("Are you sure you want to reset video?");
		alert.setContentText(videoPath);

		Optional<ButtonType> result = alert.showAndWait();
		if (result.get() == ButtonType.OK){
			resetVideo();
			log.info("Video has been reset.");
			chooseFileButton.setDisable(false);
			
		} else {
		    // ... user chose CANCEL or closed the dialog
		}
	}
	
	private void resetVideo() {		
		if(isPaused)
			isPaused = false;
		loopBreaker = true;
//		capture.set(Videoio.CV_CAP_PROP_POS_FRAMES, 0);
		//capture.release();
		capture = new VideoCapture(videoPath);
        capture.read(currentImage);
        videoFPS = capture.get(Videoio.CAP_PROP_FPS);
        resize(currentImage, currentImage, new Size(640, 360));
        updateView(currentImage);
        
        realTimeTextField.setText("0 sec");
        isPaused = true;
        playPauseButton.setText("Play");
        playPauseButton.setDisable(false);
        videoProcessor = new MixtureOfGaussianBackground(imageThreshold, history);

        resetButton.setDisable(true);

        counterLineButton.setDisable(false);
        speedLineButton.setDisable(false);
        lineCount1 = null;
        lineCount2 = null;
        lineSpeed1 = null;
        lineSpeed2 = null;

        minutes = 1;
        second = 0;
        whichFrame = 0;
        timeInSec = 0;

        quantityTextField.setText("0");
        avgSpeedTextField.setText("0");
        
        sumSpeedVehicle = 0;

        divisorVehicle = 1;

        counter = 0;
        lastTSM = 0;
        
        isStarted = false;       
	}
	
    private void updateView(Mat matImage) {
    	Image image = SwingFXUtils.toFXImage(imageProcessor.toBufferedImage(matImage), null);
    	Platform.runLater(()->{
    		videoContainerImageView.setImage(image);
        });
    	
    }
    
    public class Loop implements Runnable {

        @Override
        public void run() {
            maxWaitingFPS();
            videoProcessor = new MixtureOfGaussianBackground(imageThreshold, history);
            if (capture.isOpened()) {
                while (true) {
                    if (!isPaused) {
                    	                  
                        capture.read(currentImage);
                        if (!currentImage.empty()) {
                            resize(currentImage, currentImage, new Size(640, 360));
                            foregroundImage = currentImage.clone();
                            foregroundImage = videoProcessor.process(foregroundImage);

                            foregroundClone = foregroundImage.clone();
                            Imgproc.bilateralFilter(foregroundClone, foregroundImage, 2, 1600, 400);
                            
                            if (isBGSview) {
                                resize(foregroundImage, ImageBGS, new Size(430, 240));
                                Image image = SwingFXUtils.toFXImage(imageProcessor.toBufferedImage(ImageBGS), null);
                                
                                bgsImageView.setImage(image);
                            }

                            CountVehicles countVehicles = new CountVehicles(areaThreshold, vehicleSizeThreshold, lineCount1, lineCount2, lineSpeed1, lineSpeed2, crossingLine, crossingSpeedLine);
                            
                            countVehicles.findAndDrawContours(currentImage, foregroundImage);

                            try {
                                count(countVehicles);
                                speedMeasure(countVehicles);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            videoRealTime();

//                            if (isProcessInRealTime) {
                            long time = System.currentTimeMillis() - startTime;                           
                            if (time < oneFrameDuration) {
                                try {
                                    Thread.sleep(oneFrameDuration - time);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                            else {
                            	try {
                                    Thread.sleep(30);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
//                            }
                            updateView(currentImage);
                            startTime = System.currentTimeMillis();

                            if (loopBreaker)
                                break;

                        } else {

                            playPauseButton.setDisable(true);

//                            saveButton.setDisable(false);
                            chooseFileButton.setDisable(false);
                            Platform.runLater(()->{
                            	playPauseButton.setText("Play");
                            });
                            
                            minutes = 1;
                            second = 0;
                            whichFrame = 0;
                            isStarted = false;
                            log.info("The video has finished!");
                            break;
                        }
                    }
                }
            }
        }
    }
    
    public void maxWaitingFPS() {
        double time = (distanceCS / 3);
        double max = videoFPS * time;
        maxFPS = (int) max;

        oneFrameDuration = 1000 / (long) videoFPS;
    }
    
    private double videoRealTime() {
        whichFrame++;
        timeInSec = whichFrame / videoFPS;
        setTimeInMinutes();
        return timeInSec;
    }
    
    private void setTimeInMinutes() {
        if (timeInSec < 60) {
        	//threading issues with JavaFX
        	Platform.runLater(()->{
        		realTimeTextField.setText((int) timeInSec + " sec");
        	});
        	
        } else if (second < 60) {
            second = (int) timeInSec - (60 * minutes);
            Platform.runLater(()->{
            	realTimeTextField.setText(minutes + " min " + second + " sec");
            });
        } else {
            second = 0;
            minutes++;
        }
    }
    
    public synchronized void count(CountVehicles countVehicles) {
        if (countVehicles.isVehicleToAdd()) {
            counter++;
            lastTSM++;
            speed.put(lastTSM, 0);
           
            quantityTextField.setText(Integer.toString(counter));
//            log.debug("count: " + counter);

        }
        crossingLine = countVehicles.isCrossingLine();
    }


    public synchronized void speedMeasure(CountVehicles countVehicles) {
        if (!speed.isEmpty()) {
            int firstTSM = speed.entrySet().iterator().next().getKey();
            if (countVehicles.isToSpeedMeasure()) {
                for (int i = firstTSM; i <= lastTSM; i++) {
                    if (speed.containsKey(i)) {
                        speed.put(i, (speed.get(i) + 1));
                    }
                }

                double currentSpeed = computeSpeed(speed.get(firstTSM));

                sumSpeedVehicle += currentSpeed;
                double avgSpeed = sumSpeedVehicle / divisorVehicle;
                divisorVehicle++;
                
                dbUpdateCounters(1, avgSpeed);
                Platform.runLater(()->{
                	avgSpeedTextField.setText(String.format("%.2f", avgSpeed));
                });
                
                speed.remove(firstTSM);

            } else {
                for (int i = firstTSM; i <= lastTSM; i++) {
                    if (speed.containsKey(i)) {
                        int currentFPS = speed.get(i);
                        speed.put(i, (currentFPS + 1));
                        if (currentFPS > maxFPS) {
                            speed.remove(i);

                        }
                    }
                }
            }
        }
        crossingSpeedLine = countVehicles.isCrossingSpeedLine();
    }
    
    public double computeSpeed(int speedPFS) {
        double duration = speedPFS / videoFPS;
        double v = (distanceCS / duration) * 3.6;
        return v;
    }
    
    public void call(MouseButton mb, Point point) {
        if (mb == MouseButton.PRIMARY) {
            if (!startDraw) {
                lineCount1 = point;
                startDraw = true;
            } else {
                lineCount2 = point;
                startDraw = false;
                mouseListenertIsActive = false;
                counterLineButton.setDisable(false);
                speedLineButton.setDisable(false);
                
                videoContainerImageView.setOnMousePressed(null);
                videoContainerImageView.setOnMouseMoved(null);
            }

        } else if (mb == MouseButton.NONE && startDraw) {
            copiedImage = currentImage.clone();
            Imgproc.line(copiedImage, lineCount1, point, new Scalar(0, 0, 255), 1);
            if (lineSpeed1 != null && lineSpeed2 != null)
                Imgproc.line(copiedImage, lineSpeed1, lineSpeed2, new Scalar(0, 255, 0), 1);
            updateView(copiedImage);
        }
    }

    private void call2(MouseButton mb, Point point) {
        if (mb == MouseButton.PRIMARY) {
            if (!startDraw) {
                lineSpeed1 = point;
                startDraw = true;
            } else {
                lineSpeed2 = point;
                startDraw = false;
                mouseListenertIsActive2 = false;
                counterLineButton.setDisable(false);
                speedLineButton.setDisable(false);
                videoContainerImageView.setOnMousePressed(null);
                videoContainerImageView.setOnMouseMoved(null);
            }

        } else if (mb == MouseButton.NONE && startDraw) {
            copiedImage = currentImage.clone();
            Imgproc.line(copiedImage, lineSpeed1, point, new Scalar(0, 255, 0), 1);
            if (lineCount1 != null && lineCount2 != null)
                Imgproc.line(copiedImage, lineCount1, lineCount2, new Scalar(0, 0, 255), 1);
            updateView(copiedImage);
        }
    }
    
    private void dbUpdateCounters(int count, double avgSpeed) {
		Runnable dbUpdate = new Runnable() {
			@Override
			public void run() {
                RawDataDao rawDataDao = null;
				Date date = new Date();
                try {
                    rawDataDao = new RawDataDao();
                } catch (ConnectException e) {
                    e.printStackTrace();
                }

                String currentDateTime = dateFormat.format(date);
				String day = currentDay.format(date);
				String facilityType = "";
				
				if (interruptedCheckBox.isSelected())
					facilityType = "Interrupted";
				else
					facilityType = "Uninterrupted";
				
				log.info("updating database counters");
				rawDataDao.insert(count, avgSpeed, currentDateTime, areaTextField.getText().toUpperCase(), facilityType);
			}
		};
		
		ExecutorService executor = Executors.newFixedThreadPool(5);
		executor.submit(dbUpdate);
		executor.shutdown();
		
	}

	@FXML
    public void handleSyncButtonAction() {

        Alert alert = new Alert(AlertType.CONFIRMATION);
        if (!onSync){

            alert.setTitle("Sync");
            alert.setHeaderText("Proceed to sync cloud data?");
            alert.setContentText("This may take a while");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == ButtonType.OK) {
                log.info("start syncing...");
                onSync = true;
                Future<String> future = processSyncCloud();
                String syncStatus = null;
                try {
                    syncStatus = future.get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
                log.debug("syncStatus: " + syncStatus);
                if (syncStatus.equals("DB_SYNC_COMPLETE")) {
                    log.info("Sync successful");
                    onSync = false;
                    Alert alertSyncInfo = new Alert(AlertType.INFORMATION);
                    alertSyncInfo.setTitle("Sync Status");
                    alertSyncInfo.setHeaderText("Sync Successful");
                    alertSyncInfo.setContentText("Everything is up to date");
                    alertSyncInfo.showAndWait();

                }
                else {

                    log.error("Sync FAILURE!!!");
                    onSync = false;

                    Alert alertSyncFail = new Alert(AlertType.ERROR);
                    alertSyncFail.setTitle("Sync Status");
                    alertSyncFail.setHeaderText("Sync has failed!");
                    alertSyncFail.setContentText("Please check your network or database connection");
                    alertSyncFail.showAndWait();
                }
//            syncButton.setDisable(true);
            }
        }
        else {
            alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("Information");
            alert.setHeaderText(null);
            alert.setContentText("Already syncing. Please wait...");

            alert.showAndWait();
        }

    }

    public Future<String> processSyncCloud() {
        return executor.submit(() -> {
            CloudOperations cloudOperations = new CloudOperations();
            return cloudOperations.syncCloud();
        });
    }
      
}
