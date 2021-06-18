package com.ffst.dustbinbrain.kotlin_mvp.mvp.main.view

import android.app.AlertDialog
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.RectF
import android.os.Environment
import android.os.Handler
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
import androidx.lifecycle.ViewModelProvider
import com.android.volley.toolbox.ImageLoader
import com.blankj.utilcode.util.LogUtils
import com.ffst.annotation.StatusBar
import com.ffst.dustbinbrain.kotlin_mvp.R
import com.ffst.dustbinbrain.kotlin_mvp.app.KotlinMvpApp
import com.ffst.dustbinbrain.kotlin_mvp.bean.*
import com.ffst.dustbinbrain.kotlin_mvp.manager.ThreadManager
import com.ffst.dustbinbrain.kotlin_mvp.mvp.main.camera.CameraManager
import com.ffst.dustbinbrain.kotlin_mvp.mvp.main.camera.CameraPreviewData
import com.ffst.dustbinbrain.kotlin_mvp.mvp.main.camera.SettingVar
import com.ffst.dustbinbrain.kotlin_mvp.mvp.main.viewmodel.MainActivityViewModel
import com.ffst.dustbinbrain.kotlin_mvp.mvp.main.widget.CicleViewOutlineProvider
import com.ffst.dustbinbrain.kotlin_mvp.mvp.main.widget.FaceView
import com.ffst.dustbinbrain.kotlin_mvp.utils.DataBaseUtil
import com.ffst.dustbinbrain.kotlin_mvp.utils.DownloadUtil
import com.ffst.dustbinbrain.kotlin_mvp.utils.FenFenCommonUtil
import com.ffst.dustbinbrain.kotlin_mvp.utils.TCPConnectUtil
import com.ffst.mvp.base.activity.BaseActivity
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.littlegreens.netty.client.listener.NettyClientListener
import com.tencent.mmkv.MMKV
import kotlinx.android.synthetic.main.activity_main.*
import mcv.facepass.FacePassException
import mcv.facepass.FacePassHandler
import mcv.facepass.types.*
import okhttp3.Call
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.util.*
import java.util.concurrent.ArrayBlockingQueue


@StatusBar(enabled = true)
class MainActivity : BaseActivity(), CameraManager.CameraListener {

    companion object {
        val SDK_MODE: FacePassHandler.FacePassSDKMode = FacePassHandler.FacePassSDKMode.MODE_OFFLINE
        const val DEBUG_TAG: String = FenFenCommonUtil.FACE_TAG
        val group_name = FenFenCommonUtil.face_group_name
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
    class RecognizeData {
        var message: ByteArray
        lateinit var trackOpt: Array<FacePassTrackOptions?>

        constructor(message: ByteArray) {
            this.message = message
        }

        constructor(message: ByteArray, opt: Array<FacePassTrackOptions?>) {
            this.message = message
            trackOpt = opt
        }
    }

    var mRecognizeDataQueue: ArrayBlockingQueue<RecognizeData>? =
        null
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

    /*图片缓存*/

    private var mAndroidHandler: Handler? = null

    private val gson = Gson()

    override fun layoutId(): Int {
        return R.layout.activity_main
    }

    override fun initViewData() {
        viewModel = ViewModelProvider(this)[MainActivityViewModel::class.java]
        mmkv = MMKV.defaultMMKV()
        mImageCache = FaceImageCache()
        mRecognizeDataQueue = ArrayBlockingQueue(5)
        mFeedFrameQueue = ArrayBlockingQueue(1)
        initAndroidHandler()
        /* 初始化界面 */
        initView()
        bindData()
        initTCP()
        //获取投放时间
        viewModel!!.getBinsWorkTime()

        //初始化人脸SDK
        initFaceSDK()

        //初始化人脸识别配置
        initFaceHandler()
        mRecognizeThread = RecognizeThread()
        mRecognizeThread!!.start()
        mFeedFrameThread = FeedFrameThread()
        mFeedFrameThread!!.start()


    }

    fun addBindTest() {
        val facePath: String =
            Environment.getExternalStorageDirectory().toString() + "/testface.jpg"
        val bitmap = BitmapFactory.decodeFile(facePath)
        try {
            val result = mFacePassHandler!!.addFace(bitmap)
            if (result != null) {
                if (result.result == 0) {
                    LogUtils.d("add face successfully！")
                    val faceToken: ByteArray = result.faceToken
                    val back = mFacePassHandler!!.bindGroup(group_name, faceToken);
                    val sss = if (back) "success " else "failed"
                    LogUtils.d("bind  $sss")
                } else if (result.result == 1) {
                    LogUtils.d("no face ！")
                } else {
                    LogUtils.d("quality problem！")
                }
            }
        } catch (e: FacePassException) {
            e.printStackTrace()
            toast(e.message!!)
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
    }

    override fun onResume() {
        super.onResume()
        manager!!.open(windowManager, false, cameraWidth, cameraHeight)
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
                            "liveness.CPU.rgb.int8.E.bin"
                        )
                        //当单目或者双目有一个使用GPU活体模型时，请设置livenessGPUCache
                        //config.livenessGPUCache = FacePassModel.initModel(getApplicationContext().getAssets(), "liveness.GPU.AlgoPolicy.E.cache");
                        config.searchModel = FacePassModel.initModel(
                            applicationContext.assets,
                            "feat2.arm.H.v1.0_1core.bin"
                        )
                        config.detectModel =
                            FacePassModel.initModel(applicationContext.assets, "detector.arm.E.bin")
                        config.detectRectModel = FacePassModel.initModel(
                            applicationContext.assets,
                            "detector_rect.arm.E.bin"
                        )
                        config.landmarkModel =
                            FacePassModel.initModel(applicationContext.assets, "pf.lmk.arm.D.bin")
                        config.rcAttributeModel = FacePassModel.initModel(
                            applicationContext.assets,
                            "attr.RC.gray.12M.arm.200229.bin"
                        )
                        config.rcAttributeEnabled = true
                        config.searchThreshold = 65f
                        config.livenessThreshold = 65f
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
                        config.fileRootPath =
                            getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)!!.absolutePath
                        /* 创建SDK实例 */
                        mFacePassHandler = FacePassHandler(config)
                        val addFaceConfig: FacePassConfig = mFacePassHandler!!.getAddFaceConfig()
                        addFaceConfig.poseThreshold.pitch = 20f
                        addFaceConfig.poseThreshold.roll = 20f
                        addFaceConfig.poseThreshold.yaw = 20f
                        addFaceConfig.blurThreshold = 0.6f
                        addFaceConfig.faceMinThreshold = 100
                        mFacePassHandler!!.setAddFaceConfig(addFaceConfig)
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
        addBindTest()
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
        Log.i(FenFenCommonUtil.FACE_TAG, "feedframe")
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
            while (!isInterrupt) {
                var cameraPreviewData: CameraPreviewData? = null
                cameraPreviewData = try {
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
                var image: FacePassImage
                image = try {
                    FacePassImage(
                        cameraPreviewData!!.nv21Data,
                        cameraPreviewData!!.width,
                        cameraPreviewData!!.height,
                        cameraRotation,
                        FacePassImageType.NV21
                    )
                } catch (e: FacePassException) {
                    e.printStackTrace()
                    continue
                }

                /* 将每一帧FacePassImage 送入SDK算法， 并得到返回结果 */
                var detectionResult: FacePassDetectionResult? = null
                try {
                    detectionResult = mFacePassHandler!!.feedFrame(image)
                } catch (e: FacePassException) {
                    e.printStackTrace()
                }
                if (detectionResult == null || detectionResult.faceList.size == 0) {
                    /* 当前帧没有检出人脸 */
                    runOnUiThread(Runnable {
                        fcview!!.clear()
                        fcview.invalidate()
                    })
                } else {
                    /* 将识别到的人脸在预览界面中圈出，并在上方显示人脸位置及角度信息 */
                    val bufferFaceList = detectionResult.faceList
                    runOnUiThread(Runnable { showFacePassFace(bufferFaceList) })
                }
                if (SDK_MODE == FacePassHandler.FacePassSDKMode.MODE_OFFLINE) {
                    /*离线模式，将识别到人脸的，message不为空的result添加到处理队列中*/
                    if (detectionResult != null && detectionResult.message.size != 0) {
                        Log.d(MainActivity.DEBUG_TAG, "mRecognizeDataQueue.offer")
                        /*所有检测到的人脸框的属性信息*/for (i in detectionResult.faceList.indices) {
                            Log.d(
                                MainActivity.DEBUG_TAG, String.format(
                                    "rc attribute faceList hairType: 0x%x beardType: 0x%x hatType: 0x%x respiratorType: 0x%x glassesType: 0x%x skinColorType: 0x%x",
                                    detectionResult.faceList[i].rcAttr.hairType.ordinal,
                                    detectionResult.faceList[i].rcAttr.beardType.ordinal,
                                    detectionResult.faceList[i].rcAttr.hatType.ordinal,
                                    detectionResult.faceList[i].rcAttr.respiratorType.ordinal,
                                    detectionResult.faceList[i].rcAttr.glassesType.ordinal,
                                    detectionResult.faceList[i].rcAttr.skinColorType.ordinal
                                )
                            )
                        }
                        Log.d(
                            DEBUG_TAG,
                            "--------------------------------------------------------------------------------------------------------------------------------------------------"
                        )
                        /*送识别的人脸框的属性信息*/
                        val trackOpts =
                            arrayOfNulls<FacePassTrackOptions>(detectionResult.images.size)
                        for (i in detectionResult.images.indices) {
                            if (detectionResult.images[i].rcAttr.respiratorType != FacePassRCAttribute.FacePassRespiratorType.INVALID
                                && detectionResult.images[i].rcAttr.respiratorType != FacePassRCAttribute.FacePassRespiratorType.NO_RESPIRATOR
                            ) {
                                val searchThreshold = 60f
                                val livenessThreshold =
                                    -1.0f // -1.0f will not change the liveness threshold
                                trackOpts[i] = FacePassTrackOptions(
                                    detectionResult.images[i].trackId,
                                    searchThreshold,
                                    livenessThreshold
                                )
                            }
                            Log.d(
                                MainActivity.DEBUG_TAG, String.format(
                                    "rc attribute in FacePassImage, hairType: 0x%x beardType: 0x%x hatType: 0x%x respiratorType: 0x%x glassesType: 0x%x skinColorType: 0x%x",
                                    detectionResult.images[i].rcAttr.hairType.ordinal,
                                    detectionResult.images[i].rcAttr.beardType.ordinal,
                                    detectionResult.images[i].rcAttr.hatType.ordinal,
                                    detectionResult.images[i].rcAttr.respiratorType.ordinal,
                                    detectionResult.images[i].rcAttr.glassesType.ordinal,
                                    detectionResult.images[i].rcAttr.skinColorType.ordinal
                                )
                            )
                        }
                        val mRecData = RecognizeData(detectionResult.message, trackOpts)
                        mRecognizeDataQueue!!.offer(mRecData)
                    }
                }
                val endTime = System.currentTimeMillis() //结束时间
                val runTime = endTime - startTime
                for (i in detectionResult!!.faceList.indices) {
                    Log.i(
                        "DEBUG_TAG",
                        "rect[" + i + "] = (" + detectionResult.faceList[i].rect.left + ", " + detectionResult.faceList[i].rect.top + ", " + detectionResult.faceList[i].rect.right + ", " + detectionResult.faceList[i].rect.bottom
                    )
                }
                Log.i("]time", String.format("feedfream %d ms", runTime))
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
                    val recognizeData: RecognizeData = mRecognizeDataQueue!!.take()
                    val ageGenderResult: Array<FacePassAgeGenderResult>? = null
                    if (isLocalGroupExist) {
                        Log.d(DEBUG_TAG, "RecognizeData >>>>")
                        val recognizeResultArray: Array<Array<FacePassRecognitionResult>> =
                            mFacePassHandler!!.recognize(
                                group_name,
                                recognizeData.message,
                                1,
                                recognizeData.trackOpt
                            )
                        if (recognizeResultArray != null && recognizeResultArray.size > 0) {
                            for (recognizeResult in recognizeResultArray) {
                                if (recognizeResult != null && recognizeResult.size > 0) {
                                    for (result in recognizeResult) {
                                        val faceToken = String(result.faceToken)
                                        if (FacePassRecognitionState.RECOGNITION_PASS == result.recognitionState) {
                                            getFaceImageByFaceToken(result.trackId, faceToken)
                                        }
                                        val idx: Int = findidx(ageGenderResult, result.trackId)
                                        if (idx == -1) {
                                            showRecognizeResult(
                                                result.trackId,
                                                result.detail.searchScore,
                                                result.detail.livenessScore,
                                                !TextUtils.isEmpty(faceToken)
                                            )
                                        }
                                        Log.d(
                                            DEBUG_TAG, String.format(
                                                "recognize trackid: %d, searchScore: %f  searchThreshold: %f, hairType: 0x%x beardType: 0x%x hatType: 0x%x respiratorType: 0x%x glassesType: 0x%x skinColorType: 0x%x",
                                                result.trackId,
                                                result.detail.searchScore,
                                                result.detail.searchThreshold,
                                                result.detail.rcAttr.hairType.ordinal,
                                                result.detail.rcAttr.beardType.ordinal,
                                                result.detail.rcAttr.hatType.ordinal,
                                                result.detail.rcAttr.respiratorType.ordinal,
                                                result.detail.rcAttr.glassesType.ordinal,
                                                result.detail.rcAttr.skinColorType.ordinal
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                } catch (e: FacePassException) {
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
        idTextView.text = text
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
        isRecognizeOK: Boolean
    ) {
        mAndroidHandler!!.post {
            faceEndTextView.append(
                """
                ID = $trackId${if (isRecognizeOK) "识别成功" else "识别失败"}

                """.trimIndent()
            )
            faceEndTextView.append("识别分 = $searchScore\n")
            faceEndTextView.append("活体分 = $livenessScore\n")
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
    fun initTCP() {
        TCPConnectUtil.getInstance().connect()
        TCPConnectUtil.getInstance().setListener(object : NettyClientListener<Any?> {
            override fun onMessageResponseClient(bytes: ByteArray, i: Int) {
                //  来自服务器的响应
                tcpResponse = String(bytes, StandardCharsets.UTF_8)

                if (tcpResponse != null && tcpResponse!!.length == 9 && "error msg" == tcpResponse) {
                    return
                }
                Log.i(
                    TCP_DEBUG,
                    "服务器推送内容长度：" + tcpResponse!!.length + "，TCP 当前状态：" + i
                )
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
                //  =======================================================================================

                //  分段异常
                /*if(tcpResponse.endsWith("\"megvii_android.util.Base64\"}}}") && tcpResponse.length() < 200){
                    tcpResponse = cache + tcpResponse;
                    */
                /*mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this,"出错了，请重新扫码二维码",Toast.LENGTH_LONG).show();
                            showToast("出错了，请重新扫描二维码",Toast.LENGTH_LONG,false,null);
                        }
                    });*/
                /*
                }*/Log.i(
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
                            KotlinMvpApp.userId = vxLoginCall.getInfo().getUser_id()
                            //  修改当前用户类型
                            KotlinMvpApp.userType = vxLoginCall.getInfo().getUser_type()
                            //  隐藏二维码扫码

                            //  云端有该人的人脸特征，则将特征保存到本地
                            if (vxLoginCall.isFeatrue_state() && vxLoginCall.isFace_image_state()) {
                                val userMessage: UserMessage =
                                    DataBaseUtil.getInstance(this@MainActivity)
                                        .getDaoSession()!!.getUserMessageDao().queryBuilder()
                                        .where(
                                            UserMessageDao.Properties.UserId.eq(
                                                vxLoginCall.getInfo().getUser_id()
                                            )
                                        )
                                        .unique()
                                //  本地已经有这个人脸特征了，则删除掉原有的人脸特征，添加新的人脸特征
                                if (userMessage != null) {
                                    //  人脸库中删除这个人脸特征
                                    mFacePassHandler!!.deleteFace(
                                        userMessage.getFaceToken().toByteArray()
                                    )
                                    //  本地数据库中删除这个用户
                                    DataBaseUtil.getInstance(this@MainActivity).getDaoSession()!!.userMessageDao.delete(userMessage)
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
                                        .insertUserIdAndFaceToken(KotlinMvpApp.userId!!.toLong(), faceToken)

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
                            KotlinMvpApp.userId = gQrReturnBean.getInfo().getUser_id()
                            KotlinMvpApp.userType = gQrReturnBean.getInfo().getUser_type()
                            goControlActivity()
                        } else if (type == "nfcActivity") {
                            val nfcActivityBean: NfcActivityBean =
                                gson.fromJson(data, NfcActivityBean::class.java)
                            //  nfc 绑定成功
                            if (nfcActivityBean.getData().getCode() === 1) {
                                //  设置用户id
                                KotlinMvpApp.userId = nfcActivityBean.getData().getInfo().getUser_id()
                                KotlinMvpApp.userType = nfcActivityBean.getData().getInfo().getUser_type()

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
                            DataBaseUtil.getInstance(this@MainActivity).getDaoSession()!!.userMessageDao.deleteAll()
                            //  删除人脸库
                            mFacePassHandler!!.clearAllGroupsAndFaces()
                            //  重启
//                            AndroidDeviceSDK.reBoot(this@MainActivity)
                        } else if (type == "updateAllUserType0") {
                            //  修改所有用户类型 为 0
                            val userMessageList: List<UserMessage> = DataBaseUtil.getInstance(this@MainActivity).getDaoSession()!!.userMessageDao.queryBuilder().list()
                            for (userMessage in userMessageList) {
                                userMessage.setUserType(0)
                            }
                            DataBaseUtil.getInstance(this@MainActivity).getDaoSession()!!.userMessageDao.updateInTx(userMessageList)
//                            NetWorkUtil.getInstance().errorUpload("用户类型已全部修改为 0 ")
                            Log.i("updateAllUserType0", "修改之前")
                        } else if (type == "reboot") {
//                            AndroidDeviceSDK.reBoot(this@MainActivity)
                        } else if ( /*type.equals("addFaceImage")*/false) {  //  添加人脸，人脸在服务器注册
                            Log.i("addFaceImage", "进入添加人脸")
                            val addFaceImageJsonObject = JsonParser.parseString(data).asJsonObject
                            val userId = addFaceImageJsonObject["userId"].asInt
                            val userType = addFaceImageJsonObject["userType"].asInt
                            val imageUrl = addFaceImageJsonObject["imageUrl"].asString
                            KotlinMvpApp.userId = userId
                            KotlinMvpApp.userType = userType
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
                                mFacePassHandler!!.deleteFace(userMessage.getFaceToken().toByteArray())
                                //  本地数据库中删除这个用户
                                DataBaseUtil.getInstance(this@MainActivity).getDaoSession()!!.userMessageDao.delete(userMessage)
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

            }
        })
    }

    fun goControlActivity(){

    }

    private var nowFaceToken: String? = null
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
            object : DownloadUtil.OnDownloadListener{
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
                                val deviceId: String? = mmkv!!.decodeString("device_id")
                                val hasMap :MutableMap<String,String> = mutableMapOf(
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
