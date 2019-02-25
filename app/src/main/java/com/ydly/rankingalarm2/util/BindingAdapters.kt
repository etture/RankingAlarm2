package com.ydly.rankingalarm2.util

import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.databinding.BindingAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.widget.TextView
import android.widget.ToggleButton
import com.ydly.rankingalarm2.util.extension.getParentActivity

@BindingAdapter("mutableText")
fun setMutableText(view: TextView, text: LiveData<String>?) {
    val parentActivity: AppCompatActivity? = view.getParentActivity()
    if(parentActivity != null && text != null) {
        text.observe(parentActivity, Observer { value -> view.text = value ?: "" })
    }
}

@BindingAdapter("adapter")
fun setAdapter(view: RecyclerView, adapter: RecyclerView.Adapter<*>) {
    if(view.adapter !== adapter) view.adapter = adapter
}

@BindingAdapter("mutableVisibility")
fun setMutableVisibility(view: View, visibility: LiveData<Boolean>) {
    val parentActivity: AppCompatActivity? = view.getParentActivity()
    if(parentActivity != null) {
        visibility.observe(parentActivity, Observer { value ->
            val visibleStatus = when(value!!) {
                true -> View.VISIBLE
                false -> View.GONE
            }
            view.visibility = visibleStatus
        })
    }
}

@BindingAdapter("mutableTextOff")
fun setMutableTextOff(view: ToggleButton, text: LiveData<String>?) {
    val parentActivity: AppCompatActivity? = view.getParentActivity()
    if(parentActivity != null && text != null) {
        text.observe(parentActivity, Observer { value -> view.textOff = value ?: "" })
    }
}
