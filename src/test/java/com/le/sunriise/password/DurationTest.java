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

package com.le.sunriise.password;

import junit.framework.Assert;

import org.junit.Test;

import com.le.sunriise.password.timing.Duration;

public class DurationTest {

    @Test
    public void test() {
        long days = 0;
        long hours = 0;
        long minutes = 0;
        long seconds = 0;
        long millis = 0;

        testDuration(days, hours, minutes, seconds, millis);

        days = 1;
        hours = 0;
        minutes = 0;
        seconds = 0;
        millis = 0;
        testDuration(days, hours, minutes, seconds, millis);

        days = 0;
        hours = 1;
        minutes = 0;
        seconds = 0;
        millis = 0;
        testDuration(days, hours, minutes, seconds, millis);

        days = 0;
        hours = 0;
        minutes = 1;
        seconds = 0;
        millis = 0;
        testDuration(days, hours, minutes, seconds, millis);

        days = 0;
        hours = 0;
        minutes = 0;
        seconds = 1;
        millis = 0;
        testDuration(days, hours, minutes, seconds, millis);

        days = 0;
        hours = 0;
        minutes = 0;
        seconds = 0;
        millis = 1;
        testDuration(days, hours, minutes, seconds, millis);

        days = 1;
        hours = 1;
        minutes = 1;
        seconds = 1;
        millis = 1;
        testDuration(days, hours, minutes, seconds, millis);

        days = 1;
        hours = 2;
        minutes = 3;
        seconds = 4;
        millis = 5;
        testDuration(days, hours, minutes, seconds, millis);

        days = 0;
        hours = 0;
        minutes = 0;
        seconds = 0;
        millis = 999;
        testDuration(days, hours, minutes, seconds, millis);

        days = 0;
        hours = 0;
        minutes = 0;
        seconds = 59;
        millis = 999;
        testDuration(days, hours, minutes, seconds, millis);

        days = 0;
        hours = 0;
        minutes = 59;
        seconds = 59;
        millis = 999;
        testDuration(days, hours, minutes, seconds, millis);

        days = 0;
        hours = 23;
        minutes = 59;
        seconds = 59;
        millis = 999;
        testDuration(days, hours, minutes, seconds, millis);

        days = 999;
        hours = 23;
        minutes = 59;
        seconds = 59;
        millis = 999;
        testDuration(days, hours, minutes, seconds, millis);

    }

    private void testDuration(long days, long hours, long minutes, long seconds, long millis) {
        Duration d = new Duration(calculateDuration(days, hours, minutes, seconds, millis));

        long years = days / Duration.DAYS_IN_YEAR;
        days = days % Duration.DAYS_IN_YEAR;

        Assert.assertEquals(years, d.getYears());
        Assert.assertEquals(days, d.getDays());
        Assert.assertEquals(hours, d.getHours());
        Assert.assertEquals(minutes, d.getMinutes());
        Assert.assertEquals(seconds, d.getSeconds());
        Assert.assertEquals(millis, d.getMillis());
    }

    private long calculateDuration(long days, long hours, long minutes, long seconds, long millis) {
        // TODO: does not check valid range.
        // hours: 0-23
        return ((days * 24 * 60 * 60 * 1000) + (hours * 60 * 60 * 1000) + (minutes * 60 * 1000) + (seconds * 1000) + millis);
    }

}
