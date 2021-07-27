package com.ffst.dustbinbrain.kotlin_mvp.app

import ZtlApi.ZtlManager
import android.app.Application
import android.content.Context
import com.blankj.utilcode.util.LogUtils
import com.ffst.dustbinbrain.kotlin_mvp.R
import com.ffst.dustbinbrain.kotlin_mvp.bean.DustbinConfig
import com.ffst.dustbinbrain.kotlin_mvp.bean.DustbinStateBean
import com.ffst.dustbinbrain.kotlin_mvp.manager.SerialProManager
import com.ffst.dustbinbrain.kotlin_mvp.utils.CrashHandler
import com.ffst.dustbinbrain.kotlin_mvp.utils.SerialPortUtil
import com.scwang.smart.refresh.footer.ClassicsFooter
import com.scwang.smart.refresh.header.MaterialHeader
import com.scwang.smart.refresh.layout.SmartRefreshLayout
import com.tencent.liteav.login.model.ProfileManager
import com.tencent.mmkv.MMKV
import java.util.concurrent.CopyOnWriteArrayList

/**
 * @author catchpig
 * @date 2019/8/18 00:18
 */
class DustbinBrainApp : Application() {
    //bugly初始化使用的APPID
    private val buglyAppId = "b98d724c6c"

    companion object {
        var userType: Long? = 0
        var userId: Int? = 0
        val TAG = "KotlinMvpApp"
        val TAG_RGMTEST = "人工门测试"
        var ApkType = 3

        @Volatile
        var dustbinBeanList: CopyOnWriteArrayList<DustbinStateBean>? = CopyOnWriteArrayList()
        var dustbinConfig: DustbinConfig? = null
        var hasManTime: Long = 0
        fun getDeviceId(): String? {
            if (dustbinConfig == null) {
                return null
            }
            return dustbinConfig!!.dustbinDeviceId
        }

        fun setDustbinState(context: Context, dustbinStateBean: DustbinStateBean) {
            dustbinBeanList?.let { list ->
                // val index = it.indexOfFirst {
                //    LogUtils.iTag(TAG_RGMTEST, "获取index")
                //  it.doorNumber == dustbinStateBean.doorNumber
                //}
                list.find { it.doorNumber == dustbinStateBean.doorNumber }?.let { model ->
                    dustbinStateBean.id = model.getId()
                    dustbinStateBean.dustbinBoxType =
                        model.getDustbinBoxType()
                    dustbinStateBean.dustbinBoxNumber =
                        model.getDustbinBoxNumber()
                    list.set(list.indexOf(model), dustbinStateBean)
                } ?: let {
                    // 这里相当于 else
                    //不能ADD 否则获取数据会变成双倍
//                    list.add(dustbinStateBean)
                }
            }
            LogUtils.iTag(TAG_RGMTEST, "开始修改值")
        }
    }

    override fun onCreate() {
        super.onCreate()
        LogUtils.dTag(TAG, getFilesDir().getAbsolutePath() + "/fenfen/mmkv")
//        CrashReport.initCrashReport(
//            applicationContext,
//            buglyAppId,
//            true
//        )
        CrashHandler.getInstance().init(applicationContext)
        MMKV.initialize(this, getFilesDir().getAbsolutePath() + "/fenfen/mmkv");
        //定昌设备初始化
        ZtlManager.GetInstance().setContext(applicationContext)
        ProfileManager.getInstance().initContext(this)
        SerialPortUtil.getInstance().receiveListener {
            SerialProManager.getInstance().inOrderString(applicationContext, it)
        }
    }

    init {
        //设置全局的Header构建器
        SmartRefreshLayout.setDefaultRefreshHeaderCreator { context, layout -> //全局设置主题颜色
            layout.setPrimaryColorsId(R.color.ffst_btn)
            MaterialHeader(context)
        }
        //设置全局的Footer构建器
        SmartRefreshLayout.setDefaultRefreshFooterCreator { context, _ -> //指定为经典Footer，默认是 BallPulseFooter
            ClassicsFooter(context).setDrawableSize(20f)
        }
    }
}