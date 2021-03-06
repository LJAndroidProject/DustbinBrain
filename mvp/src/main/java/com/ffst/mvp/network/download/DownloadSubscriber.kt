package com.ffst.mvp.network.download

import com.ffst.mvp.listener.DownloadCallback
import com.ffst.mvp.listener.DownloadProgressListener
import com.ffst.utils.ext.logd
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.functions.Consumer
import io.reactivex.rxjava3.subscribers.ResourceSubscriber
import java.lang.ref.SoftReference

/**
 * 下载观察者
  * @author catchpig
 * @date 2020/11/20 10:25
 */
class DownloadSubscriber(private val downloadCallback: DownloadCallback):ResourceSubscriber<String>(), DownloadProgressListener {
    companion object{
        const val TAG = "DownloadSubscriber"
    }

    override fun onStart() {
        super.onStart()
        downloadCallback.onStart()
    }
    override fun onNext(t: String) {
        downloadCallback.onSuccess(t)
    }

    override fun onError(t: Throwable?) {
        t?.let {
            downloadCallback.onError(it)
        }
    }

    override fun onComplete() {
        downloadCallback.onComplete()
    }

    override fun update(read: Long, count: Long, done: Boolean) {
        Flowable.just(done).subscribeOn(AndroidSchedulers.mainThread()).subscribe(Consumer {
            "progress:$read/$count;$done".logd(TAG)
            downloadCallback.onProgress(read,count)
        })

    }
}