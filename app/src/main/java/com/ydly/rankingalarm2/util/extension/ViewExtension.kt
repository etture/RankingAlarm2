package com.ydly.rankingalarm2.util.extension

import android.content.ContextWrapper
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AppCompatActivity
import android.view.View

fun View.getParentActivity(): AppCompatActivity? {
    var context = this.context
    while (context is ContextWrapper) {
        if (context is AppCompatActivity) {
            return context
        }
        context = context.baseContext
    }
    return null
}

fun View.getParentFragment(): Fragment? {
    var context = this.context
    while (context is ContextWrapper) {
        if (context is Fragment) {
            return context
        }
        context = context.baseContext
    }
    return null
}
