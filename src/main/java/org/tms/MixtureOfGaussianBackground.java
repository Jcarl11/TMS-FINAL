package org.tms;
import org.opencv.core.Mat;
import org.opencv.video.BackgroundSubtractorMOG2;

public class MixtureOfGaussianBackground implements VideoProcessor {

    private BackgroundSubtractorMOG2 mog;
    private Mat foreground = new Mat();
    private double learningRate = 0.01;

    public MixtureOfGaussianBackground(double imageThreshold, int history) {

        mog = org.opencv.video.Video.createBackgroundSubtractorMOG2(history, imageThreshold, true);
        mog.setShadowValue(0);
    }


    public Mat process(Mat inputImage) {

        mog.apply(inputImage, foreground, learningRate);

        return foreground;
    }

    public void setImageThreshold(double imageThreshold) {
        mog.setVarThreshold(imageThreshold);
        mog.setVarThresholdGen(imageThreshold);
//        System.out.println(mog.getVarInit());
//        mog.setVarInit(1);
    }

    public void setHistory(int history) {
        mog.setHistory(history);
    }
}