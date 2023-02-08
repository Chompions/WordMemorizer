package com.sawelo.wordmemorizer.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.sawelo.wordmemorizer.BuildConfig
import com.sawelo.wordmemorizer.MainApplication
import com.sawelo.wordmemorizer.R
import com.sawelo.wordmemorizer.data.DatabaseHelper
import com.sawelo.wordmemorizer.data.data_class.Category
import com.sawelo.wordmemorizer.databinding.ActivityMainBinding
import com.sawelo.wordmemorizer.fragment.AddCategoryDialogFragment
import com.sawelo.wordmemorizer.fragment.HomeFragment
import com.sawelo.wordmemorizer.fragment.SortingSettingsDialogFragment
import com.sawelo.wordmemorizer.util.Constants.HOME_FRAGMENT_TAG
import com.sawelo.wordmemorizer.util.SettingsUtils
import com.sawelo.wordmemorizer.util.ViewUtils.showToast
import com.sawelo.wordmemorizer.util.WordUtils.isAll
import com.sawelo.wordmemorizer.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    @Inject
    lateinit var settingsUtils: SettingsUtils

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportFragmentManager.commit {
            replace<HomeFragment>(R.id.activityMain_fcv, HOME_FRAGMENT_TAG)
        }

        lifecycleScope.launch {
            settingsUtils.checkAllSettings()
        }

        setAds()
        setCategories()
        setNavigationListener()
    }

    override fun onResume() {
        super.onResume()
        checkForOtherPackageDb()
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

    private fun setCategories() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.getAllCategories().collectLatest { categories ->
                    val menu = binding.activityMainNavigationView.menu
                    menu.clear()
                    categories.forEachIndexed { index, it ->
                        menu.createTab(it, index)
                    }
                }
            }
        }
    }

    private fun setNavigationListener() {
        binding.activityMainToolbar.setNavigationOnClickListener {
            binding.activityMainDrawerLayout.open()
        }

        binding.activityMainNavigationView.setNavigationItemSelectedListener {
            binding.activityMainDrawerLayout.close()
            true
        }

        val header = binding.activityMainNavigationView.getHeaderView(0)
        header.findViewById<MaterialButton>(R.id.drawerNavigationHeader_addCategory_btn)
            .setOnClickListener {
                AddCategoryDialogFragment().show(supportFragmentManager, null)
            }

        binding.activityMainToolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.menuHome_reset -> {
                    viewModel.resetAllForgotCount()
                    true
                }
                R.id.menuHome_sort -> {
                    SortingSettingsDialogFragment().show(supportFragmentManager, null)
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

    private fun Menu.createTab(category: Category, position: Int) {
        val text = "${category.categoryName} (${category.wordCount})"
        add(Menu.NONE, category.categoryId, position, text)

        val item = this.findItem(category.categoryId)
        item.setClickListener(category)
        item.setActionView(R.layout.item_drawer_category)
        item.actionView?.findViewById<Button>(R.id.itemDrawer_btn)
            ?.setOnClickListener {
                viewModel.deleteCategory(category)
            }
        if (category.isAll()) {
            item.actionView?.findViewById<Button>(R.id.itemDrawer_btn)?.isVisible = false
        }
    }

    private fun MenuItem.setClickListener(category: Category) {
        setOnMenuItemClickListener {
            viewModel.currentCategory = category
            val homeFragment = supportFragmentManager
                .findFragmentByTag(HOME_FRAGMENT_TAG) as? HomeFragment
            homeFragment?.changeCurrentTab()

            binding.activityMainDrawerLayout.close()
            true
        }
    }

    companion object {
        var isPackageCheckDialogVisible = false
    }
}