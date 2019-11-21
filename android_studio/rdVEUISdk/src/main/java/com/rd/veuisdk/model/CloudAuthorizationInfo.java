package com.rd.veuisdk.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 云音乐授权证书
 *
 * @create 2018/11/27
 */
public class CloudAuthorizationInfo implements Parcelable {
    /**
     *
     * @param artist
     * @param homeTitle
     * @param homeUrl
     * @param authorizationTitle
     * @param authorizationUrl
     */
    public CloudAuthorizationInfo(String artist, String homeTitle, String homeUrl, String authorizationTitle, String authorizationUrl) {
        mArtist = artist;
        mHomeTitle = homeTitle;
        mHomeUrl = homeUrl;
        mAuthorizationTitle = authorizationTitle;
        mAuthorizationUrl = authorizationUrl;
    }

    public String getArtist() {
        return mArtist;
    }

    public String getHomeTitle() {
        return mHomeTitle;
    }

    public String getHomeUrl() {
        return mHomeUrl;
    }

    public String getAuthorizationTitle() {
        return mAuthorizationTitle;
    }

    public String getAuthorizationUrl() {
        return mAuthorizationUrl;
    }

    private String mArtist, mHomeTitle, mHomeUrl, mAuthorizationTitle, mAuthorizationUrl;

    protected CloudAuthorizationInfo(Parcel in) {
        mArtist = in.readString();
        mHomeTitle = in.readString();
        mHomeUrl = in.readString();
        mAuthorizationTitle = in.readString();
        mAuthorizationUrl = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mArtist);
        dest.writeString(mHomeTitle);
        dest.writeString(mHomeUrl);
        dest.writeString(mAuthorizationTitle);
        dest.writeString(mAuthorizationUrl);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<CloudAuthorizationInfo> CREATOR = new Creator<CloudAuthorizationInfo>() {
        @Override
        public CloudAuthorizationInfo createFromParcel(Parcel in) {
            return new CloudAuthorizationInfo(in);
        }

        @Override
        public CloudAuthorizationInfo[] newArray(int size) {
            return new CloudAuthorizationInfo[size];
        }
    };
}
