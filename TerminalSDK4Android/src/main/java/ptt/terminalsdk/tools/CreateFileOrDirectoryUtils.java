package ptt.terminalsdk.tools;

import android.util.Log;

import java.io.File;

/**
 * Created by zckj on 2017/7/25.
 */

public class CreateFileOrDirectoryUtils {

    public static boolean createFileOrDirectory(File file){
        try {
            Log.i("创建文件夹的时候log.text父目录" , file.getParentFile().toString());
            if(file.exists()){
                return true;
            }
            else if(file.isDirectory()){
                return file.mkdirs();
            }
            else if(file.getParentFile().exists()){
                return file.createNewFile();
            }
            else{
                return file.getParentFile().mkdirs() && file.createNewFile();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

}
