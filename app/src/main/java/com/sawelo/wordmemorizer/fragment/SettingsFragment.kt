package com.sawelo.wordmemorizer.fragment

import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Bundle
import android.util.Log
import androidx.core.content.edit
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference
import com.sawelo.wordmemorizer.R
import com.sawelo.wordmemorizer.activity.SettingsActivity
import com.sawelo.wordmemorizer.dataStore
import com.sawelo.wordmemorizer.util.SettingsUtils
import com.sawelo.wordmemorizer.util.ViewUtils.showToast
import com.sawelo.wordmemorizer.util.enum_class.SettingsProcess
import com.sawelo.wordmemorizer.util.enum_class.SettingsSwitch
import com.sawelo.wordmemorizer.viewmodel.MainViewModel
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

    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var floatingBubbleSwitchView: SwitchPreference
    private lateinit var drawCharacterSwitchView: SwitchPreference
    private lateinit var translateSwitchView: SwitchPreference

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preference_settings, rootKey)

        lifecycleScope.launch {
            settingsUtils.checkAllSettings()
        }

        floatingBubbleSwitchView =
            preferenceScreen.findPreference(SettingsSwitch.FloatingBubbleSwitch.switchKey)!!
        drawCharacterSwitchView =
            preferenceScreen.findPreference(SettingsSwitch.DrawSwitch.switchKey)!!
        translateSwitchView =
            preferenceScreen.findPreference(SettingsSwitch.TranslationSwitch.switchKey)!!

        preferenceScreen.findPreference<Preference>("SETTINGS_PREFERENCE_RESET_REMEMBER_COUNT_KEY")
            ?.setOnPreferenceClickListener {
                viewModel.updateResetAllForgotCount()
                true
            }

        sharedPreferences.registerOnSharedPreferenceChangeListener(this)
        observeProcess()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        lifecycleScope.launch {
            try {
                when (key) {
                    floatingBubbleSwitchView.key -> settingsUtils.checkPermissionForFloatingBubble()
                    drawCharacterSwitchView.key -> settingsUtils.checkDownloadForDrawDigitalInk()
                    translateSwitchView.key -> settingsUtils.checkDownloadForTranslator()
                }
            } catch (e: Exception) {
                context?.showToast("Something went wrong")
                Log.e(TAG, "Exception on preference change: $e")
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

    companion object {
        const val TAG = "SettingsFragment"
    }
}