
package com.zict.tooltrail.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "equipment")
data class EquipmentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val name: String,
    val category: String,
    val description: String = "",
    val condition: String = "Good",
    val dateAdded: Long = System.currentTimeMillis()
)
