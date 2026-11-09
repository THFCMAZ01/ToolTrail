package com.zict.tooltrail.data.repository

import com.zict.tooltrail.data.dao.BorrowingDao
import com.zict.tooltrail.data.dao.EquipmentDao
import com.zict.tooltrail.data.entity.BorrowingEntity
import com.zict.tooltrail.data.entity.EquipmentEntity
import kotlinx.coroutines.flow.Flow

class ToolTrailRepository(
    private val equipmentDao: EquipmentDao,
    private val borrowingDao: BorrowingDao
) {

    fun getAllEquipment(): Flow<List<EquipmentEntity>> {
        return equipmentDao.getAllEquipment()
    }

    suspend fun addEquipment(equipment: EquipmentEntity): Long {
        require(equipment.name.isNotBlank()) {
            "Equipment name cannot be empty."
        }

        require(equipment.category.isNotBlank()) {
            "Equipment category cannot be empty."
        }

        return equipmentDao.insertEquipment(equipment)
    }

    suspend fun getEquipmentById(id: Long): EquipmentEntity? {
        return equipmentDao.getEquipmentById(id)
    }

    fun getActiveBorrowings(): Flow<List<BorrowingEntity>> {
        return borrowingDao.getActiveBorrowings()
    }

    fun getOverdueBorrowings(): Flow<List<BorrowingEntity>> {
        return borrowingDao.getOverdueBorrowings(System.currentTimeMillis())
    }

    fun getBorrowingHistory(): Flow<List<BorrowingEntity>> {
        return borrowingDao.getBorrowingHistory()
    }

    fun getActiveEquipmentIds(): Flow<List<Long>> {
        return borrowingDao.getActiveEquipmentIds()
    }

    suspend fun borrowEquipment(
        equipmentId: Long,
        borrowerName: String,
        borrowerContact: String,
        dueDate: Long
    ): Long {

        require(borrowerName.isNotBlank()) {
            "Borrower name cannot be empty."
        }

        require(borrowerContact.isNotBlank()) {
            "Borrower contact cannot be empty."
        }

        require(dueDate > System.currentTimeMillis()) {
            "Return date must be in the future."
        }

        val equipment = equipmentDao.getEquipmentById(equipmentId)
            ?: throw IllegalArgumentException("Equipment not found.")

        val activeLoans = borrowingDao.countActiveBorrowings(equipmentId)

        require(activeLoans == 0) {
            "${equipment.name} is already borrowed."
        }

        return borrowingDao.insertBorrowing(
            BorrowingEntity(
                equipmentId = equipmentId,
                borrowerName = borrowerName.trim(),
                borrowerContact = borrowerContact.trim(),
                dueDate = dueDate
            )
        )
    }

    suspend fun returnEquipment(borrowingId: Long) {
        val borrowing = borrowingDao.getBorrowingById(borrowingId)
            ?: throw IllegalArgumentException("Borrowing record not found.")

        require(borrowing.returnedAt == null) {
            "This equipment has already been returned."
        }

        borrowingDao.updateBorrowing(
            borrowing.copy(returnedAt = System.currentTimeMillis())
        )
    }
}
