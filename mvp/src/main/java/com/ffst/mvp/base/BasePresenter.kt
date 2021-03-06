package com.ffst.mvp.base


import androidx.annotation.CallSuper
import com.ffst.mvp.ext.io2main
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.subscribers.ResourceSubscriber


/**
 * @author catchpig
 * @date 2019/4/6 10:35
 */
open class BasePresenter: BaseContract.Presenter {
    private var mCompositeDisposable: CompositeDisposable = CompositeDisposable()
    @CallSuper
    override fun onCreate() {
    }

    @CallSuper
    override fun onStart() {
    }

    @CallSuper
    override fun onResume() {
    }

    @CallSuper
    override fun onPause() {
    }

    @CallSuper
    override fun onStop() {
    }

    @CallSuper
    override fun onDestroy() {
        mCompositeDisposable.clear()
    }

    override fun <T> execute(
            flowable: Flowable<T>,
            callback: ResourceSubscriber<T>,io2main:Boolean):Disposable {
        val disposable = if (io2main) {
            flowable.io2main().subscribeWith(callback)
        }else{
            flowable.subscribeWith(callback)
        }
        mCompositeDisposable.add(disposable)
        return disposable
    }

    override fun remove(disposable: Disposable) {
        mCompositeDisposable.remove(disposable)
    }
}
