package com.pongploydev.priyankvex.paintapp.fragment

import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.pongploydev.priyankvex.paintapp.R
import com.pongploydev.priyankvex.paintapp.activity.thailetters.MenuThaiLettersActivity
import com.pongploydev.priyankvex.paintapp.activity.MenuActivityAZ
import com.pongploydev.priyankvex.paintapp.activity.learning.LeaningActvity
import com.pongploydev.priyankvex.paintapp.adapter.CustomAdapterAndroidGridView
import com.pongploydev.priyankvex.paintapp.databinding.FragmentMainLayoutBinding
import com.pongploydev.priyankvex.paintapp.dialog.AboutDialog
import com.pongploydev.priyankvex.paintapp.model.ItemObjectAndroidGridView

class MainGridFragment : Fragment() {

    // ViewBinding
    private var _binding: FragmentMainLayoutBinding? = null
    private val binding get() = _binding!!

    private var customAdapter: CustomAdapterAndroidGridView? = null
    private var isGridView = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainLayoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupMenu()
        setupRecyclerView()
        setupViewModeSelector()
        setupAdMob()
    }

    private fun setupMenu() {
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_main, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_settings -> {
                        openSettings()
                        true
                    }
                    R.id.action_about -> {
                        showAboutDialogNew()
                        true
                    }
                    R.id.action_ads_info -> {
                        showAdsInfo()
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun setupRecyclerView() {
        // ตั้งค่าเริ่มต้นเป็น Grid View
        setupGridView()

        val allItems = getAllItemObject()
        customAdapter = CustomAdapterAndroidGridView(requireContext(), allItems)
        binding.recyclerView.adapter = customAdapter

        customAdapter?.setOnItemClickListener { position, item ->
            handleItemClick(position, item)
        }
    }

    private fun setupViewModeSelector() {
        binding.viewModeGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.btnGridView -> switchToGridView()
                R.id.btnListView -> switchToListView()
            }
        }
    }

    private fun setupAdMob() {
//        binding.adView.loadAd(AdRequest.Builder().build())
//        binding.adView.adListener = object : AdListener() {
//            override fun onAdFailedToLoad(adError: LoadAdError) {
//                Log.e("AdMob", "Ad failed to load: ${adError.message}")
//            }
//            override fun onAdClosed() {
//                val adRequest = AdRequest.Builder().build()
//                binding.adView.loadAd(adRequest)
//            }
//        }
    }

    private fun switchToGridView() {
        if (!isGridView) {
            setupGridView()
            isGridView = true
            customAdapter?.isGridView = true
            customAdapter?.notifyDataSetChanged()
        }
    }

    private fun switchToListView() {
        if (isGridView) {
            setupListView()
            isGridView = false
            customAdapter?.isGridView = false
            customAdapter?.notifyDataSetChanged()
        }
    }

    private fun setupGridView() {
        // ใช้ GridLayoutManager 2 คอลัมน์
        val layoutManager = GridLayoutManager(requireContext(), 2)
        binding.recyclerView.layoutManager = layoutManager

        // ลบ ItemDecoration เก่าทั้งหมด
        removeAllItemDecorations()

        // เพิ่ม ItemDecoration ใหม่แบบง่ายๆ
        binding.recyclerView.addItemDecoration(SimpleGridItemDecoration(16))

        // ตั้งค่า Padding ให้ RecyclerView
        binding.recyclerView.setPadding(16, 16, 16, 16)
        binding.recyclerView.clipToPadding = false
    }

    private fun setupListView() {
        // ใช้ LinearLayoutManager
        val layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.layoutManager = layoutManager

        // ลบ ItemDecoration เก่าทั้งหมด
        removeAllItemDecorations()

        // เพิ่ม ItemDecoration สำหรับ List
        binding.recyclerView.addItemDecoration(SimpleListItemDecoration(8))

        // ตั้งค่า Padding ให้ RecyclerView
        binding.recyclerView.setPadding(16, 16, 16, 16)
        binding.recyclerView.clipToPadding = false
    }

    private fun removeAllItemDecorations() {
        while (binding.recyclerView.itemDecorationCount > 0) {
            binding.recyclerView.removeItemDecorationAt(0)
        }
    }

    // --- การจัดการ Click ---
    private fun handleItemClick(position: Int, item: ItemObjectAndroidGridView) {
        when (position) {
            0 -> startLearningActivity()
            1 -> startMenuActivity()
            2 -> startMenuActivityAZ()
            3 -> showAboutDialogNew()
        }
    }

    private fun startLearningActivity() {
        val i = Intent(requireActivity(), LeaningActvity::class.java)
        startActivity(i)
    }

    private fun startMenuActivity() {
        val i = Intent(requireActivity(), MenuThaiLettersActivity::class.java)
        startActivity(i)
    }

    private fun startMenuActivityAZ() {
        val i = Intent(requireActivity(), MenuActivityAZ::class.java)
        startActivity(i)
    }

    private fun showAboutDialogNew() {
        AboutDialog(requireContext()).show()
    }

    // --- Menu Item Clicks ---
    private fun openSettings() {
        showAboutDialogNew()
    }

    private fun showAdsInfo() {
        AlertDialog.Builder(requireContext())
            .setTitle("ข้อมูลโฆษณา")
            .setMessage("โฆษณาในแอปพลิเคชันนี้ช่วยให้เราสามารถ\nพัฒนาและปรับปรุงแอปฯ ให้ดีขึ้นอย่างต่อเนื่อง\n\nขอบคุณสำหรับการสนับสนุน!")
            .setPositiveButton("เข้าใจแล้ว", null)
            .show()
    }

    private fun getAllItemObject(): List<ItemObjectAndroidGridView> {
        return listOf(
            ItemObjectAndroidGridView("ฝึกอ่าน", R.drawable.doing_learn_01, R.raw.animation_thai, 0),
            ItemObjectAndroidGridView("ฝึกเขียน ก - ฮ", R.drawable.card_one, R.raw.animation_thai, 1),
            ItemObjectAndroidGridView("ฝึกเขียน A - Z", R.drawable.card_two, 2),
            ItemObjectAndroidGridView("จัดทำโดย", R.drawable.card_three, 3)
        )
    }

    // --- Lifecycle ของ Fragment ---
    override fun onResume() {
        super.onResume()
       // binding.adView.resume()
    }

    override fun onPause() {
        super.onPause()
        //binding.adView.pause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.recyclerView.adapter = null
       // binding.adView.destroy()
        _binding = null
    }

    // Inner class สำหรับ Grid Item Decoration แบบง่ายๆ
    inner class SimpleGridItemDecoration(private val spacing: Int) : RecyclerView.ItemDecoration() {
        override fun getItemOffsets(
            outRect: Rect,
            view: View,
            parent: RecyclerView,
            state: RecyclerView.State
        ) {
            val position = parent.getChildAdapterPosition(view)
            val column = position % 2 // 2 columns

            // ตั้งค่า spacing แบบง่ายๆ
            outRect.left = spacing / 2
            outRect.right = spacing / 2
            outRect.top = spacing / 2
            outRect.bottom = spacing / 2
        }
    }

    // Inner class สำหรับ List Item Decoration แบบง่ายๆ
    inner class SimpleListItemDecoration(private val spacing: Int) : RecyclerView.ItemDecoration() {
        override fun getItemOffsets(
            outRect: Rect,
            view: View,
            parent: RecyclerView,
            state: RecyclerView.State
        ) {
            outRect.left = spacing
            outRect.right = spacing
            outRect.bottom = spacing

            // Item แรกมี margin-top ด้วย
            if (parent.getChildAdapterPosition(view) == 0) {
                outRect.top = spacing
            } else {
                outRect.top = 0
            }
        }
    }
}