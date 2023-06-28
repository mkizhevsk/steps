package com.mk.steps.data.thread;

import static com.mk.steps.MainActivity.durationHandler;

import android.os.Message;
import android.util.Log;

public class DurationRunnable implements Runnable {

    public static boolean running = false;
    private static final String TAG = "MainActivity";
    private static long PAUSE_TIME = 10000;

    public void run() {
        running = true;

        try {
            while (running) {
                durationHandler.sendMessage(new Message());
                Thread.sleep(PAUSE_TIME);
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }
}
