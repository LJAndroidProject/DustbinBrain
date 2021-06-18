package com.ffst.dustbinbrain.kotlin_mvp.mvp.main.model

import com.ffst.dustbinbrain.kotlin_mvp.mvp.main.MainContract
import com.ffst.dustbinbrain.kotlin_mvp.network.Result
import com.ffst.dustbinbrain.kotlin_mvp.network.api.ServerAPI
import io.reactivex.rxjava3.core.Flowable

class MainModel(private val fenfenServiceAPI: ServerAPI):MainContract.Model {
    override fun banner(): Flowable<Result<Any>> {
        return fenfenServiceAPI.banner()
    }
}