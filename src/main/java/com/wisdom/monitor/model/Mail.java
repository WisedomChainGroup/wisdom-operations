package com.wisdom.monitor.model;

public class Mail {
    private String sender;
    private String receiver;
    private String password;

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Mail() {
    }

    public Mail(String sender, String receiver, String password) {
        this.sender = sender;
        this.receiver = receiver;
        this.password = password;
    }
}
