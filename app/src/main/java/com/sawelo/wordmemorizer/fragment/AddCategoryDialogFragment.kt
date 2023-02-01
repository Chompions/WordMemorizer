package com.sawelo.wordmemorizer.fragment

import android.app.Dialog
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.sawelo.wordmemorizer.R
import com.sawelo.wordmemorizer.data.data_class.Category
import com.sawelo.wordmemorizer.util.ViewUtils.showToast
import com.sawelo.wordmemorizer.viewmodel.MainViewModel

class AddCategoryDialogFragment : DialogFragment() {
    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var addCategoryDialog: AlertDialog

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let { activity ->
            val view = layoutInflater.inflate(R.layout.dialog_add_category, null)
            val builder = MaterialAlertDialogBuilder(activity).setView(view)
            addCategoryDialog = builder.create()

            val wordEt = view.findViewById<EditText>(R.id.dialog_addCategory_et)
            val addBtn: Button = view.findViewById(R.id.dialog_addCategory_btn)

            addBtn.setOnClickListener {
                val category = Category(categoryName = wordEt.text.toString())
                when {
                    category.categoryName.isBlank() -> context?.showToast("Category name cannot be empty")
                    else -> {
                        viewModel.addCategory(category)
                        addCategoryDialog.dismiss()
                        context?.showToast("You created ${category.categoryName} category")
                    }
                }
            }

            addCategoryDialog
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}