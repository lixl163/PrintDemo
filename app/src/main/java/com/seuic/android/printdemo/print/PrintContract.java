package com.seuic.android.printdemo.print;

import android.content.Context;

import com.seuic.android.printdemo.BasePresenter;
import com.seuic.android.printdemo.BaseView;

/**
 * Created by Seuic_ZPY on 2017/3/23.
 *
 */

public class PrintContract {

    interface View extends BaseView<Presenter> {

        Context getContext();

        void showPrintState(String str);
    }

    interface Presenter extends BasePresenter {

        void printText();

        void printQRCode();

        void printBarCode();

        void printImage();

        void printBlankLine();
    }
}
