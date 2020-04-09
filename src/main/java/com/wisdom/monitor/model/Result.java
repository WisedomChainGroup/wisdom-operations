package com.wisdom.monitor.model;

import com.alibaba.fastjson.JSON;

public class Result<T> {
    private int code;
    private String message;
    private T data;

    public static int FAIL = 5000;
    public static int SUCCESS = 2000;

    public Result setCode(ResultCode resultCode) {
        this.code = resultCode.code();
        return this;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public Result setMessage(String message) {
        this.message = message;
        return this;
    }

    public T getData() {
        return data;
    }

    public Result setData(T data) {
        this.data = data;
        return this;
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }

    public static <U> Result<U> newFailed(String message) {
        Result<U> result = new Result<U>();
        result.setCode(ResultCode.FAIL);
        result.setMessage(message);
        return result;
    }
}
