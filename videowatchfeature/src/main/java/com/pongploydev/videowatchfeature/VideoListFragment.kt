package com.pongploydev.videowatchfeature

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.gson.Gson
import com.pongploydev.videowatchfeature.activity.ActivityVideoAutoDetail
import com.pongploydev.videowatchfeature.adapter.AdapterVideo
import com.pongploydev.videowatchfeature.callbacks.CallbackListVideo
import com.pongploydev.videowatchfeature.config.AppConfig
import com.pongploydev.videowatchfeature.databases.prefs.SharedPref
import com.pongploydev.videowatchfeature.model.Video
import com.pongploydev.videowatchfeature.rests.RestAdapter
import com.pongploydev.videowatchfeature.utils.EqualSpacingItemDecoration
import com.pongploydev.videowatchfeature.utils.Tools

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class VideoListFragment : Fragment() {
    var rootView: View? = null
    private var recyclerView: RecyclerView? = null
    private var adapterVideo: AdapterVideo? = null
    private var swipeRefreshLayout: SwipeRefreshLayout? = null
    private var callbackCall: Call<CallbackListVideo?>? = null
    private var shimmerFrameLayout: ShimmerFrameLayout? = null
    private var postTotal = 0
    private var failedPage = 0
    var sharedPref: SharedPref? = null
    var tools: Tools? = null
    var activity: Activity? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity = context as Activity
        if (getArguments() != null) {
            //categories = requireArguments().getSerializable(ARG_ID_CAT) as Category?
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(R.layout.fragment_video_list, container, false)

        tools = Tools(activity)
        sharedPref = SharedPref(activity)

        sharedPref!!.setDefaultSortHome()

        setHasOptionsMenu(true)


        shimmerFrameLayout =
            rootView!!.findViewById<ShimmerFrameLayout>(R.id.shimmer_view_container)
        swipeRefreshLayout =
            rootView!!.findViewById<SwipeRefreshLayout>(R.id.swipe_refresh_layout_home)
        swipeRefreshLayout!!.setColorSchemeResources(R.color.color_light_primary)

        recyclerView = rootView!!.findViewById<RecyclerView>(R.id.recyclerView)

        if (sharedPref!!.getVideoViewType() == Constant.VIDEO_LIST_COMPACT) {
            recyclerView!!.setPadding(
                0,
                getResources().getDimensionPixelOffset(R.dimen.spacing_small),
                0,
                getResources().getDimensionPixelOffset(
                    R.dimen.spacing_small
                )
            )
        }

        recyclerView!!.addItemDecoration(EqualSpacingItemDecoration(0))
        recyclerView!!.setLayoutManager(
            StaggeredGridLayoutManager(
                2,
                StaggeredGridLayoutManager.VERTICAL
            )
        )

        //set data and list adapter
        adapterVideo = AdapterVideo(activity, recyclerView, ArrayList<Video?>())
        recyclerView!!.setAdapter(adapterVideo)

        // on item list clicked
        adapterVideo!!.setOnItemClickListener(AdapterVideo.OnItemClickListener { v: View?, obj: Video?, position: Int ->
            Log.e("click video ", Gson().toJson(obj))
            openVideoActivity(obj)
        })

        adapterVideo!!.setOnItemOverflowClickListener(AdapterVideo.OnItemOverflowClickListener { view: View?, obj: Video?, position: Int ->
//            tools!!.showBottomSheetDialog(
//                activity, requireActivity().findViewById<View?>(
//                    R.id.coordinatorLayout
//                ), obj
//            )
        })

        // detect when scroll reach bottom
        adapterVideo!!.setOnLoadMoreListener(AdapterVideo.OnLoadMoreListener { current_page: Int ->
            this.setLoadMore(
                current_page
            )
        })

        // on swipe list
        swipeRefreshLayout!!.setOnRefreshListener(SwipeRefreshLayout.OnRefreshListener {
            if (callbackCall != null && callbackCall!!.isExecuted()) callbackCall!!.cancel()
            adapterVideo!!.resetListData()
            requestAction(1)
        })

        if (AppConfig.ALLOW_APP_ACCESSED_USING_VPN) {
            requestAction(1)
        } else {
//            if (Tools.isVpnConnectionAvailable()) {
//                Tools.showWarningDialog(
//                    activity,
//                    getString(R.string.vpn_detected),
//                    getString(R.string.close_vpn)
//                )
//            } else {
            requestAction(1)
            // }
        }

        initShimmerLayout()
        onSortButtonClickListener()

        return rootView
    }

    fun openVideoActivity(obj: Video?) {
        // Null safety check
        if (obj == null) {
            showErrorMessage("ข้อมูลวิดีโอไม่พร้อม")
            return
        }

        // Determine video type and create appropriate intent
        val intent = createVideoIntent(obj.video_type)

        // Show ads if applicable
        showInterstitialAdIfNeeded()

        // Start activity
        intent.putExtra(Constant.EXTRA_OBJC, obj)
        startActivity(intent)
    }

    private fun createVideoIntent(videoType: String?): Intent {
        return when (videoType?.lowercase()) {
            "youtube" -> Intent(activity, ActivityVideoAutoDetail::class.java)
            else -> Intent(activity, ActivityVideoAutoDetail::class.java)
        }
    }

    private fun showInterstitialAdIfNeeded() {
        try {
            (activity as? MainActivity)?.apply {
//                showInterstitialAd()
//                destroyBannerAd()
            }
        } catch (e: Exception) {
            Log.w("Ads", "Failed to show ads: ${e.message}")
        }
    }

    private fun showErrorMessage(message: String) {
        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
    }


// เรียกใช้งาน

    fun setLoadMore(current_page: Int) {
        Log.d("page", "currentPage: " + current_page)
        // Assuming final total items equal to real post items plus the ad
        val totalItemBeforeAds = (adapterVideo!!.getItemCount() - current_page)
        if (postTotal > totalItemBeforeAds && current_page != 0) {
            val next_page = current_page + 1
            requestAction(next_page)
        } else {
            adapterVideo!!.setLoaded()
        }
    }


    //    public void setLoadMore(int pageNo) {
    //        if (postTotal > adapterVideo.getItemCount()) {
    //            requestAction(pageNo + 1); // เลขหน้าต่อไป
    //        } else {
    //            adapterVideo.setLoaded();
    //        }
    //    }
    private fun displayApiResult(videos: MutableList<Video?>) {
        adapterVideo!!.insertData(videos)
        swipeProgress(false)
        if (videos.size == 0) {
            showNoItemView(true)
        }
    }

    private fun requestListPostApi(page_no: Int) {
        val apiInterface = RestAdapter.createAPI(sharedPref!!.getBaseUrl())

        if (sharedPref!!.getCurrentSortHome() == 0) {
            callbackCall = apiInterface.getVideos(
                page_no,
                AppConfig.LOAD_MORE,
                Constant.MOST_POPULAR,
                AppConfig.REST_API_KEY
            )
        } else if (sharedPref!!.getCurrentSortHome() == 1) {
            callbackCall = apiInterface.getVideos(
                page_no,
                AppConfig.LOAD_MORE,
                Constant.ADDED_OLDEST,
                AppConfig.REST_API_KEY
            )
        } else if (sharedPref!!.getCurrentSortHome() == 2) {
            callbackCall = apiInterface.getVideos(
                page_no,
                AppConfig.LOAD_MORE,
                Constant.ADDED_NEWEST,
                AppConfig.REST_API_KEY
            )
        } else {
            callbackCall = apiInterface.getVideos(
                page_no,
                AppConfig.LOAD_MORE,
                Constant.ADDED_NEWEST,
                AppConfig.REST_API_KEY
            )
        }

        callbackCall = apiInterface.getVideos(
            page_no,
            AppConfig.LOAD_MORE,
            Constant.ADDED_NEWEST,
            AppConfig.REST_API_KEY
        )

        callbackCall!!.enqueue(object : Callback<CallbackListVideo?> {
            override fun onResponse(
                p0: Call<CallbackListVideo?>,
                response: Response<CallbackListVideo?>
            ) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.e(
                        "API_RESPONSE",
                        "Code: " + response.code() + " Message: " + response.message()
                    )

                    val callbackListVideo: CallbackListVideo = response.body()!!
                    val resp: CallbackListVideo = response.body()!!
                    postTotal = resp.count_total // อัปเดตตัวแปรรวมโพสต์ทั้งหมด
                    // ตัวอย่างการใช้ข้อมูล
                    val videoList = callbackListVideo.posts
                    Log.e("videoList", Gson().toJson(videoList))
                    displayApiResult(videoList)
                } else {
                    Log.e(
                        "API_ERROR",
                        "Code: " + response.code() + " Message: " + response.message()
                    )
                    onFailRequest(page_no)
                }
            }

            override fun onFailure(call: Call<CallbackListVideo?>, t: Throwable) {
                if (!call.isCanceled()) {
                    onFailRequest(page_no)
                }
                Log.e("onFailure", t.message, t)
            }
        })
    }

    private fun onFailRequest(page_no: Int) {
        failedPage = page_no
        adapterVideo!!.setLoaded()
        swipeProgress(false)
        if (Tools.isConnect(activity)) {
            showFailedView(true, getString(R.string.failed_text))
        } else {
            showFailedView(true, getString(R.string.failed_text))
        }
    }

    private fun requestAction(page_no: Int) {
        showFailedView(false, "")
        showNoItemView(false)
        if (page_no == 1) {
            swipeProgress(true)
        } else {
            adapterVideo!!.setLoading()
        }
        Handler().postDelayed(Runnable { requestListPostApi(page_no) }, Constant.DELAY_TIME)
    }

    override fun onDestroy() {
        super.onDestroy()
        swipeProgress(false)
        if (callbackCall != null && callbackCall!!.isExecuted()) {
            callbackCall!!.cancel()
        }
        shimmerFrameLayout!!.stopShimmer()
    }

    private fun showFailedView(show: Boolean, message: String?) {
        val lyt_failed = rootView!!.findViewById<View>(R.id.lyt_failed_home)
        (rootView!!.findViewById<View?>(R.id.failed_message) as TextView).setText(message)
        if (show) {
            recyclerView!!.setVisibility(View.GONE)
            lyt_failed.setVisibility(View.VISIBLE)
        } else {
            recyclerView!!.setVisibility(View.VISIBLE)
            lyt_failed.setVisibility(View.GONE)
        }
        rootView!!.findViewById<View?>(R.id.failed_retry)
            .setOnClickListener(View.OnClickListener { view: View? -> requestAction(1) })
    }

    private fun showNoItemView(show: Boolean) {
        val lyt_no_item = rootView!!.findViewById<View>(R.id.lyt_no_item_home)
        (rootView!!.findViewById<View?>(R.id.no_item_message) as TextView).setText(R.string.msg_no_item)
        if (show) {
            recyclerView!!.setVisibility(View.GONE)
            lyt_no_item.setVisibility(View.VISIBLE)
        } else {
            recyclerView!!.setVisibility(View.VISIBLE)
            lyt_no_item.setVisibility(View.GONE)
        }
    }

    private fun swipeProgress(show: Boolean) {
        if (!show) {
            swipeRefreshLayout!!.setRefreshing(show)
            shimmerFrameLayout!!.setVisibility(View.GONE)
            shimmerFrameLayout!!.stopShimmer()
            return
        }
        swipeRefreshLayout!!.post(Runnable {
            swipeRefreshLayout!!.setRefreshing(show)
            shimmerFrameLayout!!.setVisibility(View.VISIBLE)
            shimmerFrameLayout!!.startShimmer()
        })
    }

    private fun initShimmerLayout() {
        val lyt_shimmer_default = rootView!!.findViewById<View>(R.id.lyt_shimmer_default)
        val lyt_shimmer_compact = rootView!!.findViewById<View>(R.id.lyt_shimmer_compact)
        if (sharedPref!!.getVideoViewType() == Constant.VIDEO_LIST_COMPACT) {
            lyt_shimmer_default.setVisibility(View.GONE)
            lyt_shimmer_compact.setVisibility(View.VISIBLE)
        } else {
            lyt_shimmer_default.setVisibility(View.VISIBLE)
            lyt_shimmer_compact.setVisibility(View.GONE)
        }
    }

    private fun onSortButtonClickListener() {
        //val mainActivity = activity as MainActivity?
     //   if (mainActivity != null) {
//            mainActivity.btnSort.setOnClickListener(view -> {
//                String[] items = getResources().getStringArray(R.array.dialog_single_choice_array);
//                int itemSelected = sharedPref.getCurrentSortHome();
//                new MaterialAlertDialogBuilder(activity)
//                        .setTitle(R.string.title_sort)
//                        .setSingleChoiceItems(items, itemSelected, (dialogInterface, position) -> {
//                            if (callbackCall != null && callbackCall.isExecuted())
//                                callbackCall.cancel();
//                            adapterVideo.resetListData();
//                            requestAction(1);
//                            sharedPref.updateSortHome(position);
//                            dialogInterface.dismiss();
//                        })
//                        .show();
//            });
 //       }
    }

    override fun onResume() {
        super.onResume()
    }

    companion object {
        const val ARG_ID_CAT: String = "id_cat"
        @JvmStatic
        fun newInstance(): VideoListFragment {
            val fragment = VideoListFragment()
            val args = Bundle()
           // args.putSerializable(ARG_ID_CAT, category)
            fragment.setArguments(args)
            return fragment
        }
    }
}
