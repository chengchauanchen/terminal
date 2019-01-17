package ptt.terminalsdk.manager.message;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;

import com.alibaba.fastjson.JSONObject;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import cn.vsx.hamster.common.GroupType;
import cn.vsx.hamster.common.MessageCategory;
import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.manager.message.ISQLiteDBManager;
import cn.vsx.hamster.terminalsdk.model.CallRecord;
import cn.vsx.hamster.terminalsdk.model.Folder;
import cn.vsx.hamster.terminalsdk.model.Group;
import cn.vsx.hamster.terminalsdk.model.Member;
import cn.vsx.hamster.terminalsdk.model.TerminalMessage;
import cn.vsx.hamster.terminalsdk.tools.Params;

/**
 * Created by ysl on 2017/3/24.
 */

public class SQLiteDBManager implements ISQLiteDBManager {
    private Logger logger = Logger.getLogger(getClass());
    private Context context;
    private SQLiteDB helper;
    private final static String TABLE_TERMINAL_MESSAGE = "terminalMessage";
    private final static String MESSAGE_LIST = "messageList";
    private final static String PDT_MEMBER = "pdtMember";
    private final static String PHONE_MEMBER = "phoneMember";
    private final static String FOLDER_GROUP = "folderGroup";
    private final static String GROUP_DATA = "groupData";
    private final static String CALL_RECORD = "callRecord";

    private SQLiteDBManager(Context context) {
        this.context = context;
        helper = new SQLiteDB(context);
    }

    private static SQLiteDBManager mySqliteDBManager;

    public static synchronized SQLiteDBManager getSQLiteDBManager(Context context) {
        if (mySqliteDBManager == null) {
            mySqliteDBManager = new SQLiteDBManager(context);
        }
        return mySqliteDBManager;
    }

    public synchronized void addTerminalMessage(TerminalMessage terminalMessage) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("current_member_id",TerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0));
        values.put("message_id", terminalMessage.messageId);
        values.put("message_body", terminalMessage.messageBody.toJSONString());
        values.put("message_url", terminalMessage.messageUrl);
        values.put("message_path", terminalMessage.messagePath);
        values.put("message_from_id", terminalMessage.messageFromId);
        values.put("message_from_name", terminalMessage.messageFromName);
        values.put("message_to_id", terminalMessage.messageToId);
        values.put("message_category", terminalMessage.messageCategory);
        values.put("message_to_name", terminalMessage.messageToName);
        values.put("message_type", terminalMessage.messageType);
        values.put("message_version", terminalMessage.messageVersion);
        values.put("result_code", terminalMessage.resultCode);
        values.put("send_time", terminalMessage.sendTime);
        db.replace(TABLE_TERMINAL_MESSAGE, null, values);
//        db.close();
    }

    @Override
    public synchronized void deleteTerminalMessage() {
        SQLiteDatabase db = helper.getWritableDatabase();
        db.execSQL("DELETE FROM terminalMessage");
//        db.close();
    }

    @Override
    public void deleteMessageFromSQLite(long message_id){
        SQLiteDatabase db = helper.getWritableDatabase();
        String sql = "DELETE FROM terminalMessage WHERE message_id = ?";
        db.execSQL(sql, new String[]{""+message_id});
    }

    public synchronized void deleteMessageFromSQLite(int messageCategory, int targetId, int memberId) {
        SQLiteDatabase db = helper.getWritableDatabase();
        if (messageCategory == MessageCategory.MESSAGE_TO_GROUP.getCode()) {
            String sql = "DELETE FROM terminalMessage WHERE current_member_id = ? AND message_to_id = ? AND message_category = ?";
            db.execSQL(sql, new String[]{TerminalFactory.getSDK().getParam(Params.MEMBER_ID,0)+"",targetId + "", 2 + ""});
        } else if (messageCategory == MessageCategory.MESSAGE_TO_PERSONAGE.getCode()) {
            String sql = "DELETE FROM terminalMessage WHERE (current_member_id = ? AND message_from_id = ? AND message_to_id = ? ) AND message_category = ?";
            db.execSQL(sql, new String[]{TerminalFactory.getSDK().getParam(Params.MEMBER_ID,0)+"",targetId + "", memberId + "", 1 + ""});
            String sql1 = "DELETE FROM terminalMessage WHERE (current_member_id = ? AND message_from_id = ? AND message_to_id = ? ) AND message_category = ?";
            db.execSQL(sql1, new String[]{TerminalFactory.getSDK().getParam(Params.MEMBER_ID,0)+"",memberId + "", targetId + "", 1 + ""});
        }
//        db.close();
    }

    public synchronized void updateTerminalMessage(TerminalMessage terminalMessage) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("current_member_id",TerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0));
        values.put("message_path", terminalMessage.messagePath);
        values.put("message_body", terminalMessage.messageBody.toJSONString());
        db.update(TABLE_TERMINAL_MESSAGE, values, "message_version = ?", new String[]{terminalMessage.messageVersion + ""});
//        db.close();
    }

    /**
     * 查询所有的记录
     */
    public synchronized List<TerminalMessage> getTerminalMessage() {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.query(TABLE_TERMINAL_MESSAGE, null, "current_member_id = ?", new String[]{TerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0) + ""}, null, null, null);
        return getTerminalMessageList(db, cursor);
    }

    @Override
    public synchronized List<TerminalMessage> getMessageRecordBySQLite(int messageCategory, int targetId, long sendTime, int memberId) {
        logger.info("查询本地数据库的参数：messageCategory = " + messageCategory + "targetId = " + targetId + "sendTime = " + sendTime);

        SQLiteDatabase db = helper.getWritableDatabase();
        Cursor cursor;
        if (messageCategory == MessageCategory.MESSAGE_TO_GROUP.getCode()) {//组消息，查message_to_id
            if (sendTime == 0) {
                String sql = "SELECT * FROM terminalMessage WHERE current_member_id = ? AND message_to_id = ? AND message_category = ? ORDER BY send_time DESC LIMIT 0,10";
//                cursor = db.query(TABLE_TERMINAL_MESSAGE, null,"current_member_id = ? and message_to_id = ? and message_category = ?",new String[]{TerminalFactory.getSDK().getParam(Params.MEMBER_ID,0)+"",targetId + "", "2"},null,null,"send_time DESC","0,10");
                cursor = db.rawQuery(sql, new String[]{TerminalFactory.getSDK().getParam(Params.MEMBER_ID,0)+"",targetId + "", 2 + ""});
            } else {
                String sql = "SELECT * FROM terminalMessage WHERE current_member_id = ? AND send_time <= ? AND message_to_id = ? AND message_category = ? ORDER BY send_time DESC LIMIT 0,10";
                cursor = db.rawQuery(sql, new String[]{TerminalFactory.getSDK().getParam(Params.MEMBER_ID,0)+"",sendTime + "", targetId + "", 2 + ""});
            }
        } else if (messageCategory == MessageCategory.MESSAGE_TO_PERSONAGE.getCode()) {//个人消息，查message_from_id
            if (sendTime == 0) {
                String sql = "SELECT * FROM terminalMessage WHERE current_member_id = ? AND (message_from_id = ?  OR (message_to_id = ? AND message_from_id = ?) ) AND message_category = ? ORDER BY send_time DESC LIMIT 0,10";
                cursor = db.rawQuery(sql, new String[]{TerminalFactory.getSDK().getParam(Params.MEMBER_ID,0)+"",targetId + "", targetId + "", memberId + "", 1 + ""});
            } else {
                String sql = "SELECT * FROM terminalMessage WHERE current_member_id = ? AND send_time <= ? AND (message_from_id = ?  OR (message_to_id = ? AND message_from_id = ?) ) AND message_category = ? ORDER BY send_time DESC LIMIT 0,10";
                cursor = db.rawQuery(sql, new String[]{TerminalFactory.getSDK().getParam(Params.MEMBER_ID,0)+"",sendTime + "", targetId + "", targetId + "", memberId + "", 1 + ""});
            }
        } else {
            cursor = null;
        }
        return getTerminalMessageList(db, cursor);
    }

    @Override
    public synchronized List<TerminalMessage> getVideoLiveRecordBySQLite(int memberId, long sendTime) {
        SQLiteDatabase db = helper.getWritableDatabase();
        Cursor cursor;
        if (sendTime == 0) {
            String sql = "SELECT * FROM terminalMessage WHERE current_member_id = ?AND message_to_id = ? AND message_from_id = ? ORDER BY send_time DESC LIMIT 0,10";
            cursor = db.rawQuery(sql, new String[]{TerminalFactory.getSDK().getParam(Params.MEMBER_ID,0)+"",memberId + "", memberId + ""});
        } else {
            String sql = "SELECT * FROM terminalMessage WHERE current_member_id = ?AND send_time <= ? AND message_to_id = ? AND message_from_id = ? ORDER BY send_time DESC LIMIT 0,10";
            cursor = db.rawQuery(sql, new String[]{TerminalFactory.getSDK().getParam(Params.MEMBER_ID,0)+"",sendTime + "", memberId + "", memberId + ""});
        }
        return getTerminalMessageList(db, cursor);
    }

    @Override
    public void addPDTMember(CopyOnWriteArrayList<Member> members) {
        SQLiteDatabase db = helper.getWritableDatabase();
        for (Member member : members) {
            ContentValues values = new ContentValues();
            values.put("member_id", member.id);
            values.put("member_no",member.getNo());
            values.put("member_name", member.getName());
            values.put("member_nick_name", member.getNickName());
            values.put("member_pinyin", member.pinyin);
            values.put("member_phone", member.phone);
            values.put("member_type", member.terminalMemberType);
            values.put("unit_name", member.unitName);
            values.put("department_name", member.departmentName);
            values.put("is_contacts", member.isContacts ? "true" : "false");
            db.replace(PDT_MEMBER, null, values);
        }
    }

    @Override
    public void updatePDTMember(Member member) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("member_name", member.getName());
        values.put("member_no",member.getNo());
        values.put("member_nick_name", member.getNickName());
        values.put("member_pinyin", member.pinyin);
        values.put("member_phone", member.phone);
        values.put("member_type", member.terminalMemberType);
        values.put("unit_name", member.unitName);
        values.put("department_name", member.departmentName);
        values.put("is_contacts", member.isContacts);
        db.update(PDT_MEMBER, values, "member_id = ?", new String[]{member.id + ""});
    }

    @Override
    public void deletePDTMember() {
        SQLiteDatabase db = helper.getWritableDatabase();
        db.execSQL("DELETE FROM pdtMember");
    }

    @Override
    public CopyOnWriteArrayList<Member> getPDTMember() {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.query(PDT_MEMBER, null, null, null, null, null, null);
        return getMemberList(db, cursor);
    }

    @Override
    public CopyOnWriteArrayList<Member> getPDTContacts() {
        SQLiteDatabase db = helper.getReadableDatabase();
        String sql = "SELECT * FROM pdtMember WHERE is_contacts = ? ";
        Cursor cursor = db.rawQuery(sql, new String[]{"true"});
        return getMemberList(db, cursor);
    }

    @Override
    public void addPhoneMember(CopyOnWriteArrayList<Member> members) {
        SQLiteDatabase db = helper.getWritableDatabase();
        for (Member member : members) {
            ContentValues values = new ContentValues();
            values.put("member_id", member.id);
            values.put("member_no",member.getNo());
            values.put("member_name", member.getName());
            values.put("member_nick_name", member.getNickName());
            values.put("member_pinyin", member.pinyin);
            values.put("member_phone", member.phone);
            values.put("member_type", member.terminalMemberType);
            values.put("unit_name", member.unitName);
            values.put("department_name", member.departmentName);
            values.put("is_contacts", member.isContacts ? "true" : "false");
            db.replace(PHONE_MEMBER, null, values);
        }
    }

    @Override
    public void updatePhoneMember(Member member) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("member_name", member.getName());
        values.put("member_no",member.getNo());
        values.put("member_nick_name", member.getNickName());
        values.put("member_pinyin", member.pinyin);
        values.put("member_phone", member.phone);
        values.put("member_type", member.terminalMemberType);
        values.put("unit_name", member.unitName);
        values.put("department_name", member.departmentName);
        values.put("is_contacts", member.isContacts);
        db.update(PHONE_MEMBER, values, "member_id = ?", new String[]{member.id + ""});
    }

    @Override
    public void deletePhoneMember() {
        SQLiteDatabase db = helper.getWritableDatabase();
        db.execSQL("DELETE FROM phoneMember");
    }

    @Override
    public CopyOnWriteArrayList<Member> getPhoneMember() {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.query(PHONE_MEMBER, null, null, null, null, null, null);
        return getMemberList(db, cursor);
    }

    @Override
    public CopyOnWriteArrayList<Member> getPhoneContacts() {
        SQLiteDatabase db = helper.getReadableDatabase();
        String sql = "SELECT * FROM phoneMember WHERE is_contacts = ? ";
        Cursor cursor = db.rawQuery(sql, new String[]{"true"});
        return getMemberList(db, cursor);
    }

    private synchronized List<TerminalMessage> getTerminalMessageList(SQLiteDatabase db, Cursor cursor) {
        List<TerminalMessage> terminalMessageList = new LinkedList<>();

        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                TerminalMessage terminalMessage = new TerminalMessage();

                terminalMessage.messageId = cursor.getLong(cursor.getColumnIndex("message_id"));
                terminalMessage.messageFromId = cursor.getInt(cursor.getColumnIndex("message_from_id"));
                terminalMessage.messageFromName = cursor.getString(cursor.getColumnIndex("message_from_name"));
                terminalMessage.messageToId = cursor.getInt(cursor.getColumnIndex("message_to_id"));
                terminalMessage.messageCategory = cursor.getInt(cursor.getColumnIndex("message_category"));
                terminalMessage.messageToName = cursor.getString(cursor.getColumnIndex("message_to_name"));
                terminalMessage.messageBody = JSONObject.parseObject(cursor.getString(cursor.getColumnIndex("message_body")));
                terminalMessage.messageUrl = cursor.getString(cursor.getColumnIndex("message_url"));
                terminalMessage.messagePath = cursor.getString(cursor.getColumnIndex("message_path"));
                terminalMessage.messageType = cursor.getInt(cursor.getColumnIndex("message_type"));
                terminalMessage.messageVersion = cursor.getLong(cursor.getColumnIndex("message_version"));
                terminalMessage.resultCode = cursor.getInt(cursor.getColumnIndex("result_code"));
                terminalMessage.sendTime = cursor.getLong(cursor.getColumnIndex("send_time"));
                //消息列表数据库才有unread_count这个字段
                try {
                    if (cursor.getColumnIndex("unread_count") == 3) {
                        terminalMessage.unReadCount = cursor.getInt(cursor.getColumnIndex("unread_count"));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                terminalMessageList.add(terminalMessage);
            }
            cursor.close();
        }
//        db.close();
        return terminalMessageList;
    }

    public synchronized void updateMessageList(List<TerminalMessage> terminalMessages) {
        SQLiteDatabase db = helper.getWritableDatabase();
        //开始事务
        db.beginTransaction();
        try{
            db.execSQL("DELETE FROM messageList");
            for (TerminalMessage terminalMessage : terminalMessages) {
                ContentValues values = new ContentValues();
                values.put("current_member_id", TerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0));
                values.put("message_id", terminalMessage.messageId);
                values.put("message_body", terminalMessage.messageBody == null ? null : terminalMessage.messageBody.toJSONString());
                values.put("message_url", terminalMessage.messageUrl);
                values.put("message_path", terminalMessage.messagePath);
                values.put("message_from_id", terminalMessage.messageFromId);
                values.put("message_from_name", terminalMessage.messageFromName);
                values.put("message_to_id", terminalMessage.messageToId);
                values.put("message_category", terminalMessage.messageCategory);
                values.put("message_to_name", terminalMessage.messageToName);
                values.put("message_type", terminalMessage.messageType);
                values.put("message_version", terminalMessage.messageVersion);
                values.put("result_code", terminalMessage.resultCode);
                values.put("send_time", terminalMessage.sendTime);
                values.put("unread_count", terminalMessage.unReadCount);
                db.replace(MESSAGE_LIST, null, values);
            }
            db.setTransactionSuccessful();  //设置事务成功完成
        }catch(Exception e){
            logger.error(e);
        }
        finally{
            //结束事务
            db.endTransaction();
        }


//        db.close();
    }

    public synchronized List<TerminalMessage> getMessageList() {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.query(MESSAGE_LIST, null, "current_member_id = ?", new String[]{TerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0) + ""}, null, null, null);
        return getTerminalMessageList(db, cursor);
    }

    public void deleteMessageList() {
        SQLiteDatabase db = helper.getWritableDatabase();
        db.execSQL("DELETE FROM messageList");
    }

    private synchronized CopyOnWriteArrayList<Member> getMemberList(SQLiteDatabase db, Cursor cursor) {
        CopyOnWriteArrayList<Member> memberList = new CopyOnWriteArrayList<>();

        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                Member member = new Member();

                member.id = cursor.getInt(cursor.getColumnIndex("member_id"));
                member.setNo(cursor.getInt(cursor.getColumnIndex("member_no")));
                member.setName(cursor.getString(cursor.getColumnIndex("member_name")));
                member.setNickName(cursor.getString(cursor.getColumnIndex("member_nick_name")));
                member.pinyin = cursor.getString(cursor.getColumnIndex("member_pinyin"));
                member.phone = cursor.getString(cursor.getColumnIndex("member_phone"));
                member.terminalMemberType = cursor.getString(cursor.getColumnIndex("member_type"));
                member.unitName = cursor.getString(cursor.getColumnIndex("unit_name"));
                member.departmentName = cursor.getString(cursor.getColumnIndex("department_name"));
                member.isContacts = Boolean.valueOf(cursor.getString(cursor.getColumnIndex("is_contacts")));

                memberList.add(member);
            }
            cursor.close();
        }
//        db.close();
        return memberList;
    }

    @Override
    public synchronized void addFolder(CopyOnWriteArrayList<Folder> folders) {
        for (Folder folder : folders) {
            if (folder.groups != null) {
                SQLiteDatabase db = helper.getWritableDatabase();
                for (Group group : folder.groups) {
                    ContentValues values = new ContentValues();
                    values.put("group_id", group.id);
                    values.put("group_no", group.no);
                    values.put("group_name", group.name);
                    values.put("folder_id", folder.id);
                    values.put("folder_name", folder.name);
                    values.put("block_id", folder.blockId);
                    values.put("block_name", folder.blockName);
                    db.replace(FOLDER_GROUP, null, values);
                }
            }
        }
    }

    @Override
    public synchronized void updateFolder(Folder folder) {
        SQLiteDatabase db = helper.getWritableDatabase();
        if (folder.groups != null) {
            for (Group group : folder.groups) {
                ContentValues values = new ContentValues();
                values.put("group_name", group.name);
                values.put("group_no", group.no);
                values.put("folder_id", folder.id);
                values.put("folder_name", folder.name);
                values.put("block_id", folder.blockId);
                values.put("block_name", folder.blockName);
                db.update(FOLDER_GROUP, values, "group_id = ?", new String[]{group.id + ""});
            }
        }
    }

    public synchronized void deleteFolder() {
        SQLiteDatabase db = helper.getWritableDatabase();
        db.execSQL("DELETE FROM folderGroup");
//        db.close();
    }

    @Override
    public synchronized CopyOnWriteArrayList<Folder> getFolder() {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.query(FOLDER_GROUP, null, null, null, null, null, null);
        return getFolderList(db, cursor);
    }

    @Override
    public void addGroup(CopyOnWriteArrayList<Group> groups) {
        SQLiteDatabase db = helper.getWritableDatabase();
        for (Group group : groups) {
            ContentValues values = new ContentValues();
            values.put("group_id", group.getId());
            values.put("group_no",group.getNo());
            values.put("group_name", group.getName());
            values.put("department_name",group.getDepartmentName());
            values.put("group_type",group.getGroupType().getCode());
            db.replace(GROUP_DATA, null, values);
        }
    }

    @Override
    public void deleteGroup() {
        SQLiteDatabase db = helper.getWritableDatabase();
        db.execSQL("DELETE FROM groupData");
    }

    @Override
    public void updateGroup(Group group) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("group_name", group.getName());
        values.put("group_no", group.getNo());
        values.put("department_name",group.getDepartmentName());
        values.put("group_type",group.getGroupType().getCode());
        db.update(GROUP_DATA, values, "group_id = ?", new String[]{group.id + ""});
    }

    @Override
    public CopyOnWriteArrayList<Group> getGroup() {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.query(GROUP_DATA, null, null, null, null, null, null);
        return getGroupList(db, cursor);
    }

    private synchronized CopyOnWriteArrayList<Group> getGroupList(SQLiteDatabase db, Cursor cursor) {
        CopyOnWriteArrayList<Group> groupList = new CopyOnWriteArrayList<>();

        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                Group group = new Group();
                group.setNo(cursor.getInt(cursor.getColumnIndex("group_no")));
                group.setId(cursor.getInt(cursor.getColumnIndex("group_id")));
                group.setName(cursor.getString(cursor.getColumnIndex("group_name")));
                group.setDepartmentName(cursor.getString(cursor.getColumnIndex("department_name")));
                group.setGroupType(GroupType.getInstanceByCode(cursor.getInt(cursor.getColumnIndex("group_type"))));
                groupList.add(group);
            }
            cursor.close();
        }
        return groupList;
    }


    private synchronized CopyOnWriteArrayList<Folder> getFolderList(SQLiteDatabase db, Cursor cursor) {
        CopyOnWriteArrayList<Folder> folderList;
        Map<Integer, Folder> folderMap = new HashMap<>();
        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                if (folderMap.size() == 0) {//第一个条目时，
                    Folder folder = setFolder(cursor);
                    folderMap.put(folder.id, folder);
                } else {
                    if (folderMap.containsKey(cursor.getInt(cursor.getColumnIndex("folder_id")))) {
                        Folder folder = folderMap.get(cursor.getInt(cursor.getColumnIndex("folder_id")));
                        Group group = new Group();
                        group.no = cursor.getInt(cursor.getColumnIndex("group_no"));
                        group.id = cursor.getInt(cursor.getColumnIndex("group_id"));
                        group.name = cursor.getString(cursor.getColumnIndex("group_name"));

                        folder.groups.add(group);
                    }else {
                        Folder folder = setFolder(cursor);
                        folderMap.put(folder.id, folder);
                    }
                }
            }
            cursor.close();
        }
//        db.close();
        folderList = new CopyOnWriteArrayList<>(folderMap.values());
        return folderList;
    }

    @NonNull
    private Folder setFolder(Cursor cursor) {
        Folder folder = new Folder();
        folder.id = cursor.getInt(cursor.getColumnIndex("folder_id"));
        folder.name = cursor.getString(cursor.getColumnIndex("folder_name"));
        folder.blockId = cursor.getInt(cursor.getColumnIndex("block_id"));
        folder.blockName = cursor.getString(cursor.getColumnIndex("block_name"));
        folder.groups = new ArrayList<>();
        Group group = new Group();
        group.no = cursor.getInt(cursor.getColumnIndex("group_no"));
        group.id = cursor.getInt(cursor.getColumnIndex("group_id"));
        group.name = cursor.getString(cursor.getColumnIndex("group_name"));
        folder.groups.add(group);
        return folder;
    }

    //通话记录
    @Override
    public void addCallRecord(CopyOnWriteArrayList<CallRecord> callRecords) {
        SQLiteDatabase db = helper.getWritableDatabase();
        for (CallRecord callRecord :callRecords) {
            ContentValues values = new ContentValues();
            values.put("call_id", callRecord.getCallId());
            values.put("member_name",callRecord.getMemberName());
            values.put("call_phone", callRecord.getPhone());
            values.put("call_records",callRecord.getCallRecords());
            values.put("call_time",callRecord.getTime());
            values.put("call_path",callRecord.getPath());
            values.put("call_download",callRecord.isDownLoad()?"true":"false");
            values.put("call_playing",callRecord.isPlaying()?"true":"false");
            db.replace(CALL_RECORD, null, values);
        }
    }

    @Override
    public void deleteCallRecord() {
        SQLiteDatabase db = helper.getWritableDatabase();
        db.execSQL("DELETE FROM callRecord");
    }

    @Override
    public void updateCallRecord(CallRecord callRecord) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("call_id", callRecord.getCallId());
        values.put("member_name",callRecord.getMemberName());
        values.put("call_phone", callRecord.getPhone());
        values.put("call_records",callRecord.getCallRecords());
        values.put("call_time",callRecord.getTime());
        values.put("call_path",callRecord.getPath());
        values.put("call_download",callRecord.isDownLoad());
        values.put("call_playing",callRecord.isPlaying());
        db.update(CALL_RECORD, values, "call_id = ?", new String[]{callRecord.callId + ""});
    }

    @Override
    public CopyOnWriteArrayList<CallRecord> getCallRecords() {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.query(CALL_RECORD, null, null, null, null, null, null);
        return getCallRecordList(db, cursor);
    }

    private synchronized CopyOnWriteArrayList<CallRecord> getCallRecordList(SQLiteDatabase db, Cursor cursor) {
        CopyOnWriteArrayList<CallRecord> callRecordList = new CopyOnWriteArrayList<>();

        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                CallRecord callRecord = new CallRecord();
                callRecord.setCallId(cursor.getString(cursor.getColumnIndex("call_id")));
                callRecord.setMemberName(cursor.getString(cursor.getColumnIndex("member_name")));
                callRecord.setPhone(cursor.getString(cursor.getColumnIndex("call_phone")));
                callRecord.setCallRecords(cursor.getString(cursor.getColumnIndex("call_records")));
                callRecord.setTime(cursor.getString(cursor.getColumnIndex("call_time")));
                callRecord.setPath(cursor.getString(cursor.getColumnIndex("call_path")));
                callRecord.setDownLoad(Boolean.valueOf(cursor.getString(cursor.getColumnIndex("call_download"))));
                callRecord.setPlaying(Boolean.valueOf(cursor.getString(cursor.getColumnIndex("call_playing"))));
                callRecordList.add(callRecord);
            }
            cursor.close();
        }
        logger.info("查询本地数据库CallRecord结果：" + callRecordList);
        return callRecordList;
    }

}
