package com.wisdom.monitor.model;

public class WDCInfo {
    private String last;
    private String target;
    private String averageBlockInterval;
    private String averageFee;
    private String pendingTransactions;
    private String queuedTransactions;
    private String lastConfirmedHeight;
    private String blocksCount;
    private String price;

    public String getPrice() {
        return (price != null) ? price : "";
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getLast() {
        return (last != null) ? last : "";
    }

    public void setLast(String last) {
        this.last = last;
    }

    public String getTarget() {
        return (target != null) ? target : "";
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getAverageBlockInterval() {
        return (averageBlockInterval != null) ? averageBlockInterval : "0";
    }

    public void setAverageBlockInterval(String averageBlockInterval) {
        this.averageBlockInterval = averageBlockInterval;
    }

    public String getAverageFee() {
        return (averageFee != null) ? averageFee : "";
    }

    public void setAverageFee(String averageFee) {
        this.averageFee = averageFee;
    }

    public String getPendingTransactions() {
        return (pendingTransactions != null) ? pendingTransactions : "0";
    }

    public void setPendingTransactions(String pendingTransactions) {
        this.pendingTransactions = pendingTransactions;
    }

    public String getQueuedTransactions() {
        return (queuedTransactions != null) ? queuedTransactions : "0";
    }

    public void setQueuedTransactions(String queuedTransactions) {
        this.queuedTransactions = queuedTransactions;
    }

    public String getLastConfirmedHeight() {
        return (lastConfirmedHeight != null) ? lastConfirmedHeight : "0";
    }

    public void setLastConfirmedHeight(String lastConfirmedHeight) {
        this.lastConfirmedHeight = lastConfirmedHeight;
    }

    public String getBlocksCount() {
        return (blocksCount != null) ? blocksCount : "0";
    }

    public void setBlocksCount(String blocksCount) {
        this.blocksCount = blocksCount;
    }
}
