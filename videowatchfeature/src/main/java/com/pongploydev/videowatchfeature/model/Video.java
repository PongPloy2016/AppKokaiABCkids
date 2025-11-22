package com.pongploydev.videowatchfeature.model;

import android.os.Parcel;
import android.os.Parcelable;


public class Video implements Parcelable {
    public int id;
    public String cat_id = "";
    public String category_name = "";
    public String vid = "";
    public String video_title = "";
    public String video_url = "";
    public String video_id = "";
    public String video_thumbnail = "";
    public String video_duration = "";
    public String video_description = "";
    public String video_type = "";
    public String size = "";
    public long total_views;
    public String date_time = "";

    public Video() {}

    public Video(String vid) {
        this.vid = vid;
    }

    public Video(String category_name, String vid, String video_title, String video_url, String video_id, String video_thumbnail, String video_duration, String video_description, String video_type, long total_views, String date_time) {
        this.category_name = category_name;
        this.vid = vid;
        this.video_title = video_title;
        this.video_url = video_url;
        this.video_id = video_id;
        this.video_thumbnail = video_thumbnail;
        this.video_duration = video_duration;
        this.video_description = video_description;
        this.video_type = video_type;
        this.total_views = total_views;
        this.date_time = date_time;
    }

    // 🧱 Parcelable implementation
    protected Video(Parcel in) {
        id = in.readInt();
        cat_id = in.readString();
        category_name = in.readString();
        vid = in.readString();
        video_title = in.readString();
        video_url = in.readString();
        video_id = in.readString();
        video_thumbnail = in.readString();
        video_duration = in.readString();
        video_description = in.readString();
        video_type = in.readString();
        size = in.readString();
        total_views = in.readLong();
        date_time = in.readString();
    }

    public static final Creator<Video> CREATOR = new Creator<Video>() {
        @Override
        public Video createFromParcel(Parcel in) {
            return new Video(in);
        }

        @Override
        public Video[] newArray(int size) {
            return new Video[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeInt(id);
        parcel.writeString(cat_id);
        parcel.writeString(category_name);
        parcel.writeString(vid);
        parcel.writeString(video_title);
        parcel.writeString(video_url);
        parcel.writeString(video_id);
        parcel.writeString(video_thumbnail);
        parcel.writeString(video_duration);
        parcel.writeString(video_description);
        parcel.writeString(video_type);
        parcel.writeString(size);
        parcel.writeLong(total_views);
        parcel.writeString(date_time);
    }

    // ... Getter & Setter methods remain the same ...

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCat_id() {
        return cat_id;
    }

    public void setCat_id(String cat_id) {
        this.cat_id = cat_id;
    }

    public String getCategory_name() {
        return category_name;
    }

    public void setCategory_name(String category_name) {
        this.category_name = category_name;
    }

    public String getVid() {
        return vid;
    }

    public void setVid(String vid) {
        this.vid = vid;
    }

    public String getVideo_title() {
        return video_title;
    }

    public void setVideo_title(String video_title) {
        this.video_title = video_title;
    }

    public String getVideo_url() {
        return video_url;
    }

    public void setVideo_url(String video_url) {
        this.video_url = video_url;
    }

    public String getVideo_id() {
        return video_id;
    }

    public void setVideo_id(String video_id) {
        this.video_id = video_id;
    }

    public String getVideo_thumbnail() {
        return video_thumbnail;
    }

    public void setVideo_thumbnail(String video_thumbnail) {
        this.video_thumbnail = video_thumbnail;
    }

    public String getVideo_duration() {
        return video_duration;
    }

    public void setVideo_duration(String video_duration) {
        this.video_duration = video_duration;
    }

    public String getVideo_description() {
        return video_description;
    }

    public void setVideo_description(String video_description) {
        this.video_description = video_description;
    }

    public String getVideo_type() {
        return video_type;
    }

    public void setVideo_type(String video_type) {
        this.video_type = video_type;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public long getTotal_views() {
        return total_views;
    }

    public void setTotal_views(long total_views) {
        this.total_views = total_views;
    }

    public String getDate_time() {
        return date_time;
    }

    public void setDate_time(String date_time) {
        this.date_time = date_time;
    }

}
