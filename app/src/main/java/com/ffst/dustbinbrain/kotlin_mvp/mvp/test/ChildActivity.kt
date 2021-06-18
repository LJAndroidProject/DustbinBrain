package com.ffst.dustbinbrain.kotlin_mvp.mvp.test

import android.view.View
import com.ffst.annotation.OnClickFirstDrawable
import com.ffst.annotation.OnClickFirstText
import com.ffst.annotation.StatusBar
import com.ffst.annotation.Title
import com.ffst.dustbinbrain.kotlin_mvp.R
import com.ffst.mvp.base.activity.BaseActivity
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Flowable
import java.util.concurrent.TimeUnit

@Title(R.string.child_title)
@StatusBar
class ChildActivity : BaseActivity(){
    @OnClickFirstDrawable(R.drawable.more)
    fun clickFirstDrawable(v: View) {
        toast(" 第一个图标按钮点击生效")

    }
    @OnClickFirstText(R.string.more)
    fun clickFirstText() {
        toast("第一个文字按钮点击生效")

    }

    /**
     * dialog形式的loading
     */
    fun loadingDialog(v: View){
        loadingView(true)
        Flowable.timer(5,TimeUnit.SECONDS).observeOn(AndroidSchedulers.mainThread()).subscribe {
            hideLoadingView()
        }
    }

    /**
     * 标题栏以下的loading
     */
    fun loadingExtTitle(v: View){
        loadingView(false)
        Flowable.timer(5,TimeUnit.SECONDS).observeOn(AndroidSchedulers.mainThread()).subscribe {
            hideLoadingView()
        }
    }
    override fun layoutId(): Int {
        return R.layout.activity_child
    }

    override fun initViewData() {
        TODO("Not yet implemented")
    }
}
