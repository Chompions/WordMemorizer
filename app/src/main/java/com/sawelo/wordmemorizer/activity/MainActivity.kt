package com.sawelo.wordmemorizer.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
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
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.ListUpdateCallback
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.sawelo.wordmemorizer.BuildConfig
import com.sawelo.wordmemorizer.R
import com.sawelo.wordmemorizer.data.data_class.Category
import com.sawelo.wordmemorizer.databinding.ActivityMainBinding
import com.sawelo.wordmemorizer.fragment.AddCategoryDialogFragment
import com.sawelo.wordmemorizer.fragment.HomeFragment
import com.sawelo.wordmemorizer.fragment.SortingSettingsDialogFragment
import com.sawelo.wordmemorizer.util.Constants.HOME_FRAGMENT_TAG
import com.sawelo.wordmemorizer.util.SettingsUtils
import com.sawelo.wordmemorizer.util.WordUtils.isAll
import com.sawelo.wordmemorizer.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


@AndroidEntryPoint
class MainActivity : AppCompatActivity(), ListUpdateCallback {
    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()
    private var asyncDiffer: AsyncListDiffer<Category>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportFragmentManager.commit {
            replace<HomeFragment>(R.id.activityMain_fcv, HOME_FRAGMENT_TAG)
        }

        SettingsUtils(this).checkAll()

        setAds()
        setCategories()
        setNavigationListener()
    }

    @SuppressLint("VisibleForTests")
    private fun setAds() {
        if (BuildConfig.BUILD_TYPE != "cleanRelease") {
            MobileAds.initialize(this)
            val adRequest = AdRequest.Builder().build()
            binding.adView.loadAd(adRequest)
        }
    }

    private fun setCategories() {
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

    override fun onInserted(position: Int, count: Int) {
        val menu = binding.activityMainNavigationView.menu
        val currentList = asyncDiffer?.currentList
        (position until position + count).forEach { perPosition ->
            currentList?.get(perPosition)?.let { category ->
                menu.createTab(category, perPosition)
            }
        }
    }

    override fun onRemoved(position: Int, count: Int) {
        val menu = binding.activityMainNavigationView.menu
        (position until position + count).forEach { perPosition ->
            menu.removeItemAt(perPosition)
        }
    }

    override fun onMoved(fromPosition: Int, toPosition: Int) {
        val menu = binding.activityMainNavigationView.menu
        val currentList = asyncDiffer?.currentList
        menu.removeItemAt(fromPosition)
        currentList?.get(toPosition)?.let { category ->
            menu.createTab(category, toPosition)
        }
    }

    override fun onChanged(position: Int, count: Int, payload: Any?) {
        val menu = binding.activityMainNavigationView.menu
        val currentList = asyncDiffer?.currentList
        (position until position + count).forEach { perPosition ->
            menu.removeItemAt(perPosition)
            currentList?.get(perPosition)?.let { category ->
                menu.createTab(category, perPosition)
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
            val homeFragment = supportFragmentManager
                .findFragmentByTag(HOME_FRAGMENT_TAG) as? HomeFragment
            homeFragment?.setCurrentTab(category)
            binding.activityMainDrawerLayout.close()
            true
        }
    }
}