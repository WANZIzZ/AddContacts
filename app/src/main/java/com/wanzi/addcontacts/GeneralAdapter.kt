package com.dfsc.logistics_kotlin.adapter

import android.databinding.DataBindingUtil
import android.databinding.ViewDataBinding
import android.view.View
import android.view.ViewGroup

import com.chad.library.adapter.base.BaseQuickAdapter
import com.wanzi.addcontacts.R

/**
 * Created by WZ on 2017-11-02.
 * 需要单独建一个外部类继承BaseViewHolder，否则部分机型会出现ClassCastException，如果是内部类的构造方法要是public，定义的那个类也最好是public
 */

class GeneralAdapter<G>(layoutResId: Int, data: List<G>?, private val brId: Int) : BaseQuickAdapter<G, GeneralViewHolder>(layoutResId, data) {

    override fun convert(helper: GeneralViewHolder, item: G) {
        val binding = helper.binding
        binding.setVariable(brId, item)
        binding.executePendingBindings()
    }

    override fun getItemView(layoutResId: Int, parent: ViewGroup): View {
        val binding = DataBindingUtil.inflate<ViewDataBinding>(mLayoutInflater, layoutResId, parent, false) ?: return super.getItemView(layoutResId, parent)
        val view = binding.root
        view.setTag(R.id.BaseQuickAdapter_databinding_support, binding)
        return view
    }
}
