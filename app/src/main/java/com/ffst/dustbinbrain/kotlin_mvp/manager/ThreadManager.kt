package com.ffst.dustbinbrain.kotlin_mvp.manager

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * Created by LiuJW
 *on 2021/6/15
 */
class ThreadManager {
    //kotlin伴生对象 即java的static
    companion object {
        //双重校验锁
        @Volatile
        private var instance: ThreadManager? = null
        fun getInstance():ThreadManager{
            instance?: synchronized(this){
                instance?:ThreadManager().also { instance=it }
            }
            return instance!!
        }
    }

    private var mThreadPool:ExecutorService?=null
    //私有构造函数，单例模式
    private constructor() {
        mThreadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())
    }

    fun execute(task : Runnable){
        mThreadPool?.execute(task)
    }

    fun submit(task: Runnable){
        mThreadPool?.execute(task)
    }
}