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
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.TimeUtils
import com.blankj.utilcode.util.ToastUtils
import com.ffst.dustbinbrain.kotlin_mvp.R
import com.ffst.dustbinbrain.kotlin_mvp.app.AndroidDeviceSDK
import com.ffst.dustbinbrain.kotlin_mvp.app.DustbinBrainApp
import com.ffst.dustbinbrain.kotlin_mvp.bean.DustbinStateBean
import com.ffst.dustbinbrain.kotlin_mvp.mvp.main.camera.CameraManager
import com.ffst.mvp.base.activity.BaseActivity
import kotlinx.android.synthetic.main.activity_notworktime.*
import java.io.File
import java.math.RoundingMode
import java.text.DecimalFormat
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by LiuJW
 * 非投放时间 刷脸/扫码/手机登录 跳转页面
 *on 2021/6/21
 */
class NotWorkTimeActivity : BaseActivity() {

    private val handler: Handler = Handler(Looper.getMainLooper())

    private var userId: String? = null

    private var facePassImage: String? = null


    override fun layoutId(): Int {
        return R.layout.activity_notworktime
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        if (isTaskRoot) {
            Log.i("isTaskRoot", "true")
            finish()
        } else {
            Log.i("isTaskRoot", "false")
        }
        super.onCreate(savedInstanceState)
    }

    override fun initViewData() {
        noAction()
        userId = intent.getStringExtra("userId")
        facePassImage = intent.getStringExtra("faceImage")
        if (TextUtils.isEmpty(userId)) {
            userId = DustbinBrainApp.userId.toString()
        }
        not_worktime_welcome_textView.text = "欢迎用户 $userId "
        resgit_face_btn.background.alpha = 150
        resgit_face_btn.isEnabled = false
        resgit_face_btn.postDelayed({
            resgit_face_btn.isEnabled = true
            resgit_face_btn.background.alpha = 255
            if(MainActivity.manager?.state == CameraManager.CameraState.OPENING){
                MainActivity.manager?.release()
            }
        }, 1000 * 5)
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

                if(MainActivity.manager?.state == CameraManager.CameraState.OPENING){
                    MainActivity.manager?.release()
                }
                AndroidDeviceSDK.unKeepActivity(this)
                //人脸注册
                //  步骤一：创建存储照片的文件
                val path = Environment.getExternalStorageDirectory().toString()
                val file = File(path, System.currentTimeMillis().toString() + ".jpg")
                if (!file.parentFile.exists()) file.parentFile.mkdirs()
                mUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    //  步骤二：Android 7.0及以上获取文件 Uri
                    LogUtils.e("fileprovider")
                    FileProvider.getUriForFile(
                        this,
                        "com.ffst.dustbinbrain.kotlin_mvp.utils.MyFileProvider",
                        file
                    )
                } else {
                    //  步骤三：获取文件Uri
                    Uri.fromFile(file)
                }
                facePath = file.path
                //  步骤四：调取系统拍照
                val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                intent.putExtra(MediaStore.EXTRA_OUTPUT, mUri)
                startActivityForResult(intent, MainActivity.REQUEST_CODE_CAMERA)
            }
            R.id.control_exit_btn -> {
                //退出控制
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
                Handler(Looper.getMainLooper()).post {
                    if (exitProgressDialog != null) {
                        exitProgressDialog!!.dismiss()
                    }
                }
                //startActivity(new Intent(ControlActivity.this,MainActivity.class));
                val intent = Intent(this@NotWorkTimeActivity, MainActivity::class.java)
                intent.putExtra("faceUri", facePath)
                setResult(300, intent)
                finish()
            }
            else -> {

            }
        }
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
    }


    val DEBUG_TAG_TASK = "定时延迟结算"
    fun exit_time_task() {
        val exitThread: Thread = object : Thread() {
            override fun run() {
                super.run()
                //  1为结算超时 会关闭所有门
                exitEnd(1)
            }
        }
        exitThread.start()
    }

    /**
     * 30 s内没有人自动退出
     */
    var hasManTask: TimerTask? = null
    var hasManTimer = Timer()
    private var hasManIsRun = true
    private val AUTO_EXIT_TIME: Long = 30

    fun noAction() {
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

                //  30 s内没有人
                if (hasManIsRun && System.currentTimeMillis() - DustbinBrainApp.hasManTime > AUTO_EXIT_TIME * 1000) {
                    hasManTask!!.cancel()
                    hasManTimer.cancel()
                    Handler(Looper.getMainLooper()).post {
                        Toast.makeText(
                            this@NotWorkTimeActivity,
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
        val intent = Intent(this@NotWorkTimeActivity, MainActivity::class.java)
        intent.putExtra("exitCode", exitCode)
        setResult(300, intent)
        finish()
    }


    fun getMoneyByYuan(moneyByFen: Long) = getNoMoreThanTwoDigits(moneyByFen / 100.0)
    fun getNoMoreThanTwoDigits(number: Double): String {
        val format = DecimalFormat("0.##")
        //未保留小数的舍弃规则，RoundingMode.FLOOR表示直接舍弃。
        format.roundingMode = RoundingMode.FLOOR
        return format.format(number)
    }

}