package com.ffst.dustbinbrain.kotlin_mvp.bean

/**
 * Created by LiuJW
 *on 2021/6/18
 */
class BuySuccessMsg {
    /**
     * timestamp : 1602815591
     * goods_id : 6
     * code : 1
     * device_id : TB123456
     * out_trade_no : AMAT_20201016761605f8906580fe68
     */
    private var timestamp = 0
    private var goods_id = 0
    private var code = 0
    private var device_id: String? = null
    private var out_trade_no: String? = null

    fun getTimestamp(): Int {
        return timestamp
    }

    fun setTimestamp(timestamp: Int) {
        this.timestamp = timestamp
    }

    fun getGoods_id(): Int {
        return goods_id
    }

    fun setGoods_id(goods_id: Int) {
        this.goods_id = goods_id
    }

    fun getCode(): Int {
        return code
    }

    fun setCode(code: Int) {
        this.code = code
    }

    fun getDevice_id(): String? {
        return device_id
    }

    fun setDevice_id(device_id: String?) {
        this.device_id = device_id
    }

    fun getOut_trade_no(): String? {
        return out_trade_no
    }

    fun setOut_trade_no(out_trade_no: String?) {
        this.out_trade_no = out_trade_no
    }

    override fun toString(): String {
        return "BuySuccessMsg{" +
                "timestamp=" + timestamp +
                ", goods_id=" + goods_id +
                ", code=" + code +
                ", device_id='" + device_id + '\'' +
                ", out_trade_no='" + out_trade_no + '\'' +
                '}'
    }
}