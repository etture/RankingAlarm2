package com.ydly.rankingalarm2.util

const val BASE_URL = "placeholder sample base URL"
// TODO create server for Retrofit interaction

const val CREATE_ALARM_ACTIVITY = 21
const val EDIT_ALARM_ACTIVITY = 23
const val ALARM_FRAGMENT_PARENT_ACTIVITY = 25

const val EDIT_ALARM = 51
const val DELETE_ALARM = 53
const val NO_CHANGE_ALARM = 55

// Used in SingleAlarmViewModel to denote whether alarm was activated or deactivated
const val ACTIVATE = true
const val DEACTIVATE = false

// Used in SingleAlarmViewModel to denote whether date is being set to today or tomorrow
const val TODAY = 61
const val TOMORROW = 63

// Used in RingAlarmActivity and RingAlarmViewModel to indicate whether alarm was properly turned off or not
const val WOKE_UP = true
const val SLEPT_IN = false
