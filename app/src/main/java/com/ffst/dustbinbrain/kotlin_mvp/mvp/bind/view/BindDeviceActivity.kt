package com.ffst.dustbinbrain.kotlin_mvp.mvp.bind.view

import android.text.TextUtils
import android.view.View
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.blankj.utilcode.util.ToastUtils
import com.ffst.annotation.ClickGap
import com.ffst.annotation.MethodLog
import com.ffst.annotation.enums.LEVEL
import com.ffst.dustbinbrain.kotlin_mvp.R
import com.ffst.dustbinbrain.kotlin_mvp.app.DustbinBrainApp
import com.ffst.dustbinbrain.kotlin_mvp.bean.DustbinConfig
import com.ffst.dustbinbrain.kotlin_mvp.bean.DustbinStateBean
import com.ffst.dustbinbrain.kotlin_mvp.bean.GetDustbinConfig
import com.ffst.dustbinbrain.kotlin_mvp.constants.MMKVCommon
import com.ffst.dustbinbrain.kotlin_mvp.mvp.bind.viewmodel.BindActivityViewModel
import com.ffst.dustbinbrain.kotlin_mvp.mvp.main.view.MainActivity
import com.ffst.dustbinbrain.kotlin_mvp.utils.DataBaseUtil
import com.ffst.dustbinbrain.kotlin_mvp.utils.FenFenCommonUtil
import com.ffst.mvp.base.activity.BaseActivity
import com.ffst.utils.ext.startKtActivity
import com.tencent.mmkv.MMKV
import kotlinx.android.synthetic.main.activity_bind_device.*
import java.util.*


class BindDeviceActivity : BaseActivity() {

    var viewModel: BindActivityViewModel? = null
    var mmkv: MMKV? = null

    override fun layoutId(): Int {
        return R.layout.activity_bind_device
    }

    override fun initViewData() {
        viewModel = ViewModelProvider(this)[BindActivityViewModel::class.java]
        mmkv = MMKV.defaultMMKV()
        bind_device_id.editText?.setText(mmkv?.decodeString(MMKVCommon.DEVICE_ID))
        bind_device_auth.editText?.setText(mmkv?.decodeString(MMKVCommon.MANGE_CODE))
        bindData()
    }

    fun bindData() {
        viewModel?.liveDataForDustbinConfig?.observe(this) { data ->
            if (data.success) {
                if (data.configData?.code == 1) {
                    val id = bind_device_id.editText?.text
                    val auth = bind_device_auth.editText?.text
                    mmkv?.encode(MMKVCommon.DEVICE_ID, id.toString())
                    mmkv?.encode(MMKVCommon.MANGE_CODE, auth.toString())
                    val getDustbinConfig: GetDustbinConfig = data.configData!!
                    val list: MutableList<DustbinStateBean> = ArrayList<DustbinStateBean>()
                    val listBeans: List<GetDustbinConfig.DataBean.ListBean> =
                        getDustbinConfig.data!!.list!!
                    mmkv?.encode(MMKVCommon.IM_USERID, getDustbinConfig.data!!.id)
                    mmkv?.encode(MMKVCommon.DEVICE_NAME, getDustbinConfig.data!!.device_name)
                    for (listBean in listBeans) {

                        //  ?????????id   ???????????????
                        val id: Long = listBean.id
                        //  ????????????    ???????????????????????????
                        val number: Int = listBean.bin_code!!.toInt()
                        //  ??????????????? ?????? ?????????????????????????????????????????????
                        val typeString: String =
                            FenFenCommonUtil.getDustbinType(listBean.bin_type!!).toString()
                        //  ??????????????? ??????A1 A2 B3 B5 C5 D6 D7 D8
                        val typeNumber: String = listBean.bin_type!!
                        list.add(
                            DustbinStateBean(
                                id,
                                number,
                                typeString,
                                typeNumber,
                                0.0,
                                0.0,
                                0.0,
                                0,
                                false,
                                false,
                                false,
                                false,
                                false,
                                false,
                                false,
                                false
                            )
                        )
                    }
                    DataBaseUtil.getInstance(this).setDustBinStateConfig(list)


                    val dustbinConfig = DustbinConfig()
                    if (listBeans.isNotEmpty()) {
                        dustbinConfig.dustbinDeviceId = listBeans[0].device_id //  deviceID
                    }
                    dustbinConfig.setDustbinDeviceName(
                        getDustbinConfig.data!!.device_name
                    ) //  deviceName ????????????????????????
                    DataBaseUtil.getInstance(this)
                        .getDaoSession()!!.dustbinConfigDao.insertOrReplace(dustbinConfig) //  ????????????

                    if(DustbinBrainApp.dustbinBeanList?.isNotEmpty() == true){
                        DustbinBrainApp.dustbinBeanList!!.clear()
                    }
                    goMainActivity()
                }
                hideLoadingView()
                Toast.makeText(this, data.configData?.msg, Toast.LENGTH_SHORT)
                    .show()
            } else {
                hideLoadingView()
                Toast.makeText(this, data.msg, Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun goMainActivity() {
        startKtActivity<MainActivity>()
        finish()
    }

    @MethodLog(LEVEL.V)
    @ClickGap(300)
    fun bindCommit(v: View) {
        when (v.id) {
            R.id.bind_device_id_commit -> {
                //????????????????????????????????????
                val id = bind_device_id.editText?.text
                val auth = bind_device_auth.editText?.text
                if (TextUtils.isEmpty(id) || TextUtils.isEmpty(auth)) {
                    ToastUtils.showShort("??????????????????")
                    return
                }
                loadingView(true)
                viewModel?.getDustbinConfig(id.toString(), auth.toString())
            }
            else -> {

            }
        }
    }

}