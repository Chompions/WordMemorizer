package com.sawelo.wordmemorizer.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.sawelo.wordmemorizer.R
import com.sawelo.wordmemorizer.adapter.CategoryAdapter
import com.sawelo.wordmemorizer.data.data_class.entity.Category
import com.sawelo.wordmemorizer.databinding.FragmentMainBottomSheetBinding
import com.sawelo.wordmemorizer.util.Constants.selectedCategories
import com.sawelo.wordmemorizer.util.callback.ItemCategoryAdapterListener
import com.sawelo.wordmemorizer.viewmodel.MainViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class MainBottomSheetFragment : BottomSheetDialogFragment(), ItemCategoryAdapterListener {
    private var binding: FragmentMainBottomSheetBinding? = null
    private val viewModel: MainViewModel by activityViewModels()
    private var adapter: CategoryAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMainBottomSheetBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        adapter = CategoryAdapter(this)
        runBlocking {
            viewModel.getAllCategoryWithInfo().first().also {
                adapter?.submitList(it)
            }
        }
        binding?.fragmentMainBottomRv?.layoutManager = LinearLayoutManager(context)
        binding?.fragmentMainBottomRv?.adapter = adapter

        observeCategories()
        setButton()
    }

    private fun observeCategories() {
        lifecycleScope.launch {
            selectedCategories.collectLatest {
                adapter?.setSelectedCategory(it)
            }
        }

        lifecycleScope.launch {
            viewModel.getAllCategoryWithInfo().collectLatest {
                adapter?.submitList(it)
            }
        }
    }

    private fun setButton() {
        binding?.fragmentMainBottomSelectAllBtn?.setOnClickListener {
            viewModel.selectAllCategories()
        }

        binding?.fragmentMainBottomUnselectAllBtn?.setOnClickListener {
            viewModel.unselectAllCategories()
        }

        binding?.fragmentMainBottomSortBtn?.setOnClickListener {
            SortingSettingsDialogFragment().show(parentFragmentManager, null)
        }

        binding?.fragmentMainBottomAddBtn?.setOnClickListener {
            AddCategoryDialogFragment().show(parentFragmentManager, null)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }

    override fun onCheckedChangedListener(isSelected: Boolean, item: Category) {
        if (isSelected) {
            if (item !in selectedCategories.value)
                selectedCategories.value += item
        } else {
            if (item in selectedCategories.value)
                selectedCategories.value -= item
        }
    }

    override fun onLongClickListener(item: Category) {
        val alertDialog = MaterialAlertDialogBuilder(requireContext()).apply {
            setTitle("Delete confirmation")
            setMessage(
                "Are you sure you want to delete ${item.categoryName} category?"
            )
            setPositiveButton(R.string.ok) { dialog, _ ->
                viewModel.deleteCategory(item)
                dialog.dismiss()
            }
            setNegativeButton(R.string.cancel) { dialog, _ ->
                dialog.cancel()
            }
        }.create()
        alertDialog.show()
    }

    companion object {
        const val TAG = "MainBottomSheetFragment"
    }

}