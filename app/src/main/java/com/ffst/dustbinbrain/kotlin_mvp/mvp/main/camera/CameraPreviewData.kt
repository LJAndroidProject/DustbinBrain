package com.ffst.dustbinbrain.kotlin_mvp.mvp.main.camera

/**
 * Created by LiuJW
 *on 2021/6/17
 */
class CameraPreviewData(
    nv21Data: ByteArray?,
    var width: Int,
    var height: Int,
    var rotation: Int,
    var mirror: Boolean
) {
    var nv21Data: ByteArray = nv21Data!!

}