package com.ffst.dustbinbrain.kotlin_mvp.app

import android.app.Application
import com.ffst.dustbinbrain.kotlin_mvp.R
import com.ffst.dustbinbrain.kotlin_mvp.di.networkModule
import com.ffst.dustbinbrain.kotlin_mvp.di.scopeModule
import com.ffst.mvp.di.appModule
import com.ffst.mvp.di.downloadModule
import com.scwang.smart.refresh.footer.ClassicsFooter
import com.scwang.smart.refresh.header.MaterialHeader
import com.scwang.smart.refresh.layout.SmartRefreshLayout
import com.tencent.mmkv.MMKV
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

/**
 * @author catchpig
 * @date 2019/8/18 00:18
 */
class KotlinMvpApp : Application() {
    companion object {
        var userId: Int? = 0
        var userType: Int? = 1
    }

    override fun onCreate() {
        super.onCreate()
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