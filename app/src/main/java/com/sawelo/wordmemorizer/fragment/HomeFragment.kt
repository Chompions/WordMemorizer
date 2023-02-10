package com.sawelo.wordmemorizer.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.*
import com.sawelo.wordmemorizer.R
import com.sawelo.wordmemorizer.data.repository.LocalRepository
import com.sawelo.wordmemorizer.databinding.FragmentHomeBinding
import com.sawelo.wordmemorizer.receiver.FloatingAddWordWindowReceiver
import com.sawelo.wordmemorizer.service.NotificationFloatingBubbleService
import com.sawelo.wordmemorizer.util.Constants.WORD_LIST_FRAGMENT_TAG
import com.sawelo.wordmemorizer.util.FloatingDialogUtils
import com.sawelo.wordmemorizer.viewmodel.MainViewModel
import com.sawelo.wordmemorizer.window.dialog.FloatingAddWordWindowInstance
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment : Fragment() {
    @Inject
    lateinit var localRepository: LocalRepository
    @Inject
    lateinit var floatingDialogUtils: FloatingDialogUtils

    private val viewModel: MainViewModel by activityViewModels()
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

        binding?.fragmentHomeAddFab?.setOnClickListener {
            FloatingAddWordWindowInstance(
                requireActivity(), floatingDialogUtils, viewModel.selectedCategories.value
            ).also { instance ->
                instance.showInstance()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        NotificationFloatingBubbleService.wrapBubbleService(requireContext())
    }

    override fun onStop() {
        super.onStop()
        NotificationFloatingBubbleService.unwrapBubbleService(requireContext())
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