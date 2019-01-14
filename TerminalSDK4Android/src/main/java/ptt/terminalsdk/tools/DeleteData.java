package ptt.terminalsdk.tools;


import org.apache.log4j.Logger;

import java.io.File;

import cn.vsx.hamster.terminalsdk.TerminalFactory;
import ptt.terminalsdk.context.MyTerminalFactory;

import static cn.vsx.hamster.terminalsdk.TerminalFactory.getSDK;

/**
 * Created by ysl on 2017/7/7.
 */

public class DeleteData {
	private static Logger logger = Logger.getLogger("DeleteData");
	//删除所有数据
	public static void deleteAllData(){
		logger.info("删除所有数据");
		deleteSharedPreferences();
		deleteSerializableData();
		deleteSQLiteDatabase();
		deleteAudioRecord();
		deletePhotoRecord();
		deleteVideoRecord();
		deleteWordRecord();
		deleteFileRecord();
	}

	//删除SharedPreferences中数据
	public static void deleteSharedPreferences(){
		MyTerminalFactory.getSDK().clearSharedPreferencesData();
	}
	//删除个呼通讯录、文件夹组列表、组扫描列表、消息列表文件
	public static void deleteSerializableData(){
		File dir = new File(MyTerminalFactory.getSDK().getSerializableDataDirectory());
		if (dir == null || !dir.exists() || !dir.isDirectory())
			return;

		for (File file : dir.listFiles()) {
			if (file.isFile())
				file.delete(); // 删除所有文件
			else if (file.isDirectory())
				deleteSerializableData(); // 递规的方式删除文件夹
		}
		dir.delete();// 删除目录本身
	}
    //删除录音文件
	public static void deleteAudioRecord(){
		File dir = new File(getSDK().getAudioRecordDirectory());
		if (dir == null || !dir.exists() || !dir.isDirectory())
			return;

		for (File file : dir.listFiles()) {
			if (file.isFile())
				file.delete(); // 删除所有文件
			else if (file.isDirectory())
				deleteAudioRecord(); // 递规的方式删除文件夹
		}
		dir.delete();// 删除目录本身
	}
	//删除文字文件
	public static void deleteWordRecord(){
		File dir = new File(getSDK().getWordRecordDirectory());
		if (dir == null || !dir.exists() || !dir.isDirectory())
			return;

		for (File file : dir.listFiles()) {
			if (file.isFile())
				file.delete(); // 删除所有文件
			else if (file.isDirectory())
				deleteWordRecord(); // 递规的方式删除文件夹
		}
		dir.delete();// 删除目录本身
	}
	//删除图片文件
	public static void deletePhotoRecord(){
		File dir = new File(getSDK().getPhotoRecordDirectory());
		if (dir == null || !dir.exists() || !dir.isDirectory())
			return;

		for (File file : dir.listFiles()) {
			if (file.isFile())
				file.delete(); // 删除所有文件
			else if (file.isDirectory())
				deletePhotoRecord(); // 递规的方式删除文件夹
		}
		dir.delete();// 删除目录本身
	}
	//删除视频文件
	public static void deleteVideoRecord(){
		File dir = new File(getSDK().getVideoRecordDirectory());
		if (dir == null || !dir.exists() || !dir.isDirectory())
			return;

		for (File file : dir.listFiles()) {
			if (file.isFile())
				file.delete(); // 删除所有文件
			else if (file.isDirectory())
				deleteVideoRecord(); // 递规的方式删除文件夹
		}
		dir.delete();// 删除目录本身
	}

	public static void deleteFileRecord () {
		File dir = new File(getSDK().getFileRecordDirectory());
		if (dir == null || !dir.exists() || !dir.isDirectory())
			return;

		for (File file : dir.listFiles()) {
			if (file.isFile())
				file.delete(); // 删除所有文件
			else if (file.isDirectory())
				deleteVideoRecord(); // 递规的方式删除文件夹
		}
		dir.delete();// 删除目录本身
	}
	//删除数据库记录
	public static void deleteSQLiteDatabase(){
		logger.info("删除数据库据");
		TerminalFactory.getSDK().getSQLiteDBManager().deleteTerminalMessage();
		TerminalFactory.getSDK().getSQLiteDBManager().deleteFolder();
		TerminalFactory.getSDK().getSQLiteDBManager().deletePDTMember();
		TerminalFactory.getSDK().getSQLiteDBManager().deletePhoneMember();
		TerminalFactory.getSDK().getSQLiteDBManager().deleteGroup();
		TerminalFactory.getSDK().getSQLiteDBManager().deleteMessageList();
	}
}
