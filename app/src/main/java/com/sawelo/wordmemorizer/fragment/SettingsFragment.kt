package com.sawelo.wordmemorizer.fragment

import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.sawelo.wordmemorizer.R
import com.sawelo.wordmemorizer.util.NotificationUtils
import com.sawelo.wordmemorizer.util.NotificationUtils.checkPermissionAndStartFloatingBubbleService

class SettingsFragment: PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {
    private var postNotificationPermissionLauncher: ActivityResultLauncher<String>? = null

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preference_settings, rootKey)
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
        sharedPreferences?.registerOnSharedPreferenceChangeListener(this)

        postNotificationPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
                if (!granted) activity?.finish()
            }
    }

    override fun onResume() {
        super.onResume()
        preferenceScreen = null
        addPreferencesFromResource(R.xml.preference_settings)
    }

    override fun onSharedPreferenceChanged(sharedPref: SharedPreferences, key: String) {
        if (key == NotificationUtils.PREFERENCE_FLOATING_BUBBLE_KEY) {
            activity?.checkPermissionAndStartFloatingBubbleService(sharedPref) {
                postNotificationPermissionLauncher?.launch(
                    android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}