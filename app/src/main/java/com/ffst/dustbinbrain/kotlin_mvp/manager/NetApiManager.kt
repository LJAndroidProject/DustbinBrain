package com.ffst.dustbinbrain.kotlin_mvp.manager

import com.blankj.utilcode.util.LogUtils
import com.ffst.dustbinbrain.kotlin_mvp.network.api.ApiCallBack
import com.ffst.dustbinbrain.kotlin_mvp.network.api.ApiClient
import com.ffst.dustbinbrain.kotlin_mvp.network.api.ResponseListener
import org.json.JSONObject

/**
 * Created by LiuJW
 *on 2021/6/15
 */
class NetApiManager private constructor(){
    companion object{
        @Volatile
        private var instance: NetApiManager? = null
        fun getInstance() =
            instance ?: synchronized(this) {
                instance ?: NetApiManager().also { instance = it }
            }

        val TAG = "NetApiManager";
    }

    /**
     * 获取服务器垃圾桶配置
     */
    fun getDustbinConfig(device_id:String, mange_code:String,listener: ResponseListener){
        ApiClient.getInstance().getDustbinConfig(device_id,mange_code,object : ApiCallBack{
            override fun onFinish(result: JSONObject) {
                LogUtils.dTag(TAG,"请求结果${result.toString()}")
               val code:Int = result.optInt("code")
                if (code === 1){
                    listener.onSuccess(result.toString())
                }else{
                    listener.onFail(result.toString())
                }
            }
        })

    }

    fun getBinsWorkTime(listener: ResponseListener){
        ApiClient.getInstance().getBinsWorkTime(object :ApiCallBack{
            override fun onFinish(result: JSONObject) {
                LogUtils.dTag(TAG,"请求结果${result.toString()}")
                val code:Int = result.optInt("code")
                if (code === 1){
                    listener.onSuccess(result.toString())
                }else{
                    listener.onFail(result.toString())
                }
            }
        })
    }

    fun registerTCP(tcp_client_id:String,listener: ResponseListener){
        ApiClient.getInstance().registerTCP(tcp_client_id,object :ApiCallBack{
            override fun onFinish(result: JSONObject) {
                LogUtils.dTag(TAG,"请求结果${result.toString()}")
                val code:Int = result.optInt("code")
                if (code === 1){
                    listener.onSuccess(result.toString())
                }else{
                    listener.onFail(result.toString())
                }
            }
        })
    }

    fun postFaceRegisterSuccessLog(map: MutableMap<String,String>,listener: ResponseListener){
        ApiClient.getInstance().postFaceRegisterSuccessLog(map,object : ApiCallBack{
            override fun onFinish(result: JSONObject) {
                LogUtils.dTag(TAG,"请求结果${result.toString()}")
                val code:Int = result.optInt("code")
                if (code === 1){
                    listener.onSuccess(result.toString())
                }else{
                    listener.onFail(result.toString())
                }
            }
        })
    }

}