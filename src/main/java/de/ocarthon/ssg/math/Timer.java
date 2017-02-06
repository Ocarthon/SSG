package de.ocarthon.ssg.math;

public class Timer {
    private long lastTime = 0;

    public Timer() {
    }

    public void start() {
        this.lastTime = System.currentTimeMillis();
    }

    public long next() {
        long current = System.currentTimeMillis();
        long delta = current - lastTime;
        this.lastTime = current;
        return delta;
    }
}
