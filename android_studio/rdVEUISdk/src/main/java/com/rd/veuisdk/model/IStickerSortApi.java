package com.rd.veuisdk.model;

import android.os.Parcel;
import android.os.Parcelable;

public class IStickerSortApi implements Parcelable {

    private String id;
    private String name;
    private String updatetime;
    private String icon;
    private String iconP;

    public String getIconP() {
        return iconP;
    }

    public void setIconP(String iconP) {
        this.iconP = iconP;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUpdatetime() {
        return updatetime;
    }

    public void setUpdatetime(String updatetime) {
        this.updatetime = updatetime;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeString(this.name);
        dest.writeString(this.updatetime);
        dest.writeString(this.icon);
        dest.writeString(this.iconP);
    }

    public IStickerSortApi() {
    }

    protected IStickerSortApi(Parcel in) {
        this.id = in.readString();
        this.name = in.readString();
        this.updatetime = in.readString();
        this.icon = in.readString();
        this.iconP = in.readString();
    }

    public static final Parcelable.Creator<IStickerSortApi> CREATOR = new Parcelable.Creator<IStickerSortApi>() {
        @Override
        public IStickerSortApi createFromParcel(Parcel source) {
            return new IStickerSortApi(source);
        }

        @Override
        public IStickerSortApi[] newArray(int size) {
            return new IStickerSortApi[size];
        }
    };
}
