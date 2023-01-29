package com.sawelo.wordmemorizer.fragment

import android.content.SharedPreferences
import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.sawelo.wordmemorizer.R
import com.sawelo.wordmemorizer.util.Constants.PREFERENCE_DRAW_CHARACTER_KEY
import com.sawelo.wordmemorizer.util.Constants.PREFERENCE_FLOATING_BUBBLE_KEY
import com.sawelo.wordmemorizer.util.Constants.PREFERENCE_OFFLINE_TRANSLATION_KEY
import com.sawelo.wordmemorizer.util.SettingsUtils

class SettingsFragment : PreferenceFragmentCompat(),
    SharedPreferences.OnSharedPreferenceChangeListener {
    private var sharedPreferences: SharedPreferences? = null
    private var settingsUtils: SettingsUtils? = null

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preference_settings, rootKey)
        settingsUtils = activity?.let { SettingsUtils(it) }
        sharedPreferences = context?.let { PreferenceManager.getDefaultSharedPreferences(it) }
        sharedPreferences?.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onResume() {
        super.onResume()
        preferenceScreen = null
        addPreferencesFromResource(R.xml.preference_settings)
    }

    override fun onSharedPreferenceChanged(sharedPref: SharedPreferences, key: String) {
        when (key) {
            PREFERENCE_FLOATING_BUBBLE_KEY -> settingsUtils?.checkPermissionForFloatingBubble()
            PREFERENCE_DRAW_CHARACTER_KEY -> settingsUtils?.checkDownloadForDrawDigitalInk()
            PREFERENCE_OFFLINE_TRANSLATION_KEY -> settingsUtils?.checkDownloadForTranslator()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        sharedPreferences = null
        settingsUtils = null
    }

}