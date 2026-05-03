package com.filiht.studyplanner.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import com.filiht.studyplanner.model.StudyTask
import java.text.SimpleDateFormat
import java.util.*

class StudyAlarmScheduler(private val context: Context) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun schedule(task: StudyTask) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                // Should handle permission request in UI, but as a safeguard:
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                context.startActivity(intent)
                return
            }
        }

        val calendar = getCalendarForTask(task)
        
        // If the time has already passed today/this week, schedule for next week
        if (calendar.timeInMillis <= System.currentTimeMillis()) {
            calendar.add(Calendar.WEEK_OF_YEAR, 1)
        }

        val intent = Intent(context, StudyNotificationReceiver::class.java).apply {
            putExtra("TASK_ID", task.id)
            putExtra("SUBJECT", task.subjectName)
            putExtra("TOPIC", task.topic)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            task.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            pendingIntent
        )
    }

    fun cancel(taskId: Int) {
        val intent = Intent(context, StudyNotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            taskId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    private fun getCalendarForTask(task: StudyTask): Calendar {
        val calendar = Calendar.getInstance()
        
        // Parse time (e.g., "09:00 AM")
        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val date = timeFormat.parse(task.time) ?: Date()
        val timeCalendar = Calendar.getInstance().apply { time = date }

        calendar.set(Calendar.HOUR_OF_DAY, timeCalendar.get(Calendar.HOUR_OF_DAY))
        calendar.set(Calendar.MINUTE, timeCalendar.get(Calendar.MINUTE))
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        // Map day string to Calendar day
        val dayOfWeek = when (task.day.lowercase()) {
            "monday" -> Calendar.MONDAY
            "tuesday" -> Calendar.TUESDAY
            "wednesday" -> Calendar.WEDNESDAY
            "thursday" -> Calendar.THURSDAY
            "friday" -> Calendar.FRIDAY
            "saturday" -> Calendar.SATURDAY
            "sunday" -> Calendar.SUNDAY
            else -> calendar.get(Calendar.DAY_OF_WEEK)
        }

        calendar.set(Calendar.DAY_OF_WEEK, dayOfWeek)
        
        return calendar
    }
}
