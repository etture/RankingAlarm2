package com.ydly.rankingalarm2.ui.alarm

import android.app.Activity.RESULT_OK
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import android.content.Intent
import androidx.recyclerview.widget.SortedList
import android.view.View
import com.ydly.rankingalarm2.R
import com.ydly.rankingalarm2.base.BaseViewModel
import com.ydly.rankingalarm2.data.local.alarm.AlarmData
import com.ydly.rankingalarm2.data.repository.AlarmDataRepository
import com.ydly.rankingalarm2.util.*
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import org.jetbrains.anko.info
import java.util.*
import javax.inject.Inject

class AlarmViewModel : BaseViewModel() {

    @Inject
    lateinit var alarmDataRepo: AlarmDataRepository

    // View element interaction events to be observed by View
    private val onClickEvent: MutableLiveData<SingleEvent<Int>> = MutableLiveData()
    private val onListItemClickEvent: MutableLiveData<SingleEvent<AlarmData>> = MutableLiveData()

    // Events for activating and deactivating Alarm events
    // 4 cases
    // New alarm is created -> activate new alarm
    // Existing active alarm is updated -> deactivate old alarm, activate new alarm
    // Existing inactive alarm is updated -> activate new alarm
    // Existing active alarm is deleted -> deactivate old alarm
    private val activateAlarmEvent: MutableLiveData<SingleEvent<AlarmData>> = MutableLiveData()
    private val deactivateAlarmEvent: MutableLiveData<SingleEvent<AlarmData>> = MutableLiveData()

    // Toast event to be observed for showing toasts on View
    private val newToast: MutableLiveData<SingleEvent<String>> = MutableLiveData()

    private val alarmListOnClickListener = object : AlarmListAdapter.AlarmItemListener {
        override fun onAlarmItemClick(alarmItem: AlarmData) {
            onListItemClick(alarmItem)
        }

        override fun onOnOffToggleChanged(alarmItem: AlarmData, isToggledOn: Boolean) {
            onListItemOnOffToggleChanged(alarmItem, isToggledOn)
        }
    }

    private val alarmListAdapter = AlarmListAdapter(alarmListOnClickListener)


    //========= Init and private functions (business logic) ==========

    init {
        initAlarmList()
    }

    // When Alarm item in RecyclerView is clicked
    private fun onListItemClick(alarmItem: AlarmData) {
        onListItemClickEvent.value = SingleEvent(alarmItem)
    }

    // When toggle on Alarm item in RecyclerView is toggled, either on or off
    private fun onListItemOnOffToggleChanged(originalAlarmData: AlarmData, isToggledOn: Boolean) {

        val indexInList = alarmListAdapter.getIndexOf(originalAlarmData)
        info("onListItemOnOffToggleChanged() -> indexInList: $indexInList")
        info("onListItemOnOffToggleChanged() -> originalAlarmData: $originalAlarmData, isToggledOn: $isToggledOn")

        if (originalAlarmData.isToggledOn == isToggledOn) {

            info("onListItemOnOffToggleChanged() -> originalAlarmData.isToggledOn and the new isToggledOn are the same -- NO CHANGE")

        } else {

            subscription += Flowable.fromCallable {
                alarmDataRepo.toggleChange(
                    originalAlarmData,
                    isToggledOn
                )
            }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                // Perform alarm setting actions involving broadcast receivers and services here
                .subscribeBy(
                    onNext = { (oldAlarmData, newAlarmData) ->
                        info("onListItemOnOffToggleChanged() -> subscribeBy(), newAlarmData: $newAlarmData")

                        alarmListAdapter.updateAlarmItemAt(indexInList, newAlarmData)

                        // If the toggle was turned on, activate the alarm
                        if (newAlarmData.isToggledOn) {
                            activateAlarm(newAlarmData)
                        }
                        // If it was turned off, deactivate the alarm
                        else {
                            deactivateAlarm(oldAlarmData)
                        }

                    },
                    onError = { error -> info("onListItemOnOffToggleChanged() subscribe error, $error") }
                )
        }
    }

    private fun initAlarmList() {

        subscription += Flowable.fromCallable { alarmDataRepo.getAlarms() }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onNext = { dbAlarmList ->
                    alarmListAdapter.initAlarmList(dbAlarmList)
                    info("initAlarmList() -> AlarmList: $dbAlarmList, length: ${alarmListAdapter.itemCount}")
                },
                onError = { error -> info("An error occurred while loading the alarm list, $error") }
            )

    }

    private fun _refreshAlarmList() {

        subscription += Flowable.fromCallable { alarmDataRepo.getAlarms() }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onNext = { dbAlarmList ->
                    alarmListAdapter.refreshAlarmList(dbAlarmList)
                    info("_refreshAlarmList() -> AlarmList: $dbAlarmList, length: ${alarmListAdapter.itemCount}")
                },
                onError = { error -> info("_refreshAlarmList() subscribe error, $error") }
            )

    }

    private fun insertNewAlarmItem(dateTime: Long) {
        // Must perform Room database operation in a background thread
        subscription += Flowable.fromCallable { alarmDataRepo.insertNewAlarm(dateTime) }
            .concatMap { insertedAlarmData -> Flowable.just(insertedAlarmData) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onNext = { insertedAlarmData ->
                    // Only insert data if it is new
                    // and id of the AlarmData is not -1
                    info("alarmData id: ${insertedAlarmData.id}")
                    if (insertedAlarmData.id!! > 0) {
                        alarmListAdapter.insertAlarmItem(insertedAlarmData)
                        info("insertNewAlarmItem() called, new item inserted to adapter")

                        // Since all newly inserted alarm items are toggled on by default,
                        // activate the alarm
                        activateAlarm(insertedAlarmData)
                    } else {
                        newToast(res.getString(R.string.alarmAlreadyExists))
                        info("insertNewAlarmItem() called, ignored item NOT inserted to adapter")
                    }
                },
                onError = { error -> info("insertNewAlarmItem() subscribe error, $error") }
            )
    }

    private fun updateAlarmItem(originalAlarmData: AlarmData, editedTimeInMillis: Long) {
        val indexInList = alarmListAdapter.getIndexOf(originalAlarmData)
        info("updateAlarmItemAt(), indexInList: $indexInList")

        subscription += Flowable.fromCallable {
            alarmDataRepo.updateAlarm(
                originalAlarmData,
                editedTimeInMillis
            )
        }
            .concatMap { newAlarmData -> Flowable.just(newAlarmData) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onNext = { (oldAlarmData, newAlarmData) ->
                    // Check whether the updated AlarmData
                    // had the same exact timeInMillis as another AlarmData in the list
                    // in which case the item must be replaced in the adapter
                    val indexOfReplacedItem = alarmListAdapter.getIndexOf(newAlarmData)
                    info("indexOfReplacedItem: $indexOfReplacedItem")
                    val sameExactDateTime = indexOfReplacedItem != SortedList.INVALID_POSITION

                    // If the edited AlarmItem has the exact same timeInMillis as another item in the list
                    // The old item must be replaced with the new one
                    if (sameExactDateTime) {
                        alarmListAdapter.updateAlarmItemReplaceDuplicate(
                            indexOfReplacedItem,
                            oldAlarmData,
                            newAlarmData
                        )

                        // If the alarm item is toggled on,
                        // deactivate previous alarm and activate new alarm
                        if (oldAlarmData.isToggledOn) {
                            deactivateAlarm(oldAlarmData)
                            activateAlarm(newAlarmData, replaced = true)
                        } else {
                            activateAlarm(newAlarmData, replaced = true)
                        }

                    } else {
                        alarmListAdapter.updateAlarmItemAt(indexInList, newAlarmData)

                        // If the alarm item is toggled on,
                        // deactivate previous alarm and activate new alarm
                        if (oldAlarmData.isToggledOn) {
                            deactivateAlarm(oldAlarmData)
                            activateAlarm(newAlarmData)
                        } else {
                            activateAlarm(newAlarmData)
                        }
                    }
                    info("updateAlarmItemAt() called")
                },
                onError = { error -> info("updateAlarmItemAt() subscribeBy() error, $error") }
            )
    }

    private fun deleteAlarmItem(alarmData: AlarmData) {
        subscription += Flowable.fromCallable { alarmDataRepo.deleteAlarm(alarmData) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onNext = { deletedAlarmData ->
                    alarmListAdapter.deleteAlarmItem(deletedAlarmData)
                    info("deleteAlarmItem() called")

                    // If the deleted alarm item was previously turned on,
                    // then deactivate the deleted alarm
                    if (deletedAlarmData.isToggledOn) {
                        deactivateAlarm(deletedAlarmData)
                    }

                },
                onError = { error -> info("deleteAlarmItem() subscribe error, $error") }
            )
    }

    private fun deleteAllAlarmItems() {
        subscription += Flowable.fromCallable { alarmDataRepo.deleteAllAlarms() }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onNext = {
                    alarmListAdapter.clearAlarmList()
                    info("deleteAllAlarmItems() called")

                    // TODO code to deactivate all previously active alarms
                },
                onError = { error -> info("deleteAllAlarmItems() subscribe error, $error") }
            )
    }

    // For activating a new AlarmItem (different Toast text if existing alarm was replaced with the same date/time)
    private fun activateAlarm(alarmData: AlarmData, replaced: Boolean = false) {
        val alarmGoingOffText = if (replaced) {
            buildActivateToastMessage(alarmData, res.getString(R.string.editedExistingAlarm))
        } else {
            buildActivateToastMessage(alarmData)
        }
        info("activateAlarm(), alarmData: $alarmData, text: $alarmGoingOffText")
        newToast(alarmGoingOffText)
        activateAlarmEvent.value = SingleEvent(alarmData)
    }

    // For deactivating existing AlarmItem
    private fun deactivateAlarm(alarmData: AlarmData) {
        info("deactivateAlarm(), alarmData: $alarmData")
        deactivateAlarmEvent.value = SingleEvent(alarmData)
    }

    private fun buildActivateToastMessage(alarmData: AlarmData, additionalText: String = ""): String {

        // Calculate the amount of time until the alarm time
        // and display it as toast
        val rightNow = Calendar.getInstance()
        val alarmTimeMillisTo0 = alarmData.timeInMillis - (alarmData.timeInMillis % 1000)
        val nowTimeMillisTo0 = rightNow.timeInMillis - (rightNow.timeInMillis % 1000)
        val timeDiffInMillis = alarmTimeMillisTo0 - nowTimeMillisTo0

        val seconds = timeDiffInMillis / 1000

        val days = seconds / 60 / 60 / 24
        val hours = seconds / 60 / 60 % 24
        val minutes = (Math.ceil(seconds / 60.0) % 60).toInt()

        val daysText = if (days < 1) "" else (" " + days.toString() + res.getString(R.string.days))
        val hoursText = if (hours < 1) "" else (" " + hours.toString() + res.getString(R.string.hours))
        val minutesText = if (minutes < 1) "" else (" " + minutes.toString() + res.getString(R.string.minutes))

        return res.getString(
            R.string.alarmGoingOffIn,
            daysText,
            hoursText,
            minutesText
        ) + (if (additionalText == "") "" else " $additionalText")

    }

    private fun newToast(str: String) {
        newToast.value = SingleEvent(str)
    }

    override fun onCleared() {
        super.onCleared()
        subscription.clear()
    }


    //========= Functions accessible by View (data manipulation) ==========

    fun onClick(view: View) {
        when (view.id) {
            R.id.alarmFrag_fab_addAlarmBtn -> onClickEvent.value = SingleEvent(view.id)
            else -> info("view.id: ${view.id}, alarmItem_constLayout_item id: ${R.id.alarmItem_constLayout_item}")
        }
    }

    // View observes this to get onClick events on Views
    fun observeOnClickEvent(): LiveData<SingleEvent<Int>> = onClickEvent

    // View observes this to get onClick events on RecyclerView items
    fun observeOnListItemClickEvent(): LiveData<SingleEvent<AlarmData>> = onListItemClickEvent

    // View observes this to get activateAlarm events on RecyclerView alarm items
    fun observeActivateAlarmEvent(): LiveData<SingleEvent<AlarmData>> = activateAlarmEvent

    // View observes this to get deactivateAlarm events on RecyclerView alarm items
    fun observeDeactivateAlarmEvent(): LiveData<SingleEvent<AlarmData>> = deactivateAlarmEvent

    // View observes this to show new Toast messages
    fun observeNewToast(): LiveData<SingleEvent<String>> = newToast

    fun refreshAlarmList() {
        _refreshAlarmList()
    }

    // performing post-Activity actions from this ViewModel
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            // From CreateAlarmActivity, if setAlarmBtn was pressed, update the alarmList
            CREATE_ALARM_ACTIVITY -> if (resultCode == RESULT_OK) {
                val newAlarmInMillis = data?.getLongExtra("newAlarmInMillis", 0L)
                insertNewAlarmItem(newAlarmInMillis!!)
                info("new alarm set, millis: $newAlarmInMillis")
            }

            EDIT_ALARM_ACTIVITY -> if (resultCode == RESULT_OK) {
                val status = data?.getIntExtra("status", NO_CHANGE_ALARM)

                when (status) {

                    EDIT_ALARM -> {
                        val editedAlarmInMillis =
                            data.getLongExtra("editedAlarmInMillis", 0L)
                        val originalAlarmData = data.getParcelableExtra<AlarmData>("originalAlarmData")
                        updateAlarmItem(originalAlarmData, editedAlarmInMillis)
                    }

                    DELETE_ALARM -> {
                        val originalAlarmData = data.getParcelableExtra<AlarmData>("originalAlarmData")
                        deleteAlarmItem(originalAlarmData)
                    }

                    NO_CHANGE_ALARM -> {
                        /*Do nothing*/
                    }
                }
            }
        }
    }


    //========= Functions accessible by View (data binding) ==========

    fun getAlarmListAdapter(): AlarmListAdapter = alarmListAdapter

}
