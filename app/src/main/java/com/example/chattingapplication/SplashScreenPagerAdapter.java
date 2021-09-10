package com.example.chattingapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import java.util.List;

public class SplashScreenPagerAdapter extends PagerAdapter {
    Context mContext;
    List<SplashScreenItem> splashScreenItemsList;

    public SplashScreenPagerAdapter(Context mContext, List<SplashScreenItem> splashScreenItemsList) {
        this.mContext = mContext;
        this.splashScreenItemsList = splashScreenItemsList;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        LayoutInflater inflater =  (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View LayoutScreen = inflater.inflate(R.layout.screen_1, null);

        TextView title = LayoutScreen.findViewById(R.id.intro_title);
        TextView desc = LayoutScreen.findViewById(R.id.desc);
        ImageView screenimg = LayoutScreen.findViewById(R.id.image_splashscreen);

        title.setText(splashScreenItemsList.get(position).getTitle());
        desc.setText(splashScreenItemsList.get(position).getDesc());
        screenimg.setImageResource(splashScreenItemsList.get(position).getScreenimg());
        container.addView(LayoutScreen);
        return LayoutScreen;
    }

    @Override
    public int getCount() {
        return splashScreenItemsList.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View)object);
    }
}
