package com.logistics.common;

import lombok.Data;

/**
 * 统一响应封装
 */
@Data
public class Result<T> {

    private int code;
    private String msg;
    private T data;
    private Long total;

    private Result() {
    }

    public static <T> Result<T> ok() {
        return ok(null);
    }

    public static <T> Result<T> ok(T data) {
        Result<T> r = new Result<>();
        r.setCode(200);
        r.setMsg("操作成功");
        r.setData(data);
        return r;
    }

    public static <T> Result<T> ok(T data, Long total) {
        Result<T> r = ok(data);
        r.setTotal(total);
        return r;
    }

    public static <T> Result<T> fail(String msg) {
        return fail(400, msg);
    }

    public static <T> Result<T> fail(int code, String msg) {
        Result<T> r = new Result<>();
        r.setCode(code);
        r.setMsg(msg);
        return r;
    }
}
