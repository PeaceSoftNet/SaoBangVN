package net.peacesoft.commons;

import java.text.DateFormat;
import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateUtils {

    public static Date String2Date(String date, String format) {
        try {
            DateFormat formatter = new SimpleDateFormat(format);
            return formatter.parse(date);
        } catch (Exception ex) {
            return new Date();
        }
    }

    public static String Date2Format(Date date, String format) {
        Format formatter = new SimpleDateFormat(format);
        return formatter.format(date);
    }

    public static String Date2YYYYMMDD(Date date) {
        return Date2Format(date, "yyyyMMdd");
    }

    public static String Date2YYYY_MM_DD(Date date) {
        return Date2Format(date, "yyyy-MM-dd");
    }

    public static String Date2DDMM(Date date) {
        return Date2Format(date, "dd/MM");
    }

    public static int getDayOfWeek() {
        Calendar calendar = Calendar.getInstance();
        int iDay = calendar.get(Calendar.DAY_OF_WEEK);
        return iDay - 1;
    }

    public static int getDayOfWeek(String date) {
        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date byDate = formatter.parse(date);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(byDate);
            int iDay = calendar.get(Calendar.DAY_OF_WEEK);
            return iDay - 1;
        } catch (ParseException e) {
            return -1;
        }
    }

    public static int getYesterDayOfWeek() {
        int dayOfWeek = getDayOfWeek() - 1;
        if (dayOfWeek == -1) {
            dayOfWeek = 6;
        }
        return dayOfWeek;
    }

    public static Date getYesterDay() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1);
        return cal.getTime();
    }

    public static int compareDateAndDateInDay(Date fromDate, Date toDate) {
        return Math.round((toDate.getTime() - fromDate.getTime()) / 1000 / 60
                / 60 / 24);
    }

    public static void main(String[] arg) {
        System.out.println(getDayOfWeek());
        System.out.println(getYesterDay());
        System.out.println(getDayOfWeek("2011-06-06"));
    }
}
