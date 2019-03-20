package ptt.terminalsdk.tools;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileFilter;

/**
 * author: zjx.
 * data:on 2018/5/3
 */

public class SDCardUtil {

    public final String TAG = "SDCardUtils---";
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
        File[] files = new File(dir).listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.isFile();
            }
        });
        logger.error("MtpUtils:指定路径下文件:{}" + files + "");
        if (files == null)
            return;
        String[] paths = new String[files.length];
        for (int co = 0; co < files.length; co++) {
            paths[co] = files[co].getAbsolutePath();
            logger.error("MtpUtils:{}" + paths[co] + "");
            fileScan(context, paths[co]);
        }
    }
}
