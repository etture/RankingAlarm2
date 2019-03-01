package com.ydly.rankingalarm2.ui.alarm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import android.view.View
import android.widget.Button
import android.widget.ToggleButton
import com.ydly.rankingalarm2.R
import com.ydly.rankingalarm2.base.BaseViewModel
import com.ydly.rankingalarm2.data.local.alarm.model.AlarmData
import com.ydly.rankingalarm2.data.repository.AlarmDataRepository
import com.ydly.rankingalarm2.data.repository.AlarmHistoryRepository
import com.ydly.rankingalarm2.util.*
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.rxkotlin.zipWith
import io.reactivex.schedulers.Schedulers
import org.jetbrains.anko.info
import java.net.SocketTimeoutException
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class SingleAlarmViewModel : BaseViewModel() {

    @Inject lateinit var alarmDataRepo: AlarmDataRepository
    @Inject lateinit var alarmHistoryRepo: AlarmHistoryRepository

    private lateinit var myAlarm: AlarmData

    // Local state variable for whether ToggleButton is toggled on or off
    private var isToggled = false
    private var rangToday = false

    // Local state variable for the target date for the upcoming alarm to be set
    private lateinit var targetDate: DateTimeUtilMillisToUnits

    // Toast event to be observed for showing toasts on View
    private val newToast: MutableLiveData<SingleEvent<String>> = MutableLiveData()

    // Toggle set event to set the toggle state upon initialization
    // Only called once during ViewModel initialization
    private val initialSetEvent: MutableLiveData<SingleEvent<Triple<Boolean, Int, Int>>> = MutableLiveData()

    // Toggle set event to set the toggle back off if the alarm time was not appropriate
    private val toggleBackOffEvent: MutableLiveData<SingleEvent<Boolean>> = MutableLiveData()

    // Activate/Deactive alarm event to set clock visibility status
    private val activateEvent: MutableLiveData<SingleEvent<AlarmData>> = MutableLiveData()
    private val deactivateEvent: MutableLiveData<SingleEvent<AlarmData>> = MutableLiveData()

    // For setting TimePicker time (2-way DataBinding)
    val hour = MutableLiveData<Int>().apply { value = 6 }
    val minute = MutableLiveData<Int>().apply { value = 0 }

    // For setting TimePicker and TimeTextView visibility (1-way DataBinding)
    private val timePickerVisibility = MutableLiveData<Boolean>()
    private val timeTxtVwVisibility = MutableLiveData<Boolean>()

    // For setting TimeString
    private val timeString = MutableLiveData<String>()
    private val ampmString = MutableLiveData<String>()

    // For setting DateString
    private val dateString = MutableLiveData<String>()
    private val todayTmrwString = MutableLiveData<String>()
    private val textOffString = MutableLiveData<String>()

    // MediatorLiveData for 2-way DataBinding on hour
    private val hourMediator = MediatorLiveData<Int>().apply {
        addSource(hour) { value ->
            setValue(value)
            info("hour changed: ${hour.value}")

            setTargetDate()
        }
    }.also { it.observeForever { /*Do nothing*/ } }

    // MediatorLiveData for 2-way DataBinding on minute
    private val minuteMediator = MediatorLiveData<Int>().apply {
        addSource(minute) { value ->
            setValue(value)
            info("minute changed: ${minute.value}")

            setTargetDate()
        }
    }.also { it.observeForever { /*Do nothing*/ } }


    //========= Init and private functions (business logic) ==========

    init {
        info("Values before initialization: hour: ${hour.value}, minute: ${minute.value}, isToggled: $isToggled")
        initAlarmItem()
        info("Values after initialization: hour: ${hour.value}, minute: ${minute.value}, isToggled: $isToggled")
    }

    // When Fragment is created, initialize the calendar object with the AlarmData from DB
    // If there is no AlarmData in DB, then put a basic AlarmData object at 6:00 am to DB
    private fun initAlarmItem() {

        info("initAlarmItem() called")

        subscription += alarmDataRepo.getAlarmsFlowable()
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
                        info("initAlarmItem() -> myAlarm: $myAlarm, hour: ${hour.value}, minute: ${minute.value}")
                    }
                },
                onComplete = { setTargetDate() }
            )

    }

    // If there is no AlarmData in DB, then put a basic AlarmData object at 6:00 am to DB
    private fun initDB() {

        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, hour.value!!)
        calendar.set(Calendar.MINUTE, minute.value!!)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        subscription += Flowable.fromCallable {
            alarmDataRepo.insertNewAlarm(calendar.timeInMillis, isToggledOn = false)
        }
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

        // Set data-binded ToggleButton textOff text ("challenge!" or just "set alarm")
        subscription += alarmHistoryRepo.getToday(
            year = dateTimeUtil.year,
            month = dateTimeUtil.month,
            dayOfMonth = dateTimeUtil.dayOfMonth
        )
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onNext = { todayHistory ->
                    // Alarm hasn't rung yet today
                    if (todayHistory.isEmpty()) {

                    }
                    // Alarm has already rung today
                    else {
                        rangToday = true
                    }
                },
                onError = {}
            )
    }


    // Update item when ToggleButton is pressed, whether on or off
    // If on, then save the new AlarmData into the DB and activate the alarm
    // If off, then save the new AlarmData into the DB and deactivate the alarm
    private fun toggleChange(changedToggleStatus: Boolean) {

        val oldIsToggled = isToggled
        isToggled = changedToggleStatus
        setTargetDate()

        if (oldIsToggled != changedToggleStatus) {
            // Turned on, so update AlarmData with updated timeInMillis and isToggled values
            // and activate the new alarm
            if (changedToggleStatus) {

                val alarmSetHour = hour.value!!
                val alarmSetMinute = minute.value!!

                // Check to see what day the alarm should be set to -- today or tomorrow
                val dateTimeUtil = DateTimeUtilUnitsToMillis(
                    year = targetDate.year,
                    month = targetDate.month,
                    dayOfMonth = targetDate.dayOfMonth,
                    hour = alarmSetHour,
                    minute = alarmSetMinute
                )

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
        newToast(res.getString(R.string.alarmSet))
        activateEvent.value = SingleEvent(alarmData)
        setTimeVisibility(activated = ACTIVATE)
        info("activateAlarm() -> TimePicker visibility: ${timePickerVisibility.value}, TimeTxtVw visibility: ${timeTxtVwVisibility.value}")
        info("Alarm activated, $alarmData, Time: ${DateTimeUtilMillisToUnits.millisToString(alarmData.timeInMillis)}, activateEvent.value: ${activateEvent.value.toString()}")
    }

    private fun deactivateAlarm(alarmData: AlarmData) {
        newToast(res.getString(R.string.alarmCleared))
        deactivateEvent.value = SingleEvent(alarmData)
        setTimeVisibility(activated = DEACTIVATE)
        info("deactivateAlarm() -> TimePicker visibility: ${timePickerVisibility.value}, TimeTxtVw visibility: ${timeTxtVwVisibility.value}")
        info("Alarm deactivated, $alarmData, Time: ${DateTimeUtilMillisToUnits.millisToString(alarmData.timeInMillis)}, deactivateEvent.value: ${deactivateEvent.value.toString()}")
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

    private fun setTargetDate() {
        val rightNow = Calendar.getInstance()
        targetDate = DateTimeUtilMillisToUnits(rightNow.timeInMillis)

        info("RightNow -> hour: ${rightNow.get(Calendar.HOUR_OF_DAY)}, minute: ${rightNow.get(Calendar.MINUTE)} / Target -> hour: ${hour.value}, minute: ${minute.value}")

        when {
            // Hour is later than now -> set to today
            rightNow.get(Calendar.HOUR_OF_DAY) < hour.value!! -> {
                dateStringFinal(TODAY)
            }
            // Hour is equal to now
            rightNow.get(Calendar.HOUR_OF_DAY) == hour.value!! -> {
                when {
                    // Minute is later than now -> set to today
                    rightNow.get(Calendar.MINUTE) < minute.value!! -> {
                        dateStringFinal(TODAY)
                    }
                    // Minute is earlier than or equal to now -> set to tomorrow
                    rightNow.get(Calendar.MINUTE) >= minute.value!! -> {
                        dateStringFinal(TOMORROW)
                    }
                }
            }
            // Hour is earlier than now -> set to tomorrow
            rightNow.get(Calendar.HOUR_OF_DAY) > hour.value!! -> {
                dateStringFinal(TOMORROW)
            }
        }

    }

    private fun buildDateTodayTmrwString() {
        var month: String
        val dayOfMonth: String = res.getString(R.string.dayOfMonth, targetDate.dayOfMonth)
        var dayOfWeek: String

        with(targetDate.month) {
            month = when (this) {
                Calendar.JANUARY -> res.getString(R.string.jan)
                Calendar.FEBRUARY -> res.getString(R.string.feb)
                Calendar.MARCH -> res.getString(R.string.mar)
                Calendar.APRIL -> res.getString(R.string.apr)
                Calendar.MAY -> res.getString(R.string.may)
                Calendar.JUNE -> res.getString(R.string.jun)
                Calendar.JULY -> res.getString(R.string.jul)
                Calendar.AUGUST -> res.getString(R.string.aug)
                Calendar.SEPTEMBER -> res.getString(R.string.sep)
                Calendar.OCTOBER -> res.getString(R.string.oct)
                Calendar.NOVEMBER -> res.getString(R.string.nov)
                Calendar.DECEMBER -> res.getString(R.string.dec)
                else -> res.getString(R.string.na)
            }
            info("Month/Date: $month $dayOfMonth")
        }

        with(targetDate.dayOfWeek) {
            dayOfWeek = when (this) {
                Calendar.MONDAY -> res.getString(R.string.mon)
                Calendar.TUESDAY -> res.getString(R.string.tue)
                Calendar.WEDNESDAY -> res.getString(R.string.wed)
                Calendar.THURSDAY -> res.getString(R.string.thu)
                Calendar.FRIDAY -> res.getString(R.string.fri)
                Calendar.SATURDAY -> res.getString(R.string.sat)
                Calendar.SUNDAY -> res.getString(R.string.sun)
                else -> res.getString(R.string.na)
            }
            info("Day of Week: $dayOfWeek")
        }

        dateString.value = "$month $dayOfMonth ($dayOfWeek)"
        info("Full dayOfMonth string: ${dateString.value}")
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

    private fun dateStringFinal(todayTomorrowCode: Int) {
        when (todayTomorrowCode) {
            TODAY -> {
                todayTmrwString.value = res.getString(R.string.today)
                buildDateTodayTmrwString()

                // If alarm rang today, then "set alarm", if not, "challenge!"
                if(rangToday) {
                    textOffString.value = res.getString(R.string.set_alarm)
                }else {
                    textOffString.value = res.getString(R.string.challenge)
                }
            }
            TOMORROW -> {
                targetDate.add(Calendar.DAY_OF_MONTH, 1)
                todayTmrwString.value = res.getString(R.string.tomorrow)
                buildDateTodayTmrwString()
                textOffString.value = res.getString(R.string.challenge)
            }
        }
    }

    private fun newToast(str: String) {
        info("newToast() -> message: $str")
        newToast.value = SingleEvent(str)
    }


    //========= Functions accessible by View (data manipulation) ==========

    // View observes this to show new Toast messages
    fun observeNewToast(): LiveData<SingleEvent<String>> = newToast

    // View observes this to set the initial toggle state of ToggleButton
    fun observeInitialToggleSetEvent(): LiveData<SingleEvent<Triple<Boolean, Int, Int>>> = initialSetEvent

    fun observeToggleBackOffEvent(): LiveData<SingleEvent<Boolean>> = toggleBackOffEvent

    // View observes this to get whether ToggleButton was toggled on or off
    fun observeActivateEvent(): LiveData<SingleEvent<AlarmData>> = activateEvent

    fun observeDeactivateEvent(): LiveData<SingleEvent<AlarmData>> = deactivateEvent

    fun onClickToggle(view: View) {

        val toggleButton = view as ToggleButton
        toggleChange(toggleButton.isChecked)

        // Check local timeInMillis
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)

        // Test Endpoint
        subscription += alarmHistoryRepo.testHeader()
            // When there is no Internet or Server is down, retry 3 times, then handle error
            .retryWhen { error ->
                error.zipWith(Flowable.range(1, 4)) { err: Throwable, cnt: Int -> Pair(err, cnt) }
                    .flatMap { (throwable, count) ->
                        info("onClickToggle() -> retryWhen -> error: $throwable")
                        if (count < 4) {
                            when (throwable) {
                                is ConnectivityInterceptor.OfflineException -> {
                                    Flowable.timer(3, TimeUnit.SECONDS)
                                }
                                is SocketTimeoutException -> {
                                    Flowable.timer(1, TimeUnit.SECONDS)
                                }
                                else -> {
                                    Flowable.error(throwable)
                                }
                            }
                        } else {
                            Flowable.error(throwable)
                        }
                    }
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onNext = { response ->
                    val message = response.message
                    info("onClickToggle() -> message: $message")
                },
                onError = { error ->
                    info("onClickToggle() -> error: $error")
                }
            )
    }

    // Update the dateString showing the date, called on onResume()
    fun updateDate() {
        setTargetDate()
    }

    fun clearSubscription() {
        subscription.clear()
    }

    //========= Functions accessible by View (DataBinding) ==========

    fun getTimeTxtVwVisibility(): LiveData<Boolean> = timeTxtVwVisibility
    fun getTimePickerVisibility(): LiveData<Boolean> = timePickerVisibility
    fun getTimeString(): LiveData<String> = timeString
    fun getAmpmString(): LiveData<String> = ampmString
    fun getDateString(): LiveData<String> = dateString
    fun getTodayTmrwString(): LiveData<String> = todayTmrwString
    fun getTextOffString(): LiveData<String> = textOffString

}
