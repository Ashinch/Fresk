package com.asterism.fresk.ui.activity;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.asterism.fresk.R;
import com.asterism.fresk.contract.IReadContract;
import com.asterism.fresk.dao.bean.TocBean;
import com.asterism.fresk.presenter.ReadPresenter;
import com.asterism.fresk.ui.adapter.ChapterListAdapter;

import java.io.File;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import nl.siegmann.epublib.domain.Book;

/**
 * 阅读Activity类，继承base基类且泛型为当前模块Presenter类型，并实现当前模块View接口
 *
 * @author Ashinch
 * @email Glaxyinfinite@outlook.com
 * @date on 2019-08-03 21:18
 */
public class ReadEpubActivity extends BaseActivity<IReadContract.Presenter>
        implements IReadContract.View {

//    @BindView(R.id.readerView)
//    ReaderView readerView;

    @BindView(R.id.readTextView)
    TextView readerTextView;

    @BindView(R.id.progressBar)
    ProgressBar progressBar;

    private Book mBook;
    private List<TocBean> mTocList;
    private PopupWindow popupControl;
    private PopupWindow popupToc;
    private ChapterListAdapter tocAdapter;

    @Override
    protected int setLayoutId() {
        return R.layout.activity_read;
    }

    @Override
    protected IReadContract.Presenter setPresenter() {
        return new ReadPresenter();
    }

    @Override
    protected void initialize() {
//        readerView.afterMeasured(readerView,new StringBuffer(getResources().getString(R.string.test)));
//        readerView.setMContent(new StringBuffer(getResources().getString(R.string.test)));
        readerTextView.setMovementMethod(ScrollingMovementMethod.getInstance());
        LinearLayout layoutToc = (LinearLayout) View.inflate(ReadEpubActivity.this, R.layout.poput_toc, null);
        popupToc = new PopupWindow(layoutToc,
                (int) (300 * getResources().getDisplayMetrics().density + 0.5f),
                ViewGroup.LayoutParams.MATCH_PARENT
        );
        popupToc.setOutsideTouchable(true);
        popupToc.setFocusable(true);
        popupToc.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popupToc.setAnimationStyle(R.style.anim_popup_left_bar);

        ((TextView) layoutToc.findViewById(R.id.tv_order)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView it = (TextView) v;
                it.setText(it.getText().equals("倒序") ? "正序" : "倒序");
                Collections.reverse(mTocList);
                tocAdapter.notifyDataSetChanged();
            }
        });
        ((TextView) layoutToc.findViewById(R.id.tv_book_name)).setText(getIntent().getStringExtra("bookName"));
        ((TextView) layoutToc.findViewById(R.id.tv_book_state)).setText(getIntent().getStringExtra("bookState"));
        ((ImageView) layoutToc.findViewById(R.id.iv_book_pic)).setImageURI(Uri.fromFile(new File(getIntent().getStringExtra("bookPic"))));
        LinearLayout layoutControl = (LinearLayout) View.inflate(ReadEpubActivity.this, R.layout.poput_control, null);
        popupControl = new PopupWindow(layoutControl,
                ViewGroup.LayoutParams.MATCH_PARENT,
                (int) (60 * getResources().getDisplayMetrics().density + 0.5f)
        );
        popupControl.setOutsideTouchable(true);
        popupControl.setFocusable(true);
        popupControl.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popupControl.setAnimationStyle(R.style.anim_popup_bottom_bar);
        layoutControl.findViewById(R.id.btn_toc).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupControl.dismiss();
                popupToc.showAtLocation(getWindow().getDecorView(), Gravity.LEFT, 0, 0);
            }
        });

//        readerView.setOnClickListener(new ReaderView.OnClick() {
//            @Override
//            public void onSortClick() {
//                popupControl.showAtLocation(getWindow().getDecorView(),Gravity.BOTTOM, 0, 0);
//            }
//
//            @Override
//            public void onLongClick() {
//
//            }
//        });

        readerTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupControl.showAtLocation(getWindow().getDecorView(), Gravity.BOTTOM, 0, 0);
            }
        });

        String path = getIntent().getStringExtra("path");
        if (path == null || path.isEmpty()) {
            showErrorToast("未找到文件！");
            finish();
        }

        mBook = mPresenter.getEpubBook(path,
                "UTF-8");
        mPresenter.getToc(mBook, new IReadContract.OnGetTocListener() {
            @Override
            public void onSuccess(List<TocBean> tocString) {
                mTocList = tocString;
                ((TextView) layoutToc.findViewById(R.id.tv_chapter_num)).setText("共" + mTocList.size() + "章");
                TextView tvChapterNum = (TextView) layoutToc.findViewById(R.id.tv_chapter_num);
                ListView lvChapter = (ListView) layoutToc.findViewById(R.id.lv_chapter);
                tvChapterNum.setText("共" + mTocList.size() + "章");
                tocAdapter = new ChapterListAdapter(ReadEpubActivity.this, mTocList);
                lvChapter.setAdapter(tocAdapter);
                lvChapter.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        popupToc.dismiss();
                        mPresenter.getContent(mBook, mTocList.get(position).getId(), new IReadContract.OnGetContentListener() {
                            @Override
                            public void onSuccess(StringBuffer content) {
                                readerTextView.setText(Html.fromHtml(content.toString()));
                                readerTextView.scrollTo(0, 0);
                            }

                            @Override
                            public void onError(String message) {
                                Log.i("getContent", "onError: " + message);
                            }
                        });
                    }
                });

                mPresenter.getContent(mBook, mTocList.get(0).getId(), new IReadContract.OnGetContentListener() {
                    @Override
                    public void onSuccess(StringBuffer content) {
                        readerTextView.setText(Html.fromHtml(content.toString()));
                        readerTextView.scrollTo(0, 0);
                    }

                    @Override
                    public void onError(String message) {
                        Log.i("getContent", "onError: " + message);
                    }
                });
            }

            @Override
            public void onError(String message) {
                showErrorToast("获取目录失败: " + message);
            }
        });
    }

    @Override
    public void showLoading() {
        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideLoading() {
        progressBar.setVisibility(View.GONE);
    }

//    @Override
//    public void onBackPressed() {
////        super.onBackPressed();
//        Intent intent = new Intent(ReadEpubActivity.this, MainActivity.class)
//                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
//        startActivity(intent);
//    }
}
