package com.sawelo.wordmemorizer.activity

import android.os.Bundle
import android.view.Menu
import android.widget.Button
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.core.view.removeItemAt
import androidx.fragment.app.add
import androidx.fragment.app.commit
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.ListUpdateCallback
import com.sawelo.wordmemorizer.R
import com.sawelo.wordmemorizer.data.data_class.Category
import com.sawelo.wordmemorizer.databinding.ActivityMainBinding
import com.sawelo.wordmemorizer.fragment.MainFragment
import com.sawelo.wordmemorizer.fragment.dialog.AddCategoryDialogFragment
import com.sawelo.wordmemorizer.fragment.dialog.SortingSettingsDialogFragment
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

        if (savedInstanceState == null) {
            supportFragmentManager.commit {
                add<MainFragment>(R.id.activityMain_fcv, MainFragment.MAIN_FRAGMENT_TAG)
            }
        }

        viewModel.message.observe(this) { event ->
            event.getContentIfNotHandled()?.let {
                Toast
                    .makeText(this, it, Toast.LENGTH_SHORT)
                    .show()
            }
        }

        setNavigationListener()

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
                else -> false
            }
        }

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

    }

    override fun onInserted(position: Int, count: Int) {
        val menu = binding.activityMainNavigationView.menu
        (position until position + count).forEach { perPosition ->
            asyncDiffer?.currentList?.get(perPosition)?.let { category ->
                val text = "${category.categoryName} (${category.wordCount})"
                menu.add(Menu.NONE, category.categoryId, category.categoryId, text)

                val item = menu.findItem(category.categoryId)
                item.setOnMenuItemClickListener {
                    val mainFragment = supportFragmentManager
                        .findFragmentByTag(MainFragment.MAIN_FRAGMENT_TAG) as? MainFragment
                    mainFragment?.setCurrentTab(perPosition)
                    binding.activityMainDrawerLayout.close()
                    true
                }

                item.setActionView(R.layout.item_drawer_category)
                item.actionView?.findViewById<Button>(R.id.itemDrawer_btn)
                    ?.setOnClickListener {
                        viewModel.deleteCategory(category)
                    }
                if (category.isAll()) {
                    item.actionView?.findViewById<Button>(R.id.itemDrawer_btn)?.isVisible =
                        false
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
}