package com.sawelo.wordmemorizer.fragment

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.sawelo.wordmemorizer.R

class SettingsFragment: PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preference_settings, rootKey)
    }
}