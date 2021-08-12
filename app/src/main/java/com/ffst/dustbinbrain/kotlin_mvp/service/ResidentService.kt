package com.ffst.dustbinbrain.kotlin_mvp.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.*
import android.text.TextUtils
import android.util.Log
import android.view.SurfaceView
import androidx.core.content.FileProvider
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ToastUtils
import com.ffst.dustbinbrain.kotlin_mvp.BuildConfig
import com.ffst.dustbinbrain.kotlin_mvp.app.AndroidDeviceSDK
import com.ffst.dustbinbrain.kotlin_mvp.app.DustbinBrainApp
import com.ffst.dustbinbrain.kotlin_mvp.bean.*
import com.ffst.dustbinbrain.kotlin_mvp.constants.MMKVCommon
import com.ffst.dustbinbrain.kotlin_mvp.facepass.NetWorkUtil
import com.ffst.dustbinbrain.kotlin_mvp.facepass.ServerAddress
import com.ffst.dustbinbrain.kotlin_mvp.manager.NetApiManager
import com.ffst.dustbinbrain.kotlin_mvp.manager.SerialProManager
import com.ffst.dustbinbrain.kotlin_mvp.network.api.ResponseListener
import com.ffst.dustbinbrain.kotlin_mvp.utils.*
import com.google.gson.Gson
import com.peergine.android.livemulti.pgLibLiveMultiError
import com.peergine.android.livemulti.pgLibLiveMultiRender
import com.peergine.android.livemulti.pgLibLiveMultiView
import com.peergine.plugin.lib.pgLibJNINode
import com.tencent.mmkv.MMKV
import okhttp3.Call
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.io.File
import java.io.IOException
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Created by LiuJW
 *on 2021/7/7
 */
class ResidentService : Service() {
    private val TAG = "监测服务"
    private var downloading = false
    private var deviceCode: String? = null
    private var m_Live = pgLibLiveMultiRender()
    private val m_sServerAddr = "www.ptop21.com:7781"
    private var m_Wnd: SurfaceView? = null
    private var cameraList: MutableMap<String, String> = mutableMapOf()
    private var m_sDevID = ""
    private var binRecordImgPath = ""
    private var dustbinBeanList: List<DustbinStateBean>? = mutableListOf()

    var checkStatus: TimerTask? = null

    var getDustbinState: TimerTask? = null

    override fun onBind(intent: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    override fun onUnbind(intent: Intent?): Boolean {
        return super.onUnbind(intent)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, flags, startId)
    }


    override fun onDestroy() {
        super.onDestroy()
        LogUtils.dTag(TAG, "服务销毁")
        checkStatus?.cancel()
        getDustbinState?.cancel()
        EventBus.getDefault().unregister(this)
    }

    override fun onCreate() {
        super.onCreate()

        LogUtils.dTag(TAG, "服务创建")
        EventBus.getDefault().register(this)

        dustbinBeanList = DataBaseUtil.getInstance(this).getDustbinByType(null)

        //  设备状态上报服务器
        checkStatus = object : TimerTask() {
            override fun run() {
                Log.i(TAG, "上传状态")
                //心跳包可能服务器收不到或没发出去,通过上传状态的时候发送一次
                //通过发送数据保持连接
                TCPConnectUtil.getInstance().sendData("{\"type\":\"ping\"}")
                if (dustbinBeanList?.isEmpty() != true) {

                    val dustbinStateBeans = dustbinBeanList
                    val dustbinStateUploadBean = DustbinStateUploadBean()
                    deviceCode = MMKV.defaultMMKV()?.decodeString(MMKVCommon.DEVICE_ID)
                    val listBean: MutableList<DustbinStateUploadBean.ListBean> =
                        ArrayList<DustbinStateUploadBean.ListBean>()
                    if (dustbinStateBeans != null) {
                        for (dustbinStateBean in dustbinStateBeans) {
                            listBean.add(
                                DustbinStateUploadBean.ListBean(
                                    dustbinStateBean.dustbinWeight,
                                    dustbinStateBean.id,
                                    dustbinStateBean.isFull,
                                    dustbinStateBean.temperature
                                )
                            )
                        }
                    }
                    dustbinStateUploadBean.setList(listBean)
                    val nowTime = System.currentTimeMillis() / 1000
                    dustbinStateUploadBean.setSign(
                        FenFenCommonUtil.md5(
                            nowTime.toString() + FenFenCommonUtil.key
                        )?.uppercase(Locale.getDefault())
                    )
                    dustbinStateUploadBean.timestamp = nowTime.toString()
                    dustbinStateUploadBean.apk_type = 1
                    dustbinStateUploadBean.device_id = deviceCode
//                    dustbinStateUploadBean.version_code = getAppVersionCode(this@ResidentService)
                    dustbinStateUploadBean.version_code = BuildConfig.VERSION_NAME

                    NetApiManager.getInstance().postStatusUpload(
                        Gson().toJson(dustbinStateUploadBean),
                        object : ResponseListener {
                            override fun onSuccess(extension: String?) {
                                LogUtils.iTag(TAG, extension)
                                var statusCallBean =
                                    Gson().fromJson(extension, StateCallBean::class.java)
                                //  大于，并且没有已经在下载 所以要更新
                                if (statusCallBean.data != null && statusCallBean.data
                                        .version_code > getAppVersionCode(this@ResidentService) && !downloading
                                ) {
                                    download(statusCallBean.data.apk_download_url)
                                }
                                if (statusCallBean.data.eq_status == 0) {
                                    try{
                                        //  断开连接
                                        TCPConnectUtil.getInstance().disconnect()
                                        //  重新连接
                                        TCPConnectUtil.getInstance().connect()

                                        NetWorkUtil.getInstance().errorUpload(
                                            "上传状态时发现设备离线"
                                        )
                                    }catch (e:java.lang.Exception){
                                        AndroidDeviceSDK.restartApp(this@ResidentService)
                                    }
                                }
                            }

                            override fun onFail(extension: String?) {
                            }
                        })
                }
            }
        }

        val timer = Timer()
        timer.schedule(checkStatus, 1, 1000 * 60)
        //  获取垃圾箱状态  暂定30秒获取一次，太快会浪费资源且占用串口通道传输数据，从而导致黑屏的可能
        getDustbinState = object : TimerTask() {
            override fun run() {
                if (dustbinBeanList?.isNotEmpty() == true) {
                    for (dustbinBean in dustbinBeanList!!) {
                        LogUtils.iTag(
                            TAG,
                            "桶位个数：${dustbinBeanList?.size}获取垃圾箱状态${dustbinBean.doorNumber}"
                        )
                        SerialProManager.getInstance().getData(dustbinBean.doorNumber)
//                        try {
//                            Thread.sleep(1000)
//                        } catch (e: java.lang.Exception) {
//                            e.printStackTrace()
//                        }
                    }
                }
            }

        }
        val timer1 = Timer()
        timer1.schedule(getDustbinState, 0, 1000 * 30)

        if (!CheckPlugin()) {
            return
        }
        m_Live.SetEventListener(m_OnEvent)
        LiveLogout()
        LiveLogin()
    }
    private var closeDoorNum = 0
    @Subscribe(threadMode = ThreadMode.ASYNC)
    fun uploadDustBinRecord(params: DustBinRecordRequestParams?) {
        LogUtils.d(TAG, "EventBus收到上传投递记录：" + params?.getRequestMap())
        val map = params?.getRequestMap()
        if (map != null) {
            val bin_id = params.getRequestMap()?.get("bin_id")
            val doorNumber = params.getRequestMap()?.get("doorNumber")
            val time = params.getRequestMap()?.get("time")
            val dustbinBoxNumber = params.getRequestMap()?.get("bin_type")
            //厨余垃圾才去打开摄像头采集视频
            if (dustbinBoxNumber?.contains("A") == true) {
                if (doorNumber != null) {
                    SerialProManager.getInstance().openLight(doorNumber.toInt())
                    closeDoorNum = doorNumber.toInt()
                }
                val cameraId = "HD0010$doorNumber"
                LiveConnect(cameraId)
                val imageName: String = DustbinBrainApp.getDeviceId()
                    .toString() + "_" + doorNumber + "_" + DustbinBrainApp.userId + "_" + time + "_" + bin_id + ".jpg"
                binRecordImgPath = "/sdcard/Download/" + imageName
            }
            NetWorkUtil.getInstance().doPost(
                ServerAddress.DUSTBIN_RECORD,
                params.getRequestMap(),
                object : NetWorkUtil.NetWorkListener {
                    override fun success(response: String) {
                        LogUtils.d(TAG, "投递记录成功上传服务器: $response")
                    }

                    override fun fail(call: Call?, e: IOException) {
                        LogUtils.d("投递失败 " + e.message)
                    }

                    override fun error(e: java.lang.Exception) {
                        LogUtils.d("投递失败 " + e.message)
                    }
                })
        }


    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    fun uploadDustBinRecord(data: String) {
        LogUtils.d(TAG, "EventBus收到data：$data")
        if (!TextUtils.isEmpty(data)) {
            download(data)
        }
    }

    /**
     * 监听下载任务
     */
    fun download(url: String?) {
        val saveDir = Environment.getExternalStorageDirectory().toString()

        //  文件名称
        //final String fileName = System.currentTimeMillis() + ".apk";
        val fileName = "newPackage.apk"
        Thread {
            downloading = true
            DownloadUtil.get()
                .download(url, saveDir, fileName, object : DownloadUtil.OnDownloadListener {
                    override fun onDownloadSuccess(file: File?) {
                        LogUtils.iTag(
                            "download",
                            "文件路径：${file?.absolutePath},  文件大小：${file?.length()}"
                        )
                        //  延迟一下比较好
                        Handler(Looper.getMainLooper()).postDelayed(
                            { downloading = false },
                            (10 * 1000).toLong()
                        )
                        if (file != null) {
//                        installApk(file)
                            ToastUtils.showShort("开始远程更新APP")
                            AndroidDeviceSDK.installApp(file.path)
                            if ("qingzheng" === AndroidDeviceSDK.deviceType) {
                                installApk(file)
                            }
//                        ZtlManager.GetInstance().installAppAndStartUp(file.absolutePath, this@ResidentService.packageName);
                        }
                    }

                    override fun onDownloading(progress: Int) {
                        LogUtils.iTag("download", "进度：$progress")
                    }

                    override fun onDownloadFailed(e: Exception?) {
                        LogUtils.iTag("download", "失败：${e.toString()}")
                        downloading = false
                    }

                })
        }.start()
    }

    /**
     * 打开 安装包 开始安装
     */
    private fun installApk(file: File) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) { // 7.0+以上版本
            val apkUri: Uri = FileProvider.getUriForFile(
                this,
                "$packageName.fileprovider", file
            )
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            intent.setDataAndType(apkUri, "application/vnd.android.package-archive")
        } else {
            intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive")
        }
        startActivity(intent)
    }

    /**
     * 获取版本号
     */
    fun getAppVersionCode(context: Context): Int {
        var appVersionCode: Int = 0
        try {
            val packageInfo =
                context.applicationContext.packageManager.getPackageInfo(context.packageName, 0)
            appVersionCode = packageInfo.versionCode
        } catch (e: PackageManager.NameNotFoundException) {
            Log.e("", e.message!!)
        }
        return appVersionCode
    }


    private fun CheckPlugin(): Boolean {
        return if (pgLibJNINode.Initialize(this)) {
            pgLibJNINode.Clean()
            true
        } else {
            false
        }
    }

    private fun LiveLogout() {
        LiveDisconnect(m_sDevID)
        m_Wnd = pgLibLiveMultiView.Get("view0")
        if (m_Wnd != null) {
            pgLibLiveMultiView.Release(m_Wnd)
            m_Wnd = null
            m_Live.Clean()
        }
    }

    private fun LiveLogin() {
        m_Live.Clean()
        val sInitParam = "(Debug){1}(VideoSoftDecode){1}"
        val iErr = m_Live.Initialize(
            "ANDROID_DEMO", "1234",
            m_sServerAddr, "", 1, sInitParam, this
        )
        if (iErr != pgLibLiveMultiError.PG_ERR_Normal) {
            Log.d("pgLiveRander", "LiveStart: Live.Initialize failed! iErr=$iErr")
            Process.killProcess(Process.myPid())
            return
        }
        m_Wnd = pgLibLiveMultiView.Get("view0")

    }

    private fun LiveConnect(m_sDevID: String) {
        m_Live.Disconnect(m_sDevID)
        m_Live.Connect(m_sDevID)
        m_Live.VideoStart(m_sDevID, 0, "", m_Wnd)
        val sAudioParam = ""
        m_Live.AudioStart(m_sDevID, 0, sAudioParam)
        m_Live.AudioSyncDelay(m_sDevID, 0, 0)
    }

    private fun LiveDisconnect(m_sDevID: String) {

        m_Live.AudioStop(m_sDevID, 0)
        m_Live.VideoStop(m_sDevID, 0)
        m_Live.Disconnect(m_sDevID)
    }


    private val m_OnEvent =
        pgLibLiveMultiRender.OnEventListener { sAct, sData, sCapID ->
            if (sAct == "VideoStatus") {
                // Video status report
                //frmplay   ， 如果这个是一直在增加的，那视频是肯定在播放的
                LogUtils.dTag(
                    TAG,
                    "摄像头:$sData, sCapID：$sCapID"
                )
                var datas = sData.split("&")
                var frmplay = 0
                for (myData in datas) {
                    if (myData.contains("frmplay")) {
                        frmplay = myData.substring(("frmplay".length + 2)).toInt()
                    }
                }
                if (frmplay > 0) {

                    val errorCode0 = m_Live.VideoCamera(sCapID, 0, binRecordImgPath)
                    LogUtils.dTag(
                        TAG,
                        "摄像头:$sData, binRecordImgPath:$binRecordImgPath,  sCapID：$sCapID,   errorCode:$errorCode0"
                    )
                }
            } else if (sAct == "Notify") {
                val sInfo = "Receive notify: data=$sData"
            } else if (sAct == "Message") {
                val sInfo = "Receive msg: data=$sData, sCapID=$sCapID"
                val iInd = sData.indexOf("stamp:")
                if (iInd >= 0) {
                    val sStamp = sData.substring(6)
                    val iStamp1 = sStamp.toLong()
                    val iStamp2 = Date().time
                    val iDelta = iStamp2 - iStamp1
                }
            } else if (sAct == "Login") {
                // Login reply
                var sInfo = ""
                if (sData == "0") {
                    sInfo = "Login success"
                } else {
                    sInfo = "Login failed, error=$sData"
                }
                LogUtils.dTag(TAG, "摄像头:$sInfo")
            } else if (sAct == "Logout") {
                // Logout
                val sInfo = "Logout"
            } else if (sAct == "Connect") {
                // Connect to capture
                val sInfo = "Connect to capture"
                LogUtils.dTag(
                    TAG,
                    "摄像头:$sInfo, binRecordImgPath:$binRecordImgPath,  sCapID：$sCapID"
                )

            } else if (sAct == "Disconnect") {
                // Disconnect from capture
                val sInfo = "Disconnect from capture"
            } else if (sAct == "Reject") {
                val sInfo = "Reject by capture"
            } else if (sAct == "Offline") {
                // The capture is offline.
                val sInfo = "Capture offline"
            } else if (sAct == "LanScanResult") {
                try {
                    val cameraId = sData.split("&").get(0).substring(3)
                    cameraList.put(cameraId, cameraId)
                    LogUtils.dTag(TAG, "摄像头cameraId:$cameraId,  cameraList:$cameraList")
                } catch (e: IndexOutOfBoundsException) {
                    LogUtils.dTag(TAG, "摄像头cameraId获取错误")
                }
                // Lan scan result.
            } else if (sAct == "RecordStopVideo") {
                // Record stop video.
            } else if (sAct == "RecordStopAudio") {
                // Record stop video.
            } else if (sAct == "ForwardAllocReply") {
            } else if (sAct == "ForwardFreeReply") {
            } else if (sAct == "VideoCamera") {
                LogUtils.dTag(
                    TAG,
                    "摄像头VideoCamera:$sData, binRecordImgPath:$binRecordImgPath,  sCapID:$sCapID"
                )
                LiveDisconnect(sCapID)
                SerialProManager.getInstance().closeLight(closeDoorNum)
                //上传
                uploadImageFile(binRecordImgPath)
            } else if (sAct == "FileAccept") {
            } else if (sAct == "FileReject") {
            } else if (sAct == "FileAbort") {
                // 取消 上传或 下载
            } else if (sAct == "FileFinish") {
                // 文件传输完毕
            } else if (sAct == "FileProgress") {
            } else if (sAct == "SvrNotify") {
            } else if (sAct == "PeerInfo") {
                //局域网需要扫描后才可以预览
                LogUtils.dTag(TAG, "摄像头:$sData")
                m_Live.LanScanStart()
            }
            Log.d(
                "pgLiveRender", "OnEvent: Act=" + sAct + ", Data=" + sData
                        + ", sCapID=" + sCapID
            )
        }


    fun uploadImageFile(path: String?) {
        if (path != null) {
            val file = File(path)
            //  去除.jpg
            //  设备id + 门板编号 + 用户id + 时间戳 + 垃圾箱id . jpg
            val fileName = file.name.replace(".jpg", "")

            //  解析文件名称
            val fileArray = fileName.split("_".toRegex()).toTypedArray()


            /*
             *
             * 图片文件上传
             * */
            NetWorkUtil.getInstance()
                .fileUploadAutoDelete(file, object : NetWorkUtil.FileUploadListener {
                    override fun success(fileUrl: String) {
                        val map: MutableMap<String, String> = HashMap()
                        map["bin_id"] = fileArray[4]
                        map["user_id"] = fileArray[2]
                        map["rubbish_image"] = fileUrl
                        map["time"] = fileArray[3]
                        LogUtils.d(TAG, "图片上传参数：$map")
                        LogUtil.writeBusinessLog("上传拍照信息参数：$map")
                        NetWorkUtil.getInstance().doPost(
                            ServerAddress.RUBBISH_IMAGE_POST,
                            map,
                            object : NetWorkUtil.NetWorkListener {
                                override fun success(response: String) {
                                    Log.i(TAG, "图片绑定结果$response")
                                }

                                override fun fail(call: Call?, e: IOException) {
                                    Log.i(TAG, "图片绑定结果" + e.message)
                                }

                                override fun error(e: java.lang.Exception) {
                                    Log.i(TAG, "图片绑定结果" + e.message)
                                }
                            })
                    }

                    override fun error(e: java.lang.Exception) {
                        Log.i(TAG, "图片绑定结果" + e.message)
                    }
                })
        }
    }

}