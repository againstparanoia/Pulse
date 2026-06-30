package com.pulse.app.data.repository

import com.pulse.app.data.db.ChoreDao
import com.pulse.app.data.model.Chore
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChoreRepository @Inject constructor(private val choreDao: ChoreDao) {
    fun getActiveChores(): Flow<List<Chore>> = choreDao.getActiveChores()
    suspend fun addChore(chore: Chore): Long = choreDao.insert(chore)
    suspend fun deleteChore(id: Int) = choreDao.deleteById(id)
    suspend fun markCompleted(id: Int, completedAt: Long = System.currentTimeMillis()) =
        choreDao.markCompleted(id, completedAt)
}
