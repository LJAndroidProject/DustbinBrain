package com.ffst.dustbinbrain.kotlin_mvp.mvp.recycle

import com.ffst.dustbinbrain.kotlin_mvp.R
import com.ffst.mvp.base.adapter.CommonViewHolder
import com.ffst.mvp.base.adapter.RecyclerAdapter
import com.ffst.mvp.widget.refresh.IPageControl
import kotlinx.android.synthetic.main.item_user.*

/**
 *
 * @author TLi2
 **/
class UserAdapter(iPageControl: IPageControl): RecyclerAdapter<User>(iPageControl) {
    override fun layoutId(): Int {
        return R.layout.item_user
    }

    override fun bindViewHolder(holder: CommonViewHolder, m: User, position: Int) {
        holder.name.text = m.name
    }
}