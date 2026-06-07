package com.pongploydev.videowatchfeature.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.pongploydev.videowatchfeature.VideoListFragment
import com.pongploydev.videowatchfeature.fragment.FavoritesFragment

class MainViewPagerAdapter : FragmentStateAdapter {

    constructor(fragmentActivity: FragmentActivity) : super(fragmentActivity)
    constructor(fragment: Fragment) : super(fragment)

    override fun getItemCount(): Int {
        return 2
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> VideoListFragment()
            1 -> FavoritesFragment()
            else -> VideoListFragment()
        }
    }
}
