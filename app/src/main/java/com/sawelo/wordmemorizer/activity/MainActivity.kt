package com.sawelo.wordmemorizer.activity

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.sawelo.wordmemorizer.R
import com.sawelo.wordmemorizer.databinding.ActivityMainBinding
import com.sawelo.wordmemorizer.fragment.AddDialogFragment
import com.sawelo.wordmemorizer.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.runBlocking


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.activityMainToolbar.setNavigationOnClickListener {
            binding.activityMainDrawerLayout.open()
        }
        binding.activityMainToolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.menuOptions_reload -> {
                    runBlocking {
                        viewModel.resetCount() {
                            Toast.makeText(
                                this@MainActivity,
                                "Word count reset",
                                Toast.LENGTH_SHORT
                            )
                                .show()
                        }
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
        binding.activityMainNavigationView.menu.add("Hello")

        binding.activityMainFab.setOnClickListener {
            AddDialogFragment().show(supportFragmentManager, null)
        }
    }

}