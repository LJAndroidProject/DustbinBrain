package com.ffst.mvp.apt

import com.ffst.mvp.base.activity.BaseActivity


/**
 * @author catchpig
 * @date 2019/10/17 00:17
 */
interface MvpCompiler {
    /**
     * 初始化标题栏
     */
    fun inject(activity: BaseActivity)

}