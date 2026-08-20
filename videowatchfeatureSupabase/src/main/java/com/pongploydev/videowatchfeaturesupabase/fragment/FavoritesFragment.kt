package com.pongploydev.videowatchfeaturesupabase.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.pongploydev.videowatchfeaturesupabase.R
import com.pongploydev.videowatchfeaturesupabase.activity.ActivityVideoAutoDetail
import com.pongploydev.videowatchfeaturesupabase.adapter.AdapterVideo
import com.pongploydev.videowatchfeaturesupabase.viewmodel.FavoritesViewModel

class FavoritesFragment : Fragment() {

    private val viewModel: FavoritesViewModel by viewModels()
    private var adapterVideo: AdapterVideo? = null
    private lateinit var recyclerView: RecyclerView
    private lateinit var lytNoItem: LinearLayout
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_favorites, container, false)
        initViews(view)
        setupRecyclerView()
        observeFavorites()
        return view
    }

    private fun initViews(view: View) {
        recyclerView = view.findViewById(R.id.recycler_view)
        lytNoItem = view.findViewById(R.id.lyt_no_item)
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout)

        // Disable swipe refresh as Room updates are automatic
        swipeRefreshLayout.isEnabled = false
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(context)
        // Fix: Pass recyclerView as the 2nd argument
        adapterVideo = AdapterVideo(requireContext(), recyclerView, ArrayList())
        adapterVideo?.setOnItemClickListener { view, video, position ->
            val intent = Intent(context, ActivityVideoAutoDetail::class.java)
            intent.putExtra(com.pongploydev.videowatchfeaturesupabase.Constant.KEY_VID, video.vid)
            intent.putExtra(com.pongploydev.videowatchfeaturesupabase.Constant.EXTRA_OBJC, video)
            startActivity(intent)
        }
        recyclerView.adapter = adapterVideo
    }

    private fun observeFavorites() {
        viewModel.allFavorites.observe(viewLifecycleOwner) { videos ->
            android.util.Log.d("FavoritesDebug", "Received videos in Fragment: ${videos.size}")
            if (videos.isNullOrEmpty()) {
                lytNoItem.visibility = View.VISIBLE
                recyclerView.visibility = View.GONE
            } else {
                lytNoItem.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE
                adapterVideo?.setItems(videos)
            }
        }
    }
}
