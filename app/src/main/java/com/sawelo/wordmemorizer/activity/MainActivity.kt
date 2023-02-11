package com.sawelo.wordmemorizer.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.sawelo.wordmemorizer.BuildConfig
import com.sawelo.wordmemorizer.MainApplication
import com.sawelo.wordmemorizer.R
import com.sawelo.wordmemorizer.data.database.DatabaseHelper
import com.sawelo.wordmemorizer.databinding.ActivityMainBinding
import com.sawelo.wordmemorizer.fragment.HomeFragment
import com.sawelo.wordmemorizer.fragment.MainBottomSheetFragment
import com.sawelo.wordmemorizer.service.NotificationFloatingBubbleService
import com.sawelo.wordmemorizer.util.Constants.HOME_FRAGMENT_TAG
import com.sawelo.wordmemorizer.util.SettingsUtils
import com.sawelo.wordmemorizer.util.ViewUtils.showToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    @Inject
    lateinit var settingsUtils: SettingsUtils
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportFragmentManager.commit {
            replace<HomeFragment>(R.id.activityMain_fcv, HOME_FRAGMENT_TAG)
        }

        lifecycleScope.launch {
            settingsUtils.checkAllSettings()
            NotificationFloatingBubbleService.wrapBubbleService(this@MainActivity)
        }

        setAds()
        setNavigationListener()
    }

    override fun onResume() {
        super.onResume()
        checkForOtherPackageDb()
        NotificationFloatingBubbleService.wrapBubbleService(this)
    }

    private val otherExportedDbLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode != Activity.RESULT_OK) {
            showToast("Failed to get result from other application")
            return@registerForActivityResult
        }
        val databaseHelper = DatabaseHelper(this)
        result.data?.clipData?.also { clipData ->
            for (i in 0 until clipData.itemCount) {
                databaseHelper.importDb(
                    databaseHelper.databaseNameList[i],
                    clipData.getItemAt(i).uri
                )
            }
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finishAffinity()
        }
    }

    private fun checkForOtherPackageDb() {
        if (MainApplication.isOtherPackageExistInNewInstall && !isPackageCheckDialogVisible) {
            val alertDialog = MaterialAlertDialogBuilder(this).apply {
                setTitle("Duplicate app detected")
                setMessage(
                    "You have already installed WordMemorizer before, " +
                            "would you like to copy your words into this application?"
                )
                setPositiveButton(R.string.ok) { dialog, _ ->
                    MainApplication.isOtherPackageExistInNewInstall = false
                    val backupIntent = Intent()
                    backupIntent.component = ComponentName(
                        MainApplication.OTHER_PACKAGE_NAME,
                        "com.sawelo.wordmemorizer.activity.BackupActivity"
                    )
                    backupIntent.action = MainApplication.ACTIVITY_OTHER_CREATE_BACKUP_DB
                    otherExportedDbLauncher.launch(backupIntent)
                    dialog.dismiss()
                }
                setNegativeButton(R.string.cancel) { dialog, _ ->
                    MainApplication.isOtherPackageExistInNewInstall = false
                    dialog.cancel()
                }
            }.create()
            alertDialog.show()
            isPackageCheckDialogVisible = true
        }
    }

    @Suppress("KotlinConstantConditions")
    @SuppressLint("VisibleForTests")
    private fun setAds() {
        try {
            if (BuildConfig.BUILD_TYPE != "cleanRelease" && BuildConfig.BUILD_TYPE != "cleanDebug") {
                MobileAds.initialize(this)
                val adRequest = AdRequest.Builder().build()
                binding.adView.loadAd(adRequest)
                binding.adView.isVisible = true
            } else {
                binding.adView.isVisible = false
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Exception in setting ad: ${e.message}")
        }
    }

    private fun setNavigationListener() {
        val mainBottomSheetFragment = MainBottomSheetFragment()

        binding.activityMainToolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.menuHome_filter -> {
                    mainBottomSheetFragment.show(supportFragmentManager, MainBottomSheetFragment.TAG)
                    true
                }
                R.id.menuHome_settings -> {
                    val intent = Intent(this, SettingsActivity::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }
    }

    companion object {
        var isPackageCheckDialogVisible = false
    }
}