package com.ffst.mvp.apt

import com.ffst.mvp.base.activity.BaseActivity
import com.ffst.utils.ext.logd

/**
 * @author catchpig
 * @date 2019/10/17 00:17
 */
object KotlinMvpCompiler {
    private const val TAG = "KotlinMvpCompiler"
    fun inject(baseActivity: BaseActivity){
        val className = baseActivity.javaClass.name
        try {
            val compilerClass = Class.forName(className+"_MvpCompiler")
            compilerClass.let {
                val mvpCompiler = compilerClass.newInstance() as com.ffst.mvp.apt.MvpCompiler
                mvpCompiler.inject(baseActivity)
            }
        }catch (exception:ClassNotFoundException){
            "$className:没有被(com.ffst.annotation)下编译时注解修饰".let {
                it.logd(com.ffst.mvp.apt.KotlinMvpCompiler.TAG)
            }
        }

    }
}