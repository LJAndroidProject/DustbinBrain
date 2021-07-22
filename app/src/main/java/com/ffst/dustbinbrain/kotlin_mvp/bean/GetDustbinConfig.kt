package com.ffst.dustbinbrain.kotlin_mvp.bean

/**
 * Created by LiuJW
 *on 2021/6/15
 */
/**
 *  获取垃圾箱配置返回bean
 * */
class GetDustbinConfig {


    /**
     * code : 1
     * id : 28
     * msg : 设备激活成功
     * time : 1602732239
     * data : {"count":8,"has_amat":1,"device_name":"长洲岛-幸福小区智能垃圾箱设备","list":[{"id":1,"device_id":"EQ123456","bin_type":"A","bin_code":"A01"},{"id":2,"device_id":"EQ123456","bin_type":"A","bin_code":"A02"},{"id":3,"device_id":"EQ123456","bin_type":"B","bin_code":"B01"},{"id":4,"device_id":"EQ123456","bin_type":"C","bin_code":"C01"},{"id":5,"device_id":"EQ123456","bin_type":"C","bin_code":"C02"},{"id":6,"device_id":"EQ123456","bin_type":"D","bin_code":"D01"},{"id":7,"device_id":"EQ123456","bin_type":"D","bin_code":"D02"},{"id":8,"device_id":"EQ123456","bin_type":"D","bin_code":"D03"}]}
     */
    var code = 0
    var msg: String? = null
    var time: String? = null

    var data: DataBean? = null

    class DataBean {
        /**
         * count : 8
         * has_amat : 1
         * device_name : 长洲岛-幸福小区智能垃圾箱设备
         * list : [{"id":1,"device_id":"EQ123456","bin_type":"A","bin_code":"A01"},{"id":2,"device_id":"EQ123456","bin_type":"A","bin_code":"A02"},{"id":3,"device_id":"EQ123456","bin_type":"B","bin_code":"B01"},{"id":4,"device_id":"EQ123456","bin_type":"C","bin_code":"C01"},{"id":5,"device_id":"EQ123456","bin_type":"C","bin_code":"C02"},{"id":6,"device_id":"EQ123456","bin_type":"D","bin_code":"D01"},{"id":7,"device_id":"EQ123456","bin_type":"D","bin_code":"D02"},{"id":8,"device_id":"EQ123456","bin_type":"D","bin_code":"D03"}]
         */
        var count = 0
        var has_amat = 0
        var device_name: String? = null
        //此id为当前设备登录IM音视频通讯所需
        var id: String? = null
        var list: List<ListBean>? = null

        class ListBean {
            /**
             * id : 1
             * device_id : EQ123456
             * bin_type : A
             * bin_code : A01
             */
            var id: Long = 0
            var device_id: String? = null
            var bin_type: String? = null
            var bin_code: String? = null
        }
    }

    override fun toString(): String {
        return "GetDustbinConfig{" +
                "code=" + code +
                ", msg='" + msg + '\'' +
                ", time='" + time + '\'' +
                ", data=" + data +
                '}'
    }
}