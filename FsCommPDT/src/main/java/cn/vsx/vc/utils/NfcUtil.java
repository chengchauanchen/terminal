package cn.vsx.vc.utils;

import android.app.Activity;
import android.content.Context;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import java.nio.charset.Charset;
import java.util.Locale;
import org.apache.log4j.Logger;


public class NfcUtil {
    private static Logger logger = Logger.getLogger(NfcUtil.class.getName());
    public static final String TAG = "NfcUtil---";

    public static final int NFC_ENABLE_FALSE_NONE=1;
    public static final int NFC_ENABLE_FALSE_SHOW=2;
    public static final int NFC_ENABLE_FALSE_JUMP=3;
    public static final int NFC_ENABLE_NONE=4;

    public static final int RESULT_CODE_SUCCESS=0;
    public static final int RESULT_CODE_TYPE_ERROR=1;
    public static final int RESULT_CODE_NOT_WRITE=2;
    public static final int RESULT_CODE_NOT_ENOUGH=3;
    public static final int RESULT_CODE_ERROR=4;

    private static final String RESULT_CONTENT_SUCCESS = "写入成功！";
    private static final String RESULT_CONTENT_TYPE_ERROR = "不能识别的标签类型！";
    private static final String RESULT_CONTENT_NOT_WRITE = "该标签不能写入数据！";
    private static final String RESULT_CONTENT_NOT_ENOUGH = "标签容量不足！";
    private static final String RESULT_CONTENT_ERROR = "写入失败！";

    private NfcAdapter mNfcAdapter;
    /**
     * 检查NFC功能
     */
    public static int nfcCheck(Context context) {
        if(context!=null){
            NfcAdapter mNfcAdapter = NfcAdapter.getDefaultAdapter(context);
            if (mNfcAdapter == null) {
                logger.info(TAG + "不支持nfc功能");
                return NFC_ENABLE_FALSE_NONE;
            } else {
                if (!mNfcAdapter.isEnabled()) {
                    logger.info(TAG + "nfc开关没有打开");
                    return NFC_ENABLE_FALSE_JUMP;
                }else{
                    logger.info(TAG + "nfc开关已打开");
                    return NFC_ENABLE_FALSE_SHOW;
                }
            }
//            boolean hasNfcHce = activity.getPackageManager().hasSystemFeature(PackageManager.FEATURE_NFC_HOST_CARD_EMULATION);
//            logger.info(TAG + "是否有HCE功能："+hasNfcHce);
        }else {
            return NFC_ENABLE_NONE;
        }
    }

    /**
     * 检查是否支持NFC
     */
    public NfcAdapter checkHasNFC(Activity activity,boolean isTemp) {
        mNfcAdapter = NfcAdapter.getDefaultAdapter(activity);
        if (mNfcAdapter == null) {
            if(isTemp){
//                Toast.makeText(activity, "您的设备不支持NFC功能", Toast.LENGTH_SHORT).show();
            }
        } else {
            checkNFCEnabled(activity,mNfcAdapter,NFC_ENABLE_FALSE_NONE);
        }
        return mNfcAdapter;
    }

    /**
     * 检查NFC是否打开
     * @param mNfcAdapter
     * @param enablefalse
     */
    public void checkNFCEnabled(Activity activity,NfcAdapter mNfcAdapter,int enablefalse){
//        if (!mNfcAdapter.isEnabled()) {
//            switch (enablefalse){
//                case NFC_ENABLE_FALSE_NONE:
//                    break;
//                case NFC_ENABLE_FALSE_SHOW:
//                    Toast.makeText(activity, "NFC功能没有打开", Toast.LENGTH_SHORT).show();
//                    break;
//                case NFC_ENABLE_FALSE_JUMP:
//                    Intent setNfc = new Intent(Settings.ACTION_NFC_SETTINGS);
//                    activity.startActivity(setNfc);
//                    break;
//            }
//        }
    }

    public static NdefRecord creatTextRecord(String text) {
        byte[] langBytes = Locale.CHINA.getLanguage().getBytes(Charset.forName("US-ASCII"));
        Charset utfEncoding = Charset.forName("UTF-8");
        //将文本转换为UTF-8格式
        byte[] textBytes = text.getBytes(utfEncoding);
        //设置状态字节编码最高位数为0
        int utfBit = 0;
        //定义状态字节
        char status = (char) (utfBit + langBytes.length);
        byte[] data = new byte[1 + langBytes.length + textBytes.length];
        //设置第一个状态字节，先将状态码转换成字节
        data[0] = (byte) status;
        //设置语言编码，使用数组拷贝方法，从0开始拷贝到data中，拷贝到data的1到langBytes.length的位置
        System.arraycopy(langBytes, 0, data, 1, langBytes.length);
        //设置文本字节，使用数组拷贝方法，从0开始拷贝到data中，拷贝到data的1 + langBytes.length
        //到textBytes.length的位置
        System.arraycopy(textBytes, 0, data, 1 + langBytes.length, textBytes.length);
        //通过字节传入NdefRecord对象
        //NdefRecord.RTD_TEXT：传入类型 读写
        NdefRecord ndefRecord = new NdefRecord(NdefRecord.TNF_WELL_KNOWN,
            NdefRecord.RTD_TEXT, new byte[0], data);
        return ndefRecord;
    }
}
