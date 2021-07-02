package com.ffst.dustbinbrain.kotlin_mvp.network.api

import com.ffst.dustbinbrain.kotlin_mvp.network.Result
import io.reactivex.rxjava3.core.Flowable
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.QueryMap


interface ServerAPI {
    @GET("banner/json")
    fun banner():Flowable<Result<Any>>

    @GET("api/android/getDeviceQrcode")
    fun getDeviceQrcode(@QueryMap param:MutableMap<String,String>):Call<ResponseBody>

    @POST("api/Android/equipmentActivity")
    fun getDustbinConfig(@QueryMap param:MutableMap<String,String>):Call<ResponseBody>

    @GET("api/Android/getBinsWorkTime")
    fun getBinWorkTime(@QueryMap param: MutableMap<String, String>?):Call<ResponseBody>

    @POST("api/Android/equipmentTcpBind")
    fun registerTCP(@QueryMap param: MutableMap<String, String>):Call<ResponseBody>

    @POST("api/android/postFaceRegisterSuccessLog")
    fun postFaceRegisterSuccessLog(@QueryMap param: MutableMap<String, String>):Call<ResponseBody>
}