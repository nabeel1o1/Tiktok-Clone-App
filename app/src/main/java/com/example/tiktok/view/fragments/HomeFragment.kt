package com.example.tiktok.view.fragments

import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.tiktok.adapter.HomeFeedAdapter
import com.example.tiktok.adapter.LoaderAdapter
import com.example.tiktok.databinding.FragmentHomeBinding
import com.example.tiktok.viewmodel.HomeFeedViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    private val binding get() = _binding!!

    private lateinit var adapter: HomeFeedAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = HomeFeedAdapter()
        val homeFeedViewModel = ViewModelProvider(this)[HomeFeedViewModel::class.java]

        val snapHelper = PagerSnapHelper()

        with(binding) {

            lytToolbar.setPadding(0, getStatusBarHeight(), 0, 0)
            rvHomeFeed.layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            rvHomeFeed.setHasFixedSize(true)
            rvHomeFeed.adapter = adapter.withLoadStateHeaderAndFooter(
                header = LoaderAdapter(),
                footer = LoaderAdapter()
            )
            snapHelper.attachToRecyclerView(rvHomeFeed)

            rvHomeFeed.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)

                    if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                        playVideoAtVisiblePosition(recyclerView, newState)
                    }
                }
            })
        }

        homeFeedViewModel.homeFeedDataList.observe(viewLifecycleOwner) {
            adapter.submitData(lifecycle, it)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root
        return root
    }

    fun playVideoAtVisiblePosition(recyclerView: RecyclerView, recyclerViewState: Int) {
        val layoutManager = recyclerView.layoutManager as LinearLayoutManager
        val visiblePosition = layoutManager.findFirstCompletelyVisibleItemPosition()

        if (visiblePosition != RecyclerView.NO_POSITION && recyclerViewState != RecyclerView.SCROLL_STATE_DRAGGING) {
            val viewHolder = recyclerView.findViewHolderForAdapterPosition(visiblePosition) as HomeFeedAdapter.HomeFeedViewHolder?
            viewHolder?.let {
                adapter.playVideo(it, visiblePosition)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        adapter.pauseCurrentVideo()
    }

    override fun onResume() {
        super.onResume()
        adapter.resumeCurrentVideo()
    }

    private fun getStatusBarHeight(): Int {
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        return if (resourceId > 0) resources.getDimensionPixelSize(resourceId)
        else Rect().apply {requireActivity().window.decorView.getWindowVisibleDisplayFrame(this) }.top
    }

    override fun onDestroyView() {
        super.onDestroyView()
        adapter.releasePlayer()
        _binding = null
    }
}