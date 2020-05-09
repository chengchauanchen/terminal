package ptt.terminalsdk.tools;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;

/**
 * author: zjx.
 * data:on 2018/5/3
 */

public class SDCardUtil {

    public final static String TAG = "SDCardUtils---";
    static Logger logger = Logger.getLogger(SDCardUtil.class.getName());

    /**
     * 出发扫描 mtp下的文件，在保存文件到 sd卡下后，不能显示，故这里触发一下扫描机制，让手机连上电脑后，就可以读出文件了
     *
     * @param fName，文件的完整路径名
     */
    public static void fileScan(Context context, String fName) {
        Uri data = Uri.parse("file:///" + fName);
        context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, data));
    }

    /**
     * 刷新MTP，刷新指定文件夹路径下的所有文件（只是根目录下的文件）
     *
     * @param context Context
     * @param dir     文件夹路径
     */
    public static void scanMtpAsync(Context context, String dir) {

        logger.info(TAG + "scanMtpAsync dir" + dir);

        File[] files = new File(dir).listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.isFile();
            }
        });
//        logger.error("MtpUtils:指定路径下文件:{}" + files + "");
        if (files == null)
            return;
        String[] paths = new String[files.length];
        for (int co = 0; co < files.length; co++) {
            paths[co] = files[co].getAbsolutePath();
//            logger.error("MtpUtils:{}" + paths[co] + "");
            fileScan(context, paths[co]);
        }
    }

    /**
     * 保存文件到sdcard
     * @param fileName
     * @param content
     */
    public static void saveUuidToSdCard(String path,String fileName,String content){
        try {
            File file = new File(path);
            if(!file.exists()){
                file.mkdirs();
            }
            File fileTxt = new File(path+fileName);
            if(fileTxt.exists()){
                fileTxt.delete();
            }
            FileOutputStream fos = new FileOutputStream(fileTxt);
            //FileOutputStream是字节流，如果是写文本的话，需要进一步把FileOutputStream包装 UTF-8是编码
            OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
            //写
            osw.write(content);
            osw.flush();
            fos.flush();
            osw.close();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * 读取uuid文件中的uuid
     * @param path
     * @return
     */
    public static String readUuidFromSdCard(String path){
        String result = "";
        try {
            File file = new File(path);
            if(!file.exists()){
                return result;
            }
            FileInputStream fis = new FileInputStream(file);
            InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
            //获取文件的可用长度，构建一个字符数组
            char[] input = new char[fis.available()];
            isr.read(input);
            isr.close();
            fis.close();
            result = new String(input);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return result;
    }

}
