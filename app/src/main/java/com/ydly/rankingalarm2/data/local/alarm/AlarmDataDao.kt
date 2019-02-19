package com.ydly.rankingalarm2.data.local.alarm

import androidx.room.*
import androidx.room.OnConflictStrategy.IGNORE
import androidx.room.OnConflictStrategy.REPLACE
import io.reactivex.Flowable

@Dao
interface AlarmDataDao {

    @Query("SELECT * FROM alarmData")
    fun getAll(): Flowable<List<AlarmData>>

    @Insert(onConflict = IGNORE)
    fun insert(alarmData: AlarmData): Long

    @Update(onConflict = REPLACE)
    fun update(alarmData: AlarmData)

    @Query("UPDATE alarmData SET timeInMillis = timeInMillis + :oneDayTimeInMillis, isToggledOn = :isToggledOn WHERE id IN (:ids)")
    fun updateDatesToNextDay(ids: List<Long>, oneDayTimeInMillis: Long = 86400000, isToggledOn: Boolean = false)

    @Query("DELETE FROM alarmData")
    fun deleteAll()

    @Delete
    fun delete(alarmData: AlarmData)

}
