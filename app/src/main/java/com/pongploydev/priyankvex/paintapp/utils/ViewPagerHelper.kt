package com.pongploydev.priyankvex.paintapp.utils

import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.pongploydev.priyankvex.paintapp.R
import com.pongploydev.priyankvex.paintapp.activity.MainActivity
import com.pongploydev.priyankvex.paintapp.fragment.ComposeFragment
import com.pongploydev.priyankvex.paintapp.fragment.MainGridFragment


class ViewPagerHelper(var activity: AppCompatActivity) {
    var prevMenuItem: MenuItem? = null
    var pagerNumber: Int = 1

    fun setupViewPagerDefault(
        viewPager: ViewPager,
        navigation: BottomNavigationView,
        titleToolbar: TextView
    ) {
        viewPager.visibility = View.VISIBLE
        viewPager.adapter = AdapterNavigationDefault(activity.supportFragmentManager)
        viewPager.offscreenPageLimit = pagerNumber
        navigation.setOnItemSelectedListener { item: MenuItem ->
            when (item.itemId) {

                R.id.navigation_home -> {
                    viewPager.currentItem = 0
                    return@setOnItemSelectedListener true
                }

//                R.id.navigation_video -> {
//                    viewPager.currentItem = 1
//                    return@setOnItemSelectedListener true
//                }


            }
            false
        }

        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
            }

            override fun onPageSelected(position: Int) {
                if (prevMenuItem != null) {
                    prevMenuItem!!.setChecked(false)
                } else {
                    navigation.menu.getItem(0).setChecked(false)
                }
                navigation.menu.getItem(position).setChecked(true)
                prevMenuItem = navigation.menu.getItem(position)

                if (viewPager.currentItem == 0) {
                    titleToolbar.text = activity.resources.getString(R.string.app_name)
                    //(activity as MainActivity).showSortMenu(true)
                }
//                else if (viewPager.currentItem == 1) {
//                    titleToolbar.text = activity.resources.getString(R.string.title_nav_video)
//                    //(activity as MainActivity).showSortMenu(false)
//                }
            }

            override fun onPageScrollStateChanged(state: Int) {
            }
        })
    }

    fun setupViewPagerCategory(
        viewPager: ViewPager,
        navigation: BottomNavigationView,
        titleToolbar: TextView
    ) {
        viewPager.visibility = View.VISIBLE
        viewPager.adapter = AdapterNavigationCategory(activity.supportFragmentManager)
        viewPager.offscreenPageLimit = pagerNumber
        navigation.setOnItemSelectedListener { item: MenuItem ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    viewPager.currentItem = 0
                    return@setOnItemSelectedListener true
                }

//                R.id.navigation_video -> {
//                    viewPager.currentItem = 1
//                    return@setOnItemSelectedListener true
//                }

            }
            false
        }

        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
            }

            override fun onPageSelected(position: Int) {
                if (prevMenuItem != null) {
                    prevMenuItem!!.setChecked(false)
                } else {
                    navigation.menu.getItem(0).setChecked(false)
                }
                navigation.menu.getItem(position).setChecked(true)
                prevMenuItem = navigation.menu.getItem(position)

                when (viewPager.currentItem) {
                    0 -> {
                        titleToolbar.text = activity.resources.getString(R.string.app_name)
                        //(activity as MainActivity).showSortMenu(false)
                    }
//                    1 -> {
//                        titleToolbar.text = activity.resources.getString(R.string.title_nav_video)
//                       // (activity as MainActivity).showSortMenu(true)
//                    }

                }
            }

            override fun onPageScrollStateChanged(state: Int) {
            }
        })
    }

    fun setupViewPagerRtlDefault(
        viewPager: RtlViewPager,
        navigation: BottomNavigationView,
        titleToolbar: TextView
    ) {
        viewPager.visibility = View.VISIBLE
        viewPager.adapter = AdapterNavigationDefault(activity.supportFragmentManager)
        viewPager.offscreenPageLimit = pagerNumber
        navigation.setOnItemSelectedListener { item: MenuItem ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    viewPager.currentItem = 0
                    return@setOnItemSelectedListener true
                }

                R.id.navigation_video -> {
                    viewPager.currentItem = 1
                    return@setOnItemSelectedListener true
                }


            }
            false
        }

        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
            }

            override fun onPageSelected(position: Int) {
                if (prevMenuItem != null) {
                    prevMenuItem!!.setChecked(false)
                } else {
                    navigation.menu.getItem(0).setChecked(false)
                }
                navigation.menu.getItem(position).setChecked(true)
                prevMenuItem = navigation.menu.getItem(position)

                if (viewPager.currentItem == 0) {
                    titleToolbar.text = activity.resources.getString(R.string.app_name)
                    //(activity as MainActivity).showSortMenu(true)
                } else if (viewPager.currentItem == 1) {
                    titleToolbar.text = activity.resources.getString(R.string.title_nav_news)
                   // (activity as MainActivity).showSortMenu(false)
                }
            }

            override fun onPageScrollStateChanged(state: Int) {
            }
        })
    }

    fun setupViewPagerRtlCategory(
        viewPager: RtlViewPager,
        navigation: BottomNavigationView,
        titleToolbar: TextView
    ) {
        viewPager.visibility = View.VISIBLE
        viewPager.adapter = AdapterNavigationCategory(activity.supportFragmentManager)
        viewPager.offscreenPageLimit = pagerNumber
        navigation.setOnItemSelectedListener { item: MenuItem ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    viewPager.currentItem = 0
                    return@setOnItemSelectedListener true
                }

                R.id.navigation_video-> {
                    viewPager.currentItem = 1
                    return@setOnItemSelectedListener true
                }


            }
            false
        }

        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
            }

            override fun onPageSelected(position: Int) {
                if (prevMenuItem != null) {
                    prevMenuItem!!.setChecked(false)
                } else {
                    navigation.menu.getItem(0).setChecked(false)
                }
                navigation.menu.getItem(position).setChecked(true)
                prevMenuItem = navigation.menu.getItem(position)

                if (viewPager.currentItem == 0) {
                    titleToolbar.text = activity.resources.getString(R.string.app_name)
                  //  (activity as MainActivity).showSortMenu(false)
                } else if (viewPager.currentItem == 1) {
                    titleToolbar.text = activity.resources.getString(R.string.title_nav_video)
                   // (activity as MainActivity).showSortMenu(true)
                }
            }

            override fun onPageScrollStateChanged(state: Int) {
            }
        })
    }

    @Suppress("deprecation")
    inner class AdapterNavigationDefault internal constructor(fm: FragmentManager) :
        FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
        override fun getItem(position: Int): Fragment {
            when (position) {
//                0 -> return MainGridFragment()
                0-> return ComposeFragment()

              // 4 -> return KidsItemFragment()
            }
            return MainGridFragment()
        }

        override fun getCount(): Int {
            return pagerNumber
        }
    }

    @Suppress("deprecation")
    inner class AdapterNavigationCategory internal constructor(fm: FragmentManager) :
        FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
        override fun getItem(position: Int): Fragment {
            when (position) {
                0 -> return MainGridFragment()
//                1 -> return ComposeFragment()

            }
            return ComposeFragment()
        }

        override fun getCount(): Int {
            return pagerNumber
        }
    }
}
