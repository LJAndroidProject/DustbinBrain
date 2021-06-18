package com.ffst.dustbinbrain.kotlin_mvp.mvp.apk.model

import com.ffst.dustbinbrain.kotlin_mvp.mvp.apk.InstallApkContract
import com.ffst.mvp.manager.DownloadManager
import com.ffst.mvp.listener.DownloadCallback
import com.ffst.mvp.listener.MultiDownloadCallback

/**
 *
 * @author catchpig
 * @date 2020/11/20 15:51
 */
class InstallApkModel(private val downloadManager: DownloadManager):InstallApkContract.Model {
    override fun download(url: String, downloadCallback: DownloadCallback) {
        downloadManager.download(url,downloadCallback)
    }

    override fun multiDownload(urls: MutableList<String>, multiDownloadCallback: MultiDownloadCallback) {
        downloadManager.multiDownload(urls,multiDownloadCallback)
    }
}