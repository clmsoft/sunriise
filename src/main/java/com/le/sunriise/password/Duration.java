package com.le.sunriise.password;

import java.util.concurrent.TimeUnit;

public class Duration implements Comparable<Duration> {
    private final long days;
    private final long hours;
    private final long minutes;
    private final long seconds;
    private final long millis;
    private final long duration;

    public Duration(long duration) {
        this.duration = duration;

        long millis = duration;

        days = TimeUnit.MILLISECONDS.toDays(millis);
        millis -= TimeUnit.DAYS.toMillis(days);

        hours = TimeUnit.MILLISECONDS.toHours(millis);
        millis -= TimeUnit.HOURS.toMillis(hours);

        minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
        millis -= TimeUnit.MINUTES.toMillis(minutes);

        seconds = TimeUnit.MILLISECONDS.toSeconds(millis);
        millis -= TimeUnit.SECONDS.toMillis(seconds);

        this.millis = millis;
    }

    public String toString() {
        return String.format("%d days, %d hours, %d mins, %d secs, %d millis", days, hours, minutes, seconds, millis);
    }

    @Override
    public int compareTo(Duration o) {
        return compareDays(this, o);
    }

    private int compareDays(Duration d1, Duration d2) {
        if (d1.getDays() == d2.getDays()) {
            return compareHours(d1, d2);
        } else {
            return (int) (d1.getDays() - d2.getDays());
        }
    }

    private int compareHours(Duration d1, Duration d2) {
        if (d1.getHours() == d2.getHours()) {
            return compareMinutes(d1, d2);
        } else {
            return (int) (d1.getHours() - d2.getHours());
        }
    }

    private int compareMinutes(Duration d1, Duration d2) {
        if (d1.getMinutes() == d2.getMinutes()) {
            return compareSeconds(d1, d2);
        } else {
            return (int) (d1.getMinutes() - d2.getMinutes());
        }
    }

    private int compareSeconds(Duration d1, Duration d2) {
        if (d1.getSeconds() == d2.getSeconds()) {
            return compareMillis(d1, d2);
        } else {
            return (int) (d1.getSeconds() - d2.getSeconds());
        }
    }

    private int compareMillis(Duration d1, Duration d2) {
        return (int) (d1.getMinutes() - d2.getMinutes());
    }

    public long getDays() {
        return days;
    }

    public long getHours() {
        return hours;
    }

    public long getMinutes() {
        return minutes;
    }

    public long getSeconds() {
        return seconds;
    }

    public static String toDurationString(long millis) {
        Duration duration = new Duration(millis);
        return duration.toString();
    }

    public long getMillis() {
        return millis;
    }
}
