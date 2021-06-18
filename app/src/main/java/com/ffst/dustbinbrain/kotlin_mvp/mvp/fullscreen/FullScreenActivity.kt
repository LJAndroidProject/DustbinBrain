package com.ffst.dustbinbrain.kotlin_mvp.mvp.fullscreen

import com.ffst.dustbinbrain.kotlin_mvp.R
import com.ffst.annotation.StatusBar
import com.ffst.mvp.base.activity.BaseActivity

@StatusBar(hide = true)
class FullScreenActivity : BaseActivity() {
    override fun layoutId(): Int {
        return R.layout.activity_full_screen
    }

    override fun initViewData() {
        TODO("Not yet implemented")
    }

}
