package com.jzby.jzbysounderclient.bean;

/**
 * Created by gordan on 2018/3/2.
 */

public class ProgramBean {

    private String proName;

    private String proAuthor;

    private long proTime;

    public String getProAuthor() {
        return proAuthor;
    }

    public void setProAuthor(String proAuthor) {
        this.proAuthor = proAuthor;
    }

    public String getProName() {
        return proName;
    }

    public void setProName(String proName) {
        this.proName = proName;
    }

    public long getProTime() {
        return proTime;
    }

    public void setProTime(long proTime) {
        this.proTime = proTime;
    }
}
