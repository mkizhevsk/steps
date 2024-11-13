package com.mk.steps.data.thread;

import static com.mk.steps.ui.MainActivity.durationHandler;

import android.os.Message;
import android.util.Log;

public class DurationRunnable implements Runnable {

    public static boolean running = false;
    private static final String TAG = "myLogs";
    private static long PAUSE_TIME = 1000;

    public void run() {
        Log.d(TAG, "DurationRunnable run");
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
