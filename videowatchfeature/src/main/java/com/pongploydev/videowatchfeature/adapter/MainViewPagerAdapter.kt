package com.pongploydev.videowatchfeature.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.pongploydev.videowatchfeature.VideoListFragment
import com.pongploydev.videowatchfeature.fragment.FavoritesFragment
import com.pongploydev.videowatchfeature.fragment.TeraBoxFragment

class MainViewPagerAdapter : FragmentStateAdapter {

    constructor(fragmentActivity: FragmentActivity) : super(fragmentActivity)
    constructor(fragment: Fragment) : super(fragment)

    override fun getItemCount(): Int {
        return 3
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> VideoListFragment()
            1 -> FavoritesFragment()
            2 -> TeraBoxFragment.newInstance()
            else -> VideoListFragment()
        }
    }
}
