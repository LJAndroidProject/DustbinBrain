package com.ffst.dustbinbrain.kotlin_mvp.mvp.bind.view

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.ffst.annotation.ClickGap
import com.ffst.annotation.MethodLog
import com.ffst.annotation.enums.LEVEL
import com.ffst.dustbinbrain.kotlin_mvp.R
import com.ffst.dustbinbrain.kotlin_mvp.constants.MMKVCommon
import com.ffst.dustbinbrain.kotlin_mvp.mvp.main.view.MainActivity
import com.ffst.dustbinbrain.kotlin_mvp.mvp.test.SerialProtTestActivity
import com.ffst.utils.ext.startKtActivity
import com.tencent.mmkv.MMKV

class DeviceMannageActivity : AppCompatActivity() {
    /* 程序所需权限 ：相机 文件存储 网络访问 */
    private val PERMISSIONS_REQUEST = 1
    private val PERMISSION_CAMERA = Manifest.permission.CAMERA
    private val PERMISSION_WRITE_STORAGE = Manifest.permission.WRITE_EXTERNAL_STORAGE
    private val PERMISSION_READ_STORAGE = Manifest.permission.READ_EXTERNAL_STORAGE
    private val PERMISSION_INTERNET = Manifest.permission.INTERNET
    private val PERMISSION_ACCESS_NETWORK_STATE = Manifest.permission.ACCESS_NETWORK_STATE
    private val Permission = arrayOf(
        PERMISSION_CAMERA,
        PERMISSION_WRITE_STORAGE,
        PERMISSION_READ_STORAGE,
        PERMISSION_INTERNET,
        PERMISSION_ACCESS_NETWORK_STATE,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )
    var mmkv: MMKV? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mmkv = MMKV.defaultMMKV()
        var isDebug = false
        isDebug = intent.getBooleanExtra("isDebug",false)
        if(!TextUtils.isEmpty(mmkv?.decodeString(MMKVCommon.DEVICE_ID)) && !isDebug){
            startKtActivity<MainActivity>()
            finish()
        }
        setContentView(R.layout.activity_device_mannage)
        if(!hasPermission()){
            requestPermission()
        }
    }

    @MethodLog(LEVEL.V)
    @ClickGap(0)
    fun openChild(v: View) {
        when (v.id) {
            R.id.main_bind_dev -> {
                startKtActivity<BindDeviceActivity>()
                finish()
            }
            R.id.main_dev_debug -> {
                startKtActivity<SerialProtTestActivity>()
                finish()
            }
            R.id.main_tpm -> {

            }
            else->{

            }
        }
    }

    /* 请求程序所需权限 */
    private fun requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(Permission, PERMISSIONS_REQUEST)
        }
    }

    /* 判断程序是否有所需权限 android22以上需要自申请权限 */
    private fun hasPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkSelfPermission(PERMISSION_CAMERA) == PackageManager.PERMISSION_GRANTED && checkSelfPermission(
                PERMISSION_READ_STORAGE
            ) == PackageManager.PERMISSION_GRANTED && checkSelfPermission(PERMISSION_WRITE_STORAGE) == PackageManager.PERMISSION_GRANTED && checkSelfPermission(
                PERMISSION_INTERNET
            ) == PackageManager.PERMISSION_GRANTED && checkSelfPermission(PERMISSION_ACCESS_NETWORK_STATE) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }
}