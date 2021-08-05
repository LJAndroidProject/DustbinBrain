package com.ffst.dustbinbrain.kotlin_mvp.mvp.test

import android.view.View
import com.ffst.dustbinbrain.kotlin_mvp.R
import com.ffst.dustbinbrain.kotlin_mvp.app.AndroidDeviceSDK
import com.ffst.dustbinbrain.kotlin_mvp.manager.SerialProManager
import com.ffst.mvp.base.activity.BaseActivity
import kotlinx.android.synthetic.main.activity_serial_test.*

/**
 * Created by LiuJW
 *on 2021/6/18
 */
class SerialProtTestActivity : BaseActivity() {
    override fun layoutId(): Int {
        return R.layout.activity_serial_test
    }

    override fun initViewData() {

    }

    fun sendDataPort(v: View) {
        var doorText = getdoor.text.toString()
        var doorNum = 0
        if (doorText.isEmpty()) {
            doorText = "0"
        }
        if (doorText.toInt() in 1..16) {
            doorNum = doorText.toInt()
        }
        when (v.id) {
            R.id.open_door -> {
                SerialProManager.getInstance().openDoor(doorNum)
            }
            R.id.close_door -> {
                SerialProManager.getInstance().closeDoor(doorNum)
            }
            R.id.open_paiqi -> {
                SerialProManager.getInstance().openExhaustFan(doorNum)
            }
            R.id.close_paiqi -> {
                SerialProManager.getInstance().closeExhaustFan(doorNum)
            }
            R.id.open_xiaodu -> {
                SerialProManager.getInstance().openTheDisinfection(doorNum)
            }
            R.id.close_xiaodu -> {
                SerialProManager.getInstance().closeTheDisinfection(doorNum)
            }
            R.id.open_zhaoming -> {
                SerialProManager.getInstance().openLight(doorNum)
            }
            R.id.close_zhaoming -> {
                SerialProManager.getInstance().closeLight(doorNum)
            }
            R.id.show_SystemBar -> {
                AndroidDeviceSDK.hideStatus(this, true)
            }
            R.id.hide_SystemBar -> {
                AndroidDeviceSDK.hideStatus(this, false)
            }
            R.id.serial_test_back -> {
                finish()
            }
            R.id.keepApp -> {
                AndroidDeviceSDK.keepActivity(this)
            }
            R.id.unKeepApp -> {
                AndroidDeviceSDK.unKeepActivity(this)
            }
            else -> {


            }
        }
    }

}