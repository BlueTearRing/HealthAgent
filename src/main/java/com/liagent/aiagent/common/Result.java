package com.liagent.aiagent.common;


public class Result<T> {
    private int code;
    private String msg;
    private T data;

    public Result() {
    }
    public Result(T data) {
        this.data = data;
    }


    //返回成功对象
    public static Result success() {
        Result result = new Result<>();
        result.setCode(200);
        result.setMsg("success");
        return result;
    }

    //携带数据的成功
    public static<T> Result<T> success(T data) {
        Result<T> result = new Result<>(data);
        result.setCode(200);
        result.setMsg("success");
        return result;
    }



    //失败
    public static Result error(int code, String msg) {
        Result result = new Result<>();
        result.setCode(code);
        result.setMsg(msg);
        return result;
    }

    public static <T> Result<T> error(int code, String msg, T data) {
        Result<T> result = new Result<>(data);
        result.setCode(code);
        result.setMsg(msg);
        return result;
    }



    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
