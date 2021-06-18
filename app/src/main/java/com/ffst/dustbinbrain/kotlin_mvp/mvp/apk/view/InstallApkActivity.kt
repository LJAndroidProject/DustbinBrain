package com.ffst.dustbinbrain.kotlin_mvp.mvp.apk.view

import android.Manifest
import com.ffst.annotation.Title
import com.ffst.dustbinbrain.kotlin_mvp.R
import com.ffst.dustbinbrain.kotlin_mvp.mvp.apk.InstallApkContract
import com.ffst.mvp.base.activity.BasePresenterActivity
import com.tbruyelle.rxpermissions3.RxPermissions
import kotlinx.android.synthetic.main.activity_install_apk.*
import org.koin.core.parameter.parametersOf
import org.koin.core.scope.inject

/**
 *
 * @author catchpig
 * @date 2020/11/20 15:50
 */
@Title(R.string.download_install_apk)
class InstallApkActivity: BasePresenterActivity<InstallApkContract.Presenter>(),InstallApkContract.View {
    override val presenter: InstallApkContract.Presenter by inject{ parametersOf(this) }
    private val rxPermissions by lazy { RxPermissions(this) }
    override fun initParam() {

    }

    override fun initView() {
        rxPermissions
                .request(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .subscribe {
                    if (it) {
                        presenter.download()
                    }
                }
    }

    override fun layoutId(): Int {
        return R.layout.activity_install_apk
    }

    override fun initViewData() {
        TODO("Not yet implemented")
    }

    override fun setDownloadProgress(progress: Int) {
        progressBar.progress = progress
    }

    override fun setDownloadProgress1(progress: Int) {
        progressBar1.progress = progress
    }
}