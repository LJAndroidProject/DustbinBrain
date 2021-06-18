package com.ffst.dustbinbrain.kotlin_mvp.bean

/**
 * Created by LiuJW
 *on 2021/6/16
 */
class BinsWorkTimeBean {
    /**
     * code : 1
     * msg : 获取成功
     * time : 1606185206
     * data : {"am_start_time":"07:00:00","am_end_time":"09:00:00","pm_start_time":"18:00:00","pm_end_time":"21:00:00"}
     */
    private var code = 0
    private var msg: String? = null
    private var time: String? = null
    private var data: DataBean? = null

    fun getCode(): Int {
        return code
    }

    fun setCode(code: Int) {
        this.code = code
    }

    fun getMsg(): String? {
        return msg
    }

    fun setMsg(msg: String?) {
        this.msg = msg
    }

    fun getTime(): String? {
        return time
    }

    fun setTime(time: String?) {
        this.time = time
    }

    fun getData(): DataBean? {
        return data
    }

    fun setData(data: DataBean?) {
        this.data = data
    }

    class DataBean {
        /**
         * am_start_time : 07:00:00
         * am_end_time : 09:00:00
         * pm_start_time : 18:00:00
         * pm_end_time : 21:00:00
         */
        var am_start_time: String? = null
        var am_end_time: String? = null
        var pm_start_time: String? = null
        var pm_end_time: String? = null
    }
}