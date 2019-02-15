package com.ydly.rankingalarm2.ui.alarm

import android.databinding.DataBindingUtil
import android.support.v7.util.SortedList
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.util.SortedListAdapterCallback
import android.view.LayoutInflater
import android.view.ViewGroup
import com.ydly.rankingalarm2.R
import com.ydly.rankingalarm2.data.local.alarm.AlarmData
import com.ydly.rankingalarm2.databinding.ItemAlarmBinding
import com.ydly.rankingalarm2.util.DateTimeUtilMillisToUnits
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.debug
import org.jetbrains.anko.info

class AlarmListAdapter(private val listener: AlarmItemListener) :
    RecyclerView.Adapter<AlarmListAdapter.AlarmViewHolder>(), AnkoLogger {

    private val sortedAlarmList: SortedList<AlarmData>

    init {
        info("AlarmListAdapter instantiated: $this")
        sortedAlarmList = SortedList(AlarmData::class.java, object : SortedListAdapterCallback<AlarmData>(this) {
            override fun compare(o1: AlarmData?, o2: AlarmData?): Int {
                val o1DateTimeUtil = DateTimeUtilMillisToUnits(o1?.timeInMillis!!)
                val o2DateTimeUtil = DateTimeUtilMillisToUnits(o2?.timeInMillis!!)

                info("SortedList compare() called")
                info("alarm1-> id=${o1.id}, timeInMillis=${o1.timeInMillis} / alarm2-> id=${o2.id}, timeInMillis=${o2.timeInMillis}")

                // Sort based on time only, regardless of date
                return when {
                    o1DateTimeUtil.hour24 < o2DateTimeUtil.hour24 -> -1
                    o1DateTimeUtil.hour24 > o2DateTimeUtil.hour24 -> 1
                    else -> {
                        when {
                            o1DateTimeUtil.minute < o2DateTimeUtil.minute -> -1
                            o1DateTimeUtil.minute > o2DateTimeUtil.minute -> 1
                            else -> 0
                        }
                    }
                }
            }

            override fun areItemsTheSame(item1: AlarmData?, item2: AlarmData?): Boolean =
                item1?.id == item2?.id || item1?.timeInMillis == item2?.timeInMillis

            override fun areContentsTheSame(oldItem: AlarmData?, newItem: AlarmData?): Boolean =
                oldItem?.timeInMillis == newItem?.timeInMillis
        })
    }

    // Interface for onClick action
    interface AlarmItemListener {
        fun onAlarmItemClick(alarmItem: AlarmData)
        fun onOnOffToggleChanged(alarmItem: AlarmData, isToggledOn: Boolean)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlarmViewHolder {
        val binding: ItemAlarmBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context), R.layout.item_alarm, parent, false
        )

        return AlarmViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AlarmViewHolder, position: Int) {
        info("onBindViewHolder() called, ${sortedAlarmList[holder.adapterPosition].id}, ${sortedAlarmList[holder.adapterPosition].timeInMillis}")
        holder.bind(sortedAlarmList[holder.adapterPosition], listener)
    }

    override fun getItemCount(): Int = sortedAlarmList.size()

    fun initAlarmList(alarmList: List<AlarmData>) {
        sortedAlarmList.addAll(alarmList)
    }

    fun refreshAlarmList(alarmList: List<AlarmData>) {
        info("refreshAlarmList() called")
        sortedAlarmList.replaceAll(alarmList)
        printAlarmList()
    }

    fun insertAlarmItem(alarmItem: AlarmData) {
        info("insertAlarmItem() called")
        // Only insert if the new item is not found in the existing list (no duplicates)
        if (sortedAlarmList.indexOf(alarmItem) == SortedList.INVALID_POSITION) {
            sortedAlarmList.add(alarmItem)
        }
    }

    fun updateAlarmItemAt(index: Int, alarmItem: AlarmData) {
        info("updateAlarmItemAt() called")
        sortedAlarmList.updateItemAt(index, alarmItem)
    }

    fun deleteAlarmItem(alarmItem: AlarmData) {
        info("deleteAlarmItem() called")
        sortedAlarmList.remove(alarmItem)
    }

    fun deleteAlarmItemAt(index: Int) {
        info("deleteAlarmItemByIndex() called")
        sortedAlarmList.removeItemAt(index)
    }

    fun updateAlarmItemReplaceDuplicate(indexOfReplacedItem: Int, originalAlarmData: AlarmData, newAlarmData: AlarmData) {
        info("updateAlarmItemReplaceDuplicate() called")
        sortedAlarmList.beginBatchedUpdates()
        sortedAlarmList.removeItemAt(indexOfReplacedItem)
        sortedAlarmList.remove(originalAlarmData)
        sortedAlarmList.add(newAlarmData)
        sortedAlarmList.endBatchedUpdates()
    }

    fun getIndexOf(alarmItem: AlarmData): Int = sortedAlarmList.indexOf(alarmItem)

    fun clearAlarmList() {
        sortedAlarmList.clear()
    }

    private fun printAlarmList(): String {
        info("printAlarmList() called")
        var listToPrint = ""
        for (i in 0..(sortedAlarmList.size() - 1)) {
            val item = sortedAlarmList[i]
            if (i > 0) listToPrint += ", "
            listToPrint += "(id=${item.id}, timeInMillis=${item.timeInMillis}, isToggledOn=${item.isToggledOn})"
        }
        return listToPrint
    }

    class AlarmViewHolder(private val binding: ItemAlarmBinding) : RecyclerView.ViewHolder(binding.root), AnkoLogger {
        private val viewModel = AlarmItemViewModel()

        init {
            info("AlarmItemViewModel instantiated, $viewModel")
        }

        fun bind(alarmData: AlarmData, listener: AlarmItemListener) {
            viewModel.bind(alarmData, listener)
            binding.viewModel = viewModel
        }
    }

}
