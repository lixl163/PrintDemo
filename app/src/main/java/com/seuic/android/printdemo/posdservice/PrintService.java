package com.seuic.android.printdemo.posdservice;

import android.graphics.Bitmap;
import android.os.ConditionVariable;
import android.os.RemoteException;
import android.util.Log;

import com.seuic.android.PrintBarCodeListener;
import com.seuic.android.PrintImageListener;
import com.seuic.android.PrintQRCodeListener;
import com.seuic.android.Printer;
import com.seuic.android.PrinterListener;
import com.seuic.android.printdemo.utils.BitmapUtils;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Vector;

import static com.seuic.android.printdemo.posdservice.PosdServiceInfoContract.ERR_PRINT_INPUT_DATA_ERROR;

/**
 * Created by Seuic_ZPY on 2017/3/25.
 *
 */

public class PrintService {

    private static final String LOG_TAG = PrintService.class.getSimpleName();
    private static final ConditionVariable cv = new ConditionVariable();
    private static boolean cvBlockStatus = false;
    private static int printReturnCode = 0;
    private Printer mPrinter = null;
    private Vector<Byte> mCommand = new Vector<Byte>();

    public final int ALIGNMENT_MODE_LEFT = 0;
    public final int ALIGNMENT_MODE_CENTER = 1;
    public final int ALIGNMENT_MODE_RIGHT= 2;

    public final int PRINT_FONT_SIZE_SMALL = 0;
    public final int PRINT_FONT_SIZE_MIDDLE = 1;
    public final int PRINT_FONT_SIZE_BIG = 2;

    public void setPrinter(Printer printer) {
        mPrinter = printer;
    }

    public int printText() {
        int result;
        if (mPrinter == null)
            return PosdServiceInfoContract.ERR_PRINT_SERVICE_API_INIT;
        if (cvBlockStatus)
            return PosdServiceInfoContract.ERR_PRINT_SERVICE_BUSY;
//        if (str.isEmpty())
//            return ERR_PRINT_INPUT_DATA_ERROR;
        try {
//            byte[] data_in = str.getBytes("GBK");
            byte[] data_in = getPrintBytes();
            mCommand.clear();
            if (data_in == null) {
                return ERR_PRINT_INPUT_DATA_ERROR;
            }
            result = mPrinter.startPrint( new PrinterCallback(), data_in );
            Log.d(LOG_TAG, "printText :startPrint :" + result);
        } catch (Exception e) {
            Log.e(LOG_TAG, "printText: ", e);
            return PosdServiceInfoContract.ERR_PRINT_SERVICE_API_INIT;
        }
        if (result != 0) {
            return result;
        }
        result = getPrintReturnCode();
        Log.d(LOG_TAG, "printText :getPrintReturnCode :" + result);
        return result;
    }

    public int printBlankLine(int number) {
        int result;
        if (mPrinter == null)
            return PosdServiceInfoContract.ERR_PRINT_SERVICE_API_INIT;
        if (cvBlockStatus)
            return PosdServiceInfoContract.ERR_PRINT_SERVICE_BUSY;
        if (number <= 0)
            return PosdServiceInfoContract.ERR_PRINT_INPUT_DATA_ERROR;

        String str = "\r";
        while (number-- != 0) {
            str += "\n";
        }

        try {
            byte[] data_in = str.getBytes("GBK");
            result = mPrinter.startPrint( new PrinterCallback(), data_in );
            Log.d(LOG_TAG, "printBlankLine :startPrint :" + result);
        } catch (Exception e) {
            Log.e(LOG_TAG, "printBlankLine: ", e);
            return PosdServiceInfoContract.ERR_PRINT_SERVICE_API_INIT;
        }

        if (result != 0) {
            return result;
        }

        result = getPrintReturnCode();
        return result;
    }

    public int printBarCode(String str,  int type, int width, int height) {
        int result;
        if (mPrinter == null)
            return PosdServiceInfoContract.ERR_PRINT_SERVICE_API_INIT;
        if (cvBlockStatus)
            return PosdServiceInfoContract.ERR_PRINT_SERVICE_BUSY;

        if (str.isEmpty())
            return ERR_PRINT_INPUT_DATA_ERROR;
        if (type != 0 && type != 1) {
            return PosdServiceInfoContract.ERR_PRINT_INPUT_DATA_ERROR;
        }
        if (width <= 0 || width > 384) {
            return PosdServiceInfoContract.ERR_PRINT_INPUT_DATA_ERROR;
        }
        if (height <= 0 || height > 100) {
            return PosdServiceInfoContract.ERR_PRINT_INPUT_DATA_ERROR;
        }

        try {
            result = mPrinter.printBarCode( new PrintBarCodeCallback(), str, type, width, height );
        } catch (RemoteException e) {
            Log.e(LOG_TAG, "printQRCode: ", e);
            return PosdServiceInfoContract.ERR_PRINT_SERVICE_API_INIT;
        }

        if (result != 0) {
            return result;
        }

        result = getPrintReturnCode();
        return result;
    }

    public int printQRCode(String str,  int qr_size, int qr_level) {
        int result;
        if (mPrinter == null)
            return PosdServiceInfoContract.ERR_PRINT_SERVICE_API_INIT;
        if (cvBlockStatus)
            return PosdServiceInfoContract.ERR_PRINT_SERVICE_BUSY;

        if (str.isEmpty())
            return ERR_PRINT_INPUT_DATA_ERROR;
        if (qr_size <= 0 || qr_size > 248) {
            return PosdServiceInfoContract.ERR_PRINT_INPUT_DATA_ERROR;
        }
        if (qr_level <= 0 || qr_level > 4) {
            return PosdServiceInfoContract.ERR_PRINT_INPUT_DATA_ERROR;
        }

        try {
            result = mPrinter.printQRCode( new PrintQRodeCallback(), str, qr_size, qr_level );
        } catch (RemoteException e) {
            Log.e(LOG_TAG, "printQRCode: ", e);
            return PosdServiceInfoContract.ERR_PRINT_SERVICE_API_INIT;
        }

        if (result != 0) {
            return result;
        }

        result = getPrintReturnCode();
        return result;
    }

    public int printImage(Bitmap bmp) {
        int result;
        if (mPrinter == null)
            return PosdServiceInfoContract.ERR_PRINT_SERVICE_API_INIT;
        if (cvBlockStatus)
            return PosdServiceInfoContract.ERR_PRINT_SERVICE_BUSY;

        if (bmp == null)
            return ERR_PRINT_INPUT_DATA_ERROR;
        try {
            byte[] image = BitmapUtils.bmp2bytesStream(bmp);
            int width = bmp.getWidth();
            int height = bmp.getHeight();
            width = 0==width%8?width:width+(8-width%8);
            if ( width <= 0 || width > 312 || (width%8) != 0) {
                return PosdServiceInfoContract.ERR_PRINT_INPUT_DATA_ERROR;
            }
            if ( height <= 0 || height > 200 ) {
                return PosdServiceInfoContract.ERR_PRINT_INPUT_DATA_ERROR;
            }
            result = mPrinter.printImage(new PrintImageCallback(), image, width, height);
        } catch (RemoteException e) {
            Log.e(LOG_TAG, "printImage: ", e);
            return PosdServiceInfoContract.ERR_PRINT_SERVICE_API_INIT;
        }

        if (result != 0) {
            return result;
        }

        result = getPrintReturnCode();
        return result;
    }

    private static class PrinterCallback extends PrinterListener.Stub {

        @Override
        public void OnPrintSuccess() throws RemoteException {
            Log.d(LOG_TAG, "OnPrintSuccess.");
            setPrintReturnCode(0);
        }

        @Override
        public void OnPrintFail(int returnCode) throws RemoteException {
            Log.d(LOG_TAG, "OnPrintFail: returnCode = " + returnCode + ".");
            setPrintReturnCode(returnCode);
        }

    }

    private class PrintBarCodeCallback extends PrintBarCodeListener.Stub {

        @Override
        public void OnSuccess() throws RemoteException {
            Log.d(LOG_TAG, "OnSuccess.");
            setPrintReturnCode(0);
        }

        @Override
        public void OnFail(int returnCode) throws RemoteException {
            Log.d(LOG_TAG, "OnFail: returnCode = " + returnCode + ".");
            setPrintReturnCode(returnCode);
        }
    }

    private class PrintQRodeCallback extends PrintQRCodeListener.Stub {

        @Override
        public void OnSuccess() throws RemoteException {
            Log.d(LOG_TAG, "OnSuccess.");
            setPrintReturnCode(0);
        }

        @Override
        public void OnFail(int returnCode) throws RemoteException {
            Log.d(LOG_TAG, "OnFail: returnCode = " + returnCode + ".");
            setPrintReturnCode(returnCode);
        }
    }

    private class PrintImageCallback extends PrintImageListener.Stub {

        @Override
        public void OnSuccess() throws RemoteException {
            Log.d(LOG_TAG, "OnSuccess.");
            setPrintReturnCode(0);
        }

        @Override
        public void OnFail(int returnCode) throws RemoteException {
            Log.d(LOG_TAG, "OnFail: returnCode = " + returnCode + ".");
            setPrintReturnCode(returnCode);
        }
    }

    private static void cvOpen() {
        Log.d(LOG_TAG, "cvOpen:" );
        cv.open();
        cvBlockStatus = false;
    }

    private static void cvBlock() {
        cvBlockStatus = true;
        Log.d(LOG_TAG, "cvBlock: start" );
        cv.block();
        Log.d(LOG_TAG, "cvBlock: end.");
    }

    private static void cvClose() {
        cv.close();
        Log.d(LOG_TAG, "cvClose.");
    }

    private static int getPrintReturnCode() {
        Log.d(LOG_TAG, "getPrintReturnCode: ");
        cvBlock();
        cvClose();
        return printReturnCode;
    }

    private static void setPrintReturnCode(int returnCode) {
        cvOpen();
        printReturnCode = returnCode;
        Log.d(LOG_TAG, "setPrintReturnCode: " + returnCode);
    }

    private void writeCmd(byte[] cmd) {

        if (cmd == null) {
            return;
        }

        for (int i = 0; i < cmd.length; i++) {
            mCommand.add(Byte.valueOf(cmd[i]));
        }
    }

    public void buildString(String str) {

        byte[] bs = null;
        try {
            bs = str.getBytes("GBK");
        } catch (UnsupportedEncodingException e) {
            bs = null;
        }
        writeCmd(bs);
    }

    private byte[] getPrintBytes() {
        if (mCommand.size() == 0){
            return null;
        }

        byte[] sendData = new byte[mCommand.size()];
        for (int i = 0; i < mCommand.size(); i++) {
            sendData[i] = ((Byte) mCommand.get(i)).byteValue();
        }
        return sendData;
    }

    /**
     * 0--靠左
     * 1--居中
     * 2--靠右
     *
     * @param n
     */
    public void setAlign(int n) {
        byte[] Cmd = new byte[3];

        Cmd[0] = 0x1B;
        Cmd[1] = 0x61;
        Cmd[2] = (byte) n;
        writeCmd(Cmd);
    }

    public void setPrintFont(int size, boolean bold) {

        switch (size) {
            case PRINT_FONT_SIZE_SMALL:
                writeCmd(new byte[]{0x1B, 0x4d, 0x01});
                if (bold) {
                    writeCmd(new byte[]{0x1B, 0x21, 0x08});
                } else {
                    writeCmd(new byte[]{0x1B, 0x21, 0x00});
                }
                break;
            case PRINT_FONT_SIZE_MIDDLE:
                writeCmd(new byte[]{0x1B, 0x4d, 0x00});
                if (bold) {
                    writeCmd(new byte[]{0x1B, 0x21, 0x08});
                } else {
                    writeCmd(new byte[]{0x1B, 0x21, 0x00});
                }
                break;
            case PRINT_FONT_SIZE_BIG:
                writeCmd(new byte[]{0x1B, 0x4d, 0x00});
                if (bold) {
                    writeCmd(new byte[]{0x1B, 0x21, 0x18});
                } else {
                    writeCmd(new byte[]{0x1B, 0x21, 0x10});
                }
                break;
            default:
        }
    }

    /**
     * 补右对齐
     * @param str
     * @return
     */
    private String rightAlignString(String str){

        try {
            byte[] strBytes = str.getBytes("GBK");
            int len = strBytes.length;
            if(len < 32){
                byte[] result = new byte[32];
                Arrays.fill(result, (byte)0x20);
                System.arraycopy(strBytes, 0, result, 32-len, len);
                return new String(result, "GBK");
            }
        } catch (UnsupportedEncodingException e) {
        }

        return str;
    }
}
