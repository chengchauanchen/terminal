package cn.vsx.vc.receiver;

import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.text.TextUtils;

import com.google.gson.Gson;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Arrays;

import cn.vsx.hamster.terminalsdk.model.RecorderBindTranslateBean;

public class NFCCardReader implements NfcAdapter.ReaderCallback{

    private static Logger logger = Logger.getLogger(NFCCardReader.class.getName());
    public static final String TAG = "NFCCardReader---";

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

    private WeakReference<OnReadListener> onReadListener;

    public NFCCardReader(OnReadListener mOnReadListener) {
        onReadListener = new WeakReference<OnReadListener>( mOnReadListener);
    }

    @Override
    public void onTagDiscovered(Tag tag) {
        IsoDep isoDep = IsoDep.get(tag);
        if (isoDep == null) {
//            String info = "读取卡信息失败";
            if(onReadListener!=null){
                onReadListener.get().onReadResult(RESULT_CODE_TYPE_ERROR,NfcAdapter.ACTION_TECH_DISCOVERED,RESULT_CONTENT_TYPE_ERROR,null);
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
                    onReadListener.get().onReadResult(RESULT_CODE_SUCCESS,NfcAdapter.ACTION_TECH_DISCOVERED,RESULT_CONTENT_SUCCESS, getRecorderBindTranslateBean(accountNumber));
                }
            } else {
                String info = bytesToString(result);
                logger.info(TAG + "NfcAdapter.ACTION_TECH_DISCOVERED:proccessIntent：error:"+info);
                if(onReadListener!=null){
                    onReadListener.get().onReadResult(RESULT_CODE_ERROR,NfcAdapter.ACTION_TECH_DISCOVERED,RESULT_CONTENT_ERROR,null);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
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
    public static RecorderBindTranslateBean getRecorderBindTranslateBean(String content) {
        RecorderBindTranslateBean bean = null;
        if(TextUtils.isEmpty(content)){
            return bean;
        }
        try{
            bean =  new Gson().fromJson(content, RecorderBindTranslateBean.class);
        }catch (Exception e){
            e.printStackTrace();
            bean = null;
        }finally {
            return bean;
        }
    }

    /**
     * 写入数据结果的回调
     */
    public interface OnReadListener{
        void onReadResult(int resultCode, String readType, String resultDescribe, RecorderBindTranslateBean bean);
    }
}
