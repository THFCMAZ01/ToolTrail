
package com.zict.tooltrail.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.zict.tooltrail.data.dao.BorrowingDao
import com.zict.tooltrail.data.dao.EquipmentDao
import com.zict.tooltrail.data.entity.BorrowingEntity
import com.zict.tooltrail.data.entity.EquipmentEntity

@Database(
    entities = [
        EquipmentEntity::class,
        BorrowingEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class ToolTrailDatabase : RoomDatabase() {

    abstract fun equipmentDao(): EquipmentDao

    abstract fun borrowingDao(): BorrowingDao

    companion object {
        @Volatile
        private var INSTANCE: ToolTrailDatabase? = null

        fun getDatabase(context: Context): ToolTrailDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ToolTrailDatabase::class.java,
                    "tooltrail_database"
                ).build()

                INSTANCE = instance
                instance
            }
        }
    }
}
