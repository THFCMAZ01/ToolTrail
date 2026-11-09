package com.zict.tooltrail.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.zict.tooltrail.data.database.ToolTrailDatabase
import com.zict.tooltrail.data.entity.BorrowingEntity
import com.zict.tooltrail.data.entity.EquipmentEntity
import com.zict.tooltrail.data.repository.ToolTrailRepository
import com.zict.tooltrail.notifications.ReminderScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ToolTrailViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ToolTrailRepository

    init {
        val db = ToolTrailDatabase.getDatabase(application)
        repository = ToolTrailRepository(db.equipmentDao(), db.borrowingDao())
    }

    val equipment: StateFlow<List<EquipmentEntity>> = repository.getAllEquipment()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeBorrowings: StateFlow<List<BorrowingEntity>> = repository.getActiveBorrowings()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val history: StateFlow<List<BorrowingEntity>> = repository.getBorrowingHistory()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeEquipmentIds: StateFlow<List<Long>> = repository.getActiveEquipmentIds()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message

    fun clearMessage() {
        _message.value = null
    }

    fun addEquipment(
        name: String,
        category: String,
        description: String,
        condition: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            try {
                repository.addEquipment(
                    EquipmentEntity(
                        name = name.trim(),
                        category = category.trim(),
                        description = description.trim(),
                        condition = condition.trim().ifBlank { "Good" }
                    )
                )
                onSuccess()
            } catch (e: IllegalArgumentException) {
                _message.value = e.message
            }
        }
    }

    fun borrow(
        equipmentId: Long,
        borrowerName: String,
        borrowerContact: String,
        hoursUntilDue: Double,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            try {
                val due = System.currentTimeMillis() + (hoursUntilDue * 3_600_000).toLong()
                val borrowingId =
                    repository.borrowEquipment(equipmentId, borrowerName, borrowerContact, due)

                val equipmentName = repository.getEquipmentById(equipmentId)?.name ?: "Equipment"
                ReminderScheduler.schedule(
                    getApplication(), borrowingId, equipmentName, borrowerName.trim(), due
                )
                onSuccess()
            } catch (e: IllegalArgumentException) {
                _message.value = e.message
            }
        }
    }

    fun returnEquipment(borrowingId: Long) {
        viewModelScope.launch {
            try {
                repository.returnEquipment(borrowingId)
                ReminderScheduler.cancel(getApplication(), borrowingId)
            } catch (e: IllegalArgumentException) {
                _message.value = e.message
            }
        }
    }
}
