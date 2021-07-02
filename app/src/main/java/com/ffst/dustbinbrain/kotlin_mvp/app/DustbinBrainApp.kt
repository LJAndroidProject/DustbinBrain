package com.ffst.dustbinbrain.kotlin_mvp.app

import android.app.Application
import com.blankj.utilcode.util.LogUtils
import com.ffst.dustbinbrain.kotlin_mvp.R
import com.scwang.smart.refresh.footer.ClassicsFooter
import com.scwang.smart.refresh.header.MaterialHeader
import com.scwang.smart.refresh.layout.SmartRefreshLayout
import com.tencent.mmkv.MMKV

/**
 * @author catchpig
 * @date 2019/8/18 00:18
 */
class DustbinBrainApp : Application() {
    companion object {
        var userId: Int? = 0
        var userType: Int? = 1
        val TAG = "KotlinMvpApp"
    }

    override fun onCreate() {
        super.onCreate()
        LogUtils.dTag(TAG,getFilesDir().getAbsolutePath() + "/fenfen/mmkv")

        MMKV.initialize(this, getFilesDir().getAbsolutePath() + "/fenfen/mmkv");

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