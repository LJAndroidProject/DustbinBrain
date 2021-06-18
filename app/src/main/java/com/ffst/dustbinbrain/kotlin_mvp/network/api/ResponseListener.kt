package com.ffst.dustbinbrain.kotlin_mvp.network.api

/**
 * Created by LiuJW
 *on 2021/6/15
 */
interface ResponseListener {
    fun onSuccess(extension: String?)
    fun onFail(extension: String?)
}