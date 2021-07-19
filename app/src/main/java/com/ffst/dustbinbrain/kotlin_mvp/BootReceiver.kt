package com.ffst.dustbinbrain.kotlin_mvp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.blankj.utilcode.util.LogUtils
import com.ffst.dustbinbrain.kotlin_mvp.mvp.bind.view.DeviceMannageActivity


class BootReceiver : BroadcastReceiver() {
    val TAG = "mytag"

    companion object {
        //开机广播
        val device_start_cation = "android.intent.action.BOOT_COMPLETED"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        LogUtils.e("收到广播:${intent?.action}")
        if (device_start_cation == intent?.action) {
            //设备启动
            val i = Intent(context, DeviceMannageActivity::class.java)
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context!!.startActivity(i)
        }
    }
}