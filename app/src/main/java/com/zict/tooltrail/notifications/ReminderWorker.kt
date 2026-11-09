package com.zict.tooltrail.notifications

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.zict.tooltrail.MainActivity
import com.zict.tooltrail.data.database.ToolTrailDatabase

class ReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val borrowingId = inputData.getLong(KEY_BORROWING_ID, -1L)
        val equipmentName = inputData.getString(KEY_EQUIPMENT_NAME) ?: return Result.failure()
        val borrowerName = inputData.getString(KEY_BORROWER_NAME) ?: return Result.failure()

        // Do nothing if the item was already returned.
        val borrowing = ToolTrailDatabase.getDatabase(applicationContext)
            .borrowingDao()
            .getBorrowingById(borrowingId)
        if (borrowing == null || borrowing.returnedAt != null) {
            return Result.success()
        }

        showNotification(borrowingId, equipmentName, borrowerName)
        return Result.success()
    }

    @SuppressLint("MissingPermission")
    private fun showNotification(borrowingId: Long, equipmentName: String, borrowerName: String) {
        val context = applicationContext

        // On Android 13+ the user must grant the notification permission.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Return reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            context.getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
        }

        val openApp = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Equipment return due")
            .setContentText("$equipmentName borrowed by $borrowerName is due now.")
            .setContentIntent(openApp)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(borrowingId.toInt(), notification)
    }

    companion object {
        const val CHANNEL_ID = "return_reminders"
        const val KEY_BORROWING_ID = "borrowing_id"
        const val KEY_EQUIPMENT_NAME = "equipment_name"
        const val KEY_BORROWER_NAME = "borrower_name"
    }
}
