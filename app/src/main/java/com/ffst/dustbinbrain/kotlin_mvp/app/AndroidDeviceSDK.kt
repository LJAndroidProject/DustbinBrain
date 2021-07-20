package com.ffst.dustbinbrain.kotlin_mvp.app

import ZtlApi.ZtlManager
import android.content.Context

class AndroidDeviceSDK {
    companion object {
        val TAG = "AndroidDeviceSDK"
        var pkgName = "com.ffst.dustbinbrain"
        //  检测是否在前台
        fun checkForeground(context:Context){
            pkgName = context.getPackageName()
            //pkgName：需要保持置顶的APP
//            ZtlManager.GetInstance().keepActivity(pkgName)
            ZtlManager.GetInstance().unKeepActivity()
        }

        //设置开机自启动
        fun autoStratAPP(context: Context){
            pkgName = context.getPackageName()
            //persist.sys.bootPkgName=com.ffst.dustbinbrain(填入需要自启动的包名)
            //persist.sys.bootPkgActivity=com.ffst.dustbinbrain.mvp.bind.view.DeviceMannageActivity(填入需要自启动的类名)
            ZtlManager.GetInstance().setBootPackageActivity(pkgName,"com.ffst.dustbinbrain.mvp.bind.view.DeviceMannageActivity");
        }

        //  隐藏状态栏
        fun hideStatus(bOpen:Boolean){
            //bOpen:传入 true(显示)或 false(隐藏)即可
            ZtlManager.GetInstance().openSystemBar(bOpen);
        }

        //重启
        fun reBoot(){
            //delay：传入延迟时间，如不需要延迟，传入0即可 单位：秒
            ZtlManager.GetInstance().reboot(0)
        }

        //定时开机
        /**
         * 参数说明：hour：时(0 <= hour < 24)；minute：分(0 <= minute < 60);enableSchedulPowerOn:使能/关闭定时开机
         * 备注：使能或者关闭定时开关机功能，直接设置第三个参数为true(使能)/false(关闭)
         */
        fun setSchedulePowerOn(){
            //设置 Android 设备 17：00 每天定时开机
            ZtlManager.GetInstance().setSchedulePowerOn(10,52,true);
        }

        //定时关机
        /**
         * 参数说明：hour：时(0 <= hour < 24)；minute：分(0 <= minute < 60);enableSchedulPowerOff:使能/关闭定时关机
         * 备注：使能或者关闭定时开关机功能，直接设置第三个参数为true(使能)/false(关闭)
         */
        fun setSchedulePowerOff(){
            //设置 Android 设备 9：30 每天定时关机
            ZtlManager.GetInstance().setSchedulePowerOff(10,50,true);
        }

        //无需root权限静默安装APK
        fun installApp(filePath:String){
            //filePath：APK文件安装路径。
            ZtlManager.GetInstance().installApp(filePath);
        }
    }
}