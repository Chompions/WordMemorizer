package com.sawelo.wordmemorizer.fragment.dialog

import android.app.Dialog
import android.os.Bundle
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.datastore.preferences.core.edit
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.radiobutton.MaterialRadioButton
import com.sawelo.wordmemorizer.R
import com.sawelo.wordmemorizer.dataStore
import com.sawelo.wordmemorizer.util.SortingAnchor
import com.sawelo.wordmemorizer.util.SortingOrder
import com.sawelo.wordmemorizer.viewmodel.MainViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SortingSettingsDialogFragment : DialogFragment() {
    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var sortingSettingsDialog: AlertDialog

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let { activity ->
            val view = layoutInflater.inflate(R.layout.dialog_sorting_settings, null)
            val builder = MaterialAlertDialogBuilder(activity).setView(view)
            sortingSettingsDialog = builder.create()

            val sortingAnchorRadioGroup = view.findViewById<RadioGroup>(R.id.dialog_sortingSettings_sortingAnchorGroup)
            enumValues<SortingAnchor>().forEach { sortingAnchor ->
                val radioButton = MaterialRadioButton(activity).apply {
                    id = sortingAnchor.ordinal
                    text = sortingAnchor.obtainText(activity)
                    setOnCheckedChangeListener { _, isChecked ->
                        lifecycleScope.launch {
                            context.dataStore.edit { settings ->
                                if (isChecked) {
                                    settings[SortingAnchor.obtainPreferencesKey] = sortingAnchor.name
                                }
                            }
                        }
                    }
                }

                lifecycleScope.launch {
                    context?.dataStore?.data?.first()?.let { preferences ->
                        preferences[SortingAnchor.obtainPreferencesKey]?.let {
                            if (radioButton.id == SortingAnchor.valueOf(it).ordinal) {
                                radioButton.isChecked = true
                            }
                        }
                        sortingAnchorRadioGroup.addView(radioButton, sortingAnchor.ordinal)
                    }
                }
            }

            val sortingOrderRadioGroup = view.findViewById<RadioGroup>(R.id.dialog_sortingSettings_sortingOrderGroup)
            enumValues<SortingOrder>().forEach { sortingOrder ->
                val radioButton = MaterialRadioButton(activity).apply {
                    id = sortingOrder.ordinal
                    text = sortingOrder.obtainText(activity)
                    setOnCheckedChangeListener { _, isChecked ->
                        lifecycleScope.launch {
                            context.dataStore.edit { settings ->
                                if (isChecked) {
                                    settings[SortingOrder.obtainPreferencesKey] = sortingOrder.name
                                }
                            }
                        }
                    }
                }

                lifecycleScope.launch {
                    context?.dataStore?.data?.first()?.let { preferences ->
                        preferences[SortingOrder.obtainPreferencesKey]?.let {
                            if (radioButton.id == SortingOrder.valueOf(it).ordinal) {
                                radioButton.isChecked = true
                            }
                        }
                        sortingOrderRadioGroup.addView(radioButton, sortingOrder.ordinal)
                    }
                }
            }

            sortingSettingsDialog
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    private fun showToast(text: String) {
        Toast
            .makeText(activity, text, Toast.LENGTH_SHORT)
            .show()
    }

}