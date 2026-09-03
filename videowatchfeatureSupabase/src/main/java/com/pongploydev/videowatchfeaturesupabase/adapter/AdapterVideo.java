package com.pongploydev.videowatchfeaturesupabase.adapter;

import static com.pongploydev.videowatchfeaturesupabase.Constant.VIDEO_LIST_COMPACT;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.MotionEvent;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.pongploydev.videowatchfeaturesupabase.Constant;
import com.pongploydev.videowatchfeaturesupabase.R;
import com.pongploydev.videowatchfeaturesupabase.config.AppConfig;
import com.pongploydev.videowatchfeaturesupabase.databases.prefs.AdsPref;
import com.pongploydev.videowatchfeaturesupabase.databases.prefs.SharedPref;
import com.pongploydev.videowatchfeaturesupabase.model.Video;

import java.util.ArrayList;
import java.util.List;

public class AdapterVideo extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final int VIEW_AD = 2;
    private final int VIEW_ITEM = 1;
    private final int VIEW_PROG = 0;

    private List<Video> videos;

    private boolean loading;
    private OnLoadMoreListener onLoadMoreListener;

    private int lastPosition = -1;

    Context context;
    private OnItemClickListener mOnItemClickListener;
    private OnItemLongClickListener mOnItemLongClickListener;
    private OnItemOverflowClickListener mOnItemOverflowClickListener;

    boolean scrolling = false;
    SharedPref sharedPref;
    AdsPref adsPref;

    // เพิ่ม Handler
    private final Handler handler = new Handler(Looper.getMainLooper());

    public interface OnItemClickListener {
        void onItemClick(View view, Video obj, int position);
    }

    public interface OnItemLongClickListener {
        void onItemLongClick(View view, Video obj, int position, OriginalViewHolder holder);
    }

    public interface OnItemOverflowClickListener {
        void onItemOverflowClick(View view, Video obj, int position);
    }

    public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mOnItemClickListener = mItemClickListener;
    }

    public void setOnItemLongClickListener(final OnItemLongClickListener mItemLongClickListener) {
        this.mOnItemLongClickListener = mItemLongClickListener;
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

    public AdapterVideo(Context context, RecyclerView view) {
        this(context, view, new ArrayList<>());
    }

    public class OriginalViewHolder extends RecyclerView.ViewHolder {

        public TextView categoryName;
        public TextView videoTitle;
        public TextView videoDuration;
        public ImageView videoThumbnail;
        public View lytParent;
        public ImageButton overflow;
        public android.widget.FrameLayout lytYouTubeContainer;
        public ImageView imgPlay;
        public TextView txtAgeBadge;
        public TextView videoDescription;
        public TextView txtStarReward;

        @SuppressLint("WrongViewCast")
        public OriginalViewHolder(View v) {
            super(v);
            categoryName = v.findViewById(R.id.category_name);
            videoTitle = v.findViewById(R.id.video_title);
            videoDuration = v.findViewById(R.id.video_duration);
            videoThumbnail = v.findViewById(R.id.video_thumbnail);
            lytParent = v.findViewById(R.id.lyt_parent);
            overflow = v.findViewById(R.id.overflow);
            lytYouTubeContainer = v.findViewById(R.id.lyt_youtube_container);
            imgPlay = v.findViewById(R.id.img_play);
            txtAgeBadge = v.findViewById(R.id.txt_age_badge);
            videoDescription = v.findViewById(R.id.video_description);
            txtStarReward = v.findViewById(R.id.txt_star_reward);
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
            vItem.videoTitle.setSelected(true);
            vItem.videoDuration.setText(video.video_duration);

            if (vItem.videoDescription != null) {
                if (video.video_description != null && !video.video_description.trim().isEmpty()) {
                    vItem.videoDescription.setText(video.video_description.trim());
                } else if (video.tags != null && !video.tags.trim().isEmpty()) {
                    vItem.videoDescription.setText(video.tags.trim());
                } else {
                    vItem.videoDescription.setText("คลิปเรียนรู้เสริมทักษะแสนสนุก 🌟");
                }
            }

            if (vItem.txtStarReward != null) {
                vItem.txtStarReward.setText("+5 ⭐");
            }

            if (vItem.txtAgeBadge != null) {
                if (video.age_rating != null && !video.age_rating.isEmpty()) {
                    vItem.txtAgeBadge.setVisibility(View.VISIBLE);
                    vItem.txtAgeBadge.setText(video.age_rating);
                    
                    if (vItem.txtAgeBadge.getBackground() != null) {
                        try {
                            String age = video.age_rating.toLowerCase();
                            int colorRes = R.color.kids_secondary;
                            if (age.contains("all") || age.contains("0+")) {
                                colorRes = R.color.kids_green;
                            } else if (age.contains("3+")) {
                                colorRes = R.color.kids_blue;
                            } else if (age.contains("6+")) {
                                colorRes = R.color.kids_accent;
                            }
                            vItem.txtAgeBadge.getBackground().mutate().setColorFilter(ContextCompat.getColor(context, colorRes), PorterDuff.Mode.SRC_IN);
                        } catch (Exception ignored) {}
                    }
                } else {
                    vItem.txtAgeBadge.setVisibility(View.GONE);
                }
            }

            // Reset preview states to prevent flickering from recycled views
            vItem.lytYouTubeContainer.setVisibility(View.GONE);
            vItem.lytYouTubeContainer.removeAllViews();
            vItem.imgPlay.setVisibility(View.VISIBLE);

            if (video.video_type != null && video.video_type.equalsIgnoreCase("youtube")) {
                if (video.video_thumbnail == null || video.video_thumbnail.equals("")) {
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
                    String thumbnailUrl = video.video_thumbnail;
                    if (!thumbnailUrl.startsWith("http")) {
                        String baseUrl = sharedPref.getBaseUrl();
                        if (baseUrl.endsWith("/")) baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
                        thumbnailUrl = baseUrl + "/video_image/" + Uri.encode(thumbnailUrl);
                    } else {
                        // Handle spaces and special characters in absolute URL
                        thumbnailUrl = Uri.parse(thumbnailUrl).toString();
                    }
                    
                    Glide.with(context)
                            .load(thumbnailUrl)
                            .placeholder(R.drawable.ic_thumbnail)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .into(vItem.videoThumbnail);
                }
            } else {
                String thumbnailUrl = video.video_thumbnail;
                if (thumbnailUrl != null && !thumbnailUrl.isEmpty()) {
                    if (!thumbnailUrl.startsWith("http")) {
                        String baseUrl = sharedPref.getBaseUrl();
                        if (baseUrl.endsWith("/")) baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
                        thumbnailUrl = baseUrl + "/video_image/" + Uri.encode(thumbnailUrl);
                    } else {
                        thumbnailUrl = Uri.parse(thumbnailUrl).toString();
                    }

                    Glide.with(context)
                            .load(thumbnailUrl)
                            .placeholder(R.drawable.ic_thumbnail)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .into(vItem.videoThumbnail);
                } else {
                    vItem.videoThumbnail.setImageResource(R.drawable.ic_thumbnail);
                }
            }

            vItem.lytParent.setOnClickListener(view -> {
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClick(view, video, position);
                }
            });

            vItem.lytParent.setOnLongClickListener(view -> {
                if (mOnItemLongClickListener != null) {
                    mOnItemLongClickListener.onItemLongClick(view, video, position, vItem);
                    return true;
                }
                return false;
            });

            if (vItem.overflow != null) {
                if (sharedPref.getIsDarkTheme()) {
                    vItem.overflow.setColorFilter(ContextCompat.getColor(context, R.color.color_dark_icon),
                            PorterDuff.Mode.SRC_IN);
                } else {
                    vItem.overflow.setColorFilter(ContextCompat.getColor(context, R.color.color_light_icon),
                            PorterDuff.Mode.SRC_IN);
                }

                vItem.overflow.setOnClickListener(view -> {
                    if (mOnItemOverflowClickListener != null) {
                        mOnItemOverflowClickListener.onItemOverflowClick(view, video, position);
                    }
                });
            }

            // Set Entrance Animation
            setAnimation(vItem.itemView, position);

            // Add Click Feedback (Scale Effect)
            addClickEffect(vItem.lytParent);

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

    public void updateData(List<Video> newVideos) {
        handler.post(() -> {
            if (newVideos == null) return;
            try {
                androidx.recyclerview.widget.DiffUtil.DiffResult diffResult = androidx.recyclerview.widget.DiffUtil.calculateDiff(new androidx.recyclerview.widget.DiffUtil.Callback() {
                    @Override
                    public int getOldListSize() {
                        return videos.size();
                    }

                    @Override
                    public int getNewListSize() {
                        return newVideos.size();
                    }

                    @Override
                    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                        Video oldVideo = videos.get(oldItemPosition);
                        Video newVideo = newVideos.get(newItemPosition);
                        if (oldVideo == null || newVideo == null) return oldVideo == newVideo;
                        if (oldVideo.vid != null && newVideo.vid != null) {
                            return oldVideo.vid.equals(newVideo.vid);
                        }
                        return oldVideo.id == newVideo.id;
                    }

                    @Override
                    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                        Video oldVideo = videos.get(oldItemPosition);
                        Video newVideo = newVideos.get(newItemPosition);
                        if (oldVideo == null || newVideo == null) return oldVideo == newVideo;
                        return java.util.Objects.equals(oldVideo.video_title, newVideo.video_title) &&
                               java.util.Objects.equals(oldVideo.video_thumbnail, newVideo.video_thumbnail) &&
                               java.util.Objects.equals(oldVideo.video_duration, newVideo.video_duration);
                    }
                });

                videos.clear();
                videos.addAll(newVideos);
                diffResult.dispatchUpdatesTo(AdapterVideo.this);
            } catch (Exception e) {
                videos.clear();
                videos.addAll(newVideos);
                notifyDataSetChanged();
            }
        });
    }

    public void insertData(List<Video> videos) {
        handler.post(() -> {
            if (videos == null || videos.isEmpty())
                return;

            int positionStart = getItemCount();
            int itemCount = videos.size();
            this.videos.addAll(videos);
            notifyItemRangeInserted(positionStart, itemCount);
        });
    }

    public void setItems(List<Video> newVideos) {
        handler.post(() -> {
            this.videos = new ArrayList<>(newVideos);
            notifyDataSetChanged();
        });
    }

    public void setLoaded() {
        handler.post(() -> {
            loading = false;
            for (int i = getItemCount() - 1; i >= 0; i--) {
                if (i < videos.size() && videos.get(i) == null) {
                    videos.remove(i);
                    notifyItemRemoved(i);
                }
            }
        });
    }

    public void setLoading() {
        if (getItemCount() != 0) {
            handler.post(() -> {
                if (videos.isEmpty() || videos.get(videos.size() - 1) != null) {
                    this.videos.add(null);
                    notifyItemInserted(getItemCount() - 1);
                }
                loading = true;
            });
        }
    }

    public void resetListData() {
        handler.post(() -> {
            this.videos = new ArrayList<>();
            this.lastPosition = -1;
            notifyDataSetChanged();
        });
    }

    public void setOnLoadMoreListener(OnLoadMoreListener onLoadMoreListener) {
        this.onLoadMoreListener = onLoadMoreListener;
    }

    private void lastItemViewDetector(RecyclerView recyclerView) {
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                // ตรวจสอบว่าไม่ใช่ในระหว่าง layout
                if (recyclerView.isComputingLayout()) {
                    return;
                }

                RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
                int lastPos = -1;

                if (layoutManager instanceof StaggeredGridLayoutManager) {
                    lastPos = getLastVisibleItem(((StaggeredGridLayoutManager) layoutManager).findLastVisibleItemPositions(null));
                } else if (layoutManager instanceof LinearLayoutManager) {
                    lastPos = ((LinearLayoutManager) layoutManager).findLastVisibleItemPosition();
                }

                if (!loading && lastPos >= getItemCount() - 2 && onLoadMoreListener != null) {
                    int current_page = getItemCount() / (AppConfig.LOAD_MORE);
                    loading = true;

                    handler.post(() -> {
                        if (onLoadMoreListener != null) {
                            setLoading();
                            onLoadMoreListener.onLoadMore(current_page);
                        }
                    });
                }
            }
        });
    }

    public interface OnLoadMoreListener {
        void onLoadMore(int current_page);
    }

    private int getLastVisibleItem(int[] into) {
        int lastIdx = into[0];
        for (int i : into) {
            if (lastIdx < i)
                lastIdx = i;
        }
        return lastIdx;
    }

    private void setAnimation(View viewToAnimate, int position) {
        if (position > lastPosition) {
            Animation animation = AnimationUtils.loadAnimation(context, R.anim.anim_pop_up);
            viewToAnimate.startAnimation(animation);
            lastPosition = position;
        }
    }

    private void addClickEffect(View view) {
        view.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    ObjectAnimator scaleDown = ObjectAnimator.ofPropertyValuesHolder(
                            v,
                            PropertyValuesHolder.ofFloat("scaleX", 0.95f),
                            PropertyValuesHolder.ofFloat("scaleY", 0.95f));
                    scaleDown.setDuration(100);
                    scaleDown.start();
                    break;

                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    ObjectAnimator scaleUp = ObjectAnimator.ofPropertyValuesHolder(
                            v,
                            PropertyValuesHolder.ofFloat("scaleX", 1.0f),
                            PropertyValuesHolder.ofFloat("scaleY", 1.0f));
                    scaleUp.setDuration(100);
                    scaleUp.start();
                    break;
            }
            return false; // dispatch click event normally
        });
    }
}