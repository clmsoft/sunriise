package com.le.sunriise;

public class StopWatch {
    private long startTime;
    private long endTime;

    public StopWatch() {
        click();
    }

    public long click() {
        long delta = 0L;

        endTime = System.currentTimeMillis();

        delta = endTime - startTime;

        startTime = endTime;

        return delta;

    }
}
