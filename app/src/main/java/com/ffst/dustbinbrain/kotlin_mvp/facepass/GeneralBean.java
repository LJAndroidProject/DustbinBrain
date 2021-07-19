package com.ffst.dustbinbrain.kotlin_mvp.facepass;

/**
 * 通用 Bean
 * */
public class GeneralBean {

    /**
     * code : 1
     * msg : 验证码发送成功
     * time : 1603331416
     * data : null
     */

    private int code;
    private String msg;
    private String time;
    private String data;

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

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
