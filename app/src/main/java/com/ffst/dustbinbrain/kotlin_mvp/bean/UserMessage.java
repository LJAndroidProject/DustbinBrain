package com.ffst.dustbinbrain.kotlin_mvp.bean;


import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Unique;

@Entity
public class UserMessage {
    @Id(autoincrement = true)
    private Long localId;

    //  服务器传过来的用户id
    @Unique
    private long userId;

    //  对应底库中的 faceToken
    @Unique
    private String faceToken;

    private long userType;

    //  上次使用时间
    private long lastUsedTime;

    //  使用次数
    private int usedNumber;

    //  人脸，注册时间
    private long registerTime;


    @Generated(hash = 1564632603)
    public UserMessage(Long localId, long userId, String faceToken, long userType,
            long lastUsedTime, int usedNumber, long registerTime) {
        this.localId = localId;
        this.userId = userId;
        this.faceToken = faceToken;
        this.userType = userType;
        this.lastUsedTime = lastUsedTime;
        this.usedNumber = usedNumber;
        this.registerTime = registerTime;
    }

    @Generated(hash = 113828295)
    public UserMessage() {
    }
    

    public Long getLocalId() {
        return this.localId;
    }

    public void setLocalId(Long localId) {
        this.localId = localId;
    }

    public long getUserId() {
        return this.userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getFaceToken() {
        return this.faceToken;
    }

    public void setFaceToken(String faceToken) {
        this.faceToken = faceToken;
    }

    public long getUserType() {
        return this.userType;
    }

    public void setUserType(long userType) {
        this.userType = userType;
    }

    public long getLastUsedTime() {
        return this.lastUsedTime;
    }

    public void setLastUsedTime(long lastUsedTime) {
        this.lastUsedTime = lastUsedTime;
    }

    public int getUsedNumber() {
        return this.usedNumber;
    }

    public void setUsedNumber(int usedNumber) {
        this.usedNumber = usedNumber;
    }

    public long getRegisterTime() {
        return this.registerTime;
    }

    public void setRegisterTime(long registerTime) {
        this.registerTime = registerTime;
    }


    @Override
    public String toString() {
        return "UserMessage{" +
                "localId=" + localId +
                ", userId=" + userId +
                ", faceToken='" + faceToken + '\'' +
                ", userType=" + userType +
                ", lastUsedTime=" + lastUsedTime +
                ", usedNumber=" + usedNumber +
                ", registerTime=" + registerTime +
                '}';
    }
}
