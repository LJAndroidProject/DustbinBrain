package com.ffst.dustbinbrain.kotlin_mvp.bean;

public class StateCallBean {

    /**
     * code : 1
     * msg : 操作成功，提交条数：4，更新成功数：4
     * time : 1603779288
     * data : {"version_code":1,"version_number":"01","time":"2020-10-27 11:57:26","apk_size":87420,"update_explain":"更新了商品库","apk_download_url":"/uploads/20201027/939d6aea4c1499f275d8886951855c2e.jpg"}
     */

    private int code;
    private String msg;
    private String time;
    private DataBean data;

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

    public DataBean getData() {
        return data;
    }

    public void setData(DataBean data) {
        this.data = data;
    }

    public static class DataBean {
        /**
         * version_code : 1
         * version_number : 01
         * time : 2020-10-27 11:57:26
         * apk_size : 87420
         * update_explain : 更新了商品库
         * apk_download_url : /uploads/20201027/939d6aea4c1499f275d8886951855c2e.jpg
         */

        private int version_code;
        private String version_number;
        private String time;
        private int apk_size;
        private String update_explain;
        private String apk_download_url;
        private int eq_status;

        public int getEq_status() {
            return eq_status;
        }

        public void setEq_status(int eq_status) {
            this.eq_status = eq_status;
        }

        public int getVersion_code() {
            return version_code;
        }

        public void setVersion_code(int version_code) {
            this.version_code = version_code;
        }

        public String getVersion_number() {
            return version_number;
        }

        public void setVersion_number(String version_number) {
            this.version_number = version_number;
        }

        public String getTime() {
            return time;
        }

        public void setTime(String time) {
            this.time = time;
        }

        public int getApk_size() {
            return apk_size;
        }

        public void setApk_size(int apk_size) {
            this.apk_size = apk_size;
        }

        public String getUpdate_explain() {
            return update_explain;
        }

        public void setUpdate_explain(String update_explain) {
            this.update_explain = update_explain;
        }

        public String getApk_download_url() {
            return apk_download_url;
        }

        public void setApk_download_url(String apk_download_url) {
            this.apk_download_url = apk_download_url;
        }
    }
}
