package com.asterism.fresk;

import android.app.Application;
import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.asterism.fresk.dao.BookDao;
import com.asterism.fresk.dao.BookTypeDao;
import com.asterism.fresk.dao.bean.BookBean;
import com.asterism.fresk.dao.bean.BookTypeBean;
import com.asterism.fresk.util.DateUtils;
import com.asterism.fresk.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

/**
 * 重写Application
 *
 * @author Ashinch
 * @email Glaxyinfinite@outlook.com
 * @date on 2019-07-30 03:36
 */
public class AppApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // 初始化书籍类型
        initBookType();
        // 初始化缺省书籍封面
        initBookPictures();
        // 初始化书籍
//        initBook();
    }

    /**
     * 初始化书籍类型
     */
    private void initBookType() {
        try {
            BookTypeDao bookTypeDao = new BookTypeDao(this);
            String[] types = getAssets().list("pictures");
            if (types != null) {
                if (bookTypeDao.selectAll().size() != types.length) {
                    for (String type : types) {
                        BookTypeBean bean = new BookTypeBean();
                        Log.i("application", "getFileSimpleName: " + FileUtils.getFileSimpleName(type));
                        bean.setType(FileUtils.getFileSimpleName(type));
                        bookTypeDao.insert(bean);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 初始化缺省书籍封面
     */
    private void initBookPictures() {
        // TODO: 2019-07-30 待完成 检测缺省书籍封面已存在
        try {
            String[] picPaths = getAssets().list("pictures");
            if (picPaths == null) {
                Log.i("application", "picPaths is null!");
            } else {
                for (String path : picPaths) {
                    Log.i("application", "picPath:" + path);
                    FileUtils.writeBookPic(this, path, getAssets().open("pictures/" + path));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 初始化书籍
     */
    private void initBook() {
        // TODO: 2019-07-30 待完成 检测初始化书籍已存在
        FileUtils.writeFile(this, "TXT测试书籍.txt", "txt", Context.MODE_PRIVATE);
        FileUtils.writeFile(this, "epub测试书籍.epub", "EPUB", Context.MODE_PRIVATE);
        FileUtils.writeFile(this, "Pdf测试书籍.pdf", "pdf", Context.MODE_PRIVATE);

        List<String> preBookPathList = new ArrayList<>();
        preBookPathList.add(getFilesDir().getAbsolutePath() + "/别了，柏林.txt");
        preBookPathList.add(getFilesDir().getAbsolutePath() + "/沧海，卷一.epub");
        preBookPathList.add(getFilesDir().getAbsolutePath() + "/三个火枪手.pdf");
        preBookPathList.add(getFilesDir().getAbsolutePath() + "/月亮与六便士.txt");
        preBookPathList.add(getFilesDir().getAbsolutePath() + "/百年孤独.epub");
        preBookPathList.add(getFilesDir().getAbsolutePath() + "/雾都孤儿.pdf");
        preBookPathList.add(getFilesDir().getAbsolutePath() + "/白夜行.txt");
        preBookPathList.add(getFilesDir().getAbsolutePath() + "/茶花女.epub");
        preBookPathList.add(getFilesDir().getAbsolutePath() + "/老人与海.pdf");
        preBookPathList.add(getFilesDir().getAbsolutePath() + "/TXT测试书籍.txt");
        preBookPathList.add(getFilesDir().getAbsolutePath() + "/epub测试书籍.epub");
        preBookPathList.add(getFilesDir().getAbsolutePath() + "/Pdf测试书籍.pdf");
        for (String path : preBookPathList) {
            Log.i("application", "preBookPathList: " + path);
        }

        // 书籍类型表访问器
        BookTypeDao bookTypeDao = new BookTypeDao(this);
        // 书籍表访问器
        BookDao bookDao = new BookDao(this);

        // 遍历路径集合
        for (String path : preBookPathList) {
            // 根据后缀名获取书籍类型
            String suffixName = FileUtils.getFileSuffixName(path);
            List<BookTypeBean> list = bookTypeDao.selectAllByName(suffixName);
            // 如果文件类型存在于书籍类型表中
            if (list.size() > 0) {
                // 将书籍添加到数据库
                bookDao.insert(getBookBean(path, list.get(0)));
            } else {
                // 将路径添加到错误路径集合
                Log.i("application", "failPath: " + path);
            }
        }
    }

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
        // TODO: 2019-07-11 最后章节与书籍名称后面针对书籍类型需要更改写法
        bookBean.setLastChapter(getString(R.string.neverRead));
        bookBean.setName(FileUtils.getFileSimpleName(path));
        bookBean.setReadDate(new Date());
        Random r = new Random();
        bookBean.setReadProgress(r.nextInt(100));
        bookBean.setReadTiming(0);
        bookBean.setType(bookTypeBean);

        String picPath = "";
        // 根据不同的类型处理书籍封面
        switch (bookTypeBean.getType()) {
            case "txt":
                picPath = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                        .getAbsolutePath() + File.separator + "txt.png";
                break;
            case "pdf":
                picPath = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                        .getAbsolutePath() + File.separator + "pdf.png";
                break;
            case "epub":
                picPath = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                        .getAbsolutePath() + File.separator + "epub.png";
                break;
            case "mobi":
                picPath = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                        .getAbsolutePath() + File.separator + "mobi.png";
                break;
        }

        bookBean.setPicName(picPath);
        return bookBean;
    }
}
