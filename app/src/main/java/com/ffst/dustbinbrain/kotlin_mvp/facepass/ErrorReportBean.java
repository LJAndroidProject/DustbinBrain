package com.ffst.dustbinbrain.kotlin_mvp.facepass;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;

/**
 * 错误上报
 * */
@Entity
public class ErrorReportBean {
    @Id
    private Long errorId;

    //  指令编号
    private String orderNumber;

    //  门板编号
    private int doorNumber;

    //  数据位
    private String data;

    //  原始命令
    private String orderString;

    //  设备id
    private String deviceId;

    //  时间
    private long time;

    //  描述
    private String msg;

    @Generated(hash = 1276030179)
    public ErrorReportBean(Long errorId, String orderNumber, int doorNumber,
            String data, String orderString, String deviceId, long time,
            String msg) {
        this.errorId = errorId;
        this.orderNumber = orderNumber;
        this.doorNumber = doorNumber;
        this.data = data;
        this.orderString = orderString;
        this.deviceId = deviceId;
        this.time = time;
        this.msg = msg;
    }

    @Generated(hash = 1444523521)
    public ErrorReportBean() {
    }

    public Long getErrorId() {
        return this.errorId;
    }

    public void setErrorId(Long errorId) {
        this.errorId = errorId;
    }

    public String getOrderNumber() {
        return this.orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public int getDoorNumber() {
        return this.doorNumber;
    }

    public void setDoorNumber(int doorNumber) {
        this.doorNumber = doorNumber;
    }

    public String getData() {
        return this.data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getOrderString() {
        return this.orderString;
    }

    public void setOrderString(String orderString) {
        this.orderString = orderString;
    }

    public String getDeviceId() {
        return this.deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public long getTime() {
        return this.time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getMsg() {
        return this.msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }


    @Override
    public String toString() {
        return "ErrorReportBean{" +
                "errorId=" + errorId +
                ", orderNumber='" + orderNumber + '\'' +
                ", doorNumber=" + doorNumber +
                ", data='" + data + '\'' +
                ", orderString='" + orderString + '\'' +
                ", deviceId='" + deviceId + '\'' +
                ", time=" + time +
                ", msg='" + msg + '\'' +
                '}';
    }
}
