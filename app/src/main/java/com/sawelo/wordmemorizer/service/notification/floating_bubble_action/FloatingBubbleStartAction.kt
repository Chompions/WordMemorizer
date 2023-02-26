package com.sawelo.wordmemorizer.service.notification.floating_bubble_action

import androidx.lifecycle.LifecycleService
import com.sawelo.wordmemorizer.data.preferences.window.FloatingBubbleProcess
import com.sawelo.wordmemorizer.data.repository.PreferenceRepository
import com.sawelo.wordmemorizer.service.notification.FloatingBubbleNotification

class FloatingBubbleStartAction(
    private val lifecycleService: LifecycleService,
    private val preferenceRepository: PreferenceRepository,
) : BaseFloatingBubbleAction(lifecycleService) {

    override suspend fun setNotification() {
        FloatingBubbleNotification(lifecycleService, preferenceRepository).startForegroundNotification()
    }

    override suspend fun setCurrentProcess() {
        preferenceRepository.setCurrentProcess(FloatingBubbleProcess.IsRunning, true)
        preferenceRepository.setCurrentProcess(FloatingBubbleProcess.IsUnwrapped, true)
        preferenceRepository.setCurrentProcess(FloatingBubbleProcess.IsVisible, true)
    }
}