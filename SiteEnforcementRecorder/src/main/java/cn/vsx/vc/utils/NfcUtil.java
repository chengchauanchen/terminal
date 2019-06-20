package cn.vsx.vc.utils;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.nfc.tech.Ndef;
import android.nfc.tech.NfcA;
import android.nfc.tech.NfcB;
import android.nfc.tech.NfcF;
import android.nfc.tech.NfcV;
import android.os.Parcelable;
import android.text.TextUtils;

import com.google.gson.Gson;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.Arrays;

import cn.vsx.hamster.terminalsdk.model.NFCBean;

public class NfcUtil {

    private Logger logger = Logger.getLogger(getClass());
    public static final String TAG = "NfcUtil---";

    public static final int RESULT_CODE_SUCCESS=0;
    public static final int RESULT_CODE_TYPE_ERROR=1;
    public static final int RESULT_CODE_ERROR=2;

    private static final String RESULT_CONTENT_SUCCESS = "已刷入成员信息！";
    private static final String RESULT_CONTENT_TYPE_ERROR = "不能识别的信息类型！";
    private static final String RESULT_CONTENT_ERROR = "信息读取失败！";

    // 私有的请求码
    private static final int REQUEST_CODE = 1 << 16;
    // 卡片返回来的正确信号
    private final byte[] SELECT_OK = stringToBytes("1000");
    private static final String AID = "f22eca0ca7eb9ed0022a64e4fff5c34b";
    private static final String HEADER = "00A40400";
    //nfc
    private  NfcAdapter mNfcAdapter;
    private  IntentFilter[] mIntentFilter = null;
    private  PendingIntent mPendingIntent = null;
    private  String[][] mTechList = null;

    private OnReadListener onReadListener;

    /**
     * 构造函数，用于初始化nfc
     */
    public NfcUtil(Context context) {
        nfcCheck(context);
        nfcInit(context);
    }

    /**
     * 检查NFC是否打开
     */
    public  void nfcCheck(Context context) {
        mNfcAdapter = NfcAdapter.getDefaultAdapter(context);
        if (mNfcAdapter == null) {
            logger.info(TAG + "不支持nfc功能");
        } else {
            if (!mNfcAdapter.isEnabled()) {
                logger.info(TAG + "nfc开关没有打开");
            }else{
                logger.info(TAG + "nfc开关已打开");
            }
        }
    }

    /**
     * 初始化nfc设置
     */
    private  void nfcInit(Context context) {
        mPendingIntent = PendingIntent.getActivity(context, REQUEST_CODE, new Intent(context, context.getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        IntentFilter ndef = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
        IntentFilter tech = new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED);
        IntentFilter tag = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        try {
            ndef.addDataType("*/*");
        } catch (IntentFilter.MalformedMimeTypeException e) {
            e.printStackTrace();
        }
        mIntentFilter = new IntentFilter[]{ndef, tech, tag};
        // 只针对ACTION_TECH_DISCOVERED
        mTechList = new String[][]{
                {IsoDep.class.getName()}, {NfcA.class.getName()}, {NfcB.class.getName()},
                {NfcV.class.getName()}, {NfcF.class.getName()}, {Ndef.class.getName()}};
    }
    ///**
    // * 初始化nfc设置
    // */
    //private  void nfcInit(Context context) {
    //    mPendingIntent = PendingIntent.getActivity(context, REQUEST_CODE, new Intent(context, context.getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
    //    IntentFilter tech = new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED);
    //    mIntentFilter = new IntentFilter[]{tech};
    //    // 只针对ACTION_TECH_DISCOVERED
    //    mTechList = new String[][]{{IsoDep.class.getName()}};
    //}

    /**
     * 解析
     * @param intent
     */
    public void proccessIntent(Intent intent) {
        if(intent == null){
            return;
        }
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())){
            Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            NdefMessage msg = (NdefMessage) rawMsgs[0];
            //String text = TextRecord.parse(msg.getRecords()[0]).getText();
            String content = null;
                NdefRecord ndefRecord = msg.getRecords()[0];
                if(ndefRecord!=null){
                    content =  parseTextRecord(ndefRecord);
                }
            logger.info(TAG + "NfcAdapter.ACTION_NDEF_DISCOVERED:proccessIntent：content:"+content);
            if(onReadListener!=null){
                NFCBean nfcBean = getNFCData(content);
                if(nfcBean != null){
                    onReadListener.onReadResult(RESULT_CODE_SUCCESS,NfcAdapter.ACTION_NDEF_DISCOVERED,RESULT_CONTENT_SUCCESS,nfcBean);
                }else{
                    onReadListener.onReadResult(RESULT_CODE_ERROR,NfcAdapter.ACTION_TECH_DISCOVERED,RESULT_CONTENT_ERROR,null);
                }
            }
        }else
            if(NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction())){
            // IsoDep卡片通信的工具类，Tag就是卡
            IsoDep isoDep = IsoDep.get((Tag) intent.getParcelableExtra(NfcAdapter.EXTRA_TAG));
            if (isoDep == null) {
                String info = "读取卡信息失败";
                if(onReadListener!=null){
                    onReadListener.onReadResult(RESULT_CODE_TYPE_ERROR,NfcAdapter.ACTION_TECH_DISCOVERED,RESULT_CONTENT_TYPE_ERROR,null);
                }
                return;
            }
            try {
                // NFC与卡进行连接
                isoDep.connect();
                //转换指令为byte[]
                byte[] command = buildSelectApdu(AID);

                // 发送指令
                byte[] result = isoDep.transceive(command);

                // 截取响应数据
                int resultLength = result.length;
                byte[] statusWord = {result[resultLength - 2], result[resultLength - 1]};
                byte[] payload = Arrays.copyOf(result, resultLength - 2);

                // 检验响应数据
                if (Arrays.equals(SELECT_OK, statusWord)) {
                    String accountNumber = new String(payload, "UTF-8");
                    logger.info(TAG + "NfcAdapter.ACTION_TECH_DISCOVERED:proccessIntent：content:"+accountNumber);
                    if(onReadListener!=null){
                        onReadListener.onReadResult(RESULT_CODE_SUCCESS,NfcAdapter.ACTION_TECH_DISCOVERED,RESULT_CONTENT_SUCCESS,getNFCData(accountNumber));
                    }
                } else {
                    String info = bytesToString(result);
                    logger.info(TAG + "NfcAdapter.ACTION_TECH_DISCOVERED:proccessIntent：error:"+info);
                    if(onReadListener!=null){
                        onReadListener.onReadResult(RESULT_CODE_ERROR,NfcAdapter.ACTION_TECH_DISCOVERED,RESULT_CONTENT_ERROR,null);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 往nfc写入数据
     */
    public static void writeNFCToTag(String data, Intent intent) throws IOException, FormatException {
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        Ndef ndef = Ndef.get(tag);
        ndef.connect();
        NdefRecord ndefRecord = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            ndefRecord = NdefRecord.createTextRecord(null, data);
        }
        NdefRecord[] records = {ndefRecord};
        NdefMessage ndefMessage = new NdefMessage(records);
        ndef.writeNdefMessage(ndefMessage);
    }




    private byte[] buildSelectApdu(String aid) {
        return stringToBytes(HEADER + String.format("%02X", aid.length() / 2) + aid);
    }

    private byte[] stringToBytes(String s) {
        int len = s.length();
        if (len % 2 == 1) {
            throw new IllegalArgumentException("指令字符串长度必须为偶数 !!!");
        }
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[(i / 2)] = ((byte) ((Character.digit(s.charAt(i), 16) << 4) + Character
                    .digit(s.charAt(i + 1), 16)));
        }
        return data;
    }

    private String bytesToString(byte[] data) {
        StringBuilder sb = new StringBuilder();
        for (byte d : data) {
            sb.append(String.format("%02X", d));
        }
        return sb.toString();
    }

    /**
     * 解析NDEF文本数据，从第三个字节开始，后面的文本数据
     * @param ndefRecord
     * @return
     */
    public static String parseTextRecord(NdefRecord ndefRecord) {
        /**
         * 判断数据是否为NDEF格式
         */
        //判断TNF
        if (ndefRecord.getTnf() != NdefRecord.TNF_WELL_KNOWN) {
            return null;
        }
        //判断可变的长度的类型
        if (!Arrays.equals(ndefRecord.getType(), NdefRecord.RTD_TEXT)) {
            return null;
        }
        try {
            //获得字节数组，然后进行分析
            byte[] payload = ndefRecord.getPayload();
            //下面开始NDEF文本数据第一个字节，状态字节
            //判断文本是基于UTF-8还是UTF-16的，取第一个字节"位与"上16进制的80，16进制的80也就是最高位是1，
            //其他位都是0，所以进行"位与"运算后就会保留最高位
            String textEncoding = ((payload[0] & 0x80) == 0) ? "UTF-8" : "UTF-16";
            //3f最高两位是0，第六位是1，所以进行"位与"运算后获得第六位
            int languageCodeLength = payload[0] & 0x3f;
            //下面开始NDEF文本数据第二个字节，语言编码
            //获得语言编码
            String languageCode = new String(payload, 1, languageCodeLength, "US-ASCII");
            //下面开始NDEF文本数据后面的字节，解析出文本
            String textRecord = new String(payload, languageCodeLength + 1,
                payload.length - languageCodeLength - 1, textEncoding);
            return textRecord;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 解析获取警情和临时组id
     * @param content
     * @return
     */
    private NFCBean getNFCData(String content) {
        NFCBean bean = null;
        if(TextUtils.isEmpty(content)){
            return bean;
        }
        try{
            bean =  new Gson().fromJson(content, NFCBean.class);
        }catch (Exception e){
            e.printStackTrace();
            bean = null;
        }finally {
            return bean;
        }
    }

    /**
     * 设置写入数据的结果的回调
     * @param onReadListener
     */
    public void setOnReadListener(OnReadListener onReadListener){
        this.onReadListener = onReadListener;
    }

    /**
     * 写入数据结果的回调
     */
    public interface OnReadListener{
        void onReadResult(int resultCode, String readType, String resultDescribe, NFCBean bean);
    }

    public NfcAdapter getmNfcAdapter() {
        return mNfcAdapter;
    }

    public void setmNfcAdapter(NfcAdapter mNfcAdapter) {
        this.mNfcAdapter = mNfcAdapter;
    }

    public IntentFilter[] getmIntentFilter() {
        return mIntentFilter;
    }

    public void setmIntentFilter(IntentFilter[] mIntentFilter) {
        this.mIntentFilter = mIntentFilter;
    }

    public PendingIntent getmPendingIntent() {
        return mPendingIntent;
    }

    public void setmPendingIntent(PendingIntent mPendingIntent) {
        this.mPendingIntent = mPendingIntent;
    }

    public String[][] getmTechList() {
        return mTechList;
    }

    public void setmTechList(String[][] mTechList) {
        this.mTechList = mTechList;
    }
}
