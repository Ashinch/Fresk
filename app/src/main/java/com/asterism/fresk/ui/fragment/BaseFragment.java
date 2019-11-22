package com.asterism.fresk.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.asterism.fresk.contract.IBaseContract;

import java.io.IOException;

import butterknife.ButterKnife;
import butterknife.Unbinder;
import es.dmoral.toasty.Toasty;

/**
 * Fragment抽象基类，泛型Presenter基础接口类型，继承自Fragment，实现View基础接口
 *
 * @author Ashinch
 * @email Glaxyinfinite@outlook.com
 * @date on 2019-07-09 09:10
 */
public abstract class BaseFragment<P extends IBaseContract.Presenter>
        extends Fragment implements IBaseContract.View {

    protected P mPresenter; // 当前模块Presenter，子类可用
    protected Context mContext;
    protected View mView;
    protected Bundle mSavedInstanceState;
    private Unbinder unbinder;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(setLayoutId(), container, false);
        unbinder = ButterKnife.bind(this, mView);
        mContext = mView.getContext();
        mSavedInstanceState = savedInstanceState;
        // 实例化Presenter方法
        if (this.mPresenter == null) {
            mPresenter = setPresenter();
        }
        // 绑定视图
        if (this.mPresenter != null) {
            mPresenter.attachView(this);
        }
        try {
            initialize();
        }catch (IOException e) {
            e.printStackTrace();
        }


        return mView;
    }

    /**
     * 设置布局ID
     *
     * @return 返回布局ID
     */
    protected abstract int setLayoutId();

    /**
     * 实例化该模块的Presenter对象
     *
     * @return 返回引用
     */
    protected abstract P setPresenter();

    /**
     * 初始化
     */
    protected abstract void initialize() throws IOException;

    /**
     * 实现 显示错误消息
     *
     * @param massage 欲显示的消息内容
     */
    @Override
    public void showErrorToast(String massage) {
        Toasty.error(mContext, massage, Toast.LENGTH_SHORT, true).show();
    }

    /**
     * 实现 显示成功消息
     *
     * @param massage 欲显示的消息内容
     */
    @Override
    public void showSuccessToast(String massage) {
        Toasty.success(mContext, massage, Toast.LENGTH_SHORT, true).show();
    }

    /**
     * 实现 显示信息消息
     *
     * @param massage 欲显示的消息内容
     */
    @Override
    public void showInfoToast(String massage) {
        Toasty.info(mContext, massage, Toast.LENGTH_SHORT, true).show();
    }

    /**
     * 实现 显示警告消息
     *
     * @param massage 欲显示的消息内容
     */
    @Override
    public void showWarningToast(String massage) {
        Toasty.warning(mContext, massage, Toast.LENGTH_SHORT, true).show();
    }

    /**
     * 实现 显示普通消息
     *
     * @param massage 欲显示的消息内容
     */
    @Override
    public void showNormalToast(String massage) {
        Toasty.normal(mContext, massage, Toast.LENGTH_SHORT).show();
    }

    @Nullable
    @Override
    public Context getContext() {
        return mContext;
    }

    @Override
    public void onDestroyView() {
        if (unbinder != null) {
            unbinder.unbind();
        }
        super.onDestroyView();
    }
}
