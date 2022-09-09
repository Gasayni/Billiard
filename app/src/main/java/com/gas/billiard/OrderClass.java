package com.gas.billiard;

import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class OrderClass {
    private final SimpleDateFormat dateTimeFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.ENGLISH);
    Calendar startDateTimeReserveCal = Calendar.getInstance();
    private Calendar endDateTimeReserveCal = Calendar.getInstance();
    String reserveFinishTimeStr;
    Date reserveStartDateTime = new Date();

    private int idOrder;
    private int numTable;

    private final String dateStartReserve;
    private final String timeStartReserve;

    private String dateEndReserve;
    private String timeEndReserve;
    private Date dateTimeStartReserve;
    private String dateOrder;
    private String timeOrder;

    private String bron;
    private String status;
    private int hourStartReserve;
    private int minuteStartReserve;
    private int hourEndReserve;
    private int minuteEndReserve;
    private int duration;

    private String client;
    private String employee;

    public OrderClass(int idOrder,
                      int numTable,
                      String dateStartReserve,
                      String timeStartReserve,
                      int duration,
                      String dateOrder,
                      String timeOrder,
                      String client,
                      String employee,
                      String bron,
                      String status) {
        this.idOrder = idOrder;
        this.numTable = numTable;
        this.dateStartReserve = dateStartReserve;
        this.timeStartReserve = timeStartReserve;
        this.dateOrder = dateOrder;
        this.timeOrder = timeOrder;
        this.duration = duration;
        this.client = client;
        this.employee = employee;
        this.bron = bron;
        this.status = status;
        this.endDateTimeReserveCal = getEndReserveDateTimeMethod();
    }

    private Calendar getEndReserveDateTimeMethod() {
        try {
            reserveStartDateTime = dateTimeFormat.parse(dateStartReserve + " " + timeStartReserve); // из строки в Date
        } catch (ParseException e) {
            e.printStackTrace();
        }

        assert reserveStartDateTime != null;
        endDateTimeReserveCal.setTime(reserveStartDateTime);
        endDateTimeReserveCal.add(Calendar.MINUTE, duration);
        return endDateTimeReserveCal;
    }

    public int getHourStartReserve() {
        String[] timeArr = timeStartReserve.split(":");
        return Integer.parseInt(timeArr[0]);
    }

    public int getMinuteStartReserve() {
        String[] timeArr = timeStartReserve.split(":");
        return Integer.parseInt(timeArr[1]);
    }

    public int getIdOrder() {
        return idOrder;
    }

    public int getNumTable() {
        return numTable;
    }

    public String getDateStartReserve() {
        return dateStartReserve;
    }

    public String getTimeStartReserve() {
        return timeStartReserve;
    }

    public int getDuration() {
        return duration;
    }

    public String getClient() {
        return client;
    }

    public String getEmployee() {
        return employee;
    }

    public String getDateOrder() {
        return dateOrder;
    }

    public String getTimeOrder() {
        return timeOrder;
    }

    public String getDateEndReserve() {
        String myMonthSt, myDaySt;
        if (endDateTimeReserveCal.get(Calendar.MONTH) < 10)
            myMonthSt = "0" + (endDateTimeReserveCal.get(Calendar.MONTH) + 1);
        else myMonthSt = "" + (endDateTimeReserveCal.get(Calendar.MONTH) + 1);
        if (endDateTimeReserveCal.get(Calendar.DAY_OF_MONTH) < 10)
            myDaySt = "0" + endDateTimeReserveCal.get(Calendar.DAY_OF_MONTH);
        else myDaySt = "" + endDateTimeReserveCal.get(Calendar.DAY_OF_MONTH);

        return myDaySt + "." + myMonthSt + "." + endDateTimeReserveCal.get(Calendar.YEAR);
    }

    public String getTimeEndReserve() {
        String hourReserveSt, minuteReserveSt;

        if (endDateTimeReserveCal.get(Calendar.HOUR_OF_DAY) < 10)
            hourReserveSt = "0" + endDateTimeReserveCal.get(Calendar.HOUR_OF_DAY);
        else hourReserveSt = "" + endDateTimeReserveCal.get(Calendar.HOUR_OF_DAY);
        if (endDateTimeReserveCal.get(Calendar.MINUTE) < 10)
            minuteReserveSt = "0" + endDateTimeReserveCal.get(Calendar.MINUTE);
        else minuteReserveSt = "" + endDateTimeReserveCal.get(Calendar.MINUTE);

        return hourReserveSt + ":" + minuteReserveSt;
    }

    public int getHourEndReserve() {
        return hourEndReserve;
    }

    public int getMinuteEndReserve() {
        return minuteEndReserve;
    }

    public Calendar getStartDateTimeReserveCal() {
        try {
            reserveStartDateTime = dateTimeFormat.parse(dateStartReserve + " " + timeStartReserve); // из строки в Date
        } catch (ParseException e) {
            e.printStackTrace();
        }
        startDateTimeReserveCal.setTime(reserveStartDateTime);
        return startDateTimeReserveCal;
    }

    public Calendar getEndDateTimeReserveCal() {
        return endDateTimeReserveCal;
    }

    public String getBron() {
        return bron;
    }

    public String getStatus() {
        return status;
    }


}
