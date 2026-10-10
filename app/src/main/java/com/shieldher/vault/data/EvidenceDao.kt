package com.shieldher.vault.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface EvidenceDao {
    @Insert
    suspend fun insert(evidence: EvidenceEntity)

    @Query("SELECT * FROM evidence WHERE deleted = 0 ORDER BY receiptTimeEpochMillis DESC")
    fun getAll(): Flow<List<EvidenceEntity>>

    @Query("SELECT * FROM evidence WHERE id = :id")
    suspend fun getById(id: String): EvidenceEntity?

    @Query("SELECT * FROM evidence WHERE parentId = :parentId")
    suspend fun getDerived(parentId: String): List<EvidenceEntity>

    @Query("UPDATE evidence SET deleted = 1 WHERE id = :id")
    suspend fun markDeleted(id: String)

    @Query("DELETE FROM evidence WHERE id = :id")
    suspend fun hardDelete(id: String)

}
