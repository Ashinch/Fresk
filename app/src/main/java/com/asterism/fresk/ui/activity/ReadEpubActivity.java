package com.asterism.fresk.ui.activity;

import android.view.LayoutInflater;

import com.asterism.fresk.R;
import com.asterism.fresk.contract.IReadContract;
import com.asterism.fresk.presenter.ReadPresenter;
import com.yarolegovich.discretescrollview.DSVOrientation;
import com.yarolegovich.discretescrollview.DiscreteScrollView;

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

//    @BindView(R.id.iv_book_pic)
//    ImageView ivBookPic;
//
//    @BindView(R.id.tv_book_name)
//    TextView tvBookName;
//
//    @BindView(R.id.tv_book_state)
//    TextView tvBookState;
//
//    @BindView(R.id.tv_chapter_num)
//    TextView tvChapterNum;
//
//    @BindView(R.id.tv_order)
//    TextView tvOrder;
//
//    @BindView(R.id.lv_chapter)
//    ListView lvChapter;

//    @BindView(R.id.align_tv)
//    AlignTextView alignTV;

//    @BindView(R.id.scrollView)
//    DiscreteScrollView scrollView;

    private Book mBook;
    private List<String> mTocList;

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
        String path = getIntent().getStringExtra("path");
        if (path == null || path.isEmpty()) {
            showErrorToast("未找到文件！");
            finish();
        }
        mBook = mPresenter.getEpubBook("/storage/emulated/0/Download/三体三部曲_私人典藏版.epub",
                "UTF-8");
        mPresenter.getToc(mBook, new IReadContract.OnGetTocListener() {
            @Override
            public void onSuccess(List<String> tocString) {
                mTocList = tocString;
//                tvChapterNum.setText("共" + mTocList.size() + "章");
//                lvChapter.setAdapter(new ChapterListAdapter(ReadEpubActivity.this, mTocList));
            }

            @Override
            public void onError(String message) {
                showErrorToast("获取目录失败: " + message);
            }
        });

    }

    @Override
    public void showLoading() {

    }

    @Override
    public void hideLoading() {

    }
}
