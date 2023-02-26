package com.sawelo.wordmemorizer.service.notification.floating_bubble_action

import androidx.lifecycle.LifecycleService
import com.sawelo.wordmemorizer.data.preferences.settings.SettingsSwitch
import com.sawelo.wordmemorizer.data.preferences.window.FloatingBubbleProcess
import com.sawelo.wordmemorizer.data.repository.PreferenceRepository

class FloatingBubbleStopAction(
    lifecycleService: LifecycleService,
    private val preferenceRepository: PreferenceRepository,
) : BaseFloatingBubbleAction(lifecycleService) {

    override suspend fun setNotification() {}

    override suspend fun setCurrentProcess() {
        preferenceRepository.setCurrentSwitch(SettingsSwitch.FloatingBubbleSwitch, false)
        preferenceRepository.setCurrentProcess(FloatingBubbleProcess.IsRunning, false)
        preferenceRepository.setCurrentProcess(FloatingBubbleProcess.IsUnwrapped, false)
        preferenceRepository.setCurrentProcess(FloatingBubbleProcess.IsVisible, false)
    }
}