package org.tms;

import org.opencv.core.Core;
import java.io.IOException;

public class Application {
    static {
        try {
            System.loadLibrary(Core.NATIVE_LIBRARY_NAME); // used for tests. This library in classpath only
            System.out.println("Native library loaded.");
        } catch (UnsatisfiedLinkError e) {
            System.out.println("Error loading library");
        }
    }

//    public static void main(String[] args) throws IOException, InterruptedException {
//        GUI gui = new GUI();
//        gui.init();
//    }
}
