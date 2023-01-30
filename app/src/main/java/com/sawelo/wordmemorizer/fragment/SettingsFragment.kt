package com.sawelo.wordmemorizer.fragment

import android.content.SharedPreferences
import android.os.Bundle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import androidx.preference.SwitchPreference
import com.sawelo.wordmemorizer.R
import com.sawelo.wordmemorizer.util.Constants
import com.sawelo.wordmemorizer.util.Constants.PREFERENCE_DRAW_CHARACTER_KEY
import com.sawelo.wordmemorizer.util.Constants.PREFERENCE_FLOATING_BUBBLE_KEY
import com.sawelo.wordmemorizer.util.Constants.PREFERENCE_OFFLINE_TRANSLATION_KEY
import com.sawelo.wordmemorizer.util.SettingsUtils
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SettingsFragment : PreferenceFragmentCompat(),
    SharedPreferences.OnSharedPreferenceChangeListener {
    private var sharedPreferences: SharedPreferences? = null
    private var settingsUtils: SettingsUtils? = null

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preference_settings, rootKey)
        settingsUtils = activity?.let { SettingsUtils(it) }
        sharedPreferences = context?.let { PreferenceManager.getDefaultSharedPreferences(it) }
        sharedPreferences?.registerOnSharedPreferenceChangeListener(this)
        setPreferenceEnabled()
    }

    override fun onSharedPreferenceChanged(sharedPref: SharedPreferences, key: String) {
        when (key) {
            PREFERENCE_FLOATING_BUBBLE_KEY -> settingsUtils?.checkPermissionForFloatingBubble()
            PREFERENCE_DRAW_CHARACTER_KEY -> settingsUtils?.checkDownloadForDrawDigitalInk()
            PREFERENCE_OFFLINE_TRANSLATION_KEY -> settingsUtils?.checkDownloadForTranslator()
        }
    }

    private fun setPreferenceEnabled() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                preferenceScreen = null
                addPreferencesFromResource(R.xml.preference_settings)
                launch {
                    Constants.isDrawDigitalInkDownloadingFlow.collectLatest { isDownloading ->
                        preferenceScreen?.findPreference<SwitchPreference>(PREFERENCE_DRAW_CHARACTER_KEY)
                            ?.isEnabled = !isDownloading
                    }
                }
                launch {
                    Constants.isOfflineTranslationDownloadingFlow.collectLatest { isDownloading ->
                        preferenceScreen?.findPreference<SwitchPreference>(PREFERENCE_OFFLINE_TRANSLATION_KEY)
                            ?.isEnabled = !isDownloading
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        sharedPreferences = null
        settingsUtils = null
    }

}