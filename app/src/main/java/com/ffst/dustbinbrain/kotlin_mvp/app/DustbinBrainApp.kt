package com.ffst.dustbinbrain.kotlin_mvp.app

import ZtlApi.ZtlManager
import android.app.Application
import android.content.Context
import com.blankj.utilcode.util.LogUtils
import com.ffst.dustbinbrain.kotlin_mvp.R
import com.ffst.dustbinbrain.kotlin_mvp.bean.DustbinConfig
import com.ffst.dustbinbrain.kotlin_mvp.bean.DustbinStateBean
import com.ffst.dustbinbrain.kotlin_mvp.manager.SerialProManager
import com.ffst.dustbinbrain.kotlin_mvp.utils.SerialPortUtil
import com.scwang.smart.refresh.footer.ClassicsFooter
import com.scwang.smart.refresh.header.MaterialHeader
import com.scwang.smart.refresh.layout.SmartRefreshLayout
import com.tencent.liteav.login.model.ProfileManager
import com.tencent.mmkv.MMKV
import java.util.concurrent.CopyOnWriteArrayList

/**
 * @author catchpig
 * @date 2019/8/18 00:18
 */
class DustbinBrainApp : Application() {

    companion object {
        var userType: Long = 0
        var userId: Int? = 0
        val TAG = "KotlinMvpApp"
        val TAG_RGMTEST = "人工门测试"
        var ApkType = 3

        @Volatile
        var dustbinBeanList: CopyOnWriteArrayList<DustbinStateBean>? = CopyOnWriteArrayList()
        var dustbinConfig: DustbinConfig? = null
        var hasManTime: Long = 0
        fun getDeviceId(): String? {
            if (dustbinConfig == null) {
                return null
            }
            return dustbinConfig!!.dustbinDeviceId
        }

        fun setDustbinState(context: Context, dustbinStateBean: DustbinStateBean) {
            dustbinBeanList?.let {list->
               // val index = it.indexOfFirst {
                //    LogUtils.iTag(TAG_RGMTEST, "获取index")
                  //  it.doorNumber == dustbinStateBean.doorNumber
                //}
                list.find { it.doorNumber == dustbinStateBean.doorNumber }?.let {model->
                    dustbinStateBean.id = model.getId()
                    dustbinStateBean.dustbinBoxType =
                        model.getDustbinBoxType()
                    dustbinStateBean.dustbinBoxNumber =
                        model.getDustbinBoxNumber()
                    list.set(list.indexOf(model),dustbinStateBean)
                }?:let {
                    // 这里相当于 else
                    list.add(dustbinStateBean)
                }
//
//                if (index>=0){
//                    //  dustbinStateBean没有设置 垃圾箱  id 导致为 null
//                    dustbinStateBean.id = it[index].getId()
//                    dustbinStateBean.dustbinBoxType =
//                        it[index].getDustbinBoxType()
//                    dustbinStateBean.dustbinBoxNumber =
//                        it[index].getDustbinBoxNumber()
//                    it.set(index,dustbinStateBean)
//                }
            }
            LogUtils.iTag(TAG_RGMTEST, "开始修改值")
//            for (dsbList in dustbinBeanList!!) {
//                if (dsbList.doorNumber == dustbinStateBean.doorNumber) {
//                    LogUtils.iTag(
//                        TAG_RGMTEST,
//                        dustbinStateBean.doorNumber.toString() + ",之前:" + dsbList.artificialDoor +
//                                ",之后:" + dustbinStateBean.artificialDoor
//                    )
//                    //  如果之前人工门关闭为 true，而新的为 false 说明人工门被打开了
//                    if (dsbList.artificialDoor && !dustbinStateBean.artificialDoor) {
//                        LogUtils.iTag(
//                            TAG_RGMTEST,
//                            dustbinStateBean.doorNumber.toString() + "号门人工门被开启"
//                        )
//                        //  关闭本身的紫外线灯
//                        SerialProManager.getInstance()
//                            .closeTheDisinfection(dustbinStateBean.doorNumber)
//                        //  是否为 奇数
//                        val isOddNumber = dustbinStateBean.doorNumber % 2 != 0
//                        //  奇数 + 1，偶数 -1
//                        val adjoinDoorNumber = if (isOddNumber) 1 else -1
//
//                        SerialPortUtil.getInstance().sendData(
//                            SerialProManager.getInstance()
//                                .closeTheDisinfection(dustbinStateBean.doorNumber + adjoinDoorNumber)
//                        )
//                    }
//
//                    //  如果之前人工门开启为 true，而新的为 false 说明人工门被关闭了
//                    if (!dsbList.artificialDoor && dustbinStateBean.artificialDoor) {
//                        LogUtils.iTag(
//                            TAG_RGMTEST,
//                            dustbinStateBean.doorNumber.toString() + "号门人工门被关闭"
//                        )
//
//                        //  关闭本身的紫外线灯
//                        SerialProManager.getInstance()
//                            .closeTheDisinfection(dustbinStateBean.doorNumber)
//                        //  是否为 奇数
//                        val isOddNumber = dustbinStateBean.doorNumber % 2 != 0
//                        //  奇数 + 1，偶数 -1
//                        val adjoinDoorNumber = if (isOddNumber) 1 else -1
//
//                        SerialPortUtil.getInstance().sendData(
//                            SerialProManager.getInstance()
//                                .openTheDisinfection(dustbinStateBean.doorNumber + adjoinDoorNumber)
//                        )
//                    }
//                }
//
//
//                //  之前没有设置 垃圾箱  id 导致为 null
//                dustbinStateBean.id = dsbList.getId()
//                dustbinStateBean.dustbinBoxType =
//                    dsbList.getDustbinBoxType()
//                dustbinStateBean.dustbinBoxNumber =
//                    dsbList.getDustbinBoxNumber()
//                dustbinBeanList?.set(1, dustbinStateBean)
//            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        LogUtils.dTag(TAG, getFilesDir().getAbsolutePath() + "/fenfen/mmkv")

        MMKV.initialize(this, getFilesDir().getAbsolutePath() + "/fenfen/mmkv");
        //定昌设备初始化
        ZtlManager.GetInstance().setContext(applicationContext)
        ProfileManager.getInstance().initContext(this)
        SerialPortUtil.getInstance().receiveListener {
            SerialProManager.getInstance().inOrderString(applicationContext, it)
        }
    }

    init {
        //设置全局的Header构建器
        SmartRefreshLayout.setDefaultRefreshHeaderCreator { context, layout -> //全局设置主题颜色
            layout.setPrimaryColorsId(R.color.ffst_btn)
            MaterialHeader(context)
        }
        //设置全局的Footer构建器
        SmartRefreshLayout.setDefaultRefreshFooterCreator { context, _ -> //指定为经典Footer，默认是 BallPulseFooter
            ClassicsFooter(context).setDrawableSize(20f)
        }
    }
}