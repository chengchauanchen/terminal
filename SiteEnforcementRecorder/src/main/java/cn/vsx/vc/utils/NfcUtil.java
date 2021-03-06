package cn.vsx.vc.utils;

import android.content.Context;
import android.nfc.NfcAdapter;

import org.apache.log4j.Logger;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class NfcUtil {

    private static Logger logger = Logger.getLogger(NfcUtil.class.getName());
    public static final String TAG = "NfcUtil---";

    public static final int NFC_ENABLE_FALSE_NONE=1;
    public static final int NFC_ENABLE_FALSE_SHOW=2;
    public static final int NFC_ENABLE_FALSE_JUMP=3;
    public static final int NFC_ENABLE_NONE=4;
    //nfc
    private static NfcAdapter mNfcAdapter;


    /**
     * 检查NFC是否打开
     */
    public static int nfcCheck(Context context) {
        if(context!=null){
            mNfcAdapter = NfcAdapter.getDefaultAdapter(context);
            String content = "";
            if (mNfcAdapter == null) {
                content = "该设备不支持NFC功能";
                logger.info(TAG + content);
                return NFC_ENABLE_FALSE_NONE;
            } else {
                if (!mNfcAdapter.isEnabled()) {
                    content = "NFC没有打开";
                    logger.info(TAG + content);
                    return NFC_ENABLE_FALSE_JUMP;
                }else{
                    content = "NFC已打开";
                    logger.info(TAG + content);
                    return NFC_ENABLE_FALSE_SHOW;
                }
            }
//            if(show){
//                ToastUtil.showToast(context,content);
//            }

        }else{
            return NFC_ENABLE_NONE;
        }
    }


    /**
     * open NFC
     */
    public static void enable(Context context){
        try {
            mNfcAdapter = NfcAdapter.getDefaultAdapter(context);
            if(mNfcAdapter!=null){
                Method method =mNfcAdapter.getClass().getDeclaredMethod("enable");
                method.invoke(mNfcAdapter);
            }
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * close NFC
     */
    public static void disable(Context context){
        try {
            mNfcAdapter = NfcAdapter.getDefaultAdapter(context);
            if(mNfcAdapter!=null){
                Method method = mNfcAdapter.getClass().getDeclaredMethod("disable");
                method.invoke(mNfcAdapter);
            }
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
