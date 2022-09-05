package com.gas.billiard;

public class AdminClass {
    private int id;
    private String name;
    private String phone;
    private String pass;

    public AdminClass(int id, String name, String phone, String pass) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.pass = pass;
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

    public String getPass() {
        return pass;
    }
}
