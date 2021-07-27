package com.ffst.dustbinbrain.kotlin_mvp.mvp.main.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ffst.dustbinbrain.kotlin_mvp.bean.BinsWorkTimeBean
import com.ffst.dustbinbrain.kotlin_mvp.bean.UserBeanModel
import com.ffst.dustbinbrain.kotlin_mvp.manager.NetApiManager
import com.ffst.dustbinbrain.kotlin_mvp.network.api.ResponseListener
import com.google.gson.Gson
import java.lang.Exception

/**
 * Created by LiuJW
 *on 2021/6/16
 */
class MainActivityViewModel : ViewModel() {
    class WorkTimeData {
        var success = false
        var msg: String? = null
        var binsWorkTimeBean: BinsWorkTimeBean? = null
    }

    class DeviceQrcode {
        //{
        //  "code": 1,
        //  "msg": "获取成功",
        //  "time": "1625208959",
        //  "data": "https://ffadmin.fenfeneco.com/uploads/qrcode_device/20210702/2021070260deb608f38351570.png"
        //}
        var success = false
        var code: String? = null
        var msg: String? = null
        var time: String? = null
        var data: String? = null
    }

    class ScanLogin{
        var success = false
        var code: String? = null
        var msg: String? = null
        var time: String? = null
        var data:UserBeanModel?=null
    }

    var liveDataForDustbinConfig = MutableLiveData<WorkTimeData>()
    var liveDataForDeviceQrcode = MutableLiveData<DeviceQrcode>()
    var liveDataForScanLogin = MutableLiveData<ScanLogin>()

    fun getBinsWorkTime(map: MutableMap<String, String>?) {
        NetApiManager.getInstance().getBinsWorkTime(map, object : ResponseListener {
            override fun onSuccess(extension: String?) {
                var data = WorkTimeData()
                data.success = true
                try {
                    data.msg = extension
                    data.binsWorkTimeBean = Gson().fromJson(extension, BinsWorkTimeBean::class.java)
                } catch (e: Exception) {
                    data.success = false
                    data.msg = "服务器数据解析异常"
                    liveDataForDustbinConfig.postValue(data)
                }
                liveDataForDustbinConfig.postValue(data)
            }

            override fun onFail(extension: String?) {
                var data = WorkTimeData()
                data.success = false
                try {
                    data.binsWorkTimeBean = Gson().fromJson(extension, BinsWorkTimeBean::class.java)
                } catch (e: Exception) {
                    data.success = false
                    data.msg = "服务器数据解析异常"
                    liveDataForDustbinConfig.postValue(data)
                }
                liveDataForDustbinConfig.postValue(data)
            }

        })
    }

    fun getDeviceQrcode(map: MutableMap<String, String>) {
        NetApiManager.getInstance().getDeviceQrcode(map, object : ResponseListener {
            override fun onSuccess(extension: String?) {
                var deviceQrcode = DeviceQrcode()
                try {
                    deviceQrcode = Gson().fromJson(extension,DeviceQrcode::class.java)
                    deviceQrcode.success = true
                } catch (e: Exception) {
                    deviceQrcode.success = false
                    deviceQrcode.msg = "服务器数据解析异常"
                    liveDataForDeviceQrcode.postValue(deviceQrcode)
                }
            }

            override fun onFail(extension: String?) {
                var deviceQrcode = DeviceQrcode()
                deviceQrcode.success = false
                try {
                    deviceQrcode = Gson().fromJson(extension, DeviceQrcode::class.java)
                } catch (e: Exception) {
                    deviceQrcode.success = false
                    deviceQrcode.msg = "服务器数据解析异常"
                    liveDataForDeviceQrcode.postValue(deviceQrcode)
                }
                liveDataForDeviceQrcode.postValue(deviceQrcode)
            }
        })
    }

    fun getScanLogin(qrCodeScan:String){
        NetApiManager.getInstance().getScanLogin(qrCodeScan, object : ResponseListener {
            var scanLogin = ScanLogin()
            override fun onSuccess(extension: String?) {
                try {
                    scanLogin = Gson().fromJson(extension,ScanLogin::class.java)
                    scanLogin.success = true
                    liveDataForScanLogin.postValue(scanLogin)
                }catch (e:Exception){
                    scanLogin.success = false
                    scanLogin.msg = "服务器数据解析异常"
                    liveDataForScanLogin.postValue(scanLogin)
                }
                liveDataForScanLogin.postValue(scanLogin)
            }

            override fun onFail(extension: String?) {
                try {
                    scanLogin = Gson().fromJson(extension,ScanLogin::class.java)
                    scanLogin.success = false
                }catch (e:Exception){
                    scanLogin.success = false
                    scanLogin.msg = "服务器数据解析异常"
                    liveDataForScanLogin.postValue(scanLogin)
                }
            }
        })
    }

    fun registerTCP(tcp_client_id: String) {
        NetApiManager.getInstance().registerTCP(tcp_client_id, object : ResponseListener {
            override fun onSuccess(extension: String?) {
            }

            override fun onFail(extension: String?) {
            }
        })
    }

    fun postFaceRegisterSuccessLog(map: MutableMap<String, String>) {
        NetApiManager.getInstance().postFaceRegisterSuccessLog(map, object : ResponseListener {
            override fun onSuccess(extension: String?) {
            }

            override fun onFail(extension: String?) {
            }

        })
    }
}