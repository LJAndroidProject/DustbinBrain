package com.ffst.dustbinbrain.kotlin_mvp.mvp.main.view

import android.content.Intent
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import com.blankj.utilcode.util.ToastUtils
import com.ffst.dustbinbrain.kotlin_mvp.R
import com.ffst.dustbinbrain.kotlin_mvp.app.DustbinBrainApp
import com.ffst.dustbinbrain.kotlin_mvp.facepass.NetWorkUtil
import com.ffst.dustbinbrain.kotlin_mvp.facepass.PhoneLoginBean
import com.ffst.dustbinbrain.kotlin_mvp.facepass.ServerAddress
import com.ffst.dustbinbrain.kotlin_mvp.utils.QRCodeUtil
import com.ffst.mvp.base.activity.BaseActivity
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_bind_device.*
import kotlinx.android.synthetic.main.activity_face_register.*
import kotlinx.android.synthetic.main.activity_phone_login.*
import okhttp3.Call
import java.io.IOException
import java.util.*

/**
 * Created by LiuJW
 *on 2021/7/15
 */
class FaceRegisterActivity : BaseActivity() {
    override fun layoutId(): Int {
        return R.layout.activity_face_register
    }

    override fun initViewData() {
        qr_login_regist.setImageBitmap(QRCodeUtil.getAppletLoginCode("https://ffadmin.fenfeneco.com/index.php/index/index/weixinRegister?device_id=" + DustbinBrainApp.getDeviceId()))
    }

    fun bindCommit(v: View) {
        when(v.id){
            R.id.back_btn->{
                finish()
            }
            else->{

            }
        }
    }
}