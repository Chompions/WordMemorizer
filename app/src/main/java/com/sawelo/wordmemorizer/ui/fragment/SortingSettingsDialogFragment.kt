package com.sawelo.wordmemorizer.ui.fragment

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.RadioGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.radiobutton.MaterialRadioButton
import com.sawelo.wordmemorizer.R
import com.sawelo.wordmemorizer.data.preferences.base.BaseSorting
import com.sawelo.wordmemorizer.data.preferences.sorting.SortingAnchor
import com.sawelo.wordmemorizer.data.preferences.sorting.SortingOrder
import com.sawelo.wordmemorizer.data.repository.PreferenceRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SortingSettingsDialogFragment : DialogFragment() {
    @Inject
    lateinit var preferenceRepository: PreferenceRepository

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

    private inline fun <reified T : BaseSorting> RadioGroup.addRadioButton(context: Context) {
        T::class.sealedSubclasses.forEach { kClass ->
            kClass.objectInstance?.also { baseSorting ->

                val radioButton = MaterialRadioButton(context).apply {
                    id = baseSorting.obtainId()
                    text = baseSorting.obtainText(context)
                    setOnCheckedChangeListener { _, isChecked ->
                        if (isChecked) lifecycleScope.launch {
                            preferenceRepository.setCurrentSorting(baseSorting)
                        }
                    }
                }

                lifecycleScope.launch {
                    val currentSortingId = preferenceRepository.getCurrentSortingId<T>()
                    if (radioButton.id == currentSortingId)
                        radioButton.isChecked = true
                    addView(radioButton)
                }
            }
        }
    }
}