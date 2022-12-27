package com.sawelo.wordmemorizer.activity

import android.os.Bundle
import android.view.Menu.NONE
import android.widget.Button
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.add
import androidx.fragment.app.commit
import com.sawelo.wordmemorizer.R
import com.sawelo.wordmemorizer.databinding.ActivityMainBinding
import com.sawelo.wordmemorizer.fragment.AddCategoryDialogFragment
import com.sawelo.wordmemorizer.fragment.MainFragment
import com.sawelo.wordmemorizer.utils.WordUtils.isAll
import com.sawelo.wordmemorizer.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            supportFragmentManager.commit {
                add<MainFragment>(R.id.activityMain_fcv, MainFragment.MAIN_FRAGMENT_TAG)
            }
        }

        binding.activityMainToolbar.setNavigationOnClickListener {
            binding.activityMainDrawerLayout.open()
        }
        binding.activityMainToolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.menuOptions_reload -> {
                    viewModel.resetAllForgotCount {
                        Toast.makeText(
                            this@MainActivity,
                            "Word count reset",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    true
                }
                else -> false
            }
        }

        binding.activityMainNavigationView.setNavigationItemSelectedListener {
            binding.activityMainDrawerLayout.close()
            true
        }

        viewModel.getAllCategories().observe(this) { categories ->
            with(binding.activityMainNavigationView.menu) {
                clear()
                for ((index, category) in categories.withIndex()) {
                    viewModel.getAllWordsByCategory(category).observe(this@MainActivity) {
                        val text = "${category.categoryName} (${it.size})"
                        add(NONE, category.id, index, text)
                        findItem(category.id).setActionView(R.layout.item_drawer_category)

                        val item = findItem(category.id)
                        item.setOnMenuItemClickListener {
                            val mainFragment = supportFragmentManager
                                .findFragmentByTag(MainFragment.MAIN_FRAGMENT_TAG) as? MainFragment
                            mainFragment?.viewPager?.setCurrentItem(index, true)
                            binding.activityMainDrawerLayout.close()
                            true
                        }

                        item.actionView?.findViewById<Button>(R.id.itemDrawer_btn)?.setOnClickListener {
                            viewModel.deleteCategory(category) {
                                showToast("You deleted ${category.categoryName} category")
                            }
                        }
                        if (category.isAll()) {
                            item.actionView?.findViewById<Button>(R.id.itemDrawer_btn)?.isVisible = false
                        }

                        binding.activityMainNavigationViewAddCategoryBtn.setOnClickListener {
                            AddCategoryDialogFragment().show(supportFragmentManager, null)
                        }
                    }
                }
            }
        }
    }

    private fun showToast(text: String) {
        Toast
            .makeText(this, text, Toast.LENGTH_SHORT)
            .show()
    }
}