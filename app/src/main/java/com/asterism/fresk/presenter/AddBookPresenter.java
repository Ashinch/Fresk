package com.asterism.fresk.presenter;

import android.annotation.SuppressLint;
import android.os.Environment;

import com.asterism.fresk.R;
import com.asterism.fresk.contract.IAddBookContract;
import com.asterism.fresk.dao.BookDao;
import com.asterism.fresk.dao.BookTypeDao;
import com.asterism.fresk.dao.bean.BookBean;
import com.asterism.fresk.dao.bean.BookTypeBean;
import com.asterism.fresk.util.DateUtils;
import com.asterism.fresk.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * 添加书籍模块Presenter类，继承base基类且泛型为当前模块View接口类型，并实现当前模块Presenter接口
 *
 * @author Ashinch
 * @email Glaxyinfinite@outlook.com
 * @date on 2019-07-08 13:56
 */
public class AddBookPresenter extends BasePresenter<IAddBookContract.View>
        implements IAddBookContract.Presenter {

    /**
     * 获取BookBean对象
     *
     * @param path         文件路径
     * @param bookTypeBean 书籍类型
     *
     * @return 返回实例化后的BookBean
     */
    private BookBean getBookBean(String path, BookTypeBean bookTypeBean) {
        BookBean bookBean = new BookBean();

        bookBean.setFilePath(path);
        bookBean.setAddDate(DateUtils.getNowToString());
        bookBean.setLastChapter("从未阅读");
        bookBean.setName(FileUtils.getFileSimpleName(path));
        bookBean.setReadDate(DateUtils.getNowToString());
        bookBean.setReadProgress(0);
        bookBean.setReadTiming(0);
        bookBean.setType(bookTypeBean);

        String picPath = "";
        // 根据不同的类型处理书籍封面
        switch (bookTypeBean.getType()) {
            case "txt":
                picPath = "android.resource://" + mView.getContext().getPackageName() + "/" + R.raw.txt;
                break;
            case "pdf":
                picPath = "android.resource://" + mView.getContext().getPackageName() + "/" + R.raw.pdf;
                break;
            case "epub":
                picPath = "android.resource://" + mView.getContext().getPackageName() + "/" + R.raw.epub;
                break;
            case "mobi":
                picPath = "android.resource://" + mView.getContext().getPackageName() + "/" + R.raw.mobi;
                break;
        }
        bookBean.setPicName(picPath);
        return bookBean;
    }

    /**
     * 文件扫描递归
     *
     * @param Dir 文件目录
     */
    private void fileScan(File Dir, Set<String> typeNameSet, ObservableEmitter<String> emitter) {
        // 初始化书籍类型表访问器
        BookTypeDao bookTypeDao = new BookTypeDao(mView.getContext());
        // 获取当前目录内所有文件类型数组
        File[] files = Dir.listFiles();
        // 不为空文件夹时
        if (files != null) {
            for (File file : files) {
                // 如果当前文件是个目录且不为隐藏文件时
                if (file.isDirectory() && !file.isHidden()) {
                    // 继续扫描
                    fileScan(file, typeNameSet, emitter);
                } else if (typeNameSet.contains(FileUtils.getFileSuffixName(file.getName()))
                        && !file.isHidden()) {
                    // 如果该文件是书籍类型且不为隐藏文件时
                    emitter.onNext(file.getPath() + file.getName());
                }
            }
        }
    }

    /**
     * 实现 添加书籍
     *
     * @param pathList 选中的书籍文件路径集合
     * @param listener 监听器
     */
    @SuppressLint("CheckResult")
    @Override
    public void addBooks(final List<String> pathList, final IAddBookContract.OnAddBooksListener listener) {
        // 创建被观察者，传递List<String>类型事件
        Observable<List<String>> observable
                = Observable.create(new ObservableOnSubscribe<List<String>>() {
            @Override
            public void subscribe(ObservableEmitter<List<String>> emitter) {
                // 错误路径集合
                List<String> errorPathList = new ArrayList<>();
                // 书籍类型表访问器
                BookTypeDao bookTypeDao = new BookTypeDao(mView.getContext());
                // 书籍表访问器
                BookDao bookDao = new BookDao(mView.getContext());

                // 遍历路径集合
                for (String path : pathList) {
                    // 根据后缀名获取书籍类型
                    String suffixName = FileUtils.getFileSuffixName(path);
                    List<BookTypeBean> list = bookTypeDao.selectAllByName(suffixName);
                    // 如果文件类型存在于书籍类型表中
                    if (list.size() != 0) {
                        // 将书籍添加到数据库
                        bookDao.insert(getBookBean(path, list.get(0)));
                    } else {
                        // 将路径添加到错误路径集合
                        errorPathList.add(path);
                    }
                }

                emitter.onNext(errorPathList);
                emitter.onComplete();
            }
        });

        // 处理于IO子线程
        observable.subscribeOn(Schedulers.io())
                // 响应于Android主线程
                .observeOn(AndroidSchedulers.mainThread())
                // 设置订阅的响应事件
                .subscribe(new Consumer<List<String>>() {
                    @Override
                    public void accept(List<String> errorPathList) throws Exception {
                        if (errorPathList.size() == 0) {
                            listener.onSuccess();
                        } else {
                            listener.onError(errorPathList);
                        }
                    }
                });
    }

    /**
     * 实现 获取目录下所有文件信息
     *
     * @param currentDir 当前文件夹目录
     * @param listener   监听器
     */
    @SuppressLint("CheckResult")
    @Override
    public void getFilesInDir(final File currentDir, final IAddBookContract.OnGetFilesListener listener) {
        // 被观察者 传递List<Map<String,Object>>类型事件
        Observable<List<Map<String, Object>>> observable
                = Observable.create(new ObservableOnSubscribe<List<Map<String, Object>>>() {
            @Override
            public void subscribe(ObservableEmitter<List<Map<String, Object>>> emitter) throws Exception {
                // 先判断当前目录是否存在
                if (!currentDir.exists()) {
                    mView.showWarningToast("目录不存在！");
                    return;
                }

                // 初始化文件列表集合
                List<Map<String, Object>> itemList = new ArrayList<>();
                // 先为列表集合添加返回上级文件夹
                try {
                    // 如果不为手机储存根目录（再向上会需要ROOT权限）
                    if (!"/storage/emulated/0".equals(currentDir.getCanonicalPath())) {
                        Map<String, Object> returnUp = new HashMap<>();
                        returnUp.put("icon", R.drawable.icon_folder);
                        returnUp.put("name", "/..");
                        returnUp.put("type", "dir");
                        itemList.add(0, returnUp);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                // 获取当前目录内所有文件类型数组
                File[] currentFiles = currentDir.listFiles();
                // 初始化列表集合子项集合
                Map<String, Object> itemMap = new HashMap<>();
                // 初始化书籍类型表访问器
                BookTypeDao bookTypeDao = new BookTypeDao(mView.getContext());

                // 遍历当前目录下所有文件
                for (File file : currentFiles) {
                    String type;
                    // 判断类型，如果当前file是文件夹就使用文件夹图标，否则使用书籍文件图标
                    if (file.isDirectory()) {
                        itemMap.put("icon", R.drawable.icon_folder);
                        type = "dir";
                    } else if (bookTypeDao.isExistsByName(FileUtils.getFileSuffixName(file.getName()))) {
                        itemMap.put("icon", R.drawable.icon_file);
                        type = "file";
                    } else {
                        // 除了文件夹和书籍类型文件，其他一律忽略
                        continue;
                    }

                    // 记录文件路径
                    try {
                        itemMap.put("path", currentDir.getCanonicalPath() + "/");
                    } catch (IOException e) {
                        e.printStackTrace();
                        mView.showErrorToast(e.getMessage());
                    }
                    // 记录文件名称，例：123.txt
                    itemMap.put("name", file.getName());
                    // 记录文件类型
                    itemMap.put("type", type);
                    // 添加到列表集合
                    itemList.add(itemMap);
                }

                emitter.onNext(itemList);
                emitter.onComplete();
            }
        });

        // 处理于IO子线程
        observable.subscribeOn(Schedulers.io())
                // 响应于Android主线程
                .observeOn(AndroidSchedulers.mainThread())
                // 设置订阅的响应事件
                .subscribe(new Consumer<List<Map<String, Object>>>() {
                    @Override
                    public void accept(List<Map<String, Object>> itemList) throws Exception {
                        if (itemList.size() != 0) {
                            listener.onSuccess(itemList);
                        } else {
                            listener.onError();
                        }
                    }
                });
    }

    /**
     * 扫描储存设备内所有书籍
     *
     * @param typeNameSet 欲扫描的文件类型格式后缀名集合
     * @param observer    订阅观察者
     */
    @SuppressLint("CheckResult")
    @Override
    public void scanBooks(final Set<String> typeNameSet, Observer<String> observer) {
        // 被观察者 传递List<Map<String,Object>>类型事件
        Observable<String> observable = Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> emitter) throws Exception {
                File file = new File(Environment.getExternalStorageDirectory().getPath());
                fileScan(file, typeNameSet, emitter);
                emitter.onComplete();
            }
        });

        // 处理于IO子线程
        observable.subscribeOn(Schedulers.io())
                // 响应于Android主线程
                .observeOn(AndroidSchedulers.mainThread())
                // 设置订阅的响应事件
                .subscribe(observer);
    }
}
