package org.tensorflow.demo.env;

import android.os.SystemClock;

public class SplitTimer {
    private long lastCpuTime;
    private long lastWallTime;
    private final Logger logger;

    public SplitTimer(String name) {
        this.logger = new Logger(name);
        newSplit();
    }

    public void newSplit() {
        this.lastWallTime = SystemClock.uptimeMillis();
        this.lastCpuTime = SystemClock.currentThreadTimeMillis();
    }

    public void endSplit(String splitName) {
        long currWallTime = SystemClock.uptimeMillis();
        long currCpuTime = SystemClock.currentThreadTimeMillis();
        this.logger.mo6294i("%s: cpu=%dms wall=%dms", splitName, Long.valueOf(currCpuTime - this.lastCpuTime), Long.valueOf(currWallTime - this.lastWallTime));
        this.lastWallTime = currWallTime;
        this.lastCpuTime = currCpuTime;
    }
}
