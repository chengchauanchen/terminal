package ptt.terminalsdk.service;

import android.annotation.TargetApi;
import android.nfc.cardemulation.HostApduService;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;

import org.apache.log4j.Logger;

import java.util.Arrays;

import ptt.terminalsdk.R;
import ptt.terminalsdk.context.MyTerminalFactory;
import ptt.terminalsdk.receiveHandler.ReceiveNFCWriteResultHandler;

@TargetApi(Build.VERSION_CODES.KITKAT)
public class CardService extends HostApduService {
    // 正确信号
    private byte[] SELECT_OK = hexStringToByteArray("1000");

    // 错误信号
    private byte[] UNKNOWN_ERROR = hexStringToByteArray("0000");

    private static  String AID = "f22eca0ca7eb9ed0022a64e4fff5c34b";
    private static  String HEADER = "00A40400";
    protected Logger logger = Logger.getLogger(getClass());
    @Override
    public void onCreate() {
        super.onCreate();
        AID = getResources().getString(R.string.aid);
        HEADER = getResources().getString(R.string.header);
    }

    /**
     * 接收到 NFC 读卡器发送的应用协议数据单元 (APDU) 调用
     * 注意：此方法回调在UI线程,若进行联网操作时，需开子线程
     * 并先返回null，当子线程有数据结果后，再进行回调返回处理
     */
    @Override
    public byte[] processCommandApdu(byte[] commandApdu, Bundle extras) {
        //做判断，是否在警情临时组（警情编号和警情临时组id）
        //如果没有，就不发送  return null
        String transmitData = MyTerminalFactory.getSDK().getNfcManager().getTransmitData();
        logger.debug("processCommandApdu---transmitData:"+transmitData);
        if(TextUtils.isEmpty(transmitData)){
            return null;
        }
        // 将指令转换成 byte[]
        byte[] selectAPDU = buildSelectApdu(AID);

        // 判断是否和读卡器发来的数据相同
        if (Arrays.equals(selectAPDU, commandApdu)) {
            // 获取卡号 byte[]
            byte[] accountBytes = transmitData.getBytes();

            // 处理欲返回的响应数据
            MyTerminalFactory.getSDK().notifyReceiveHandler(ReceiveNFCWriteResultHandler.class,0,"");
            return concatArrays(accountBytes, SELECT_OK);
        } else {
            MyTerminalFactory.getSDK().notifyReceiveHandler(ReceiveNFCWriteResultHandler.class,-1,"");
            return UNKNOWN_ERROR;
        }
    }
    @Override
    public void onDeactivated(int reason) {

    }

    private byte[] hexStringToByteArray(String s) throws IllegalArgumentException {
        int len = s.length();
        if (len % 2 == 1) {
            throw new IllegalArgumentException("指令字符串长度必须为偶数 !!!");
        }
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    private byte[] buildSelectApdu(String aid) {
        return hexStringToByteArray(HEADER + String.format("%02X", aid.length() / 2) + aid);
    }

    private byte[] concatArrays(byte[] first, byte[]... rest) {
        int totalLength = first.length;
        for (byte[] array : rest) {
            totalLength += array.length;
        }
        byte[] result = Arrays.copyOf(first, totalLength);
        int offset = first.length;
        for (byte[] array : rest) {
            System.arraycopy(array, 0, result, offset, array.length);
            offset += array.length;
        }
        return result;
    }

}