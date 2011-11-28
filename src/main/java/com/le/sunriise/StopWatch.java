package com.le.sunriise;

public class StopWatch {
    private long startTime;
    private long endTime;

    public StopWatch() {
        click();
    }

    public long click() {
        return click(true);
    }

    public long click(boolean reset) {
        long delta = 0L;

        endTime = System.currentTimeMillis();

        delta = endTime - startTime;

        if (reset) {
            startTime = endTime;
        }
        return delta;
    }

}
