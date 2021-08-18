package com.ffst.dustbinbrain.kotlin_mvp.utils

import android.content.Context
import android.text.TextUtils
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.ffst.dustbinbrain.kotlin_mvp.bean.DustbinENUM
import okhttp3.internal.and
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

/**
 * Created by LiuJW
 *on 2021/6/15
 */
class FenFenCommonUtil {
    companion object{
         const val authIP = "https://api-cn.faceplusplus.com"
         const val apiKey = "-"
         const val apiSecret = "-"
         const val FACE_TAG = "FacePassDemo"
         const val face_group_name = "ffst_facepass"
        /**
         * A：厨余垃圾，B：其他垃圾，C：可回收垃圾，D：有害垃圾
         */
        fun getDustbinType(text: String): String? {
            return if (text == "A") {
                DustbinENUM.KITCHEN.toString()
            } else if (text == "B") {
                DustbinENUM.OTHER.toString()
            } else if (text == "C") {
                DustbinENUM.RECYCLABLES.toString()
            } else if (text == "D") {
                DustbinENUM.HARMFUL.toString()
            } else if (text == "E") {
                DustbinENUM.BOTTLE.toString()
            } else if (text == "F") {
                DustbinENUM.WASTE_PAPER.toString()
            } else {
                null
            }
        }

        val key = "e0e9061d403f1898a501b8d7a840b949"

        fun md5(string: String): String? {
            if (TextUtils.isEmpty(string)) {
                return ""
            }
            var md5: MessageDigest? = null
            try {
                md5 = MessageDigest.getInstance("MD5")
                val bytes = md5.digest(string.toByteArray())
                val result = StringBuilder()
                for (b in bytes) {
                    var temp = Integer.toHexString(b and 0xff)
                    if (temp.length == 1) {
                        temp = "0$temp"
                    }
                    result.append(temp)
                }
                return result.toString()
            } catch (e: NoSuchAlgorithmException) {
                e.printStackTrace()
            }
            return ""
        }
        fun HideKeyboard(v: View){
            val imm = v.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            if (imm.isActive) {
                imm.hideSoftInputFromWindow( v.applicationWindowToken, 0 );
            }
        }


    }
}