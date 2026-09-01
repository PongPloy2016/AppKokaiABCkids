package com.pongploydev.videowatchfeature

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.pongploydev.videowatchfeature.databases.prefs.SharedPref
import com.pongploydev.videowatchfeature.fragment.FavoriteListContentFragment
import com.pongploydev.videowatchfeature.fragment.TeraBoxFragment

class FavoriteListFragment : Fragment() {

    private var rootView: View? = null
    private lateinit var btnBack: ImageView
    private lateinit var switchViewButton: ImageView
    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager2

    private lateinit var sharedPref: SharedPref
    private var favoriteContentFragment: FavoriteListContentFragment? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(R.layout.fragment_favorite_list, container, false)

        sharedPref = SharedPref(requireContext())

        initViews()
        setupViewPager()
        setupListeners()
        updateSwitchViewButtonIcon()

        (activity as? AppCompatActivity)?.supportActionBar?.hide()

        return rootView
    }

    private fun initViews() {
        rootView?.apply {
            btnBack = findViewById(R.id.btn_back)
            switchViewButton = findViewById(R.id.switch_view_button)
            tabLayout = findViewById(R.id.tab_layout)
            viewPager = findViewById(R.id.view_pager)
        }
    }

    private fun setupViewPager() {
        val adapter = FavoritePagerAdapter(this)
        viewPager.adapter = adapter

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            when (position) {
                0 -> {
                    tab.text = getString(R.string.title_favorite_list)
                    tab.setIcon(R.drawable.ic_kids_heart)
                }
                1 -> {
                    tab.text = "TeraBox"
                    tab.setIcon(R.drawable.ic_category)
                }
            }
        }.attach()

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                if (position == 0) {
                    switchViewButton.visibility = View.VISIBLE
                } else {
                    switchViewButton.visibility = View.GONE
                }
            }
        })
    }

    private fun setupListeners() {
        btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        switchViewButton.setOnClickListener {
            favoriteContentFragment?.toggleViewType()
            updateSwitchViewButtonIcon()
        }
    }

    private fun updateSwitchViewButtonIcon() {
        if (sharedPref.videoViewType == Constant.VIDEO_LIST_COMPACT) {
            switchViewButton.setImageResource(R.drawable.ic_grid)
        } else {
            switchViewButton.setImageResource(R.drawable.ic_view)
        }
    }

    private inner class FavoritePagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
        override fun getItemCount(): Int = 2

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> {
                    val contentFragment = FavoriteListContentFragment.newInstance()
                    favoriteContentFragment = contentFragment
                    contentFragment
                }
                1 -> TeraBoxFragment.newInstance()
                else -> FavoriteListContentFragment.newInstance()
            }
        }
    }

    companion object {
        fun newInstance(): FavoriteListFragment {
            return FavoriteListFragment()
        }
    }
}
