package com.ffst.dustbinbrain.kotlin_mvp.mvp.main.widget

import android.graphics.Outline
import android.graphics.Rect
import android.os.Parcel
import android.os.Parcelable
import android.view.View
import android.view.ViewOutlineProvider

/**
 * Created by LiuJW
 *on 2021/6/17
 */
class CicleViewOutlineProvider() : ViewOutlineProvider(){
    private var dimater = 0

    /**
     * 在这个矩形中央画一个以diameter为直径的圆
     * @param diameter 直径
     */
    constructor(dimater:Int) : this() {
        this.dimater = dimater
    }

    private var mRect: Rect? = null


    fun getInnerRect(): Rect? {
        return mRect
    }
    override fun getOutline(view: View?, outline: Outline?) {
        val top: Int = (view!!.height - dimater) / 2
        val left: Int = (view.width - dimater) / 2
        val right: Int = left + dimater
        val bottom: Int = top + dimater
        mRect = Rect(left, top, right, bottom)
        outline!!.setOval(left, top, right, bottom)
    }
}