package com.sawelo.wordmemorizer.fragment.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.RadioGroup
import androidx.appcompat.app.AlertDialog
import androidx.datastore.preferences.core.edit
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.radiobutton.MaterialRadioButton
import com.sawelo.wordmemorizer.R
import com.sawelo.wordmemorizer.dataStore
import com.sawelo.wordmemorizer.util.PreferencesUtil
import com.sawelo.wordmemorizer.util.Sorting
import com.sawelo.wordmemorizer.util.SortingAnchor
import com.sawelo.wordmemorizer.util.SortingOrder
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SortingSettingsDialogFragment : DialogFragment() {
    private lateinit var sortingSettingsDialog: AlertDialog

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let { activity ->
            val view = layoutInflater.inflate(R.layout.dialog_sorting_settings, null)
            val builder = MaterialAlertDialogBuilder(activity).setView(view)
            sortingSettingsDialog = builder.create()

            val sortingAnchorRadioGroup =
                view.findViewById<RadioGroup>(R.id.dialog_sortingSettings_sortingAnchorGroup)
            sortingAnchorRadioGroup.addRadioButton<SortingAnchor>(activity)

            val sortingOrderRadioGroup =
                view.findViewById<RadioGroup>(R.id.dialog_sortingSettings_sortingOrderGroup)
            sortingOrderRadioGroup.addRadioButton<SortingOrder>(activity)

            sortingSettingsDialog
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    private inline fun <reified T : Enum<T>> RadioGroup.addRadioButton(context: Context) {
        enumValues<T>().forEach { enum ->
            val radioButton = MaterialRadioButton(context).apply {
                id = (enum as Sorting).obtainId()
                text = (enum as Sorting).obtainText(context)
                setOnCheckedChangeListener { _, isChecked ->
                    lifecycleScope.launch {
                        context.dataStore.edit { settings ->
                            if (isChecked) PreferencesUtil.setCurrentSortingToPreferences(
                                settings, enum
                            )
                        }
                    }
                }
            }

            lifecycleScope.launch {
                context.dataStore.data.first().also { preferences ->
                    val currentEnum =
                        PreferencesUtil.obtainCurrentSortingFromPreferences<T>(preferences)
                    if (radioButton.id == currentEnum.obtainId()) radioButton.isChecked = true
                    this@addRadioButton.addView(radioButton, (enum as Sorting).obtainId())
                }
            }
        }
    }
}