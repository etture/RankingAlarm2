package com.ydly.rankingalarm2.ui.alarm

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MediatorLiveData
import android.arch.lifecycle.MutableLiveData
import android.view.View
import android.widget.ToggleButton
import com.ydly.rankingalarm2.R
import com.ydly.rankingalarm2.base.BaseViewModel
import com.ydly.rankingalarm2.data.local.alarm.AlarmData
import com.ydly.rankingalarm2.data.repository.AlarmDataRepository
import com.ydly.rankingalarm2.util.DateTimeUtilMillisToUnits
import com.ydly.rankingalarm2.util.DateTimeUtilUnitsToMillis
import com.ydly.rankingalarm2.util.SingleEvent
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import org.jetbrains.anko.info
import java.util.*
import javax.inject.Inject

const val ACTIVATE = true
const val DEACTIVATE = false

class SingleAlarmViewModel : BaseViewModel() {

    @Inject
    lateinit var alarmDataRepo: AlarmDataRepository

    private lateinit var myAlarm: AlarmData

    // Toast event to be observed for showing toasts on View
    private val newToast: MutableLiveData<SingleEvent<String>> = MutableLiveData()

    // Toggle set event to set the toggle state upon initialization
    // Only called once during ViewModel initialization
    private val initialSetEvent: MutableLiveData<SingleEvent<Triple<Boolean, Int, Int>>> = MutableLiveData()

    // Toggle set event to set the toggle back off if the alarm time was not appropriate
    private val toggleBackOffEvent: MutableLiveData<SingleEvent<Boolean>> = MutableLiveData()

    // Activate/Deactive alarm event to set clock visibility status
    // Boolean value denotes whether ToggleButton was toggled on or off
    private val activateAlarmEvent: MutableLiveData<SingleEvent<Boolean>> = MutableLiveData()

    // For setting TimePicker time (2-way DataBinding)
    val hour = MutableLiveData<Int>()
    val minute = MutableLiveData<Int>()

    // For setting TimeTextView timeString
    private val timeString = MutableLiveData<String>()
    private val ampmString = MutableLiveData<String>()

    // For setting TimePicker and TimeTextView visibility (1-way DataBinding)
    private val timePickerVisibility = MutableLiveData<Boolean>()
    private val timeTxtVwVisibility = MutableLiveData<Boolean>()

    private var isToggled = false

    // MediatorLiveData for 2-way DataBinding on hour
    private val hourMediator = MediatorLiveData<Int>().apply {
        addSource(hour) { value ->
            setValue(value)
            info("hour changed: ${hour.value}")
        }
    }.also { it.observeForever { /*Do nothing*/ } }

    // MediatorLiveData for 2-way DataBinding on minute
    private val minuteMediator = MediatorLiveData<Int>().apply {
        addSource(minute) { value ->
            setValue(value)
            info("minute changed: ${minute.value}")
        }
    }.also { it.observeForever { /*Do nothing*/ } }


    //========= Init and private functions (business logic) ==========

    // Initialize time at 6:00 am if there is no item in DB
    init {
        info("Values before initialization: hour: ${hour.value}, minute: ${minute.value}, isToggled: $isToggled")
        initAlarmItem()
        info("Values after initialization: hour: ${hour.value}, minute: ${minute.value}, isToggled: $isToggled")
    }

    // When Fragment is created, initialize the calendar object with the AlarmData from DB
    // If there is no AlarmData in DB, then put a basic AlarmData object at 6:00 am to DB
    private fun initAlarmItem() {

        subscription += alarmDataRepo.getAlarms()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onNext = { dbAlarmList ->
                    if (dbAlarmList.isEmpty()) {
                        initDB()
                        info("initAlarmItem() -> DB is empty")
                    } else {
                        myAlarm = dbAlarmList[0]
                        initDisplayedElements()
                        info("initAlarmItem() -> myAlarm: $myAlarm")
                    }
                }
            )

    }

    // If there is no AlarmData in DB, then put a basic AlarmData object at 6:00 am to DB
    private fun initDB() {

        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 7)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        subscription += Flowable.fromCallable { alarmDataRepo.insertNewAlarm(calendar.timeInMillis) }
            .subscribeOn(Schedulers.io())
            .subscribeBy(
                onNext = { initializedAlarmData ->
                    myAlarm = initializedAlarmData
                    initDisplayedElements()
                    info("initDB() -> DB initialized, myAlarm: $myAlarm")
                },
                onError = {}
            )

    }

    private fun initDisplayedElements() {
        val dateTimeUtil = DateTimeUtilMillisToUnits(myAlarm.timeInMillis)
        hour.value = dateTimeUtil.hour24
        minute.value = dateTimeUtil.minute

        // Send an event to View to set the intial toggle state of ToggleButton
        initialSetEvent.value = SingleEvent(Triple(myAlarm.isToggledOn, dateTimeUtil.hour24, dateTimeUtil.minute))

        // Set data-binded variables to visibility of TimePicker and TimeTxtVw
        setTimeVisibility(activated = myAlarm.isToggledOn)
        isToggled = myAlarm.isToggledOn
        info("initDisplayedElements() -> hour: ${dateTimeUtil.hour24}, minute: ${dateTimeUtil.minute}, isToggled: ${myAlarm.isToggledOn}")
    }


    // Update item when ToggleButton is pressed, whether on or off
    // If on, then save the new AlarmData into the DB and activate the alarm
    // If off, then save the new AlarmData into the DB and deactivate the alarm
    private fun toggleChange(changedToggleStatus: Boolean) {

        val oldIsToggled = isToggled
        isToggled = changedToggleStatus

        if (oldIsToggled != changedToggleStatus) {
            // Turned on, so update AlarmData with updated timeInMillis and isToggled values
            // and activate the new alarm
            if (changedToggleStatus) {

                val alarmSetHour = hour.value!!
                val alarmSetMinute = minute.value!!

                // Here you must check whether the alarm is set between the designated range (05:00 ~ 10:59)
                // If yes, then proceed with normal logic to activate alarm
                // If not, then don't bother to activate the alarm, just display a Toast message -> "You suck!"
                when {
                    // Alarm is not in the designated time range
                    alarmSetHour < 5 -> {
                        newToast(res.getString(R.string.alarmSetBefore5))
                        toggleBackOffEvent.value = SingleEvent(DEACTIVATE)
                        isToggled = DEACTIVATE
                    }
                    alarmSetHour >= 11 -> {
                        newToast(res.getString(R.string.alarmSetAfter11))
                        toggleBackOffEvent.value = SingleEvent(DEACTIVATE)
                        isToggled = DEACTIVATE
                    }

                    // Alarm is between the designated time (4:00 ~ 10:59) so activate it
                    else -> {
                        // Check to see what day the alarm should be set to -- today or tomorrow
                        val rightNow = Calendar.getInstance()
                        val dateTimeUtil = DateTimeUtilUnitsToMillis(
                            year = rightNow.get(Calendar.YEAR),
                            month = rightNow.get(Calendar.MONTH),
                            dayOfMonth = rightNow.get(Calendar.DAY_OF_MONTH),
                            hour = alarmSetHour,
                            minute = alarmSetMinute
                        )

                        // If before 11:00 am, then set to this time today
                        if (rightNow.get(Calendar.HOUR_OF_DAY) < 11) {
                            subscription += Flowable.fromCallable {
                                alarmDataRepo.toggleChange(
                                    myAlarm,
                                    changedToggleStatus,
                                    dateTimeUtil.getDateTimeInMillis()
                                )
                            }
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribeBy(
                                    onNext = { (_, newAlarmData) ->
                                        activateAlarm(newAlarmData)
                                        myAlarm = newAlarmData
                                    },
                                    onError = {}
                                )
                        }
                        // Else if after noon, set to this time tomorrow
                        else {
                            dateTimeUtil.add(Calendar.DAY_OF_MONTH, 1)
                            subscription += Flowable.fromCallable {
                                alarmDataRepo.toggleChange(
                                    myAlarm,
                                    changedToggleStatus,
                                    dateTimeUtil.getDateTimeInMillis()
                                )
                            }
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribeBy(
                                    onNext = { (_, newAlarmData) ->
                                        activateAlarm(newAlarmData)
                                        myAlarm = newAlarmData
                                    },
                                    onError = {}
                                )
                        }
                    }
                }
            }
            // Turned off, so update AlarmData with only changed isToggled value
            // and deactivate the old alarm
            else {
                subscription += Flowable.fromCallable {
                    alarmDataRepo.toggleChange(
                        myAlarm,
                        changedToggleStatus
                    )
                }
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeBy(
                        onNext = { (oldAlarmData, newAlarmData) ->
                            deactivateAlarm(oldAlarmData)
                            myAlarm = newAlarmData
                        },
                        onError = {}
                    )
            }
        }
        // Changed toggle status is the same as the previously saved state
        else {
            info("toggleChange() -> changedToggleStatus == isToggled")
        }
    }

    private fun activateAlarm(alarmData: AlarmData) {
        setTimeVisibility(activated = ACTIVATE)
        info("activateAlarm() -> TimePicker visibility: ${timePickerVisibility.value}, TimeTxtVw visibility: ${timeTxtVwVisibility.value}")
        newToast("Alarm activated, $alarmData, Time: ${DateTimeUtilMillisToUnits.millisToString(alarmData.timeInMillis)}")
    }

    private fun deactivateAlarm(alarmData: AlarmData) {
        setTimeVisibility(activated = DEACTIVATE)
        info("deactivateAlarm() -> TimePicker visibility: ${timePickerVisibility.value}, TimeTxtVw visibility: ${timeTxtVwVisibility.value}")
        newToast("Alarm deactivated, $alarmData, Time: ${DateTimeUtilMillisToUnits.millisToString(alarmData.timeInMillis)}")
    }

    private fun setTimeVisibility(activated: Boolean) {
        buildTimeString()
        if (activated) {
            timePickerVisibility.value = false
            timeTxtVwVisibility.value = true
        } else {
            timePickerVisibility.value = true
            timeTxtVwVisibility.value = false
        }
    }

    // Function for building the timeString to be displayed whenever alarm is activated
    private fun buildTimeString() {
        val ampm = if (hour.value!! < 12) R.string.am else R.string.pm

        val hour12 = with(hour.value!! % 12) {
            when (this) {
                0 -> 12
                else -> this
            }
        }

        val hourString = if (hour12 < 10) "0$hour12" else "$hour12"
        val minuteString = if (minute.value!! < 10) "0${minute.value}" else "${minute.value}"
        timeString.value = "$hourString:$minuteString"
        ampmString.value = res.getString(ampm)
    }

    private fun newToast(str: String) {
        newToast.value = SingleEvent(str)
    }


    //========= Functions accessible by View (data manipulation) ==========

    // View observes this to show new Toast messages
    fun observeNewToast(): LiveData<SingleEvent<String>> = newToast

    // View observes this to set the initial toggle state of ToggleButton
    fun observeInitialToggleSetEvent(): LiveData<SingleEvent<Triple<Boolean, Int, Int>>> = initialSetEvent

    fun observeToggleBackOffEvent(): LiveData<SingleEvent<Boolean>> = toggleBackOffEvent

    // View observes this to get whether ToggleButton was toggled on or off
    fun observeActivateAlarmEvent(): LiveData<SingleEvent<Boolean>> = activateAlarmEvent

    fun onClickToggle(view: View) {
        info("onClickToggle() -> hour: ${hour.value}, minute: ${minute.value}")
        val toggleButton = view as ToggleButton
        toggleChange(toggleButton.isChecked)
    }


    //========= Functions accessible by View (DataBinding) ==========

    fun getTimeTxtVwVisibility(): LiveData<Boolean> = timeTxtVwVisibility
    fun getTimePickerVisibility(): LiveData<Boolean> = timePickerVisibility
    fun getTimeString(): LiveData<String> = timeString
    fun getAmpmString(): LiveData<String> = ampmString

}
