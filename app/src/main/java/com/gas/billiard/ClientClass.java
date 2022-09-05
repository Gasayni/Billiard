package com.gas.billiard;

public class ClientClass {
    private final int id;
    private final String name;
    private final String phone;
    private final int ordersCount;
    private final int durationSumMinute;

    public ClientClass(int id, String name, String phone, int ordersCount, int durationSumMinute) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.ordersCount = ordersCount;
        this.durationSumMinute = durationSumMinute;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    public int getOrdersCount() {
        return ordersCount;
    }

    public int getDurationSumMinute() {
        return durationSumMinute;
    }
}
