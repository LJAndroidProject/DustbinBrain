package com.ffst.dustbinbrain.kotlin_mvp.mvp.transparent

import com.ffst.annotation.StatusBar
import com.ffst.dustbinbrain.kotlin_mvp.R
import com.ffst.mvp.base.activity.BaseActivity

@StatusBar(transparent = true)
class TransparentActivity : BaseActivity() {
    override fun layoutId(): Int {
        return R.layout.activity_transparent
    }

    override fun initViewData() {
        TODO("Not yet implemented")
    }
}
