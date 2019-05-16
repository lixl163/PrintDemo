package com.seuic.android.printdemo.print;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.seuic.android.printdemo.R;
import com.seuic.android.printdemo.posdservice.PosdServiceSDK;
import com.seuic.android.printdemo.posdservice.PrintService;

import java.io.InputStream;

/**
 * Created by Seuic_ZPY on 2017/3/23.
 */

public class PrintPresenter implements PrintContract.Presenter {

    private static final String LOG_TAG = PrintPresenter.class.getSimpleName();
    private final PrintContract.View mPrintView;
    private PosdServiceSDK posdServiceSDK = new PosdServiceSDK();
    private PrintService printService;
    private static final String PRINT_RESULT_RETURN_CODE = "returnCode";

    public PrintPresenter(PrintContract.View printView) {
        mPrintView = printView;
        Log.d(LOG_TAG, "PrintPresenter: Init");
        mPrintView.setPresenter(this);
        posdServiceSDK.setContext(mPrintView.getContext().getApplicationContext());
        mPrintView.showPrintState("打印Demo");
    }

    @Override
    public void start() {
        if (posdServiceSDK.checkPosdServiceStauts() == 0) {
            mPrintView.showPrintState("服务已连接，打印就绪");
            printService = posdServiceSDK.getPrintService();
        }
    }

    @Override
    public void printText() {
        mPrintView.showPrintState("正在打印中，请稍后");
        new printTextThread().start();
    }

    private class printTextThread extends Thread {

        public printTextThread() {
        }

        @Override
        public void run() {
            int result;

            StringBuffer bill = new StringBuffer();
            // 打印字体设置功能需要最新字库才能正常使用
            // 新旧字库判定，新字库打印 0 字符，0 中有斜线，旧字库没有
            // 旧字库，使用小字体打印会模糊

            // 居中，加粗
            printService.setAlign(printService.ALIGNMENT_MODE_CENTER);
            printService.setPrintFont(printService.PRINT_FONT_SIZE_BIG, true);
            // 数据区
            bill.append("果速送\n");
            // 写入数据
            printService.buildString(bill.toString());
            bill.delete(0, bill.length());

            // 左对齐
            printService.setAlign(printService.ALIGNMENT_MODE_LEFT);
            printService.setPrintFont(printService.PRINT_FONT_SIZE_MIDDLE, false);
            // 数据区
            bill.append("\n商户名称: 某某某水果店铺\n");
            bill.append("联系人: 某某某 15511365120\r\n");
            bill.append("订单编号: 20171206010008\r\n");
            bill.append("时间: 2015-11-12  15:32:24\r\n");
            bill.append("交易类型  交易笔数  交易金额\r\n");
            bill.append("------------------------------\r\n");
            // 写入数据
            printService.buildString(bill.toString());
            bill.delete(0, bill.length());

            // 右对齐
            printService.setAlign(printService.ALIGNMENT_MODE_RIGHT);
            printService.setPrintFont(printService.PRINT_FONT_SIZE_SMALL, false);
            // 数据区
            bill.append("刷卡消费    0笔  0.00元\r\n");
            bill.append("微信收款    0笔  0.00元\r\n");
            bill.append("支付宝收款  0笔  0.00元\r\n");
            bill.append("撤销交易    0笔  0.00元\r\n");
            bill.append("------------------------------\r\n");
            bill.append("合计    0笔  0.00元\r\n");
            // 写入数据
            printService.buildString(bill.toString());
            bill.delete(0, bill.length());

            result = printService.printText();
            showResult(result);

            try {
                //睡眠
                Thread.sleep(100, 100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            result = printService.printBlankLine(4);
            Log.d(LOG_TAG, "printBlankLine :" + result);
            showResult(result);
        }
    }

    @Override
    public void printQRCode() {
        mPrintView.showPrintState("正在打印中，请稍后");
        new printQRCodeThread().start();
    }

    private class printQRCodeThread extends Thread {

        public printQRCodeThread() {
        }

        @Override
        public void run() {
            int result;
            result = printService.printQRCode("280474691692042932", 248, 3);
            Log.d(LOG_TAG, "printBlankLine :" + result);
            result = printService.printBlankLine(6);
            Log.d(LOG_TAG, "printBlankLine :" + result);
            showResult(result);
        }
    }

    @Override
    public void printBarCode() {
        mPrintView.showPrintState("正在打印中，请稍后");
        new printBarCodeThread().start();
    }

    private class printBarCodeThread extends Thread {

        public printBarCodeThread() {
        }

        @Override
        public void run() {
            int result;
            result = printService.printBarCode("280474691692042932", 0, 384, 100);
            Log.d(LOG_TAG, "printBlankLine :" + result);
            result = printService.printBlankLine(6);
            Log.d(LOG_TAG, "printBlankLine :" + result);
            showResult(result);
        }
    }

    @Override
    public void printImage() {
        mPrintView.showPrintState("正在打印中，请稍后");
        new printImageThread().start();
    }

    private class printImageThread extends Thread {

        public printImageThread() {
        }

        @Override
        public void run() {
            int result;
            InputStream ins = mPrintView.getContext().getResources().openRawResource(R.raw.seuic);
            Bitmap bmp = BitmapFactory.decodeStream(ins);
            result = printService.printImage(bmp);
            Log.d(LOG_TAG, "printBlankLine :" + result);
            result = printService.printBlankLine(6);
            Log.d(LOG_TAG, "printBlankLine :" + result);
            showResult(result);
        }
    }

    @Override
    public void printBlankLine() {
        mPrintView.showPrintState("正在打印中，请稍后");
        new printBlankLineThread().start();
    }

    private class printBlankLineThread extends Thread {

        public printBlankLineThread() {
        }

        @Override
        public void run() {
            int result;
            result = printService.printBlankLine(6);
            Log.d(LOG_TAG, "printBlankLine :" + result);
            showResult(result);
        }
    }

    private void showResult(int result) {
        Bundle bundle = new Bundle();
        bundle.putInt(PRINT_RESULT_RETURN_CODE, result);
        Message msg = new Message();
        msg.setData(bundle);
        mHandler.sendMessage(msg);
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Bundle bundle = msg.getData();
            int result = bundle.getInt(PRINT_RESULT_RETURN_CODE);
            if (result == 0) {
                mPrintView.showPrintState("打印成功");
            } else {
                mPrintView.showPrintState("打印失败：" + result);
            }
        }
    };
}
