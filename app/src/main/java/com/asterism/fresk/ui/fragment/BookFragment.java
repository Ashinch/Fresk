package com.asterism.fresk.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;

import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.asterism.fresk.R;
import com.asterism.fresk.contract.IBookContract;
import com.asterism.fresk.presenter.BookPresenter;
import com.asterism.fresk.ui.activity.AddBookLocalActivity;
import com.asterism.fresk.ui.adapter.PagerAdapter;
import com.asterism.fresk.ui.widget.ScrollViewPager;
import com.asterism.fresk.util.PermissionUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * 书籍页面Fragment类，继承base基类且泛型为当前模块Presenter接口类型，并实现当前模块View接口
 *
 * @author Ashinch
 * @email Glaxyinfinite@outlook.com
 * @date on 2019-07-09 22:48
 */
public class BookFragment extends BaseFragment<IBookContract.Presenter>
        implements IBookContract.View {

    @BindView(R.id.title_bookshelf)
    View titleBookshelf;

    @BindView(R.id.title_desk)
    View titleDesk;

    @BindView(R.id.btn_title_bookshelf)
    ImageButton btnTitleBookshelf;

    @BindView(R.id.btn_title_desk)
    ImageButton btnTitleDesk;

    @BindView(R.id.btn_title_add_book1)
    ImageButton btnTitleAddBook1;

    @BindView(R.id.btn_title_add_book2)
    ImageButton btnTitleAddBook2;

    @BindView(R.id.img_in_triangle)
    ImageView imgInTriangle;

    @BindView(R.id.img_slide_fore)
    ImageView imgSlideFore;

    @BindView(R.id.navigation_book)
    ScrollViewPager viewPager;

    @BindView(R.id.layout_slide)
    RelativeLayout layoutSlide;

    /**
     * ViewPager 页面改变事件
     */
    private ViewPager.OnPageChangeListener pageChangeListener
            = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int i, float v, int i1) {
            // 设置滑块动画
            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(imgSlideFore.getLayoutParams());
            lp.leftMargin = (int) (lp.width * v) + i * lp.width;
            imgSlideFore.setLayoutParams(lp);
            if (i < 1) {
                layoutSlide.setAlpha(v);
            }
        }

        @Override
        public void onPageSelected(int i) {
            // 设置标题栏切换
            titleDesk.setVisibility(i < 1 ? View.GONE : View.VISIBLE);
            titleBookshelf.setVisibility(i < 1 ? View.VISIBLE : View.GONE);
            imgInTriangle.setVisibility(i < 1 ? View.GONE : View.VISIBLE);
            //mLayoutSlide.setVisibility(i < 1 ? View.GONE : View.VISIBLE);
        }

        @Override
        public void onPageScrollStateChanged(int i) {

        }
    };

    /**
     * 添加书籍按钮弹出菜单 选中事件
     */
    private PopupMenu.OnMenuItemClickListener menuAddOnMenuItemClickListener = new PopupMenu.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
            switch (item.getItemId()) {
                case R.id.item_add_book_local:
                    Intent intent = new Intent(getActivity(), AddBookLocalActivity.class);
                    startActivity(intent);
                    return true;
                case R.id.item_add_book_network:
                    return true;
                default:
                    return false;
            }
        }
    };

    @Override
    protected int setLayoutId() {
        return R.layout.fragment_book;
    }

    @Override
    protected IBookContract.Presenter setPresenter() {
        return new BookPresenter();
    }

    @Override
    protected void initialize() {
        titleBookshelf.setVisibility(View.GONE);

        // 碎片集合
        List<Fragment> fragmentList = new ArrayList<>();

        // 添加书桌页面碎片
        fragmentList.add(new BookshelfFragment());

        // 添加书桌页面碎片
        for (int i = 0; i < 3; i++) {
            DeskFragment deskFrag = new DeskFragment();
            Bundle bundle = new Bundle();
            bundle.putInt("pos", i);
            deskFrag.setArguments(bundle);
            fragmentList.add(deskFrag);
        }

        // 设置滚动视图适配器
        PagerAdapter pagerAdapter = new PagerAdapter(getChildFragmentManager(), fragmentList);

        // 为滚动视图容器设置不可滚动
        viewPager.setCanScroll(true);
        // 为滚动视图容器设置适配器
        viewPager.setAdapter(pagerAdapter);
        // 为滚动视图容器设置默认当前页面为第二页（书桌页面）
        viewPager.setCurrentItem(1);
        // 为滚动视图容器设置屏幕外最大页面数量为3页
        viewPager.setOffscreenPageLimit(3);
        // 为滚动视图容器设置页面改变监听器
        viewPager.addOnPageChangeListener(pageChangeListener);

        imgInTriangle.setVisibility(View.VISIBLE);
        titleDesk.setVisibility(View.VISIBLE);
        titleBookshelf.setVisibility(View.GONE);
    }

    @Override
    public void showLoading() {

    }

    @Override
    public void hideLoading() {

    }

    @Override
    public void showRemoving() {

    }

    @Override
    public void hideRemoving() {

    }

    /**
     * 按钮点击事件
     *
     * @param view 书架按钮 书桌按钮 添加数据按钮1 添加数据按钮2
     */
    @OnClick({R.id.btn_title_bookshelf, R.id.btn_title_desk, R.id.btn_title_add_book1, R.id.btn_title_add_book2})
    public void onClick(View view) {
        switch (view.getId()) {
            // 点击书架按钮
            case R.id.btn_title_bookshelf:
                viewPager.setCurrentItem(0);
                titleDesk.setVisibility(View.GONE);
                titleBookshelf.setVisibility(View.VISIBLE);
                imgInTriangle.setVisibility(View.GONE);
                break;

            //点击书桌按钮
            case R.id.btn_title_desk:
                viewPager.setCurrentItem(1);
                titleDesk.setVisibility(View.VISIBLE);
                titleBookshelf.setVisibility(View.GONE);
                imgInTriangle.setVisibility(View.VISIBLE);
                break;

            //书架与书桌都添加弹出加载书籍按钮
            case R.id.btn_title_add_book1:
            case R.id.btn_title_add_book2:
                PopupMenu popupMenu = new PopupMenu(getContext(), view);
                getActivity().getMenuInflater().inflate(R.menu.menu_add_book, popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(menuAddOnMenuItemClickListener);
                //显示菜单
                popupMenu.show();
                //对权限进行申请
                PermissionUtils.requestPermissionsRWM(getActivity());
                break;
        }
    }


}
