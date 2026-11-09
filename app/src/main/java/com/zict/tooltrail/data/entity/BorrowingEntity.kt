
package com.zict.tooltrail.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "borrowings",
    foreignKeys = [
        ForeignKey(
            entity = EquipmentEntity::class,
            parentColumns = ["id"],
            childColumns = ["equipmentId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index(value = ["equipmentId"])]
)
data class BorrowingEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val equipmentId: Long,
    val borrowerName: String,
    val borrowerContact: String,
    val borrowedAt: Long = System.currentTimeMillis(),
    val dueDate: Long,
    val returnedAt: Long? = null
)
