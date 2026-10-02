package com.appsbay.minguoliteratural.Tools;

import java.util.Calendar;
import java.util.GregorianCalendar;

/** Local age decision. Unknown ages and users below 21 never enter the ads flow. */
public final class AdvertisingAge {
    private AdvertisingAge() {
    }

    public static boolean isValidBirthDate(int year, int month, int day, Calendar today) {
        if (year < 1900 || month < 1 || month > 12 || day < 1 || day > 31) {
            return false;
        }
        Calendar birth = new GregorianCalendar(year, month - 1, day);
        birth.setLenient(false);
        try {
            birth.getTimeInMillis();
        } catch (IllegalArgumentException invalidDate) {
            return false;
        }
        return !birth.after(today);
    }

    public static boolean mayRequestAds(int year, int month, int day, Calendar today) {
        if (!isValidBirthDate(year, month, day, today)) {
            return false;
        }
        int age = today.get(Calendar.YEAR) - year;
        if (today.get(Calendar.MONTH) + 1 < month
                || (today.get(Calendar.MONTH) + 1 == month
                && today.get(Calendar.DAY_OF_MONTH) < day)) {
            age--;
        }
        return age >= 21;
    }
}
