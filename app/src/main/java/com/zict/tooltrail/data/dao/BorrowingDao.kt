package com.zict.tooltrail.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.zict.tooltrail.data.entity.BorrowingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BorrowingDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertBorrowing(borrowing: BorrowingEntity): Long

    @Update
    suspend fun updateBorrowing(borrowing: BorrowingEntity)

    @Query("SELECT * FROM borrowings WHERE id = :borrowingId LIMIT 1")
    suspend fun getBorrowingById(borrowingId: Long): BorrowingEntity?

    @Query("""
        SELECT * FROM borrowings
        WHERE returnedAt IS NULL
        ORDER BY dueDate ASC
    """)
    fun getActiveBorrowings(): Flow<List<BorrowingEntity>>

    @Query("""
        SELECT * FROM borrowings
        WHERE returnedAt IS NULL AND dueDate < :currentTime
        ORDER BY dueDate ASC
    """)
    fun getOverdueBorrowings(currentTime: Long): Flow<List<BorrowingEntity>>

    @Query("""
        SELECT * FROM borrowings
        WHERE returnedAt IS NOT NULL
        ORDER BY returnedAt DESC
    """)
    fun getBorrowingHistory(): Flow<List<BorrowingEntity>>

    @Query("SELECT equipmentId FROM borrowings WHERE returnedAt IS NULL")
    fun getActiveEquipmentIds(): Flow<List<Long>>

    @Query("""
        SELECT COUNT(*) FROM borrowings
        WHERE equipmentId = :equipmentId AND returnedAt IS NULL
    """)
    suspend fun countActiveBorrowings(equipmentId: Long): Int
}
