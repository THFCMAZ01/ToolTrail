
package com.zict.tooltrail.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.zict.tooltrail.data.entity.EquipmentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EquipmentDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertEquipment(equipment: EquipmentEntity): Long

    @Update
    suspend fun updateEquipment(equipment: EquipmentEntity)

    @Delete
    suspend fun deleteEquipment(equipment: EquipmentEntity)

    @Query("SELECT * FROM equipment ORDER BY name COLLATE NOCASE ASC")
    fun getAllEquipment(): Flow<List<EquipmentEntity>>

    @Query("SELECT * FROM equipment WHERE id = :equipmentId LIMIT 1")
    suspend fun getEquipmentById(equipmentId: Long): EquipmentEntity?
}
