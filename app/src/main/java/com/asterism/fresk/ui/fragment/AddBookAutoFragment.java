package com.asterism.fresk.ui.fragment;


import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;

import androidx.annotation.RequiresApi;

import com.asterism.fresk.R;
import com.asterism.fresk.contract.IAddBookContract;
import com.asterism.fresk.dao.BookDao;
import com.asterism.fresk.dao.BookTypeDao;
import com.asterism.fresk.dao.bean.BookTypeBean;
import com.asterism.fresk.presenter.AddBookPresenter;
import com.asterism.fresk.ui.activity.MainActivity;
import com.asterism.fresk.ui.adapter.DirectoryListAdapter;
import com.asterism.fresk.util.FileSizeUtil;
import com.asterism.fresk.util.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import butterknife.BindView;
import butterknife.OnClick;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

/**
 * 自动添加书籍页面Fragment类，继承base基类且泛型为当前模块Presenter接口类型，并实现当前模块View接口
 *
 * @author lulushuiba
 * @email 1315269930@qq.com
 * @date on 2019-08-04 11:17
 */
public class AddBookAutoFragment extends BaseFragment<IAddBookContract.Presenter>
        implements IAddBookContract.View {

    @BindView(R.id.lv_manual_files)
    ListView filesListView;                 // 目录容器
    @BindView(R.id.btn_import_select)
    Button btnImportSelect;                 // 导入选中 按钮
    private DirectoryListAdapter adapter;   // 目录列表适配器
    PopupWindow popupWindow = null;
    final List<Map<String, Object>> fileList = new ArrayList<>();      //搜索到的全部list
    final List<Map<String, Object>> showList = new ArrayList<>();      //需要显示的list
    BookTypeDao typeDao = null;

    //region 筛选等有关控件
    ImageButton btnTitleFilter;         // 筛选 按钮
    //得到书籍类型dao
    PopupWindow popupFilterWindow = null;  //筛选窗口 弹出窗口
    Set<String> hsFilterShowType = new HashSet<>();  // 要显示的文件类型
    //筛选的最小文件大小输入框
    EditText etMinFileSize = null;
    //筛选的最大文件大小数据库
    EditText etMaxFileSize = null;
    Button btFilterConfirm;   //筛选窗 确认按钮
    Button btFilterAnew;      //筛选窗 重置按钮
    LinearLayout checkLine;   //筛选窗 复选框的容器
    //endregion

    //region BaseFragment 继承实现方法
    @Override
    protected int setLayoutId() {
        return R.layout.fragment_add_book_auto;
    }

    @Override
    protected IAddBookContract.Presenter setPresenter() {
        return new AddBookPresenter();
    }

    @Override
    protected void initialize() {

        typeDao = new BookTypeDao(mContext);
        hsFilterShowType = new HashSet<>();
        for (BookTypeBean s : typeDao.selectAll()) {
            hsFilterShowType.add(s.getType());
        }
        //初始化容器
        initListView();
        //初始化筛选窗口
        initFilterWindow();
    }
    //endregion

    //region 正在加载窗口有关方法

    /**
     * 显示正在加载窗口
     */
    private void ShowNowLoadWindow() {

        View contentView = LayoutInflater.from(mContext).inflate(R.layout.poput_now_search, null);
        Button btCancel = contentView.findViewById(R.id.btCancel);

        if (popupWindow == null) {
            popupWindow = new PopupWindow(contentView,
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        popupWindow.setFocusable(false);
        popupWindow.setTouchable(true);
        popupWindow.setOutsideTouchable(false);

        //设置透明度
        backgroundAlpha((float) 0.5);
        //绑定点击事件
        btCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismissNowLoadWindow();
            }
        });
        //显示PopupWindow
        View rootview = LayoutInflater.from(mContext).inflate(R.layout.fragment_add_book_auto, null);
        popupWindow.showAtLocation(rootview, Gravity.CENTER, 0, 0);
        //设置Dismiss事件
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                backgroundAlpha(1);
            }
        });
    }

    /**
     * 关闭正在加载窗口
     */
    private void dismissNowLoadWindow() {
        popupWindow.dismiss();
    }

    //endregion

    //region 容器有关方法

    /**
     * 初始化容器
     */
    private void initListView() {

        mPresenter.scanBooks(hsFilterShowType, new Observer<String>() {
            @Override
            public void onSubscribe(Disposable d) {
                ShowNowLoadWindow();
            }

            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onNext(String path) {
                File file = new File(path);

                //筛选  判断添加类型是否包含在里面
                if (!hsFilterShowType.contains(FileUtils.getFileSuffixName(path))) {
                    return;
                }
                //筛选  筛选大小
                String size = FileSizeUtil.getAutoFileOrFilesSize(file.getPath());

                if (!etMinFileSize.getText().toString().equals("") && !etMinFileSize.getText().toString().equals("0")) {
                    if (!FileSizeUtil.IsGreatInt(size, Integer.parseInt(etMinFileSize.getText().toString()))) {
                        return;
                    }
                }

                if (!etMaxFileSize.getText().toString().equals("") && !etMaxFileSize.getText().toString().equals("0")) {
                    if (FileSizeUtil.IsGreatInt(size, Integer.parseInt(etMaxFileSize.getText().toString()))) {
                        return;
                    }
                }


                Map<String, Object> itemMap = new HashMap<>();
                //加载图标
                itemMap.put("icon", R.drawable.icon_file);
                itemMap.put("path", path);

                // 记录文件名称，例：123.txt
                itemMap.put("name", FileUtils.getFileSimpleName(path) + "." + FileUtils.getFileSuffixName(path));

                BookDao dao = new BookDao(getContext());
                String Type = "file";
                if (dao.queryIsExistByPath(path)) {
                    Type = "already_file";
                }
                // 记录文件类型
                itemMap.put("type", Type);
                //记录大小
                itemMap.put("size", size);
                //记录时间
                itemMap.put("time", FileUtils.GetShowFileTime(path));

                fileList.add(itemMap);
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {
                //填充item
                inflateListView(fileList);
                //关闭加载框
                dismissNowLoadWindow();
            }
        });
        // 绑定点击事件
        filesListView.setOnItemClickListener(listViewItemOnClick);

    }

    /**
     * Item的点击事件
     */
    private ListView.OnItemClickListener listViewItemOnClick = new ListView.OnItemClickListener() {
        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            if (fileList.get(position).get("type").equals("file")) {

                if (((DirectoryListAdapter.ViewHolder) view.getTag()).cbOption.isChecked()) {
                    adapter.setBook(position);
                    updateProgressPartly(position, false);
                } else {
                    adapter.setBook(position);
                    updateProgressPartly(position, true);
                }

                List<String> li = new ArrayList<>(adapter.book.keySet());

                // 更新UI数据
                btnImportSelect.setText(getResources().getString(R.string.importSelect) + "(" + adapter.book.size() + ")");

                //当点击的为already_file()类型时
            } else if (fileList.get(position).get("type").equals("already_file")) {
                //无需操作
            } else {
                showSuccessToast("错误，未知的类型+ ");
            }
        }
    };

    /**
     * 当更新书籍选中与被选中时调用
     *
     * @param position 被点击的位置
     * @param b        要设置的值
     */
    private void updateProgressPartly(int position, boolean b) {
        int firstVisiblePosition = filesListView.getFirstVisiblePosition();
        int lastVisiblePosition = filesListView.getLastVisiblePosition();
        if (position >= firstVisiblePosition && position <= lastVisiblePosition) {
            View v = filesListView.getChildAt(position - firstVisiblePosition);
            if (v.getTag() instanceof DirectoryListAdapter.ViewHolder) {
                DirectoryListAdapter.ViewHolder vh = (DirectoryListAdapter.ViewHolder) v.getTag();
                vh.cbOption.setChecked(b);
            }
        }
    }

    /**
     * 填充ListView
     *
     * @param fileList 文件数组
     */
    private void inflateListView(List<Map<String, Object>> fileList) {
        // 创建Adapter
        if (adapter == null) {
            adapter = new DirectoryListAdapter(getContext(), fileList);
        } else {
            adapter.setData(fileList);
        }
        // 为ListView设置Adapter
        filesListView.setAdapter(adapter);
    }

    //endregion

    //region 筛选窗口有关方法

    /**
     * 初始化筛选窗口
     */
    private void initFilterWindow() {
        View contentView = LayoutInflater.from(getContext()).inflate(R.layout.poput_filter, null);

        //得到确定按钮
        btFilterConfirm = contentView.findViewById(R.id.btConfirm);
        //得到重置按钮
        btFilterAnew = contentView.findViewById(R.id.btAnew);
        //得到复选框的容器 （LinearLayout）
        checkLine = contentView.findViewById(R.id.ll_type_check_list);
        //得到最小文件大小输入框
        etMinFileSize = contentView.findViewById(R.id.etMinFileSize);
        //得到最大文件大小输入框
        etMaxFileSize = contentView.findViewById(R.id.etMaxFileSize);
        //得到书籍类型dao
        if (null == typeDao) {
            typeDao = new BookTypeDao(getContext());
        }
        //添加复习框到弹出按钮
        for (BookTypeBean bean : typeDao.selectAll()) {
            CheckBox temCheckBox = new CheckBox(getContext());
            temCheckBox.setText(bean.getType());
            temCheckBox.setChecked(true);
            checkLine.addView(temCheckBox);
        }

        btFilterConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FilterConfirm();
            }
        });

        popupFilterWindow = new PopupWindow(contentView,
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        //触摸窗口外消失
        popupFilterWindow.setOutsideTouchable(true);
        //让触控聚焦于弹出窗口. 主要解决，弹出窗口未消失时点击外部会触发其他控件
        popupFilterWindow.setFocusable(true);

        //设置Dismiss事件
        popupFilterWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                backgroundAlpha(1);
            }
        });

        //绑定按钮
        btnTitleFilter = getActivity().findViewById(R.id.btn_title_filter);
        btnTitleFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShowFilterWindow();
            }
        });
    }

    /**
     * 筛选确认
     */
    private void FilterConfirm() {
        //临时CheckBox
        CheckBox temCheckBox = null;
        //清空
        hsFilterShowType.clear();
        //查看所有的复习框
        for (int i = 0; i < checkLine.getChildCount(); i++) {
            temCheckBox = (CheckBox) checkLine.getChildAt(i);
            //假若勾选了，则添加到Set中
            if (temCheckBox.isChecked()) {
                hsFilterShowType.add(temCheckBox.getText().toString());
            }
        }
        //关闭窗口
        if (null != popupFilterWindow) {
            popupFilterWindow.dismiss();
        }

        //清空list
        fileList.clear();
        //查询加载文件
        initListView();
    }

    /**
     * 显示筛选窗口
     */
    private void ShowFilterWindow() {
        backgroundAlpha((float) 0.5);
        //显示PopupWindow
        View rootview = LayoutInflater.from(getContext()).inflate(R.layout.activity_add_book_local, null);
        //居中显示PopupWindow
        popupFilterWindow.showAtLocation(rootview, Gravity.CENTER, 0, 0);

    }

    //endregion

    //region 合约需实现的方法
    @Override
    public Context GetContext() {
        return super.getContext();
    }

    @Override
    public void showAdding() {
    }

    @Override
    public void hideAdding() {
    }

    @Override
    public void showScanning() {
    }

    @Override
    public void hideScanning() {
    }
    //endregion

    //region 其他公共方法

    /**
     * 设置添加屏幕的背景透明度
     *
     * @param bgAlpha
     */
    public void backgroundAlpha(float bgAlpha) {
        WindowManager.LayoutParams lp = getActivity().getWindow().getAttributes();
        lp.alpha = bgAlpha; //0.0-1.0
        getActivity().getWindow().setAttributes(lp);
    }

    /**
     * 按钮点击事件
     *
     * @param view 导入加载按钮
     */
    @OnClick({R.id.btn_import_select})
    public void onClick(View view) {
        switch (view.getId()) {
            // 点击导入选中
            case R.id.btn_import_select:
                if (adapter.getCount() >= 0) {
                    // 临时存储要导入的书籍路径
                    List<String> preBookPathList = new ArrayList<>(adapter.book.keySet());
                    if (preBookPathList.size() < 1) {
                        break;
                    }
                    mPresenter.addBooks(preBookPathList, new IAddBookContract.OnAddBooksListener() {
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
                break;
        }
    }
    //endregion

}