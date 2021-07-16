package com.ffst.dustbinbrain.kotlin_mvp.adapter

import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import com.blankj.utilcode.util.LogUtils
import com.bumptech.glide.Glide
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.ffst.dustbinbrain.kotlin_mvp.R
import com.ffst.dustbinbrain.kotlin_mvp.bean.DustbinStateBean

/**
 * Created by LiuJW
 *on 2021/7/9
 */
class DustbinControlItemAdapter(layoutResId:Int, listData: ArrayList<DustbinStateBean?>?) : BaseQuickAdapter<DustbinStateBean, BaseViewHolder>(layoutResId,listData),View.OnTouchListener {

    override fun convert(helper: BaseViewHolder?, item: DustbinStateBean?) {
        //设置点击效果
        helper?.getView<ImageView>(R.id.control_item_iv)?.setOnTouchListener(this)
        //图片资源
        var imageResource = 0
        when(item?.dustbinBoxType){
            "KITCHEN"->{
                //厨余垃圾
                imageResource = R.mipmap.chuyu
            }
            "HARMFUL"->{
                //有害垃圾
                imageResource = R.mipmap.youhai
            }
            "RECYCLABLES"->{
                //可回收垃圾
                imageResource = R.mipmap.kehuishou
            }
            "OTHER"->{
                //其它垃圾
                imageResource = R.mipmap.qita
            }
            else->{
                imageResource = R.mipmap.qita
            }
        }

        helper?.getView<ImageView>(R.id.control_item_iv)?.let {
            Glide.with(mContext).load(imageResource).into(
                it
            )
        }
        helper?.addOnClickListener(R.id.control_item_iv)
        helper?.addOnLongClickListener(R.id.control_item_iv)
    }

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        when(event?.action){
            MotionEvent.ACTION_DOWN->{
                v?.alpha =0.8f
            }
            MotionEvent.ACTION_UP->{
                v?.alpha = 1f
            }
            else->{
                return false
            }
        }
        return false
    }


}