package com.asterism.fresk.ui.adapter;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import java.util.List;

/**
 * 碎片ViewPager适配器类
 *
 * @author Ashinch
 * @email Glaxyinfinite@outlook.com
 * @date on 2019-07-01 16:14
 */
public class PagerAdapter extends FragmentPagerAdapter {

    private List<Fragment> FragmentList;    // 视图碎片集合
    private List<String> titleList;         // 页面标题集合

    public PagerAdapter(FragmentManager fm, List<Fragment> list) {
        super(fm);
        this.FragmentList = list;
    }

    public PagerAdapter(FragmentManager fm, List<Fragment> list, List<String> titles) {
        super(fm);
        this.FragmentList = list;
        this.titleList = titles;
    }

    /**
     * 获取item对应的对象
     *
     * @param i 指定item位置
     *
     * @return 返回碎片集合中指定位置的碎片对象引用
     */
    @Override
    public Fragment getItem(int i) {
        return FragmentList.get(i);
    }

    /**
     * 获取item总数
     *
     * @return 返回集合长度
     */
    @Override
    public int getCount() {
        return FragmentList.size();
    }

    /**
     * 获取页面标题
     *
     * @param position 当前页面位置
     *
     * @return 返回指定titleList内容值
     */
    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return titleList.get(position);
    }
}
