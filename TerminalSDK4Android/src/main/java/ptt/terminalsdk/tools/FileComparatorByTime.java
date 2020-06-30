package ptt.terminalsdk.tools;

import java.io.File;
import java.util.Comparator;

/**
 * 将文件按时间降序排列
 */
public  class FileComparatorByTime implements Comparator<File> {

    @Override
    public int compare(File file1, File file2) {
        if (file1.lastModified() < file2.lastModified()) {
            return 1;// 最后修改的文件在前
        } else {
            return -1;
        }
    }
}

