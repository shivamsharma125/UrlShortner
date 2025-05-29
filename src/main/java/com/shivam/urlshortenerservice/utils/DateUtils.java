package com.shivam.urlshortenerservice.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtils {
    private static final SimpleDateFormat formatterDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final SimpleDateFormat formatterDate = new SimpleDateFormat("yyyy-MM-dd");

    public static String formatDate(Date date) {
        if (date == null) return null;
        return formatterDateTime.format(date);
    }

    public static boolean validDate(String date) {
        if (StringUtils.isEmpty(date)) return false;
        try {
            formatterDate.parse(date);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }

    public static Date formatDate(String date) throws ParseException {
        return formatterDate.parse(date);
    }

    public static boolean isDateRangeValid(String startDate, String endDate) {
        try {
            Date start = formatDate(startDate);
            Date end = formatDate(endDate);
            return start.before(end);
        } catch (ParseException e){
            return false;
        }
    }
}
