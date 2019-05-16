package com.seuic.android.printdemo.posdservice;

/**
 * Created by Seuic_ZPY on 2017/3/23.
 *
 */

public class PosdServiceInfoContract {

    public static final String MESSAGE_HANDLE = "msg_type";

    public static final int MSG_CONNECTED = 0;
    public static final int MSG_DISCONNECTED = 1;

    public static final int MCU_STATUS_OK = 1;
    public static final int MCU_STATUS_WRONG = -1;


    public static final int ERR_PRINT_SERVICE_CONNECT = -198;
    public static final int ERR_PRINT_SERVICE_BUSY = -200;
    public static final int ERR_PRINT_INPUT_DATA_ERROR = -201;
    public static final int ERR_SERVICE_UNINSTALLED = -199;
    public static final int ERR_PRINT_SERVICE_API_INIT = -197;
    public static final int ERR_POS_LOCKED = -196;
}
