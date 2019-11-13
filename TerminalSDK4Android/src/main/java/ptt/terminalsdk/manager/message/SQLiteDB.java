package ptt.terminalsdk.manager.message;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by ysl on 2017/3/24.
 */

public class SQLiteDB extends SQLiteOpenHelper {


    public SQLiteDB(Context context) {
        super(context, "4gptt.db", null, 22);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //会话列表
        db.execSQL("CREATE TABLE IF NOT EXISTS terminalMessage (_id INTEGER primary key autoincrement, current_member_id INTEGER, message_version INTEGER, send_time INTEGER, " +
                "message_id INTEGER, message_from_id INTEGER, message_from_name varchar, message_url varchar, message_path varchar, message_body TEXT, " +
                "message_type INTEGER, message_to_id INTEGER, message_to_name varchar, result_code INTEGER, message_category INTEGER,message_status INTEGER,message_to_unique_no INTEGER,message_from_unique_no INTEGER, " +
                "message_body_id varchar, unique(message_id))");
        //消息列表
        db.execSQL("CREATE TABLE IF NOT EXISTS messageList (_id INTEGER primary key autoincrement, current_member_id INTEGER,message_version INTEGER, send_time INTEGER, unread_count INTEGER," +
                "message_id INTEGER, message_from_id INTEGER, message_from_name varchar, message_url varchar, message_path varchar, message_body TEXT, " +
                "message_type INTEGER, message_to_id INTEGER, message_to_name varchar, result_code INTEGER, message_category INTEGER,message_status INTEGER,message_to_unique_no INTEGER,message_from_unique_no INTEGER," +
                "message_body_id varchar)");
        //合成作战组消息列表
        db.execSQL("CREATE TABLE IF NOT EXISTS combatMessageList (_id INTEGER primary key autoincrement, current_member_id INTEGER,message_version INTEGER, send_time INTEGER, unread_count INTEGER," +
                "message_id INTEGER, message_from_id INTEGER, message_from_name varchar, message_url varchar, message_path varchar, message_body TEXT, " +
                "message_type INTEGER, message_to_id INTEGER, message_to_name varchar, result_code INTEGER, message_category INTEGER,message_status INTEGER,message_to_unique_no INTEGER,message_from_unique_no INTEGER," +
                "message_body_id varchar)");
        //已完成合成作战组消息列表
        db.execSQL("CREATE TABLE IF NOT EXISTS historyCombatMessageList (_id INTEGER primary key autoincrement, current_member_id INTEGER,message_version INTEGER, send_time INTEGER, unread_count INTEGER," +
                "message_id INTEGER, message_from_id INTEGER, message_from_name varchar, message_url varchar, message_path varchar, message_body TEXT, " +
                "message_type INTEGER, message_to_id INTEGER, message_to_name varchar, result_code INTEGER, message_category INTEGER,message_status INTEGER,message_to_unique_no INTEGER,message_from_unique_no INTEGER," +
                "message_body_id varchar)");
        //成员列表
        db.execSQL("CREATE TABLE IF NOT EXISTS member (_id INTEGER primary key autoincrement, member_id INTEGER, member_name varchar, " +
                "member_pinyin varchar, member_phone varchar, department_name varchar, unique(member_id))");
        //PDT成员列表
        db.execSQL("CREATE TABLE IF NOT EXISTS pdtMember (_id INTEGER primary key autoincrement, member_id INTEGER, member_no INTEGER,unique_no INTEGER, member_name varchar,  " +
                "member_pinyin varchar, member_phone varchar, department_name varchar, unique(member_id))");

        //警务通成员列表
        db.execSQL("CREATE TABLE IF NOT EXISTS phoneMember (_id INTEGER primary key autoincrement, member_id INTEGER, member_no INTEGER,unique_no INTEGER, member_name varchar,  " +
                "member_pinyin varchar, member_phone varchar, department_name varchar, unique(member_id))");
        //组列表
        db.execSQL("CREATE TABLE IF NOT EXISTS folderGroup (_id INTEGER primary key autoincrement, group_id INTEGER, group_no INTEGER, group_name varchar,department_name varchar, folder_id INTEGER, folder_name varchar, " +
                "block_id INTEGER, block_name varchar, unique(group_id))");

        //组列表
        db.execSQL("CREATE TABLE IF NOT EXISTS groupData (_id INTEGER primary key autoincrement, group_id INTEGER, group_no INTEGER, group_unique_no INTEGER,group_name varchar,department_name varchar,group_type varchar,response_group_type varchar, unique(group_id))");

        //通话记录列表
        db.execSQL("CREATE TABLE IF NOT EXISTS callRecord (_id INTEGER primary key autoincrement, call_id varchar,member_name varchar ,call_phone varchar,call_records varchar,call_time varchar,call_path varchar,call_download varchar,call_playing varchar, unique(call_id))");

        //警情记录
        db.execSQL("CREATE TABLE IF NOT EXISTS warningRecord (_id INTEGER primary key autoincrement,alarm_no varchar,status INTEGER,levels INTEGER,alarm_time varchar, address varchar, summary varchar,apersonphone varchar,aperson varchar,recvperson varchar,recvphone varchar,date varchar,unread INTEGER,unique(alarm_no))");

        //比特星本地文件
        db.execSQL("CREATE TABLE IF NOT EXISTS bitStarFileRecord (_id INTEGER primary key autoincrement,file_height INTEGER,file_width INTEGER,file_duration INTEGER,file_date varchar,file_name varchar,file_path varchar ,file_type varchar,file_time INTEGER,file_state INTEGER, unique(file_name))");

        db.execSQL("CREATE TABLE IF NOT EXISTS allGroup (_id INTEGER primary key autoincrement, current_member_id INTEGER,group_id INTEGER, group_no INTEGER, group_name_py varchar,group_name_first_py varchar,group_name varchar,department_name varchar,temp_group_type varchar,business_id varchar,group_type varcha,unique_no INTEGER,dept_id INTEGER,created_member_unique_no INTEGER ,response_group_type varcha ,high_user INTEGER,processing_state varcha,created_member_name varcha,created_member_no INTEGER, unique(group_id))");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO: 2019/11/12 有问题，更新数据库的时候把以前的数据都丢了
        db.execSQL("DROP TABLE IF EXISTS terminalMessage");
        db.execSQL("DROP TABLE IF EXISTS messageList");
        db.execSQL("DROP TABLE IF EXISTS combatMessageList");
        db.execSQL("DROP TABLE IF EXISTS historyCombatMessageList");
        db.execSQL("DROP TABLE IF EXISTS member");
        db.execSQL("DROP TABLE IF EXISTS pdtMember");
        db.execSQL("DROP TABLE IF EXISTS phoneMember");
        db.execSQL("DROP TABLE IF EXISTS folderGroup");
        db.execSQL("DROP TABLE IF EXISTS groupData");
        db.execSQL("DROP TABLE IF EXISTS callRecord");
        db.execSQL("DROP TABLE IF EXISTS bitStarFileRecord");
        db.execSQL("DROP TABLE IF EXISTS allGroup");
        onCreate(db);
    }
}
