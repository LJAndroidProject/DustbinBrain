package com.ffst.dustbinbrain.kotlin_mvp.mvp.bind.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ffst.dustbinbrain.kotlin_mvp.bean.GetDustbinConfig
import com.ffst.dustbinbrain.kotlin_mvp.manager.NetApiManager
import com.ffst.dustbinbrain.kotlin_mvp.network.api.ResponseListener
import com.google.gson.Gson
import org.json.JSONException
import org.json.JSONObject


/**
 * Created by LiuJW
 *on 2021/6/15
 */
class BindActivityViewModel : ViewModel() {
    class DustbinConfigData{
        var success = false
        var errorCode: String? = null
        var errorMsg: String? = null
        var isLoading = false
        var configData: GetDustbinConfig? = null
    }

    var liveDataForDustbinConfig = MutableLiveData<DustbinConfigData>()

    fun getDustbinConfig(device_id:String, mange_code:String){
        NetApiManager.getInstance().getDustbinConfig(device_id,mange_code,object : ResponseListener{
            override fun onSuccess(extension: String?) {
                var data = DustbinConfigData()
                data.success = true
                data.configData = Gson().fromJson(
                    extension,
                    GetDustbinConfig::class.java
                )
                liveDataForDustbinConfig.postValue(data)
            }

            override fun onFail(extension: String?) {
                var data = DustbinConfigData()
                var jsonObject: JSONObject? = null
                data.success = false
                try {
                    jsonObject = JSONObject(extension)
                    data.errorCode = jsonObject.optString("code")
                    data.errorMsg = jsonObject.optString("msg")
                } catch (e: JSONException) {
                    e.printStackTrace()
                    data.errorCode = "-1"
                    data.errorMsg = "服务器返回信息异常"
                }
                liveDataForDustbinConfig.postValue(data)
            }

        })
    }
}