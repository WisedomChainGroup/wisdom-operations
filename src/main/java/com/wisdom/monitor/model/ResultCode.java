package com.wisdom.monitor.model;

public enum ResultCode {
    SUCCESS(200),//成功
    FAIL(400),//失败
    NOT_FOUND(404),
    Warn(300);

    private final int code;

    ResultCode(int code) {
        this.code = code;
    }

    public int code() {
        return code;
    }
}
