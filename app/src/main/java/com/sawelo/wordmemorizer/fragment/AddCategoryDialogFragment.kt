package com.sawelo.wordmemorizer.fragment

import android.app.Dialog
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputLayout
import com.sawelo.wordmemorizer.R
import com.sawelo.wordmemorizer.data.data_class.entity.Category
import com.sawelo.wordmemorizer.data.data_class.entity.CategoryInfo
import com.sawelo.wordmemorizer.data.data_class.relation_ref.CategoryWithInfo
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

            val nameTil = view.findViewById<TextInputLayout>(R.id.dialog_addCategory_name_til)
            val nameEt = view.findViewById<EditText>(R.id.dialog_addCategory_name_et)
            val descEt = view.findViewById<EditText>(R.id.dialog_addCategory_desc_et)
            val addBtn: Button = view.findViewById(R.id.dialog_addCategory_btn)

            nameEt.doOnTextChanged { _, _, _, _ ->
                nameTil.isErrorEnabled = false
            }

            addBtn.setOnClickListener {
                val categoryWithInfo = CategoryWithInfo(
                    Category(
                        categoryName = nameEt.text.toString(),
                        categoryDesc = descEt.text.toString()
                    ),
                    CategoryInfo()
                )
                when {
                    nameEt.text.isBlank() -> {
                        context?.showToast("Category name cannot be empty")
                        nameTil.error = "Cannot be empty"
                    }
                    else -> {
                        viewModel.addCategory(requireContext(), categoryWithInfo)
                        addCategoryDialog.dismiss()
                    }
                }
            }

            addCategoryDialog
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}