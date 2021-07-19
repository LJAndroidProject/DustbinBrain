package com.ffst.dustbinbrain.kotlin_mvp.facepass;

public class ImageUploadResult
{
    /**
     * code : 1
     * msg : 上传成功
     * time : 1601257128
     * data : http://ffuserupload.oss-cn-shenzhen.aliyuncs.com/userupload/other/20200928/c431658a5b38930131ef99a297383453516f63bb.jpg
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

    @Override
    public String toString() {
        return "ImageUploadResult{" +
                "code=" + code +
                ", msg='" + msg + '\'' +
                ", time='" + time + '\'' +
                ", data='" + data + '\'' +
                '}';
    }
}
