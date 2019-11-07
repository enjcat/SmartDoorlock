package com.example.smartdoorlock;

public class singleton {
    private static final singleton instance = new singleton();
    public String URI;
    public String uid;
    public String name;
    public String role;
    public String perm;

    private singleton() {}

    public static singleton getInstance() {
        return instance;
    }
}
