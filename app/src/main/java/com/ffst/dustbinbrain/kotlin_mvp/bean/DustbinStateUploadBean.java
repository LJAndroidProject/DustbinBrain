package com.ffst.dustbinbrain.kotlin_mvp.bean;

import java.util.List;

public class DustbinStateUploadBean {

    /**
     * time :
     * sign :
     * list : [{"dustbinWeight":0,"id":34,"isFull":false,"temperature":0},{"dustbinWeight":0,"id":35,"isFull":false,"temperature":0},{"dustbinWeight":0,"id":36,"isFull":false,"temperature":0},{"dustbinWeight":0,"id":37,"isFull":false,"temperature":0}]
     */

    private String timestamp;
    private String sign;
    private int apk_type;
    private String device_id;
    private int version_code;
    private List<ListBean> list;

    public int getApk_type() {
        return apk_type;
    }

    public String getDevice_id() {
        return device_id;
    }

    public void setDevice_id(String device_id) {
        this.device_id = device_id;
    }

    public void setApk_type(int apk_type) {
        this.apk_type = apk_type;
    }

    public int getVersion_code() {
        return version_code;
    }

    public void setVersion_code(int version_code) {
        this.version_code = version_code;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public List<ListBean> getList() {
        return list;
    }

    public void setList(List<ListBean> list) {
        this.list = list;
    }

    public static class ListBean {
        /**
         * dustbinWeight : 0
         * id : 34
         * isFull : false
         * temperature : 0
         */

        private double dustbinWeight;
        private long id;
        private boolean isFull;
        private double temperature;

        public ListBean(double dustbinWeight, long id, boolean isFull, double temperature) {
            this.dustbinWeight = dustbinWeight;
            this.id = id;
            this.isFull = isFull;
            this.temperature = temperature;
        }

        public double getDustbinWeight() {
            return dustbinWeight;
        }

        public void setDustbinWeight(double dustbinWeight) {
            this.dustbinWeight = dustbinWeight;
        }

        public long getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public boolean isIsFull() {
            return isFull;
        }

        public void setIsFull(boolean isFull) {
            this.isFull = isFull;
        }

        public double getTemperature() {
            return temperature;
        }

        public void setTemperature(double temperature) {
            this.temperature = temperature;
        }
    }
}
