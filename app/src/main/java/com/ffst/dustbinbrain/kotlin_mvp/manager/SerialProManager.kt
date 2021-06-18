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

    override fun closeDoor(doorNumber: Int): ByteArray {
        return SerialPortUtil.getInstance().sendData(
            OrderUtil.generateOrder(
                OrderUtil.DOOR_BYTE, OrderUtil.DATA_LENGTH03_BYTES, doorNumber,
                CLOSE_PARAMETER
            )
        )?:byteArrayOf(0x00)
    }

    override fun measureTheDistance(doorNumber: Int): ByteArray {
        TODO("Not yet implemented")
    }

    override fun measureTheWeight(doorNumber: Int): ByteArray {
        TODO("Not yet implemented")
    }

    override fun openTheDisinfection(doorNumber: Int): ByteArray {
        TODO("Not yet implemented")
    }

    override fun closeTheDisinfection(doorNumber: Int): ByteArray {
        TODO("Not yet implemented")
    }

    override fun openExhaustFan(doorNumber: Int): ByteArray {
        TODO("Not yet implemented")
    }

    override fun closeExhaustFan(doorNumber: Int): ByteArray {
        TODO("Not yet implemented")
    }

    override fun openElectromagnetism(doorNumber: Int): ByteArray {
        TODO("Not yet implemented")
    }

    override fun closeElectromagnetism(doorNumber: Int): ByteArray {
        TODO("Not yet implemented")
    }

    override fun openTheHeating(doorNumber: Int): ByteArray {
        TODO("Not yet implemented")
    }

    override fun closeTheHeating(doorNumber: Int): ByteArray {
        TODO("Not yet implemented")
    }

    override fun openBlender(doorNumber: Int): ByteArray {
        TODO("Not yet implemented")
    }

    override fun closeBlender(doorNumber: Int): ByteArray {
        TODO("Not yet implemented")
    }

    override fun openDogHouse(doorNumber: Int): ByteArray {
        TODO("Not yet implemented")
    }

    override fun closeDogHouse(doorNumber: Int): ByteArray {
        TODO("Not yet implemented")
    }

    override fun openLight(doorNumber: Int): ByteArray {
        TODO("Not yet implemented")
    }

    override fun closeLight(doorNumber: Int): ByteArray {
        TODO("Not yet implemented")
    }

    override fun weightCalibration_1(doorNumber: Int): ByteArray {
        TODO("Not yet implemented")
    }

    override fun exitWeightCalibrationMode(doorNumber: Int): ByteArray {
        TODO("Not yet implemented")
    }

    override fun weightCalibration_2(doorNumber: Int, weight: Int): ByteArray {
        TODO("Not yet implemented")
    }

    override fun getDate(doorNumber: Int): ByteArray {
        TODO("Not yet implemented")
    }


}