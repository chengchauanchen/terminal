package com.zectec.imageandfileselector.base;

import android.support.v4.util.ArrayMap;

import com.zectec.imageandfileselector.bean.FileInfo;
import com.zectec.imageandfileselector.bean.Image;
import com.zectec.imageandfileselector.bean.Record;

/**
 * Created by gt358 on 2017/8/23.
 */

public class Constant {
    public static ArrayMap<String, FileInfo> files = new ArrayMap<>();
    public static ArrayMap<String, Image> images = new ArrayMap<>();
    public static ArrayMap<String, Record> records = new ArrayMap<>();

    //同时发送文件的最大数量
    public static final int FILE_COUNT_MAX = 5;
    public static final String FILE_COUNT_MAX_PROMPT = "只能同时发送"+FILE_COUNT_MAX+"个文件!";
}
