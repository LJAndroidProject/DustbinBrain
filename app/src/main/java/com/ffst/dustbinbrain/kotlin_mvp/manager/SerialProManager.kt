package com.ffst.dustbinbrain.kotlin_mvp.manager

import android.content.Context
import android.util.Log
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.TimeUtils
import com.blankj.utilcode.util.ToastUtils
import com.ffst.dustbinbrain.kotlin_mvp.app.DustbinBrainApp
import com.ffst.dustbinbrain.kotlin_mvp.bean.DustbinStateBean
import com.ffst.dustbinbrain.kotlin_mvp.bean.ImlSerialPortRequest
import com.ffst.dustbinbrain.kotlin_mvp.utils.OrderUtil
import com.ffst.dustbinbrain.kotlin_mvp.utils.SerialPortUtil
import com.serialportlibrary.util.ByteStringUtil
import okhttp3.internal.and
import java.util.*

/**
 * Created by LiuJW
 *on 2021/6/18
 */
class SerialProManager : ImlSerialPortRequest.ByteHEX {

    companion object {
        val OPEN_PARAMETER = byteArrayOf(0x11)

        val CLOSE_PARAMETER = byteArrayOf(0x00)

        val WEIGHT_PARAMETER = byteArrayOf(0x01)

        val default = byteArrayOf(0x01)


        val result_00 = byteArrayOf(0x00)
        val result_01 = byteArrayOf(0x01)
        val result_10 = byteArrayOf(0x10)
        val result_11 = byteArrayOf(0x11)
        val result_12 = byteArrayOf(0x12)

        var serialPortUtil: SerialPortUtil? = null
        private var instance: SerialProManager? = null
        fun getInstance() = instance ?: synchronized(this) {
            init()
            instance ?: SerialProManager().also { instance = it }
        }

        fun init() {
            serialPortUtil = SerialPortUtil.getInstance()
        }
    }

    fun inOrderString(context: Context, order: ByteArray) {
        Log.i(DustbinBrainApp.TAG, "接收: " + ByteStringUtil.byteArrayToHexStr(order))
        //长度小于4丢弃
        if (order.size < 4) {
            Log.i(DustbinBrainApp.TAG, "接收: " + ByteStringUtil.byteArrayToHexStr(order) + "长度过小")
            return
        }
        //判断帧头 (目前从机协议无帧尾，只需判断帧头即可)
        if (Arrays.equals(byteArrayOf(order[0], order[1]), OrderUtil.FRAME_HEADER_BYTES)) {
            //指令解析
            var orderMessage = OrderUtil.orderAnalysis(order)
            if (orderMessage.order?.get(0) == OrderUtil.DOOR_BYTE) {
                var tag = "开关门指令"
                Log.i(tag, "接收: " + ByteStringUtil.byteArrayToHexStr(order))
                if (orderMessage.dataContent?.get(0) == result_00[0]) {
                    //关成功
                    ToastUtils.showShort("关成功")
                    Log.i(tag, "关成功")
                } else if (orderMessage.dataContent?.get(0) == result_10[0]) {
                    //开成功
                    ToastUtils.showShort("开成功")
                    Log.i(tag, "开成功")
                } else if (orderMessage.dataContent?.get(0) == result_11[0]) {
                    //开失败
                    ToastUtils.showShort("开失败")
                    Log.i(tag, "开失败")
                } else if (orderMessage.dataContent?.get(0) == result_01[0]) {
                    //关失败
                    ToastUtils.showShort("关失败")
                    Log.i(tag, "关失败")
                }else if (orderMessage.dataContent?.get(0) == result_12[0]) {
                    //电机过载
                    ToastUtils.showShort("电机过载")
                    Log.i(tag, "电机过载")
                }
            } else if (orderMessage.order?.get(0) == OrderUtil.GET_DATA_BYTE) {
                //  获取数据
                /**
                 * 5AA50C010200001900830000000001
                 * 帧头 5AA5
                 * 数据长度 0C
                 * 地址码(门) 01
                 * 功能码 02
                 * 数据区 00001900830000000001
                 */
                val dustbinStateBean = DustbinStateBean()
                var weight = byteArrayOf(
                    orderMessage.dataContent!!.get(0),
                    orderMessage.dataContent!!.get(1)
                )
                //  重量 0-25000 * 10g
                dustbinStateBean.dustbinWeight = bytes2Int(weight).toDouble()
                //  温度0-200°C
                dustbinStateBean.temperature =orderMessage.dataContent!![2].toDouble()
                //  湿度 0-100%
                dustbinStateBean.humidity =orderMessage.dataContent!![3].toDouble()
                //  设置门编号
                dustbinStateBean.doorNumber = orderMessage.address!!.get(0).toInt()
                //  其它
                //  1.空	2.接近开关	3.人工门开关	 4.测满	5.推杆过流	6.通信异常	7.投料锁	8.人工门锁
                val other: Byte = orderMessage.dataContent!!.get(4)


                val tString = Integer.toBinaryString((other and 0xFF) + 0x100).substring(1)
                val chars = tString.toCharArray()
                //  挡板是否开启
                dustbinStateBean.doorIsOpen = chars[0] == '1'
                //  接近开关
                dustbinStateBean.proximitySwitch = chars[1] == '0' //   !!!!
                //  人工门开关
                dustbinStateBean.artificialDoor = chars[2] == '0'
                //  侧满
                dustbinStateBean.isFull = chars[3] == '0'
                //  推杆过流
                dustbinStateBean.pushRod = chars[4] == '1'
                //  通信异常
                dustbinStateBean.abnormalCommunication = chars[5] == '1'
                //  投料锁
                dustbinStateBean.deliverLock = chars[6] == '1'
                //  人工锁
                dustbinStateBean.artificialDoorLock = chars[7] == '1'
                Log.i(DustbinBrainApp.TAG, "状态解析" + dustbinStateBean.toChineseString())
                if(!dustbinStateBean.proximitySwitch){
                    //垃圾箱接近感应红外异常可能导致一直无法自动结算
                    DustbinBrainApp.hasManTime = System.currentTimeMillis();
                    LogUtils.dTag("改变倒计时","DustbinBrainApp.hasManTime:${TimeUtils.millis2Date(DustbinBrainApp.hasManTime)}")
                }
                DustbinBrainApp.setDustbinState(context,dustbinStateBean)
            } else if (orderMessage.order?.get(0) == OrderUtil.WEIGHING_BYTE) {

            } else if (orderMessage.order?.get(0) == OrderUtil.WEIGHING_2_BYTE) {

            }
        }
    }

    fun bytes2Int(bytes: ByteArray): Int {
        var result = 0
        //将每个byte依次搬运到int相应的位置
        result = bytes[0] and 0xff
        result = result shl 8 or bytes[1].toInt() and 0xff
        return result
    }

    /**
     * 开投料口门
     */
    override fun openDoor(doorNumber: Int): ByteArray {
        val bytes = SerialPortUtil.getInstance().sendData(
            OrderUtil.generateOrder(
                OrderUtil.DOOR_BYTE,
                OrderUtil.DATA_LENGTH03_BYTES,
                doorNumber,
                OPEN_PARAMETER
            )
        )
        return bytes ?: default
    }

    /**
     * 关投料口门
     */
    override fun closeDoor(doorNumber: Int): ByteArray {
        return SerialPortUtil.getInstance().sendData(
            OrderUtil.generateOrder(
                OrderUtil.DOOR_BYTE, OrderUtil.DATA_LENGTH03_BYTES, doorNumber,
                CLOSE_PARAMETER
            )
        ) ?: default
    }


    /**
     * 开启消毒
     */
    override fun openTheDisinfection(doorNumber: Int): ByteArray {
        return SerialPortUtil.getInstance().sendData(
            OrderUtil.generateOrder(
                OrderUtil.STERILIZE_BYTE, OrderUtil.DATA_LENGTH03_BYTES, doorNumber,
                OPEN_PARAMETER
            )
        ) ?: default
    }

    /**
     * 关闭消毒
     */
    override fun closeTheDisinfection(doorNumber: Int): ByteArray {
        return SerialPortUtil.getInstance().sendData(
            OrderUtil.generateOrder(
                OrderUtil.STERILIZE_BYTE, OrderUtil.DATA_LENGTH03_BYTES, doorNumber,
                CLOSE_PARAMETER
            )
        ) ?: default
    }

    /**
     * 开启排气
     */
    override fun openExhaustFan(doorNumber: Int): ByteArray {
        return SerialPortUtil.getInstance().sendData(
            OrderUtil.generateOrder(
                OrderUtil.EXHAUST_FAN_BYTE, OrderUtil.DATA_LENGTH03_BYTES, doorNumber,
                OPEN_PARAMETER
            )
        ) ?: default
    }

    /**
     * 关闭排气
     */
    override fun closeExhaustFan(doorNumber: Int): ByteArray {
        return SerialPortUtil.getInstance().sendData(
            OrderUtil.generateOrder(
                OrderUtil.EXHAUST_FAN_BYTE, OrderUtil.DATA_LENGTH03_BYTES, doorNumber,
                CLOSE_PARAMETER
            )
        ) ?: default
    }

    /**
     * 开启电磁
     */
    override fun openElectromagnetism(doorNumber: Int): ByteArray {
        return SerialPortUtil.getInstance().sendData(
            OrderUtil.generateOrder(
                OrderUtil.ELECTROMAGNETIC_SWITCH_BYTE, OrderUtil.DATA_LENGTH03_BYTES, doorNumber,
                OPEN_PARAMETER
            )
        ) ?: default
    }

    /**
     * 关闭电磁
     */
    override fun closeElectromagnetism(doorNumber: Int): ByteArray {
        return SerialPortUtil.getInstance().sendData(
            OrderUtil.generateOrder(
                OrderUtil.ELECTROMAGNETIC_SWITCH_BYTE, OrderUtil.DATA_LENGTH03_BYTES, doorNumber,
                CLOSE_PARAMETER
            )
        ) ?: default
    }

    /**
     * 开启加热
     */
    override fun openTheHeating(doorNumber: Int): ByteArray {
        return SerialPortUtil.getInstance().sendData(
            OrderUtil.generateOrder(
                OrderUtil.WARM_BYTE, OrderUtil.DATA_LENGTH03_BYTES, doorNumber,
                OPEN_PARAMETER
            )
        ) ?: default
    }

    /**
     * 关闭加热
     */
    override fun closeTheHeating(doorNumber: Int): ByteArray {
        return SerialPortUtil.getInstance().sendData(
            OrderUtil.generateOrder(
                OrderUtil.WARM_BYTE, OrderUtil.DATA_LENGTH03_BYTES, doorNumber,
                CLOSE_PARAMETER
            )
        ) ?: default
    }

    /**
     * 开启搅拌
     */
    override fun openBlender(doorNumber: Int): ByteArray {
        return SerialPortUtil.getInstance().sendData(
            OrderUtil.generateOrder(
                OrderUtil.BLENDER_BYTE, OrderUtil.DATA_LENGTH03_BYTES, doorNumber,
                OPEN_PARAMETER
            )
        ) ?: default
    }

    /**
     * 关闭搅拌
     */
    override fun closeBlender(doorNumber: Int): ByteArray {
        return SerialPortUtil.getInstance().sendData(
            OrderUtil.generateOrder(
                OrderUtil.BLENDER_BYTE, OrderUtil.DATA_LENGTH03_BYTES, doorNumber,
                CLOSE_PARAMETER
            )
        ) ?: default
    }

    /**
     * 开启投料口锁
     */
    override fun openDogHouse(doorNumber: Int): ByteArray {
        return SerialPortUtil.getInstance().sendData(
            OrderUtil.generateOrder(
                OrderUtil.DOG_HOUSE_BYTE, OrderUtil.DATA_LENGTH03_BYTES, doorNumber,
                OPEN_PARAMETER
            )
        ) ?: default
    }

    /**
     * 关闭投料口锁
     */
    override fun closeDogHouse(doorNumber: Int): ByteArray {
        return SerialPortUtil.getInstance().sendData(
            OrderUtil.generateOrder(
                OrderUtil.DOG_HOUSE_BYTE, OrderUtil.DATA_LENGTH03_BYTES, doorNumber,
                CLOSE_PARAMETER
            )
        ) ?: default
    }

    /**
     * 开启照明
     */
    override fun openLight(doorNumber: Int): ByteArray {
        return SerialPortUtil.getInstance().sendData(
            OrderUtil.generateOrder(
                OrderUtil.LIGHT_BYTE, OrderUtil.DATA_LENGTH03_BYTES, doorNumber,
                OPEN_PARAMETER
            )
        ) ?: default
    }

    /**
     * 关闭照明
     */
    override fun closeLight(doorNumber: Int): ByteArray {
        return SerialPortUtil.getInstance().sendData(
            OrderUtil.generateOrder(
                OrderUtil.LIGHT_BYTE, OrderUtil.DATA_LENGTH03_BYTES, doorNumber,
                CLOSE_PARAMETER
            )
        ) ?: default
    }

    /**
     * 进入重量校准
     */
    override fun weightCalibration_1(doorNumber: Int): ByteArray {
        TODO("Not yet implemented")
    }

    /**
     * 退出重量校准
     */
    override fun exitWeightCalibrationMode(doorNumber: Int): ByteArray {
        TODO("Not yet implemented")
    }

    /**
     * 重量校准
     */
    override fun weightCalibration_2(doorNumber: Int, weight: Int): ByteArray {
        TODO("Not yet implemented")
    }

    /**
     * 读取数据
     */
    override fun getData(doorNumber: Int): ByteArray {
        return SerialPortUtil.getInstance().sendData(
            OrderUtil.generateOrder(
                OrderUtil.GET_DATA_BYTE, OrderUtil.DATA_LENGTH03_BYTES, doorNumber,
                CLOSE_PARAMETER
            )
        ) ?: default
    }


}