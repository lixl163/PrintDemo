package com.seuic.android.printdemo;

/**
 * Created by Seuic_ZPY on 2017/3/23.
 * MVP BaseView, set Presenter.
 */

public interface BaseView<T> {

    void setPresenter(T presenter);

}
