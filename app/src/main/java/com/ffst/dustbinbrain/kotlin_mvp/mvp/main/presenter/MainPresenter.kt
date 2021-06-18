package com.ffst.dustbinbrain.kotlin_mvp.mvp.main.presenter

import com.ffst.annotation.TimeLog
import com.ffst.dustbinbrain.kotlin_mvp.mvp.main.MainContract
import com.ffst.mvp.base.BasePresenter

/**
 * @author catchpig
 * @date 2019/8/18 00:18
 */
class MainPresenter @TimeLog constructor(private val view:MainContract.View, private val model:MainContract.Model): BasePresenter(),MainContract.Presenter {
    override fun onCreate() {
        super.onCreate()
//        execute(model.banner(), object :Callback<Result<Any>>(){
//            override fun onNext(result: Result<Any>?) {
//                view.toast(result!!.errorCode.toString())
//            }
//        })
    }
}