package com.example.playground

import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding

abstract class BaseViewHolder<T>(binding: ViewBinding) : RecyclerView.ViewHolder(binding.root) {
    abstract fun bind(data: T)
}

abstract class BaseDBViewHolder<T>(private val binding: ViewDataBinding) :
    RecyclerView.ViewHolder(binding.root) {

    open fun bind(data: T, bindingResourceId: Int) {
        binding.setVariable(bindingResourceId, data)
        binding.executePendingBindings()
    }

}

