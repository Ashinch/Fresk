package com.asterism.fresk.presenter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.asterism.fresk.R;
import com.asterism.fresk.contract.IBookContract;
import com.asterism.fresk.dao.BookDao;
import com.asterism.fresk.dao.bean.BookBean;
import com.tencent.smtt.sdk.QbSdk;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * 书籍模块Presenter类，继承base基类且泛型为当前模块View接口类型，并实现当前模块Presenter接口
 *
 * @author Rainydays
 * @email 2036361118@qq.com
 * @date on 2019-07-09 19:14:56
 */
public class BookPresenter extends BasePresenter<IBookContract.View>
        implements IBookContract.Presenter {

    private int bookid; // 书籍的编号
    private Uri bookpicUri; // 书籍图片路径
    private String selectedImageUri;
    private String longclickUri;
    private Bitmap longclickbitmap;
    private String bookname;
    private ImageView imgBookPic;
    private TextView tvBookName;
    private IBookContract.OnNormalListener onNormalListener;

    /**
     * 实现 给BookPresenter成员变量传值
     *
     * @param imgBookPic 显示书籍封面的ImageView
     * @param tvBookName 显示书名的TextView
     * @param onNormalListener 监听器
     * @param bookid 书籍的编号
     * @param bookname 书名
     * @param selectedImageUri 相册选择的图片地址
     * @param bookpicUri 书籍图片路径
     */
    @Override
    public void initData(ImageView imgBookPic, TextView tvBookName,
                         IBookContract.OnNormalListener onNormalListener, int bookid,
                         String bookname, String selectedImageUri, Uri bookpicUri) {
        this.imgBookPic = imgBookPic;
        this.tvBookName = tvBookName;
        this.onNormalListener = onNormalListener;
        this.bookid = bookid;
        this.bookname = bookname;
        this.selectedImageUri = selectedImageUri;
        this.bookpicUri = bookpicUri;
    }

    /**
     * 实现 从数据库内获取所有书籍
     *
     * @param listener 监听器
     */
    @SuppressLint("CheckResult")
    @Override
    public void getAllBooks(final IBookContract.OnBookListListener listener) {
        // 显示正在加载
        mView.showLoading();

        // 创建被观察者，传递List<BookBean>类型事件
        Observable<List<BookBean>> BookObservable
                = Observable.create(new ObservableOnSubscribe<List<BookBean>>() {
            @Override
            public void subscribe(ObservableEmitter<List<BookBean>> emitter) throws Exception {
                // 初始化书籍类型表访问器
                BookDao bookDao = new BookDao(getContext());
                emitter.onNext(bookDao.selectAll());
                emitter.onComplete();
            }
        });

        // 处理于IO子线程
        BookObservable.subscribeOn(Schedulers.io())
                // 响应于Android主线程
                .observeOn(AndroidSchedulers.mainThread())
                // 设置订阅的响应事件
                .subscribe(new Consumer<List<BookBean>>() {
                    @Override
                    public void accept(List<BookBean> bookBeans) throws Exception {
                        // 隐藏正在加载
                        mView.hideLoading();
                        if (bookBeans != null) {
                            listener.onSuccess(bookBeans);
                        } else {
                            listener.onError();
                        }
                    }
                });
    }

    /**
     * 从数据库内按照阅读日期排序获取指定索引书籍
     *
     * @param index    索引
     * @param listener 监听器
     */
    @SuppressLint("CheckResult")
    @Override
    public void getBookByIndexSortReadDate(final int index,
                                           final IBookContract.OnBookBeanListener listener) {
        // 创建被观察者，传递BookBean类型事件
        Observable<BookBean> BookObservable
                = Observable.create(new ObservableOnSubscribe<BookBean>() {
            @Override
            public void subscribe(ObservableEmitter<BookBean> emitter) throws Exception {
                // 初始化书籍类型表访问器
                BookDao bookDao = new BookDao(getContext());
                BookBean bookBean = bookDao.selectByIndexSortReadDate(index);
                if (bookBean != null) {
                    emitter.onNext(bookBean);
                } else {
                    emitter.onError(new Throwable("bookBean is null!"));
                }
                emitter.onComplete();
            }
        });

        // 处理于IO子线程
        BookObservable.subscribeOn(Schedulers.io())
                // 响应于Android主线程
                .observeOn(AndroidSchedulers.mainThread())
                // 设置订阅的响应事件
                .subscribe(new Observer<BookBean>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(BookBean bookBean) {
                        listener.onSuccess(bookBean);
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

    /**
     * 实现 从数据库内移除书籍
     *
     * @param bookList 欲移除的BookBean集合
     * @param listener 监听器
     */
    @SuppressLint("CheckResult")
    @Override
    public void removeBooksInDatabase(final List<BookBean> bookList,
                                      final IBookContract.OnBookListListener listener) {
        // 显示正在移除
        mView.showRemoving();

        // 创建被观察者，传递List<BookBean>类型事件
        Observable<List<BookBean>> BookObservable
                = Observable.create(new ObservableOnSubscribe<List<BookBean>>() {
            @Override
            public void subscribe(ObservableEmitter<List<BookBean>> emitter) throws Exception {
                // 初始化书籍表访问器
                BookDao bookDao = new BookDao(getContext());
                for (BookBean bookBean : bookList) {
                    // 执行书籍类型表访问器删除操作
                    bookDao.delete(bookBean);
                }
                emitter.onNext(bookList);
                emitter.onComplete();
            }
        });

        // 处理于IO子线程
        BookObservable.subscribeOn(Schedulers.io())
                // 响应于Android主线程
                .observeOn(AndroidSchedulers.mainThread())
                // 设置订阅的响应事件
                .subscribe(new Consumer<List<BookBean>>() {
                    @Override
                    public void accept(List<BookBean> bookBeans) throws Exception {
                        // 隐藏正在移除
                        mView.hideRemoving();
                        listener.onSuccess(bookBeans);
                    }
                });
    }

    /**
     * 实现 从储存设备内移除书籍
     *
     * @param bookList 欲移除的BookBean集合
     */
    @Override
    public void removeBooksInStorage(List<BookBean> bookList) {
        // TODO: 2019-07-09 待完成 实现 从储存设备内移除书籍
    }

    /**
     * 实现 撤销移除数据库内的书籍
     *
     * @param bookList 被移除的BookBean集合
     * @param listener 监听器
     */
    @SuppressLint("CheckResult")
    @Override
    public void restoreBooks(final List<BookBean> bookList,
                             final IBookContract.OnNormalListener listener) {
        // 创建被观察者，传递List<BookBean>类型事件
        Observable<BookBean> BookObservable
                = Observable.create(new ObservableOnSubscribe<BookBean>() {
            @Override
            public void subscribe(ObservableEmitter<BookBean> emitter) throws Exception {
                // 初始化书籍表访问器
                BookDao bookDao = new BookDao(getContext());
                for (BookBean bookBean : bookList) {
                    // 执行书籍类型表访问器添加操作
                    bookDao.insert(bookBean);
                    emitter.onNext(bookBean);
                }
                emitter.onComplete();
            }
        });

        // 处理于IO子线程
        BookObservable.subscribeOn(Schedulers.io())
                // 响应于Android主线程
                .observeOn(AndroidSchedulers.mainThread())
                // 设置订阅的响应事件
                .subscribe(new Observer<BookBean>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(BookBean bookBean) {

                    }

                    @Override
                    public void onError(Throwable e) {
                        // 隐藏正在移除
                        mView.hideRemoving();
                        listener.onError();
                    }

                    @Override
                    public void onComplete() {
                        // 隐藏正在移除
                        mView.hideRemoving();
                        listener.onSuccess();
                    }
                });
    }

    /**
     * 实现 修改数据库内书籍信息
     *
     * @param bookBean 欲更改的书籍实体类
     * @param listener 监听器
     */
    @SuppressLint("CheckResult")
    @Override
    public void alterBookInfo(final BookBean bookBean,
                              final IBookContract.OnNormalListener listener) {
        // 创建被观察者，传递BookBean类型事件
        Observable<BookBean> BookObservable
                = Observable.create(new ObservableOnSubscribe<BookBean>() {
            @Override
            public void subscribe(ObservableEmitter<BookBean> emitter) throws Exception {
                // 初始化书籍表访问器
                BookDao bookDao = new BookDao(getContext());
                if (bookBean != null) {
                    // 执行书籍类型表访问器修改操作
                    bookDao.update(bookBean);
                }
                emitter.onNext(bookBean);
                emitter.onComplete();
            }
        });

        // 处理于IO子线程
        BookObservable.subscribeOn(Schedulers.io())
                // 响应于Android主线程
                .observeOn(AndroidSchedulers.mainThread())
                // 设置订阅的响应事件
                .subscribe(new Consumer<BookBean>() {
                    @Override
                    public void accept(BookBean bookBean) throws Exception {
                        if (bookBean != null) {
                            listener.onSuccess();
                        } else {
                            listener.onError();
                        }
                    }
                });
    }

    /**
     * 实现 修改数据库内书籍书名信息
     *
     * @param bookid 欲更改的书籍编号
     * @param newbookname 欲更改书名
     * @param listener 监听器
     */
    @SuppressLint("CheckResult")
    @Override
    public void alterBookNameInfo(final int bookid, final String newbookname,
                                  final IBookContract.OnNormalListener listener) {

        // 创建被观察者，传递BookBean类型事件
        Observable<BookBean> BookObservable
                = Observable.create(new ObservableOnSubscribe<BookBean>() {
            @Override
            public void subscribe(ObservableEmitter<BookBean> emitter) throws Exception {
                // 初始化书籍表访问器
                BookDao bookDao = new BookDao(getContext());
                BookBean bookBean=null;
                // 执行书籍类型表访问器根据编号查询操作
                bookBean=bookDao.getbookbyid(bookid);
                if (bookBean != null) {
                 bookBean.setName(newbookname);
                 // 执行书籍类型表访问器修改操作
                 bookDao.update(bookBean);
                }
                emitter.onNext(bookBean);
                emitter.onComplete();
            }
        });

        // 处理于IO子线程
        BookObservable.subscribeOn(Schedulers.io())
                // 响应于Android主线程
                .observeOn(AndroidSchedulers.mainThread())
                // 设置订阅的响应事件
                .subscribe(new Consumer<BookBean>() {
                    @Override
                    public void accept(BookBean bookBean) throws Exception {
                        if (bookBean != null) {
                            listener.onSuccess();
                        } else {
                            listener.onError();
                        }
                    }
                });
    }

    /**
     * 实现 修改数据库内书籍图片信息
     *
     * @param bookid 欲更改的书籍编号
     * @param newpic 欲更改图片
     * @param listener 监听器
     */
    @SuppressLint("CheckResult")
    @Override
    public void alterBookPicInfo(final int bookid, final String newpic,
                                 final IBookContract.OnNormalListener listener) {

        // 创建被观察者，传递BookBean类型事件
        Observable<BookBean> BookObservable
                = Observable.create(new ObservableOnSubscribe<BookBean>() {
            @Override
            public void subscribe(ObservableEmitter<BookBean> emitter) throws Exception {
                // 初始化书籍表访问器
                BookDao bookDao = new BookDao(getContext());
                BookBean bookBean=null;
                // 执行书籍类型表访问器根据编号查询操作
                bookBean=bookDao.getbookbyid(bookid);
                if (bookBean != null) {
                    bookBean.setPicName(newpic);
                    // 执行书籍类型表访问器修改操作
                    bookDao.update(bookBean);
                }
                emitter.onNext(bookBean);
                emitter.onComplete();
            }
        });

        // 处理于IO子线程
        BookObservable.subscribeOn(Schedulers.io())
                // 响应于Android主线程
                .observeOn(AndroidSchedulers.mainThread())
                // 设置订阅的响应事件
                .subscribe(new Consumer<BookBean>() {
                    @Override
                    public void accept(BookBean bookBean) throws Exception {
                        if (bookBean != null) {
                            listener.onSuccess();
                        } else {
                            listener.onError();
                        }
                    }
                });
    }

    /**
     * 当长按WebView图片时调用
     *
     * @param v WebView所在视图
     * @return 返回长按图片的Url
     */
    private String responseWebLongClick(View v) {
        if (v instanceof WebView) {
            WebView.HitTestResult result = ((WebView) v).getHitTestResult();
            //获得了点击处数据
            if (result != null) {
                int type = result.getType();
                //如果长按的是图片
                if (type == WebView.HitTestResult.IMAGE_TYPE || type == WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE) {
                    String longClickUrl = result.getExtra();
                    return longClickUrl;
                }
            }
        }
        return null;
    }

    /**
     * Url转换为BitMap时调用
     *
     * @param url 图片的路径
     * @return 图片Url转换为BitMap的值
     */
    public Bitmap returnBitMap(String url) {
        URL myFileUrl = null;
        Bitmap bitmap = null;
        try {
            myFileUrl = new URL(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        try {
            HttpURLConnection conn = (HttpURLConnection) myFileUrl.openConnection();
            conn.setDoInput(true);
            conn.connect();
            InputStream is = conn.getInputStream();
            bitmap = BitmapFactory.decodeStream(is);
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    /**
     * 当点击下载长按图片时调用
     *
     * @param bitmap 想要保存图片的bitmap
     * @param picUrl 想要保存图片的Url地址
     */
    private void save2Album(Bitmap bitmap, String picUrl) {
        File appDir = new File(Environment.getExternalStorageDirectory(), System.currentTimeMillis() + ".png");
        if (!appDir.exists()) appDir.mkdir();
        String[] str = picUrl.split("/");
        String fileName = str[str.length - 1];
        File file = new File(appDir, fileName);
        try {
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
            onSaveSuccess(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 当save2Album执行时调用
     *
     * @param file 保存下来的图片文件
     */
    private void onSaveSuccess(final File file) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                // 扫描指定目录
                getContext().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));
                bookpicUri = Uri.fromFile(file);
            }
        }).start();
    }

    /**
     * 当点击本地上传图片时调用
     *
     * @param imageView 对话框视图预览图片
     */
    private void alterBookPicByLocal(ImageView imageView) {
        final ImageView dialog_imagbookpic = imageView;
        Intent intent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        ((Activity)getContext()).startActivityForResult(intent, 1);
        final Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.obj != null) {
                    dialog_imagbookpic.setImageURI(Uri.fromFile(new File(
                            msg.obj.toString())));
                }
            }
        };
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (true) {
                        SharedPreferences sp = getContext().getSharedPreferences
                                ("model", getContext().MODE_PRIVATE);
                        SharedPreferences.Editor editor = sp.edit();
                        String selectedImage = sp.getString("selectedImage"
                                , null);
                        if (selectedImage == null) {
                            Thread.sleep(1000);
                        } else if (selectedImage.equals("没选择")) {
                            Thread.interrupted();
                            editor.remove("selectedImage");
                            editor.commit();
                        } else if (selectedImage != null) {
                            editor.remove("selectedImage");
                            editor.commit();
                            Message message = new Message();
                            message.obj = selectedImage;
                            handler.sendMessage(message);
                            selectedImageUri = selectedImage;
                            Thread.interrupted();
                        }
                    }
                } catch (InterruptedException exception) {
                    exception.printStackTrace();
                }
            }
        }).start();
    }

    /**
     * 创建下载图片对话框
     *
     * @param imageView 对话框视图预览图片
     */
    private void alterBookPicBydownload(ImageView imageView) {
        final ImageView dialog_imgbookpic = imageView;
        final Dialog dialog_download = new Dialog(getContext());
        dialog_download.setTitle("下载图片");
        // 设置对话框显示下载视图
        final View alter_download_pic_view = LinearLayout.inflate(getContext(),
                R.layout.dialog_alter_download_bookpic, null);
        // 允许没有Wifi网络时使用X5内核
        QbSdk.setDownloadWithoutWifi(true);
        //x5内核初始化接口
        final com.tencent.smtt.sdk.WebView wv_alter_pic = alter_download_pic_view.findViewById(
                R.id.wv_alter_pic);
        final ImageView dialog_download_imgpic = alter_download_pic_view.
                findViewById(R.id.dl_img_book_pic);
        Button dl_btn_commit = alter_download_pic_view.findViewById(
                R.id.dl_btn_commit);
        Button dl_btn_cancle = alter_download_pic_view.findViewById(
                R.id.dl_btn_cancle);
        dialog_download_imgpic.setImageURI(bookpicUri);
        wv_alter_pic.loadUrl("https://m.baidu.com/sf/vsearch?pd=" +
                "image_content&word=" + bookname + "&tn=vsearch&atn=page&sa=" +
                "vs_img_indexhot&fr=index&from=415a");
        wv_alter_pic.getSettings().setJavaScriptEnabled(true);
        wv_alter_pic.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                final Handler handler = new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        if (msg.obj != null) {
                            dialog_download_imgpic.setImageBitmap((Bitmap) msg.obj);
                        }
                    }
                };
                longclickUri = responseWebLongClick(v);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Message message = new Message();
                        longclickbitmap = returnBitMap(longclickUri);
                        message.obj = longclickbitmap;
                        handler.sendMessage(message);
                    }
                }).start();
                return true;
            }
        });
        dl_btn_cancle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog_download.cancel();
            }
        });
        dl_btn_commit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                save2Album(longclickbitmap, longclickUri);
                dialog_download.cancel();
                dialog_imgbookpic.setImageURI(bookpicUri);
                selectedImageUri = bookpicUri.getPath();
            }
        });
        dialog_download.setContentView(alter_download_pic_view);
        dialog_download.show();
    }

    /**
     * 实现编辑书籍信息
     *
     * @param items 对话框点击元素
     */
    private void ShowAlterDialog(int items){
        final AlertDialog.Builder dialogupdate = new AlertDialog.Builder(getContext());
                switch (items) {
                    case 0:
                        // 设置对话框标题
                        dialogupdate.setTitle("编辑封面");
                        // 设置对话框显示视图
                        final View alter_pic_view =
                                LinearLayout.inflate(getContext(),
                                        R.layout.dialog_alter_upload_bookpic, null);
                        // 对话框预览书籍封面
                        final ImageView dialog_imgbookpic = alter_pic_view.findViewById
                                (R.id.dl_img_book_pic);
                        // 本地上传按钮
                        Button btn_local = alter_pic_view.findViewById
                                (R.id.dl_btn_local_upload);
                        //  下载按钮
                        Button btn_download = alter_pic_view.findViewById(R.id.dl_btn_download);
                        // 设置本地上传按钮点击事件
                        btn_local.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                alterBookPicByLocal(dialog_imgbookpic);
                            }
                        });

                        // 设置下载按钮点击事件
                        btn_download.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                alterBookPicBydownload(dialog_imgbookpic);
                            }
                        });
                        // 设置对话框显示封面图片
                        dialog_imgbookpic.setImageURI(bookpicUri);
                        dialogupdate.setPositiveButton("确定",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if (selectedImageUri != null) {
                                            alterBookPicInfo(bookid, selectedImageUri,
                                                    onNormalListener);
                                            imgBookPic.setImageURI
                                                    (Uri.fromFile(new File(selectedImageUri)));
                                        }
                                    }
                                });
                        dialogupdate.setNegativeButton("取消", null);
                        dialogupdate.setView(alter_pic_view);
                        dialogupdate.show();
                        break;
                    case 1:
                        // 设置对话框标题
                        dialogupdate.setTitle("输入想要更改名称");
                        // 设置对话框显示视图
                        final View alter_name_view =
                                LinearLayout.inflate(getContext(),
                                        R.layout.dialog_alter_bookname,null);
                        // 设置对话框确定按钮和监听事件
                        dialogupdate.setPositiveButton("确定",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        EditText et_name = alter_name_view.findViewById(
                                                R.id.edit_bookname);
                                        alterBookNameInfo(bookid,
                                                et_name.getText().toString().trim(),
                                                onNormalListener);
                                        tvBookName.setText(et_name.getText().toString().trim());
                                    }
                                });
                        // 设置对话框取消按钮和监听事件
                        dialogupdate.setNegativeButton("取消", null);
                        // 对话框绑定视图
                        dialogupdate.setView(alter_name_view);
                        // 显示对话框
                        dialogupdate.show();
                        break;
                    case 2:
                        // TODO: 2019-08-09 待完成 实现 修改书籍作者功能
                        break;
                    default:
                        break;
        };
    }

    /**
     * 实现 长按编辑书籍信息
     */
    @Override
    public void LongPressEditor(){
        imgBookPic.setOnLongClickListener(longClickListener);
    }

    // 长按响应事件
    private ImageView.OnLongClickListener longClickListener = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            // 对话框构建器
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            // 设置项的来源和监听器
            builder.setItems(getContext().getResources().getStringArray(R.array.edit_menu),
                    new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int items) {
                    ShowAlterDialog(items);
                }
            });
            // 显示对话框
            builder.show();
            return true;
        }
    };
}