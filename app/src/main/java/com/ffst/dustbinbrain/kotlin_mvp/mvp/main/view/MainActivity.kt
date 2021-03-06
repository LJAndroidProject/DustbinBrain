package com.ffst.dustbinbrain.kotlin_mvp.mvp.main.view

import android.app.AlertDialog
import android.content.Intent
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
import android.view.KeyEvent
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
import com.tencent.liteav.trtccalling.model.VoiceUtil
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
        const val MY_TAG = "??????????????????"
        val group_name = FenFenCommonUtil.face_group_name
        val CONTROL_RESULT_CODE = 300

        /* ???????????? */
        var manager: CameraManager? = null

        //  ??????
        val REQUEST_CODE_CAMERA = 500

        //??????
        val REQUEST_CODE_PHONE_LOGIN = 400
    }

    //  ??????????????????
    enum class FacePassSDKMode {
        MODE_ONLINE, MODE_OFFLINE
    }

    var viewModel: MainActivityViewModel? = null

    var mmkv: MMKV? = null

    var binsWorkTimeBean: BinsWorkTimeBean? = null

    /**SDK??????*/
    var mFacePassHandler: FacePassHandler? = null


    /* ????????????????????????????????? */
    private var cameraFacingFront = true

    /* ?????????????????????????????????????????????????????????
     * ???????????????????????????????????????????????????rotation
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

    var screenState = 0 // 0 ??? 1 ???
    private var heightPixels = 0
    private var widthPixels = 0

    private var isLocalGroupExist = false

    /*????????????*/
    private var mImageCache: FaceImageCache? = null

    /*DetectResult queue*/
    var mDetectResultQueue: ArrayBlockingQueue<ByteArray>? = null
    var mFeedFrameQueue: ArrayBlockingQueue<CameraPreviewData>? = null

    /*Toast*/
    private var mRecoToast: Toast? = null

    /*recognize thread*/
    var mRecognizeThread: RecognizeThread? = null
    var mFeedFrameThread: FeedFrameThread? = null

    /*????????????*/
    private val mSyncGroupBtn: ImageView? = null
    private val mSyncGroupDialog: AlertDialog? = null

    private val mFaceOperationBtn: ImageView? = null

    private var canRecognize = true

    var deviceCode: String? = null

    /*????????????*/

    private var mAndroidHandler: Handler? = null

    private val gson = Gson()

    private var userMessageDao: UserMessageDao? = null

    private var app: DustbinBrainApp? = null

    private var mLastClickTime = 0.toLong()
    private var mSecretNumber = 0

    private var dustbinArrry = mutableListOf<DustbinStateBean>()
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
        if ("qingzheng" === AndroidDeviceSDK.deviceType) {

            AndroidDeviceSDK.hideStatus(this, true)
        } else {
            AndroidDeviceSDK.hideStatus(this, false)
        }
        AndroidDeviceSDK.keepActivity(this)
        AndroidDeviceSDK.setSchedulePowerOn(this)
        AndroidDeviceSDK.setSchedulePowerOff()
        AndroidDeviceSDK.setDynamicIP()

        initAndroidHandler()
        /* ??????????????? */
        initView()
        bindData()
        //  ????????? greenDao ???????????????????????????????????????
        userMessageDao = DataBaseUtil.getInstance(this@MainActivity).daoSession.userMessageDao
        initTCP()
        //??????????????????
        var map: MutableMap<String, String> = mutableMapOf(
            "device_id" to deviceCode.toString()
        )
        viewModel?.getBinsWorkTime(map)
        //?????????????????????
        viewModel?.getDeviceQrcode(map)


        //  ?????????????????????
        val dustbinConfig =
            DataBaseUtil.getInstance(this).daoSession.dustbinConfigDao.queryBuilder().unique()
        DustbinBrainApp.dustbinConfig = dustbinConfig
        val dustbinList = DataBaseUtil.getInstance(this@MainActivity).getDustbinByType(null)
        DustbinBrainApp.dustbinBeanList?.addAll(
            dustbinList
        )
        dustbinArrry.addAll(dustbinList)
        //  ??????APP?????????????????????
        closeAllDoor()

        //  ????????????????????????????????? ??????????????????????????????????????????????????????????????????????????????
        VoiceUtil.getInstance()
        //???????????????SDK
        initFaceSDK()

        //???????????????????????????
        initFaceHandler()
        mRecognizeThread = RecognizeThread()
        mRecognizeThread?.start()
        mFeedFrameThread = FeedFrameThread()
        mFeedFrameThread?.start()
        //  ????????????????????????????????????
        startService(Intent(this, ResidentService::class.java))
        val imUserId = mmkv?.decodeString(MMKVCommon.IM_USERID)
        LogUtils.iTag("ProfileManager", "??????ID???$imUserId")
        ProfileManager.getInstance().login(
            imUserId,
            "",
            object : ProfileManager.ActionCallback {
                override fun onSuccess() {
                    LogUtils.iTag("ProfileManager", "??????????????????")
                    runOnUiThread {
                        if ("qingzheng" === AndroidDeviceSDK.deviceType) {
                            video_call_siv.visibility = View.INVISIBLE
                        } else {
                            video_call_siv.visibility = View.VISIBLE

                        }
                        CallService.start(this@MainActivity)
                    }
                }

                override fun onFailed(code: Int, msg: String?) {
                    LogUtils.iTag("ProfileManager", "${code}??????????????????$msg")
                    runOnUiThread {
                        video_call_siv.visibility = View.INVISIBLE
                    }

                }
            })

        scanKeyManager = ScanKeyManager { qrScan ->
            LogUtils.eTag("USB??????", "?????????$qrScan")
            loadingView(true)
            canRecognize = false
            viewModel?.getScanLogin(qrScan)
        }
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
        //  ??????????????????
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
        //????????????
        preview.outlineProvider = CicleViewOutlineProvider(FaceView.circleDimater)
        preview.clipToOutline = true
        //??????????????????
        manager?.setPreviewDisplay(preview)
        /* ???????????????????????? */
        manager?.setListener(this)
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
            //  ?????????????????????
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
        face_register_siv.setOnClickListener {
            val intent = Intent(this@MainActivity, FaceRegisterActivity::class.java)
            startActivity(intent)
        }

    }

    override fun onResume() {
        super.onResume()
        if (!isPhoneLogin) {
            var isOpen =manager!!.open(windowManager, false, cameraWidth, cameraHeight)
            LogUtils.dTag(TAG,"???????????????isOpen:$isOpen")
        }
        canRecognize = true
        LogUtils.iTag(TAG, "?????????????????????????????????")
    }

    override fun onPause() {
        super.onPause()
        LogUtils.iTag(TAG, "?????????????????????????????????")
        manager!!.release()
        canRecognize = false
    }

    var isPhoneLogin = false

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        canRecognize = true
        isPhoneLogin = false
        when (requestCode) {
            CONTROL_RESULT_CODE -> {

                if (data != null) {
                    val exitCode = data.getIntExtra("exitCode", 0)
                    val facePath = data.getStringExtra("faceUri")
                    if (TextUtils.isEmpty(facePath)) {
                        DustbinBrainApp.userId = 0;
                        DustbinBrainApp.userType = 0;
//                        //  ????????????
//                        if (exitCode != 0) {
//                            if (exitCode == 1) {
////                                closeAllDoor()
//                            }
//                        }
                    } else {
                        val TAG = "??????"
                        file = File(facePath)
                        var bitmap = BitmapFactory.decodeFile(file.toString())
                        try {
                            //  ??????????????????
//                        resiget_face.setImageBitmap(bitmap)
                            //  ??????????????????????????????????????????
                            if (bitmap.width > bitmap.height) {
                                bitmap = adjustPhotoRotation(bitmap, 90)
                            }
                        } catch (e: Exception) {
                            runOnUiThread { ToastUtils.showShort("?????????????????????") }
                        }


                        try {
                            //  ???????????????
                            val facePassExtractFeatureResult =
                                mFacePassHandler?.extractFeature(bitmap)

                            //  ?????????????????????
                            if (facePassExtractFeatureResult?.result == 0) {
                                val facePassFeatureAppendInfo = FacePassFeatureAppendInfo()

                                //  ?????? faceToken
                                val faceToken = mFacePassHandler?.insertFeature(
                                    facePassExtractFeatureResult.featureData,
                                    facePassFeatureAppendInfo
                                )
                                if (faceToken != null) {
                                    nowFaceToken = faceToken
                                }
                                val bindResult =
                                    mFacePassHandler?.bindGroup(
                                        group_name,
                                        faceToken?.toByteArray()
                                    )

                                //  ????????????
                                if (bindResult == true) {
                                    Log.i(
                                        TAG,
                                        "????????????faceToken:$faceToken" + "  , userId:${DustbinBrainApp.userId} , userType:${DustbinBrainApp.userType}"
                                    )

                                    //  ??????facetoken ?????????id????????????
                                    val userMessage: UserMessage? =
                                        DataBaseUtil.getInstance(this@MainActivity)
                                            .daoSession?.userMessageDao?.queryBuilder()
                                            ?.where(
                                                UserMessageDao.Properties.UserId.eq(
                                                    DustbinBrainApp.userId!!
                                                )
                                            )
                                            ?.unique()
                                    //  ???????????????????????????????????????????????????????????????????????????????????????????????????
                                    if (userMessage != null) {
                                        //  ????????????????????????????????????
                                        mFacePassHandler!!.deleteFace(
                                            userMessage.faceToken.toByteArray()
                                        )
                                        //  ????????????????????????????????????
                                        DataBaseUtil.getInstance(this@MainActivity)
                                            .daoSession?.userMessageDao?.delete(userMessage)
                                        Log.i(
                                            "addFaceImage",
                                            "???????????????????????????????????????" + userMessage.getUserId()
                                        )
                                    }
                                    //  ????????????
                                    DustbinBrainApp.userType?.let {
                                        DataBaseUtil.getInstance(this@MainActivity)
                                            .insertUserIdAndFaceTokenThread(
                                                DustbinBrainApp.userId!!.toLong(),
                                                it,
                                                nowFaceToken
                                            )
                                    }
                                    //  ??????????????????,????????????????????????id
                                    uploadFace(
                                        file!!,
                                        facePassExtractFeatureResult.featureData,
                                        DustbinBrainApp.userId!!.toLong()
                                    )
                                } else {
                                    mFacePassHandler!!.deleteFace(faceToken?.toByteArray())
                                    Log.i(TAG, "????????????")
                                }
                            } else {
                                toast("?????????????????????")
                            }
                        } catch (e: java.lang.Exception) {
                            //  ????????????
                            DataBaseUtil.getInstance(this@MainActivity)
                                .insertUserIdAndFaceTokenThread(
                                    DustbinBrainApp.userId!!.toLong(),
                                    DustbinBrainApp.userType!!,
                                    nowFaceToken
                                )
                            Log.i(TAG, e.message!!)
                        } finally {
                            //  ????????????
                            //  file.delete();
                        }
                    }
                }
                Thread {
                    for (dustbinStateBean in dustbinArrry) {
                        //  ????????????
                        if (dustbinStateBean.dustbinBoxNumber != "A") {
                            SerialProManager.getInstance().closeLight(dustbinStateBean.doorNumber)
                        }
                        try {
                            Thread.sleep(50)
                        } catch (e: java.lang.Exception) {
                            e.printStackTrace()
                        }
                    }
                }.start()
            }
            REQUEST_CODE_PHONE_LOGIN -> {
                isPhoneLogin = true
                if (data != null) {
                    if (data.getBooleanExtra("isSuccess", false)) {
                        goControlActivity()
                    }else{
                        var isOpen =manager?.open(windowManager, false, cameraWidth, cameraHeight)
                        LogUtils.dTag(TAG,"???????????????isOpen:$isOpen")
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

        // ??????????????????????????????????????????
        val requestBody = RequestBody.create("image/jpg".toMediaTypeOrNull(), file)
        val nowTime = System.currentTimeMillis() / 1000
        val androidID = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)


        // ??????????????????????????????
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
                Log.i(TAG, "?????????????????????" + e.message)
                Handler().post(Runnable { //  ???????????????????????????????????????
                    val alertB = AlertDialog.Builder(this@MainActivity)
                    alertB.setCancelable(false)
                    alertB.setTitle("??????")
                    alertB.setMessage("??????????????????")
                    alertB.setPositiveButton(
                        "??????"
                    ) { dialogInterface, i -> uploadFace(file, feature, userId) }
                    alertB.setNegativeButton(
                        "??????"
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
                Log.i(TAG, "?????????????????????" + imageUploadResult.toString())


                //  ??????????????????
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
                                Log.i(TAG, "???????????? json ?????????$response")
                                val resultMould: ResultMould = Gson().fromJson(
                                    response,
                                    ResultMould::class.java
                                )
                                //  ??????????????????
                                if (resultMould.getCode() == 1) {
                                    Log.i(TAG, "???????????? userToken ??? userID ??????")

                                    //  ????????????
                                    DataBaseUtil.getInstance(this@MainActivity)
                                        .insertUserIdAndFaceToken(
                                            DustbinBrainApp.userId!!.toLong(),
                                            nowFaceToken
                                        )
                                    Log.i(
                                        TAG,
                                        DustbinBrainApp.userId.toString() + "?????? " + nowFaceToken
                                    )
                                    file.delete()
//                                    goControlActivity()
                                }
                            }

                            override fun fail(call: Call?, e: IOException) {
                                Log.i(TAG, "?????????????????? fail???" + e.message)
                            }

                            override fun error(e: java.lang.Exception) {
                                Log.i(TAG, "??????????????????error ???" + e.message)
                            }
                        })
                }
            }
        })
    }

    //  ???????????????
    private fun closeAllDoor() {
        //  ??????APP??????????????????
        object : Thread() {
            override fun run() {
                super.run()
                for (dustbinStateBean in dustbinArrry) {
                    SerialProManager.getInstance()
                        .closeDoor(dustbinStateBean.doorNumber)
                    try {
                        sleep(250)
                    } catch (e: java.lang.Exception) {
                        e.printStackTrace()
                    }

                    //  ????????????
                    SerialProManager.getInstance()
                        .openExhaustFan(dustbinStateBean.doorNumber)
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
     * ???????????????SDK
     */
    private fun initFaceSDK() {
        //  ??????????????????
        FacePassHandler.getAuth(
            FenFenCommonUtil.authIP,
            FenFenCommonUtil.apiKey,
            FenFenCommonUtil.apiSecret
        )
        FacePassHandler.initSDK(applicationContext)
        LogUtils.dTag(FenFenCommonUtil.FACE_TAG, FacePassHandler.getVersion())
    }

    /**
     * ???????????????????????????
     */
    private fun initFaceHandler() {
        ThreadManager.getInstance().execute {
            while (!isFinishing) {
                while (FacePassHandler.isAvailable()) {
                    LogUtils.dTag(FenFenCommonUtil.FACE_TAG, "start to build FacePassHandler")
                    val config: FacePassConfig
                    try {
                        /* ???????????????????????? */
                        config = FacePassConfig()
                        config.poseBlurModel = FacePassModel.initModel(
                            applicationContext.assets,
                            "attr.pose_blur.align.av200.190630.bin"
                        )
                        //????????????CPU rgb????????????
                        config.livenessModel = FacePassModel.initModel(
                            applicationContext.assets,
                            "liveness.CPU.rgb.int8.D.bin"
                        )
                        //????????????CPU rgbir????????????
                        config.rgbIrLivenessModel = FacePassModel.initModel(
                            applicationContext.assets,
                            "liveness.CPU.rgbir.int8.D.bin"
                        )
                        //????????????????????????????????????GPU???????????????????????????livenessGPUCache
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
                        //?????????????????????????????????????????????smileModel???ageGenderModel?????????null
                        config.smileModel = null
                        config.ageGenderModel = null
                        config.searchThreshold = 71f //???????????????????????????????????????
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
                        /* ??????SDK?????? */
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
     * ???????????????????????????????????????????????????
     */
    fun checkGroup() {
        //??????????????????
//        addBindTest()
        if (mFacePassHandler == null) {
            return
        }
        //  ??????????????????
        var localGroups: Array<String?>? = arrayOfNulls(0)
        try {
            localGroups = mFacePassHandler!!.localGroups
        } catch (e: FacePassException) {
            e.printStackTrace()
        }
        isLocalGroupExist = false

        //  ????????? null ????????????
        if (localGroups == null || localGroups.size == 0) {

            //  ??????????????????????????????????????????
            try {
                mFacePassHandler!!.createLocalGroup(group_name)
                isLocalGroupExist = true
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return
        }

        //  ???????????????????????????????????????
        for (group in localGroups) {
            if (group_name == group) {
                isLocalGroupExist = true
            }
        }
        if (!isLocalGroupExist) {

            //  ??????????????????????????????????????????
            try {
                mFacePassHandler!!.createLocalGroup(group_name)
                isLocalGroupExist = true
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * ?????????????????????
     */
    fun bindData() {
        //??????????????????
        viewModel?.liveDataForDustbinConfig?.observe(this) { data ->
            if (data.success) {
                if (data.binsWorkTimeBean != null) {
                    binsWorkTimeBean = data.binsWorkTimeBean
                }
            }
        }

        viewModel?.liveDataForScanLogin?.observe(this) { data ->
            canRecognize = true
            hideLoadingView()
            if (data.success) {
                val userBeanModel = data.data
                DustbinBrainApp.userId = userBeanModel?.user_id?.toInt()
                DustbinBrainApp.userType = userBeanModel?.user_type?.toLong()
                goControlActivity()
            } else {
                ToastUtils.showShort("${data.msg}")
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
     * ??????facetoken??????????????????
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

            //  ????????????
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
                /* ????????????????????????SDK???????????????????????? FacePassImage */
                val startTime = System.currentTimeMillis() //????????????
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
                    Log.e(MainActivity.MY_TAG, "?????????invalid??????")
                    continue
                }

                /* ????????????FacePassImage ??????SDK????????? ????????????????????? */
                var detectionResult: FacePassDetectionResult? = null
                try {
                    detectionResult = mFacePassHandler!!.feedFrame(image)
                } catch (e: FacePassException) {
                    Log.e(MainActivity.MY_TAG, "??????????????????")
                    e.printStackTrace()
                }
                if (detectionResult == null || detectionResult.faceList.size == 0) {
                    /* ??????????????????????????? */
                    runOnUiThread {
                        fcview.clear()
                        fcview.invalidate()
                    }
                } else {
                    /* ????????????????????????????????????????????????????????????????????????????????????????????? */
                    val bufferFaceList = detectionResult.faceList
//                    if(!fcview.isInCircle(faceList)){
//                        break
//                    }
                    runOnUiThread { showFacePassFace(bufferFaceList) }
                }

                //  ???????????????????????????????????? detectionResult ?????????????????????????????????????????? FacePassRecognitionResult ???????????? faceToken ???faceToken???????????????id???
                if (SDK_MODE == FacePassSDKMode.MODE_ONLINE) {

                } else {
                    /*???????????????????????????????????????message????????????result????????????????????????*/
                    if (detectionResult != null && detectionResult.message.size != 0) {
                        Log.d(DEBUG_TAG, "mDetectResultQueue.offer")
                        //  FacePassRecognitionResult
                        Log.i(MY_TAG, "??????????????????")
                        //  ??????
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
                        Log.i(MY_TAG, "??????????????? detectionResult.message " + faceToken);*/
                        mDetectResultQueue!!.offer(
                            detectionResult.message
                        )
                    }
                }
                val endTime = System.currentTimeMillis() //????????????
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
            val mirror: Boolean = cameraFacingFront /* ???????????????mirror???true */
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
            faceRollString.append("??????: ").append(face.pose.roll.toInt()).append("??")
            val facePitchString = StringBuilder()
            facePitchString.append("??????: ").append(face.pose.pitch.toInt()).append("??")
            val faceYawString = StringBuilder()
            faceYawString.append("??????: ").append(face.pose.yaw.toInt()).append("??")
            val faceBlurString = StringBuilder()
            faceBlurString.append("??????: ").append(face.blur)
            val smileString = StringBuilder()
            smileString.append("??????: ").append(String.format("%.6f", face.smile))
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

                    //  ?????????????????????
                    val ageGenderResult: Array<FacePassAgeGenderResult>? = null
                    Log.i(MY_TAG, "????????????????????????????????????")
                    Log.d(DEBUG_TAG, "mDetectResultQueue.isLocalGroupExist")

                    //  ??????????????????
                    if (isLocalGroupExist) {
                        Log.d(DEBUG_TAG, "mDetectResultQueue.recognize")
                        Log.i(MY_TAG, "isLocalGroupExist ??? true ??? ???????????????")

                        //  ?????? ?????????????????????FacePassRecognitionResult?????????????????????????????????????????????????????????
                        val recognizeResult =
                            mFacePassHandler!!.recognize(group_name, detectionResult)


                        //  ???????????????????????????????????????
                        if (recognizeResult != null && recognizeResult.size > 0) {
                            Log.i(MY_TAG, "??????????????????")
                            for (result in recognizeResult) {
                                val faceToken = String(result.faceToken)
                                nowFaceToken = faceToken
                                Log.i("addFaceImage", "faceToken ??????????????????????????????$faceToken")


                                //  ??????faceToken ?????????????????????????????????????????? userId????????????????????????????????????????????????

//                                if (FacePassRecognitionResultType.RECOG_OK == result.facePassRecognitionResultType) {
                                getFaceImageByFaceToken(result.trackId, faceToken)
                                //                                }
                                val idx = findidx(ageGenderResult, result.trackId)
                                //  -1?????????????????????????????? ????????? ageGenderResult (??????????????????) ??? null
                                if (idx == -1) {

                                    //  ??????????????? Toast
                                    showRecognizeResult(
                                        result.trackId,
                                        result.detail.searchScore,
                                        result.detail.livenessScore,
                                        !TextUtils.isEmpty(faceToken),
                                        0f,
                                        0
                                    )
                                } else {
                                    //  ???????????? Toast
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
                            //  ?????????????????????,???????????????????????????????????????
                            Handler(Looper.getMainLooper()).post {
                                //  showVerifyFail();
                            }
                            Log.i(
                                MY_TAG,
                                "?????????????????? recognizeResult != null && recognizeResult.length > 0) ??? false"
                            )
                        }
                    } else {
                        Log.i(MY_TAG, "isLocalGroupExist ??? false ?????????????????????")
                    }
                } catch (e: InterruptedException) {
                    Log.e(MY_TAG, "??????getFaceImageByFaceToken?????????RecognizeThread.run")
                    e.printStackTrace()
                } catch (e: FacePassException) {
                    Log.e(MY_TAG, "??????getFaceImageByFaceToken?????????RecognizeThread.run")
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
            s = SpannableString("????????????")
            imageView.setImageResource(R.drawable.success)
        } else {
            s = SpannableString("????????????")
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
                //showToast("ID = " + String.valueOf(trackId), Toast.LENGTH_SHORT, false, null);    ??????????????????
                Log.i(
                    MY_TAG,
                    "searchScore : " + searchScore + ",livenessScore : " + livenessScore + "???????????????"
                )
                //  ???????????????????????????
//                showQRCodeDialog()
                //  showVerifyFail();
            } else {
                showToast("ID = $nowFaceToken", Toast.LENGTH_SHORT, true, null)
                Log.i(
                    MY_TAG,
                    "searchScore : " + searchScore + ",livenessScore : " + livenessScore + "????????????"
                )
                queryFaceToken(nowFaceToken)
            }

//            faceEndTextView.append(
//                """
//                        ID = $trackId${if (isRecognizeOK) "????????????" else "????????????"}
//
//                        """.trimIndent()
//            )
//            faceEndTextView.append("????????? = $searchScore\n")
//            faceEndTextView.append("????????? = $livenessScore\n")
        }
    }

    //  ?????????????????????????????????
    private var lastPassTime: Long = 0
    private var faceImagePath: String = ""
    fun queryFaceToken(faceToken: String) {
        Log.i(MY_TAG, "faceToken:$faceToken")
        //  ?????????????????? ??????????????????2s
        if (System.currentTimeMillis() - lastPassTime < 2000) {
            Log.i(MY_TAG, "????????????")
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
            val list = userMessageDao!!.queryBuilder().list()
            for (um in list) {
                Log.i(TAG, "??????id????????????$um")
            }
            //  ???????????????????????????
            goControlActivity()
        } else {
            Log.i("addFaceImage", "?????????" + faceToken + "???????????????")
            val list = userMessageDao!!.queryBuilder().list()
            for (um in list) {
                Log.i(TAG, "??????id????????????$um")
            }
        }
    }


    private var tcp_client_id //  ???????????????????????? id
            : String? = null
    private var cache //  ??????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????? ???????????????
            : String? = null
    private var tcpResponse //  ?????????????????????????????? ??????????????????
            : String? = null
    private var lastBindTime // ?????????TCP????????????
            : Long = 0
    private var QRReturnTime // ????????????????????????????????????
            : Long = 0
    val TCP_DEBUG = "TCP??????"
    private fun initTCP() {
        TCPConnectUtil.getInstance().connect()
        TCPConnectUtil.getInstance().setListener(object : NettyClientListener<Any?> {
            override fun onMessageResponseClient(bytes: ByteArray, i: Int) {
                //  ????????????????????????
                tcpResponse = String(bytes, StandardCharsets.UTF_8)
                Log.i(
                    TCP_DEBUG,
                    "??????????????????????????????" + tcpResponse!!.length + "???TCP ???????????????" + i + "tcpResponse$tcpResponse"
                )
                if (tcpResponse != null && tcpResponse!!.length == 9 && "error msg" == tcpResponse) {
                    return
                }
//                Log.i(
//                    TCP_DEBUG,
//                    "??????????????????????????????" + tcpResponse!!.length + "???TCP ???????????????" + i
//                )
                //  ????????????????????????????????????????????????    =====================================================
                //  ??????????????????????????????
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
                //  ????????????????????????????????????????????????    =====================================================
                //  ????????????????????????????????????
                if (tcpResponse!!.startsWith("{\"type\":\"nearByFeatrueSend\",") && !tcpResponse!!.endsWith(
                        "}"
                    )
                ) {
                    cache = tcpResponse
                    return
                }
                //  ?????? ??? 3?????????????????????
                if (!tcpResponse!!.startsWith("{") /*&& tcpResponse.length() > 200*/ && !tcpResponse!!.endsWith(
                        "}"
                    )
                ) {
                    cache = cache + tcpResponse
                    return
                }
                //  ?????? 60 ??????
                if (!tcpResponse!!.startsWith("{") &&  /*tcpResponse.length() > 200 &&*/tcpResponse!!.endsWith(
                        "}"
                    )
                ) {
                    tcpResponse = cache + tcpResponse
                }
                Log.i(
                    TCP_DEBUG,
                    "?????????????????????????????????????????????" + tcpResponse + ",??????:" + tcpResponse!!.length + "?????????:" + i
                )
                //  ???????????? ????????????????????? type ??? data ????????? { ??????
                if (tcpResponse!!.startsWith("{") && tcpResponse!!.contains("type") && tcpResponse!!.contains(
                        "data"
                    ) && tcpResponse!!.endsWith("}")
                ) {
                    try {
                        val jsonObject = JSONObject(tcpResponse)
                        val type = jsonObject.getString("type")
                        val data = jsonObject.getString("data")

                        //  ????????????
                        if (type == "connect_rz_msg") {
                            viewModel!!.registerTCP(tcp_client_id!!)
                        } else if (type == "client_connect_msgect_msg") {
                            //  ?????????????????? ??? ??????
                            //NetWorkUtil.getInstance().errorUpload("TCP ?????? client_connect_msgect_msg");
                            //  TCP ?????????bug
                            if (System.currentTimeMillis() - lastBindTime < 1000) {
                                Log.i(TCP_DEBUG, "TCP?????????????????????1s???????????????????????????????????????????????????????????????")
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
                            Log.i(TCP_DEBUG, "??????TCP????????????" + gson.toJson(verify))
                            TCPConnectUtil.getInstance().sendData(gson.toJson(verify))
                            val tcpVerify: TCPVerifyResponse =
                                gson.fromJson(data, TCPVerifyResponse::class.java)
                            tcp_client_id = tcpVerify.getClient_id()
                        } else if (type == "buy_success_msg") {
                            //  ?????? ????????????
                            val buySuccessMsg: BuySuccessMsg =
                                gson.fromJson(data, BuySuccessMsg::class.java)
                        } else if (type == "QrReturn") {

                            //  ???????????????????????????????????????
                            if (System.currentTimeMillis() - QRReturnTime < 1000) {
//                                return;
                            }
                            val vxLoginCall: VXLoginCall =
                                gson.fromJson(data, VXLoginCall::class.java)
                            //  ???????????????????????????id
                            DustbinBrainApp.userId = vxLoginCall.info.user_id
                            //  ????????????????????????
                            DustbinBrainApp.userType = vxLoginCall.info.user_type.toLong()
                            //  ?????????????????????

                            //  ????????????????????????????????????????????????????????????
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
                                //  ???????????????????????????????????????????????????????????????????????????????????????????????????
                                if (userMessage != null) {
                                    //  ????????????????????????????????????
                                    mFacePassHandler!!.deleteFace(
                                        userMessage.faceToken.toByteArray()
                                    )
                                    //  ????????????????????????????????????
                                    DataBaseUtil.getInstance(this@MainActivity)
                                        .daoSession?.userMessageDao?.delete(userMessage)
                                    Log.i(DEBUG_TAG, "???????????????????????????????????????")
                                } else {
                                    Log.i(DEBUG_TAG, "?????????????????????????????????")
                                }
                                Log.i(DEBUG_TAG, "???????????? ????????????????????? ??? ????????????")
                                //  ????????????????????????????????? ( ?????????????????? Base64 ?????? )
                                val feature: ByteArray = Base64.decode(
                                    vxLoginCall.getInfo().getFeatrue(),
                                    Base64.DEFAULT
                                )
                                Log.i("???????????????", "??????:" + feature.size)
                                val facePassFeatureAppendInfo = FacePassFeatureAppendInfo()
                                //  ??????????????????????????????faceToken ??????????????????????????????????????????
                                val faceToken = mFacePassHandler!!.insertFeature(
                                    feature,
                                    facePassFeatureAppendInfo
                                )
                                //  faceToken ????????????
                                val bindResult = mFacePassHandler!!.bindGroup(
                                    group_name,
                                    faceToken.toByteArray()
                                )
                                //  ????????????????????? ??? faceToken ??? id ???????????????
                                if (bindResult) {

                                    //  ??????QRReturn??????
                                    QRReturnTime = System.currentTimeMillis()
                                    Log.i(DEBUG_TAG, "?????????????????????????????????")
                                    //  faceToken ?????????id ??????
                                    DataBaseUtil.getInstance(this@MainActivity)
                                        .insertUserIdAndFaceToken(
                                            DustbinBrainApp.userId!!.toLong(),
                                            faceToken
                                        )

                                    //  ???????????????????????????
                                    goControlActivity()
                                } else {
                                    Log.i(DEBUG_TAG, "???????????????????????????")
                                    //  ????????????????????????????????????
                                    mFacePassHandler!!.deleteFace(faceToken.toByteArray())
                                }
                            } else {
                                //   ?????? ?????????????????? ?????? ???????????????????????????????????????
                                Log.i(DEBUG_TAG, "????????????????????????????????????????????????????????????")
//                                mainHandler.post(Runnable { showVerifyFail() })
                            }
                        } else if (type == "GQrReturn") {
                            //  ????????????????????????
                            val gQrReturnBean: GQrReturnBean =
                                gson.fromJson(data, GQrReturnBean::class.java)
                            DustbinBrainApp.userId = gQrReturnBean.info.user_id
                            DustbinBrainApp.userType = gQrReturnBean.info.user_type.toLong()
                            canRecognize = true
                            goControlActivity()
                        } else if (type == "out_time") {//???????????????
                            val gQrReturnBean: GQrReturnBean =
                                gson.fromJson(data, GQrReturnBean::class.java)
                            DustbinBrainApp.userId = gQrReturnBean.info.user_id
                            DustbinBrainApp.userType = gQrReturnBean.info.user_type.toLong()
                            canRecognize = true
                            goNotWorkTimeActivity()
                        } else if (type == "nfcActivity") {
                            val nfcActivityBean: NfcActivityBean =
                                gson.fromJson(data, NfcActivityBean::class.java)
                            //  nfc ????????????
                            if (nfcActivityBean.getData().getCode() === 1) {
                                //  ????????????id
                                DustbinBrainApp.userId =
                                    nfcActivityBean.getData().info.user_id
                                DustbinBrainApp.userType =
                                    nfcActivityBean.getData().info.user_type.toLong()

                                //  ???????????????????????????
                                goControlActivity()
                            }
                        } else if (type == "nearByFeatrueSend") {
                            val nearByFeatrueSendBean: NearByFeatrueSendBean = gson.fromJson(
                                data,
                                NearByFeatrueSendBean::class.java
                            )
                            if (nearByFeatrueSendBean != null && nearByFeatrueSendBean.getFeatrue() != null) {
                                if (nearByFeatrueSendBean.user_id == 1720) {
                                    Log.i("????????????", nearByFeatrueSendBean.toString())
                                }
                                Log.i("????????????", nearByFeatrueSendBean.toString())
                                //  ????????????????????????????????? ( ?????????????????? Base64 ?????? )
                                val feature: ByteArray = Base64.decode(
                                    nearByFeatrueSendBean.getFeatrue(),
                                    Base64.DEFAULT
                                )
                                val facePassFeatureAppendInfo = FacePassFeatureAppendInfo()
                                //  ??????????????????????????????faceToken ??????????????????????????????????????????
                                val faceToken = mFacePassHandler!!.insertFeature(
                                    feature,
                                    facePassFeatureAppendInfo
                                )
                                //  facetoken ????????????
                                val bindResult = mFacePassHandler!!.bindGroup(
                                    group_name,
                                    faceToken.toByteArray()
                                )
                                //  ????????????????????? ??? facetoken ??? id ???????????????
                                if (bindResult) {
                                    //  facetoken ?????????id ??????
                                    DataBaseUtil.getInstance(this@MainActivity)
                                        .insertUserIdAndFaceToken(
                                            nearByFeatrueSendBean.getUser_id().toLong(),
                                            faceToken
                                        )
                                }
                            } else {
                            }
                        } else if (type == "deleteAllFace") {
                            //  ????????????????????????
                            DataBaseUtil.getInstance(this@MainActivity)
                                .getDaoSession()!!.userMessageDao.deleteAll()
                            //  ???????????????
                            mFacePassHandler!!.clearAllGroupsAndFaces()
                            //  ??????
//                            AndroidDeviceSDK.reBoot(this@MainActivity)
                            AndroidDeviceSDK.restartApp(this@MainActivity)
                        } else if (type == "updateAllUserType0") {
                            //  ???????????????????????? ??? 0
                            val userMessageList: List<UserMessage> =
                                DataBaseUtil.getInstance(this@MainActivity)
                                    .getDaoSession()!!.userMessageDao.queryBuilder().list()
                            for (userMessage in userMessageList) {
                                userMessage.setUserType(0)
                            }
                            DataBaseUtil.getInstance(this@MainActivity)
                                .getDaoSession()!!.userMessageDao.updateInTx(userMessageList)
//                            NetWorkUtil.getInstance().errorUpload("?????????????????????????????? 0 ")
                            Log.i("updateAllUserType0", "????????????")
                        } else if (type == "connect_restartApp_msg") {
                            AndroidDeviceSDK.restartApp(this@MainActivity)
                        } else if (type == "updateApp1to1") {
                            if (!TextUtils.isEmpty(data)) {
                                //??????????????????
                                LogUtils.e("????????????:$data")
                                EventBus.getDefault().post(data)
                            }
                        } else if (type.equals("addFaceImage")) {  //  ???????????????????????????????????????
                            Log.i("addFaceImage", "??????????????????")
                            val addFaceImageJsonObject = JsonParser.parseString(data).asJsonObject
                            val userId = addFaceImageJsonObject["userId"].asInt
                            val userType = addFaceImageJsonObject["userType"].asLong
                            val imageUrl = addFaceImageJsonObject["imageUrl"].asString
                            DustbinBrainApp.userId = userId
                            DustbinBrainApp.userType = userType
                            Log.i("addFaceImage", addFaceImageJsonObject.toString())
                            Log.i("addFaceImage", "$userId,$userType,$imageUrl")


                            //  ??????????????????????????????????????????
                            val userMessage: UserMessage? =
                                DataBaseUtil.getInstance(this@MainActivity)
                                    .daoSession?.userMessageDao?.queryBuilder()
                                    ?.where(UserMessageDao.Properties.UserId.eq(userId))?.unique()
                            //  ???????????????????????????????????????????????????????????????????????????????????????????????????
                            if (userMessage != null) {
                                //  ????????????????????????????????????
                                mFacePassHandler?.deleteFace(
                                    userMessage.faceToken.toByteArray()
                                )
                                //  ????????????????????????????????????
                                DataBaseUtil.getInstance(this@MainActivity)
                                    .getDaoSession()?.userMessageDao?.delete(userMessage)
                                Log.i("addFaceImage", "???????????????????????????????????????" + userMessage.getUserId())
                            } else {
                                Log.i("addFaceImage", "?????????????????????????????????$userId")
                            }
                            downloadFaceImage(imageUrl, userId.toLong(), userType.toLong())
                        }
                    } catch (e: java.lang.Exception) {
                        e.printStackTrace()
                        Log.i(TCP_DEBUG, "???????????????" + e.message)
                    }
                } else {
                    Log.i(TCP_DEBUG, "??????????????????????????? TCP ?????????$tcpResponse")
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

    val TAG = "????????????"
    fun goControlActivity() {
        if (!canRecognize) {
            return
        }

        if (binsWorkTimeBean == null) {
            var map: MutableMap<String, String> = mutableMapOf(
                "device_id" to deviceCode.toString()
            )
            viewModel?.getBinsWorkTime(map)
        }
        LogUtils.dTag(
            "goControlActivity",
            "binsWorkTime${binsWorkTimeBean?.getData()?.am_start_time}"
        )
        DustbinBrainApp.hasManTime = System.currentTimeMillis()
        LogUtils.dTag(
            "???????????????",
            "DustbinBrainApp.hasManTime:${TimeUtils.millis2Date(DustbinBrainApp.hasManTime)}"
        )
        //???????????????????????????
        if (DustbinBrainApp.userType?.toInt() == 1) {
            //  ???????????????????????????
            val intent = Intent(this@MainActivity, ControlActivity::class.java)
            intent.putExtra("userId", "" + DustbinBrainApp.userId)
            intent.putExtra("faceImage", faceImagePath)
            startActivityForResult(intent, 300)
        } else {
            //  ???????????????
            if (BinsWorkTimeUntil.getBinsWorkTime(binsWorkTimeBean)) {
                Log.i("MainActivity.TAG", "??????????????? ????????????")
                //  ?????????????????????????????????????????????
                val intent = Intent(
                    this@MainActivity,
                    ControlActivity::class.java
                )
                intent.putExtra("userId", "" + DustbinBrainApp.userId)
                intent.putExtra("faceImage", faceImagePath)
                manager?.release()
                startActivityForResult(intent, 300)
            } else {
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "???????????????", Toast.LENGTH_LONG).show()
                    VoiceUtil.getInstance().openAssetMusics(this@MainActivity, "no_work_time.aac")
                }
                //  ???????????????
                val intent = Intent(
                    this@MainActivity,
                    NotWorkTimeActivity::class.java
                )
                intent.putExtra("userId", "" + DustbinBrainApp.userId)
                intent.putExtra("faceImage", faceImagePath)
                manager?.release()
                startActivityForResult(intent, 301)
            }
        }

    }

    fun goNotWorkTimeActivity() {
        if (!canRecognize) {
            return
        }
        if (binsWorkTimeBean == null) {
            var map: MutableMap<String, String> = mutableMapOf(
                "device_id" to deviceCode.toString()
            )
            viewModel?.getBinsWorkTime(map)
        }
        LogUtils.dTag(
            "goControlActivity",
            "binsWorkTime${binsWorkTimeBean?.getData()?.am_start_time}"
        )
        DustbinBrainApp.hasManTime = System.currentTimeMillis()
        //  ???????????????
        val intent = Intent(
            this@MainActivity,
            NotWorkTimeActivity::class.java
        )
        intent.putExtra("userId", "" + DustbinBrainApp.userId)
        intent.putExtra("faceImage", faceImagePath)
        manager?.release()
        startActivityForResult(intent, 300)
    }

    /**
     * ????????????,????????????????????????
     */
    private var alertDialog: AlertDialog? = null


    //  H5 ???????????? ?????????????????????
    private var mUri: Uri? = null

    //  ????????????
    private var file: File? = null

    private var nowFaceToken: String = ""

    /**
     * ??????????????????
     */
    @Synchronized
    @Throws(java.lang.Exception::class)
    private fun downloadFaceImage(imageUrl: String, userId: Long, userType: Long) {
        Log.i("addFaceImage", "????????????????????????")
        //  ??????????????????
        DownloadUtil.get().download(
            imageUrl,
            Environment.getExternalStorageDirectory().toString(),
            System.currentTimeMillis().toString() + ".jpg",
            object : DownloadUtil.OnDownloadListener {
                override fun onDownloadSuccess(file: File) {
                    Log.i("addFaceImage", "????????????")
                    try {


                        //  ?????????????????????
                        val facePassExtractFeatureResult =
                            mFacePassHandler?.extractFeature(BitmapFactory.decodeFile(file.absolutePath))
                        //  ?????????????????????
                        if (facePassExtractFeatureResult?.result == 0) {
                            val facePassFeatureAppendInfo = FacePassFeatureAppendInfo()

                            //  ?????? faceToken
                            val faceToken = mFacePassHandler?.insertFeature(
                                facePassExtractFeatureResult.featureData,
                                facePassFeatureAppendInfo
                            )
                            if (faceToken != null) {
                                nowFaceToken = faceToken
                            }
                            val bindResult =
                                mFacePassHandler?.bindGroup(group_name, faceToken?.toByteArray())

                            //  ????????????
                            if (bindResult == true) {
                                Log.i("addFaceImage", "$userId,????????????$faceToken")
                                //  ????????????
                                DataBaseUtil.getInstance(this@MainActivity)
                                    .insertUserIdAndFaceTokenThread(userId, userType, nowFaceToken)
                                val deviceId: String? = mmkv?.decodeString(MMKVCommon.DEVICE_ID)
                                val hasMap: MutableMap<String, String> = mutableMapOf(
                                    "user_id" to userId.toString(),
                                    "device_id" to deviceId!!
                                )
                                //  ??????????????????
                                viewModel?.postFaceRegisterSuccessLog(hasMap)
                            } else {
                                mFacePassHandler?.deleteFace(faceToken?.toByteArray())
                                Log.i("addFaceImage", "$userId,????????????")
                            }
                        } else {
                            Handler().post(Runnable {
                                Toast.makeText(this@MainActivity, "???????????????", Toast.LENGTH_SHORT)
                                    .show()
                                file.delete()
                            })
                        }
                    } catch (e: java.lang.Exception) {
                        Log.i("addFaceImage", e.message!!)
                    } finally {
                        //  ????????????
                        //  file.delete();
                    }
                }

                override fun onDownloading(progress: Int) {}
                override fun onDownloadFailed(e: java.lang.Exception) {
                    Log.i("addFaceImage", "????????????" + e.message)
                }
            })
    }

    var scanKeyManager: ScanKeyManager? = null
    override fun dispatchKeyEvent(event: KeyEvent?): Boolean {
        LogUtils.e("event keyCode:${event?.keyCode}")
        if (event?.keyCode != KeyEvent.KEYCODE_BACK) {
            scanKeyManager?.analysisKeyEvent(event)
            return true
        }
        return super.dispatchKeyEvent(event)
    }
}
