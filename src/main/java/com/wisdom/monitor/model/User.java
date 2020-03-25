package com.wisdom.monitor.model;

public class User {
    private String name;
    private int type;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public User(String name,int type){
        this.name = name;
        this.type = type;
    }
}
