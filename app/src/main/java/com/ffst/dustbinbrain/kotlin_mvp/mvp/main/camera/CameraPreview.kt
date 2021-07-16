package com.ffst.dustbinbrain.kotlin_mvp.mvp.main.camera

import android.content.Context
import android.hardware.Camera
import android.util.AttributeSet
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.blankj.utilcode.util.LogUtils
import com.ffst.dustbinbrain.kotlin_mvp.mvp.main.widget.FaceView

/**
 * Created by LiuJW
 *on 2021/6/17
 */
class CameraPreview : SurfaceView, SurfaceHolder.Callback {
    private var camera: Camera? = null
    private var listener: CameraPreviewListener? = null
    private var scaleW = 1f
    private var scaleH = 1f

    constructor(context: Context?) : super(context) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init()
    }

    private fun init() {
        val holder = holder
        holder.addCallback(this)
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS)
    }

    fun setCamera(camera: Camera?) {
        this.camera = camera
        restartPreview(holder)
    }

    private fun restartPreview(holder: SurfaceHolder) {
        if (camera != null) {
            if (holder.surface == null) {
                return
            }
            try {
                camera!!.stopPreview()
            } catch (e: Exception) {
            }
            try {
                camera!!.setPreviewDisplay(holder)
                camera!!.startPreview()
                //                camera.startFaceDetection();
                if (listener != null) {
                    listener!!.onStartPreview()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun setScale(scaleW: Float, scaleH: Float) {
        this.scaleW = scaleW
        this.scaleH = scaleH
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        restartPreview(holder)
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, w: Int, h: Int) {
        restartPreview(holder)
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
    }

    fun setListener(listener: CameraPreviewListener?) {
        this.listener = listener
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = MeasureSpec.getSize(heightMeasureSpec)
        setMeasuredDimension((width * scaleW).toInt(), (height * scaleH).toInt())
    }

    interface CameraPreviewListener {
        fun onStartPreview()
    }
}