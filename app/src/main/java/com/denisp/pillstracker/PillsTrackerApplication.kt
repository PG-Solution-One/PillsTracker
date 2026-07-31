package com.denisp.pillstracker

import android.app.Application
import com.denisp.pillstracker.data.TrackerRepository
import com.denisp.pillstracker.data.local.TrackerDatabase
import com.denisp.pillstracker.notifications.NotificationScheduler

class PillsTrackerApplication : Application() {
    lateinit var repository: TrackerRepository
        private set

    lateinit var notificationScheduler: NotificationScheduler
        private set

    override fun onCreate() {
        super.onCreate()
        repository = TrackerRepository(TrackerDatabase(this))
        notificationScheduler = NotificationScheduler(this, repository)
        notificationScheduler.createChannels()
        notificationScheduler.rescheduleAll(resetExisting = false)
    }
}
