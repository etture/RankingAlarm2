package com.ydly.rankingalarm2.data.local.alarm

import androidx.room.*
import androidx.room.OnConflictStrategy.IGNORE
import androidx.room.OnConflictStrategy.REPLACE
import io.reactivex.Flowable

@Dao
interface AlarmHistoryDao {

    @Query("SELECT * FROM alarmHistoryData")
    fun getAll(): Flowable<List<AlarmHistoryData>>

    @Insert(onConflict = IGNORE)
    fun insert(alarmHistory: AlarmHistoryData): Long

    @Update(onConflict = REPLACE)
    fun update(alarmHistory: AlarmHistoryData)

    @Query("DELETE FROM alarmHistoryData")
    fun deleteAll()

    @Delete
    fun delete(alarmHistory: AlarmHistoryData)

}