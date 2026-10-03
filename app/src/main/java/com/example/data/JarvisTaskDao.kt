package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface JarvisTaskDao {
    @Query("SELECT * FROM jarvis_tasks ORDER BY isCompleted ASC, CASE priority WHEN 'OMEGA' THEN 1 WHEN 'ALPHA' THEN 2 WHEN 'BETA' THEN 3 ELSE 4 END, id DESC")
    fun getAllTasks(): Flow<List<JarvisTask>>

    @Query("SELECT * FROM jarvis_tasks WHERE isCompleted = 0 ORDER BY CASE priority WHEN 'OMEGA' THEN 1 WHEN 'ALPHA' THEN 2 WHEN 'BETA' THEN 3 ELSE 4 END, id DESC")
    fun getPendingTasks(): Flow<List<JarvisTask>>

    @Query("SELECT COUNT(*) FROM jarvis_tasks WHERE isCompleted = 0")
    fun getPendingCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: JarvisTask): Long

    @Update
    suspend fun updateTask(task: JarvisTask)

    @Delete
    suspend fun deleteTask(task: JarvisTask)

    @Query("DELETE FROM jarvis_tasks WHERE id = :id")
    suspend fun deleteTaskById(id: Long)

    @Query("UPDATE jarvis_tasks SET isCompleted = :completed WHERE id = :id")
    suspend fun setTaskCompleted(id: Long, completed: Boolean)
}
