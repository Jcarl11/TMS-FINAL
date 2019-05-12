package org.tms;

import com.jfoenix.controls.JFXProgressBar;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tms.cloud.CloudOperations;
import org.tms.db.localdb.RawDataDAO;

import java.io.File;
import java.io.IOException;
import java.net.ConnectException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

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
    private int whichFrame;

    private double sumSpeedVehicle = 0;


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
    
    //video frame 2
    private VideoCapture capture1;
    private VideoProcessor videoProcessor1 = new MixtureOfGaussianBackground(imageThreshold, history);
    private volatile String videoPath1;
    private double videoFPS1;
    private Mat currentImage1 = new Mat();
    private Mat foregroundImage1;
    private Mat foregroundClone1;
    private boolean startDraw1;

    private Point lineCount11;           //new Point(370,200);
    private volatile Point lineCount12;          //new Point(400,280);
    private Point lineSpeed11;           //new Point(460,200);
    private volatile Point lineSpeed12;          //new Point(490,270);
    private volatile boolean isPaused1 = true;
    private boolean isStarted1 = false;
    private Mat copiedImage1;

    private int maxFPS1;
    private long oneFrameDuration1;

    private double timeInSec1;
    private int whichFrame1;
    private double sumSpeedVehicle1 = 0;

    private boolean crossingLine1 = false;
    private boolean crossingSpeedLine1 = false;

    private long startTime1;
    private int counter1 = 0;
    private int lastTSM1 = 0;
    private HashMap<Integer, Integer> speed1 = new HashMap<Integer, Integer>();

    private int divisorVehicle1 = 3;

    private volatile boolean loopBreaker1 = false;

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
    private JFXProgressBar syncCloudProgressBar;
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
    private Button chooseFileButton1;
    @FXML
    private TextField filePathTextField1;
	@FXML
    private Button playPauseButton1;
    @FXML
    private Button resetButton1;
    @FXML
    private TextField quantityTextField1;
    @FXML
    private TextField avgSpeedTextField1;
    @FXML
    private Button counterLineButton1;
    @FXML
    private Button speedLineButton1;
    @FXML
    private ImageView videoContainerImageView1;
    @FXML
    private TextField areaTextField1;
    @FXML
    private CheckBox interruptedCheckBox1;

	
    @FXML
    public void initialize() {
    	    	
        log.info("initialize window.");
        playPauseButton.setDisable(true);
        counterLineButton.setDisable(true);
        speedLineButton.setDisable(true);
        resetButton.setDisable(true);
        playPauseButton1.setDisable(true);
        counterLineButton1.setDisable(true);
        speedLineButton1.setDisable(true);
        resetButton1.setDisable(true);
        syncCloudProgressBar.setVisible(false);
        
//        log.info("initialize database.");
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
			updateView(currentImage, 1);
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
		        Thread mainLoop = new Thread(new Loop(1));
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
			resetVideo(1);
			log.info("Video has been reset.");
			chooseFileButton.setDisable(false);
			
		} else {
		    // ... user chose CANCEL or closed the dialog
		}
	}

    @FXML
    public void handleChooseFileButtonAction1(ActionEvent event) {

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Resource File");
        fileChooser.getExtensionFilters().addAll(
                new ExtensionFilter("Video Files", "*.avi", "*.mp4", "*.mpg", "*.mov"));
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home"), "Desktop"));
        File selectedFile = fileChooser.showOpenDialog(chooseFileButton.getScene().getWindow());

        if (selectedFile != null) {

            videoPath1 = selectedFile.getPath();
            filePathTextField1.setText(videoPath1);
            capture1 = new VideoCapture(videoPath1);
            capture1.read(currentImage1);
            videoFPS1 = capture1.get(Videoio.CAP_PROP_FPS);
            log.debug("videoFPS1: " + videoFPS1);
            resize(currentImage1, currentImage1, new Size(640, 360));
            updateView(currentImage1, 2);
            if (videoPath1 != null) {
                counterLineButton1.setDisable(false);
                speedLineButton1.setDisable(false);

                resetButton1.setDisable(false);
                playPauseButton1.setDisable(false);
            }
        }

    }

    @FXML
    public void handleCounterLineButtonAction1(ActionEvent event) {
        counterLineButton1.setDisable(true);
        speedLineButton1.setDisable(true);
        mouseListenertIsActive = true;
        startDraw1 = false;
        videoContainerImageView1.setOnMousePressed(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent me) {
                if (mouseListenertIsActive) {
                    call1(me.getButton(), new Point(me.getX(), me.getY()));
                } else if (mouseListenertIsActive2) {
                    call12(me.getButton(), new Point(me.getX(), me.getY()));
                }
            }

        });

        videoContainerImageView1.setOnMouseMoved(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent me) {
                if (mouseListenertIsActive) {
                    call1(me.getButton(), new Point(me.getX(), me.getY()));
                } else if (mouseListenertIsActive2) {
                    call12(me.getButton(), new Point(me.getX(), me.getY()));
                }
            }
        });
    }

    @FXML
    public void handleSpeedLineButtonAction1(ActionEvent event) {
        counterLineButton1.setDisable(true);
        speedLineButton1.setDisable(true);
        mouseListenertIsActive2 = true;
        startDraw1 = false;
        videoContainerImageView1.setOnMousePressed(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent me) {
                if (mouseListenertIsActive) {
                    call1(me.getButton(), new Point(me.getX(), me.getY()));
                } else if (mouseListenertIsActive2) {
                    call12(me.getButton(), new Point(me.getX(), me.getY()));
                }
            }

        });

        videoContainerImageView1.setOnMouseMoved(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent me) {
                if (mouseListenertIsActive) {
                    call1(me.getButton(), new Point(me.getX(), me.getY()));
                } else if (mouseListenertIsActive2) {
                    call12(me.getButton(), new Point(me.getX(), me.getY()));
                }
            }
        });
    }

    @FXML
    public void handlePlayPauseButtonAction1(ActionEvent event) {

        if (lineSpeed12 == null && lineCount12 == null) {
            Alert alert = new Alert(AlertType.WARNING);
            alert.setTitle("Invalid Input");
            alert.setHeaderText("Draw sensor lines");
            alert.setContentText("Please draw counter line and speed line!");

            alert.showAndWait();
        } else {
            if (!isPaused1) {
                isPaused1 = true;
                playPauseButton1.setText("Play");

                chooseFileButton1.setDisable(false);

                counterLineButton1.setDisable(false);
                speedLineButton1.setDisable(false);

            } else {
                isPaused1 = false;
                playPauseButton1.setText("Pause");

                maxWaitingFPS1();

                chooseFileButton1.setDisable(true);

                counterLineButton1.setDisable(true);
                speedLineButton1.setDisable(true);

            }

            if (!isStarted1) {
                log.info("mainLoop");
                loopBreaker1 = false;
                Thread mainLoop = new Thread(new Loop(2));
                mainLoop.start();
                isStarted1 = true;
                resetButton1.setDisable(false);

            }
        }
    }

    @FXML
    public void handleResetButtonAction1(ActionEvent event) {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Reset Video");
        alert.setHeaderText("Are you sure you want to reset video?");
        alert.setContentText(videoPath);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK){
            resetVideo(2);
            log.info("Video has been reset.");
            chooseFileButton.setDisable(false);

        } else {
            // ... user chose CANCEL or closed the dialog
        }
    }
	
	private void resetVideo(int videoId) {
        if (videoId == 1) {
            if(isPaused)
                isPaused = false;
            loopBreaker = true;
//		capture.set(Videoio.CV_CAP_PROP_POS_FRAMES, 0);
            //capture.release();
            capture = new VideoCapture(videoPath);
            capture.read(currentImage);
            videoFPS = capture.get(Videoio.CAP_PROP_FPS);
            resize(currentImage, currentImage, new Size(640, 360));
            updateView(currentImage, 1);

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

            whichFrame = 0;
            timeInSec = 0;

            quantityTextField.setText("0");
            avgSpeedTextField.setText("0");

            sumSpeedVehicle = 0;

            divisorVehicle = 1;

            counter = 0;
            lastTSM = 0;

            isStarted = false;
        } else if(videoId == 2) {
            if(isPaused1)
                isPaused1 = false;
            loopBreaker1 = true;

            capture1 = new VideoCapture(videoPath1);
            capture1.read(currentImage1);
            videoFPS1 = capture1.get(Videoio.CAP_PROP_FPS);
            resize(currentImage1, currentImage1, new Size(640, 360));
            updateView(currentImage1, 2);

            isPaused1 = true;
            playPauseButton1.setText("Play");
            playPauseButton1.setDisable(false);
            videoProcessor1 = new MixtureOfGaussianBackground(imageThreshold, history);

            resetButton1.setDisable(true);

            counterLineButton1.setDisable(false);
            speedLineButton1.setDisable(false);
            lineCount11 = null;
            lineCount12 = null;
            lineSpeed11 = null;
            lineSpeed12 = null;

            whichFrame1 = 0;
            timeInSec1 = 0;

            quantityTextField1.setText("0");
            avgSpeedTextField1.setText("0");

            sumSpeedVehicle1 = 0;

            divisorVehicle1 = 1;

            counter1 = 0;
            lastTSM1 = 0;

            isStarted1 = false;
        }

	}
	
    private void updateView(Mat matImage, int videoId) {
    	Image image = SwingFXUtils.toFXImage(imageProcessor.toBufferedImage(matImage), null);
    	Platform.runLater(()->{
    	    if (videoId == 1 )
    	        videoContainerImageView.setImage(image);
    	    else if (videoId == 2)
    	        videoContainerImageView1.setImage(image);
        });
    	
    }
    
    public class Loop implements Runnable {

        private int videoId;
        Loop(int videoId) {
            this.videoId = videoId;
        }
        @Override
        public void run() {
            if (videoId == 1) {
                maxWaitingFPS();
                log.debug("videoId: " + this.videoId);
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
                                    count(countVehicles, videoId);
                                    speedMeasure(countVehicles, videoId);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                                videoRealTime(videoId);

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
                                updateView(currentImage, videoId);
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

                                whichFrame = 0;
                                isStarted = false;
                                log.info("The video has finished!");
                                break;
                            }
                        }
                    }
                }
            } else if (videoId == 2) {
                maxWaitingFPS1();
                log.debug("videoId: " + this.videoId);
                videoProcessor1 = new MixtureOfGaussianBackground(imageThreshold, history);
                if (capture1.isOpened()) {
                    while (true) {
                        if (!isPaused1) {

                            capture1.read(currentImage1);
                            if (!currentImage1.empty()) {
                                resize(currentImage1, currentImage1, new Size(640, 360));
                                foregroundImage1 = currentImage1.clone();
                                foregroundImage1 = videoProcessor1.process(foregroundImage1);

                                foregroundClone1 = foregroundImage1.clone();
                                Imgproc.bilateralFilter(foregroundClone1, foregroundImage1, 2, 1600, 400);

                                CountVehicles countVehicles = new CountVehicles(areaThreshold, vehicleSizeThreshold, lineCount11, lineCount12, lineSpeed11, lineSpeed12, crossingLine1, crossingSpeedLine1);

                                countVehicles.findAndDrawContours(currentImage1, foregroundImage1);

                                try {
                                    count(countVehicles, videoId);
                                    speedMeasure(countVehicles, videoId);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                                videoRealTime(videoId);

                                long time = System.currentTimeMillis() - startTime1;
                                if (time < oneFrameDuration1) {
                                    try {
                                        Thread.sleep(oneFrameDuration1 - time);
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
                                updateView(currentImage1, videoId);
                                startTime1 = System.currentTimeMillis();

                                if (loopBreaker1)
                                    break;

                            } else {

                                playPauseButton1.setDisable(true);

                                chooseFileButton1.setDisable(false);
                                Platform.runLater(()->{
                                    playPauseButton1.setText("Play");
                                });

                                whichFrame1 = 0;
                                isStarted1 = false;
                                log.info("The video has finished!");
                                break;
                            }
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

    public void maxWaitingFPS1() {
        double time = (distanceCS / 3);
        double max = videoFPS1 * time;
        maxFPS1 = (int) max;

        oneFrameDuration1 = 1000 / (long) videoFPS1;
    }
    
    private double videoRealTime(int videoId) {
        if (videoId == 1) {
            whichFrame++;
            timeInSec = whichFrame / videoFPS;
            return timeInSec;

        } else if (videoId == 2) {
            whichFrame1++;
            timeInSec1 = whichFrame1 / videoFPS1;
            return timeInSec1;
        }

        return 0;

    }
    
    public synchronized void count(CountVehicles countVehicles, int videoId) {
        if (videoId == 1) {
            if (countVehicles.isVehicleToAdd()) {
                counter++;
                lastTSM++;
                speed.put(lastTSM, 0);

                quantityTextField.setText(Integer.toString(counter));
//            log.debug("count: " + counter);

            }
            crossingLine = countVehicles.isCrossingLine();
        } else if (videoId == 2) {
            if (countVehicles.isVehicleToAdd()) {
                counter1++;
                lastTSM1++;
                speed1.put(lastTSM1, 0);

                quantityTextField1.setText(Integer.toString(counter1));
//            log.debug("count: " + counter);

            }
            crossingLine1 = countVehicles.isCrossingLine();
        }

    }


    public synchronized void speedMeasure(CountVehicles countVehicles, int videoId) {
        if (videoId == 1) {
            if (!speed.isEmpty()) {
                int firstTSM = speed.entrySet().iterator().next().getKey();
                if (countVehicles.isToSpeedMeasure()) {
                    for (int i = firstTSM; i <= lastTSM; i++) {
                        if (speed.containsKey(i)) {
                            speed.put(i, (speed.get(i) + 1));
                        }
                    }

                    double currentSpeed = computeSpeed(speed.get(firstTSM), videoId);

                    sumSpeedVehicle += currentSpeed;
                    double avgSpeed = sumSpeedVehicle / divisorVehicle;
                    divisorVehicle++;

                    dbUpdateCounters(1, currentSpeed, videoId);
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
        } else if (videoId == 2) {
            if (!speed1.isEmpty()) {
                int firstTSM = speed1.entrySet().iterator().next().getKey();
                if (countVehicles.isToSpeedMeasure()) {
                    for (int i = firstTSM; i <= lastTSM1; i++) {
                        if (speed1.containsKey(i)) {
                            speed1.put(i, (speed1.get(i) + 1));
                        }
                    }

                    double currentSpeed = computeSpeed(speed1.get(firstTSM), videoId);
                    sumSpeedVehicle1 += currentSpeed;
                    double avgSpeed = sumSpeedVehicle1 / divisorVehicle1;
                    divisorVehicle1++;

                    dbUpdateCounters(1, currentSpeed, videoId);
                    Platform.runLater(()->{
                        avgSpeedTextField1.setText(String.format("%.2f", avgSpeed));
                    });

                    speed1.remove(firstTSM);

                } else {
                    for (int i = firstTSM; i <= lastTSM1; i++) {
                        if (speed1.containsKey(i)) {
                            int currentFPS = speed1.get(i);
                            speed1.put(i, (currentFPS + 1));
                            if (currentFPS > maxFPS1) {
                                speed1.remove(i);

                            }
                        }
                    }
                }
            }
            crossingSpeedLine1 = countVehicles.isCrossingSpeedLine();
        }

    }
    
    public double computeSpeed(int speedPFS, int videoId) {
        if (videoId == 1) {
            double duration = speedPFS / videoFPS;
            double v = (distanceCS / duration) * 3.6;
            return v;
        } else if (videoId == 2) {
            double duration = speedPFS / videoFPS1;
            double v = (distanceCS / duration) * 3.6;
            return v;
        }
        return 0;
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
            updateView(copiedImage, 1);
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
            updateView(copiedImage, 1);
        }
    }

    public void call1(MouseButton mb, Point point) {
        if (mb == MouseButton.PRIMARY) {
            if (!startDraw1) {
                lineCount11 = point;
                startDraw1 = true;
            } else {
                lineCount12 = point;
                startDraw1 = false;
                mouseListenertIsActive = false;
                counterLineButton1.setDisable(false);
                speedLineButton1.setDisable(false);

                videoContainerImageView1.setOnMousePressed(null);
                videoContainerImageView1.setOnMouseMoved(null);
            }

        } else if (mb == MouseButton.NONE && startDraw1) {
            copiedImage1 = currentImage1.clone();
            Imgproc.line(copiedImage1, lineCount11, point, new Scalar(0, 0, 255), 1);
            if (lineSpeed11 != null && lineSpeed12 != null)
                Imgproc.line(copiedImage1, lineSpeed11, lineSpeed12, new Scalar(0, 255, 0), 1);
            updateView(copiedImage1, 2);
        }
    }

    private void call12(MouseButton mb, Point point) {
        if (mb == MouseButton.PRIMARY) {
            if (!startDraw1) {
                lineSpeed11 = point;
                startDraw1 = true;
            } else {
                lineSpeed12 = point;
                startDraw1 = false;
                mouseListenertIsActive2 = false;
                counterLineButton1.setDisable(false);
                speedLineButton1.setDisable(false);
                videoContainerImageView1.setOnMousePressed(null);
                videoContainerImageView1.setOnMouseMoved(null);
            }

        } else if (mb == MouseButton.NONE && startDraw1) {
            copiedImage1 = currentImage1.clone();
            Imgproc.line(copiedImage1, lineSpeed11, point, new Scalar(0, 255, 0), 1);
            if (lineCount11 != null && lineCount12 != null)
                Imgproc.line(copiedImage1, lineCount11, lineCount12, new Scalar(0, 0, 255), 1);
            updateView(copiedImage1, 2);
        }
    }
    
    private void dbUpdateCounters(int count, double avgSpeed, int videoId) {
		Runnable dbUpdate = new Runnable() {
			@Override
			public void run() {
                RawDataDAO rawDataDao = null;
                Date date = new Date();
                try {
                    rawDataDao = new RawDataDAO();
                } catch (ConnectException e) {
                    e.printStackTrace();
                }

                String currentDateTime = dateFormat.format(date);
				String day = currentDay.format(date);
				String facilityType = "";

				if (videoId == 1) {
                    if (interruptedCheckBox.isSelected())
                        facilityType = "Interrupted";
                    else
                        facilityType = "Uninterrupted";

                    log.info("updating database counters");
                    rawDataDao.addRawData(count, avgSpeed, currentDateTime, areaTextField.getText().toUpperCase(), facilityType);
                } else if (videoId == 2) {
                    if (interruptedCheckBox1.isSelected())
                        facilityType = "Interrupted";
                    else
                        facilityType = "Uninterrupted";

                    log.info("updating database counters");
                    rawDataDao.addRawData(count, avgSpeed, currentDateTime, areaTextField1.getText().toUpperCase(), facilityType);
                }

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

               processSyncCloud();

            }
        }
        else {
            Alert alertInfo = new Alert(AlertType.INFORMATION);
            alertInfo.setTitle("Information");
            alertInfo.setHeaderText(null);
            alertInfo.setContentText("Already syncing. Please wait...");

            alertInfo.showAndWait();
        }

    }

    public void processSyncCloud() {

        Task<String> task = new Task<String>() {
            @Override public String call() {
                CloudOperations cloudOperations = new CloudOperations();
                return cloudOperations.syncCloud();
            }
        };

        task.setOnScheduled(new EventHandler<WorkerStateEvent>()  {
            public void handle(WorkerStateEvent t ) {
                syncCloudProgressBar.setVisible(true);
            }
        });

        task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent t) {
                String syncStatus =  task.getValue();
                log.debug("syncStatus: " + syncStatus);
                syncCloudProgressBar.setVisible(false);

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

            }
        });

        new Thread(task).start();
		 

    }

      
}
