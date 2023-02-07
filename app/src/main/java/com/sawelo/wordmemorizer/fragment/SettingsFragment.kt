package com.sawelo.wordmemorizer.fragment

import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Bundle
import androidx.core.content.edit
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference
import com.sawelo.wordmemorizer.R
import com.sawelo.wordmemorizer.activity.SettingsActivity
import com.sawelo.wordmemorizer.dataStore
import com.sawelo.wordmemorizer.util.SettingsProcess
import com.sawelo.wordmemorizer.util.SettingsUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SettingsFragment : PreferenceFragmentCompat(), OnSharedPreferenceChangeListener {
    @Inject
    lateinit var sharedPreferences: SharedPreferences

    @Inject
    lateinit var settingsUtils: SettingsUtils

    lateinit var floatingBubbleSwitchView: SwitchPreference
    lateinit var drawCharacterSwitchView: SwitchPreference
    lateinit var translateSwitchView: SwitchPreference

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preference_settings, rootKey)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                settingsUtils.checkAllSettings()
            }
        }

        floatingBubbleSwitchView =
            preferenceScreen.findPreference(SettingsSwitch.FloatingBubbleSwitch.switchKey)!!
        drawCharacterSwitchView =
            preferenceScreen.findPreference(SettingsSwitch.DrawSwitch.switchKey)!!
        translateSwitchView =
            preferenceScreen.findPreference(SettingsSwitch.TranslationSwitch.switchKey)!!

        sharedPreferences.registerOnSharedPreferenceChangeListener(this)
        observeProcess()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        lifecycleScope.launch {
            when (key) {
                floatingBubbleSwitchView.key -> if (SettingsSwitch.FloatingBubbleSwitch.isChecked)
                    settingsUtils.checkPermissionForFloatingBubble()
                drawCharacterSwitchView.key -> if (SettingsSwitch.DrawSwitch.isChecked)
                    settingsUtils.checkDownloadForDrawDigitalInk()
                translateSwitchView.key -> if (SettingsSwitch.TranslationSwitch.isChecked)
                    settingsUtils.checkDownloadForTranslator()
            }
        }
    }

    private fun observeProcess() {
        lifecycleScope.launch {
            context?.dataStore?.data?.collectLatest { preferences ->
                SettingsProcess.PreparingDownloadProcess.getCurrentProcess(preferences)
                    .also { isLoading ->
                        preferenceScreen.isEnabled = !isLoading
                        (activity as SettingsActivity).setProgress(isLoading)
                    }

                println("CURRENTLY ${SettingsSwitch.DrawSwitch.isChecked}")

                floatingBubbleSwitchView.isEnabled =
                    !SettingsProcess.FloatingBubbleSetUp.getCurrentProcess(preferences)
                drawCharacterSwitchView.isEnabled =
                    !SettingsProcess.DrawDownload.getCurrentProcess(preferences)
                translateSwitchView.isEnabled =
                    !SettingsProcess.TranslationDownload.getCurrentProcess(preferences)

                floatingBubbleSwitchView.isChecked =
                    SettingsSwitch.FloatingBubbleSwitch.isChecked
                drawCharacterSwitchView.isChecked =
                    SettingsSwitch.DrawSwitch.isChecked
                translateSwitchView.isChecked =
                    SettingsSwitch.TranslationSwitch.isChecked

                println("CURRENTLY AGAIN ${SettingsSwitch.DrawSwitch.isChecked}")
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        SettingsProcess.PreparingDownloadProcess.setCurrentProcess(false)
    }

    private fun SettingsProcess.setCurrentProcess(boolean: Boolean) {
        CoroutineScope(Dispatchers.IO).launch {
            context?.dataStore?.edit { preferences ->
                preferences[booleanPreferencesKey(processKey)] = boolean
            }
        }
    }

    private fun SettingsProcess.getCurrentProcess(preferences: Preferences): Boolean {
        return preferences[booleanPreferencesKey(processKey)] ?: false
    }

    private var SettingsSwitch.isChecked: Boolean
        get() = sharedPreferences.getBoolean(switchKey, false)
        set(value) = sharedPreferences.edit { putBoolean(switchKey, value) }
}

enum class SettingsSwitch(val switchKey: String) {
    FloatingBubbleSwitch("SETTINGS_PREFERENCE_FLOATING_BUBBLE_KEY"),
    TranslationSwitch("SETTINGS_PREFERENCE_OFFLINE_TRANSLATION_KEY"),
    DrawSwitch("SETTINGS_PREFERENCE_DRAW_CHARACTER_KEY")
}