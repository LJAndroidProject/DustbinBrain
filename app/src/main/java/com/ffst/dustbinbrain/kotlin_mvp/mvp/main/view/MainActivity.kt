package com.ffst.dustbinbrain.kotlin_mvp.mvp.main.view

import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.RectF
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import android.provider.Settings
import android.text.SpannableString
import android.text.Spanned
import android.text.TextUtils
import android.text.style.TypefaceSpan
import android.util.Base64
import android.util.DisplayMetrics
import android.util.Log
import android.util.LruCache
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import com.android.volley.toolbox.ImageLoader
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.TimeUtils
import com.blankj.utilcode.util.ToastUtils
import com.ffst.annotation.StatusBar
import com.ffst.dustbinbrain.kotlin_mvp.R
import com.ffst.dustbinbrain.kotlin_mvp.app.AndroidDeviceSDK
import com.ffst.dustbinbrain.kotlin_mvp.app.DustbinBrainApp
import com.ffst.dustbinbrain.kotlin_mvp.bean.*
import com.ffst.dustbinbrain.kotlin_mvp.constants.MMKVCommon
import com.ffst.dustbinbrain.kotlin_mvp.facepass.ImageUploadResult
import com.ffst.dustbinbrain.kotlin_mvp.facepass.NetWorkUtil
import com.ffst.dustbinbrain.kotlin_mvp.facepass.ResultMould
import com.ffst.dustbinbrain.kotlin_mvp.facepass.ServerAddress
import com.ffst.dustbinbrain.kotlin_mvp.manager.SerialProManager
import com.ffst.dustbinbrain.kotlin_mvp.manager.ThreadManager
import com.ffst.dustbinbrain.kotlin_mvp.mvp.bind.view.DeviceMannageActivity
import com.ffst.dustbinbrain.kotlin_mvp.mvp.main.camera.CameraManager
import com.ffst.dustbinbrain.kotlin_mvp.mvp.main.camera.CameraPreviewData
import com.ffst.dustbinbrain.kotlin_mvp.mvp.main.camera.SettingVar
import com.ffst.dustbinbrain.kotlin_mvp.mvp.main.viewmodel.MainActivityViewModel
import com.ffst.dustbinbrain.kotlin_mvp.mvp.main.widget.CicleViewOutlineProvider
import com.ffst.dustbinbrain.kotlin_mvp.mvp.main.widget.FaceView
import com.ffst.dustbinbrain.kotlin_mvp.service.CallService
import com.ffst.dustbinbrain.kotlin_mvp.service.ResidentService
import com.ffst.dustbinbrain.kotlin_mvp.utils.*
import com.ffst.mvp.base.activity.BaseActivity
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.littlegreens.netty.client.listener.NettyClientListener
import com.littlegreens.netty.client.status.ConnectState
import com.tencent.liteav.login.model.ProfileManager
import com.tencent.liteav.login.ui.ProfileActivity
import com.tencent.liteav.trtccalling.ui.videocall.TRTCVideoCallActivity
import com.tencent.mmkv.MMKV
import kotlinx.android.synthetic.main.activity_main.*
import mcv.facepass.FacePassException
import mcv.facepass.FacePassHandler
import mcv.facepass.types.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.greenrobot.eventbus.EventBus
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.util.*
import java.util.concurrent.ArrayBlockingQueue


@StatusBar(enabled = true)
class MainActivity : BaseActivity(), CameraManager.CameraListener {

    companion object {
        val SDK_MODE: FacePassSDKMode = FacePassSDKMode.MODE_OFFLINE
        const val DEBUG_TAG: String = FenFenCommonUtil.FACE_TAG
        const val MY_TAG = "人脸识别调试"
        val group_name = FenFenCommonUtil.face_group_name
        val CONTROL_RESULT_CODE = 300

        //  相机
        val REQUEST_CODE_CAMERA = 500

        //登录
        val REQUEST_CODE_PHONE_LOGIN = 400
    }

    //  人脸识别模式
    enum class FacePassSDKMode {
        MODE_ONLINE, MODE_OFFLINE
    }

    var viewModel: MainActivityViewModel? = null

    var mmkv: MMKV? = null

    var binsWorkTimeBean: BinsWorkTimeBean? = null

    /**SDK实例*/
    var mFacePassHandler: FacePassHandler? = null

    /* 相机实例 */
    private var manager: CameraManager? = null

    /* 相机是否使用前置摄像头 */
    private var cameraFacingFront = true

    /* 相机图片旋转角度，请根据实际情况来设置
     * 对于标准设备，可以如下计算旋转角度rotation
     * int windowRotation = ((WindowManager)(getApplicationContext().getSystemService(Context.WINDOW_SERVICE))).getDefaultDisplay().getRotation() * 90;
     * Camera.CameraInfo info = new Camera.CameraInfo();
     * Camera.getCameraInfo(cameraFacingFront ? Camera.CameraInfo.CAMERA_FACING_FRONT : Camera.CameraInfo.CAMERA_FACING_BACK, info);
     * int cameraOrientation = info.orientation;
     * int rotation;
     * if (cameraFacingFront) {
     *     rotation = (720 - cameraOrientation - windowRotation) % 360;
     * } else {
     *     rotation = (windowRotation - cameraOrientation + 360) % 360;
     * }
     */
    private var cameraRotation = 0

    private val cameraWidth = 640
    private val cameraHeight = 480

    var screenState = 0 // 0 横 1 竖
    private var heightPixels = 0
    private var widthPixels = 0

    private var isLocalGroupExist = false

    /*图片缓存*/
    private var mImageCache: FaceImageCache? = null

    /*DetectResult queue*/
    var mDetectResultQueue: ArrayBlockingQueue<ByteArray>? = null
    var mFeedFrameQueue: ArrayBlockingQueue<CameraPreviewData>? = null

    /*Toast*/
    private var mRecoToast: Toast? = null

    /*recognize thread*/
    var mRecognizeThread: RecognizeThread? = null
    var mFeedFrameThread: FeedFrameThread? = null

    /*底库同步*/
    private val mSyncGroupBtn: ImageView? = null
    private val mSyncGroupDialog: AlertDialog? = null

    private val mFaceOperationBtn: ImageView? = null

    private var canRecognize = true

    var deviceCode: String? = null

    /*图片缓存*/

    private var mAndroidHandler: Handler? = null

    private val gson = Gson()

    private var userMessageDao: UserMessageDao? = null

    private var app: DustbinBrainApp? = null

    private var mLastClickTime = 0.toLong()
    private var mSecretNumber = 0

    override fun layoutId(): Int {
        return R.layout.activity_main
    }

    override fun initViewData() {
        viewModel = ViewModelProvider(this)[MainActivityViewModel::class.java]
        mmkv = MMKV.defaultMMKV()
        app = application as DustbinBrainApp?
        mImageCache = FaceImageCache()
        mDetectResultQueue = ArrayBlockingQueue(5)
        mFeedFrameQueue = ArrayBlockingQueue(1)
        deviceCode = mmkv?.decodeString(MMKVCommon.DEVICE_ID)
        AndroidDeviceSDK.autoStratAPP(this)
        AndroidDeviceSDK.hideStatus(false)
        AndroidDeviceSDK.keepActivity(this)
        AndroidDeviceSDK.setSchedulePowerOn()
        AndroidDeviceSDK.setSchedulePowerOff()

        initAndroidHandler()
        /* 初始化界面 */
        initView()
        bindData()
        //  初始化 greenDao 数据库，以及数据库操作对象
        userMessageDao = DataBaseUtil.getInstance(this@MainActivity).daoSession.userMessageDao
        initTCP()
        //获取投放时间
        var map: MutableMap<String, String> = mutableMapOf(
            "device_id" to deviceCode.toString()
        )
        viewModel!!.getBinsWorkTime(map)
        //获取投放二维码
        viewModel!!.getDeviceQrcode(map)


        //  设置垃圾箱配置
        val dustbinConfig =
            DataBaseUtil.getInstance(this).daoSession.dustbinConfigDao.queryBuilder().unique()
        DustbinBrainApp.dustbinConfig = dustbinConfig
        DustbinBrainApp.dustbinBeanList?.addAll(DataBaseUtil.getInstance(this@MainActivity).getDustbinByType(null))

        //  启动APP默认关闭所有门
        closeAllDoor()

        //  必须在第一次语音播报前 先初始化对象，否则可能出现第一次语音播报无声音的情况
        VoiceUtil.getInstance()
        //初始化人脸SDK
        initFaceSDK()

        //初始化人脸识别配置
        initFaceHandler()
        mRecognizeThread = RecognizeThread()
        mRecognizeThread!!.start()
        mFeedFrameThread = FeedFrameThread()
        mFeedFrameThread!!.start()
        //  开启读取数据服务，定时器
        startService(Intent(this, ResidentService::class.java))
        val imUserId = mmkv?.decodeString(MMKVCommon.IM_USERID)
        LogUtils.iTag("ProfileManager","登录ID：$imUserId")
        ProfileManager.getInstance().login(
            imUserId,
            "",
            object : ProfileManager.ActionCallback {
                override fun onSuccess() {
                    LogUtils.iTag("ProfileManager","通话登录成功")
                    video_call_siv.visibility = View.VISIBLE
                    CallService.start(this@MainActivity)
                }

                override fun onFailed(code: Int, msg: String?) {
                    LogUtils.iTag("ProfileManager","${code}通话登录成功$msg")
                    video_call_siv.visibility = View.GONE
                }
            })
    }

    private fun initView() {
        val windowRotation =
            (applicationContext.getSystemService(WINDOW_SERVICE) as WindowManager).defaultDisplay.rotation * 90
        cameraRotation = if (windowRotation == 0) {
            FacePassImageRotation.DEG90
        } else if (windowRotation == 90) {
            FacePassImageRotation.DEG0
        } else if (windowRotation == 270) {
            FacePassImageRotation.DEG180
        } else {
            FacePassImageRotation.DEG270
        }
        LogUtils.dTag(FenFenCommonUtil.FACE_TAG, "Rotation: cameraRation: $cameraRotation")
        cameraFacingFront = true
        if (SettingVar.isSettingAvailable) {
            cameraRotation = SettingVar.faceRotation
            cameraFacingFront = SettingVar.cameraFacingFront
        }
        LogUtils.dTag(FenFenCommonUtil.FACE_TAG, "Rotation: screenRotation: $windowRotation")
        LogUtils.dTag(
            FenFenCommonUtil.FACE_TAG,
            "Rotation: faceRotation: ${SettingVar.faceRotation}"
        )
        LogUtils.dTag(FenFenCommonUtil.FACE_TAG, "Rotation: new cameraRation: $cameraRotation")
        //  获取屏幕朝向
        val mCurrentOrientation = resources.configuration.orientation
        if (mCurrentOrientation == Configuration.ORIENTATION_PORTRAIT) {
            screenState = 1
        } else if (mCurrentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
            screenState = 0
        }

        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        heightPixels = displayMetrics.heightPixels
        widthPixels = displayMetrics.widthPixels
        SettingVar.mHeight = heightPixels
        SettingVar.mWidth = widthPixels
        SettingVar.cameraSettingOk = false
        manager = CameraManager()
        //设置圆形
        preview.outlineProvider = CicleViewOutlineProvider(FaceView.circleDimater)
        preview.clipToOutline = true
        //加载预览界面
        manager!!.setPreviewDisplay(preview)
        /* 注册相机回调函数 */
        manager!!.setListener(this)
        show_login_qr.setImageBitmap(QRCodeUtil.getAppletLoginCode("https://ffadmin.fenfeneco.com/index.php/index/index/weixinRegister?device_id=" + deviceCode + "&device_type=2"));
        video_call_siv.setOnClickListener {
            val callUserId = "123456789"
            val selfInfo = TRTCVideoCallActivity.UserInfo()
            selfInfo.userId = mmkv?.decodeString(MMKVCommon.IM_USERID)
            val callUserInfoList: MutableList<TRTCVideoCallActivity.UserInfo> =
                java.util.ArrayList()
            val callUserInfo = TRTCVideoCallActivity.UserInfo()
            callUserInfo.userId = callUserId
            callUserInfoList.add(callUserInfo)

            TRTCVideoCallActivity.startCallSomeone(this, selfInfo, callUserInfoList)
        }
        phone_login_siv.setOnClickListener {
            //  跳转到手机登录
            val intent = Intent(this@MainActivity, PhoneLoginActivity::class.java)
            startActivityForResult(intent, REQUEST_CODE_PHONE_LOGIN)
        }
        show_login_qr.setOnClickListener {
            val curTime = System.currentTimeMillis()
            val durTime = curTime - mLastClickTime
            mLastClickTime = curTime
            if (durTime < 600) {
                ++mSecretNumber
                if (mSecretNumber == 10) {
                    mSecretNumber = 0
//                    startKtActivity<DeviceMannageActivity>()
                    val intent = Intent(this@MainActivity, DeviceMannageActivity::class.java)
                    intent.putExtra("isDebug", true)
                    startActivity(intent)
                }
            }
        }

    }

    override fun onResume() {
        super.onResume()
        manager!!.open(windowManager, false, cameraWidth, cameraHeight)
        canRecognize = true
        LogUtils.iTag(TAG, "回到首页，开启人脸识别")
    }

    override fun onPause() {
        super.onPause()
        LogUtils.iTag(TAG, "页面跳转，暂停人脸识别")
        manager!!.release()
        canRecognize = false
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            CONTROL_RESULT_CODE -> {

                if (data != null) {
                    val exitCode = data.getIntExtra("exitCode", 0)
                    val facePath = data.getStringExtra("faceUri")
                    if (TextUtils.isEmpty(facePath)) {
                        DustbinBrainApp.userId = 0;
                        DustbinBrainApp.userType = 0;
                        //  结算超时
                        if (exitCode != 0) {
                            if (exitCode == 1) {
//                                closeAllDoor()
                            }
                        }
                    } else {
                        val TAG = "特征"
                        file = File(facePath)
                        //  获取位图对象
                        var bitmap = BitmapFactory.decodeFile(file.toString())
//                        resiget_face.setImageBitmap(bitmap)
                        //  如果图片是颠倒的，则旋转过来
                        if (bitmap.width > bitmap.height) {
                            bitmap = adjustPhotoRotation(bitmap, 90)
                        }

                        try {
                            //  提取特征值
                            val facePassExtractFeatureResult =
                                mFacePassHandler!!.extractFeature(bitmap)

                            //  如果特征值合格
                            if (facePassExtractFeatureResult.result == 0) {
                                val facePassFeatureAppendInfo = FacePassFeatureAppendInfo()

                                //  创建 faceToken
                                val faceToken = mFacePassHandler!!.insertFeature(
                                    facePassExtractFeatureResult.featureData,
                                    facePassFeatureAppendInfo
                                )
                                nowFaceToken = faceToken
                                val bindResult =
                                    mFacePassHandler!!.bindGroup(
                                        group_name,
                                        faceToken.toByteArray()
                                    )

                                //  绑定结果
                                if (bindResult) {
                                    Log.i(
                                        TAG,
                                        "绑定成功faceToken:$faceToken" + "  , userId:${DustbinBrainApp.userId} , userType:${DustbinBrainApp.userType}"
                                    )

                                    //  将该facetoken 和用户id进行绑定
                                    val userMessage: UserMessage? =
                                        DataBaseUtil.getInstance(this@MainActivity)
                                            .daoSession?.userMessageDao?.queryBuilder()
                                            ?.where(
                                                UserMessageDao.Properties.UserId.eq(
                                                    DustbinBrainApp.userId!!
                                                )
                                            )
                                            ?.unique()
                                    //  本地已经有这个人脸特征了，则删除掉原有的人脸特征，添加新的人脸特征
                                    if (userMessage != null) {
                                        //  人脸库中删除这个人脸特征
                                        mFacePassHandler!!.deleteFace(
                                            userMessage.faceToken.toByteArray()
                                        )
                                        //  本地数据库中删除这个用户
                                        DataBaseUtil.getInstance(this@MainActivity)
                                            .daoSession?.userMessageDao?.delete(userMessage)
                                        Log.i(
                                            "addFaceImage",
                                            "删除旧的人脸特征与注册信息" + userMessage.getUserId()
                                        )
                                    }
                                    //  本地实现
                                    DataBaseUtil.getInstance(this@MainActivity)
                                        .insertUserIdAndFaceTokenThread(
                                            DustbinBrainApp.userId!!.toLong(),
                                            DustbinBrainApp.userType,
                                            nowFaceToken
                                        )
                                    //  上传人脸图片,特征值，以及用户id
                                    uploadFace(
                                        file!!,
                                        facePassExtractFeatureResult.featureData,
                                        DustbinBrainApp.userId!!.toLong()
                                    )
                                } else {
                                    mFacePassHandler!!.deleteFace(faceToken.toByteArray())
                                    Log.i(TAG, "绑定失败")
                                }
                            } else {
                                toast("人脸质量不合格")
                            }
                        } catch (e: java.lang.Exception) {
                            //  本地实现
                            DataBaseUtil.getInstance(this@MainActivity)
                                .insertUserIdAndFaceTokenThread(
                                    DustbinBrainApp.userId!!.toLong(),
                                    DustbinBrainApp.userType!!,
                                    nowFaceToken
                                )
                            Log.i(TAG, e.message!!)
                        } finally {
                            //  删除图片
                            //  file.delete();
                        }
                    }
                }
                Thread {
                    for (dustbinStateBean in DustbinBrainApp.dustbinBeanList!!) {
                        //  关补光灯
                        SerialProManager.getInstance().closeLight(dustbinStateBean.doorNumber)
                        try {
                            Thread.sleep(50)
                        } catch (e: java.lang.Exception) {
                            e.printStackTrace()
                        }
                    }
                }.start()
            }
            REQUEST_CODE_PHONE_LOGIN -> {
                manager?.release()
                canRecognize = true
                if (data != null) {
                    if (data.getBooleanExtra("isSuccess", false)) {
                        goControlActivity()
                    }
                }
            }
            else -> {

            }
        }
    }

    fun adjustPhotoRotation(bm: Bitmap, orientationDegree: Int): Bitmap? {
        val m = Matrix()
        m.setRotate(orientationDegree.toFloat(), bm.width.toFloat() / 2, bm.height.toFloat() / 2)
        return Bitmap.createBitmap(bm, 0, 0, bm.width, bm.height, m, true)
    }

    fun uploadFace(file: File, feature: ByteArray?, userId: Long) {
        val client = OkHttpClient.Builder().build()

        // 设置文件以及文件上传类型封装
        val requestBody = RequestBody.create("image/jpg".toMediaTypeOrNull(), file)
        val nowTime = System.currentTimeMillis() / 1000
        val androidID = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)


        // 文件上传的请求体封装
        val multipartBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM) //.addFormDataPart("user_id",String.valueOf(userId))
            //.addFormDataPart("featrue", Base64.encodeToString(feature,Base64.DEFAULT))
            .addFormDataPart(
                "file",
                file.name,
                requestBody
            ) //.addFormDataPart("sign",md5(androidID + nowTime + key).toUpperCase())
            //.addFormDataPart("device_id",androidID)
            //.addFormDataPart("timestamp",String.valueOf(nowTime))
            .build()
        val request: Request = Request.Builder()
            .url(ServerAddress.FILE_UPLOAD)
            .post(multipartBody)
            .build()
        val call = client.newCall(request)
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.i(TAG, "图片上传失败：" + e.message)
                Handler().post(Runnable { //  如果存在二维码弹窗，则关闭
                    val alertB = AlertDialog.Builder(this@MainActivity)
                    alertB.setCancelable(false)
                    alertB.setTitle("提示")
                    alertB.setMessage("人脸注册失败")
                    alertB.setPositiveButton(
                        "重试"
                    ) { dialogInterface, i -> uploadFace(file, feature, userId) }
                    alertB.setNegativeButton(
                        "取消"
                    ) { dialog, which -> dialog.dismiss() }
                    alertB.create()
                    alertB.show()
                })
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                val imageUploadResult: ImageUploadResult = Gson().fromJson(
                    response.body!!.string(),
                    ImageUploadResult::class.java
                )
                Log.i(TAG, "图片上传结果：" + imageUploadResult.toString())


                //  图片上传成功
                if (imageUploadResult.code == 1) {
                    val map: MutableMap<String, String> = HashMap()
                    map["user_id"] = userId.toString()
                    map["featrue"] = Base64.encodeToString(feature, Base64.DEFAULT)
                    map["face_image"] = imageUploadResult.getData()
                    map["sign"] =
                        FenFenCommonUtil.md5(androidID + nowTime + FenFenCommonUtil.key)!!
                            .uppercase(
                                Locale.ROOT
                            )
                    //map.put("device_id",androidID);
                    map["timestamp"] = nowTime.toString()
                    NetWorkUtil.getInstance().doPost(
                        ServerAddress.FACE_AND_USER_ID_UPLOAD,
                        map,
                        object : NetWorkUtil.NetWorkListener {
                            override fun success(response: String) {
                                Log.i(TAG, "人脸注册 json 结果：$response")
                                val resultMould: ResultMould = Gson().fromJson(
                                    response,
                                    ResultMould::class.java
                                )
                                //  人脸注册成功
                                if (resultMould.getCode() == 1) {
                                    Log.i(TAG, "开始本地 userToken 和 userID 绑定")

                                    //  本地实现
                                    DataBaseUtil.getInstance(this@MainActivity)
                                        .insertUserIdAndFaceToken(
                                            DustbinBrainApp.userId!!.toLong(),
                                            nowFaceToken
                                        )
                                    Log.i(
                                        TAG,
                                        DustbinBrainApp.userId.toString() + "绑定 " + nowFaceToken
                                    )
                                    file.delete()
                                    goControlActivity()
                                }
                            }

                            override fun fail(call: Call?, e: IOException) {
                                Log.i(TAG, "人脸注册失败 fail：" + e.message)
                            }

                            override fun error(e: java.lang.Exception) {
                                Log.i(TAG, "人脸注册失败error ：" + e.message)
                            }
                        })
                }
            }
        })
    }

    //  关闭所有门
    private fun closeAllDoor() {
        //  启动APP就关闭所有门
        object : Thread() {
            override fun run() {
                super.run()
                for (dustbinStateBean in DustbinBrainApp.dustbinBeanList!!) {
                    SerialPortUtil.getInstance().sendData(
                        SerialProManager.getInstance()
                            .closeDoor(dustbinStateBean.doorNumber)
                    )
                    try {
                        sleep(250)
                    } catch (e: java.lang.Exception) {
                        e.printStackTrace()
                    }

                    //  开排气扇
                    SerialPortUtil.getInstance().sendData(
                        SerialProManager.getInstance()
                            .openExhaustFan(dustbinStateBean.doorNumber)
                    )
                    try {
                        sleep(250)
                    } catch (e: java.lang.Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }.start()
    }

    /**
     * 初始化人脸SDK
     */
    private fun initFaceSDK() {
        //  获取认证方式
        FacePassHandler.getAuth(
            FenFenCommonUtil.authIP,
            FenFenCommonUtil.apiKey,
            FenFenCommonUtil.apiSecret
        )
        FacePassHandler.initSDK(applicationContext)
        LogUtils.dTag(FenFenCommonUtil.FACE_TAG, FacePassHandler.getVersion())
    }

    /**
     * 初始化人脸识别配置
     */
    private fun initFaceHandler() {
        ThreadManager.getInstance().execute {
            while (!isFinishing) {
                while (FacePassHandler.isAvailable()) {
                    LogUtils.dTag(FenFenCommonUtil.FACE_TAG, "start to build FacePassHandler")
                    val config: FacePassConfig
                    try {
                        /* 填入所需要的配置 */
                        config = FacePassConfig()
                        config.poseBlurModel = FacePassModel.initModel(
                            applicationContext.assets,
                            "attr.pose_blur.align.av200.190630.bin"
                        )
                        //单目使用CPU rgb活体模型
                        config.livenessModel = FacePassModel.initModel(
                            applicationContext.assets,
                            "liveness.CPU.rgb.int8.D.bin"
                        )
                        //双目使用CPU rgbir活体模型
                        config.rgbIrLivenessModel = FacePassModel.initModel(
                            applicationContext.assets,
                            "liveness.CPU.rgbir.int8.D.bin"
                        )
                        //当单目或者双目有一个使用GPU活体模型时，请设置livenessGPUCache
                        config.livenessGPUCache = FacePassModel.initModel(
                            applicationContext.assets,
                            "liveness.GPU.AlgoPolicy.D.cache"
                        )
                        config.searchModel = FacePassModel.initModel(
                            applicationContext.assets,
                            "feat2.arm.G.v1.0_1core.bin"
                        )
                        config.detectModel =
                            FacePassModel.initModel(applicationContext.assets, "detector.arm.D.bin")
                        config.detectRectModel = FacePassModel.initModel(
                            applicationContext.assets,
                            "detector_rect.arm.D.bin"
                        )
                        config.landmarkModel =
                            FacePassModel.initModel(applicationContext.assets, "pf.lmk.arm.D.bin")
                        config.smileModel = FacePassModel.initModel(
                            applicationContext.assets,
                            "attr.smile.mgf29.0.1.1.181229.bin"
                        )
                        config.ageGenderModel = FacePassModel.initModel(
                            applicationContext.assets,
                            "attr.age_gender.surveillance.nnie.av200.0.1.0.190630.bin"
                        )
                        config.occlusionFilterModel = FacePassModel.initModel(
                            applicationContext.assets,
                            "occlusion.all_attr_configurable.occ.190816.bin"
                        )
                        //如果不需要表情和年龄性别功能，smileModel和ageGenderModel可以为null
                        config.smileModel = null
                        config.ageGenderModel = null
                        config.searchThreshold = 71f //未带口罩时，识别使用的阈值
                        config.livenessThreshold = 60f
                        config.livenessEnabled = true
                        config.rgbIrLivenessEnabled = false
                        config.faceMinThreshold = 100
                        config.poseThreshold = FacePassPose(30f, 30f, 30f)
                        config.blurThreshold = 0.8f
                        config.lowBrightnessThreshold = 70f
                        config.highBrightnessThreshold = 210f
                        config.brightnessSTDThreshold = 80f
                        config.retryCount = 10
                        config.smileEnabled = false
                        config.maxFaceEnabled = true
                        config.rotation = cameraRotation
                        config.fileRootPath =
                            getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)!!.absolutePath
                        /* 创建SDK实例 */
                        mFacePassHandler = FacePassHandler(config)
                        val addFaceConfig = mFacePassHandler!!.addFaceConfig
                        addFaceConfig.blurThreshold = 0.8f
                        addFaceConfig.faceMinThreshold = 100
                        mFacePassHandler!!.addFaceConfig = addFaceConfig
                        checkGroup()
                    } catch (e: FacePassException) {
                        e.printStackTrace()
                        LogUtils.dTag(FenFenCommonUtil.FACE_TAG, "FacePassHandler is null")
                        return@execute
                    }
                    return@execute
                }
            }
        }
    }

    /**
     * 检测本地脸库，无脸库则创建本地脸库
     */
    fun checkGroup() {
        //绑定测试人脸
//        addBindTest()
        if (mFacePassHandler == null) {
            return
        }
        //  获取所有底库
        var localGroups: Array<String?>? = arrayOfNulls(0)
        try {
            localGroups = mFacePassHandler!!.localGroups
        } catch (e: FacePassException) {
            e.printStackTrace()
        }
        isLocalGroupExist = false

        //  如果为 null 直接提示
        if (localGroups == null || localGroups.size == 0) {

            //  如果底库不存在，默认创建底库
            try {
                mFacePassHandler!!.createLocalGroup(group_name)
                isLocalGroupExist = true
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return
        }

        //  如果没有找到名称符合规则的
        for (group in localGroups) {
            if (group_name == group) {
                isLocalGroupExist = true
            }
        }
        if (!isLocalGroupExist) {

            //  如果底库不存在，默认创建底库
            try {
                mFacePassHandler!!.createLocalGroup(group_name)
                isLocalGroupExist = true
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * 服务器返回数据
     */
    fun bindData() {
        //拿到投放时间
        viewModel?.liveDataForDustbinConfig?.observe(this) { data ->
            if (data.success) {
                if (data.binsWorkTimeBean != null) {
                    binsWorkTimeBean = data.binsWorkTimeBean
                }
            }
        }


    }

    override fun onPictureTaken(cameraPreviewData: CameraPreviewData?) {
        mFeedFrameQueue!!.offer(cameraPreviewData)
//        Log.i(FenFenCommonUtil.FACE_TAG, "feedframe")
    }

    private fun initAndroidHandler() {
        mAndroidHandler = Handler()
    }

    /**
     * 根据facetoken下载图片缓存
     */
    private class FaceImageCache : ImageLoader.ImageCache {
        var mCache: LruCache<String, Bitmap>? = null

        companion object {
            val CACHE_SIZE: Int = 6 * 1024 * 1024
        }

        constructor() {
            mCache = LruCache<String, Bitmap>(CACHE_SIZE)
        }

        override fun getBitmap(url: String?): Bitmap {
            return mCache!!.get(url)
        }

        override fun putBitmap(url: String?, bitmap: Bitmap?) {
            mCache!!.put(url, bitmap)
        }

    }

    inner class FeedFrameThread : Thread() {
        var isInterrupt = false
        override fun run() {

            //  是否中断
            while (!isInterrupt) {
                val cameraPreviewData: CameraPreviewData? = try {
                    mFeedFrameQueue!!.take()
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                    continue
                }
                if (mFacePassHandler == null) {
                    continue
                }
                /* 将相机预览帧转成SDK算法所需帧的格式 FacePassImage */
                val startTime = System.currentTimeMillis() //起始时间
                var image: FacePassImage = try {
                    FacePassImage(
                        cameraPreviewData!!.nv21Data,
                        cameraPreviewData!!.width,
                        cameraPreviewData!!.height,
                        cameraRotation,
                        FacePassImageType.NV21
                    )
                } catch (e: FacePassException) {
                    e.printStackTrace()
                    Log.e(MainActivity.MY_TAG, "人脸帧invalid失败")
                    continue
                }

                /* 将每一帧FacePassImage 送入SDK算法， 并得到返回结果 */
                var detectionResult: FacePassDetectionResult? = null
                try {
                    detectionResult = mFacePassHandler!!.feedFrame(image)
                } catch (e: FacePassException) {
                    Log.e(MainActivity.MY_TAG, "人脸脸框异常")
                    e.printStackTrace()
                }
                if (detectionResult == null || detectionResult.faceList.size == 0) {
                    /* 当前帧没有检出人脸 */
                    runOnUiThread {
                        fcview.clear()
                        fcview.invalidate()
                    }
                } else {
                    /* 将识别到的人脸在预览界面中圈出，并在上方显示人脸位置及角度信息 */
                    val bufferFaceList = detectionResult.faceList
//                    if(!fcview.isInCircle(faceList)){
//                        break
//                    }
                    runOnUiThread { showFacePassFace(bufferFaceList) }
                }

                //  如果是联机的状态，就传递 detectionResult 对象给服务器进行处理，返回的 FacePassRecognitionResult 对象带有 faceToken ，faceToken（人脸唯一id）
                if (SDK_MODE == FacePassSDKMode.MODE_ONLINE) {

                } else {
                    /*离线模式，将识别到人脸的，message不为空的result添加到处理队列中*/
                    if (detectionResult != null && detectionResult.message.size != 0) {
                        Log.d(DEBUG_TAG, "mDetectResultQueue.offer")
                        //  FacePassRecognitionResult
                        Log.i(MY_TAG, "触发离线识别")
                        //  为空
                        //  Log.i(MY_TAG,detectionResult.feedback[0].trackId+"");
                        val facePassDetectionResult = detectionResult.feedback
                        if (facePassDetectionResult != null && facePassDetectionResult.size != 0) {
                            for (f in facePassDetectionResult) {
                            }
                        } else {
                            Log.i(
                                MY_TAG,
                                "facePassDetectionResult == null && facePassDetectionResult.length == 0"
                            )
                        }


                        /*String faceToken = new String(detectionResult.message);
                        Log.i(MY_TAG, "无线状态下 detectionResult.message " + faceToken);*/
                        mDetectResultQueue!!.offer(
                            detectionResult.message
                        )
                    }
                }
                val endTime = System.currentTimeMillis() //结束时间
                val runTime = endTime - startTime

            }
        }

        override fun interrupt() {
            isInterrupt = true
            super.interrupt()
        }
    }


    private fun showFacePassFace(detectResult: Array<FacePassFace>) {
        fcview!!.clear()
        for (face in detectResult) {
            Log.d(
                "facefacelist",
                "width " + (face.rect.right - face.rect.left) + " height " + (face.rect.bottom - face.rect.top)
            )
            Log.d("facefacelist", "smile " + face.smile)
            val mirror: Boolean = cameraFacingFront /* 前摄像头时mirror为true */
            val faceIdString = StringBuilder()
            faceIdString.append("ID = ").append(face.trackId)
            val faceViewString = SpannableString(faceIdString)
            faceViewString.setSpan(
                TypefaceSpan("fonts/kai"),
                0,
                faceViewString.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            val faceRollString = StringBuilder()
            faceRollString.append("旋转: ").append(face.pose.roll.toInt()).append("°")
            val facePitchString = StringBuilder()
            facePitchString.append("上下: ").append(face.pose.pitch.toInt()).append("°")
            val faceYawString = StringBuilder()
            faceYawString.append("左右: ").append(face.pose.yaw.toInt()).append("°")
            val faceBlurString = StringBuilder()
            faceBlurString.append("模糊: ").append(face.blur)
            val smileString = StringBuilder()
            smileString.append("微笑: ").append(String.format("%.6f", face.smile))
            val mat = Matrix()
            val w: Int = preview.getMeasuredWidth()
            val h: Int = preview.getMeasuredHeight()
            val cameraHeight = manager!!.getCameraheight()
            val cameraWidth = manager!!.getCameraWidth()
            var left = 0f
            var top = 0f
            var right = 0f
            var bottom = 0f
            when (cameraRotation) {
                0 -> {
                    left = face.rect.left.toFloat()
                    top = face.rect.top.toFloat()
                    right = face.rect.right.toFloat()
                    bottom = face.rect.bottom.toFloat()
                    mat.setScale((if (mirror) -1 else 1.toFloat()) as Float, 1f)
                    mat.postTranslate(if (mirror) cameraWidth.toFloat() else 0f, 0f)
                    mat.postScale(
                        w.toFloat() / cameraWidth.toFloat(),
                        h.toFloat() / cameraHeight.toFloat()
                    )
                }
                90 -> {
                    mat.setScale((if (mirror) -1 else 1.toFloat()) as Float, 1f)
                    mat.postTranslate(if (mirror) cameraHeight.toFloat() else 0f, 0f)
                    mat.postScale(
                        w.toFloat() / cameraHeight.toFloat(),
                        h.toFloat() / cameraWidth.toFloat()
                    )
                    left = face.rect.top.toFloat()
                    top = (cameraWidth - face.rect.right).toFloat()
                    right = face.rect.bottom.toFloat()
                    bottom = (cameraWidth - face.rect.left).toFloat()
                }
                180 -> {
                    mat.setScale(1f, (if (mirror) -1 else 1.toFloat()) as Float)
                    mat.postTranslate(0f, if (mirror) cameraHeight.toFloat() else 0f)
                    mat.postScale(
                        w.toFloat() / cameraWidth.toFloat(),
                        h.toFloat() / cameraHeight.toFloat()
                    )
                    left = face.rect.right.toFloat()
                    top = face.rect.bottom.toFloat()
                    right = face.rect.left.toFloat()
                    bottom = face.rect.top.toFloat()
                }
                270 -> {
                    mat.setScale((if (mirror) -1 else 1.toFloat()) as Float, 1f)
                    mat.postTranslate(if (mirror) cameraHeight.toFloat() else 0f, 0f)
                    mat.postScale(
                        w.toFloat() / cameraHeight.toFloat(),
                        h.toFloat() / cameraWidth.toFloat()
                    )
                    left = (cameraHeight - face.rect.bottom).toFloat()
                    top = face.rect.left.toFloat()
                    right = (cameraHeight - face.rect.top).toFloat()
                    bottom = face.rect.right.toFloat()
                }
            }
            val drect = RectF()
            val srect = RectF(left, top, right, bottom)
            mat.mapRect(drect, srect)
            fcview.addRect(drect)
            fcview.addId(faceIdString.toString())
            fcview.addRoll(faceRollString.toString())
            fcview.addPitch(facePitchString.toString())
            fcview.addYaw(faceYawString.toString())
            fcview.addBlur(faceBlurString.toString())
            fcview.addSmile(smileString.toString())
        }
        fcview.invalidate()
    }


    inner class RecognizeThread : Thread() {
        var isInterrupt = false
        override fun run() {
            while (!isInterrupt) {
                try {
                    val detectionResult = mDetectResultQueue!!.take()

                    //  年龄性别结果。
                    val ageGenderResult: Array<FacePassAgeGenderResult>? = null
                    Log.i(MY_TAG, "触发无线人脸识别处理线程")
                    Log.d(DEBUG_TAG, "mDetectResultQueue.isLocalGroupExist")

                    //  是否存在底库
                    if (isLocalGroupExist) {
                        Log.d(DEBUG_TAG, "mDetectResultQueue.recognize")
                        Log.i(MY_TAG, "isLocalGroupExist 为 true ， 底库存在。")

                        //  获取 返回识别结果（FacePassRecognitionResult）的数组，每一项对应一张图的识别结果。
                        val recognizeResult =
                            mFacePassHandler!!.recognize(group_name, detectionResult)


                        //  判定返回识别结果是不是为空
                        if (recognizeResult != null && recognizeResult.size > 0) {
                            Log.i(MY_TAG, "人脸识别正常")
                            for (result in recognizeResult) {
                                val faceToken = String(result.faceToken)
                                nowFaceToken = faceToken
                                Log.i("addFaceImage", "faceToken 离线状态下的人脸识别$faceToken")


                                //  查询faceToken 是否对应某一个从服务器传过来 userId，如果存在则直接进入垃圾箱控制台

//                                if (FacePassRecognitionResultType.RECOG_OK == result.facePassRecognitionResultType) {
                                getFaceImageByFaceToken(result.trackId, faceToken)
                                //                                }
                                val idx = findidx(ageGenderResult, result.trackId)
                                //  -1就是没有找到的意思， 也就是 ageGenderResult (年龄性别结果) 为 null
                                if (idx == -1) {

                                    //  没有性别的 Toast
                                    showRecognizeResult(
                                        result.trackId,
                                        result.detail.searchScore,
                                        result.detail.livenessScore,
                                        !TextUtils.isEmpty(faceToken),
                                        0f,
                                        0
                                    )
                                } else {
                                    //  有性别的 Toast
                                    showRecognizeResult(
                                        result.trackId,
                                        result.detail.searchScore,
                                        result.detail.livenessScore,
                                        !TextUtils.isEmpty(faceToken),
                                        0f,
                                        0
                                    )
                                }
                            }
                        } else {
                            //  底库中没有人脸,显示人脸验证失败，注册人脸
                            Handler(Looper.getMainLooper()).post {
                                //  showVerifyFail();
                            }
                            Log.i(
                                MY_TAG,
                                "人脸识别为空 recognizeResult != null && recognizeResult.length > 0) 为 false"
                            )
                        }
                    } else {
                        Log.i(MY_TAG, "isLocalGroupExist 为 false ，底库不存在。")
                    }
                } catch (e: InterruptedException) {
                    Log.e(MY_TAG, "人脸getFaceImageByFaceToken异常，RecognizeThread.run")
                    e.printStackTrace()
                } catch (e: FacePassException) {
                    Log.e(MY_TAG, "人脸getFaceImageByFaceToken异常，RecognizeThread.run")
                    e.printStackTrace()
                }
            }
        }

        override fun interrupt() {
            isInterrupt = true
            super.interrupt()
        }
    }

    private fun getFaceImageByFaceToken(trackId: Long, faceToken: String) {
        if (TextUtils.isEmpty(faceToken)) {
            return
        }
        try {
            val bitmap = mFacePassHandler!!.getFaceImage(faceToken.toByteArray())
            mAndroidHandler!!.post {
                Log.i(DEBUG_TAG, "getFaceImageByFaceToken cache is null")
                showToast("ID = $trackId", Toast.LENGTH_SHORT, true, bitmap)
            }
            if (bitmap != null) {
                return
            }
        } catch (e: FacePassException) {
            e.printStackTrace()
        }
    }

    fun showToast(text: CharSequence?, duration: Int, isSuccess: Boolean, bitmap: Bitmap?) {
        val inflater = layoutInflater
        val toastView: View = inflater.inflate(R.layout.toast, null)
        val toastLLayout = toastView.findViewById(R.id.toastll) as LinearLayout
            ?: return
        toastLLayout.background.alpha = 100
        val imageView = toastView.findViewById(R.id.toastImageView) as ImageView
        val idTextView = toastView.findViewById(R.id.toastTextView) as TextView
        val stateView = toastView.findViewById(R.id.toastState) as TextView
        val s: SpannableString
        if (isSuccess) {
            s = SpannableString("验证成功")
            imageView.setImageResource(R.drawable.success)
        } else {
            s = SpannableString("验证失败")
            imageView.setImageResource(R.drawable.success)
        }
        if (bitmap != null) {
            imageView.setImageBitmap(bitmap)
        }
        stateView.text = s
//        idTextView.text = text
        if (mRecoToast == null) {
            mRecoToast = Toast(applicationContext)
            mRecoToast!!.setGravity(Gravity.CENTER_VERTICAL, 0, 0)
        }
        mRecoToast!!.setDuration(duration)
        mRecoToast!!.setView(toastView)
        mRecoToast!!.show()
    }

    fun findidx(results: Array<FacePassAgeGenderResult>?, trackId: Long): Int {
        val result = -1
        if (results == null) {
            return result
        }
        for (i in results.indices) {
            if (results[i].trackId == trackId) {
                return i
            }
        }
        return result
    }


    private fun showRecognizeResult(
        trackId: Long,
        searchScore: Float,
        livenessScore: Float,
        isRecognizeOK: Boolean,
        age: Float,
        gender: Int
    ) {
        mAndroidHandler!!.post {
            if (searchScore < 64) {
                //showToast("ID = " + String.valueOf(trackId), Toast.LENGTH_SHORT, false, null);    原先处理方式
                Log.i(
                    MY_TAG,
                    "searchScore : " + searchScore + ",livenessScore : " + livenessScore + "验证不通过"
                )
                //  显示验证识别并录脸
//                showQRCodeDialog()
                //  showVerifyFail();
            } else {
                showToast("ID = $nowFaceToken", Toast.LENGTH_SHORT, true, null)
                Log.i(
                    MY_TAG,
                    "searchScore : " + searchScore + ",livenessScore : " + livenessScore + "验证通过"
                )
                queryFaceToken(nowFaceToken)
            }

//            faceEndTextView.append(
//                """
//                        ID = $trackId${if (isRecognizeOK) "识别成功" else "识别失败"}
//
//                        """.trimIndent()
//            )
//            faceEndTextView.append("识别分 = $searchScore\n")
//            faceEndTextView.append("活体分 = $livenessScore\n")
        }
    }

    //  上一次人脸识别成功时间
    private var lastPassTime: Long = 0
    private var faceImagePath: String = ""
    fun queryFaceToken(faceToken: String) {
        Log.i(MY_TAG, "faceToken:$faceToken")
        //  避免重复进入 控制台界面，2s
        if (System.currentTimeMillis() - lastPassTime < 2000) {
            Log.i(MY_TAG, "重复进入")
            return
        }

        val userMessage: UserMessage? =
//            userMessageDao?.queryBuilder()?.where(UserMessageDao.Properties.FaceToken.eq("faceToken"))
            userMessageDao?.queryBuilder()?.where(UserMessageDao.Properties.FaceToken.eq(faceToken))
                ?.build()?.unique()
        if (userMessage != null) {

            DustbinBrainApp.userId = userMessage.userId.toInt()
            DustbinBrainApp.userType = userMessage.userType
            LogUtils.dTag(
                "queryFaceToken",
                "userMessage.userId${userMessage.userId},  userMessage.userType${userMessage.userType}"
            )
            LogUtils.dTag(
                "queryFaceToken",
                "userId${DustbinBrainApp.userId},  userType${DustbinBrainApp.userType}"
            )
            lastPassTime = System.currentTimeMillis()
            faceImagePath = mFacePassHandler!!.getFaceImagePath(faceToken.toByteArray())
            //  跳转到垃圾箱控制台
            goControlActivity()
        } else {
            Log.i("addFaceImage", "找不到" + faceToken + "对应的用户")
        }
    }


    private var tcp_client_id //  服务器分配的连接 id
            : String? = null
    private var cache //  字符串缓存，当从服务器传输过来的内容太多可能会被分段发送，所以这里用一个缓存字符串拼接原本 分段的内容
            : String? = null
    private var tcpResponse //  实际经过拼接处理后的 服务器字符串
            : String? = null
    private var lastBindTime // 上一次TCP绑定时间
            : Long = 0
    private var QRReturnTime // 上一次扫码二维码返回时间
            : Long = 0
    val TCP_DEBUG = "TCP调试"
    private fun initTCP() {
        TCPConnectUtil.getInstance().connect()
        TCPConnectUtil.getInstance().setListener(object : NettyClientListener<Any?> {
            override fun onMessageResponseClient(bytes: ByteArray, i: Int) {
                //  来自服务器的响应
                tcpResponse = String(bytes, StandardCharsets.UTF_8)
                Log.i(
                    TCP_DEBUG,
                    "服务器推送内容长度：" + tcpResponse!!.length + "，TCP 当前状态：" + i + "tcpResponse$tcpResponse"
                )
                if (tcpResponse != null && tcpResponse!!.length == 9 && "error msg" == tcpResponse) {
                    return
                }
//                Log.i(
//                    TCP_DEBUG,
//                    "服务器推送内容长度：" + tcpResponse!!.length + "，TCP 当前状态：" + i
//                )
                //  这一步解决一个返回特征值分段问题    =====================================================
                //  以扫码推送特征值开头
                if (tcpResponse!!.startsWith("{\"type\":\"QrReturn\",") && !tcpResponse!!.endsWith("}")) {
                    cache = tcpResponse
                    return
                }

                if (tcpResponse!!.startsWith("{\"type\":\"GQrReturn\",") && !tcpResponse!!.endsWith(
                        "}"
                    )
                ) {
                    cache = tcpResponse
                    return
                }
                //  这一步解决一个返回特征值分段问题    =====================================================
                //  以其它设备推送特征值开头
                if (tcpResponse!!.startsWith("{\"type\":\"nearByFeatrueSend\",") && !tcpResponse!!.endsWith(
                        "}"
                    )
                ) {
                    cache = tcpResponse
                    return
                }
                //  如果 分 3段，为中间那段
                if (!tcpResponse!!.startsWith("{") /*&& tcpResponse.length() > 200*/ && !tcpResponse!!.endsWith(
                        "}"
                    )
                ) {
                    cache = cache + tcpResponse
                    return
                }
                //  改成 60 即可
                if (!tcpResponse!!.startsWith("{") &&  /*tcpResponse.length() > 200 &&*/tcpResponse!!.endsWith(
                        "}"
                    )
                ) {
                    tcpResponse = cache + tcpResponse
                }
                Log.i(
                    TCP_DEBUG,
                    "服务器推送过来的内容拼接结果：" + tcpResponse + ",长度:" + tcpResponse!!.length + "，状态:" + i
                )
                //  首先判定 响应中是否存在 type 和 data 是否以 { 开头
                if (tcpResponse!!.startsWith("{") && tcpResponse!!.contains("type") && tcpResponse!!.contains(
                        "data"
                    ) && tcpResponse!!.endsWith("}")
                ) {
                    try {
                        val jsonObject = JSONObject(tcpResponse)
                        val type = jsonObject.getString("type")
                        val data = jsonObject.getString("data")

                        //  连接认证
                        if (type == "connect_rz_msg") {
                            viewModel!!.registerTCP(tcp_client_id!!)
                        } else if (type == "client_connect_msgect_msg") {
                            //  连接成功注册 与 绑定
                            //NetWorkUtil.getInstance().errorUpload("TCP 绑定 client_connect_msgect_msg");
                            //  TCP 发两个bug
                            if (System.currentTimeMillis() - lastBindTime < 1000) {
                                Log.i(TCP_DEBUG, "TCP触发重复绑定，1s内多次接收到服务器传递过来的绑定信号，抛弃")
                                return
                            }
                            lastBindTime = System.currentTimeMillis() / 1000
                            val verify = TCPVerify()
                            verify.setType("login")
                            val dataBean = TCPVerify.DataBean()
                            dataBean.sign =
                                FenFenCommonUtil.md5(lastBindTime.toString() + FenFenCommonUtil.key)!!
                                    .uppercase(Locale.getDefault())
                            /*dataBean.setTimestamp(String.valueOf(lastBindTime-(60*1000*10)));*/
                            dataBean.timestamp = lastBindTime.toString()
                            verify.setData(dataBean)
                            Log.i(TCP_DEBUG, "发送TCP认证信息" + gson.toJson(verify))
                            TCPConnectUtil.getInstance().sendData(gson.toJson(verify))
                            val tcpVerify: TCPVerifyResponse =
                                gson.fromJson(data, TCPVerifyResponse::class.java)
                            tcp_client_id = tcpVerify.getClient_id()
                        } else if (type == "buy_success_msg") {
                            //  购买 成功反馈
                            val buySuccessMsg: BuySuccessMsg =
                                gson.fromJson(data, BuySuccessMsg::class.java)
                        } else if (type == "QrReturn") {

                            //  扫码推送特征值太快则抛弃。
                            if (System.currentTimeMillis() - QRReturnTime < 1000) {
//                                return;
                            }
                            val vxLoginCall: VXLoginCall =
                                gson.fromJson(data, VXLoginCall::class.java)
                            //  修改当前设置的用户id
                            DustbinBrainApp.userId = vxLoginCall.info.user_id
                            //  修改当前用户类型
                            DustbinBrainApp.userType = vxLoginCall.info.user_type.toLong()
                            //  隐藏二维码扫码

                            //  云端有该人的人脸特征，则将特征保存到本地
                            if (vxLoginCall.isFeatrue_state() && vxLoginCall.isFace_image_state()) {
                                val userMessage: UserMessage? =
                                    DataBaseUtil.getInstance(this@MainActivity)
                                        .daoSession?.userMessageDao?.queryBuilder()
                                        ?.where(
                                            UserMessageDao.Properties.UserId.eq(
                                                vxLoginCall.getInfo().user_id
                                            )
                                        )
                                        ?.unique()
                                //  本地已经有这个人脸特征了，则删除掉原有的人脸特征，添加新的人脸特征
                                if (userMessage != null) {
                                    //  人脸库中删除这个人脸特征
                                    mFacePassHandler!!.deleteFace(
                                        userMessage.faceToken.toByteArray()
                                    )
                                    //  本地数据库中删除这个用户
                                    DataBaseUtil.getInstance(this@MainActivity)
                                        .daoSession?.userMessageDao?.delete(userMessage)
                                    Log.i(DEBUG_TAG, "删除旧的人脸特征与注册信息")
                                } else {
                                    Log.i(DEBUG_TAG, "不存在该用户，可以添加")
                                }
                                Log.i(DEBUG_TAG, "当前用户 含有人脸特征值 和 人脸图片")
                                //  获取来自服务器人脸特征 ( 字符串之前是 Base64 形式 )
                                val feature: ByteArray = Base64.decode(
                                    vxLoginCall.getInfo().getFeatrue(),
                                    Base64.DEFAULT
                                )
                                Log.i("特征值长度", "结果:" + feature.size)
                                val facePassFeatureAppendInfo = FacePassFeatureAppendInfo()
                                //  插入人脸特征值，返回faceToken ，如果特征值不可用会抛出异常
                                val faceToken = mFacePassHandler!!.insertFeature(
                                    feature,
                                    facePassFeatureAppendInfo
                                )
                                //  faceToken 绑定底库
                                val bindResult = mFacePassHandler!!.bindGroup(
                                    group_name,
                                    faceToken.toByteArray()
                                )
                                //  绑定成功就可以 将 faceToken 和 id 进行绑定了
                                if (bindResult) {

                                    //  更新QRReturn时间
                                    QRReturnTime = System.currentTimeMillis()
                                    Log.i(DEBUG_TAG, "绑定成功，将跳转控制台")
                                    //  faceToken 和用户id 绑定
                                    DataBaseUtil.getInstance(this@MainActivity)
                                        .insertUserIdAndFaceToken(
                                            DustbinBrainApp.userId!!.toLong(),
                                            faceToken
                                        )

                                    //  跳转到垃圾箱控制台
                                    goControlActivity()
                                } else {
                                    Log.i(DEBUG_TAG, "绑定失败，删除人脸")
                                    //  如果没有则删除之前的绑定
                                    mFacePassHandler!!.deleteFace(faceToken.toByteArray())
                                }
                            } else {
                                //   云端 没有该用户的 人脸 特征值，则提示需要人脸注册
                                Log.i(DEBUG_TAG, "云端没有该人脸图片和特征值，显示人脸注册")
//                                mainHandler.post(Runnable { showVerifyFail() })
                            }
                        } else if (type == "GQrReturn") {
                            //  回收桶二维码登录
                            val gQrReturnBean: GQrReturnBean =
                                gson.fromJson(data, GQrReturnBean::class.java)
                            DustbinBrainApp.userId = gQrReturnBean.info.user_id
                            DustbinBrainApp.userType = gQrReturnBean.info.user_type.toLong()
                            canRecognize = true
                            goControlActivity()
                        } else if (type == "nfcActivity") {
                            val nfcActivityBean: NfcActivityBean =
                                gson.fromJson(data, NfcActivityBean::class.java)
                            //  nfc 绑定成功
                            if (nfcActivityBean.getData().getCode() === 1) {
                                //  设置用户id
                                DustbinBrainApp.userId =
                                    nfcActivityBean.getData().info.user_id
                                DustbinBrainApp.userType =
                                    nfcActivityBean.getData().info.user_type.toLong()

                                //  跳转到垃圾箱控制台
                                goControlActivity()
                            }
                        } else if (type == "nearByFeatrueSend") {
                            val nearByFeatrueSendBean: NearByFeatrueSendBean = gson.fromJson(
                                data,
                                NearByFeatrueSendBean::class.java
                            )
                            if (nearByFeatrueSendBean != null && nearByFeatrueSendBean.getFeatrue() != null) {
                                Log.i("响应结果", nearByFeatrueSendBean.toString())
                                //  获取来自服务器人脸特征 ( 字符串之前是 Base64 形式 )
                                val feature: ByteArray = Base64.decode(
                                    nearByFeatrueSendBean.getFeatrue(),
                                    Base64.DEFAULT
                                )
                                val facePassFeatureAppendInfo = FacePassFeatureAppendInfo()
                                //  插入人脸特征值，返回faceToken ，如果特征值不可用会抛出异常
                                val faceToken = mFacePassHandler!!.insertFeature(
                                    feature,
                                    facePassFeatureAppendInfo
                                )
                                //  facetoken 绑定底库
                                val bindResult = mFacePassHandler!!.bindGroup(
                                    group_name,
                                    faceToken.toByteArray()
                                )
                                //  绑定成功就可以 将 facetoken 和 id 进行绑定了
                                if (bindResult) {
                                    //  facetoken 和用户id 绑定
                                    DataBaseUtil.getInstance(this@MainActivity)
                                        .insertUserIdAndFaceToken(
                                            nearByFeatrueSendBean.getUser_id().toLong(),
                                            faceToken
                                        )
                                }
                            } else {
                            }
                        } else if (type == "deleteAllFace") {
                            //  删除所有用户信息
                            DataBaseUtil.getInstance(this@MainActivity)
                                .getDaoSession()!!.userMessageDao.deleteAll()
                            //  删除人脸库
                            mFacePassHandler!!.clearAllGroupsAndFaces()
                            //  重启
//                            AndroidDeviceSDK.reBoot(this@MainActivity)
                        } else if (type == "updateAllUserType0") {
                            //  修改所有用户类型 为 0
                            val userMessageList: List<UserMessage> =
                                DataBaseUtil.getInstance(this@MainActivity)
                                    .getDaoSession()!!.userMessageDao.queryBuilder().list()
                            for (userMessage in userMessageList) {
                                userMessage.setUserType(0)
                            }
                            DataBaseUtil.getInstance(this@MainActivity)
                                .getDaoSession()!!.userMessageDao.updateInTx(userMessageList)
//                            NetWorkUtil.getInstance().errorUpload("用户类型已全部修改为 0 ")
                            Log.i("updateAllUserType0", "修改之前")
                        } else if (type == "connect_restartApp_msg") {
                            var url =
                                "https://ffadmin.fenfeneco.com/uploads/20210630/74a1162af848ccf6ee362dd16454ff18.apk"
                            val packageManager: PackageManager = packageManager
                            val intent =
                                packageManager.getLaunchIntentForPackage(getPackageName())
                            if (intent != null) {
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                startActivity(intent)
                                Process.killProcess(Process.myPid())
                            }
                        } else if (type == "updateApp1to1") {
                            if (!TextUtils.isEmpty(data)) {
                                //执行更新操作
                                LogUtils.e("更新地址:$data")
                                EventBus.getDefault().post(data)
                            }
                        } else if ( /*type.equals("addFaceImage")*/false) {  //  添加人脸，人脸在服务器注册
                            Log.i("addFaceImage", "进入添加人脸")
                            val addFaceImageJsonObject = JsonParser.parseString(data).asJsonObject
                            val userId = addFaceImageJsonObject["userId"].asInt
                            val userType = addFaceImageJsonObject["userType"].asLong
                            val imageUrl = addFaceImageJsonObject["imageUrl"].asString
                            DustbinBrainApp.userId = userId
                            DustbinBrainApp.userType = userType
                            Log.i("addFaceImage", addFaceImageJsonObject.toString())
                            Log.i("addFaceImage", "$userId,$userType,$imageUrl")


                            //  查询是否已经有这个人的特征值
                            val userMessage: UserMessage =
                                DataBaseUtil.getInstance(this@MainActivity)
                                    .getDaoSession()!!.getUserMessageDao().queryBuilder()
                                    .where(UserMessageDao.Properties.UserId.eq(userId))
                                    .unique()
                            //  本地已经有这个人脸特征了，则删除掉原有的人脸特征，添加新的人脸特征
                            if (userMessage != null) {
                                //  人脸库中删除这个人脸特征
                                mFacePassHandler!!.deleteFace(
                                    userMessage.getFaceToken().toByteArray()
                                )
                                //  本地数据库中删除这个用户
                                DataBaseUtil.getInstance(this@MainActivity)
                                    .getDaoSession()!!.userMessageDao.delete(userMessage)
                                Log.i("addFaceImage", "删除旧的人脸特征与注册信息" + userMessage.getUserId())
                            } else {
                                Log.i("addFaceImage", "不存在该用户，可以添加$userId")
                            }
                            downloadFaceImage(imageUrl, userId.toLong(), userType.toLong())
                        }
                    } catch (e: java.lang.Exception) {
                        e.printStackTrace()
                        Log.i(TCP_DEBUG, "发生异常：" + e.message)
                    }
                } else {
                    Log.i(TCP_DEBUG, "接收到不符合规范的 TCP 推送值$tcpResponse")
                }

            }

            override fun onClientStatusConnectChanged(i: Int, i1: Int) {
                runOnUiThread {
                    if (i == ConnectState.STATUS_CONNECT_SUCCESS) {
                        Log.e(TAG, "STATUS_CONNECT_SUCCESS:")
                    } else {
                        Log.e(TAG, "onServiceStatusConnectChanged:$i")
                    }
                }
            }
        })
    }

    val TAG = "投递时间"
    fun goControlActivity() {
        if (!canRecognize) {
            return
        }
        if (binsWorkTimeBean == null) {
            var map: MutableMap<String, String> = mutableMapOf(
                "device_id" to deviceCode.toString()
            )
            viewModel!!.getBinsWorkTime(map)
        }
        LogUtils.dTag(
            "goControlActivity",
            "binsWorkTime${binsWorkTimeBean?.getData()?.am_start_time}"
        )
        DustbinBrainApp.hasManTime = System.currentTimeMillis()
        LogUtils.dTag("改变倒计时","DustbinBrainApp.hasManTime:${TimeUtils.millis2Date(DustbinBrainApp.hasManTime)}")
        //判断是否为特殊用户
        if (DustbinBrainApp.userType.toInt() == 1) {
            //  跳转到垃圾箱控制台
            val intent = Intent(this@MainActivity, ControlActivity::class.java)
//            val intent = Intent(this@MainActivity, SerialProtTestActivity::class.java)
            intent.putExtra("userId", ""+DustbinBrainApp.userId)
            intent.putExtra("faceImage", faceImagePath)
            startActivityForResult(intent, 300)
        } else {
            //  非特殊用户
            if (BinsWorkTimeUntil.getBinsWorkTime(binsWorkTimeBean)) {
                Log.i("MainActivity.TAG", "非特殊用户 投放时间")
                //  是投放时间，跳转到垃圾箱控制台
                val intent = Intent(
                    this@MainActivity,
                    ControlActivity::class.java
//                    SerialProtTestActivity::class.java
                )
                intent.putExtra("userId", ""+DustbinBrainApp.userId)
                intent.putExtra("faceImage", faceImagePath)
                startActivityForResult(intent, 300)
            } else {
                //  showToast("验证成功，但非投放时间", Toast.LENGTH_SHORT, false, null);
                Toast.makeText(this@MainActivity, "非投放时间", Toast.LENGTH_LONG).show()
                //  非投放时间
                VoiceUtil.getInstance().openAssetMusics(this@MainActivity, "no_work_time.aac")
            }
        }
    }

    /**
     * 验证失败,显示添加人脸弹窗
     */
    private var alertDialog: AlertDialog? = null


    //  H5 唤起相机 拍照回调此路径
    private var mUri: Uri? = null

    //  图片文件
    private var file: File? = null
    private fun showVerifyFail() {
        val alertB = AlertDialog.Builder(this@MainActivity)
        alertB.setCancelable(false)
        alertB.setTitle("提示")
        alertB.setMessage("需要进行人脸注册")
        alertB.setPositiveButton(
            "人脸注册"
        ) { dialogInterface, i -> //  打开相机拍照
            //  步骤一：创建存储照片的文件
            val path = Environment.getExternalStorageDirectory().toString()
            file = File(path, System.currentTimeMillis().toString() + ".jpg")
            if (!file!!.parentFile.exists()) file!!.parentFile.mkdirs()
            mUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                //  步骤二：Android 7.0及以上获取文件 Uri
                FileProvider.getUriForFile(this@MainActivity, "$packageName.fileprovider", file!!)
            } else {
                //  步骤三：获取文件Uri
                Uri.fromFile(file)
            }
            //  步骤四：调取系统拍照
            val intent = Intent("android.media.action.IMAGE_CAPTURE")
            intent.putExtra(MediaStore.EXTRA_OUTPUT, mUri)
            startActivityForResult(intent, REQUEST_CODE_CAMERA)
        }
        alertB.setNegativeButton(
            "取消"
        ) { dialog, which ->
            DustbinBrainApp.userId = 0
            DustbinBrainApp.userType = 0
            dialog.dismiss()
        }
        alertB.create()
        alertDialog = alertB.show()
    }

    private var nowFaceToken: String = ""

    /**
     * 下载人脸图片
     */
    @Synchronized
    @Throws(java.lang.Exception::class)
    private fun downloadFaceImage(imageUrl: String, userId: Long, userType: Long) {
        Log.i("addFaceImage", "开始下载人脸图片")
        //  开始下载图片
        DownloadUtil.get().download(
            imageUrl,
            Environment.getExternalStorageDirectory().toString(),
            System.currentTimeMillis().toString() + ".jpg",
            object : DownloadUtil.OnDownloadListener {
                override fun onDownloadSuccess(file: File) {
                    Log.i("addFaceImage", "下载完毕")
                    try {


                        //  提取图片特征值
                        val facePassExtractFeatureResult =
                            mFacePassHandler!!.extractFeature(BitmapFactory.decodeFile(file.absolutePath))
                        //  如果特征值合格
                        if (facePassExtractFeatureResult.result == 0) {
                            val facePassFeatureAppendInfo = FacePassFeatureAppendInfo()

                            //  创建 faceToken
                            val faceToken = mFacePassHandler!!.insertFeature(
                                facePassExtractFeatureResult.featureData,
                                facePassFeatureAppendInfo
                            )
                            nowFaceToken = faceToken
                            val bindResult =
                                mFacePassHandler!!.bindGroup(group_name, faceToken.toByteArray())

                            //  绑定结果
                            if (bindResult) {
                                Log.i("addFaceImage", "$userId,绑定成功$faceToken")
                                //  本地实现
                                DataBaseUtil.getInstance(this@MainActivity)
                                    .insertUserIdAndFaceTokenThread(userId, userType, nowFaceToken)
                                val deviceId: String? = mmkv!!.decodeString(MMKVCommon.DEVICE_ID)
                                val hasMap: MutableMap<String, String> = mutableMapOf(
                                    "user_id" to userId.toString(),
                                    "device_id" to deviceId!!
                                )
                                //  通知人脸合格
                                viewModel!!.postFaceRegisterSuccessLog(hasMap)
                            } else {
                                mFacePassHandler!!.deleteFace(faceToken.toByteArray())
                                Log.i("addFaceImage", "$userId,绑定失败")
                            }
                        } else {
                            Handler().post(Runnable {
                                Toast.makeText(this@MainActivity, "人脸不合格", Toast.LENGTH_SHORT)
                                    .show()
                                file.delete()
                            })
                        }
                    } catch (e: java.lang.Exception) {
                        Log.i("addFaceImage", e.message!!)
                    } finally {
                        //  删除图片
                        //  file.delete();
                    }
                }

                override fun onDownloading(progress: Int) {}
                override fun onDownloadFailed(e: java.lang.Exception) {
                    Log.i("addFaceImage", "下载失败" + e.message)
                }
            })
    }
}
