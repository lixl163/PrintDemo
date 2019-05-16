package com.seuic.android.printdemo.print;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.seuic.android.printdemo.R;

/**
 * Created by Seuic_ZPY on 2017/3/23.
 *
 */
public class PrintActivity extends Activity implements PrintContract.View{

    private static final String LOG_TAG = PrintActivity.class.getSimpleName();
    private PrintContract.Presenter mPresenter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_print);
        new PrintPresenter(this);

        Button button= (Button) findViewById(R.id.bt_print);
        button.setOnClickListener(buttonClick);
        button = (Button) findViewById(R.id.printText);
        button.setOnClickListener(buttonClick);
        button = (Button) findViewById(R.id.printBarCode);
        button.setOnClickListener(buttonClick);
        button = (Button) findViewById(R.id.printQrCode);
        button.setOnClickListener(buttonClick);
        button = (Button) findViewById(R.id.printBlankLine);
        button.setOnClickListener(buttonClick);
        button = (Button) findViewById(R.id.printImage);
        button.setOnClickListener(buttonClick);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mPresenter.start();
    }

    @Override
    public void setPresenter(PrintContract.Presenter presenter) {
        if ( null == presenter) {
            Log.e(LOG_TAG, "setPresenter: Presenter cannot be null.");
            return;
        }
        Log.d(LOG_TAG, "setPresenter: Init");
        mPresenter = presenter;
    }

    @Override
    public void showPrintState(String str) {
        TextView textView = (TextView) findViewById(R.id.showText);
        textView.setText(str);
    }

    @Override
    public Context getContext() {
        return getApplicationContext();
    }

    View.OnClickListener buttonClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            mPresenter.start();

            switch (v.getId()) {

                case R.id.bt_print:
                    //自定义打印
                    mPresenter.printText();
                    break;

                case R.id.printText:
                    //打印结算单
                    mPresenter.printText();
                    break;
                case R.id.printBarCode:
                    mPresenter.printBarCode();
                    break;
                case R.id.printQrCode:
                    mPresenter.printQRCode();
                    break;
                case R.id.printBlankLine:
                    //走纸
                    mPresenter.printBlankLine();
                    break;
                case R.id.printImage:
                    mPresenter.printImage();
                    break;
                default:
            }
        }
    };
}
