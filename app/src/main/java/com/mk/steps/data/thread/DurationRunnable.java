package com.mk.steps.data.thread;

import static com.mk.steps.MainActivity.durationHandler;

import android.os.Message;
import android.util.Log;

public class DurationRunnable implements Runnable {

    public static boolean running = false;
    private static final String TAG = "MainActivity";

    public void run() {
        running = true;

        try {
            while (running) {
                durationHandler.sendMessage(new Message());
                Thread.sleep(10000);
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }
}
