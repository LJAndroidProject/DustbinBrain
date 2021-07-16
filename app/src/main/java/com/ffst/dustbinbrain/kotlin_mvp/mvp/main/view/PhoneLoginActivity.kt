package com.ffst.dustbinbrain.kotlin_mvp.mvp.main.view

import android.text.TextUtils
import android.view.View
import android.widget.Toast
import com.blankj.utilcode.util.ToastUtils
import com.ffst.dustbinbrain.kotlin_mvp.R
import com.ffst.dustbinbrain.kotlin_mvp.app.DustbinBrainApp
import com.ffst.dustbinbrain.kotlin_mvp.facepass.NetWorkUtil
import com.ffst.dustbinbrain.kotlin_mvp.facepass.PhoneLoginBean
import com.ffst.dustbinbrain.kotlin_mvp.facepass.ServerAddress
import com.ffst.mvp.base.activity.BaseActivity
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_bind_device.*
import kotlinx.android.synthetic.main.activity_phone_login.*
import okhttp3.Call
import java.io.IOException
import java.util.*

/**
 * Created by LiuJW
 *on 2021/7/15
 */
class PhoneLoginActivity :BaseActivity() {
    override fun layoutId(): Int {
        return R.layout.activity_phone_login
    }

    override fun initViewData() {

    }

    fun bindCommit(v: View) {
        when (v.id) {
            R.id.phone_login_btn -> {
                var phone = phone_login.editText?.text
                if (TextUtils.isEmpty(phone) || phone?.length != 11) {
                    ToastUtils.showShort("手机号码格式不符合规范")
                    return
                }
                loadingView(true)
                val hasMap: MutableMap<String, String> = HashMap()
                hasMap["phone"] = phone.toString()
                NetWorkUtil.getInstance()
                    .doPost(ServerAddress.PHONE_LOGIN, hasMap, object :
                        NetWorkUtil.NetWorkListener {
                        override fun success(response: String?) {

                            //  手机号码登录返回结果
                            val phoneLoginBean: PhoneLoginBean =
                                Gson().fromJson(response, PhoneLoginBean::class.java)
                            if (phoneLoginBean.getCode() === 1) {
                                DustbinBrainApp.userId = phoneLoginBean.data.user_id
                                DustbinBrainApp.userType = phoneLoginBean.data.user_type.toLong()
                                loadingView(false)
                                setResult(MainActivity.REQUEST_CODE_PHONE_LOGIN)
                                finish()
                            } else {
                                ToastUtils.showShort("请先使用微信扫描二维码绑定手机号码")
                            }
                        }

                        override fun fail(call: Call?, e: IOException) {
                            Toast.makeText(this@PhoneLoginActivity, e.message, Toast.LENGTH_SHORT).show()
                        }

                        override fun error(e: Exception) {
                            Toast.makeText(this@PhoneLoginActivity, e.message, Toast.LENGTH_SHORT).show()
                        }
                    })
            }
            else -> {

            }
        }
    }
}