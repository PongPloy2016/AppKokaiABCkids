package com.pongploydev.videowatchfeature.fragment

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pongploydev.videowatchfeature.Constant
import com.pongploydev.videowatchfeature.R
import com.pongploydev.videowatchfeature.activity.ActivityVideoAutoDetail
import com.pongploydev.videowatchfeature.activity.ActivityVideoDetail
import com.pongploydev.videowatchfeature.adapter.AdapterVideo
import com.pongploydev.videowatchfeature.databases.prefs.SharedPref
import com.pongploydev.videowatchfeature.model.Video
import com.pongploydev.videowatchfeature.viewmodel.FavoriteViewModel

class FavoriteListContentFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var lytEmptyFavorite: LinearLayout
    private lateinit var btnGoHome: Button

    private var adapterVideo: AdapterVideo? = null
    private lateinit var sharedPref: SharedPref
    private var activityContext: Activity? = null
    private lateinit var viewModel: FavoriteViewModel

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activityContext = context as? Activity
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_favorite_list_content, container, false)

        viewModel = ViewModelProvider(this).get(FavoriteViewModel::class.java)
        sharedPref = SharedPref(requireContext())

        initViews(view)
        setupRecyclerView()
        setupListeners()
        setupObservers()

        return view
    }

    private fun initViews(view: View) {
        recyclerView = view.findViewById(R.id.recyclerView)
        lytEmptyFavorite = view.findViewById(R.id.lyt_empty_favorite)
        btnGoHome = view.findViewById(R.id.btn_go_home)
    }

    private fun setupRecyclerView() {
        recyclerView.setHasFixedSize(true)
        setupRecyclerViewLayoutManager()

        adapterVideo = AdapterVideo(requireContext(), recyclerView, mutableListOf()).apply {
            setOnItemClickListener { _, video, _ ->
                openVideoActivity(video)
            }
        }
        recyclerView.adapter = adapterVideo
    }

    fun setupRecyclerViewLayoutManager() {
        val viewType = sharedPref.videoViewType
        if (viewType == Constant.VIDEO_LIST_COMPACT) {
            recyclerView.layoutManager = LinearLayoutManager(activityContext)
        } else {
            val gridLayoutManager = GridLayoutManager(activityContext, 2)
            gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    return if (adapterVideo?.getItemViewType(position) == 0) 2 else 1
                }
            }
            recyclerView.layoutManager = gridLayoutManager
        }
    }

    private fun setupListeners() {
        btnGoHome.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    private fun setupObservers() {
        viewModel.favoriteVideos.observe(viewLifecycleOwner) { videos ->
            if (videos.isNullOrEmpty()) {
                lytEmptyFavorite.visibility = View.VISIBLE
                recyclerView.visibility = View.GONE
            } else {
                lytEmptyFavorite.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE
                adapterVideo?.updateData(videos)
            }
        }
    }

    fun toggleViewType() {
        val currentType = sharedPref.videoViewType
        val newType = if (currentType == Constant.VIDEO_LIST_COMPACT) {
            Constant.VIDEO_LIST_DEFAULT
        } else {
            Constant.VIDEO_LIST_COMPACT
        }
        sharedPref.updateVideoViewType(newType)

        setupRecyclerViewLayoutManager()

        viewModel.favoriteVideos.value?.let {
            adapterVideo?.updateData(it)
        }
    }

    private fun openVideoActivity(video: Video) {
        val intent = if (video.video_type?.lowercase() == "youtube") {
            Intent(activityContext, ActivityVideoAutoDetail::class.java)
        } else {
            Intent(activityContext, ActivityVideoDetail::class.java)
        }
        intent.putExtra(Constant.KEY_VID, video.vid)
        startActivity(intent)
    }

    companion object {
        fun newInstance(): FavoriteListContentFragment {
            return FavoriteListContentFragment()
        }
    }
}
