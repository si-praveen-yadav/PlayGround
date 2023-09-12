package com.example.playground

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding

typealias Inflate<T> = (LayoutInflater, ViewGroup?, Boolean) -> T

inline fun <VB : ViewBinding> createBinding(
    parent: ViewGroup,
    inflate: Inflate<VB>,
    attachToRoot: Boolean = false
): VB {
    return inflate.invoke(
        LayoutInflater.from(parent.context),
        parent,
        attachToRoot
    )
}
