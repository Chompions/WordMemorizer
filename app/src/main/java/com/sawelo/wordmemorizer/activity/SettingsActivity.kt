package com.sawelo.wordmemorizer.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import com.sawelo.wordmemorizer.R
import com.sawelo.wordmemorizer.databinding.ActivitySettingsBinding
import com.sawelo.wordmemorizer.fragment.SettingsFragment
import com.sawelo.wordmemorizer.util.Constants
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.activitySettingsToolbar.setNavigationOnClickListener {
            finish()
        }

        supportFragmentManager.commit {
            replace<SettingsFragment>(R.id.activitySettings_fcv, Constants.SETTINGS_FRAGMENT_TAG)
        }
    }
}