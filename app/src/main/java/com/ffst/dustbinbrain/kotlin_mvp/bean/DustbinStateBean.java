package com.ffst.dustbinbrain.kotlin_mvp.bean;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Unique;
import org.greenrobot.greendao.annotation.Generated;

/**
 * Created by LiuJW
 * on 2021/6/15
 */
@Entity
public class DustbinStateBean {

    //  在服务器的id
    @Id
    private Long id;

    //  门板
    @Unique
    private int doorNumber;

    //  箱体类型，厨余、可回收、有害、其他
    private String dustbinBoxType;

    //  垃圾箱编号 例如A B C C D
    private String dustbinBoxNumber;

    //  当前重量 0-25000 * 10g
    private double dustbinWeight;

    //  温度 0-200 °C
    private double temperature;

    //  湿度 0 - 100%
    private double humidity;


    /**
     *
     * 人接近  1
     * 人工门关是 1
     * 测满已满是 1
     * 推杆过流卡住 1
     * 通信异常就是 1
     * 投料锁锁住了就是 1
     * 人工门锁通电 ( 也就是锁住了 ) 就是1
     *
     * */


    //  关闭失败次数
    private int closeFailNumber;
    //  电机是否开启，也就是垃圾门是否开启
    private boolean doorIsOpen;
    //  接近开关
    private boolean proximitySwitch;
    //  人工门开关
    private boolean artificialDoor;
    //  测满
    private boolean isFull = true;
    //  推杆过流
    private boolean pushRod;
    //  通信异常
    private boolean abnormalCommunication;
    //  投料锁
    private boolean deliverLock;
    //  人工门锁
    private boolean artificialDoorLock;
    @Generated(hash = 1745814078)
    public DustbinStateBean(Long id, int doorNumber, String dustbinBoxType,
            String dustbinBoxNumber, double dustbinWeight, double temperature,
            double humidity, int closeFailNumber, boolean doorIsOpen,
            boolean proximitySwitch, boolean artificialDoor, boolean isFull,
            boolean pushRod, boolean abnormalCommunication, boolean deliverLock,
            boolean artificialDoorLock) {
        this.id = id;
        this.doorNumber = doorNumber;
        this.dustbinBoxType = dustbinBoxType;
        this.dustbinBoxNumber = dustbinBoxNumber;
        this.dustbinWeight = dustbinWeight;
        this.temperature = temperature;
        this.humidity = humidity;
        this.closeFailNumber = closeFailNumber;
        this.doorIsOpen = doorIsOpen;
        this.proximitySwitch = proximitySwitch;
        this.artificialDoor = artificialDoor;
        this.isFull = isFull;
        this.pushRod = pushRod;
        this.abnormalCommunication = abnormalCommunication;
        this.deliverLock = deliverLock;
        this.artificialDoorLock = artificialDoorLock;
    }
    @Generated(hash = 1773612545)
    public DustbinStateBean() {
    }
    public Long getId() {
        return this.id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public int getDoorNumber() {
        return this.doorNumber;
    }
    public void setDoorNumber(int doorNumber) {
        this.doorNumber = doorNumber;
    }
    public String getDustbinBoxType() {
        return this.dustbinBoxType;
    }
    public void setDustbinBoxType(String dustbinBoxType) {
        this.dustbinBoxType = dustbinBoxType;
    }
    public String getDustbinBoxNumber() {
        return this.dustbinBoxNumber;
    }
    public void setDustbinBoxNumber(String dustbinBoxNumber) {
        this.dustbinBoxNumber = dustbinBoxNumber;
    }
    public double getDustbinWeight() {
        return this.dustbinWeight;
    }
    public void setDustbinWeight(double dustbinWeight) {
        this.dustbinWeight = dustbinWeight;
    }
    public double getTemperature() {
        return this.temperature;
    }
    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }
    public double getHumidity() {
        return this.humidity;
    }
    public void setHumidity(double humidity) {
        this.humidity = humidity;
    }
    public int getCloseFailNumber() {
        return this.closeFailNumber;
    }
    public void setCloseFailNumber(int closeFailNumber) {
        this.closeFailNumber = closeFailNumber;
    }
    public boolean getDoorIsOpen() {
        return this.doorIsOpen;
    }
    public void setDoorIsOpen(boolean doorIsOpen) {
        this.doorIsOpen = doorIsOpen;
    }
    public boolean getProximitySwitch() {
        return this.proximitySwitch;
    }
    public void setProximitySwitch(boolean proximitySwitch) {
        this.proximitySwitch = proximitySwitch;
    }
    public boolean getArtificialDoor() {
        return this.artificialDoor;
    }
    public void setArtificialDoor(boolean artificialDoor) {
        this.artificialDoor = artificialDoor;
    }

    /**
     * 未满为true,已满为false
     * @return
     */
    public boolean getIsFull() {
        return this.isFull;
    }
    public void setIsFull(boolean isFull) {
        this.isFull = isFull;
    }
    public boolean getPushRod() {
        return this.pushRod;
    }
    public void setPushRod(boolean pushRod) {
        this.pushRod = pushRod;
    }
    public boolean getAbnormalCommunication() {
        return this.abnormalCommunication;
    }
    public void setAbnormalCommunication(boolean abnormalCommunication) {
        this.abnormalCommunication = abnormalCommunication;
    }
    public boolean getDeliverLock() {
        return this.deliverLock;
    }
    public void setDeliverLock(boolean deliverLock) {
        this.deliverLock = deliverLock;
    }
    public boolean getArtificialDoorLock() {
        return this.artificialDoorLock;
    }
    public void setArtificialDoorLock(boolean artificialDoorLock) {
        this.artificialDoorLock = artificialDoorLock;
    }

    @Override
    public String toString() {
        return "DustbinStateBean{" +
                "id=" + id +
                ", doorNumber=" + doorNumber +
                ", dustbinBoxType='" + dustbinBoxType + '\'' +
                ", dustbinBoxNumber='" + dustbinBoxNumber + '\'' +
                ", dustbinWeight=" + dustbinWeight +
                ", temperature=" + temperature +
                ", humidity=" + humidity +
                ", closeFailNumber=" + closeFailNumber +
                ", doorIsOpen=" + doorIsOpen +
                ", proximitySwitch=" + proximitySwitch +
                ", artificialDoor=" + artificialDoor +
                ", isFull=" + isFull +
                ", pushRod=" + pushRod +
                ", abnormalCommunication=" + abnormalCommunication +
                ", deliverLock=" + deliverLock +
                ", artificialDoorLock=" + artificialDoorLock +
                '}';
    }

    public String toChineseString() {
        return "垃圾箱状态{" +
                "id=" + id +
                ", 门板号=" + doorNumber +
                ", 垃圾箱类型='" + dustbinBoxType + '\'' +
                ", 垃圾箱类型编号='" + dustbinBoxNumber + '\'' +
                ", 重量=" + dustbinWeight +
                ", 温度=" + temperature +
                ", 湿度=" + humidity +
                ", 关门失败次数=" + closeFailNumber +
                ", 是否开门=" + doorIsOpen +
                ", 人接近=" + proximitySwitch +
                ", 人工门=" + artificialDoor +
                ", 测满=" + isFull +
                ", 推杆过流=" + pushRod +
                ", 通信异常=" + abnormalCommunication +
                ", 投料口锁=" + deliverLock +
                ", 人工门锁=" + artificialDoorLock +
                '}';
    }
}
