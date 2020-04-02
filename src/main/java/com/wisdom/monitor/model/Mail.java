package com.wisdom.monitor.model;

public class Mail {
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

    public Mail() {
    }

    public Mail(String sender, String receiver) {
        this.sender = sender;
        this.receiver = receiver;
    }

    private String sender;
    private String receiver;
}
