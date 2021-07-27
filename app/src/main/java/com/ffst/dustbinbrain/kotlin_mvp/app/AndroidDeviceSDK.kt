package com.ffst.dustbinbrain.kotlin_mvp.app

import ZtlApi.ZtlManager
import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Process
import androidx.core.app.ActivityCompat

class AndroidDeviceSDK {
    companion object {
        val TAG = "AndroidDeviceSDK"
        var pkgName = "com.ffst.dustbinbrain"
        var deviceType: String = "qingzheng"   // qingzheng ： 铁甲   ZTL：定昌

        //  检测是否在前台
        fun keepActivity(context: Context) {
            pkgName = context.getPackageName()
            //pkgName：需要保持置顶的APP
            ZtlManager.GetInstance().keepActivity(pkgName)
//            ZtlManager.GetInstance().unKeepActivity()
            if ("qingzheng" === deviceType) {
                //  监听 app 是否在前台
                //  监听 app 是否在前台
                val intent2 = Intent("android.q_zheng.action.APPMONITOR")
                intent2.putExtra("package_name", pkgName) //设置所监控应用的包名为 com.xxx.yyy

                intent2.putExtra("self_starting", true) //设置开机自启动

                intent2.putExtra(
                    "period",
                    30
                ) //设置监控应有的周期，秒为单位，最小值为 15 秒，如果不设置

                context.sendBroadcast(intent2)
            }
        }

        fun unKeepActivity(context: Context) {
            ZtlManager.GetInstance().unKeepActivity()
            if ("qingzheng" === deviceType) {
                //  监听 app 是否在前台
                val intent2 = Intent("android.q_zheng.action.APPMONITOR")
                intent2.putExtra("package_name", pkgName) //设置所监控应用的包名为 com.xxx.yyy

                intent2.putExtra("self_starting", true) //设置开机自启动

                intent2.putExtra(
                    "period",
                    0
                ) //设置监控应有的周期，秒为单位，最小值为 15 秒，如果不设置

                context.sendBroadcast(intent2)
            }
        }

        //设置开机自启动
        fun autoStratAPP(context: Context) {
            pkgName = context.getPackageName()
            //persist.sys.bootPkgName=com.ffst.dustbinbrain(填入需要自启动的包名)
            //persist.sys.bootPkgActivity=com.ffst.dustbinbrain.mvp.bind.view.DeviceMannageActivity(填入需要自启动的类名)
            ZtlManager.GetInstance().setBootPackageActivity(
                pkgName,
                "com.ffst.dustbinbrain.mvp.bind.view.DeviceMannageActivity"
            );
            if ("qingzheng" === deviceType) {
                //  监听 app 是否在前台
                val intent2 = Intent("android.q_zheng.action.APPMONITOR")
                intent2.putExtra("package_name", pkgName) //设置所监控应用的包名为 com.xxx.yyy

                intent2.putExtra("self_starting", true) //设置开机自启动

                intent2.putExtra(
                    "period",
                    30
                ) //设置监控应有的周期，秒为单位，最小值为 15 秒，如果不设置

                context.sendBroadcast(intent2)
            }
        }

        //  隐藏状态栏
        fun hideStatus(context: Context,bOpen: Boolean) {
            //bOpen:传入 true(显示)或 false(隐藏)即可
            ZtlManager.GetInstance().openSystemBar(bOpen);
            if ("qingzheng" === deviceType){
                //  隐藏状态栏，也就是 app 打开后不能退出
                val intent = Intent("android.q_zheng.action.statusbar")
                intent.putExtra("forbidden", bOpen)
                intent.putExtra("status_bar", bOpen)
                intent.putExtra("navigation_bar", bOpen)
                context.sendBroadcast(intent)
            }
        }

        //重启
        fun reBoot(context: Context) {
            //delay：传入延迟时间，如不需要延迟，传入0即可 单位：秒
            ZtlManager.GetInstance().reboot(0)
            if ("qingzheng" === deviceType){
                val intent = Intent("android.q_zheng.action.REBOOT")
                context.sendBroadcast(intent)
            }
        }

        //定时开机
        /**
         * 参数说明：hour：时(0 <= hour < 24)；minute：分(0 <= minute < 60);enableSchedulPowerOn:使能/关闭定时开机
         * 备注：使能或者关闭定时开关机功能，直接设置第三个参数为true(使能)/false(关闭)
         */
        fun setSchedulePowerOn(context: Context) {
            //设置 Android 设备 6：30每天定时开机
            ZtlManager.GetInstance().setSchedulePowerOn(6, 30, true);
            if ("qingzheng" === deviceType){
                val intent = Intent("android.q_zheng.action.POWERONOFF")
                /*int[] poweroff = {0,1}; //    即在每天 0:1 关机,小时取值 0-23,分钟取值 0-59
        int[] poweron = {0,3}; //  即在每天 0:3 开机,小时取值 0-23,分钟取值 0-59*/
                /*int[] poweroff = {0,1}; //    即在每天 0:1 关机,小时取值 0-23,分钟取值 0-59
        int[] poweron = {0,3}; //  即在每天 0:3 开机,小时取值 0-23,分钟取值 0-59*/
                val poweroff = intArrayOf(6, 30) //    即在每天 9:30 关机,小时取值 0-23,分钟取值 0-59

                val poweron = intArrayOf(21, 30) //  即在每天 17:30 开机,小时取值 0-23,分钟取值 0-59

                intent.putExtra("timeon", poweron)
                intent.putExtra("timeoff", poweroff)
                intent.putExtra("type", 2) //类型 2 代表设置每天开关机时间

                intent.putExtra("enable", true) //使能开关机功能，设为 false,则为关闭,缺省为 true

                context.sendBroadcast(intent)
            }
        }

        //定时关机
        /**
         * 参数说明：hour：时(0 <= hour < 24)；minute：分(0 <= minute < 60);enableSchedulPowerOff:使能/关闭定时关机
         * 备注：使能或者关闭定时开关机功能，直接设置第三个参数为true(使能)/false(关闭)
         */
        fun setSchedulePowerOff() {
            //设置 Android 设备 21：30 每天定时关机
            ZtlManager.GetInstance().setSchedulePowerOff(21, 30, false);
        }

        //安装APP并启动安装的APP
        fun installApp(filePath: String) {
            //filePath：APK文件安装路径。
            ZtlManager.GetInstance().installAppAndStartUp(filePath, pkgName);
        }

        //
        fun getSimIccid(context: Context): String {
            return if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_PHONE_STATE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return ""
            } else {
                ZtlManager.GetInstance().simIccid
            }
        }

        fun setDynamicIP() {
            ZtlManager.GetInstance().setEthIP(true, null, null, null, null, null)
        }

        fun restartApp(context: Context) {
            val packageManager: PackageManager = context.packageManager
            val intent =
                packageManager.getLaunchIntentForPackage(context.getPackageName())
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                context.startActivity(intent)
                Process.killProcess(Process.myPid())
            }
        }
    }
}