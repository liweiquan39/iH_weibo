package com.example.weibo_liweiquan;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import java.util.List;

public class ViewPagerAdapter extends FragmentPagerAdapter {
    private List<Fragment> mfragmentList;
    public ViewPagerAdapter(@NonNull FragmentManager fm, List<Fragment> fragmentList) {
        super(fm);//调用父类构造函数，传递FragmentManager 参数
        //用于确保适配器类内部具有有效的 FragmentManager 实例，从而顺利完成片段管理和展示的任务
        this.mfragmentList = fragmentList;

    }//设置 ViewPager 的适配器。

    @NonNull
    @Override
    public Fragment getItem(int position) {
        return  mfragmentList == null ? null: mfragmentList.get(position);
    }

    @Override
    public int getCount() {
        return  mfragmentList == null ? null: mfragmentList.size();
    }

    public void updateView(List<Fragment> fragments) {
        mfragmentList = fragments;
    }
}