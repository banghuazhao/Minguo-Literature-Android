package com.appsbay.minguoliteratural;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.appsbay.minguoliteratural.Tools.AdvertisingAge;

import org.junit.Test;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class AdvertisingAgeTest {
    private final Calendar today = new GregorianCalendar(2026, Calendar.OCTOBER, 3);

    @Test
    public void eligibilityBeginsOnTwentyFirstBirthday() {
        assertTrue(AdvertisingAge.mayRequestAds(2005, 10, 3, today));
        assertFalse(AdvertisingAge.mayRequestAds(2005, 10, 4, today));
        assertFalse(AdvertisingAge.mayRequestAds(2009, 10, 3, today));
    }

    @Test
    public void invalidAndFutureDatesNeverPermitAds() {
        assertFalse(AdvertisingAge.isValidBirthDate(2005, 2, 29, today));
        assertFalse(AdvertisingAge.isValidBirthDate(2027, 1, 1, today));
        assertFalse(AdvertisingAge.mayRequestAds(0, 0, 0, today));
    }
}
