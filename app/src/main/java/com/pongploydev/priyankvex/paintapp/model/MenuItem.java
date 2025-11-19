package com.pongploydev.priyankvex.paintapp.model;

import android.os.Parcel;
import android.os.Parcelable;

public class MenuItem implements Parcelable {
    private String name;
    private int image;
    private int id;

    public MenuItem(String name, int image, int id) {
        this.name = name;
        this.image = image;
        this.id = id;
    }

    protected MenuItem(Parcel in) {
        name = in.readString();
        image = in.readInt();
        id = in.readInt();
    }

    public static final Creator<MenuItem> CREATOR = new Creator<MenuItem>() {
        @Override
        public MenuItem createFromParcel(Parcel in) {
            return new MenuItem(in);
        }

        @Override
        public MenuItem[] newArray(int size) {
            return new MenuItem[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeInt(image);
        dest.writeInt(id);
    }

    // Getters
    public String getName() { return name; }
    public int getImage() { return image; }
    public int getId() { return id; }

    // Setters
    public void setName(String name) { this.name = name; }
    public void setImage(int image) { this.image = image; }
    public void setId(int id) { this.id = id; }
}