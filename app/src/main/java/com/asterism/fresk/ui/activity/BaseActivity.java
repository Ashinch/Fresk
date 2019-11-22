package com.asterism.fresk.ui.activity;

import android.os.Bundle;
import android.view.Window;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.asterism.fresk.contract.IBaseContract;

import butterknife.ButterKnife;
import es.dmoral.toasty.Toasty;

/**
 * Activity抽象基类，泛型Presenter基础接口类型，继承自AppCompatActivity，实现View基础接口
 *
 * @author Ashinch
 * @email Glaxyinfinite@outlook.com
 * @date on 2019-07-01 16:01
 */
public abstract class BaseActivity<P extends IBaseContract.Presenter>
        extends AppCompatActivity implements IBaseContract.View {

    protected P mPresenter; // 当前模块中介，子类可用

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 隐藏标题栏
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        // 通过实现抽象方法，获取布局ID
        setContentView(setLayoutId());
        // 绑定ButterKnife库
        ButterKnife.bind(this);
        // 实例化Presenter方法
        if (this.mPresenter == null) {
            mPresenter = setPresenter();
        }
        // 绑定视图
        if (this.mPresenter != null) {
            mPresenter.attachView(this);
        }
        // 初始化方法
        initialize();
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
    protected abstract void initialize();

    /**
     * 实现 显示错误消息
     *
     * @param massage 欲显示的消息内容
     */
    @Override
    public void showErrorToast(String massage) {
        Toasty.error(this, massage, Toast.LENGTH_SHORT, true).show();
    }

    /**
     * 实现 显示成功消息
     *
     * @param massage 欲显示的消息内容
     */
    @Override
    public void showSuccessToast(String massage) {
        Toasty.success(this, massage, Toast.LENGTH_SHORT, true).show();
    }

    /**
     * 实现 显示信息消息
     *
     * @param massage 欲显示的消息内容
     */
    @Override
    public void showInfoToast(String massage) {
        Toasty.info(this, massage, Toast.LENGTH_SHORT, true).show();
    }

    /**
     * 实现 显示警告消息
     *
     * @param massage 欲显示的消息内容
     */
    @Override
    public void showWarningToast(String massage) {
        Toasty.warning(this, massage, Toast.LENGTH_SHORT, true).show();
    }

    /**
     * 实现 显示普通消息
     *
     * @param massage 欲显示的消息内容
     */
    @Override
    public void showNormalToast(String massage) {
        Toasty.normal(this, massage, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 解绑视图
        if (this.mPresenter != null) {
            mPresenter.detachView();
        }
    }
}
