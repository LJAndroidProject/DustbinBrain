package com.ffst.dustbinbrain.kotlin_mvp.mvp.main.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ffst.dustbinbrain.kotlin_mvp.bean.BinsWorkTimeBean
import com.ffst.dustbinbrain.kotlin_mvp.manager.NetApiManager
import com.ffst.dustbinbrain.kotlin_mvp.network.api.ResponseListener
import com.google.gson.Gson
import java.lang.Exception

/**
 * Created by LiuJW
 *on 2021/6/16
 */
class MainActivityViewModel : ViewModel() {
    class WorkTimeData{
        var success = false
        var errorMsg: String? = null
        var binsWorkTimeBean : BinsWorkTimeBean? = null
    }

    var liveDataForDustbinConfig = MutableLiveData<WorkTimeData>()

    fun getBinsWorkTime(){
        NetApiManager.getInstance().getBinsWorkTime(object :ResponseListener{
            override fun onSuccess(extension: String?) {
                var data = WorkTimeData()
                data.success = true
                try {
                    data.binsWorkTimeBean = Gson().fromJson(extension,BinsWorkTimeBean::class.java)
                }catch (e:Exception){
                    data.success = false
                    data.errorMsg = "服务器数据解析异常"
                    liveDataForDustbinConfig.postValue(data)
                }
                liveDataForDustbinConfig.postValue(data)
            }

            override fun onFail(extension: String?) {
                var data = WorkTimeData()
                data.success = false
                try {
                    data.binsWorkTimeBean = Gson().fromJson(extension,BinsWorkTimeBean::class.java)
                }catch (e:Exception){
                    data.success = false
                    data.errorMsg = "服务器数据解析异常"
                    liveDataForDustbinConfig.postValue(data)
                }
                liveDataForDustbinConfig.postValue(data)
            }

        })
    }

    fun registerTCP(tcp_client_id:String){
        NetApiManager.getInstance().registerTCP(tcp_client_id,object :ResponseListener{
            override fun onSuccess(extension: String?) {
            }

            override fun onFail(extension: String?) {
            }
        })
    }

    fun postFaceRegisterSuccessLog(map: MutableMap<String,String>){
        NetApiManager.getInstance().postFaceRegisterSuccessLog(map,object :ResponseListener{
            override fun onSuccess(extension: String?) {
            }

            override fun onFail(extension: String?) {
            }

        })
    }
}