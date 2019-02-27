package com.ydly.rankingalarm2.data.local.alarm.dao

import androidx.room.*
import androidx.room.OnConflictStrategy.IGNORE
import androidx.room.OnConflictStrategy.REPLACE
import com.ydly.rankingalarm2.data.local.alarm.model.AlarmHistoryData
import io.reactivex.Flowable

@Dao
interface AlarmHistoryDao {

    @Query("SELECT * FROM alarmHistoryData")
    fun getAll(): Flowable<List<AlarmHistoryData>>

    @Query("SELECT * FROM alarmHistoryData WHERE year = :year AND month = :month AND dayOfMonth = :dayOfMonth LIMIT 1")
    fun getToday(year: Int, month: Int, dayOfMonth: Int): Flowable<List<AlarmHistoryData>>

    @Insert(onConflict = IGNORE)
    fun insert(alarmHistory: AlarmHistoryData): Long

    @Update(onConflict = REPLACE)
    fun update(alarmHistory: AlarmHistoryData)

    @Query("UPDATE alarmHistoryData SET dayRank = :dayRank AND morningRank = :morningRank WHERE id = :originalId")
    fun updateRank(originalId: Long, dayRank: Int, morningRank: Int)



    @Query("DELETE FROM alarmHistoryData")
    fun deleteAll()

    @Delete
    fun delete(alarmHistory: AlarmHistoryData)

}