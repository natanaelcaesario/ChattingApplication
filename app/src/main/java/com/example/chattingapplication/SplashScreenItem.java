package com.example.chattingapplication;

public class SplashScreenItem {
    String title,desc;
    int Screenimg;

    public SplashScreenItem(String title, String desc, int screenimg) {
        this.title = title;
        this.desc = desc;
        Screenimg = screenimg;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public int getScreenimg() {
        return Screenimg;
    }

    public void setScreenimg(int screenimg) {
        Screenimg = screenimg;
    }
}
