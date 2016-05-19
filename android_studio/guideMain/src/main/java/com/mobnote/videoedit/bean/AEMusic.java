package com.mobnote.videoedit.bean;

public class AEMusic {
    boolean isSelected;
    String musicName;
    String musicPath;
    int musicCoverNormal;
    int musicCoverSelected;

    public int getMusicCoverNormal() {
        return musicCoverNormal;
    }

    public void setMusicCoverNormal(int musicCoverNormal) {
        this.musicCoverNormal = musicCoverNormal;
    }

    public int getMusicCoverSelected() {
        return musicCoverSelected;
    }

    public void setMusicCoverSelected(int musicCoverSelected) {
        this.musicCoverSelected = musicCoverSelected;
    }

    public AEMusic(String name, String path, boolean selected, int coverNormal, int coverSelected) {
        this.musicName = name;
        this.musicPath = path;
        this.isSelected = selected;
        musicCoverNormal = coverNormal;
        musicCoverSelected = coverSelected;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean isSelected) {
        this.isSelected = isSelected;
    }

    public String getMusicName() {
        return musicName;
    }

    public void setMusicName(String musicName) {
        this.musicName = musicName;
    }

    public String getMusicPath() {
        return musicPath;
    }

    public void setMusicPath(String musicPath) {
        this.musicPath = musicPath;
    }

}
