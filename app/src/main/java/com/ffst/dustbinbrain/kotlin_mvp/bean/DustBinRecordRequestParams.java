package com.ffst.dustbinbrain.kotlin_mvp.bean;

import java.util.Map;

/**
 * Created by LiuJW
 * on 2021/5/26
 */
public class DustBinRecordRequestParams {
    public Map<String,String> requestMap;

    public Map<String, String> getRequestMap() {
        return requestMap;
    }

    public void setRequestMap(Map<String, String> requestMap) {
        this.requestMap = requestMap;
    }
}
