package com.sawelo.wordmemorizer.ui.fragment

import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import androidx.preference.SwitchPreference
import com.sawelo.wordmemorizer.R
import com.sawelo.wordmemorizer.data.preferences.settings.SettingsProcess
import com.sawelo.wordmemorizer.data.preferences.settings.SettingsSwitch
import com.sawelo.wordmemorizer.data.repository.PreferenceRepository
import com.sawelo.wordmemorizer.dataStore
import com.sawelo.wordmemorizer.ui.activity.SettingsActivity
import com.sawelo.wordmemorizer.ui.ui_util.SettingsUtil
import com.sawelo.wordmemorizer.ui.ui_util.ViewUtils.showToast
import com.sawelo.wordmemorizer.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@AndroidEntryPoint
class SettingsFragment : PreferenceFragmentCompat(), OnSharedPreferenceChangeListener {

    @Inject
    lateinit var preferenceRepository: PreferenceRepository

    @Inject
    lateinit var settingsUtil: SettingsUtil

    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var floatingBubbleSwitchView: SwitchPreference
    private lateinit var drawCharacterSwitchView: SwitchPreference
    private lateinit var translateSwitchView: SwitchPreference

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preference_settings, rootKey)

        /**
         * Do not call floating bubble check in here,
         * it risks ForegroundServiceDidNotStartInTimeException
         */
        lifecycleScope.launch {
            preferenceScreen.isEnabled = false
            settingsUtil.checkDownloadForDrawDigitalInk()
            settingsUtil.checkDownloadForTranslator()
            preferenceScreen.isEnabled = true
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

        PreferenceManager.getDefaultSharedPreferences(requireContext())
            .registerOnSharedPreferenceChangeListener(this)
        observeProcess()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        lifecycleScope.launch {
            try {
                when (key) {
                    floatingBubbleSwitchView.key -> settingsUtil.checkPermissionForFloatingBubble()
                    drawCharacterSwitchView.key -> settingsUtil.checkDownloadForDrawDigitalInk()
                    translateSwitchView.key -> settingsUtil.checkDownloadForTranslator()
                }
            } catch (e: Exception) {
                context?.showToast("Something went wrong")
                Log.e(TAG, "Exception on preference change: $e")
            }
        }
    }

    private fun observeProcess() {
        lifecycleScope.launch {
            context?.dataStore?.data?.collectLatest {
                preferenceRepository.getCurrentProcessSnapshot(SettingsProcess.PrepareSetupProcess)
                    .also { isLoading ->
                        preferenceScreen.isEnabled = !isLoading
                        (activity as SettingsActivity).setProgress(isLoading)
                }

                floatingBubbleSwitchView.isChecked =
                    preferenceRepository.getCurrentSwitch(SettingsSwitch.FloatingBubbleSwitch)
                drawCharacterSwitchView.isChecked =
                    preferenceRepository.getCurrentSwitch(SettingsSwitch.DrawSwitch)
                translateSwitchView.isChecked =
                    preferenceRepository.getCurrentSwitch(SettingsSwitch.TranslationSwitch)

                drawCharacterSwitchView.isEnabled =
                    !preferenceRepository.getCurrentProcessSnapshot(SettingsProcess.DrawDownload)
                translateSwitchView.isEnabled =
                    !preferenceRepository.getCurrentProcessSnapshot(SettingsProcess.TranslationDownload)

            }
        }
    }

    override fun onDestroy() {
        runBlocking {
            preferenceRepository.setCurrentProcess(
                SettingsProcess.PrepareSetupProcess, false
            )
        }
        super.onDestroy()
    }

    companion object {
        const val TAG = "SettingsFragment"
    }
}