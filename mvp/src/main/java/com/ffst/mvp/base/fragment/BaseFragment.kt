package com.ffst.mvp.base.fragment

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.annotation.LayoutRes
import androidx.annotation.Nullable
import androidx.fragment.app.Fragment
import com.ffst.mvp.base.BaseContract
import com.ffst.mvp.base.activity.BaseActivity

/**
 * Fragment封装基类
 * @author catchpig
 * @date 2019/4/4 23:14
 */
abstract class BaseFragment : Fragment(), BaseContract.View {
    override fun baseActivity(): BaseActivity? {
        if (activity is BaseActivity) {
            return activity as BaseActivity
        }
        return null
    }

    override fun activity(): Activity {
        return requireActivity()
    }

    override fun applicationContext(): Context {
        return activity().applicationContext
    }

    @CallSuper
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }

    @CallSuper
    @Nullable
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(layoutId(), container, false)
    }

    @CallSuper
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    @CallSuper
    override fun onStart() {
        super.onStart()
    }

    @CallSuper
    override fun onPause() {
        super.onPause()
    }

    @CallSuper
    override fun onStop() {
        super.onStop()
    }

    @CallSuper
    override fun onDestroyView() {
        super.onDestroyView()
    }

    @CallSuper
    override fun onDestroy() {
        super.onDestroy()
    }

    @LayoutRes
    protected abstract fun layoutId(): Int

    override fun loadingView(isDialog: Boolean) {
        baseActivity()?.let {
            it.loadingView(isDialog)
        }
    }

    override fun hideLoadingView() {
        baseActivity()?.let {
            it.hideLoadingView()
        }
    }

    override fun closeActivity() {
        activity?.let {
            it.finish()
        }
    }

    override fun toast(text: String, isLong: Boolean) {
        baseActivity()?.let {
            it.toast(text, isLong)
        }
    }
}
