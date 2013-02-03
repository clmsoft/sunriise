/*******************************************************************************
 * Copyright (c) 2010 Hung Le
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *******************************************************************************/
package com.le.sunriise.password.timing;

import java.util.concurrent.TimeUnit;

public class Duration implements Comparable<Duration> {
    public static final long DAYS_IN_YEAR = 365L;

    private static final long MILLISECONDS = 1000L;
    private static final long SECONDS = 60L;
    private static final long MINUTES = 60L;
    private static final long HOURS = 24L;

    public static final long MILLISECONDS_PER_SECOND = MILLISECONDS;
    public static final long MILLISECONDS_PER_MINUTE = MILLISECONDS_PER_SECOND * SECONDS;
    public static final long MILLISECONDS_PER_HOUR = MILLISECONDS_PER_MINUTE * MINUTES;
    public static final long MILLISECONDS_PER_DAY = MILLISECONDS_PER_HOUR * HOURS;

    private long years;
    private long days;
    private final long hours;
    private final long minutes;
    private final long seconds;
    private final long millis;
    private final long duration;

    private static final String[] FULL_UNIT_NAME = { "years", "days", "hours", "mins", "secs", "millis", };
    private static final String[] SHORT_UNIT_NAME = { "y", "d", "h", "m", "s", "ms", };

    public Duration(long duration) {
        this.duration = duration;

        long millis = duration;

        years = 0L;

        days = TimeUnit.MILLISECONDS.toDays(millis);
        millis -= TimeUnit.DAYS.toMillis(days);

        if (days > DAYS_IN_YEAR) {
            years = days / 365L;
            days = days % 365L;
        }

        hours = TimeUnit.MILLISECONDS.toHours(millis);
        millis -= TimeUnit.HOURS.toMillis(hours);

        minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
        millis -= TimeUnit.MINUTES.toMillis(minutes);

        seconds = TimeUnit.MILLISECONDS.toSeconds(millis);
        millis -= TimeUnit.SECONDS.toMillis(seconds);

        this.millis = millis;
    }

    public String toString(boolean compact) {
        boolean shortName = true;
        String[] unitNames = null;
        if (shortName) {
            unitNames = SHORT_UNIT_NAME;
        } else {
            unitNames = FULL_UNIT_NAME;
        }
        if (compact) {
            StringBuilder sb = new StringBuilder();
            int n = 0;

            n = appendIfPositive(sb, years, unitNames[0], n);
            n = appendIfPositive(sb, days, unitNames[1], n);
            n = appendIfPositive(sb, hours, unitNames[2], n);
            n = appendIfPositive(sb, minutes, unitNames[3], n);
            n = appendIfPositive(sb, seconds, unitNames[4], n);
            n = appendIfPositive(sb, millis, unitNames[5], n);

            return sb.toString();
        } else {
            return String.format("%d " + unitNames[0] + "," + " %d " + unitNames[1] + "," + " %d " + unitNames[2] + "," + " %d "
                    + unitNames[3] + "," + " %d " + unitNames[4] + "," + " %d " + unitNames[5], years, days, hours, minutes,
                    seconds, millis);
        }
    }

    private int appendIfPositive(StringBuilder sb, long value, String label, int n) {
        return appendIfPositive(sb, value, label, n, null);
    }

    private int appendIfPositive(StringBuilder sb, long value, String label, int n, String sep) {
        if (value > 0) {
            if (n > 0) {
                sb.append(" ");
            }
            if (sep != null) {
                sb.append(String.format("%d" + sep + label, value));
            } else {
                sb.append(String.format("%d" + label, value));
            }
            n++;
        }
        return n;
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

    @Override
    public String toString() {
        return toString(true);
    }

    public long getYears() {
        return years;
    }

    public void setYears(long years) {
        this.years = years;
    }

    public long getDuration() {
        return duration;
    }

    public void setDays(long days) {
        this.days = days;
    }
}
