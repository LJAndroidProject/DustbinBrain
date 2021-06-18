package com.ffst.dustbinbrain.kotlin_mvp.bean

/**
 * Created by LiuJW
 *on 2021/6/18
 */
class TCPVerifyResponse {
    /**
     * msg : Hello 7f0000010b5600000001 连接成功,请在10秒内发送认证，否则连接将会关闭！！！
     * client_id : 7f0000010b5600000001
     * code : 1
     */
    private var msg: String? = null
    private var client_id: String? = null
    private var code = 0

    fun getMsg(): String? {
        return msg
    }

    fun setMsg(msg: String?) {
        this.msg = msg
    }

    fun getClient_id(): String? {
        return client_id
    }

    fun setClient_id(client_id: String?) {
        this.client_id = client_id
    }

    fun getCode(): Int {
        return code
    }

    fun setCode(code: Int) {
        this.code = code
    }
}