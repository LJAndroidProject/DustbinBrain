package com.ffst.dustbinbrain.kotlin_mvp.utils

import android.content.Context
import com.peergine.plugin.lib.pgLibJNINode

/**
 * Created by LiuJW
 *on 2021/7/29
 */
class pgLiveMultiRenderUtils {
    companion object {
        fun CheckPlugin(context: Context): Boolean {
            return if (pgLibJNINode.Initialize(context)) {
                pgLibJNINode.Clean()
                true
            } else {
                false
            }
        }
    }

}