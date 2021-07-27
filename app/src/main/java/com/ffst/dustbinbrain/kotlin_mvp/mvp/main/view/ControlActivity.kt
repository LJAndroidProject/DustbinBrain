package com.ffst.dustbinbrain.kotlin_mvp.mvp.main.view

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.TimeUtils
import com.blankj.utilcode.util.ToastUtils
import com.ffst.dustbinbrain.kotlin_mvp.R
import com.ffst.dustbinbrain.kotlin_mvp.adapter.DustbinControlItemAdapter
import com.ffst.dustbinbrain.kotlin_mvp.app.AndroidDeviceSDK
import com.ffst.dustbinbrain.kotlin_mvp.app.DustbinBrainApp
import com.ffst.dustbinbrain.kotlin_mvp.bean.DustBinRecordRequestParams
import com.ffst.dustbinbrain.kotlin_mvp.bean.DustbinENUM
import com.ffst.dustbinbrain.kotlin_mvp.bean.DustbinStateBean
import com.ffst.dustbinbrain.kotlin_mvp.manager.SerialProManager
import com.ffst.dustbinbrain.kotlin_mvp.utils.DataBaseUtil
import com.ffst.dustbinbrain.kotlin_mvp.utils.DustbinUtil
import com.ffst.dustbinbrain.kotlin_mvp.utils.SerialPortUtil
import com.ffst.dustbinbrain.kotlin_mvp.utils.VoiceUtil
import com.ffst.mvp.base.activity.BaseActivity
import kotlinx.android.synthetic.main.activity_control.*
import org.greenrobot.eventbus.EventBus
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by LiuJW
 *on 2021/6/21
 */
class ControlActivity : BaseActivity() {

    private var beforeDustbinStateBeans: List<DustbinStateBean>? = null
    private val handler: Handler = Handler(Looper.getMainLooper())
    private var dustbinControlItemAdapter: DustbinControlItemAdapter? = null

    //  需要关闭的垃圾箱列表
    private val needCloseDustbin = ArrayList<DustbinStateBean>()

    private var userId: String? = null

    private var facePassImage: String? = null


    override fun layoutId(): Int {
        return R.layout.activity_control
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        //关闭紫外线（后期由主板控制）
//        SerialProManager.getInstance().closeTheDisinfection(1)
        beforeDustbinStateBeans = DustbinBrainApp.dustbinBeanList
        //开门后关闭所有紫外线灯 作废：开门后关闭需要开门的紫外线就好
//        for (dustbin in DustbinBrainApp.dustbinBeanList!!) {
//            SerialProManager.getInstance().closeTheDisinfection(dustbin.doorNumber)
//        }
        //
        if (isTaskRoot) {
            Log.i("isTaskRoot", "true")
            finish()
        } else {
            Log.i("isTaskRoot", "false")
        }
        super.onCreate(savedInstanceState)
    }

    override fun initViewData() {
        hasMan()
        openDefaultDoor()
        val dustbinStateBeans: ArrayList<DustbinStateBean> =
            DataBaseUtil.getInstance(this).daoSession.dustbinStateBeanDao.queryBuilder()
                .list() as ArrayList<DustbinStateBean>
        dustbinControlItemAdapter = DustbinControlItemAdapter(
            R.layout.item_control_dustbin,
            removeDuplicateUser(dustbinStateBeans)
        )
        //点击垃圾箱类型开启对应垃圾箱
        dustbinControlItemAdapter!!.setOnItemChildClickListener { adapter, view, position ->
            if (view.id == R.id.control_item_iv) {
                val data = dustbinControlItemAdapter!!.data.get(position)
                var dustbinStateBean = openDoorByType(data.dustbinBoxType)
                if (dustbinStateBean == null) {
                    Toast.makeText(this@ControlActivity, "没有合适的垃圾箱", Toast.LENGTH_SHORT).show()
                    return@setOnItemChildClickListener
                }
                //开门关闭消毒，开启照明
                resgit_face_btn.postDelayed(object : Runnable {
                    override fun run() {
                        SerialProManager.getInstance()
                            .closeTheDisinfection(dustbinStateBean.doorNumber)
                    }
                }, 0)
                resgit_face_btn.postDelayed(object : Runnable {
                    override fun run() {
                        SerialProManager.getInstance().openLight(dustbinStateBean.doorNumber)
                    }
                }, 30)
                addNeedCloseDustbin(dustbinStateBean)
                resgit_face_btn.postDelayed(object : Runnable {
                    override fun run() {
                        SerialProManager.getInstance().openDoor(dustbinStateBean.doorNumber)
                    }
                }, 60)
            }
        }
        dustbin_list.layoutManager = GridLayoutManager(this, 2)
        dustbin_list.adapter = dustbinControlItemAdapter
        val intent = getIntent()
        userId = intent.getStringExtra("userId")
        facePassImage = intent.getStringExtra("faceImage")
        if (TextUtils.isEmpty(userId)) {
            userId = DustbinBrainApp.userId.toString()
        }
        control_welcome_textView.text = "欢迎用户 $userId 进入操作界面"
    }

    private fun openDefaultDoor() {

        /*
         * 开其它
         * */
        DustbinBrainApp.dustbinBeanList?.let { list ->
            list.find { it.dustbinBoxType.equals(DustbinENUM.OTHER.toString()) && it.isFull && it.doorNumber != 0 }
                ?.let { model ->
                    addNeedCloseDustbin(model)

                    resgit_face_btn.postDelayed(object : Runnable {
                        override fun run() {
                            //关消毒
                            SerialProManager.getInstance().closeTheDisinfection(model.doorNumber)
                        }
                    }, 0)
                    resgit_face_btn.postDelayed(object : Runnable {
                        override fun run() {
                            SerialProManager.getInstance().openLight(model.doorNumber)
                        }
                    }, 30)
                    resgit_face_btn.postDelayed(object : Runnable {
                        override fun run() {
                            SerialProManager.getInstance().openDoor(model.doorNumber)
                        }
                    }, 60)

                } ?: let {
                Toast.makeText(
                    this,
                    "其他垃圾箱已满",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }


//        for (dustbinStateBean in DustbinBrainApp.dustbinBeanList!!) {
//            if (dustbinStateBean.dustbinBoxType.equals(DustbinENUM.OTHER.toString())) {
//
//                //  为什么不为 0 呢 ，0 号桶位置作废
//                if (dustbinStateBean.isFull && dustbinStateBean.doorNumber != 0) {
//                    //  添加需要关闭的垃圾箱
//                    addNeedCloseDustbin(dustbinStateBean)
//                    otherIsFull = false
//                    //  开灯
//                    SerialProManager.getInstance().openLight(dustbinStateBean.doorNumber)
//                    //开门
//                    SerialProManager.getInstance().openDoor(dustbinStateBean.doorNumber)
//                    break
//                }
//            }
//        }
//        if (otherIsFull) {
//            Toast.makeText(
//                this,
//                (DustbinENUM.OTHER.toString()) + "垃圾箱已满",
//                Toast.LENGTH_SHORT
//            ).show()
//        }

        /*
         *
         * 开餐厨
         * */
//        var kitchenIsFull = true
        DustbinBrainApp.dustbinBeanList?.let { list ->
            list.find { it.dustbinBoxType.equals(DustbinENUM.KITCHEN.toString()) && it.isFull && it.doorNumber != 0 }
                ?.let { model ->
                    addNeedCloseDustbin(model)
//                otherIsFull = false


                    resgit_face_btn.postDelayed(object : Runnable {
                        override fun run() {
                            //关消毒
                            SerialProManager.getInstance().closeTheDisinfection(model.doorNumber)
                        }
                    }, 0)
                    resgit_face_btn.postDelayed(object : Runnable {
                        override fun run() {
                            SerialProManager.getInstance().openLight(model.doorNumber)
                        }
                    }, 30)
                    resgit_face_btn.postDelayed(object : Runnable {
                        override fun run() {
                            SerialProManager.getInstance().openDoor(model.doorNumber)
                        }
                    }, 60)
                } ?: let {
                Toast.makeText(
                    this,
                    "厨余垃圾箱已满",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
//        for (dustbinStateBean in DustbinBrainApp.dustbinBeanList!!) {
//            if (dustbinStateBean.dustbinBoxType.equals(DustbinENUM.KITCHEN.toString())) {
//
//                //  为什么不为 0 呢 ，0 号桶位置作废
//                if (dustbinStateBean.isFull && dustbinStateBean.doorNumber != 0) {
//                    kitchenIsFull = false
//                    //  添加需要关闭的垃圾箱
//                    addNeedCloseDustbin(dustbinStateBean)
//
//                    //  开灯
//                    SerialProManager.getInstance().openLight(dustbinStateBean.doorNumber)
//                    //开门
//                    SerialProManager.getInstance().openDoor(dustbinStateBean.doorNumber)
//                    break
//                }
//            }
//        }
//        if (kitchenIsFull) {
//            Toast.makeText(
//                this,
//                DustbinENUM.KITCHEN.toString() + "垃圾箱已满",
//                Toast.LENGTH_SHORT
//            ).show()
//        }
    }

    /**
     * 退出投递
     */
    //  退出投递弹窗
    private var exitProgressDialog: ProgressDialog? = null

    //  开始退出时间
    private var beginExitTime: Long = 0
    private var timerTask: TimerTask? = null
    private var timer: Timer? = null
    var mUri: Uri? = null
    var facePath: String? = null
    fun onControl(view: View) {
        val viewId = view.id
        when (viewId) {
            R.id.resgit_face_btn -> {

                AndroidDeviceSDK.unKeepActivity()
                //人脸注册
                //  步骤一：创建存储照片的文件
                val path = Environment.getExternalStorageDirectory().toString()
                val file = File(path, System.currentTimeMillis().toString() + ".jpg")
                if (!file.parentFile.exists()) file.parentFile.mkdirs()
                mUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    //  步骤二：Android 7.0及以上获取文件 Uri
                    FileProvider.getUriForFile(this, "$packageName.fileprovider", file)
                } else {
                    //  步骤三：获取文件Uri
                    Uri.fromFile(file)
                }
                facePath = file.path
                //  步骤四：调取系统拍照
                val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                intent.putExtra(MediaStore.EXTRA_OUTPUT, mUri)
                startActivityForResult(intent, MainActivity.REQUEST_CODE_CAMERA)
//                closeAllDoor()
//                finish()
            }
            R.id.control_exit_btn -> {
                //退出控制
                VoiceUtil.getInstance()
                    .openAssetMusics(this@ControlActivity, "exit_alert_voice.aac")

                exitProgressDialog = ProgressDialog(this)
                exitProgressDialog!!.setCancelable(false)
                exitProgressDialog!!.setProgressStyle(ProgressDialog.STYLE_SPINNER)
                exitProgressDialog!!.setTitle("提示")
                exitProgressDialog!!.setMessage("正在退出与结算积分...")
                exitProgressDialog!!.create()
                exitProgressDialog!!.show()


                beginExitTime = System.currentTimeMillis()

                timerTask = object : TimerTask() {
                    override fun run() {
                        handler.post {
                            val timeDiff = (System.currentTimeMillis() - beginExitTime) / 1000
                            if (exitProgressDialog != null) {
                                exitProgressDialog!!.setTitle("结算中 ( " + timeDiff + "s )")
                                //  一般一个桶 6-7 s就可以关闭了，所有桶位的数量 * 8
                                if (timeDiff > DustbinBrainApp.dustbinBeanList!!.size * 9) {
                                    timer!!.cancel()
                                    timerTask!!.cancel()
//                                    Toast.makeText(
//                                        this@ControlActivity,
//                                        "结算超时",
//                                        Toast.LENGTH_SHORT
//                                    ).show()
                                    exitEnd(1)
                                }
                            }
                        }
                    }
                }

                timer = Timer()
                timer!!.schedule(timerTask, 1, 1000)
                exit_time_task()
            }
            else -> {

            }
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            MainActivity.REQUEST_CODE_CAMERA -> {
                AndroidDeviceSDK.keepActivity(this)
                LogUtils.e("注册返回")
                closeOpenDoor()

                Handler(Looper.getMainLooper()).post {
                    if (exitProgressDialog != null) {
                        exitProgressDialog!!.dismiss()
                    }
                }
                //startActivity(new Intent(ControlActivity.this,MainActivity.class));
                val intent = Intent(this@ControlActivity, MainActivity::class.java)
                intent.putExtra("faceUri", facePath)
                setResult(300, intent)
                finish()
            }
            else -> {

            }
        }
    }

    fun closeOpenDoor() {
        //  关闭已经开启的门
        object : Thread() {
            override fun run() {
                super.run()

                for (dustbinStateBean in needCloseDustbin) {

                    SerialProManager.getInstance()
                        .closeDoor(dustbinStateBean.doorNumber)

                    try {
                        sleep(10)
                    } catch (e: java.lang.Exception) {
                        e.printStackTrace()
                    }

                    //  开排气扇

                    SerialProManager.getInstance()
                        .openExhaustFan(dustbinStateBean.doorNumber)

                    try {
                        sleep(10)
                    } catch (e: java.lang.Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }.start()
    }

    //  关闭所有门
    private fun closeAllDoor() {
        //  启动APP就关闭所有门
        object : Thread() {
            override fun run() {
                super.run()

                for (dustbinStateBean in DustbinBrainApp.dustbinBeanList!!) {

                    resgit_face_btn.postDelayed(object : Runnable {
                        override fun run() {
                            SerialProManager.getInstance()
                                .closeDoor(dustbinStateBean.doorNumber)
                        }
                    }, 0)


                    //  开排气扇


                    resgit_face_btn.postDelayed(object : Runnable {
                        override fun run() {
                            SerialProManager.getInstance()
                                .openExhaustFan(dustbinStateBean.doorNumber)
                        }
                    }, 100)
                }
            }
        }.start()
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        DustbinBrainApp.hasManTime = System.currentTimeMillis()
        hasManIsRun = true
        LogUtils.dTag(
            "改变倒计时",
            "DustbinBrainApp.hasManTime:${TimeUtils.millis2Date(DustbinBrainApp.hasManTime)}"
        )
        return super.dispatchTouchEvent(ev)
    }

    override fun onRestart() {
        super.onRestart()
        DustbinBrainApp.hasManTime = System.currentTimeMillis()
        hasManIsRun = true
        LogUtils.dTag(
            "改变倒计时",
            "DustbinBrainApp.hasManTime:${TimeUtils.millis2Date(DustbinBrainApp.hasManTime)}"
        )
    }

    override fun onStop() {
        super.onStop()
        hasManIsRun = false
    }

    override fun onDestroy() {
        super.onDestroy()
        hasManTask?.cancel()
        for (dustbin in DustbinBrainApp.dustbinBeanList!!) {
//            SerialProManager.getInstance().openTheDisinfection(dustbin.doorNumber)
        }
    }


    val DEBUG_TAG_TASK = "定时延迟结算"
    fun exit_time_task() {
        val exitThread: Thread = object : Thread() {
            override fun run() {
                super.run()
                for (dustbinStateBean in needCloseDustbin /* 之前是关闭当前状态为开的门 APP.dustbinBeanList*/) {

                    //  如果人工门没有开，才开紫外线灯
                    if (dustbinStateBean.doorIsOpen) {
                        //  开消毒灯
                        val i: Int = DustbinUtil.getLeftOrRight(dustbinStateBean.doorNumber)



                        resgit_face_btn.postDelayed(object : Runnable {
                            override fun run() {
                                SerialProManager.getInstance().openElectromagnetism(i)
                            }
                        }, 0)

                        resgit_face_btn.postDelayed(object : Runnable {
                            override fun run() {
                                SerialProManager.getInstance()
                                    .openElectromagnetism(dustbinStateBean.doorNumber)
                            }
                        }, 100)


                    }

                    //  如果是开门的
                    if (true /* 之前是关闭当前状态为开的门 dustbinStateBean.getDoorIsOpen()*/) {
                        Log.i(
                            DEBUG_TAG_TASK,
                            dustbinStateBean.doorNumber.toString() + "是开的"
                        )

                        //  时间
                        val time = System.currentTimeMillis() / 1000
                        //  文件名称
                        val imageName: String = DustbinBrainApp.getDeviceId()
                            .toString() + "_" + dustbinStateBean.doorNumber + "_" + userId + "_" + time + "_" + dustbinStateBean.id + ".jpg"
                        Log.i(
                            DEBUG_TAG_TASK,
                            dustbinStateBean.doorNumber.toString() + "开始关门"
                        )

                        //  关门
                        SerialProManager.getInstance().closeDoor(dustbinStateBean.doorNumber)
                        resgit_face_btn.postDelayed(object : Runnable {
                            override fun run() {
                                //  开补光灯
                                SerialProManager.getInstance().openLight(dustbinStateBean.doorNumber)
                            }
                        }, 200)



                        Log.i(
                            DEBUG_TAG_TASK,
                            dustbinStateBean.doorNumber.toString() + "开始添加投递记录"
                        )

                        //  添加投递记录
                        addRecord(dustbinStateBean, time)

                        //  拍照
                        val intent = Intent("MY_BROADCAST_RECEIVER")
                        intent.putExtra("type", "broadcast_camera_type")
                        intent.putExtra("data", "")
                        intent.putExtra("doorNumber", dustbinStateBean.doorNumber)
                        intent.putExtra("time", time)
                        intent.putExtra("bin_id", dustbinStateBean.id)
                        //打印time是为了确认 拍照广播 是否收到且是否一致
                        Log.i(
                            "拍照调试",
                            "实际传输的bin_id:" + dustbinStateBean.id.toString() + ", time:" + time
                        )
//                        sendBroadcast(intent)
                        try {
                            sleep(500)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
                Log.i(DEBUG_TAG_TASK, "take picture 进入关闭")
                //  1为结算超时 会关闭所有门
                exitEnd(1)
            }
        }
        exitThread.start()
    }

    fun addNeedCloseDustbin(dustbinStateBean: DustbinStateBean) {
        //  如果为 0 则直接添加
        if (needCloseDustbin.size == 0) {
            needCloseDustbin.add(dustbinStateBean)
        } else {

            //  查找该垃圾箱是否已经被添加进去，如果有则直接返回
            for (dustbinStateBeanChild in needCloseDustbin) {
                if (dustbinStateBeanChild.doorNumber == dustbinStateBean.doorNumber) {
                    return
                }
            }

            //  如果能执行到这里说明还没有被加入进去，则添加该垃圾箱
            needCloseDustbin.add(dustbinStateBean)
            Thread {
                val i: Int = DustbinUtil.getLeftOrRight(dustbinStateBean.doorNumber)
                SerialProManager.getInstance().closeElectromagnetism(i)

                resgit_face_btn.postDelayed(object : Runnable {
                    override fun run() {
                        SerialProManager.getInstance().closeElectromagnetism(dustbinStateBean.doorNumber)
                    }
                }, 200)

            }.start()
        }
    }

    /**
     * 30 s内没有人自动退出
     */
    var hasManTask: TimerTask? = null
    var hasManTimer = Timer()
    private var hasManIsRun = true
    private val AUTO_EXIT_TIME: Long = 60

    fun hasMan() {
        hasManTask = object : TimerTask() {
            override fun run() {
                LogUtils.iTag(
                    "hasMan",
                    "${System.currentTimeMillis() / 1000}, ${DustbinBrainApp.hasManTime}"
                )
                handler.post {
                    var time = (System.currentTimeMillis() - DustbinBrainApp.hasManTime) / 1000
                    if ((AUTO_EXIT_TIME - time) < 0.toLong()) {
                        if (timer != null) {
                            timer!!.cancel()
                        }

                        if (timerTask != null) {
                            timerTask!!.cancel()
                        }
                        exitEnd(1)
                    }
                    control_exit_btn.text =
                        "退出 ( " + (AUTO_EXIT_TIME - (System.currentTimeMillis() - DustbinBrainApp.hasManTime) / 1000).toString() + "s )"
                }

                //剩余10s提示语音
                if (hasManIsRun && (AUTO_EXIT_TIME - (System.currentTimeMillis() - DustbinBrainApp.hasManTime) / 1000) == 10.toLong()) {
                    VoiceUtil.getInstance()
                        .openAssetMusics(this@ControlActivity, "exit_alert_voice.aac")
                }
                //  30 s内没有人
                if (hasManIsRun && System.currentTimeMillis() - DustbinBrainApp.hasManTime > AUTO_EXIT_TIME * 1000) {
                    hasManTask!!.cancel()
                    hasManTimer.cancel()
                    Handler(Looper.getMainLooper()).post {
                        Toast.makeText(
                            this@ControlActivity,
                            "无人，自动结算",
                            Toast.LENGTH_SHORT
                        ).show()
                        //  开始结算
                        onControl(control_exit_btn)
                    }
                }
            }

        }

        hasManTimer = Timer()
        hasManTimer.schedule(hasManTask, 1, 1000)
    }

    /**
     * 垃圾箱类型 去重
     */
    fun removeDuplicateUser(list: List<DustbinStateBean>?): ArrayList<DustbinStateBean?> {
        val set: MutableSet<DustbinStateBean> = TreeSet { o1, o2 ->
            o1.dustbinBoxType
                .compareTo(o2.dustbinBoxType)
        }
        set.addAll(list!!)
        return ArrayList(set)
    }

    /**
     * 筛选合适的垃圾箱
     */
    private fun openDoorByType(type: String): DustbinStateBean? {
        var isFull = true
        DustbinBrainApp.dustbinBeanList?.let { list ->
            list.find { it.dustbinBoxType.equals(type) && it.isFull }?.let { model ->
                return model
            } ?: let {
                ToastUtils.showShort(type + "箱已满")
                return null
            }
        }
//        if (DustbinBrainApp.dustbinBeanList != null && DustbinBrainApp.dustbinBeanList!!.isNotEmpty()) {
//            LogUtils.iTag("筛选打开类型未满的垃圾箱")
//
//            for (dustbinBean in DustbinBrainApp.dustbinBeanList!!) {
//                if (dustbinBean.dustbinBoxType === type) {
//                    if (dustbinBean.isFull) {
//                        isFull = false
//                        return dustbinBean
//                    }
//                }
//            }
//            if (isFull) {
//                ToastUtils.showShort(type + "箱已满")
//                return null
//            }
//        } else {
//            ToastUtils.showShort("垃圾箱配置为空")
//        }
        return null
    }

    /**
     * 退出清算,将一些数据置为空
     *
     * @param exitCode 1为结算超时 ， 0 为结算正常
     */
    private fun exitEnd(exitCode: Int) {
        //  因为可能在子线程中被调用
        Handler(Looper.getMainLooper()).post {
            if (exitProgressDialog != null) {
                exitProgressDialog!!.dismiss()
            }
        }
        //startActivity(new Intent(ControlActivity.this,MainActivity.class));
        val intent = Intent(this@ControlActivity, MainActivity::class.java)
        intent.putExtra("exitCode", exitCode)
        setResult(300, intent)
        finish()
    }

    /**
     * 获取投递之前的某个垃圾桶参数
     */
    private fun getBeforeDustbin(doorNumber: Int): DustbinStateBean? {
        for (dustbinStateBean in beforeDustbinStateBeans!!) {
            if (dustbinStateBean.doorNumber == doorNumber) {
                return dustbinStateBean
            }
        }
        return null
    }

    fun addRecord(dustbinStateBean: DustbinStateBean, time: Long) {

        //  重量差
        var diff = 0.0

        //  获取垃圾箱之前的状态
        val beforeDustbin: DustbinStateBean? = getBeforeDustbin(dustbinStateBean.doorNumber)
        if (beforeDustbin != null) {
            diff = dustbinStateBean.dustbinWeight - beforeDustbin.dustbinWeight
        }
        /*user_id	是	int	用户ID
        device_id	是	string	设备ID
        bin_id	是	int	垃圾箱ID
        bin_type	是	string	垃圾箱分类 ABCDEF
        post_weight	否	float	投放重量
        former_weight	否	float	原来的重量
        now_weight	否	float	现在的重量
        plastic_bottle_num	否	int	瓶子的个数
        rubbish_image	否	string	垃圾图片
        timestamp	否	string	当前时间戳*/
        val map = mutableMapOf<String, String>()
        map.put("user_id", DustbinBrainApp.userId.toString())
        map.put("bin_id", dustbinStateBean.id.toString())
        map.put("bin_type", dustbinStateBean.dustbinBoxNumber)
        map.put("post_weight", diff.toString())
        map.put("former_weight", (dustbinStateBean.dustbinWeight - diff).toString())
        map.put("now_weight", dustbinStateBean.dustbinWeight.toString())
        map.put("plastic_bottle_num", "0")
        map["err_code"] =
            if (dustbinStateBean.closeFailNumber == 0) "0" else "1" ////  0是正常的，其它是不正常的 1、2、3 对应一个err_msg
        map["err_msg"] =
            if (dustbinStateBean.closeFailNumber == 0) "无描述" else "关门失败，结算异常" //    异常描述

        map["time"] = time.toString()
        map.put("rubbish_image", " ")
        if (!TextUtils.isEmpty(facePassImage)) {
            map.put("user_pictrue", facePassImage!!)
        }
        val recordRequestParams = DustBinRecordRequestParams()
        recordRequestParams.setRequestMap(map)
        EventBus.getDefault().post(recordRequestParams)
    }
}