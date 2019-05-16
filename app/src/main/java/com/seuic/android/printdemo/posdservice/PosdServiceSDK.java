package com.seuic.android.printdemo.posdservice;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;

import com.seuic.android.PosdService;
import com.seuic.android.Printer;

import java.util.List;

import static android.content.Context.BIND_AUTO_CREATE;

/**
 * Created by Seuic_ZPY on 2017/3/25.
 *
 */

public class PosdServiceSDK {
    private static final String LOG_TAG = PosdServiceSDK.class.getSimpleName();
    private static PosdService posdService = null;
    private static Printer printer = null;
    private static PrintService printService = new PrintService();
    private static int mcuStatus = PosdServiceInfoContract.MCU_STATUS_WRONG;
    private Context mContext;

    private static final String POS_DAEMON_SERVICE_PACKAGE_NAME = "com.seuic.android";
    private static final String POS_DAEMON_SERVICE_ACTION = "com.seuic.android.PosdService";
    private static final String POSD_SERVIVCE_INTENT_ACTION = "com.seuic.android.PosdService";

    public int setContext(Context context) {
        this.mContext = context.getApplicationContext();
        return bindPosdService(mContext);
    }

    public PrintService getPrintService() {
        return printService;
    }

    public int checkPosdServiceStauts() {
        return bindPosdService(mContext);
    }

    private static boolean isInstalled(Context context, String packageName) {
        if(packageName == null || context == null) {
            return false;
        }
        PackageManager packageManager = context.getPackageManager();
        List<PackageInfo> list = packageManager.getInstalledPackages(PackageManager.PERMISSION_GRANTED);
        for (PackageInfo info : list) {
            if (packageName.equals(info.packageName)) {
                return true;
            }
        }
        return false;
    }

    private ServiceConnection posdConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            posdService = PosdService.Stub.asInterface(service);
            if (null != posdService) {
                try {
                    if (posdService.getMcuStatus() == PosdServiceInfoContract.MCU_STATUS_OK) {
                        mcuStatus = PosdServiceInfoContract.MCU_STATUS_OK;
                    } else {
                        Log.e(LOG_TAG, "onServiceConnected: POS is locked.");
                    }
                    printer = Printer.Stub.asInterface(posdService.getPrinter());
                } catch (RemoteException e) {
                    Log.e(LOG_TAG, "onServiceConnected: Failed to initialize the API.");
                } finally {
                    Log.d(LOG_TAG, "onServiceConnected: Connection successful.");
                }
                Bundle bundle = new Bundle();
                bundle.putInt(PosdServiceInfoContract.MESSAGE_HANDLE, PosdServiceInfoContract.MSG_CONNECTED);
                Message msg = new Message();
                msg.setData(bundle);
                mHandler.sendMessage(msg);
            } else {
                Log.e(LOG_TAG, "onServiceConnected: Failed to connect service.");
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.e(LOG_TAG, "onServiceDisconnected: Connection is broken.");
            mcuStatus = PosdServiceInfoContract.MCU_STATUS_WRONG;
            posdService = null;
            printer = null;

            Bundle bundle = new Bundle();
            bundle.putInt(PosdServiceInfoContract.MESSAGE_HANDLE, PosdServiceInfoContract.MSG_DISCONNECTED);
            Message msg = new Message();
            msg.setData(bundle);
            mHandler.sendMessage(msg);
        }
    };

    private int bindPosdService(Context context) {
//        if (!isInstalled(context, POS_DAEMON_SERVICE_PACKAGE_NAME)) {
//            return PosdServiceInfoContract.ERR_SERVICE_UNINSTALLED;
//        }

        if (null == posdService) {
            Intent intent = new Intent();
            String packageName = getPosdServicePackageName(POSD_SERVIVCE_INTENT_ACTION);
            if(packageName == null) {
                return PosdServiceInfoContract.ERR_SERVICE_UNINSTALLED;
            }
            intent.setPackage(packageName);
            intent.setAction(POSD_SERVIVCE_INTENT_ACTION);
            Log.d(LOG_TAG, "bindPosdService: " + intent);
            context.bindService(intent, posdConnection, BIND_AUTO_CREATE);
            if (null == posdService)
                return PosdServiceInfoContract.ERR_PRINT_SERVICE_CONNECT;
        }

        if (null == printer) {
            return PosdServiceInfoContract.ERR_PRINT_SERVICE_API_INIT;
        }

        if (mcuStatus != PosdServiceInfoContract.MCU_STATUS_OK ) {
            return PosdServiceInfoContract.ERR_POS_LOCKED;
        }

        return 0;
    }

    private String getPosdServicePackageName(String intent_action) {
        PackageManager pm = mContext.getPackageManager();
        List<ResolveInfo> list = pm.queryIntentServices(new Intent(intent_action), 0);
        if (list == null || list.isEmpty()) {
            return null;
        }
        if (list.size() > 1) {
        }
        String packageName = list.get(0).serviceInfo.packageName;
        return packageName;
    }

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            Bundle bundle = msg.getData();
            switch (bundle.getInt(PosdServiceInfoContract.MESSAGE_HANDLE)) {
                case PosdServiceInfoContract.MSG_CONNECTED:
                    printService.setPrinter(printer);
                    break;
                case PosdServiceInfoContract.MSG_DISCONNECTED:
                    printService.setPrinter(null);
                    bindPosdService(mContext);
                    break;
                default:
            }
        }
    };
}
