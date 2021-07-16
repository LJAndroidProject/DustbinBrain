package com.ffst.dustbinbrain.kotlin_mvp.mvp.test

import android.view.View
import com.ffst.dustbinbrain.kotlin_mvp.R
import com.ffst.dustbinbrain.kotlin_mvp.manager.SerialProManager
import com.ffst.mvp.base.activity.BaseActivity
import kotlinx.android.synthetic.main.activity_serial_test.*

/**
 * Created by LiuJW
 *on 2021/6/18
 */
class SerialProtTestActivity:BaseActivity() {
    override fun layoutId(): Int {
        return R.layout.activity_serial_test
    }

    override fun initViewData() {

    }

    fun sendDataPort(v: View){
        var doorText = getdoor.text
        var doorNum = 0
        when(doorText.toString()){
            "1"->{
                doorNum = 1
            }
            "2"->{
                doorNum = 2
            }
            "3"->{
                doorNum = 3
            }
            "4"->{
                doorNum = 4
            }
            "5"->{
                doorNum = 5
            }
            else->{
                doorNum = 1
            }

        }
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