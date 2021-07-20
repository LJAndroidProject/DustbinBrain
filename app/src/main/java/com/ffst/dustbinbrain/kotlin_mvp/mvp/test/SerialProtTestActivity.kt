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
        when (doorText) {
            "1" -> {
                doorNum = 1
            }
            "2" -> {
                doorNum = 2
            }
            "3" -> {
                doorNum = 3
            }
            "4" -> {
                doorNum = 4
            }
            "5" -> {
                doorNum = 5
            }
            "6" -> {
                doorNum = 6
            }
            "7" -> {
                doorNum = 7
            }
            "8" -> {
                doorNum = 8
            }
            "9" -> {
                doorNum = 9
            }
            "10" -> {
                doorNum = 10
            }
            else -> {
                doorNum = 1
            }

        }
        when (v.id) {
            R.id.open_door -> {
                SerialProManager.getInstance().openDoor(doorNum)
            }
            R.id.close_door -> {
                SerialProManager.getInstance().closeDoor(doorNum)
            }
            R.id.open_paiqi -> {
                SerialProManager.getInstance().closeDoor(doorNum)
            }
            R.id.close_paiqi -> {
                SerialProManager.getInstance().closeDoor(doorNum)
            }
            R.id.open_xiaodu -> {
                SerialProManager.getInstance().closeDoor(doorNum)
            }
            R.id.close_xiaodu -> {
                SerialProManager.getInstance().closeDoor(doorNum)
            }
            R.id.open_zhaoming -> {
                SerialProManager.getInstance().closeDoor(doorNum)
            }
            R.id.close_zhaoming -> {
                SerialProManager.getInstance().closeDoor(doorNum)
            }
            R.id.show_SystemBar -> {
                AndroidDeviceSDK.hideStatus(true)
            }
            R.id.hide_SystemBar -> {
                AndroidDeviceSDK.hideStatus(false)
            }
            R.id.serial_test_back -> {
                finish()
            }
            else -> {


            }
        }
    }

}