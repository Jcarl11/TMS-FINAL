package org.tms;

import org.opencv.core.*;
import org.opencv.core.Point;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;
// import org.opencv.videoio.VideoWriter;
import org.opencv.videoio.Videoio;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;

import static org.opencv.imgproc.Imgproc.resize;

public class GUI {
	private JLabel imageView;
	private JFrame frame;
	private JFrame frameBGS;
    private JLabel BGSview;
    
	private JButton playPauseButton;
    JButton loadButton;
    private JButton resetButton;
    private JButton countingLineButton;
    private JButton speedLineButton;
    
    private volatile boolean loopBreaker = false;
    
    private int areaThreshold = 1700;
    private double imageThreshold = 16;
    private int history = 500;
    private int vehicleSizeThreshold = 20000;
    
    private VideoCapture capture;
    private Mat currentImage = new Mat();
    private VideoProcessor videoProcessor = new MixtureOfGaussianBackground(imageThreshold, history);
    private ImageProcessor imageProcessor = new ImageProcessor();
    private Mat foregroundImage;
    
    private JFormattedTextField currentTimeField;
    private volatile boolean isPaused = true;
    
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
    private int divisorVehicle = 1;
    
    private int counter = 0;
    private int lastTSM = 0;
    private HashMap<Integer, Integer> speed = new HashMap<Integer, Integer>();
    
    private volatile boolean isProcessInRealTime = true;
    private long startTime;
    private JFormattedTextField vehiclesAmountField;
    private JFormattedTextField vehiclesSpeedField;
    
    private Mat foregroundClone;
    
    private boolean mouseListenertIsActive;
    private boolean mouseListenertIsActive2;
    private boolean startDraw;
    private Mat copiedImage;
    
    private JButton BGSButton;
//    private JSpinner imgThresholdField;
    private volatile boolean isBGSview = false;
    private Mat ImageBGS = new Mat();
	    
	public void init() throws IOException {
		setSystemLookAndFeel();
		initGUI();

        while (true) {
            if (videoPath != null) {
                countingLineButton.setEnabled(true);
                speedLineButton.setEnabled(true);
//                distanceBLfield.setEnabled(true);

                resetButton.setEnabled(true);
                break;
            }
        }

        while (true) {
            if (lineSpeed2 != null && lineCount2 != null) {
                playPauseButton.setEnabled(true);
                break;
            }
        }

        Thread mainLoop = new Thread(new Loop());
        mainLoop.start();
    }
	
	public void initGUI() {
        frame = createJFrame("Traffic Management System");

        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        playPauseButton.setEnabled(false);
        countingLineButton.setEnabled(false);
        speedLineButton.setEnabled(false);
        resetButton.setEnabled(false);


    }
	
    private JFrame createJFrame(String windowName) {
        frame = new JFrame(windowName);
        frame.setLayout(new GridBagLayout());

        setupVideo(frame);

        reset(frame);
        playPause(frame);
//        setupSaveVideo(frame);
//        setupWriteType(frame);

        loadFile(frame);
//        saveFile(frame);

//        infoCars(frame);
//        infoVans(frame);
//        infoLorries(frame);
        infoVehicles(frame);

        selectCountingLine(frame);
        selectSpeedLine(frame);
//        setupDistanceBetweenLines(frame);

//        setupImageThreshold(frame);
//        setupVideoHistory(frame);
//        setupAreaThreshold(frame);
//        setupVehicleSizeThreshold(frame);

        setupBGSvisibility(frame);
        currentTime(frame);
//        setupRealTime(frame);

        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        return frame;
    }
    
    private void setupVideo(JFrame frame) {
        imageView = new JLabel();


        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 2;
        c.gridy = 2;
        c.gridwidth = 6;
        c.gridheight = 9;

        frame.add(imageView, c);

        Mat localImage = new Mat(new Size(640, 360), CvType.CV_8UC3, new Scalar(255, 255, 255));
        resize(localImage, localImage, new Size(640, 360));
        updateView(localImage);
    }
    
    private void setupBGSvisibility(JFrame frame) {
        BGSButton = new JButton("BGS view");
        BGSButton.setPreferredSize(new Dimension(200, 30));

        BGSButton.addActionListener(event -> {
            initBGSview();
            BGSButton.setEnabled(false);
            isBGSview = true;
        });

        GridBagConstraints c = new GridBagConstraints();
        BGSButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        c.gridx = 0;
        c.gridy = 10;
        c.gridwidth = 2;

        frame.add(BGSButton, c);

    }
    
    private void initBGSview() {
        frameBGS = new JFrame("BGS View");
        BGSview = new JLabel();
        frameBGS.add(BGSview);
        Mat localImage = new Mat(new Size(430, 240), CvType.CV_8UC3, new Scalar(255, 255, 255));
        BGSview.setIcon(new ImageIcon(imageProcessor.toBufferedImage(localImage)));
        frameBGS.setVisible(true);
        frameBGS.pack();

        frameBGS.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                BGSButton.setEnabled(true);
                isBGSview = false;
            }
        });

    }
    
    private void reset(JFrame frame) {
        resetButton = new JButton("Reset");

        resetButton.addActionListener(event -> {

            int n = JOptionPane.showConfirmDialog(
                    frame, "Are you sure you want to reset the video?",
                    "Reset", JOptionPane.YES_NO_OPTION);
            if (n == JOptionPane.YES_OPTION) {
                loopBreaker = true;
                System.out.println("videoPath: " + videoPath);
                capture = new VideoCapture(videoPath);
                capture.read(currentImage);
                System.out.println("read current image done.");
                videoFPS = capture.get(Videoio.CAP_PROP_FPS);
                resize(currentImage, currentImage, new Size(640, 360));
                updateView(currentImage);

                currentTimeField.setValue("0 sec");

                isPaused = true;
                playPauseButton.setText("Play");
                playPauseButton.setEnabled(false);
                videoProcessor = new MixtureOfGaussianBackground(imageThreshold, history);

                resetButton.setEnabled(false);

                countingLineButton.setEnabled(true);
                speedLineButton.setEnabled(true);
//                distanceBLfield.setEnabled(true);
                lineCount1 = null;
                lineCount2 = null;
                lineSpeed1 = null;
                lineSpeed2 = null;

                minutes = 1;
                second = 0;
                whichFrame = 0;
                timeInSec = 0;

                vehiclesAmountField.setValue(new Integer(0));
                vehiclesSpeedField.setValue(new Integer(0));
//                carsSpeedField.setValue(new Integer(0));
//                vansAmountField.setValue(new Integer(0));
//                vansSpeedField.setValue(new Integer(0));
//                lorriesAmountField.setValue(new Integer(0));
//                lorriesSpeedField.setValue(new Integer(0));
//
                cars = 0;
                vans = 0;
                lorries = 0;

                sumSpeedCar = 0;
                sumSpeedVan = 0;
                sumSpeedLorry = 0;
                sumSpeedVehicle = 0;

                divisorCar = 1;
                divisorVan = 1;
                divisorLorry = 1;
                divisorVehicle = 1;

                counter = 0;
                lastTSM = 0;

                Thread reseting = new Thread(new Reseting());
                reseting.start();
                loopBreaker = false;
            }

        });

        resetButton.setAlignmentX(Component.LEFT_ALIGNMENT);

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(5, 60, 5, 60);
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 2;

        frame.add(resetButton, c);
    }
    private class Reseting implements Runnable {

        @Override
        public void run() {

            while (true) {
                if (lineSpeed2 != null && lineCount2 != null) {
                    playPauseButton.setEnabled(true);
                    resetButton.setEnabled(true);

                    Thread mainLoop = new Thread(new Loop());
                    mainLoop.start();
                    
                    break;
                }
            }
        }
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
                                BGSview.setIcon(new ImageIcon(imageProcessor.toBufferedImage(ImageBGS)));
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
//                            }
                            updateView(currentImage);
                            startTime = System.currentTimeMillis();

                            if (loopBreaker)
                                break;

                        } else {

                            playPauseButton.setEnabled(false);

//                            saveButton.setEnabled(true);
                            loadButton.setEnabled(true);

                            playPauseButton.setText("Play");
                            minutes = 1;
                            second = 0;
                            whichFrame = 0;
//                            System.out.println("The video has finished!");
                            break;
                        }
                    }
                }
            }
        }
    }
    
    private double videoRealTime() {
        whichFrame++;
        timeInSec = whichFrame / videoFPS;
        setTimeInMinutes();
        return timeInSec;
    }
    
    private void setTimeInMinutes() {
        if (timeInSec < 60) {
            currentTimeField.setValue((int) timeInSec + " sec");
        } else if (second < 60) {
            second = (int) timeInSec - (60 * minutes);
            currentTimeField.setValue(minutes + " min " + second + " sec");
        } else {
            second = 0;
            minutes++;
        }
    }

    public void maxWaitingFPS() {
        double time = (distanceCS / 3);
        double max = videoFPS * time;
        maxFPS = (int) max;

        oneFrameDuration = 1000 / (long) videoFPS;
    }

    
    private void updateView(Mat image) {
        imageView.setIcon(new ImageIcon(imageProcessor.toBufferedImage(image)));
    }
    
    public synchronized void count(CountVehicles countVehicles) {
        if (countVehicles.isVehicleToAdd()) {
            counter++;
            lastTSM++;
            speed.put(lastTSM, 0);
            String vehicleType = countVehicles.classifier();
            switch (vehicleType) {
                case "Car":
                    cars++;
//                    carsAmountField.setValue(cars);
                    break;
                case "Van":
                    vans++;
//                    vansAmountField.setValue(vans);
                    break;
                case "Lorry":
                    lorries++;
//                    lorriesAmountField.setValue(lorries);
                    break;
            }
            vehiclesAmountField.setValue(counter);
//            System.out.println("count: " + counter);

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

//                switch (carType) {
//                    case "Car":
//                        sumSpeedCar = sumSpeedCar + currentSpeed;
//                        double avgspeed1 = sumSpeedCar / divisorCar;
//                        divisorCar++;
//                        carsSpeedField.setValue(avgspeed1);
//                        break;
//                    case "Van":
//                        sumSpeedVan = sumSpeedVan + currentSpeed;
//                        double avgspeed2 = sumSpeedVan / divisorVan;
//                        divisorVan++;
//                        vansSpeedField.setValue(avgspeed2);
//                        break;
//                    case "Lorry":
//                        sumSpeedLorry = sumSpeedLorry + currentSpeed;
//                        double avgspeed3 = sumSpeedLorry / divisorLorry;
//                        divisorLorry++;
//                        lorriesSpeedField.setValue(avgspeed3);
//                        break;
//                }
                
//                System.out.println("currentSpeed:" + currentSpeed);
                sumSpeedVehicle += currentSpeed;
                double avgSpeed = sumSpeedVehicle / divisorVehicle;
                divisorVehicle++;
                vehiclesSpeedField.setValue(avgSpeed);

                speed.remove(firstTSM);

            } else {
                for (int i = firstTSM; i <= lastTSM; i++) {
                    if (speed.containsKey(i)) {
                        int currentFPS = speed.get(i);
                        speed.put(i, (currentFPS + 1));
                        if (currentFPS > maxFPS) {
                            speed.remove(i);

//                            String carType = cell.getContents();
//                            switch (carType) {
//                                case "Car":
//                                    cars--;
//                                    carsAmountField.setValue(cars);
//                                    break;
//                                case "Van":
//                                    vans--;
//                                    vansAmountField.setValue(vans);
//                                    break;
//                                case "Lorry":
//                                    lorries--;
//                                    lorriesAmountField.setValue(lorries);
//                                    break;
//                            }
//                            vehicles--;
                            
//                            vehiclesAmountField.setValue(vehicles);

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
    
    private void infoVehicles(JFrame frame) {
        JLabel quantityLabel = new JLabel("Quantity", JLabel.RIGHT);
        quantityLabel.setFont(new Font("defaut", Font.BOLD, 12));

        JLabel averageLabel = new JLabel("Average speed [km/h]", JLabel.RIGHT);
        averageLabel.setFont(new Font("defaut", Font.BOLD, 12));

        JLabel vehiclesLabel = new JLabel("Cars", JLabel.CENTER);
        vehiclesLabel.setFont(new Font("defaut", Font.BOLD, 12));

        NumberFormat numberFormat = NumberFormat.getNumberInstance();
        vehiclesAmountField = new JFormattedTextField(numberFormat);
        vehiclesAmountField.setValue(new Integer(0));
        vehiclesAmountField.setBackground(Color.YELLOW);
        vehiclesAmountField.setEditable(false);
        vehiclesAmountField.setPreferredSize(new Dimension(50, 20));
        vehiclesAmountField.setHorizontalAlignment(JFormattedTextField.CENTER);

        vehiclesSpeedField = new JFormattedTextField(numberFormat);
        vehiclesSpeedField.setValue(new Integer(0));
        vehiclesSpeedField.setBackground(Color.GREEN);
        vehiclesSpeedField.setEditable(false);
        vehiclesSpeedField.setPreferredSize(new Dimension(50, 20));
        vehiclesSpeedField.setHorizontalAlignment(JFormattedTextField.CENTER);

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 3;
        c.gridy = 12;
        c.gridwidth = 1;
        c.insets = new Insets(0, 70, 5, 5);
        frame.add(quantityLabel, c);

        c.gridy = 13;
        frame.add(averageLabel, c);

        c.insets = new Insets(0, 0, 5, 5);
        c.gridx = 4;
        c.gridy = 11;
        frame.add(vehiclesLabel, c);

        c.gridy = 12;
        frame.add(vehiclesAmountField, c);

        c.gridy = 13;
        frame.add(vehiclesSpeedField, c);
    }
    
    private void playPause(JFrame frame) {

        playPauseButton = new JButton("Play");
        playPauseButton.setPreferredSize(new Dimension(100, 40));

        playPauseButton.addActionListener(event -> {
            if (!isPaused) {
                isPaused = true;
                playPauseButton.setText("Play");

                loadButton.setEnabled(true);

                countingLineButton.setEnabled(true);
                speedLineButton.setEnabled(true);

            } else {
                isPaused = false;
                playPauseButton.setText("Pause");

                maxWaitingFPS();

                loadButton.setEnabled(false);

                countingLineButton.setEnabled(false);
                speedLineButton.setEnabled(false);

                frame.pack();
            }
        });
        playPauseButton.setAlignmentX(Component.LEFT_ALIGNMENT);

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(5, 10, 15, 10);
        c.gridx = 0;
        c.gridy = 3;
        c.gridwidth = 2;

        frame.add(playPauseButton, c);
    }
    
    private static ImageIcon createImageIcon(String path) {
        java.net.URL imgURL = GUI.class.getResource(path);
        if (imgURL != null) {
            return new ImageIcon(imgURL);
        } else {
            System.err.println("Couldn't find file: " + path);
            return null;
        }
    }
    
    private void loadFile(JFrame frame) {

        JTextField field = new JTextField();
        field.setText(" ");
        field.setEditable(false);

        loadButton = new JButton("Open a video", createImageIcon("/resources/Open16.gif"));

        JFileChooser fc = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Video Files", "avi", "mp4", "mpg", "mov");
        fc.setFileFilter(filter);
        fc.setCurrentDirectory(new File(System.getProperty("user.home"), "Desktop"));
        fc.setAcceptAllFileFilterUsed(false);

        loadButton.addActionListener(event -> {
            int returnVal = fc.showOpenDialog(null);

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fc.getSelectedFile();

                videoPath = file.getPath();
                field.setText(videoPath);
                capture = new VideoCapture(videoPath);
                capture.read(currentImage);
                videoFPS = capture.get(Videoio.CAP_PROP_FPS);
                resize(currentImage, currentImage, new Size(640, 360));
                updateView(currentImage);

            }
        });
        loadButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setAlignmentX(Component.LEFT_ALIGNMENT);

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(0, 5, 0, 5);
        c.gridx = 3;
        c.gridy = 0;
        c.gridwidth = 1;
        frame.add(loadButton, c);

        c.insets = new Insets(0, 0, 0, 10);
        c.gridx = 4;
        c.gridy = 0;
        c.gridwidth = 4;
        frame.add(field, c);
    }
    
    private void selectCountingLine(JFrame frame) {

        countingLineButton = new JButton("Draw a counting line");
        countingLineButton.setPreferredSize(new Dimension(120, 40));

        countingLineButton.addActionListener(event -> {
            countingLineButton.setEnabled(false);
            speedLineButton.setEnabled(false);
            mouseListenertIsActive = true;
            startDraw = false;
            imageView.addMouseListener(ml);
            imageView.addMouseMotionListener(ml2);

        });
        countingLineButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(10, 5, 10, 5);
        c.gridx = 0;
        c.gridy = 5;
        c.gridwidth = 1;

        frame.add(countingLineButton, c);
    }
    
    private void selectSpeedLine(JFrame frame) {

        speedLineButton = new JButton("Draw a speed line");
        speedLineButton.setPreferredSize(new Dimension(120, 40));

        speedLineButton.addActionListener(event -> {
            countingLineButton.setEnabled(false);
            speedLineButton.setEnabled(false);
            mouseListenertIsActive2 = true;
            startDraw = false;
            imageView.addMouseListener(ml);
            imageView.addMouseMotionListener(ml2);

        });
        speedLineButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(10, 0, 10, 5);
        c.gridx = 1;
        c.gridy = 5;
        c.gridwidth = 1;

        frame.add(speedLineButton, c);
    }
    
    private MouseListener ml = new MouseListener() {
        public void mouseClicked(MouseEvent e) {
        }

        public void mousePressed(MouseEvent e) {
            if (mouseListenertIsActive) {
                call(e.getButton(), new Point(e.getX(), e.getY()));
            } else if (mouseListenertIsActive2) {
                call2(e.getButton(), new Point(e.getX(), e.getY()));
            }
        }

        public void mouseReleased(MouseEvent e) {
        }

        public void mouseEntered(MouseEvent e) {
        }

        public void mouseExited(MouseEvent e) {
        }
    };

    private MouseMotionListener ml2 = new MouseMotionListener() {
        public void mouseDragged(MouseEvent e) {

        }

        public void mouseMoved(MouseEvent e) {
            if (mouseListenertIsActive) {
                call(e.getButton(), new Point(e.getX(), e.getY()));
            } else if (mouseListenertIsActive2) {
                call2(e.getButton(), new Point(e.getX(), e.getY()));
            }
        }
    };
    
    public void call(int event, Point point) {
        if (event == 1) {
            if (!startDraw) {
                lineCount1 = point;
                startDraw = true;
            } else {
                lineCount2 = point;
                startDraw = false;
                mouseListenertIsActive = false;
                countingLineButton.setEnabled(true);
                speedLineButton.setEnabled(true);
                imageView.removeMouseListener(ml);
                imageView.removeMouseMotionListener(ml2);
            }

        } else if (event == 0 && startDraw) {
            copiedImage = currentImage.clone();
            Imgproc.line(copiedImage, lineCount1, point, new Scalar(0, 0, 255), 1);
            if (lineSpeed1 != null && lineSpeed2 != null)
                Imgproc.line(copiedImage, lineSpeed1, lineSpeed2, new Scalar(0, 255, 0), 1);
            updateView(copiedImage);
        }
    }

    private void call2(int event, Point point) {
        if (event == 1) {
            if (!startDraw) {
                lineSpeed1 = point;
                startDraw = true;
            } else {
                lineSpeed2 = point;
                startDraw = false;
                mouseListenertIsActive2 = false;
                countingLineButton.setEnabled(true);
                speedLineButton.setEnabled(true);
                imageView.removeMouseListener(ml);
                imageView.removeMouseMotionListener(ml2);
            }

        } else if (event == 0 && startDraw) {
            copiedImage = currentImage.clone();
            Imgproc.line(copiedImage, lineSpeed1, point, new Scalar(0, 255, 0), 1);
            if (lineCount1 != null && lineCount2 != null)
                Imgproc.line(copiedImage, lineCount1, lineCount2, new Scalar(0, 0, 255), 1);
            updateView(copiedImage);
        }
    }

    private void currentTime(JFrame frame) {

        JLabel currentTimeLabel = new JLabel("Real time:", JLabel.RIGHT);
        currentTimeLabel.setFont(new Font("Arial", Font.BOLD, 12));

        currentTimeField = new JFormattedTextField();
        currentTimeField.setValue("0 sec");
        currentTimeField.setHorizontalAlignment(JFormattedTextField.CENTER);
        currentTimeField.setEditable(false);

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;

        c.gridx = 0;
        c.gridy = 13;
        c.gridwidth = 1;
        c.insets = new Insets(0, 0, 0, 20);
        frame.add(currentTimeLabel, c);
        c.insets = new Insets(0, 0, 0, 40);
        c.gridx = 1;
        frame.add(currentTimeField, c);
    }
    
    
	private void setSystemLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }
    }
}
