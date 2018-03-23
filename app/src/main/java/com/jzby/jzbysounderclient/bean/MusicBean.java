package com.jzby.jzbysounderclient.bean;

/**
 * Created by gordan on 2018/3/6.
 */

public class MusicBean {

    public int musicImage;

    public String musicName;

    public String musicArtist;

    public String getMusicArtist() {
        return musicArtist;
    }

    public void setMusicArtist(String musicArtist) {
        this.musicArtist = musicArtist;
    }

    public int getMusicImage() {
        return musicImage;
    }

    public void setMusicImage(int musicImage) {
        this.musicImage = musicImage;
    }

    public String getMusicName() {
        return musicName;
    }

    public void setMusicName(String musicName) {
        this.musicName = musicName;
    }
}
