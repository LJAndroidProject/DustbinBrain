package com.ffst.dustbinbrain.kotlin_mvp.mvp.main.camera

import mcv.facepass.types.FacePassImageRotation

/**
 * Created by LiuJW
 *on 2021/6/17
 */
class SettingVar {
    companion object{
        var cameraFacingFront = false
        var faceRotation = FacePassImageRotation.DEG270
        var isSettingAvailable = true
        var cameraPreviewRotation = 90
        var isCross = false
        var SharedPrefrence = "SettingVar.user"
        var mHeight = 0
        var mWidth = 0
        var cameraSettingOk = false
        var iscameraNeedConfig = false
        var isButtonInvisible = false
    }
}