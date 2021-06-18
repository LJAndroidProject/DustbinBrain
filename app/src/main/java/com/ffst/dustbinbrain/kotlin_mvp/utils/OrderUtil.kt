package com.ffst.dustbinbrain.kotlin_mvp.utils

import android.util.Log
import com.serialportlibrary.util.ByteStringUtil

/**
 * Created by LiuJW
 *on 2021/6/18
 */
class OrderUtil {
    companion object{
        //帧头
        const val FRAME_HEADER = "5AA5"
        val FRAME_HEADER_BYTES = byteArrayOf(0x5a.toByte(), 0xa5.toByte())
        //数据长度03
        const val DATA_LENGTH03 = "03"
        const val DATA_LENGTH03_BYTES:Byte = 0x03
        //数据长度04
        const val DATA_LENGTH04 = "04"
        const val DATA_LENGTH04_BYTES:Byte  = 0x04
        //  开门
        const val DOOR = "01"
        const val DOOR_BYTE: Byte = 0x01

        //  获取数据
        const val GET_DATA = "02"
        const val GET_DATA_BYTE: Byte = 0x02

        //  进入称重校准
        const val WEIGHING = "03"
        const val WEIGHING_BYTE: Byte = 0x03

        //  称重校准设置重量
        const val WEIGHING_2 = "04"
        const val WEIGHING_2_BYTE: Byte = 0x04

        //  杀菌、消毒
        const val STERILIZE = "05"
        const val STERILIZE_BYTE: Byte = 0x05

        //  照明灯
        const val LIGHT = "06"
        const val LIGHT_BYTE: Byte = 0x06

        //  排气扇
        const val EXHAUST_FAN = "07"
        const val EXHAUST_FAN_BYTE: Byte = 0x07

        //  电磁开关
        const val ELECTROMAGNETIC_SWITCH = "08"
        const val ELECTROMAGNETIC_SWITCH_BYTE: Byte = 0x08

        //  加热
        const val WARM = "09"
        const val WARM_BYTE: Byte = 0x09

        //  搅拌机
        const val BLENDER = "0A"
        const val BLENDER_BYTE: Byte = 0x0A

        //  电磁投料口
        const val DOG_HOUSE = "0C"
        const val DOG_HOUSE_BYTE: Byte = 0x0C

        fun generateOrder(function: Byte, dataLength:Byte , targetDoor: Int, parameter: ByteArray): ByteArray {
            val head: ByteArray = FRAME_HEADER_BYTES
            Log.i(SerialPortUtil.TAG, "帧头：" + ByteStringUtil.byteArrayToHexStr(head))
            val length = byteArrayOf(dataLength)
            Log.i(SerialPortUtil.TAG, "长度：" + ByteStringUtil.byteArrayToHexStr(length))
            val order = byteArrayOf( (targetDoor and 0xff).toByte(),function)
            Log.i(SerialPortUtil.TAG, "门板+功能：" + ByteStringUtil.byteArrayToHexStr(order))
            Log.i(SerialPortUtil.TAG, "开关：" + ByteStringUtil.byteArrayToHexStr(parameter))

            val bytes = ByteArray(head.size+length.size+order.size+parameter.size)
            System.arraycopy(head, 0, bytes, 0, head.size)
            System.arraycopy(length, 0, bytes, head.size, length.size)
            System.arraycopy(order, 0, bytes, head.size+length.size, order.size)
            System.arraycopy(parameter, 0, bytes, head.size+length.size+order.size, parameter.size)

            return bytes
        }
    }
}