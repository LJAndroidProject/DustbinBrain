package com.ffst.dustbinbrain.kotlin_mvp.manager

import com.ffst.dustbinbrain.kotlin_mvp.bean.ImlSerialPortRequest
import com.ffst.dustbinbrain.kotlin_mvp.utils.OrderUtil
import com.ffst.dustbinbrain.kotlin_mvp.utils.SerialPortUtil
import com.ffst.utils.ext.toByteArray

/**
 * Created by LiuJW
 *on 2021/6/18
 */
class SerialProManager : ImlSerialPortRequest.ByteHEX {

    companion object {
        val OPEN_PARAMETER = byteArrayOf(0x11)

        val CLOSE_PARAMETER = byteArrayOf(0x00)

        val WEIGHT_PARAMETER = byteArrayOf(0x01)

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
         return bytes ?: byteArrayOf(0x00)
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
        )?:byteArrayOf(0x00)
    }


    /**
     * 开启消毒
     */
    override fun openTheDisinfection(doorNumber: Int): ByteArray {
        TODO("Not yet implemented")
    }

    /**
     * 关闭消毒
     */
    override fun closeTheDisinfection(doorNumber: Int): ByteArray {
        TODO("Not yet implemented")
    }

    /**
     * 开启
     */
    override fun openExhaustFan(doorNumber: Int): ByteArray {
        TODO("Not yet implemented")
    }

    /**
     * 关闭排气
     */
    override fun closeExhaustFan(doorNumber: Int): ByteArray {
        TODO("Not yet implemented")
    }

    /**
     * 开启电磁
     */
    override fun openElectromagnetism(doorNumber: Int): ByteArray {
        TODO("Not yet implemented")
    }

    /**
     * 关闭电磁
     */
    override fun closeElectromagnetism(doorNumber: Int): ByteArray {
        TODO("Not yet implemented")
    }

    /**
     * 开启加热
     */
    override fun openTheHeating(doorNumber: Int): ByteArray {
        TODO("Not yet implemented")
    }

    /**
     * 关闭加热
     */
    override fun closeTheHeating(doorNumber: Int): ByteArray {
        TODO("Not yet implemented")
    }

    /**
     * 开启搅拌
     */
    override fun openBlender(doorNumber: Int): ByteArray {
        TODO("Not yet implemented")
    }

    /**
     * 关闭搅拌
     */
    override fun closeBlender(doorNumber: Int): ByteArray {
        TODO("Not yet implemented")
    }

    /**
     * 开启投料口锁
     */
    override fun openDogHouse(doorNumber: Int): ByteArray {
        TODO("Not yet implemented")
    }

    /**
     * 关闭投料口锁
     */
    override fun closeDogHouse(doorNumber: Int): ByteArray {
        TODO("Not yet implemented")
    }

    /**
     * 开启照明
     */
    override fun openLight(doorNumber: Int): ByteArray {
        TODO("Not yet implemented")
    }

    /**
     * 关闭照明
     */
    override fun closeLight(doorNumber: Int): ByteArray {
        TODO("Not yet implemented")
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
        TODO("Not yet implemented")
    }


}