package com.ffst.dustbinbrain.kotlin_mvp.network.api

import android.text.TextUtils
import com.blankj.utilcode.util.LogUtils
import com.ffst.dustbinbrain.kotlin_mvp.constants.CommonConstants
import com.ffst.dustbinbrain.kotlin_mvp.manager.NetApiManager
import com.ffst.dustbinbrain.kotlin_mvp.manager.ThreadManager
import com.ffst.dustbinbrain.kotlin_mvp.utils.FenFenCommonUtil
import com.tencent.mmkv.MMKV
import okhttp3.ResponseBody
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*


/**
 * Created by LiuJW
 *on 2021/6/15
 */
class ApiClient private constructor() {
    //伴生对象代替java 静态static
    companion object {
        private val TAG = NetApiManager.TAG

        // == java双重校验锁式
        @Volatile
        private var instance: ApiClient? = null
        fun getInstance() =
            instance ?: synchronized(this) {
                instance ?: ApiClient().also { instance = it }
            }

        private val mmkv = MMKV.defaultMMKV()
    }

    /**
     * 获取服务器垃圾桶配置
     */
    fun getDustbinConfig(device_id: String, mange_code: String, apiCallBack: ApiCallBack) {
        val url: String = CommonConstants.IP
        ThreadManager.getInstance().execute {
            val map = getPostParams()
            map.put("device_id", device_id)
            map.put("mange_code", mange_code)
            RetrofitClient.buildClient(url)!!.create(ServerAPI::class.java).getDustbinConfig(map)
                .enqueue(object : Callback<ResponseBody?> {
                    override fun onResponse(
                        call: Call<ResponseBody?>?,
                        response: Response<ResponseBody?>?
                    ) {
                        LogUtils.dTag(TAG, response.toString())
                        jsonResponseAndCallBack(apiCallBack, response)
                    }

                    override fun onFailure(call: Call<ResponseBody?>?, t: Throwable?) {
                        jsonResponseAndCallBack(apiCallBack, null, t)
                    }
                })
        }
    }

    /**
     * 获取垃圾投放时间
     */
    fun getBinsWorkTime(apiCallBack: ApiCallBack) {
        val url: String = CommonConstants.IP
        ThreadManager.getInstance().execute {
            RetrofitClient.buildClient(url)!!.create(ServerAPI::class.java).getBinWorkTime()
                .enqueue(object : Callback<ResponseBody> {
                    override fun onResponse(
                        call: Call<ResponseBody?>?,
                        response: Response<ResponseBody?>?
                    ) {
                        LogUtils.dTag(TAG, response.toString())
                        jsonResponseAndCallBack(apiCallBack, response)
                    }

                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        jsonResponseAndCallBack(apiCallBack, null, t)
                    }

                })
        }
    }

    fun registerTCP(tcp_client_id: String, apiCallBack: ApiCallBack) {
        val url: String = CommonConstants.IP
        ThreadManager.getInstance().execute {
            val map = getPostParams()
            map.put("tcp_client_id", tcp_client_id)
            RetrofitClient.buildClient(url)!!.create(ServerAPI::class.java).registerTCP(map)
                .enqueue(object : Callback<ResponseBody> {
                    override fun onResponse(
                        call: Call<ResponseBody>,
                        response: Response<ResponseBody?>?
                    ) {
                        LogUtils.dTag(TAG, response.toString())
                        jsonResponseAndCallBack(apiCallBack, response)
                    }

                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        jsonResponseAndCallBack(apiCallBack, null, t)
                    }

                })
        }
    }

    fun postFaceRegisterSuccessLog(map: MutableMap<String, String>, apiCallBack: ApiCallBack) {
        val url: String = CommonConstants.IP
        ThreadManager.getInstance().execute{
            val param = getPostParams()
            map.putAll(map)
            RetrofitClient.buildClient(url)!!.create(ServerAPI::class.java).postFaceRegisterSuccessLog(map).enqueue(object : Callback<ResponseBody>{
                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody?>?
                ) {
                    LogUtils.dTag(TAG, response.toString())
                    jsonResponseAndCallBack(apiCallBack, response)
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    jsonResponseAndCallBack(apiCallBack, null, t)
                }

            })
        }
    }

    //获取Post必须携带参数
    private fun getPostParams(): MutableMap<String, String> {
        //  当前时间
        val nowTime = System.currentTimeMillis() / 1000
        val sign = FenFenCommonUtil.md5(nowTime.toString() + FenFenCommonUtil.key)
            ?.uppercase(Locale.getDefault())
        val device_id = mmkv?.decodeString("device_id")
        var map: MutableMap<String, String> = mutableMapOf(
            "sign" to sign.toString(),
            "timestamp" to nowTime.toString()
        )
        if (!TextUtils.isEmpty(device_id)) {
            map.put("device_id", device_id.toString())
        }
        return map
    }

    // 所有返回信息为json的接口,回调信息统一处理.
    private fun jsonResponseAndCallBack(
        apiCallBack: ApiCallBack,
        response: Response<ResponseBody?>?
    ) {
        jsonResponseAndCallBack(apiCallBack, response, null)
    }


    // 所有返回信息为json的接口,回调信息统一处理.
    private fun jsonResponseAndCallBack(
        apiCallBack: ApiCallBack,
        response: Response<ResponseBody?>?,
        t: Throwable?
    ) {
        var result: JSONObject? = null
        var string = ""
        try {
            if (t != null) {
                result = JSONObject()
                result.put("errorMsg", t.message)
            } else {
                if (response != null) {
                    if (response.code() == 200) {
                        string = response.body()!!.string()
                        result = JSONObject(string)
                    } else {
                        result = JSONObject()
                        result.put(
                            "errorMsg",
                            "httpCode:" + response.code() + " " + if (response.body() == null) "" else response.body()!!
                                .string()
                        )
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            LogUtils.e("jsonResponseAndCallBack error!")
            result = JSONObject()
            try {
                result.put(
                    "errorMsg",
                    if (" ClientException:$e" == null) "" else e.message + "__" + string
                )
            } catch (ex: JSONException) {
                ex.printStackTrace()
            }
        } finally {
            if (result != null) {
                apiCallBack.onFinish(result)
            }
        }
    }

}