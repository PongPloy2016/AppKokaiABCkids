package com.pongploydev.videowatchfeature.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.pongploydev.videowatchfeature.R
import com.pongploydev.videowatchfeature.adapter.MainViewPagerAdapter

class MainVideoFragment : Fragment() {

    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_main_video, container, false)
        initViews(view)
        setupViewPager()
        return view
    }

    private fun initViews(view: View) {
        viewPager = view.findViewById(R.id.view_pager)
        tabLayout = view.findViewById(R.id.tab_layout)
    }

    private fun setupViewPager() {
        val adapter = MainViewPagerAdapter(this)
        viewPager.adapter = adapter

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
                    when (position) {
                        0 -> {
                            tab.text = "All Videos"
                            tab.setIcon(R.drawable.ic_kids_tv)
                        }
                        1 -> {
                            tab.text = "Favorites"
                            tab.setIcon(R.drawable.ic_heart_filled)
                        }
                        2 -> {
                            tab.text = "TeraBox"
                            tab.setIcon(R.drawable.ic_category)
                        }
                    }
                }
                .attach()
    }
}
