package com.asterism.fresk.ui.fragment;

import android.content.Intent;
import android.net.Uri;
import android.widget.ImageView;
import android.widget.TextView;

import com.asterism.fresk.R;
import com.asterism.fresk.contract.IBookContract;
import com.asterism.fresk.dao.BookDao;
import com.asterism.fresk.dao.bean.BookBean;
import com.asterism.fresk.presenter.BookPresenter;
import com.asterism.fresk.ui.activity.ReadEpubActivity;

import java.io.File;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * 书桌页面Fragment类，继承base基类且泛型为当前模块Presenter接口类型，并实现当前模块View接口
 *
 * @author Ashinch
 * @email Glaxyinfinite@outlook.com
 * @date on 2019-07-10 16:10
 */
public class DeskFragment extends BaseFragment<IBookContract.Presenter>
        implements IBookContract.View {

    private int bookid; // 书籍的编号
    private Uri bookpicUri; // 书籍图片路径
    private String selectedImageUri; // 相册选择的图片地址
    private String bookname; // 书名

    @BindView(R.id.img_book_Pic)
    ImageView imgBookPic; // 书籍封面

    @BindView(R.id.tv_book_name)
    TextView tvBookName; // 书名

    @BindView(R.id.tv_last_chapter)
    TextView tvLastChapter; // 最近时间读到章节

    @BindView(R.id.tv_read_progress)
    TextView tvReadProgress; // 阅读进程

    private int pos = 0;
    private BookBean mBookBean;

    @Override
    protected int setLayoutId() {
        return R.layout.fragment_desk;
    }

    @Override
    protected IBookContract.Presenter setPresenter() {
        return new BookPresenter();
    }

    @Override
    protected void initialize() {
        if (getArguments() != null) {
            pos = getArguments().getInt("pos", 0);
        }
        mPresenter.getBookByIndexSortReadDate(pos, new IBookContract.OnBookBeanListener() {
            @Override
            public void onSuccess(BookBean bookBean) {
                mBookBean = bookBean;
                imgBookPic.setImageURI(Uri.fromFile(new File(bookBean.getPicName())));
                bookpicUri = Uri.fromFile(new File(bookBean.getPicName()));
                tvBookName.setText(bookBean.getName());
                tvLastChapter.setText(bookBean.getLastChapter());
                tvReadProgress.setText(bookBean.getReadProgress() + "%");
                mPresenter.initData(imgBookPic, tvBookName, onNormalListener, bookid, bookname, selectedImageUri, bookpicUri);
                mPresenter.LongPressEditor();
            }

            @Override
            public void onError(String message) {
                showErrorToast("获取书桌书籍错误: " + message);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        mPresenter.getBookByIndexSortReadDate(pos, new IBookContract.OnBookBeanListener() {
            @Override
            public void onSuccess(BookBean bookBean) {
                mBookBean = bookBean;
                imgBookPic.setImageURI(Uri.fromFile(new File(bookBean.getPicName())));
                bookpicUri = Uri.fromFile(new File(bookBean.getPicName()));
                tvBookName.setText(bookBean.getName());
                tvLastChapter.setText(bookBean.getLastChapter());
                tvReadProgress.setText(bookBean.getReadProgress() + "%");
                mPresenter.initData(imgBookPic, tvBookName, onNormalListener, bookid, bookname, selectedImageUri, bookpicUri);
                mPresenter.LongPressEditor();
            }

            @Override
            public void onError(String message) {
                showErrorToast("获取书桌书籍错误: " + message);
            }
        });
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

    @OnClick(R.id.img_book_Pic)
    public void onClick() {
        if (mBookBean != null) {
            Intent intent = new Intent(mContext, ReadEpubActivity.class);
            intent.putExtra("path", mBookBean.getFilePath());
            intent.putExtra("bookName", mBookBean.getName());
            intent.putExtra("bookPic", mBookBean.getPicName());
            intent.putExtra("bookState", "已读 " + mBookBean.getReadProgress() + "% ，"
                    + mBookBean.getReadTiming() + "分钟");
            BookDao bookDao=new BookDao(mContext);
            bookDao.updateBookByBookName(mBookBean.getName());
            startActivity(intent);
        }
    }

    // 修改书名监听事件
    private IBookContract.OnNormalListener onNormalListener = new IBookContract.OnNormalListener() {
        @Override
        public void onSuccess() {
            showSuccessToast("修改书籍信息成功!");
        }

        @Override
        public void onError() {
            showSuccessToast("修改书籍信息失败!");
        }
    };
}
