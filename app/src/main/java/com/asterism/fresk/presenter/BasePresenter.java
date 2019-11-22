package com.asterism.fresk.presenter;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.asterism.fresk.contract.IBaseContract;

/**
 * Presenter抽象基类，泛型View基础接口类型，实现Presenter基础接口
 *
 * @author Ashinch
 * @email Glaxyinfinite@outlook.com
 * @date on 2019-07-01 15:56
 */
public abstract class BasePresenter<V extends IBaseContract.View>
        implements IBaseContract.Presenter<V> {

    protected V mView; // 绑定的视图，子类可用

    /**
     * 实现 绑定视图
     *
     * @param view 欲绑定的视图，当前合约内的View
     */
    @Override
    public void attachView(@NonNull V view) {
        this.mView = view;
    }

    /**
     * 实现 解绑视图
     */
    @Override
    public void detachView() {
        this.mView = null;
    }

    /**
     * 判断是否已经绑定View
     *
     * @return 返回绑定状态
     */
    @Override
    public boolean isAttached() {
        return mView != null;
    }

    /**
     * 获取上下文对象
     *
     * @return 返回上下文对象
     */
    @Override
    public Context getContext() {
        if (mView instanceof Context) {
            // 如果当前mView是Activity，直接强制转型
            return (Context) mView;
        } else if (mView instanceof Fragment) {
            // 如果当前mView是Fragment，获取父视图的Context对象
            return ((Fragment) mView).getContext();
        } else {
            // 都不是则抛出异常
            throw new NullPointerException("BasePresenter:mView is't instance of Context,can't use getContext() method.");
        }
    }
}
