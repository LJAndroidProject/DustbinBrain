package com.ffst.dustbinbrain.kotlin_mvp.mvp.main.camera

import android.graphics.ImageFormat
import android.graphics.PixelFormat
import android.hardware.Camera
import android.os.AsyncTask
import android.util.Log
import android.view.Surface
import android.view.SurfaceHolder
import android.view.WindowManager
import java.util.*

/**
 * Created by LiuJW
 *on 2021/6/17
 */
class CameraManager : CameraPreview.CameraPreviewListener {
    protected var front = false
    protected var camera: Camera? = null
    protected var cameraId = -1
    protected var surfaceHolder: SurfaceHolder? = null
    private var listener: CameraListener? = null
    private var cameraPreview: CameraPreview? = null
    private var state = CameraState.IDEL
    private val previewDegreen = 0
    private var manualWidth = 0
    private var manualHeight = 0
    private var previewSize: Camera.Size? = null
    private var mPicBuffer: ByteArray? = null
    private fun isSupportedPreviewSize(width: Int, height: Int, mCamera: Camera): Boolean {
        val camPara = mCamera.parameters
        val allSupportedSize = camPara.supportedPreviewSizes
        for (tmpSize in allSupportedSize) {
            Log.i("metrics", "support height" + tmpSize.height + "width " + tmpSize.width)
            if (tmpSize.height == height && tmpSize.width == width) return true
        }
        return false
    }

    fun getCameraWidth(): Int {
        return manualWidth
    }

    fun getCameraheight(): Int {
        return manualHeight
    }

    fun open(windowManager: WindowManager): Boolean {
        return if (state != CameraState.OPENING) {
            state = CameraState.OPENING
            release()
            object : AsyncTask<Any?, Any?, Any?>() {
                override fun onPostExecute(result: Any?) {
                    super.onPostExecute(result)
                    cameraPreview!!.setCamera(camera)
                    state = CameraState.OPENED
                }
                override fun doInBackground(vararg params: Any?): Any? {
                    cameraId =
                        if (front) Camera.CameraInfo.CAMERA_FACING_FRONT else Camera.CameraInfo.CAMERA_FACING_BACK
                    try {
                        camera = Camera.open(cameraId)
                    } catch (e: Exception) {
                        val cameraInfo = Camera.CameraInfo()
                        val count = Camera.getNumberOfCameras()
                        if (count > 0) {
                            cameraId = 0
                            camera = Camera.open(cameraId)
                        } else {
                            cameraId = -1
                            camera = null
                        }
                    }
                    if (camera != null) {
                        val info = Camera.CameraInfo()
                        Camera.getCameraInfo(cameraId, info)
                        val rotation = windowManager.defaultDisplay.rotation
                        var degrees = 0
                        when (rotation) {
                            Surface.ROTATION_0 -> degrees = 0
                            Surface.ROTATION_90 -> degrees = 90
                            Surface.ROTATION_180 -> degrees = 180
                            Surface.ROTATION_270 -> degrees = 270
                        }
                        var previewRotation: Int
                        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                            previewRotation = (info.orientation + degrees) % 360
                            previewRotation = (360 - previewRotation) % 360 // compensate the mirror
                        } else {  // back-facing
                            previewRotation = (info.orientation - degrees + 360) % 360
                        }
                        previewRotation = 90
                        if (SettingVar.isSettingAvailable) {
                            previewRotation = SettingVar.cameraPreviewRotation
                        }
                        Log.i(
                            "CameraManager",
                            String.format(
                                "camera rotation: %d %d %d",
                                degrees,
                                info.orientation,
                                previewRotation
                            )
                        )
                        camera!!.setDisplayOrientation(previewRotation)
                        val param = camera!!.parameters
                        if (manualHeight > 0 && manualWidth > 0 && isSupportedPreviewSize(
                                manualWidth, manualHeight,
                                camera!!
                            )
                        ) {
                            param.setPreviewSize(manualWidth, manualHeight)
                        } else {
                            val bestPreviewSize = getBestPreviewSize(
                                camera!!
                            )
                            Log.i(
                                "metrics",
                                "best height is" + bestPreviewSize!!.height + "width is " + bestPreviewSize.width
                            )
                            manualWidth = bestPreviewSize.width
                            manualHeight = bestPreviewSize.height
                            param.setPreviewSize(bestPreviewSize.width, bestPreviewSize.height)
                            SettingVar.iscameraNeedConfig = true
                            Log.i(
                                "cameraManager",
                                "camerawidth : " + bestPreviewSize.width + "  height  : " + bestPreviewSize.height
                            )
                        }
                        SettingVar.cameraSettingOk = true
                        param.previewFormat = ImageFormat.NV21
                        camera!!.parameters = param
                        val pixelinfo = PixelFormat()
                        val pixelformat = camera!!.parameters.previewFormat
                        PixelFormat.getPixelFormatInfo(pixelformat, pixelinfo)
                        val parameters = camera!!.parameters
                        val sz = parameters.previewSize
                        Log.i(
                            "cameraManager",
                            "camerawidth : " + sz.width + "  height  : " + sz.height
                        )
                        val bufSize = sz.width * sz.height * pixelinfo.bitsPerPixel / 8
                        if (mPicBuffer == null || mPicBuffer!!.size != bufSize) {
                            mPicBuffer = ByteArray(bufSize)
                        }
                        camera!!.addCallbackBuffer(mPicBuffer)
                        previewSize = sz
                    }
                    return null
                }
            }.execute()
            true
        } else {
            false
        }
    }

    fun open(windowManager: WindowManager, front: Boolean): Boolean {
        if (state == CameraState.OPENING) {
            return false
        }
        this.front = front
        return open(windowManager)
    }

    fun open(windowManager: WindowManager, front: Boolean, width: Int, height: Int): Boolean {
        if (state == CameraState.OPENING) {
            return false
        }
        manualHeight = height
        manualWidth = width
        this.front = front
        return open(windowManager)
    }

    fun release() {
        if (camera != null) {
            cameraPreview!!.setCamera(null)
            camera!!.stopPreview()
            camera!!.setPreviewCallback(null)
            camera!!.release()
            camera = null
        }
    }

    fun finalRelease() {
        listener = null
        cameraPreview = null
        surfaceHolder = null
    }

    fun setPreviewDisplay(preview: CameraPreview) {
        cameraPreview = preview
        surfaceHolder = preview.holder
        preview.setListener(this)
    }

    fun setListener(listener: CameraListener?) {
        this.listener = listener
    }

    override fun onStartPreview() {
        camera!!.setPreviewCallbackWithBuffer { data, camera ->
            if (listener != null) {
                listener!!.onPictureTaken(
                    CameraPreviewData(
                        data, previewSize!!.width, previewSize!!.height,
                        previewDegreen, front
                    )
                )
            }
            camera.addCallbackBuffer(data)
        }
    }

    enum class CameraState {
        IDEL, OPENING, OPENED
    }

    interface CameraListener {
        fun onPictureTaken(cameraPreviewData: CameraPreviewData?)
    }

    companion object {
        private fun getBestPreviewSize(mCamera: Camera): Camera.Size? {
            val camPara = mCamera.parameters
            val allSupportedSize = camPara.supportedPreviewSizes
            val widthLargerSize = ArrayList<Camera.Size?>()
            var max = Int.MIN_VALUE
            var maxSize: Camera.Size? = null
            for (tmpSize in allSupportedSize) {
                val multi = tmpSize.height * tmpSize.width
                if (multi > max) {
                    max = multi
                    maxSize = tmpSize
                }
                //选分辨率比较高的
                if (tmpSize.width > tmpSize.height && (tmpSize.width > SettingVar.mHeight / 2 || tmpSize.height > SettingVar.mWidth / 2)) {
                    widthLargerSize.add(tmpSize)
                }
            }
            if (widthLargerSize.isEmpty()) {
                widthLargerSize.add(maxSize)
            }
            val propotion =
                if (SettingVar.mWidth >= SettingVar.mHeight) SettingVar.mWidth.toFloat() / SettingVar.mHeight.toFloat() else SettingVar.mHeight.toFloat() / SettingVar.mWidth.toFloat()
            Collections.sort(widthLargerSize, object : Comparator<Camera.Size?> {
                override fun compare(lhs: Camera.Size?, rhs: Camera.Size?): Int {
                    //                                int off_one = Math.abs(lhs.width * lhs.height - Screen.mWidth * Screen.mHeight);
                    //                                int off_two = Math.abs(rhs.width * rhs.height - Screen.mWidth * Screen.mHeight);
                    //                                return off_one - off_two;
                    //选预览比例跟屏幕比例比较接近的
                    val a = getPropotionDiff(lhs, propotion)
                    val b = getPropotionDiff(rhs, propotion)
                    return ((a - b) * 10000).toInt()
                }
            })
            val minPropotionDiff = getPropotionDiff(widthLargerSize[0], propotion)
            val validSizes = ArrayList<Camera.Size?>()
            for (i in widthLargerSize.indices) {
                val size = widthLargerSize[i]
                val propotionDiff = getPropotionDiff(size, propotion)
                if (propotionDiff > minPropotionDiff) {
                    break
                }
                validSizes.add(size)
            }
            Collections.sort(validSizes, object : Comparator<Camera.Size?> {
                override fun compare(lhs: Camera.Size?, rhs: Camera.Size?): Int {
                    if (rhs != null && lhs != null) {
                        return rhs.width * rhs.height - lhs.width * lhs.height
                    }
                    return 0
                }
            })
            return widthLargerSize[0]
        }

        fun getPropotionDiff(size: Camera.Size?, standardPropotion: Float): Float {
            return Math.abs(size!!.width.toFloat() / size.height.toFloat() - standardPropotion)
        }
    }
}