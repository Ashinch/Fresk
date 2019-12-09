package com.asterism.fresk.ui.fragment;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.asterism.fresk.R;
import com.asterism.fresk.contract.IAddBookContract;
import com.asterism.fresk.contract.IBookContract;
import com.asterism.fresk.dao.BookDao;
import com.asterism.fresk.dao.BookTypeDao;
import com.asterism.fresk.dao.bean.BookBean;
import com.asterism.fresk.dao.bean.BookTypeBean;
import com.asterism.fresk.presenter.AddBookPresenter;
import com.asterism.fresk.presenter.BookPresenter;
import com.asterism.fresk.ui.activity.AddBookLocalActivity;
import com.asterism.fresk.ui.activity.MainActivity;
import com.asterism.fresk.ui.adapter.PagerAdapter;
import com.asterism.fresk.ui.widget.ScrollViewPager;
import com.asterism.fresk.util.DateUtils;
import com.asterism.fresk.util.FileUtils;
import com.asterism.fresk.util.PermissionUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.MediaType;
import nl.siegmann.epublib.epub.EpubReader;
import nl.siegmann.epublib.service.MediatypeService;

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
                    if (Build.VERSION.SDK_INT >= 30) {
                        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                        intent.setType("*/*");//设置类型，我这里是任意类型，任意后缀的可以这样写。
                        intent.addCategory(Intent.CATEGORY_OPENABLE);
                        startActivityForResult(intent,1);
                    } else {
                        Intent intent = new Intent(getActivity(), AddBookLocalActivity.class);
                        startActivity(intent);
                    }
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

    @Override
    public void onResume() {
        super.onResume();
        viewPager.setCurrentItem(1);
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 1) {
                Uri uri = data.getData();
                addBooks(Arrays.asList(getPath(getContext(), uri)), new IAddBookContract.OnAddBooksListener(){
                    @Override
                    public void onSuccess() {
                        showSuccessToast("添加成功");
                        // 查询加载MainActivity, 并清空栈中所有Activity
                        Intent intent = new Intent(mContext, MainActivity.class)
                                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }

                    @Override
                    public void onFailed(List<String> pathList) {
                        StringBuilder buffer = new StringBuilder("导入失败: ");
                        for (String s : pathList) {
                            buffer.append(FileUtils.getFileSimpleName(s)).append(", ");
                        }
                        showWarningToast(buffer.toString());
                    }

                    @Override
                    public void onError(String message) {
                        showErrorToast("导入书籍错误: " + message);
                    }
                });
            }
        }
    }

    public String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{split[1]};

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }

    public String getDataColumn(Context context, Uri uri, String selection,
                                String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {column};

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    public boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }
    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }
    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    private BookBean getBookBean(String path, BookTypeBean bookTypeBean) {
        BookBean bookBean = new BookBean();

        bookBean.setFilePath(path);
        bookBean.setAddDate(DateUtils.getNowToString());
        // TODO: 2019-07-11 最后章节与书籍名称后面针对书籍类型需要更改写法
        bookBean.setLastChapter("从未阅读");
        bookBean.setName(FileUtils.getFileSimpleName(path));
        bookBean.setReadDate(new Date());
        bookBean.setReadProgress(0);
        bookBean.setReadTiming(0);
        bookBean.setType(bookTypeBean);

        String picPath = "";
        // 根据不同的类型处理书籍封面
        switch (bookTypeBean.getType()) {
            case "txt":
                picPath = getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                        .getAbsolutePath() + File.separator + "txt.png";
                break;
            case "pdf":
                picPath = getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                        .getAbsolutePath() + File.separator + "pdf.png";
                break;
            case "epub":
                Book book = null;
                EpubReader reader = new EpubReader();
                MediaType[] lazyTypes = {
                        MediatypeService.CSS,
                        MediatypeService.GIF,
                        MediatypeService.JPG,
                        MediatypeService.PNG,
                        MediatypeService.MP3,
                        MediatypeService.MP4
                };
                try {
                    book = reader.readEpubLazy(path, "utf-8", Arrays.asList(lazyTypes));
                    Bitmap coverImage = BitmapFactory.decodeStream(book.getCoverImage().getInputStream());
                    if (coverImage != null) {
                        File file = new File(getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                                .getAbsolutePath() + File.separator + book.getMetadata().getTitles().get(0) + ".png");
                        FileOutputStream out = new FileOutputStream(file);
                        coverImage.compress(Bitmap.CompressFormat.PNG, 100, out);
                        out.flush();
                        out.close();
                        picPath = getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                                .getAbsolutePath() + File.separator + book.getMetadata().getTitles().get(0) + ".png";
                    } else {
                        picPath = getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                                .getAbsolutePath() + File.separator + "epub.png";
                        showWarningToast("获取书籍封面失败！");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                break;
            case "mobi":
                picPath = getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                        .getAbsolutePath() + File.separator + "mobi.png";
                break;
        }

        bookBean.setPicName(picPath);
        return bookBean;
    }

    public void addBooks(final List<String> pathList,
                         final IAddBookContract.OnAddBooksListener listener) {
        // 创建被观察者，传递List<String>类型事件
        Observable<List<String>> observable
                = Observable.create(new ObservableOnSubscribe<List<String>>() {
            @Override
            public void subscribe(ObservableEmitter<List<String>> emitter) {
                // 错误路径集合
                List<String> failPathList = new ArrayList<>();
                // 书籍类型表访问器
                BookTypeDao bookTypeDao = new BookTypeDao(getContext());
                // 书籍表访问器
                BookDao bookDao = new BookDao(getContext());

                // 遍历路径集合
                for (String path : pathList) {
                    // 根据后缀名获取书籍类型
                    String suffixName = FileUtils.getFileSuffixName(path);
                    List<BookTypeBean> list = bookTypeDao.selectAllByName(suffixName);
                    // 如果文件类型存在于书籍类型表中
                    if (list.size() > 0) {
                        // 将书籍添加到数据库
                        bookDao.insert(getBookBean(path, list.get(0)));
                    } else {
                        // 将路径添加到错误路径集合
                        failPathList.add(path);
                    }
                }

                emitter.onNext(failPathList);
                emitter.onComplete();
            }
        });

        // 处理于IO子线程
        observable.subscribeOn(Schedulers.io())
                // 响应于Android主线程
                .observeOn(AndroidSchedulers.mainThread())
                // 设置订阅的响应事件
                .subscribe(new Observer<List<String>>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(List<String> errorPathList) {
                        if (errorPathList.size() == 0) {
                            listener.onSuccess();
                        } else {
                            listener.onFailed(errorPathList);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        listener.onError(e.getMessage());
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }
}
