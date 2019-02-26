package com.ydly.rankingalarm2.util;

import android.os.Parcelable;
import androidx.annotation.NonNull;
import com.ydly.rankingalarm2.data.local.alarm.model.AlarmData;

public class ParcelableCreator {
    @NonNull
    public static Parcelable.Creator<AlarmData> getAlarmDataCreator() {
        return AlarmData.CREATOR;
    }
}
