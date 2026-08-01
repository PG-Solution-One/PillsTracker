package com.denisp.pillstracker.application

import com.denisp.pillstracker.model.Medicine

interface IntakeNotificationGateway {
    fun showLowStockNotifications(medicines: List<Medicine>)
    fun cancelFollowUps(scheduledAt: Long)
    fun dismissDoseNotification(scheduledAt: Long)
}
