package cn.vsx.vc.utils;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.nfc.NfcAdapter;

import org.apache.log4j.Logger;

public class NfcUtil {
    private static Logger logger = Logger.getLogger(NfcUtil.class.getName());
    public static final String TAG = "NfcUtil---";


    /**
     * 检查NFC功能
     */
    public static void nfcCheck(Activity activity) {
        if(activity!=null&&!activity.isFinishing()){
            NfcAdapter mNfcAdapter = NfcAdapter.getDefaultAdapter(activity);
            if (mNfcAdapter == null) {
                logger.info(TAG + "不支持nfc功能");
                return ;
            } else {
                if (!mNfcAdapter.isEnabled()) {
                    logger.info(TAG + "nfc开关没有打开");
                }else{
                    logger.info(TAG + "nfc开关已打开");
                }
            }
            boolean hasNfcHce = activity.getPackageManager().hasSystemFeature(PackageManager.FEATURE_NFC_HOST_CARD_EMULATION);
            logger.info(TAG + "是否有HCE功能："+hasNfcHce);
        }
    }
}
