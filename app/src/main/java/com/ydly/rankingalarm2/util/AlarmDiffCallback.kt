package com.ydly.rankingalarm2.util

import androidx.recyclerview.widget.DiffUtil
import com.ydly.rankingalarm2.data.local.alarm.model.AlarmData

class AlarmDiffCallback(
    private val oldList: List<AlarmData>,
    private val newList: List<AlarmData>
): DiffUtil.Callback() {

    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].id == newList[newItemPosition].id
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].timeInMillis == newList[newItemPosition].timeInMillis
    }

}
