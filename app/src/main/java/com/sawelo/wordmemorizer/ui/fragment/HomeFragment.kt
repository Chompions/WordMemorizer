package com.sawelo.wordmemorizer.ui.fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.*
import androidx.lifecycle.lifecycleScope
import com.sawelo.wordmemorizer.R
import com.sawelo.wordmemorizer.data.repository.LocalRepository
import com.sawelo.wordmemorizer.databinding.FragmentHomeBinding
import com.sawelo.wordmemorizer.receiver.FloatingAddWordWindowReceiver
import com.sawelo.wordmemorizer.ui.activity.FlashcardActivity
import com.sawelo.wordmemorizer.ui.ui_util.FloatingDialogUtil
import com.sawelo.wordmemorizer.ui.window.instance.FloatingAddWordWindowInstance
import com.sawelo.wordmemorizer.util.Constants.WORD_LIST_FRAGMENT_TAG
import com.sawelo.wordmemorizer.util.Constants.selectedCategories
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment : Fragment() {
    @Inject
    lateinit var localRepository: LocalRepository
    @Inject
    lateinit var floatingDialogUtil: FloatingDialogUtil

    private var binding: FragmentHomeBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding?.root
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        parentFragmentManager.commit {
            replace<WordListFragment>(R.id.fragmentHome_fcv, WORD_LIST_FRAGMENT_TAG)
        }

        lifecycleScope.launch {
            selectedCategories.collectLatest {
                binding?.fragmentHomeFlashCardsFab?.isVisible = it.isNotEmpty()
            }
        }

        binding?.fragmentHomeFlashCardsFab?.setOnClickListener {
            val intent = Intent(context, FlashcardActivity::class.java)
            startActivity(intent)
        }

        binding?.fragmentHomeAddFab?.setOnClickListener {
            FloatingAddWordWindowInstance(
                requireActivity(), floatingDialogUtil, selectedCategories.value
            ).also { instance ->
                instance.showWindow()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    override fun onDestroy() {
        super.onDestroy()
        FloatingAddWordWindowReceiver.closeWindow(requireContext())
    }
}