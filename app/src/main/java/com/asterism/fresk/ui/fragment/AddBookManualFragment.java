package com.asterism.fresk.ui.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Environment;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.RequiresApi;

import com.asterism.fresk.R;
import com.asterism.fresk.contract.IAddBookContract;
import com.asterism.fresk.presenter.AddBookPresenter;
import com.asterism.fresk.ui.activity.MainActivity;
import com.asterism.fresk.ui.adapter.DirectoryListAdapter;
import com.asterism.fresk.util.DirectoryUtils;
import com.asterism.fresk.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * 手动添加书籍页面Fragment类，继承base基类且泛型为当前模块Presenter接口类型，并实现当前模块View接口
 *
 * @author lulushuiba
 * @email 1315269930@qq.com
 * @date on 2019-08-03 22:48
 */
public class AddBookManualFragment extends BaseFragment<IAddBookContract.Presenter>
        implements IAddBookContract.View {

    @BindView(R.id.lv_manual_files)
    ListView filesListView;                 // 目录容器

    @BindView(R.id.tv_manual_path)
    TextView tvPath;                        // 当前路径 文本框

    @BindView(R.id.btn_import_select)
    Button btnImportSelect;                 // 导入选中 按钮

    private File parent;                    // 当前父文件夹
    private File[] files = null;            // 当前路径下所有文件
    private DirectoryListAdapter adapter;   // 目录列表适配器

    /**
     * 文件信息集合
     * Map：名称、图标、路径、类型
     * key：name、icon、path、type
     * value：String、Drawable_Id、String、（dir、file）
     */
    private List<Map<String, Object>> listItems;

    /**
     * Item的点击事件
     */
    private ListView.OnItemClickListener listViewItemOnClick = new ListView.OnItemClickListener() {
        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            //当点击的为dir类型时

            if (listItems.get(position).get("type").equals("dir")) {
                try {
                    // 获取系统SD卡目录
                    File root = new File(AddBookManualFragment.this.parent.getCanonicalPath() + "/"
                            + listItems.get(position).get("name"));
                    if (root.exists()) {
                        AddBookManualFragment.this.parent = root;
                        files = root.listFiles();
                        mPresenter.getFilesInDir(root, new IAddBookContract.OnGetFilesListener() {
                            @Override
                            public void onSuccess(List<Map<String, Object>> fileList) {
                                inflateListView(fileList);
                            }

                            @Override
                            public void onError(String message) {

                        }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
             //当点击的为File类型时
            } else if(listItems.get(position).get("type").equals("file")){

                if (((DirectoryListAdapter.ViewHolder) view.getTag()).cbOption.isChecked()) {
                    adapter.setBook(position);
                    updateProgressPartly(position, false);
                } else {
                    adapter.setBook(position);
                    updateProgressPartly(position, true);
                }
                // 更新UI数据
                 btnImportSelect.setText(getResources().getString(R.string.importSelect) + "(" + adapter.book.size() + ")");

                //当点击的为already_file()类型时
            }else if(listItems.get(position).get("type").equals("already_file")){
                //无需操作
            }
            else {
                showSuccessToast("错误，未知错误");
            }
        }
    };

    @Override
    public Context GetContext() {
        return super.getContext();
    }

    @Override
    protected int setLayoutId() {
        return R.layout.fragment_add_book_manual;
    }

    @Override
    protected IAddBookContract.Presenter setPresenter() {
        return new AddBookPresenter();
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void initialize(){
        // 设置为可滑动
        tvPath.setMovementMethod(ScrollingMovementMethod.getInstance());

        // 获取系统SD卡目录
        File root = new File(Environment.getExternalStorageDirectory().getPath());
        if (root.exists()) {
            parent = root;
            mPresenter.getFilesInDir(parent, new IAddBookContract.OnGetFilesListener() {
                @Override
                public void onSuccess(List<Map<String, Object>> fileList) {
                    inflateListView(fileList);
                }

                @Override
                public void onError(String message) {

                }
            });
            // 绑定点击事件
            filesListView.setOnItemClickListener(listViewItemOnClick);
        }
        // 设置初始值
       btnImportSelect.setText(getResources().getString(R.string.importSelect) + "(0)");
    }

    /**
     * 填充ListView
     *
     * @param fileList 文件数组
     */
    private void inflateListView(List<Map<String, Object>> fileList) {
        // 对目录与文件进行分类排序
        fileList = DirectoryUtils.DirectorySort(fileList);

        // 创建Adapter
        if (adapter == null) {
            adapter = new DirectoryListAdapter(getContext(), fileList);
        } else {
            adapter.setData(fileList);
        }
        listItems = fileList;
        // 为ListView设置Adapter
        filesListView.setAdapter(adapter);
        try {
            tvPath.setText(parent.getCanonicalPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

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
        }
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
}