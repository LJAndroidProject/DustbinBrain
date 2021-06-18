package com.ffst.dustbinbrain.kotlin_mvp.network

data class Result<T>(val errorCode:Int,val errorMsg:String,val data:T)