package com.gas.billiard;

import lombok.Data;

public class ReserveTable {
    private int idOrder;
    private int numTable;

    private String date;
    private String time;
    private int hour;
    private int minute;
    private int duration;

    private String client;
    private String employee;
    private String tariff;

    public ReserveTable(int idOrder, int numTable, String date, String time, int duration, String client, String employee, String tariff) {
        this.idOrder = idOrder;
        this.numTable = numTable;
        this.date = date;
        this.time = time;
        this.duration = duration;
        this.client = client;
        this.employee = employee;
        this.tariff = tariff;
    }

    public int getHour() {
        String[] timeArr = time.split(":");
        return Integer.parseInt(timeArr[0]);
    }

    public int getMinute() {
        String[] timeArr = time.split(":");
        return Integer.parseInt(timeArr[1]);
    }

    public int getIdOrder() {
        return idOrder;
    }

    public int getNumTable() {
        return numTable;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
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

    public String getTariff() {
        return tariff;
    }
}
