package com.ffst.dustbinbrain.kotlin_mvp.bean

/**
 * Created by LiuJW
 *on 2021/6/18
 */
class TCPVerify {
    companion object{

    }
    /**
     * type : login
     * data : {"sign":"asdasdasdasdas","timestamp":"16092023232"}
     */
    private var type: String? = null
    private var data: DataBean? = null

    fun getType(): String? {
        return type
    }

    fun setType(type: String?) {
        this.type = type
    }

    fun getData(): DataBean? {
        return data
    }

    fun setData(data: DataBean?) {
        this.data = data
    }

    class DataBean {
        /**
         * sign : asdasdasdasdas
         * timestamp : 16092023232
         */
        var sign: String? = null
        var timestamp: String? = null
    }
}