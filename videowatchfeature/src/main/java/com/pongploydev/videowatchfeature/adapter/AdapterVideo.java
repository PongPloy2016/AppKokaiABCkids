package com.pongploydev.videowatchfeature.adapter;

import static com.pongploydev.videowatchfeature.Constant.VIDEO_LIST_COMPACT;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.PorterDuff;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;


import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.pongploydev.videowatchfeature.Constant;
import com.pongploydev.videowatchfeature.R;
import com.pongploydev.videowatchfeature.config.AppConfig;
import com.pongploydev.videowatchfeature.databases.prefs.AdsPref;
import com.pongploydev.videowatchfeature.databases.prefs.SharedPref;
import com.pongploydev.videowatchfeature.model.Video;
import com.pongploydev.videowatchfeature.utils.Tools;

import java.util.ArrayList;
import java.util.List;

public class AdapterVideo extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final int VIEW_AD = 2;
    private final int VIEW_ITEM = 1;
    private final int VIEW_PROG = 0;

    private List<Video> videos;

    private boolean loading;
    private OnLoadMoreListener onLoadMoreListener;

    Context context;
    private OnItemClickListener mOnItemClickListener;
    private OnItemOverflowClickListener mOnItemOverflowClickListener;

    boolean scrolling = false;
    SharedPref sharedPref;
    AdsPref adsPref;

    // เพิ่ม Handler
    private final Handler handler = new Handler(Looper.getMainLooper());

    public interface OnItemClickListener {
        void onItemClick(View view, Video obj, int position);
    }

    public interface OnItemOverflowClickListener {
        void onItemOverflowClick(View view, Video obj, int position);
    }

    public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mOnItemClickListener = mItemClickListener;
    }

    public void setOnItemOverflowClickListener(final OnItemOverflowClickListener mItemOverflowClickListener) {
        this.mOnItemOverflowClickListener = mItemOverflowClickListener;
    }

    public AdapterVideo(Context context, RecyclerView view, List<Video> videos) {
        this.videos = videos;
        this.context = context;
        this.sharedPref = new SharedPref(context);
        this.adsPref = new AdsPref(context);
        lastItemViewDetector(view);

        view.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    scrolling = true;
                } else if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    scrolling = false;
                }
                super.onScrollStateChanged(recyclerView, newState);
            }
        });
    }

    public class OriginalViewHolder extends RecyclerView.ViewHolder {

        public TextView categoryName;
        public TextView videoTitle;
        public TextView videoDuration;
        public TextView totalViews;
        public TextView dateTime;
        public LinearLayout lytView;
        public LinearLayout lytDate;
        public ImageView videoThumbnail;
        public ConstraintLayout lytParent;
        public ImageButton overflow;

        @SuppressLint("WrongViewCast")
        public OriginalViewHolder(View v) {
            super(v);
            categoryName = v.findViewById(R.id.category_name);
            videoTitle = v.findViewById(R.id.video_title);
            videoDuration = v.findViewById(R.id.video_duration);
            dateTime = v.findViewById(R.id.date_time);
            totalViews = v.findViewById(R.id.total_views);
            lytView = v.findViewById(R.id.lyt_view_count);
            lytDate = v.findViewById(R.id.lyt_date);
            videoThumbnail = v.findViewById(R.id.video_thumbnail);
            lytParent = v.findViewById(R.id.lyt_parent);
            overflow = v.findViewById(R.id.overflow);

        }

    }

    public static class ProgressViewHolder extends RecyclerView.ViewHolder {
        public ProgressBar progressBar;

        public ProgressViewHolder(View v) {
            super(v);
            progressBar = v.findViewById(R.id.progressBar1);
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;
        if (viewType == VIEW_ITEM) {
            if (sharedPref.getVideoViewType() == VIDEO_LIST_COMPACT) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_video_compact, parent, false);
                vh = new OriginalViewHolder(v);
            } else {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_video_kids, parent, false);
                vh = new OriginalViewHolder(v);
            }
        } else {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_load_more, parent, false);
            vh = new ProgressViewHolder(v);
        }
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof OriginalViewHolder) {
            final Video video = videos.get(position);
            final OriginalViewHolder vItem = (OriginalViewHolder) holder;

            vItem.categoryName.setText(video.category_name);
            vItem.videoTitle.setText(video.video_title);
            vItem.videoDuration.setText(video.video_duration);
            if (AppConfig.ENABLE_VIEW_COUNT) {
                vItem.totalViews.setText(Tools.withSuffix(video.total_views) + " " + context.getResources().getString(R.string.views_count));
            } else {
                vItem.lytView.setVisibility(View.GONE);
            }

            if (AppConfig.ENABLE_DATE_DISPLAY && AppConfig.DISPLAY_DATE_AS_TIME_AGO) {
                vItem.dateTime.setText(Tools.getTimeAgo(video.date_time));
            } else if (AppConfig.ENABLE_DATE_DISPLAY && !AppConfig.DISPLAY_DATE_AS_TIME_AGO) {
                vItem.dateTime.setText(Tools.getFormatedDateSimple(video.date_time));
            } else {
                vItem.lytDate.setVisibility(View.GONE);
            }

            if (video.video_type != null && video.video_type.equals("youtube")) {
                if (video.video_thumbnail.equals("")) {
                    if (sharedPref.getVideoViewType() == VIDEO_LIST_COMPACT) {
                        Glide.with(context)
                                .load(Constant.YOUTUBE_IMAGE_FRONT + video.video_id + Constant.YOUTUBE_IMAGE_BACK_MQ)
                                .placeholder(R.drawable.ic_thumbnail)
                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                .into(vItem.videoThumbnail);
                    } else {
                        Glide.with(context)
                                .load(Constant.YOUTUBE_IMAGE_FRONT + video.video_id + Constant.YOUTUBE_IMAGE_BACK_HQ)
                                .placeholder(R.drawable.ic_thumbnail)
                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                .into(vItem.videoThumbnail);
                    }
                } else {
                    Glide.with(context)
                            .load(sharedPref.getBaseUrl() + "/upload/" + video.video_thumbnail)
                            .placeholder(R.drawable.ic_thumbnail)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .into(vItem.videoThumbnail);
                }
            } else {
                Glide.with(context)
                        .load(sharedPref.getBaseUrl() + "/upload/" + video.video_thumbnail)
                        .placeholder(R.drawable.ic_thumbnail)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(vItem.videoThumbnail);
            }

            vItem.lytParent.setOnClickListener(view -> {
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClick(view, video, position);
                }
            });

            if (sharedPref.getIsDarkTheme()) {
                vItem.overflow.setColorFilter(ContextCompat.getColor(context, R.color.color_dark_icon), PorterDuff.Mode.SRC_IN);
            } else {
                vItem.overflow.setColorFilter(ContextCompat.getColor(context, R.color.color_light_icon), PorterDuff.Mode.SRC_IN);
            }

            vItem.overflow.setOnClickListener(view -> {
                if (mOnItemOverflowClickListener != null) {
                    mOnItemOverflowClickListener.onItemOverflowClick(view, video, position);
                }
            });

        } else {
            ((ProgressViewHolder) holder).progressBar.setIndeterminate(true);
        }
    }

    @Override
    public int getItemCount() {
        return videos.size();
    }

    @Override
    public int getItemViewType(int position) {
        Video video = videos.get(position);
        if (video != null) {
            return VIEW_ITEM;
        } else {
            return VIEW_PROG;
        }
    }

    public void insertData(List<Video> videos) {
        handler.post(() -> {
            if (videos == null || videos.isEmpty()) return;

            int positionStart = getItemCount();
            int itemCount = videos.size();
            this.videos.addAll(videos);
            notifyItemRangeInserted(positionStart, itemCount);
        });
    }

    public void setLoaded() {
        handler.post(() -> {
            loading = false;
            // ตรวจสอบจากท้ายไปหน้าเพื่อป้องกัน ConcurrentModificationException
            for (int i = getItemCount() - 1; i >= 0; i--) {
                if (videos.get(i) == null) {
                    videos.remove(i);
                    notifyItemRemoved(i);
                }
            }
        });
    }

    public void setLoading() {
        if (getItemCount() != 0) {
            handler.post(() -> {
                if (!loading) {
                    this.videos.add(null);
                    notifyItemInserted(getItemCount() - 1);
                    loading = true;
                }
            });
        }
    }

    public void resetListData() {
        handler.post(() -> {
            this.videos = new ArrayList<>();
            notifyDataSetChanged();
        });
    }

    public void setOnLoadMoreListener(OnLoadMoreListener onLoadMoreListener) {
        this.onLoadMoreListener = onLoadMoreListener;
    }

    private void lastItemViewDetector(RecyclerView recyclerView) {
        if (recyclerView.getLayoutManager() instanceof StaggeredGridLayoutManager) {
            final StaggeredGridLayoutManager layoutManager = (StaggeredGridLayoutManager) recyclerView.getLayoutManager();
            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);

                    // ตรวจสอบว่าไม่ใช่ในระหว่าง layout
                    if (recyclerView.isComputingLayout()) {
                        return;
                    }

                    int lastPos = getLastVisibleItem(layoutManager.findLastVisibleItemPositions(null));
                    if (!loading && lastPos == getItemCount() - 1 && onLoadMoreListener != null) {
                        int current_page = getItemCount() / (AppConfig.LOAD_MORE);

                        // ใช้ handler เพื่อเลื่อนการเรียกไปยังเฟรมถัดไป
                        handler.post(() -> {
                            if (!loading && onLoadMoreListener != null) {
                                onLoadMoreListener.onLoadMore(current_page);
                                loading = true;
                            }
                        });
                    }
                }
            });
        }
    }

    public interface OnLoadMoreListener {
        void onLoadMore(int current_page);
    }

    private int getLastVisibleItem(int[] into) {
        int lastIdx = into[0];
        for (int i : into) {
            if (lastIdx < i) lastIdx = i;
        }
        return lastIdx;
    }
}