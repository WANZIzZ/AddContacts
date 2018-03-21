package com.dfsc.logistics_kotlin.adapter

import android.databinding.ViewDataBinding
import android.view.View
import com.chad.library.adapter.base.BaseViewHolder
import com.wanzi.addcontacts.R

/**
 * Created by WZ on 2017-12-26.
 */
class GeneralViewHolder(view: View) : BaseViewHolder(view) {

    val binding: ViewDataBinding
        get() = itemView.getTag(R.id.BaseQuickAdapter_databinding_support) as ViewDataBinding
}