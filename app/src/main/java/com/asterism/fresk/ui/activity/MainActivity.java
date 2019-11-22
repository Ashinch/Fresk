package com.asterism.fresk.ui.activity;


import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.ashokvarma.bottomnavigation.BottomNavigationBar;
import com.ashokvarma.bottomnavigation.BottomNavigationItem;
import com.asterism.fresk.R;
import com.asterism.fresk.contract.IMainContract;
import com.asterism.fresk.presenter.MainPresenter;
import com.asterism.fresk.ui.adapter.PagerAdapter;
import com.asterism.fresk.ui.fragment.BookFragment;
import com.asterism.fresk.ui.fragment.MoreFragment;
import com.asterism.fresk.ui.fragment.NoteFragment;
import com.asterism.fresk.ui.widget.ScrollViewPager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

/**
 * 主要Activity类，继承base基类且泛型为当前模块Presenter类型，并实现当前模块View接口
 *
 * @author Ashinch
 * @email Glaxyinfinite@outlook.com
 * @date on 2019-07-09 16:48
 */
public class MainActivity extends BaseActivity<IMainContract.Presenter>
        implements IMainContract.View {

    @BindView(R.id.content_main)
    ScrollViewPager viewPager;          // 滚动视图容器

    @BindView(R.id.navigation_main)
    BottomNavigationBar bottomNavigationBar;    // 底部导航栏
    /**
     * 底部导航栏 item选中事件监听器
     */
    private BottomNavigationView.OnNavigationItemSelectedListener onNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
            switch (menuItem.getItemId()) {
                // 切换为笔记页面
                case R.id.nav_note:
                    viewPager.setCurrentItem(0);
                    return true;
                // 切换为书桌页面
                case R.id.nav_desk:
                    viewPager.setCurrentItem(1);
                    return true;
                // 切换为更多页面
                case R.id.nav_more:
                    viewPager.setCurrentItem(2);
                    return true;
            }
            return false;
        }
    };

    @Override
    protected int setLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    protected IMainContract.Presenter setPresenter() {
        return new MainPresenter();
    }

    @Override
    protected void initialize() {
        //设置底部导航栏
        initBottomNavigationBar();

        // 碎片集合
        List<Fragment> fragmentList = new ArrayList<>();
        // 添加笔记页面碎片
        fragmentList.add(new NoteFragment());
        // 添加书桌页面碎片
        fragmentList.add(new BookFragment());
        // 添加更多页面碎片
        fragmentList.add(new MoreFragment());

        // 设置滚动视图适配器
        PagerAdapter pagerAdapter = new PagerAdapter(getSupportFragmentManager(), fragmentList);

        // 为滚动视图容器设置不可滚动
        viewPager.setCanScroll(false);
        // 为滚动视图容器设置适配器
        viewPager.setAdapter(pagerAdapter);
        // 为滚动视图容器设置默认当前页面为第二页（书桌页面）
        viewPager.setCurrentItem(1);
        // 为滚动视图容器设置屏幕外最大页面数量为3页
        viewPager.setOffscreenPageLimit(3);
    }

    /**
     * 初始化底部导航栏
     */
    void initBottomNavigationBar() {
        /*2.进行必要的设置*/
        bottomNavigationBar.setBarBackgroundColor(R.color.colorPrimary);
        bottomNavigationBar.setBackgroundStyle(BottomNavigationBar.BACKGROUND_STYLE_STATIC);
        bottomNavigationBar.setMode(BottomNavigationBar.MODE_FIXED);//适应大小
        /*3.添加Tab*/
        bottomNavigationBar
                .addItem(new BottomNavigationItem(
                        R.drawable.nav_write)
                        .setActiveColor(R.color.title1))
                .addItem(new BottomNavigationItem(
                        R.drawable.nav_desk)
                        .setActiveColor(R.color.title1))
                .addItem(new BottomNavigationItem(R.drawable.nav_more))
                .setActiveColor(R.color.title1)
                .setFirstSelectedPosition(0)  //默认显示面板
                .initialise();//初始化

        bottomNavigationBar.setTabSelectedListener(new BottomNavigationBar.OnTabSelectedListener() {
            @Override
            public void onTabSelected(int position) {
                switch (position) {
                    case 0:
                        viewPager.setCurrentItem(0);
                        break;
                    case 1:
                        viewPager.setCurrentItem(1);
                        break;
                    case 2:
                        viewPager.setCurrentItem(2);
                        break;
                }
            }

            @Override
            public void onTabUnselected(int position) {

            }

            @Override
            public void onTabReselected(int position) {

            }
        });
    }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        SharedPreferences sp = getSharedPreferences("data", MODE_PRIVATE);
//        SharedPreferences.Editor editor = sp.edit();
//        //获取图片路径
//        if (requestCode == 1 && resultCode == Activity.RESULT_OK && data != null) {
//            Uri selectedImage = data.getData();
//            Cursor cursor = getContentResolver().query(selectedImage, null, null, null, null);
//            if (cursor != null && cursor.moveToFirst()) {
//                String path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
//                editor.putString("selectedImage", path);
//                editor.commit();
//            }
//        } else {
//            editor.putString("selectedImage", "没选择");
//            editor.commit();
//        }
//    }

}
