package com.sawelo.wordmemorizer.activity

import android.content.Intent
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Bundle
import android.view.Menu
import android.widget.Button
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.core.view.removeItemAt
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.ListUpdateCallback
import com.sawelo.wordmemorizer.R
import com.sawelo.wordmemorizer.data.data_class.Category
import com.sawelo.wordmemorizer.databinding.ActivityMainBinding
import com.sawelo.wordmemorizer.fragment.HomeFragment
import com.sawelo.wordmemorizer.fragment.dialog.AddCategoryDialogFragment
import com.sawelo.wordmemorizer.fragment.dialog.SortingSettingsDialogFragment
import com.sawelo.wordmemorizer.util.NotificationUtils
import com.sawelo.wordmemorizer.util.NotificationUtils.checkPermissionAndSendNotification
import com.sawelo.wordmemorizer.util.WordUtils.isAll
import com.sawelo.wordmemorizer.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


@AndroidEntryPoint
class MainActivity : AppCompatActivity(), ListUpdateCallback, OnSharedPreferenceChangeListener {
    private lateinit var binding: ActivityMainBinding
    private lateinit var sharedPreferences: SharedPreferences
    private val viewModel: MainViewModel by viewModels()
    private var asyncDiffer: AsyncListDiffer<Category>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportFragmentManager.commit {
            replace<HomeFragment>(R.id.activityMain_fcv, HomeFragment.MAIN_FRAGMENT_TAG)
        }

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        sharedPreferences.registerOnSharedPreferenceChangeListener(this)
        sharedPreferences.checkPermissionAndSendNotification(this)

        setNavigationListener()

        asyncDiffer = AsyncListDiffer(this, viewModel.asyncDifferConfig)
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.getAllCategories().collectLatest { categories ->
                    asyncDiffer?.submitList(categories)
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

        binding.activityMainNavigationViewAddCategoryBtn.setOnClickListener {
            AddCategoryDialogFragment().show(supportFragmentManager, null)
        }

        binding.activityMainToolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.menuOptions_reload -> {
                    viewModel.resetAllForgotCount()
                    true
                }
                R.id.menuOptions_sort -> {
                    SortingSettingsDialogFragment().show(supportFragmentManager, null)
                    true
                }
                R.id.menuOptions_settings -> {
                    val intent = Intent(this, SettingsActivity::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }
    }

    override fun onSharedPreferenceChanged(sharedPref: SharedPreferences, key: String) {
        if (key == NotificationUtils.PREFERENCE_FLOATING_BUBBLE_KEY) {
            sharedPreferences.checkPermissionAndSendNotification(this)
        }
    }

    override fun onInserted(position: Int, count: Int) {
        val menu = binding.activityMainNavigationView.menu
        (position until position + count).forEach { perPosition ->
            asyncDiffer?.currentList?.get(perPosition)?.let { category ->
                val text = "${category.categoryName} (${category.wordCount})"
                menu.add(Menu.NONE, category.categoryId, category.categoryId, text)

                val item = menu.findItem(category.categoryId)
                item.setOnMenuItemClickListener {
                    val homeFragment = supportFragmentManager
                        .findFragmentByTag(HomeFragment.MAIN_FRAGMENT_TAG) as? HomeFragment
                    homeFragment?.setCurrentTab(perPosition)
                    binding.activityMainDrawerLayout.close()
                    true
                }

                item.setActionView(R.layout.item_drawer_category)
                item.actionView?.findViewById<Button>(R.id.itemDrawer_btn)
                    ?.setOnClickListener {
                        viewModel.deleteCategory(category)
                    }
                if (category.isAll()) {
                    item.actionView?.findViewById<Button>(R.id.itemDrawer_btn)?.isVisible = false
                }
            }
        }
    }

    override fun onRemoved(position: Int, count: Int) {
        val menu = binding.activityMainNavigationView.menu
        (position until position + count).forEach { perPosition ->
            menu.removeItemAt(perPosition)
        }
    }

    override fun onMoved(fromPosition: Int, toPosition: Int) {}

    override fun onChanged(position: Int, count: Int, payload: Any?) {
        val menu = binding.activityMainNavigationView.menu
        (position until position + count).forEach { perPosition ->
            asyncDiffer?.currentList?.get(perPosition)?.let { category ->
                val text = "${category.categoryName} (${category.wordCount})"
                menu.findItem(category.categoryId).title = text
            }
        }
    }


//
//    private fun setNotification() {
//        // Create bubble intent
//        val target = Intent(this, BubbleActivity::class.java)
//        val bubbleIntent = PendingIntent.getActivity(
//            this, 0, target, PendingIntent.FLAG_IMMUTABLE)
//        val category = "com.example.category.IMG_SHARE_TARGET"
//
//        // Create sharing shortcut
//        val shortcut =
//            ShortcutInfo.Builder(this, SHORTCUT_ID)
//                .setCategories(setOf(category))
//                .setIntent(Intent(Intent.ACTION_DEFAULT))
//                .setLongLived(true)
//                .setShortLabel("Touch me")
//                .build()
//
//        // Create bubble metadata
//        val bubbleData = if (Build.VERSION.SDK_INT >= 30) {
//            Notification.BubbleMetadata.Builder(
//                bubbleIntent, Icon.createWithResource(
//                    this, R.drawable.ic_launcher_foreground))
//                .setDesiredHeight(600)
//                .build()
//        } else
//            Notification.BubbleMetadata.Builder()
//                .setDesiredHeight(600)
//                .build()
//
//        // Create notification, referencing the sharing shortcut
//        val builder = Notification.Builder(this, CHANNEL_ID)
//
//            .setBubbleMetadata(bubbleData)
//            .setShortcutId(SHORTCUT_ID)
//

//
//        NotificationManagerCompat.from(this).notify(1, builder.build())
//    }
//
//    companion object {
//        const val SHORTCUT_ID = "SHORTCUT_ID"
//    }
}