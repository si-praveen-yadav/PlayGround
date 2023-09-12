package com.example.playground

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncDifferConfig
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding

class SimpleListAdapter<VB : ViewBinding, Data : Any>(
    private val inflate: Inflate<VB>,
    itemComparator: DiffUtil.ItemCallback<Data>,
    private val onBind: (position: Int, rowBinding: VB, Data) -> Unit,
    private val onChangePayload: ((position: Int, VB, Data, Bundle) -> Unit)? = null,
    private val itemInit: ((SimpleListAdapter<VB, Data>.SimpleViewHolder) -> Unit)? = null
) : ListAdapter<Data, SimpleListAdapter<VB, Data>.SimpleViewHolder>(
    AsyncDifferConfig.Builder(
        itemComparator
    ).build()
) {

    inner class SimpleViewHolder(private val binding: VB) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            itemInit?.invoke(this)
        }

        fun getBinding(): VB {
            return binding
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SimpleViewHolder {
        val binding = inflate.invoke(LayoutInflater.from(parent.context), parent, false)
        return SimpleViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SimpleViewHolder, position: Int) {
        onBind.invoke(position, holder.getBinding(), getItem(position))
    }

    override fun onBindViewHolder(
        holder: SimpleViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        val data = getItem(position)
        if(payloads.isEmpty() || payloads[0] !is Bundle) {
            data ?: return
            onBind.invoke(position, holder.getBinding(), data)
        }
        else {
            val bundle = payloads[0] as Bundle
            onChangePayload?.invoke(position, holder.getBinding(), data, bundle)
        }
    }
}