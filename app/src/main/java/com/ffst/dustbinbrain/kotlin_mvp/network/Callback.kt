package com.ffst.dustbinbrain.kotlin_mvp.network

import com.ffst.utils.ext.loge
import io.reactivex.rxjava3.subscribers.ResourceSubscriber

abstract class Callback<T>:ResourceSubscriber<T>() {
    override fun onError(t: Throwable?) {
        t!!.message!!.loge()
    }

    override fun onComplete() {

    }
}