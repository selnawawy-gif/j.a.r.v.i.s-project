package com.example.data

import kotlinx.coroutines.flow.Flow

class TaskRepository(private val taskDao: JarvisTaskDao) {
    val allTasks: Flow<List<JarvisTask>> = taskDao.getAllTasks()
    val pendingTasks: Flow<List<JarvisTask>> = taskDao.getPendingTasks()
    val pendingCount: Flow<Int> = taskDao.getPendingCount()

    suspend fun insert(task: JarvisTask): Long = taskDao.insertTask(task)

    suspend fun update(task: JarvisTask) = taskDao.updateTask(task)

    suspend fun delete(task: JarvisTask) = taskDao.deleteTask(task)

    suspend fun deleteById(id: Long) = taskDao.deleteTaskById(id)

    suspend fun setCompleted(id: Long, completed: Boolean) = taskDao.setTaskCompleted(id, completed)
}
