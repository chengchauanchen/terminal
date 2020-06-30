package cn.vsx.vc.utils;

import android.content.Context;
import android.hardware.usb.UsbManager;
import android.os.storage.StorageManager;
import android.text.TextUtils;
import android.util.Log;

import java.lang.reflect.Method;

/**
 * Created by abero on 2018/10/8.
 */

public class UsbModeUtils {

    private static final String TAG = "UsbModeUtils";

    public static final String USB_FUNCTION_NONE = "none";

    public static final String USB_FUNCTION_MASS_STORAGE = "mass_storage";


    public static final String USB_FUNCTION_MTP = "mtp";


    public static final String USB_FUNCTION_PTP = "ptp";


    public static final String USB_FUNCTION_CHARGING = "charging";


    public static boolean setUsbFunction(Context context, String mode, boolean unlock) {

        boolean res = false;
        Log.i(TAG, "setUsbFunction: " + mode + " " + unlock);
        try {

            StorageManager storageManager = context.getSystemService(StorageManager.class);

            if (TextUtils.equals(mode,USB_FUNCTION_MASS_STORAGE)) {
                Method h = storageManager.getClass().getDeclaredMethod("enableUsbMassStorage");
                h.setAccessible(true);
                h.invoke(storageManager);
                Log.i(TAG, "enableUsbMassStorage: ");
            } else {
                Method h = storageManager.getClass().getDeclaredMethod("disableUsbMassStorage");
                h.setAccessible(true);
                h.invoke(storageManager);
                Log.i(TAG, "disenableUsbMassStorage: ");
            }

            UsbManager um = (UsbManager) context.getSystemService(UsbManager.class);
            Method m = um.getClass().getDeclaredMethod("setCurrentFunction", new Class[]{String.class, boolean.class});
            m.setAccessible(true);
            m.invoke(um, mode, unlock);
            Log.i(TAG, "setCurrentFunction: ");
        } catch (Exception e) {
            Log.e(TAG, e.getLocalizedMessage(), e);
        }

        return res;
    }
}
