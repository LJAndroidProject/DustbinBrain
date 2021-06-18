package com.ffst.dustbinbrain.kotlin_mvp.mvp.main

import com.ffst.dustbinbrain.kotlin_mvp.network.Result
import com.ffst.mvp.base.BaseContract
import io.reactivex.rxjava3.core.Flowable

/**
 * @author catchpig
 * @date 2019/8/18 00:18
 */
interface MainContract {
    interface View:BaseContract.View{

    }
    interface Presenter:BaseContract.Presenter{

    }
    interface Model{
        fun banner():Flowable<Result<Any>>
    }
}