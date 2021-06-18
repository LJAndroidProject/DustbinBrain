package com.ffst.dustbinbrain.kotlin_mvp.mvp.test

import android.util.Log
import android.view.View
import com.ffst.dustbinbrain.kotlin_mvp.R
import com.ffst.dustbinbrain.kotlin_mvp.manager.SerialProManager
import com.ffst.dustbinbrain.kotlin_mvp.utils.SerialPortUtil
import com.ffst.mvp.base.activity.BaseActivity
import com.serialportlibrary.util.ByteStringUtil

/**
 * Created by LiuJW
 *on 2021/6/18
 */
class SerialProtTestActivity:BaseActivity() {
    override fun layoutId(): Int {
        return R.layout.activity_serial_test
    }

    override fun initViewData() {
        SerialPortUtil.getInstance().receiveListener {
            //  打印收到的指令
            Log.i("APP.TAG", "接收: " + ByteStringUtil.byteArrayToHexStr(it))
        }
    }

    fun sendDataPort(v: View){
        when(v.id){
            R.id.open_door ->{
               SerialProManager.getInstance().openDoor(1)
            }
            R.id.close_door ->{
                SerialProManager.getInstance().closeDoor(1)
            }
            else->{

            }
        }
    }
}