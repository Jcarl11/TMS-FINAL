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
import java.util.logging.Level;
import org.asynchttpclient.Response;

import static org.opencv.imgproc.Imgproc.resize;
import org.tms.db.TaskExecutor;
import org.tms.utilities.GlobalObjects;
import org.tms.utilities.UsersPreferences;

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

    //Video Frame 3
    private VideoCapture capture2;
    private VideoProcessor videoProcessor2 = new MixtureOfGaussianBackground(imageThreshold, history);
    private volatile String videoPath2;
    private double videoFPS2;
    private Mat currentImage2 = new Mat();
    private Mat foregroundImage2;
    private Mat foregroundClone2;
    private boolean startDraw2;
    private Point lineCount112;           //new Point(370,200);
    private volatile Point lineCount122;          //new Point(400,280);
    private Point lineSpeed112;           //new Point(460,200);
    private volatile Point lineSpeed122;          //new Point(490,270);
    private volatile boolean isPaused2 = true;
    private boolean isStarted2 = false;
    private Mat copiedImage2;
    private int maxFPS2;
    private long oneFrameDuration2;
    private double timeInSec2;
    private int whichFrame2;
    private double sumSpeedVehicle2 = 0;
    private boolean crossingLine2 = false;
    private boolean crossingSpeedLine2 = false;
    private long startTime2;
    private int counter2 = 0;
    private int lastTSM2 = 0;
    private HashMap<Integer, Integer> speed2 = new HashMap<Integer, Integer>();
    private int divisorVehicle2 = 3;
    private volatile boolean loopBreaker2 = false;
    //Video Frame 4
    private VideoCapture capture3;
    private VideoProcessor videoProcessor3 = new MixtureOfGaussianBackground(imageThreshold, history);
    private volatile String videoPath3;
    private double videoFPS3;
    private Mat currentImage3 = new Mat();
    private Mat foregroundImage3;
    private Mat foregroundClone3;
    private boolean startDraw3;
    private Point lineCount113;           //new Point(370,200);
    private volatile Point lineCount123;          //new Point(400,280);
    private Point lineSpeed113;           //new Point(460,200);
    private volatile Point lineSpeed123;          //new Point(490,270);
    private volatile boolean isPaused3 = true;
    private boolean isStarted3 = false;
    private Mat copiedImage3;
    private int maxFPS3;
    private long oneFrameDuration3;
    private double timeInSec3;
    private int whichFrame3;
    private double sumSpeedVehicle3 = 0;
    private boolean crossingLine3 = false;
    private boolean crossingSpeedLine3 = false;
    private long startTime3;
    private int counter3 = 0;
    private int lastTSM3 = 0;
    private HashMap<Integer, Integer> speed3 = new HashMap<Integer, Integer>();
    private int divisorVehicle3 = 3;
    private volatile boolean loopBreaker3 = false;

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
    private Button dashboard_logout;
    @FXML
    private TextField filePathTextField2;
    @FXML
    private ImageView videoContainerImageView2;
    @FXML
    private TextField quantityTextField2;
    @FXML
    private TextField avgSpeedTextField2;
    @FXML
    private Button playPauseButton2;
    @FXML
    private Button resetButton2;
    @FXML
    private Button chooseFileButton2;
    @FXML
    private TextField areaTextField2;
    @FXML
    private Button counterLineButton2;
    @FXML
    private Button speedLineButton2;
    @FXML
    private CheckBox interruptedCheckBox2;
    @FXML
    private TextField filePathTextField3;
    @FXML
    private ImageView videoContainerImageView3;
    @FXML
    private TextField quantityTextField3;
    @FXML
    private TextField avgSpeedTextField3;
    @FXML
    private Button playPauseButton3;
    @FXML
    private Button resetButton3;
    @FXML
    private Button chooseFileButton3;
    @FXML
    private TextField areaTextField3;
    @FXML
    private Button counterLineButton3;
    @FXML
    private Button speedLineButton3;
    @FXML
    private CheckBox interruptedCheckBox3;


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
        playPauseButton2.setDisable(true);
        counterLineButton2.setDisable(true);
        speedLineButton2.setDisable(true);
        resetButton2.setDisable(true);
        playPauseButton3.setDisable(true);
        counterLineButton3.setDisable(true);
        speedLineButton3.setDisable(true);
        resetButton3.setDisable(true);
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
    private void handleCounterLineButtonAction2(ActionEvent event) {
        counterLineButton2.setDisable(true);
        speedLineButton2.setDisable(true);
        mouseListenertIsActive = true;
        startDraw2 = false;
        videoContainerImageView2.setOnMousePressed(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent me) {
                if (mouseListenertIsActive) {
                    call_2(me.getButton(), new Point(me.getX(), me.getY()));
                } else if (mouseListenertIsActive2) {
                    call_22(me.getButton(), new Point(me.getX(), me.getY()));
                }
            }

        });

        videoContainerImageView2.setOnMouseMoved(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent me) {
                if (mouseListenertIsActive) {
                    call_2(me.getButton(), new Point(me.getX(), me.getY()));
                } else if (mouseListenertIsActive2) {
                    call_22(me.getButton(), new Point(me.getX(), me.getY()));
                }
            }
        });
    }

    @FXML
    private void handleSpeedLineButtonAction2(ActionEvent event) {
        counterLineButton2.setDisable(true);
        speedLineButton2.setDisable(true);
        mouseListenertIsActive2 = true;
        startDraw2 = false;
        videoContainerImageView2.setOnMousePressed(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent me) {
                if (mouseListenertIsActive) {
                    call_2(me.getButton(), new Point(me.getX(), me.getY()));
                } else if (mouseListenertIsActive2) {
                    call_22(me.getButton(), new Point(me.getX(), me.getY()));
                }
            }

        });

        videoContainerImageView2.setOnMouseMoved(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent me) {
                if (mouseListenertIsActive) {
                    call_2(me.getButton(), new Point(me.getX(), me.getY()));
                } else if (mouseListenertIsActive2) {
                    call_22(me.getButton(), new Point(me.getX(), me.getY()));
                }
            }
        });
    }
    @FXML
    private void handleCounterLineButtonAction3(ActionEvent event) {
        counterLineButton3.setDisable(true);
        speedLineButton3.setDisable(true);
        mouseListenertIsActive = true;
        startDraw3 = false;
        videoContainerImageView3.setOnMousePressed(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent me) {
                if (mouseListenertIsActive) {
                    call_3(me.getButton(), new Point(me.getX(), me.getY()));
                } else if (mouseListenertIsActive2) {
                    call_33(me.getButton(), new Point(me.getX(), me.getY()));
                }
            }

        });

        videoContainerImageView3.setOnMouseMoved(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent me) {
                if (mouseListenertIsActive) {
                    call_3(me.getButton(), new Point(me.getX(), me.getY()));
                } else if (mouseListenertIsActive2) {
                    call_33(me.getButton(), new Point(me.getX(), me.getY()));
                }
            }
        });
    }

    @FXML
    private void handleSpeedLineButtonAction3(ActionEvent event) {
        counterLineButton3.setDisable(true);
        speedLineButton3.setDisable(true);
        mouseListenertIsActive2 = true;
        startDraw3 = false;
        videoContainerImageView3.setOnMousePressed(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent me) {
                if (mouseListenertIsActive) {
                    call_3(me.getButton(), new Point(me.getX(), me.getY()));
                } else if (mouseListenertIsActive2) {
                    call_33(me.getButton(), new Point(me.getX(), me.getY()));
                }
            }

        });

        videoContainerImageView3.setOnMouseMoved(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent me) {
                if (mouseListenertIsActive) {
                    call_3(me.getButton(), new Point(me.getX(), me.getY()));
                } else if (mouseListenertIsActive2) {
                    call_33(me.getButton(), new Point(me.getX(), me.getY()));
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
    private void handleChooseFileButtonAction2(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Resource File");
        fileChooser.getExtensionFilters().addAll(
                new ExtensionFilter("Video Files", "*.avi", "*.mp4", "*.mpg", "*.mov"));
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home"), "Desktop"));
        File selectedFile = fileChooser.showOpenDialog(chooseFileButton.getScene().getWindow());

        if (selectedFile != null) {

            videoPath2 = selectedFile.getPath();
            filePathTextField2.setText(videoPath2);
            capture2 = new VideoCapture(videoPath2);
            capture2.read(currentImage2);
            videoFPS2 = capture2.get(Videoio.CAP_PROP_FPS);
            log.debug("videoFPS1: " + videoFPS2);
            resize(currentImage2, currentImage2, new Size(640, 360));
            updateView(currentImage2, 3);
            if (videoPath2 != null) {
                counterLineButton2.setDisable(false);
                speedLineButton2.setDisable(false);

                resetButton2.setDisable(false);
                playPauseButton2.setDisable(false);
            }
        }
    }

    @FXML
    private void handleChooseFileButtonAction3(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Resource File");
        fileChooser.getExtensionFilters().addAll(
                new ExtensionFilter("Video Files", "*.avi", "*.mp4", "*.mpg", "*.mov"));
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home"), "Desktop"));
        File selectedFile = fileChooser.showOpenDialog(chooseFileButton.getScene().getWindow());

        if (selectedFile != null) {

            videoPath3 = selectedFile.getPath();
            filePathTextField3.setText(videoPath3);
            capture3 = new VideoCapture(videoPath3);
            capture3.read(currentImage3);
            videoFPS3 = capture3.get(Videoio.CAP_PROP_FPS);
            log.debug("videoFPS1: " + videoFPS3);
            resize(currentImage3, currentImage3, new Size(640, 360));
            updateView(currentImage3, 4);
            if (videoPath3 != null) {
                counterLineButton3.setDisable(false);
                speedLineButton3.setDisable(false);

                resetButton3.setDisable(false);
                playPauseButton3.setDisable(false);
            }
        }
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
            chooseFileButton1.setDisable(false);

        } else {
            // ... user chose CANCEL or closed the dialog
        }
    }

    @FXML
    private void handlePlayPauseButtonAction2(ActionEvent event) {
        if (lineSpeed122 == null && lineCount122 == null) {
            Alert alert = new Alert(AlertType.WARNING);
            alert.setTitle("Invalid Input");
            alert.setHeaderText("Draw sensor lines");
            alert.setContentText("Please draw counter line and speed line!");

            alert.showAndWait();
        } else {
            if (!isPaused2) {
                isPaused2 = true;
                playPauseButton2.setText("Play");

                chooseFileButton2.setDisable(false);

                counterLineButton2.setDisable(false);
                speedLineButton2.setDisable(false);

            } else {
                isPaused2 = false;
                playPauseButton2.setText("Pause");

                maxWaitingFPS2();

                chooseFileButton2.setDisable(true);

                counterLineButton2.setDisable(true);
                speedLineButton2.setDisable(true);

            }

            if (!isStarted2) {
                log.info("mainLoop");
                loopBreaker2 = false;
                Thread mainLoop = new Thread(new Loop(3));
                mainLoop.start();
                isStarted2 = true;
                resetButton2.setDisable(false);

            }
        }
    }

    @FXML
    private void handleResetButtonAction2(ActionEvent event) {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Reset Video");
        alert.setHeaderText("Are you sure you want to reset video?");
        alert.setContentText(videoPath);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK){
            resetVideo(3);
            log.info("Video has been reset.");
            chooseFileButton2.setDisable(false);

        } else {
            // ... user chose CANCEL or closed the dialog
        }
    }

    @FXML
    private void handlePlayPauseButtonAction3(ActionEvent event) {
        if (lineSpeed123 == null && lineCount123 == null) {
            Alert alert = new Alert(AlertType.WARNING);
            alert.setTitle("Invalid Input");
            alert.setHeaderText("Draw sensor lines");
            alert.setContentText("Please draw counter line and speed line!");

            alert.showAndWait();
        } else {
            if (!isPaused3) {
                isPaused3 = true;
                playPauseButton3.setText("Play");

                chooseFileButton3.setDisable(false);

                counterLineButton3.setDisable(false);
                speedLineButton3.setDisable(false);

            } else {
                isPaused3 = false;
                playPauseButton3.setText("Pause");

                maxWaitingFPS3();

                chooseFileButton3.setDisable(true);

                counterLineButton3.setDisable(true);
                speedLineButton3.setDisable(true);

            }

            if (!isStarted3) {
                log.info("mainLoop");
                loopBreaker3 = false;
                Thread mainLoop = new Thread(new Loop(4));
                mainLoop.start();
                isStarted3 = true;
                resetButton3.setDisable(false);

            }
        }
    }

    @FXML
    private void handleResetButtonAction3(ActionEvent event) {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Reset Video");
        alert.setHeaderText("Are you sure you want to reset video?");
        alert.setContentText(videoPath);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK){
            resetVideo(4);
            log.info("Video has been reset.");
            chooseFileButton3.setDisable(false);

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
        }else if(videoId == 3) {
            if(isPaused2)
                isPaused2 = false;
            loopBreaker2 = true;

            capture2 = new VideoCapture(videoPath2);
            capture2.read(currentImage2);
            videoFPS2 = capture2.get(Videoio.CAP_PROP_FPS);
            resize(currentImage2, currentImage2, new Size(640, 360));
            updateView(currentImage2, 3);

            isPaused2 = true;
            playPauseButton2.setText("Play");
            playPauseButton2.setDisable(false);
            videoProcessor2 = new MixtureOfGaussianBackground(imageThreshold, history);

            resetButton2.setDisable(true);

            counterLineButton2.setDisable(false);
            speedLineButton2.setDisable(false);
            lineCount112 = null;
            lineCount122 = null;
            lineSpeed112 = null;
            lineSpeed122 = null;

            whichFrame2 = 0;
            timeInSec2 = 0;

            quantityTextField2.setText("0");
            avgSpeedTextField2.setText("0");

            sumSpeedVehicle2 = 0;

            divisorVehicle2 = 1;

            counter2 = 0;
            lastTSM2 = 0;

            isStarted2 = false;
        } else if(videoId == 4) {
            if(isPaused3)
                isPaused3 = false;
            loopBreaker3 = true;

            capture3 = new VideoCapture(videoPath3);
            capture3.read(currentImage3);
            videoFPS3 = capture3.get(Videoio.CAP_PROP_FPS);
            resize(currentImage3, currentImage3, new Size(640, 360));
            updateView(currentImage3, 4);

            isPaused3 = true;
            playPauseButton3.setText("Play");
            playPauseButton3.setDisable(false);
            videoProcessor3 = new MixtureOfGaussianBackground(imageThreshold, history);

            resetButton3.setDisable(true);

            counterLineButton3.setDisable(false);
            speedLineButton3.setDisable(false);
            lineCount113 = null;
            lineCount123 = null;
            lineSpeed113 = null;
            lineSpeed123 = null;

            whichFrame3 = 0;
            timeInSec3 = 0;

            quantityTextField3.setText("0");
            avgSpeedTextField3.setText("0");

            sumSpeedVehicle3 = 0;

            divisorVehicle3 = 1;

            counter3 = 0;
            lastTSM3 = 0;

            isStarted3 = false;
        }

    }

    private void updateView(Mat matImage, int videoId) {
        Image image = SwingFXUtils.toFXImage(imageProcessor.toBufferedImage(matImage), null);
        Platform.runLater(()->{
            if (videoId == 1 )
                videoContainerImageView.setImage(image);
            else if (videoId == 2)
                videoContainerImageView1.setImage(image);
            else if (videoId == 3)
                videoContainerImageView2.setImage(image);
            else if (videoId == 4)
                videoContainerImageView3.setImage(image);
        });

    }

    @FXML
    private void logoutClicked(ActionEvent event) {
        log.info("Logout process started");
        TaskExecutor.getInstance().logout(UsersPreferences.getInstance()
                .getPreference().get("sessionToken", null));
        GlobalObjects.getInstance().bindBtnNProgress(dashboard_logout,
                syncCloudProgressBar, TaskExecutor.getInstance()
                        .getMyTask().runningProperty());
        TaskExecutor.getInstance().getMyTask().setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent event) {
                Response response = (Response) TaskExecutor.getInstance().getMyTask().getValue();
                log.debug("LOGOUT RESPONSE CODE: " + String.valueOf(response.getStatusCode()));
                if (response.getStatusCode() == 200) {
                    UsersPreferences.getInstance().clearPreference();
                    try {
                        ((Stage) dashboard_logout.getScene().getWindow()).close();
                        new TrafficMonitoringSystem().start(new Stage());

                    } catch (Exception ex) {
                        java.util.logging.Logger
                                .getLogger(DashboardController.class.getName())
                                .log(Level.SEVERE, null, ex);
                    }

                }

            }
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
            } else if (videoId == 3) {
                maxWaitingFPS2();
                log.debug("videoId: " + this.videoId);
                videoProcessor2 = new MixtureOfGaussianBackground(imageThreshold, history);
                if (capture2.isOpened()) {
                    while (true) {
                        if (!isPaused2) {

                            capture2.read(currentImage2);
                            if (!currentImage2.empty()) {
                                resize(currentImage2, currentImage2, new Size(640, 360));
                                foregroundImage2 = currentImage2.clone();
                                foregroundImage2 = videoProcessor2.process(foregroundImage2);

                                foregroundClone2 = foregroundImage2.clone();
                                Imgproc.bilateralFilter(foregroundClone2, foregroundImage2, 2, 1600, 400);

                                CountVehicles countVehicles = new CountVehicles(areaThreshold, vehicleSizeThreshold, lineCount112, lineCount122, lineSpeed112, lineSpeed122, crossingLine2, crossingSpeedLine2);

                                countVehicles.findAndDrawContours(currentImage2, foregroundImage2);

                                try {
                                    count(countVehicles, videoId);
                                    speedMeasure(countVehicles, videoId);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                                videoRealTime(videoId);

                                long time = System.currentTimeMillis() - startTime2;
                                if (time < oneFrameDuration2) {
                                    try {
                                        Thread.sleep(oneFrameDuration2 - time);
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
                                updateView(currentImage2, videoId);
                                startTime2 = System.currentTimeMillis();

                                if (loopBreaker2)
                                    break;

                            } else {

                                playPauseButton2.setDisable(true);

                                chooseFileButton2.setDisable(false);
                                Platform.runLater(()->{
                                    playPauseButton2.setText("Play");
                                });

                                whichFrame2 = 0;
                                isStarted2 = false;
                                log.info("The video has finished!");
                                break;
                            }
                        }
                    }
                }
            } else if (videoId == 4) {
                maxWaitingFPS3();
                log.debug("videoId: " + this.videoId);
                videoProcessor3 = new MixtureOfGaussianBackground(imageThreshold, history);
                if (capture3.isOpened()) {
                    while (true) {
                        if (!isPaused3) {

                            capture3.read(currentImage3);
                            if (!currentImage3.empty()) {
                                resize(currentImage3, currentImage3, new Size(640, 360));
                                foregroundImage3 = currentImage3.clone();
                                foregroundImage3 = videoProcessor3.process(foregroundImage3);

                                foregroundClone3 = foregroundImage3.clone();
                                Imgproc.bilateralFilter(foregroundClone3, foregroundImage3, 2, 1600, 400);

                                CountVehicles countVehicles = new CountVehicles(areaThreshold, vehicleSizeThreshold, lineCount113, lineCount123, lineSpeed113, lineSpeed123, crossingLine3, crossingSpeedLine3);

                                countVehicles.findAndDrawContours(currentImage3, foregroundImage3);

                                try {
                                    count(countVehicles, videoId);
                                    speedMeasure(countVehicles, videoId);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                                videoRealTime(videoId);

                                long time = System.currentTimeMillis() - startTime3;
                                if (time < oneFrameDuration3) {
                                    try {
                                        Thread.sleep(oneFrameDuration3 - time);
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
                                updateView(currentImage3, videoId);
                                startTime3 = System.currentTimeMillis();

                                if (loopBreaker3)
                                    break;

                            } else {

                                playPauseButton3.setDisable(true);

                                chooseFileButton3.setDisable(false);
                                Platform.runLater(()->{
                                    playPauseButton3.setText("Play");
                                });

                                whichFrame3 = 0;
                                isStarted3 = false;
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

    public void maxWaitingFPS2() {
        double time = (distanceCS / 3);
        double max = videoFPS2 * time;
        maxFPS2 = (int) max;

        oneFrameDuration2 = 1000 / (long) videoFPS2;
    }
    public void maxWaitingFPS3() {
        double time = (distanceCS / 3);
        double max = videoFPS3 * time;
        maxFPS3 = (int) max;

        oneFrameDuration3 = 1000 / (long) videoFPS3;
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
        } else if (videoId == 3) {
            whichFrame2++;
            timeInSec2 = whichFrame2 / videoFPS2;
            return timeInSec2;
        } else if (videoId == 4) {
            whichFrame3++;
            timeInSec3 = whichFrame3 / videoFPS3;
            return timeInSec3;
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
        } else if (videoId == 3) {
            if (countVehicles.isVehicleToAdd()) {
                counter2++;
                lastTSM2++;
                speed2.put(lastTSM2, 0);

                quantityTextField2.setText(Integer.toString(counter2));
//            log.debug("count: " + counter);

            }
            crossingLine2 = countVehicles.isCrossingLine();
        } else if (videoId == 4) {
            if (countVehicles.isVehicleToAdd()) {
                counter3++;
                lastTSM3++;
                speed3.put(lastTSM3, 0);

                quantityTextField3.setText(Integer.toString(counter3));
//            log.debug("count: " + counter);

            }
            crossingLine3 = countVehicles.isCrossingLine();
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
        } else if (videoId == 3) {
            if (!speed2.isEmpty()) {
                int firstTSM = speed2.entrySet().iterator().next().getKey();
                if (countVehicles.isToSpeedMeasure()) {
                    for (int i = firstTSM; i <= lastTSM2; i++) {
                        if (speed2.containsKey(i)) {
                            speed2.put(i, (speed2.get(i) + 1));
                        }
                    }

                    double currentSpeed = computeSpeed(speed2.get(firstTSM), videoId);
                    sumSpeedVehicle2 += currentSpeed;
                    double avgSpeed = sumSpeedVehicle2 / divisorVehicle2;
                    divisorVehicle2++;

                    dbUpdateCounters(1, currentSpeed, videoId);
                    Platform.runLater(()->{
                        avgSpeedTextField2.setText(String.format("%.2f", avgSpeed));
                    });

                    speed2.remove(firstTSM);

                } else {
                    for (int i = firstTSM; i <= lastTSM2; i++) {
                        if (speed2.containsKey(i)) {
                            int currentFPS = speed2.get(i);
                            speed2.put(i, (currentFPS + 1));
                            if (currentFPS > maxFPS2) {
                                speed2.remove(i);

                            }
                        }
                    }
                }
            }
            crossingSpeedLine2 = countVehicles.isCrossingSpeedLine();
        } else if (videoId == 4) {
            if (!speed3.isEmpty()) {
                int firstTSM = speed3.entrySet().iterator().next().getKey();
                if (countVehicles.isToSpeedMeasure()) {
                    for (int i = firstTSM; i <= lastTSM3; i++) {
                        if (speed3.containsKey(i)) {
                            speed3.put(i, (speed3.get(i) + 1));
                        }
                    }

                    double currentSpeed = computeSpeed(speed3.get(firstTSM), videoId);
                    sumSpeedVehicle3 += currentSpeed;
                    double avgSpeed = sumSpeedVehicle3 / divisorVehicle3;
                    divisorVehicle3++;

                    dbUpdateCounters(1, currentSpeed, videoId);
                    Platform.runLater(()->{
                        avgSpeedTextField3.setText(String.format("%.2f", avgSpeed));
                    });

                    speed3.remove(firstTSM);

                } else {
                    for (int i = firstTSM; i <= lastTSM3; i++) {
                        if (speed3.containsKey(i)) {
                            int currentFPS = speed3.get(i);
                            speed3.put(i, (currentFPS + 1));
                            if (currentFPS > maxFPS3) {
                                speed3.remove(i);

                            }
                        }
                    }
                }
            }
            crossingSpeedLine2 = countVehicles.isCrossingSpeedLine();
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
        } else if (videoId == 3) {
            double duration = speedPFS / videoFPS2;
            double v = (distanceCS / duration) * 3.6;
            return v;
        } else if (videoId == 4) {
            double duration = speedPFS / videoFPS3;
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

    public void call_2(MouseButton mb, Point point) {
        if (mb == MouseButton.PRIMARY) {
            if (!startDraw2) {
                lineCount112 = point;
                startDraw2 = true;
            } else {
                lineCount122 = point;
                startDraw2 = false;
                mouseListenertIsActive = false;
                counterLineButton2.setDisable(false);
                speedLineButton2.setDisable(false);

                videoContainerImageView2.setOnMousePressed(null);
                videoContainerImageView2.setOnMouseMoved(null);
            }

        } else if (mb == MouseButton.NONE && startDraw2) {
            copiedImage2 = currentImage2.clone();
            Imgproc.line(copiedImage2, lineCount112, point, new Scalar(0, 0, 255), 1);
            if (lineSpeed112 != null && lineSpeed122 != null)
                Imgproc.line(copiedImage2, lineSpeed112, lineSpeed122, new Scalar(0, 255, 0), 1);
            updateView(copiedImage2, 3);
        }
    }

    private void call_22(MouseButton mb, Point point) {
        if (mb == MouseButton.PRIMARY) {
            if (!startDraw2) {
                lineSpeed112 = point;
                startDraw2 = true;
            } else {
                lineSpeed122 = point;
                startDraw2 = false;
                mouseListenertIsActive2 = false;
                counterLineButton2.setDisable(false);
                speedLineButton2.setDisable(false);
                videoContainerImageView2.setOnMousePressed(null);
                videoContainerImageView2.setOnMouseMoved(null);
            }

        } else if (mb == MouseButton.NONE && startDraw2) {
            copiedImage2 = currentImage2.clone();
            Imgproc.line(copiedImage2, lineSpeed112, point, new Scalar(0, 255, 0), 1);
            if (lineCount112 != null && lineCount122 != null)
                Imgproc.line(copiedImage2, lineCount112, lineCount122, new Scalar(0, 0, 255), 1);
            updateView(copiedImage2, 3);
        }
    }

    public void call_3(MouseButton mb, Point point) {
        if (mb == MouseButton.PRIMARY) {
            if (!startDraw3) {
                lineCount113 = point;
                startDraw3 = true;
            } else {
                lineCount123 = point;
                startDraw3 = false;
                mouseListenertIsActive = false;
                counterLineButton3.setDisable(false);
                speedLineButton3.setDisable(false);

                videoContainerImageView3.setOnMousePressed(null);
                videoContainerImageView3.setOnMouseMoved(null);
            }

        } else if (mb == MouseButton.NONE && startDraw3) {
            copiedImage3 = currentImage3.clone();
            Imgproc.line(copiedImage3, lineCount113, point, new Scalar(0, 0, 255), 1);
            if (lineSpeed113 != null && lineSpeed123 != null)
                Imgproc.line(copiedImage3, lineSpeed113, lineSpeed123, new Scalar(0, 255, 0), 1);
            updateView(copiedImage3, 4);
        }
    }

    private void call_33(MouseButton mb, Point point) {
        if (mb == MouseButton.PRIMARY) {
            if (!startDraw3) {
                lineSpeed113 = point;
                startDraw3 = true;
            } else {
                lineSpeed123 = point;
                startDraw3 = false;
                mouseListenertIsActive = false;
                counterLineButton3.setDisable(false);
                speedLineButton3.setDisable(false);
                videoContainerImageView3.setOnMousePressed(null);
                videoContainerImageView3.setOnMouseMoved(null);
            }

        } else if (mb == MouseButton.NONE && startDraw3) {
            copiedImage3 = currentImage3.clone();
            Imgproc.line(copiedImage3, lineSpeed113, point, new Scalar(0, 255, 0), 1);
            if (lineCount113 != null && lineCount123 != null)
                Imgproc.line(copiedImage3, lineCount113, lineCount123, new Scalar(0, 0, 255), 1);
            updateView(copiedImage3, 4);
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
                } else if (videoId == 3) {
                    if (interruptedCheckBox2.isSelected())
                        facilityType = "Interrupted";
                    else
                        facilityType = "Uninterrupted";

                    log.info("updating database counters");
                    rawDataDao.addRawData(count, avgSpeed, currentDateTime, areaTextField2.getText().toUpperCase(), facilityType);
                } else if (videoId == 4) {
                    if (interruptedCheckBox3.isSelected())
                        facilityType = "Interrupted";
                    else
                        facilityType = "Uninterrupted";

                    log.info("updating database counters");
                    rawDataDao.addRawData(count, avgSpeed, currentDateTime, areaTextField3.getText().toUpperCase(), facilityType);
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
