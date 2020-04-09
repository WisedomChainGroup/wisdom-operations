package com.wisdom.monitor.model;

public enum ResultCode {
    SUCCESS(2000),//成功
    FAIL(5000),//失败
    NOT_FOUND(404),
    Warn(3000);

    private final int code;

    ResultCode(int code) {
        this.code = code;
    }

    public int code() {
        return code;
    }
}
