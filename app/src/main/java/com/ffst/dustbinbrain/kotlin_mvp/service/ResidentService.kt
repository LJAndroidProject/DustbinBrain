package com.ffst.dustbinbrain.kotlin_mvp.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.*
import android.util.Log
import androidx.core.content.FileProvider
import com.blankj.utilcode.util.LogUtils
import com.ffst.dustbinbrain.kotlin_mvp.app.DustbinBrainApp
import com.ffst.dustbinbrain.kotlin_mvp.bean.DustbinStateUploadBean
import com.ffst.dustbinbrain.kotlin_mvp.bean.StateCallBean
import com.ffst.dustbinbrain.kotlin_mvp.constants.MMKVCommon
import com.ffst.dustbinbrain.kotlin_mvp.manager.NetApiManager
import com.ffst.dustbinbrain.kotlin_mvp.manager.SerialProManager
import com.ffst.dustbinbrain.kotlin_mvp.network.api.ResponseListener
import com.ffst.dustbinbrain.kotlin_mvp.utils.DownloadUtil
import com.ffst.dustbinbrain.kotlin_mvp.utils.FenFenCommonUtil
import com.ffst.dustbinbrain.kotlin_mvp.utils.TCPConnectUtil
import com.google.gson.Gson
import com.tencent.mmkv.MMKV
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.io.File
import java.util.*

/**
 * Created by LiuJW
 *on 2021/7/7
 */
class ResidentService : Service() {
    private val TAG = "监测服务"
    private var downloading = false
    private var deviceCode: String? = null

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
//        EventBus.getDefault().unregister(this)
    }

    override fun onCreate() {
        super.onCreate()

        LogUtils.dTag(TAG, "服务创建")
//        EventBus.getDefault().register(this)
        //  设备状态上报服务器
        val timerTask: TimerTask = object : TimerTask() {
            override fun run() {
                Log.i(TAG, "上传状态")
                //心跳包可能服务器收不到或没发出去,通过上传状态的时候发送一次
                //通过发送数据保持连接
                TCPConnectUtil.getInstance().sendData("{\"type\":\"ping\"}")
                if (!DustbinBrainApp.dustbinBeanList?.isEmpty()!!) {

                    val dustbinStateBeans = DustbinBrainApp.dustbinBeanList
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
                    dustbinStateUploadBean.version_code = getAppVersionCode(this@ResidentService)

                    NetApiManager.getInstance().postStatusUpload(Gson().toJson(dustbinStateUploadBean), object : ResponseListener {
                        override fun onSuccess(extension: String?) {
                            LogUtils.iTag(TAG,extension)
                            var statusCallBean =Gson().fromJson(extension,StateCallBean::class.java)
                            //  大于，并且没有已经在下载 所以要更新
                            if (statusCallBean.data != null && statusCallBean.data
                                    .version_code > getAppVersionCode(this@ResidentService) && !downloading
                            ) {
                                download(statusCallBean.data.apk_download_url)
                            }
                        }

                        override fun onFail(extension: String?) {
                        }
                    })
                }
            }
        }

        val timer = Timer()
        timer.schedule(timerTask,1,1000*60)
        //  获取垃圾箱状态  暂定30秒获取一次，太快会浪费资源且占用串口通道传输数据，从而导致黑屏的可能
        val getDustbinState :TimerTask = object :TimerTask(){
            override fun run() {
                if(DustbinBrainApp.dustbinBeanList?.size!!>0){
                    for(dustbinBeanList in DustbinBrainApp.dustbinBeanList!!){
                        LogUtils.iTag(TAG, "获取垃圾箱状态${dustbinBeanList.doorNumber}")
                        SerialProManager.getInstance().getData(dustbinBeanList.doorNumber)
                        try {
                            Thread.sleep(400)
                        } catch (e: java.lang.Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }

        }
        val timer1 = Timer()
        timer1.schedule(getDustbinState,1,1000*30)

    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    fun uploadDustBinRecord(params: String?){

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
            DownloadUtil.get().download(url, saveDir, fileName, object : DownloadUtil.OnDownloadListener {
                override fun onDownloadSuccess(file: File?) {
                    //  延迟一下比较好
                    Handler(Looper.getMainLooper()).postDelayed(
                        { downloading = false },
                        (10 * 1000).toLong()
                    )
                    if (file != null) {
                        installApk(file)
                    }
                }

                override fun onDownloading(progress: Int) {
                }

                override fun onDownloadFailed(e: Exception?) {
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

}