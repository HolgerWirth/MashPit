package com.holger.mashpit;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

public class WearTempPagerAdapter extends PagerAdapter {
    private static final String DEBUG_TAG = "WearTempPagerAdapter";

    private List<View> views;
    private View mCurrentView;

    WearTempPagerAdapter(List<View> views) {
        this.views = views;
    }

    public void updatePie(int position)
    {
        views.get(position).invalidate();
    }

    @Override
    public int getCount() {
        return views.size();
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView(views.get(position));
    }

    @Override
    public int getItemPosition(@NonNull Object object) {
        for (int index = 0; index < getCount(); index++) {
            if (object == views.get(index)) {
                return index;
            }
        }
        return POSITION_NONE;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return views.get(position).getContentDescription();
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        View view = views.get(position);
        container.addView(view);
        view.setTag(position);
        return view;
    }

    @Override
    public void setPrimaryItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        mCurrentView = (View)object;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object o) {
        return view == o;
    }

    public View getCurrentView()
    {
        return mCurrentView;
    }

}