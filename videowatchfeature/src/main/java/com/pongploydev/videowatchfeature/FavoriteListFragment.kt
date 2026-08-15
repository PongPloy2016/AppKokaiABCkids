package com.pongploydev.videowatchfeature

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pongploydev.videowatchfeature.activity.ActivityVideoAutoDetail
import com.pongploydev.videowatchfeature.activity.ActivityVideoDetail
import com.pongploydev.videowatchfeature.adapter.AdapterVideo
import com.pongploydev.videowatchfeature.databases.prefs.SharedPref
import com.pongploydev.videowatchfeature.model.Video
import com.pongploydev.videowatchfeature.viewmodel.FavoriteViewModel

class FavoriteListFragment : Fragment() {

    private var rootView: View? = null
    private lateinit var recyclerView: RecyclerView
    private lateinit var lytEmptyFavorite: LinearLayout
    private lateinit var btnBack: ImageView
    private lateinit var switchViewButton: ImageView
    private lateinit var btnGoHome: Button
    
    private var adapterVideo: AdapterVideo? = null
    private lateinit var sharedPref: SharedPref
    private var activityContext: Activity? = null
    private lateinit var viewModel: FavoriteViewModel

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activityContext = context as Activity
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(R.layout.fragment_favorite_list, container, false)

        viewModel = ViewModelProvider(this).get(FavoriteViewModel::class.java)
        sharedPref = SharedPref(requireContext())

        initViews()
        setupRecyclerView()
        setupListeners()
        setupObservers()
        updateSwitchViewButtonIcon()

        // Hide ActionBar for a cleaner kid look
        (activity as? AppCompatActivity)?.supportActionBar?.hide()

        return rootView
    }

    private fun initViews() {
        rootView?.apply {
            recyclerView = findViewById(R.id.recyclerView)
            lytEmptyFavorite = findViewById(R.id.lyt_empty_favorite)
            btnBack = findViewById(R.id.btn_back)
            switchViewButton = findViewById(R.id.switch_view_button)
            btnGoHome = findViewById(R.id.btn_go_home)
        }
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

    private fun setupRecyclerViewLayoutManager() {
        val viewType = sharedPref.videoViewType
        if (viewType == Constant.VIDEO_LIST_COMPACT) {
            recyclerView.layoutManager = LinearLayoutManager(activityContext)
        } else {
            // ปรับเป็น List (span 1) แทน Grid (span 2) ตามคำขอของผู้ใช้
            recyclerView.layoutManager = LinearLayoutManager(activityContext)
        }
    }

    private fun setupListeners() {
        btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        btnGoHome.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        switchViewButton.setOnClickListener {
            toggleViewType()
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

    private fun toggleViewType() {
        val currentType = sharedPref.videoViewType
        val newType = if (currentType == Constant.VIDEO_LIST_COMPACT) {
            Constant.VIDEO_LIST_DEFAULT
        } else {
            Constant.VIDEO_LIST_COMPACT
        }
        sharedPref.updateVideoViewType(newType)

        setupRecyclerViewLayoutManager()
        updateSwitchViewButtonIcon()
        
        // Refresh adapter with current data
        viewModel.favoriteVideos.value?.let {
            adapterVideo?.updateData(it)
        }
    }

    private fun updateSwitchViewButtonIcon() {
        if (sharedPref.videoViewType == Constant.VIDEO_LIST_COMPACT) {
            switchViewButton.setImageResource(R.drawable.ic_grid)
        } else {
            switchViewButton.setImageResource(R.drawable.ic_view)
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
        fun newInstance(): FavoriteListFragment {
            return FavoriteListFragment()
        }
    }
}
