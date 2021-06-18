package com.ffst.aop

import com.ffst.utils.ext.logd
import org.aspectj.lang.JoinPoint
import org.aspectj.lang.annotation.*

/**
 * 打印生命周期日志
 * @author catchpig
 * @date 2019/9/26 00:26
 */
@Aspect
class LifecycleLogAspectJ {
    companion object {
        const val TAG = "[LIFE_CYCLE_LOG]"
    }

    /**
     * Presenter的生命周期
     */
    @Pointcut("execution(@androidx.annotation.CallSuper * com.ffst.mvp.base.BasePresenter.on*(..))")
    fun presenterLifecycle(){

    }

    /**
     * Activity的生命周期
     */
    @Pointcut("execution(@androidx.annotation.CallSuper * com.ffst.mvp.base.activity.BaseActivity.on*(..))")
    fun activityLifecycle(){

    }

    /**
     * Fragment的生命周期
     */
    @Pointcut("execution(@androidx.annotation.CallSuper * com.ffst.mvp.base.fragment.BaseFragment.on*(..))")
    fun fragmentLifecycle(){

    }

    @After("presenterLifecycle() || activityLifecycle() || fragmentLifecycle()")
    fun lifecycleLog(joinPoint: JoinPoint){
        val className  = joinPoint.target::class.java.simpleName
        val methodName = joinPoint.signature.name
        "$className:$methodName".logd(TAG)
    }
}