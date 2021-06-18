package com.ffst.dustbinbrain.kotlin_mvp.network.api

import org.json.JSONObject

/**
 * Created by LiuJW
 *on 2021/6/15
 */
interface ApiCallBack {
    fun onFinish(result:JSONObject)

    fun onFinish(result:String){

    }
}