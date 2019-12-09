package com.asterism.fresk.presenter;

import com.asterism.fresk.contract.IReadContract;
import com.asterism.fresk.dao.bean.TocBean;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.MediaType;
import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.domain.TOCReference;
import nl.siegmann.epublib.domain.TableOfContents;
import nl.siegmann.epublib.epub.EpubReader;
import nl.siegmann.epublib.service.MediatypeService;

/**
 * 阅读模块Presenter类，继承base基类且泛型为当前模块View接口类型，并实现当前模块Presenter接口
 *
 * @author Ashinch
 * @email Glaxyinfinite@outlook.com
 * @date on 2019-08-03 21:19
 */
public class ReadPresenter extends BasePresenter<IReadContract.View>
        implements IReadContract.Presenter {

    private void getAllTOCReference(List<TOCReference> parent, List<TocBean> tocStrings) {
        if (parent == null) {
            return;
        }
        for (int i = 0; i < parent.size(); i++) {
            TOCReference current = parent.get(i);
            String title = current.getTitle();
            if (!"".equals(title)) {
                tocStrings.add(new TocBean(title,current.getResourceId()));
            }
            getAllTOCReference(current.getChildren(), tocStrings);
        }
    }

    @Override
    public Book getEpubBook(String path, String encoding) {
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
            return reader.readEpubLazy(path, encoding, Arrays.asList(lazyTypes));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void getToc(final Book book, final IReadContract.OnGetTocListener listener) {
        mView.showLoading();
        // 创建被观察者，传递List<TOCReference>类型事件
        Observable<List<TocBean>> observable
                = Observable.create(new ObservableOnSubscribe<List<TocBean>>() {
            @Override
            public void subscribe(ObservableEmitter<List<TocBean>> emitter) throws Exception {
                TableOfContents table = book.getTableOfContents();
                List<TOCReference> tocList = table.getTocReferences();
                List<TocBean> tocStrings = new ArrayList<>();
                getAllTOCReference(tocList, tocStrings);
                emitter.onNext(tocStrings);
                emitter.onComplete();
            }
        });

        // 处理于IO子线程
        observable.subscribeOn(Schedulers.io())
                // 响应于Android主线程
                .observeOn(AndroidSchedulers.mainThread())
                // 设置订阅的响应事件
                .subscribe(new Observer<List<TocBean>>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(List<TocBean> tocString) {
                        listener.onSuccess(tocString);
                    }

                    @Override
                    public void onError(Throwable e) {
                        listener.onError(e.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        mView.hideLoading();
                    }
                });
    }

    @Override
    public void getContent(Book book, String id, IReadContract.OnGetContentListener listener) {
        mView.showLoading();
        // 创建被观察者，传递List<TOCReference>类型事件
        Observable<StringBuffer> observable
                = Observable.create(new ObservableOnSubscribe<StringBuffer>() {
            @Override
            public void subscribe(ObservableEmitter<StringBuffer> emitter) throws Exception {
                Resource resource = book.getResources().getById(id);
                byte[] data = resource.getData();
                if (data == null || data.length <= 0) {
                    emitter.onError(new Throwable("resource.getData() is null"));
                    emitter.onComplete();
                }
                Charset charset = Charset.defaultCharset();
                ByteBuffer buf = ByteBuffer.wrap(data);
                CharBuffer cBuf = charset.decode(buf);

                emitter.onNext(new StringBuffer(resource.getTitle() + cBuf));
                emitter.onComplete();
            }
        });

        // 处理于IO子线程
        observable.subscribeOn(Schedulers.io())
                // 响应于Android主线程
                .observeOn(AndroidSchedulers.mainThread())
                // 设置订阅的响应事件
                .subscribe(new Observer<StringBuffer>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(StringBuffer content) {
                        listener.onSuccess(content);
                    }

                    @Override
                    public void onError(Throwable e) {
                        listener.onError(e.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        mView.hideLoading();
                    }
                });
    }
}
