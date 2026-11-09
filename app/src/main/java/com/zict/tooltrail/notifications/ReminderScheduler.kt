package com.zict.tooltrail.notifications

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import java.util.concurrent.TimeUnit

object ReminderScheduler {

    private fun workName(borrowingId: Long) = "reminder_$borrowingId"

    fun schedule(
        context: Context,
        borrowingId: Long,
        equipmentName: String,
        borrowerName: String,
        dueDate: Long
    ) {
        val delay = (dueDate - System.currentTimeMillis()).coerceAtLeast(0L)

        val request = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(
                workDataOf(
                    ReminderWorker.KEY_BORROWING_ID to borrowingId,
                    ReminderWorker.KEY_EQUIPMENT_NAME to equipmentName,
                    ReminderWorker.KEY_BORROWER_NAME to borrowerName
                )
            )
            .build()

        WorkManager.getInstance(context.applicationContext)
            .enqueueUniqueWork(workName(borrowingId), ExistingWorkPolicy.REPLACE, request)
    }

    fun cancel(context: Context, borrowingId: Long) {
        WorkManager.getInstance(context.applicationContext)
            .cancelUniqueWork(workName(borrowingId))
    }
}
