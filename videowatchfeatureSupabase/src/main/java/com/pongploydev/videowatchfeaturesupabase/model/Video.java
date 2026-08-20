package com.pongploydev.videowatchfeaturesupabase.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import java.util.Locale;

import com.google.gson.annotations.SerializedName;

public class Video implements Parcelable {
    public int id;
    @SerializedName("cat_id")
    public int cat_id;
    @SerializedName("category_name")
    public String category_name = "";
    @SerializedName("vid")
    public String vid = "";
    @SerializedName("video_title")
    public String video_title = "";
    @SerializedName("video_url")
    public String video_url = "";
    @SerializedName("video_id")
    public String video_id = "";
    @SerializedName("video_thumbnail")
    public String video_thumbnail = "";
    @SerializedName("video_duration")
    public String video_duration = "";
    @SerializedName("video_description")
    public String video_description = "";
    @SerializedName("video_type")
    public String video_type = "";
    @SerializedName("size")
    public String size = "";
    @SerializedName("total_views")
    public long total_views;
    @SerializedName("date_time")
    public String date_time = "";

    // ฟิลด์เพิ่มเติมสำหรับการค้นหาและแสดงผล
    public boolean isNew = false; // วิดีโอใหม่
    public String tags = ""; // แท็กสำหรับการค้นหา
    public String age_rating = ""; // อายุที่เหมาะสม
    public String language = ""; // ภาษา
    public float rating = 0.0f; // คะแนน 0-5
    public String resolution = ""; // ความละเอียด (HD, SD)
    public boolean isFavorite = false; // วิดีโอที่ชอบ
    public int watch_count = 0; // จำนวนครั้งที่ดูแล้ว
    public String last_watched = ""; // ครั้งสุดท้ายที่ดู

    // สำหรับการกรองหมวดหมู่
    public static final String CATEGORY_CARTOON = "cartoon";
    public static final String CATEGORY_MUSIC = "music";
    public static final String CATEGORY_LEARNING = "learning";
    public static final String CATEGORY_STORY = "story";

    public Video() {}

    public Video(String vid) {
        this.vid = vid;
    }

    public Video(String category_name, String vid, String video_title, String video_url,
                 String video_id, String video_thumbnail, String video_duration,
                 String video_description, String video_type, long total_views, String date_time) {
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

        // ตั้งค่าพื้นฐาน
        initializeDefaultValues();
    }

    // ฟังก์ชันตั้งค่าค่าเริ่มต้น
    private void initializeDefaultValues() {
        // ตรวจสอบว่าเป็นวิดีโอใหม่หรือไม่ (ภายใน 7 วัน)
        this.isNew = VideoHelper.isVideoNew(this.date_time);

        // ตั้งค่าแท็กอัตโนมัติจากหมวดหมู่และคำสำคัญ
        this.tags = generateTags();

        // ตั้งค่าอายุที่เหมาะสมตามหมวดหมู่
        this.age_rating = determineAgeRating();

        // ตั้งค่าภาษา (สมมติว่าภาษาไทย)
        this.language = "th";

        // คะแนนเริ่มต้น
        this.rating = 4.5f;

        // ความละเอียด
        this.resolution = "HD";
    }

    // 🧱 Parcelable implementation
    protected Video(Parcel in) {
        id = in.readInt();
        cat_id = in.readInt();
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
        isNew = in.readByte() != 0;
        tags = in.readString();
        age_rating = in.readString();
        language = in.readString();
        rating = in.readFloat();
        resolution = in.readString();
        isFavorite = in.readByte() != 0;
        watch_count = in.readInt();
        last_watched = in.readString();
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
        parcel.writeInt(cat_id);
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
        parcel.writeByte((byte) (isNew ? 1 : 0));
        parcel.writeString(tags);
        parcel.writeString(age_rating);
        parcel.writeString(language);
        parcel.writeFloat(rating);
        parcel.writeString(resolution);
        parcel.writeByte((byte) (isFavorite ? 1 : 0));
        parcel.writeInt(watch_count);
        parcel.writeString(last_watched);
    }

    // ==================== ฟังก์ชันการค้นหาและกรอง ====================

    /**
     * ตรวจสอบว่าวิดีโอนี้ตรงกับคำค้นหาหรือไม่
     * @param query คำค้นหา
     * @return true ถ้าตรงกับคำค้นหา
     */
    public boolean matchesSearch(String query) {
        if (TextUtils.isEmpty(query)) {
            return true;
        }

        String lowercaseQuery = query.toLowerCase(Locale.getDefault());

        // ค้นหาในชื่อวิดีโอ
        if (!TextUtils.isEmpty(video_title) &&
                video_title.toLowerCase(Locale.getDefault()).contains(lowercaseQuery)) {
            return true;
        }

        // ค้นหาในคำอธิบาย
        if (!TextUtils.isEmpty(video_description) &&
                video_description.toLowerCase(Locale.getDefault()).contains(lowercaseQuery)) {
            return true;
        }

        // ค้นหาในหมวดหมู่
        if (!TextUtils.isEmpty(category_name) &&
                category_name.toLowerCase(Locale.getDefault()).contains(lowercaseQuery)) {
            return true;
        }

        // ค้นหาในแท็ก
        if (!TextUtils.isEmpty(tags) &&
                tags.toLowerCase(Locale.getDefault()).contains(lowercaseQuery)) {
            return true;
        }

        return false;
    }

    /**
     * ตรวจสอบว่าวิดีโอนี้อยู่ในหมวดหมู่ที่กำหนดหรือไม่
     * @param category หมวดหมู่ (all, cartoon, music, learning, story)
     * @return true ถ้าอยู่ในหมวดหมู่
     */
    public boolean isInCategory(String category) {
        if (TextUtils.isEmpty(category) || "all".equalsIgnoreCase(category)) {
            return true;
        }

        // ตรวจสอบจากชื่อหมวดหมู่
        if (!TextUtils.isEmpty(category_name)) {
            String lowercaseCategory = category.toLowerCase(Locale.getDefault());
            String lowercaseCategoryName = category_name.toLowerCase(Locale.getDefault());

            if (lowercaseCategoryName.contains(lowercaseCategory)) {
                return true;
            }

            switch (category.toLowerCase(Locale.getDefault())) {
                case "cartoon":
                    return lowercaseCategoryName.contains("การ์ตูน") ||
                            lowercaseCategoryName.contains("cartoon") ||
                            lowercaseCategoryName.contains("animation");
                case "music":
                    return lowercaseCategoryName.contains("เพลง") ||
                            lowercaseCategoryName.contains("music") ||
                            lowercaseCategoryName.contains("song");
                case "learning":
                case "math":
                    return lowercaseCategoryName.contains("เรียนรู้") ||
                            lowercaseCategoryName.contains("learning") ||
                            lowercaseCategoryName.contains("education") ||
                            lowercaseCategoryName.contains("สอน") ||
                            lowercaseCategoryName.contains("math") ||
                            lowercaseCategoryName.contains("คณิต");
                case "abc":
                case "english":
                    return lowercaseCategoryName.contains("abc") ||
                            lowercaseCategoryName.contains("english") ||
                            lowercaseCategoryName.contains("ภาษาอังกฤษ") ||
                            lowercaseCategoryName.contains("อักษร") ||
                            lowercaseCategoryName.contains("ก ไก่") ||
                            lowercaseCategoryName.contains("alphabet");
                case "story":
                    return lowercaseCategoryName.contains("นิทาน") ||
                            lowercaseCategoryName.contains("story") ||
                            lowercaseCategoryName.contains("tale") ||
                            lowercaseCategoryName.contains("เรื่อง");
            }
        }

        return false;
    }

    /**
     * ตรวจสอบว่าวิดีโอตรงกับเงื่อนไขการกรองทั้งหมดหรือไม่
     * @param query คำค้นหา
     * @param category หมวดหมู่
     * @param showNewOnly แสดงเฉพาะวิดีโอใหม่
     * @param minRating คะแนนขั้นต่ำ
     * @return true ถ้าตรงกับเงื่อนไขทั้งหมด
     */
    public boolean matchesFilters(String query, String category, boolean showNewOnly, float minRating) {
        // ตรวจสอบคำค้นหา
        if (!matchesSearch(query)) {
            return false;
        }

        // ตรวจสอบหมวดหมู่
        if (!isInCategory(category)) {
            return false;
        }

        // ตรวจสอบวิดีโอใหม่
        if (showNewOnly && !isNew) {
            return false;
        }

        // ตรวจสอบคะแนน
        if (rating < minRating) {
            return false;
        }

        return true;
    }

    /**
     * สร้างแท็กอัตโนมัติจากข้อมูลวิดีโอ
     */
    private String generateTags() {
        StringBuilder tagsBuilder = new StringBuilder();

        // เพิ่มชื่อหมวดหมู่
        if (!TextUtils.isEmpty(category_name)) {
            tagsBuilder.append(category_name).append(",");
        }

        // แยกคำจากชื่อวิดีโอ
        if (!TextUtils.isEmpty(video_title)) {
            String[] words = video_title.split("\\s+");
            for (String word : words) {
                if (word.length() > 2) { // เพิ่มเฉพาะคำที่มีความยาวมากกว่า 2 ตัวอักษร
                    tagsBuilder.append(word).append(",");
                }
            }
        }

        // เพิ่มตามประเภทวิดีโอ
        if (!TextUtils.isEmpty(video_type)) {
            tagsBuilder.append(video_type).append(",");
        }

        // เพิ่มตามอายุที่เหมาะสม
        tagsBuilder.append(age_rating).append(",");

        // ลบเครื่องหมายคอมมาสุดท้าย
        if (tagsBuilder.length() > 0) {
            tagsBuilder.setLength(tagsBuilder.length() - 1);
        }

        return tagsBuilder.toString();
    }

    /**
     * กำหนดอายุที่เหมาะสมจากหมวดหมู่
     */
    private String determineAgeRating() {
        if (TextUtils.isEmpty(category_name)) {
            return "all";
        }

        String lowercaseCategory = category_name.toLowerCase(Locale.getDefault());

        if (lowercaseCategory.contains("ทารก") || lowercaseCategory.contains("baby")) {
            return "0+";
        } else if (lowercaseCategory.contains("เด็กเล็ก") || lowercaseCategory.contains("toddler")) {
            return "2+";
        } else if (lowercaseCategory.contains("ก่อนวัยเรียน") || lowercaseCategory.contains("preschool")) {
            return "3+";
        } else if (lowercaseCategory.contains("เด็ก") || lowercaseCategory.contains("kids") ||
                lowercaseCategory.contains("การ์ตูน") || lowercaseCategory.contains("cartoon")) {
            return "6+";
        } else if (lowercaseCategory.contains("ครอบครัว") || lowercaseCategory.contains("family")) {
            return "all";
        } else {
            return "all";
        }
    }

    /**
     * ฟังก์ชันช่วยเหลือสำหรับการจัดรูปแบบ
     */
    public static class VideoHelper {

        /**
         * จัดรูปแบบจำนวนผู้ชม (เช่น 1.2K, 5.6M)
         */
        public static String formatViewCount(long viewCount) {
            if (viewCount < 1000) {
                return String.valueOf(viewCount);
            } else if (viewCount < 1000000) {
                return String.format(Locale.getDefault(), "%.1fK", viewCount / 1000.0);
            } else {
                return String.format(Locale.getDefault(), "%.1fM", viewCount / 1000000.0);
            }
        }

        /**
         * จัดรูปแบบระยะเวลาวิดีโอ
         */
        public static String formatDuration(String duration) {
            if (TextUtils.isEmpty(duration)) {
                return "00:00";
            }

            try {
                // แปลงจากวินาทีเป็น mm:ss
                int totalSeconds = Integer.parseInt(duration);
                int minutes = totalSeconds / 60;
                int seconds = totalSeconds % 60;

                return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
            } catch (NumberFormatException e) {
                return duration; // คืนค่าเดิมถ้าแปลงไม่ได้
            }
        }

        /**
         * ตรวจสอบว่าเป็นวิดีโอใหม่หรือไม่ (ภายใน 7 วัน)
         */
        public static boolean isVideoNew(String dateTime) {
            // ในกรณีจริงควรตรวจสอบจากวันที่
            // ตัวอย่างง่ายๆ: สุ่มคืนค่า
            return Math.random() > 0.7; // 30% เป็นวิดีโอใหม่
        }

        /**
         * รับสีสำหรับหมวดหมู่
         */
        public static String getCategoryColor(String category) {
            if (TextUtils.isEmpty(category)) {
                return "#4CAF50"; // สีพื้นฐาน
            }

            String lowercaseCategory = category.toLowerCase(Locale.getDefault());

            if (lowercaseCategory.contains("การ์ตูน") || lowercaseCategory.contains("cartoon")) {
                return "#2196F3"; // สีน้ำเงิน
            } else if (lowercaseCategory.contains("เพลง") || lowercaseCategory.contains("music")) {
                return "#E91E63"; // สีชมพู
            } else if (lowercaseCategory.contains("เรียนรู้") || lowercaseCategory.contains("learning")) {
                return "#4CAF50"; // สีเขียว
            } else if (lowercaseCategory.contains("นิทาน") || lowercaseCategory.contains("story")) {
                return "#9C27B0"; // สีม่วง
            } else {
                return "#FF9800"; // สีส้ม
            }
        }
    }

    // ==================== Getter & Setter ====================

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCat_id() {
        return cat_id;
    }

    public void setCat_id(int cat_id) {
        this.cat_id = cat_id;
    }

    public String getCategory_name() {
        return category_name;
    }

    public void setCategory_name(String category_name) {
        this.category_name = category_name;
        // อัพเดทแท็กและอายุที่เหมาะสมเมื่อเปลี่ยนหมวดหมู่
        this.tags = generateTags();
        this.age_rating = determineAgeRating();
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
        // อัพเดทแท็กเมื่อเปลี่ยนชื่อวิดีโอ
        this.tags = generateTags();
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
        // อัพเดทแท็กเมื่อเปลี่ยนประเภทวิดีโอ
        this.tags = generateTags();
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
        // อัพเดทสถานะวิดีโอใหม่
        this.isNew = VideoHelper.isVideoNew(date_time);
    }

    // Getter & Setter ใหม่

    public boolean isNew() {
        return isNew;
    }

    public void setNew(boolean isNew) {
        this.isNew = isNew;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getAge_rating() {
        return age_rating;
    }

    public void setAge_rating(String age_rating) {
        this.age_rating = age_rating;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public String getResolution() {
        return resolution;
    }

    public void setResolution(String resolution) {
        this.resolution = resolution;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }

    public int getWatch_count() {
        return watch_count;
    }

    public void setWatch_count(int watch_count) {
        this.watch_count = watch_count;
    }

    public String getLast_watched() {
        return last_watched;
    }

    public void setLast_watched(String last_watched) {
        this.last_watched = last_watched;
    }

    // ==================== Utility Methods ====================

    /**
     * คืนค่าจำนวนผู้ชมที่จัดรูปแบบแล้ว
     */
    public String getFormattedViewCount() {
        return VideoHelper.formatViewCount(total_views);
    }

    /**
     * คืนค่าความยาววิดีโอที่จัดรูปแบบแล้ว
     */
    public String getFormattedDuration() {
        return VideoHelper.formatDuration(video_duration);
    }

    /**
     * เพิ่มจำนวนผู้ชม
     */
    public void incrementViewCount() {
        this.total_views++;
        this.watch_count++;
    }

    /**
     * สลับสถานะรายการโปรด
     */
    public void toggleFavorite() {
        this.isFavorite = !this.isFavorite;
    }

    /**
     * คืนค่าสีสำหรับหมวดหมู่
     */
    public String getCategoryColor() {
        return VideoHelper.getCategoryColor(category_name);
    }

    /**
     * ตรวจสอบว่าวิดีโอเป็น YouTube หรือไม่
     */
    public boolean isYouTubeVideo() {
        return "youtube".equalsIgnoreCase(video_type);
    }

    /**
     * ตรวจสอบว่าวิดีโอเป็นวิดีโอท้องถิ่นหรือไม่
     */
    public boolean isLocalVideo() {
        return "local".equalsIgnoreCase(video_type) ||
                "mp4".equalsIgnoreCase(video_type) ||
                (!isYouTubeVideo() && video_url.toLowerCase(Locale.getDefault()).endsWith(".mp4"));
    }

    @Override
    public String toString() {
        return "Video{" +
                "id=" + id +
                ", video_title='" + video_title + '\'' +
                ", category_name='" + category_name + '\'' +
                ", total_views=" + total_views +
                ", isNew=" + isNew +
                ", rating=" + rating +
                ", isFavorite=" + isFavorite +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Video video = (Video) o;

        if (id != video.id) return false;
        return vid != null ? vid.equals(video.vid) : video.vid == null;
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + (vid != null ? vid.hashCode() : 0);
        return result;
    }
}