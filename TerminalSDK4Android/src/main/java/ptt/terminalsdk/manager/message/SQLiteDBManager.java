package ptt.terminalsdk.manager.message;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.alibaba.fastjson.JSONObject;
import com.pinyinsearch.util.PinyinUtil;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import cn.vsx.hamster.common.MessageCategory;
import cn.vsx.hamster.common.MessageStatus;
import cn.vsx.hamster.common.MessageType;
import cn.vsx.hamster.common.util.JsonParam;
import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.manager.message.ISQLiteDBManager;
import cn.vsx.hamster.terminalsdk.manager.search.GroupSearchBean;
import cn.vsx.hamster.terminalsdk.manager.search.MemberSearchBean;
import cn.vsx.hamster.terminalsdk.manager.search.TianjinDeviceBean;
import cn.vsx.hamster.terminalsdk.model.Account;
import cn.vsx.hamster.terminalsdk.model.BitStarFileRecord;
import cn.vsx.hamster.terminalsdk.model.CallRecord;
import cn.vsx.hamster.terminalsdk.model.Folder;
import cn.vsx.hamster.terminalsdk.model.Group;
import cn.vsx.hamster.terminalsdk.model.Member;
import cn.vsx.hamster.terminalsdk.model.TerminalMessage;
import cn.vsx.hamster.terminalsdk.model.VideoMeetingDataBean;
import cn.vsx.hamster.terminalsdk.model.VideoMeetingMessage;
import cn.vsx.hamster.terminalsdk.model.WarningRecord;
import cn.vsx.hamster.terminalsdk.tools.MyGsonUtil;
import cn.vsx.hamster.terminalsdk.tools.Params;
import cn.vsx.hamster.terminalsdk.tools.TerminalMessageUtil;
import cn.vsx.hamster.terminalsdk.tools.Util;
import ptt.terminalsdk.tools.PinyinUtils;

/**
 * Created by ysl on 2017/3/24.
 */

public class SQLiteDBManager implements ISQLiteDBManager {
    private Logger logger = Logger.getLogger(getClass());
    private Context context;
    private SQLiteDB helper;
    private final static String TABLE_TERMINAL_MESSAGE = "terminalMessage";
    private final static String MESSAGE_LIST = "messageList";
    private final static String COMBAT_MESSAGE_LIST = "combatMessageList";
    private final static String HISTORY_COMBAT_MESSAGE_LIST = "historyCombatMessageList";
    private final static String PDT_MEMBER = "pdtMember";
    private final static String PHONE_MEMBER = "phoneMember";
    private final static String FOLDER_GROUP = "folderGroup";
    private final static String GROUP_DATA = "groupData";
    private final static String CALL_RECORD = "callRecord";
    private final static String BIT_STAR_FILE_RECORD = "bitStarFileRecord";
    private final static String WARNING_RECORD = "warningRecord";
    private final static String ALL_GROUP = "allGroup";
    private final static String ALL_ACCOUNT = "allAccount";
    private final static String VIDEO_MEETING_MESSAGE = "videoMeetingMessage";

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

    @Override
    public synchronized void addTerminalMessage(TerminalMessage terminalMessage) {
//        logger.info("向terminalMessage表存消息：" + terminalMessage);
        try{
            SQLiteDatabase db = helper.getWritableDatabase();
            TerminalMessage message = null;
            Cursor cursor = db.query(TABLE_TERMINAL_MESSAGE, null, "current_member_id = ? AND message_id = ?", new String[]{TerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0) + "",terminalMessage.messageId+""}, null, null, null);
            List<TerminalMessage> list = getTerminalMessageList(db, cursor);
            if(list!=null&&!list.isEmpty()) {
                message = list.get(0);
            }
            if(message!=null){
                if(message.messageBody!=null){
                    terminalMessage.messageBody.put(
                        JsonParam.UNREAD, (message.messageBody.containsKey(JsonParam.UNREAD))?message.messageBody.getBooleanValue(JsonParam.UNREAD):true);
                }else{
                    terminalMessage.messageBody.put(JsonParam.UNREAD, true);
                }
            }
            ContentValues values = new ContentValues();
            values.put("current_member_id", TerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0));
            values.put("message_id", terminalMessage.messageId);
            values.put("message_body_id", terminalMessage.messageBodyId);
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
            values.put("message_to_unique_no", terminalMessage.messageToUniqueNo);
            values.put("message_from_unique_no", terminalMessage.messageFromUniqueNo);
            values.put("message_status", MessageStatus.valueOf(terminalMessage.messageStatus).getCode());
            db.replace(TABLE_TERMINAL_MESSAGE, null, values);
            //        db.close();
        }catch (Exception e){
            logger.error(e.toString());
        }
    }

    @Override
    public synchronized void deleteTerminalMessage() {
        try{
            SQLiteDatabase db = helper.getWritableDatabase();
            db.execSQL("DELETE FROM terminalMessage");
            //        db.close();
        }catch (Exception e){
            logger.error(e.toString());
        }
    }

    @Override
    public void deleteMessageFromSQLite(long message_id) {
        try{
            logger.error("删除message_id" + message_id + "消息");
            SQLiteDatabase db = helper.getWritableDatabase();
            String sql = "DELETE FROM terminalMessage WHERE message_id = ?";
            db.execSQL(sql, new String[]{"" + message_id});
        }catch (Exception e){
            logger.error(e.toString());
        }
    }

    @Override
    public synchronized void deleteMessageFromSQLite(int messageCategory, int targetId, int memberId) {
        try{
            SQLiteDatabase db = helper.getWritableDatabase();
            logger.error("删除targetId" + targetId + "消息");
            if (messageCategory == MessageCategory.MESSAGE_TO_GROUP.getCode()) {
                String sql = "DELETE FROM terminalMessage WHERE current_member_id = ? AND message_to_id = ? AND message_category = ?";
                db.execSQL(sql, new String[]{TerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0) + "", targetId + "", 2 + ""});
            } else if (messageCategory == MessageCategory.MESSAGE_TO_PERSONAGE.getCode()) {
                String sql = "DELETE FROM terminalMessage WHERE (current_member_id = ? AND message_from_id = ? AND message_to_id = ? ) AND message_category = ?";
                db.execSQL(sql, new String[]{TerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0) + "", targetId + "", memberId + "", 1 + ""});
                String sql1 = "DELETE FROM terminalMessage WHERE (current_member_id = ? AND message_from_id = ? AND message_to_id = ? ) AND message_category = ?";
                db.execSQL(sql1, new String[]{TerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0) + "", memberId + "", targetId + "", 1 + ""});
            }
            //        db.close();
        }catch (Exception e){
            logger.error(e.toString());
        }
    }

    @Override
    public synchronized void updateTerminalMessage(TerminalMessage terminalMessage) {
        try{
            SQLiteDatabase db = helper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("current_member_id", TerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0));
            values.put("message_path", terminalMessage.messagePath);
            values.put("message_body", terminalMessage.messageBody.toJSONString());
            db.update(TABLE_TERMINAL_MESSAGE, values, "current_member_id = ? AND message_id = ?", new String[]{TerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0)+"",terminalMessage.messageId + ""});
            //        db.close();
        }catch (Exception e){
            logger.error(e.toString());
        }
    }

    /**
     * 更新消息的撤回状态
     *
     * @param messageBodyId
     * @param messageStatus
     */
    @Override
    public synchronized void updateTerminalMessageWithDraw(String messageBodyId, int messageStatus) {
        try{
            SQLiteDatabase db = helper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("message_status", messageStatus);
            db.update(TABLE_TERMINAL_MESSAGE, values, "message_body_id = ?", new String[]{messageBodyId + ""});
            //        db.close();
        }catch (Exception e){
            logger.error(e.toString());
        }
    }

    /**
     * 查询所有的记录
     */
    @Override
    public synchronized List<TerminalMessage> getTerminalMessage() {
        try{
            SQLiteDatabase db = helper.getReadableDatabase();
            Cursor cursor = db.query(TABLE_TERMINAL_MESSAGE, null, "current_member_id = ?", new String[]{TerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0) + ""}, null, null, null);
            return getTerminalMessageList(db, cursor);
        }catch (Exception e){
            logger.error(e.toString());
        }
        return new LinkedList<TerminalMessage>();
    }

    @Override
    public synchronized List<TerminalMessage> getMessageRecordBySQLite(int messageCategory, int targetId, long sendTime, int memberId) {
        logger.info("查询本地数据库的参数：messageCategory = " + messageCategory + "targetId = " + targetId + "sendTime = " + sendTime);
        try{
            SQLiteDatabase db = helper.getWritableDatabase();
            Cursor cursor;
            if (messageCategory == MessageCategory.MESSAGE_TO_GROUP.getCode()) {//组消息，查message_to_id
                if (sendTime == 0) {
                    String sql = "SELECT * FROM terminalMessage WHERE current_member_id = ? AND message_to_id = ? AND message_category = ? ORDER BY send_time DESC LIMIT 0,10";
                    //                cursor = db.query(TABLE_TERMINAL_MESSAGE, null,"current_member_id = ? and message_to_id = ? and message_category = ?",new String[]{TerminalFactory.getSDK().getParam(Params.MEMBER_ID,0)+"",targetId + "", "2"},null,null,"send_time DESC","0,10");
                    cursor = db.rawQuery(sql, new String[]{TerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0) + "", targetId + "", 2 + ""});
                } else {
                    String sql = "SELECT * FROM terminalMessage WHERE current_member_id = ? AND send_time <= ? AND message_to_id = ? AND message_category = ? ORDER BY send_time DESC LIMIT 0,10";
                    cursor = db.rawQuery(sql, new String[]{TerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0) + "", sendTime + "", targetId + "", 2 + ""});
                }
            } else if (messageCategory == MessageCategory.MESSAGE_TO_PERSONAGE.getCode()) {//个人消息，查message_from_id
                if (sendTime == 0) {
                    String sql = "SELECT * FROM terminalMessage WHERE current_member_id = ? AND (message_from_id = ?  OR (message_to_id = ? AND message_from_id = ?) ) AND message_category = ? ORDER BY send_time DESC LIMIT 0,10";
                    cursor = db.rawQuery(sql, new String[]{TerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0) + "", targetId + "", targetId + "", memberId + "", 1 + ""});
                } else {
                    String sql = "SELECT * FROM terminalMessage WHERE current_member_id = ? AND send_time <= ? AND (message_from_id = ?  OR (message_to_id = ? AND message_from_id = ?) ) AND message_category = ? ORDER BY send_time DESC LIMIT 0,10";
                    cursor = db.rawQuery(sql, new String[]{TerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0) + "", sendTime + "", targetId + "", targetId + "", memberId + "", 1 + ""});
                }
            } else {
                cursor = null;
            }
            return getTerminalMessageList(db, cursor);
        }catch (Exception e){
            logger.error(e.toString());
        }
        return new LinkedList<TerminalMessage>();
    }

    @Override
    public synchronized List<TerminalMessage> getVideoLiveRecordBySQLite(int memberId, long sendTime) {
        List<TerminalMessage> terminalMessageList = new LinkedList<>();
        try{
            SQLiteDatabase db = helper.getWritableDatabase();
            Cursor cursor;
            if (sendTime == 0) {
                String sql = "SELECT * FROM terminalMessage WHERE current_member_id = ? AND message_from_id = ? AND message_type = ? AND result_code = ? ORDER BY send_time DESC LIMIT 0,10";
                cursor = db.rawQuery(sql, new String[]{TerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0) + "", memberId + "", MessageType.VIDEO_LIVE.getCode() + "", "0"});
            } else {
                String sql = "SELECT * FROM terminalMessage WHERE current_member_id = ?AND send_time <= ? AND message_from_id = ? AND message_type = ? AND result_code = ? ORDER BY send_time DESC LIMIT 0,10";
                cursor = db.rawQuery(sql, new String[]{TerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0) + "", sendTime + "", memberId + "", MessageType.VIDEO_LIVE.getCode() + "", "0"});
            }
            if (cursor != null && cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    TerminalMessage terminalMessage = new TerminalMessage();

                    terminalMessage.messageId = cursor.getLong(cursor.getColumnIndex("message_id"));
                    terminalMessage.messageBodyId = cursor.getString(cursor.getColumnIndex("message_body_id"));
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
                    terminalMessage.messageFromUniqueNo = cursor.getLong(cursor.getColumnIndex("message_from_unique_no"));
                    terminalMessage.messageToUniqueNo = cursor.getLong(cursor.getColumnIndex("message_to_unique_no"));
                    int messageStatus = cursor.getInt(cursor.getColumnIndex("message_status"));
                    terminalMessage.messageStatus = (messageStatus == 1) ? MessageStatus.MESSAGE_RECALL.toString() : MessageStatus.MESSAGE_NORMAL.toString();
                    //消息列表数据库才有unread_count这个字段
                    try {
                        if (cursor.getColumnIndex("unread_count") != -1) {
                            terminalMessage.unReadCount = cursor.getInt(cursor.getColumnIndex("unread_count"));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    //筛选
                    if (TerminalMessageUtil.isLiveMessage(terminalMessage)) {
                        terminalMessageList.add(terminalMessage);
                    }
                }
                cursor.close();
            }
        }catch (Exception e){
            logger.error(e.toString());
        }
        return terminalMessageList;
    }

    @Override public TerminalMessage getMessageFromSQLite(long message_id) {
        TerminalMessage message = null;
        SQLiteDatabase db = null;
        try{
            db = helper.getWritableDatabase();
            Cursor cursor = db.query(TABLE_TERMINAL_MESSAGE, null, "current_member_id = ? AND message_id = ?", new String[]{TerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0) + "",message_id+""}, null, null, null);
            List<TerminalMessage> list = getTerminalMessageList(db, cursor);
            if(list!=null&&!list.isEmpty()) {
                message = list.get(0);
            }
        }catch (Exception e){
            logger.error(e.toString());
        }finally {
            try{
                if(db!=null){
                    db.close();
                }
            }catch (Exception e){
                logger.error(e.toString());
            }
        }
//        logger.debug("向terminalMessage表获取消息message_id："+message_id+"-message:"+message);
        return message;
    }

    @Override
    public void addPDTMember(CopyOnWriteArrayList<Member> members) {
        try{
            SQLiteDatabase db = helper.getWritableDatabase();
            for (Member member : members) {
                ContentValues values = new ContentValues();
                values.put("member_id", member.id);
                values.put("member_no", member.getNo());
                values.put("member_name", member.getName());
                values.put("member_pinyin", member.getPinyin());
                values.put("member_phone", member.phone);
                values.put("department_name", member.departmentName);
                values.put("unique_no", member.getUniqueNo());
                db.replace(PDT_MEMBER, null, values);
            }
        }catch (Exception e){
            logger.error(e.toString());
        }
    }

    @Override
    public void updatePDTMember(Member member) {
        try{
            SQLiteDatabase db = helper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("member_name", member.getName());
            values.put("member_no", member.getNo());
            values.put("member_pinyin", member.pinyin);
            values.put("member_phone", member.phone);
            values.put("department_name", member.departmentName);
            values.put("unique_no", member.getUniqueNo());
            db.update(PDT_MEMBER, values, "member_id = ?", new String[]{member.id + ""});
        }catch (Exception e){
            logger.error(e.toString());
        }
    }

    @Override
    public void deletePDTMember() {
        try{
            SQLiteDatabase db = helper.getWritableDatabase();
            db.execSQL("DELETE FROM pdtMember");
        }catch (Exception e){
            logger.error(e.toString());
        }
    }

    @Override
    public CopyOnWriteArrayList<Member> getPDTMember() {
        try{
            SQLiteDatabase db = helper.getReadableDatabase();
            Cursor cursor = db.query(PDT_MEMBER, null, null, null, null, null, null);
            return getMemberList(db, cursor);
        }catch (Exception e){
            logger.error(e.toString());
        }
        return new CopyOnWriteArrayList<Member>();
    }

    @Override
    public CopyOnWriteArrayList<Member> getPDTContacts() {
        try{
            SQLiteDatabase db = helper.getReadableDatabase();
            String sql = "SELECT * FROM pdtMember WHERE is_contacts = ? ";
            Cursor cursor = db.rawQuery(sql, new String[]{"true"});
            return getMemberList(db, cursor);
        }catch (Exception e){
            logger.error(e.toString());
        }
        return new CopyOnWriteArrayList<Member>();
    }

    @Override
    public void addPhoneMember(CopyOnWriteArrayList<Member> members) {
        try{
            SQLiteDatabase db = helper.getWritableDatabase();
            for (Member member : members) {
                ContentValues values = new ContentValues();
                values.put("member_id", member.id);
                values.put("member_no", member.getNo());
                values.put("member_name", member.getName());
                values.put("member_pinyin", member.pinyin);
                values.put("member_phone", member.phone);
                values.put("department_name", member.departmentName);
                values.put("unique_no", member.getUniqueNo());
                db.replace(PHONE_MEMBER, null, values);
            }
        }catch (Exception e){
            logger.error(e.toString());
        }
    }

    @Override
    public void updatePhoneMember(Member member) {
        try{
            SQLiteDatabase db = helper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("member_name", member.getName());
            values.put("member_no", member.getNo());
            values.put("member_pinyin", member.pinyin);
            values.put("member_phone", member.phone);
            values.put("department_name", member.departmentName);
            values.put("unique_no", member.getUniqueNo());
            db.update(PHONE_MEMBER, values, "member_id = ?", new String[]{member.id + ""});
        }catch (Exception e){
            logger.error(e.toString());
        }
    }

    @Override
    public void deletePhoneMember() {
        try{
            SQLiteDatabase db = helper.getWritableDatabase();
            db.execSQL("DELETE FROM phoneMember");
        }catch (Exception e){
            logger.error(e.toString());
        }
    }

    @Override
    public CopyOnWriteArrayList<Member> getPhoneMember() {
        try{
            SQLiteDatabase db = helper.getReadableDatabase();
            Cursor cursor = db.query(PHONE_MEMBER, null, null, null, null, null, null);
            return getMemberList(db, cursor);
        }catch (Exception e){
            logger.error(e.toString());
        }
        return  new CopyOnWriteArrayList<Member>();
    }

    @Override
    public CopyOnWriteArrayList<Member> getPhoneContacts() {
        try{
            SQLiteDatabase db = helper.getReadableDatabase();
            String sql = "SELECT * FROM phoneMember WHERE is_contacts = ? ";
            Cursor cursor = db.rawQuery(sql, new String[]{"true"});
            return getMemberList(db, cursor);
        }catch (Exception e){
            logger.error(e.toString());
        }
        return new CopyOnWriteArrayList<Member>();
    }

    private synchronized List<TerminalMessage> getTerminalMessageList(SQLiteDatabase db, Cursor cursor) {
        List<TerminalMessage> terminalMessageList = new LinkedList<>();
        try {
            if (cursor != null && cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    TerminalMessage terminalMessage = new TerminalMessage();

                    terminalMessage.messageId = cursor.getLong(cursor.getColumnIndex("message_id"));
                    terminalMessage.messageBodyId = cursor.getString(cursor.getColumnIndex("message_body_id"));
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
                    terminalMessage.messageFromUniqueNo = cursor.getLong(cursor.getColumnIndex("message_from_unique_no"));
                    terminalMessage.messageToUniqueNo = cursor.getLong(cursor.getColumnIndex("message_to_unique_no"));
                    int messageStatus = cursor.getInt(cursor.getColumnIndex("message_status"));
                    terminalMessage.messageStatus = (messageStatus == 1) ? MessageStatus.MESSAGE_RECALL.toString() : MessageStatus.MESSAGE_NORMAL.toString();
                    //消息列表数据库才有unread_count这个字段
                    if (cursor.getColumnIndex("unread_count") != -1) {
                        terminalMessage.unReadCount = cursor.getInt(cursor.getColumnIndex("unread_count"));
                    }
//                    logger.info("从数据库取出数据：" + terminalMessage);
                    terminalMessageList.add(terminalMessage);
                }
                cursor.close();
            }
        } catch (Exception e) {
            logger.error(e.toString());
        }

//        db.close();
        return terminalMessageList;
    }

    @Override
    public synchronized void updateMessageList(List<TerminalMessage> terminalMessages) {
//        logger.info("保存消息列表数据:" + terminalMessages);
        SQLiteDatabase db = null;
        try {
            db = helper.getWritableDatabase();
            //开始事务
            db.beginTransaction();
            db.execSQL("DELETE FROM messageList");
            for (TerminalMessage terminalMessage : terminalMessages) {
                ContentValues values = new ContentValues();
                values.put("current_member_id", TerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0));
                values.put("message_id", terminalMessage.messageId);
                values.put("message_body_id", terminalMessage.messageBodyId);
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
                values.put("message_to_unique_no", terminalMessage.messageToUniqueNo);
                values.put("message_from_unique_no", terminalMessage.messageFromUniqueNo);
                values.put("message_status", MessageStatus.valueOf(terminalMessage.messageStatus).getCode());
                db.replace(MESSAGE_LIST, null, values);
            }
            db.setTransactionSuccessful();  //设置事务成功完成
        } catch (Exception e) {
            logger.error(e);
        } finally {
            //结束事务
            try{
                if(db!=null){
                    db.endTransaction();
                }
            }catch (Exception e){
                logger.error(e.toString());
            }
        }


//        db.close();
    }

    @Override
    public TerminalMessage existMessageListDB(int userId) {
        logger.info("获取本地数据库中是否存在对应消息existMessageListDB userId:" + userId);
        SQLiteDatabase db = helper.getReadableDatabase();
        TerminalMessage terminalMessage = null;
        Cursor cursor;

        int memberId = TerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0);

//        if (userId ==memberId) {
//            //发给谁
//            cursor = db.query(MESSAGE_LIST, null, "message_from_id = ? and current_member_id = ?", new String[]{userId + "",memberId + ""}, null, null, null);
//            logger.info("获取本地数据库中是否存在对应消息existMessageListDB message_from_id = {"+userId+"} and current_member_id = {"+memberId+"}");
//        } else {
//            cursor = db.query(MESSAGE_LIST, null, "(message_to_id = ? or message_from_id =?) and current_member_id = ?", new String[]{userId + "",memberId + ""}, null, null, null);
//            logger.info("获取本地数据库中是否存在对应消息existMessageListDB message_to_id = {"+userId+"} and current_member_id = {"+memberId+"}");
//        }

        cursor = db.query(MESSAGE_LIST, null, "(message_to_id = ? or message_from_id =?) and current_member_id = ?", new String[]{userId + "",userId + "",memberId + ""}, null, null, null);
        logger.info("获取本地数据库中是否存在对应消息existMessageListDB message_to_id = {"+userId+"} and current_member_id = {"+memberId+"}");

        List<TerminalMessage> terminalMessageList = getTerminalMessageList(db, cursor);
        logger.info("查询消息列表数据existMessageListDB：" + terminalMessageList);
        if(terminalMessageList!=null &&terminalMessageList.size()>0){
            terminalMessage = terminalMessageList.get(0);
            logger.info("找到了："+terminalMessage);
        }
        return terminalMessage;
    }

    @Override
    public synchronized void updateCombatMessageList(List<TerminalMessage> terminalMessages) {
        logger.info("保存合成作战组列表数据:" + terminalMessages);
        SQLiteDatabase db = null;
        try {
            db = helper.getWritableDatabase();
            //开始事务
            db.beginTransaction();
            db.execSQL("DELETE FROM combatMessageList");
            for (TerminalMessage terminalMessage : terminalMessages) {
                ContentValues values = new ContentValues();
                values.put("current_member_id", TerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0));
                values.put("message_id", terminalMessage.messageId);
                values.put("message_body_id", terminalMessage.messageBodyId);
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
                values.put("message_to_unique_no", terminalMessage.messageToUniqueNo);
                values.put("message_from_unique_no", terminalMessage.messageFromUniqueNo);
                values.put("message_status", MessageStatus.valueOf(terminalMessage.messageStatus).getCode());
                db.replace(COMBAT_MESSAGE_LIST, null, values);
            }
            db.setTransactionSuccessful();  //设置事务成功完成
        } catch (Exception e) {
            logger.error(e);
        } finally {
            //结束事务
            try{
                if(db!=null){
                    db.endTransaction();
                }
            }catch (Exception e){
                logger.error(e.toString());
            }
        }


//        db.close();
    }

    @Override
    public synchronized void updateHistoryCombatMessageList(List<TerminalMessage> terminalMessages) {
        logger.info("保存合成作战组历史列表数据:" + terminalMessages);
        SQLiteDatabase db = null;
        try {
            db = helper.getWritableDatabase();
            //开始事务
            db.beginTransaction();
            db.execSQL("DELETE FROM historyCombatMessageList");
            for (TerminalMessage terminalMessage : terminalMessages) {
                ContentValues values = new ContentValues();
                values.put("current_member_id", TerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0));
                values.put("message_id", terminalMessage.messageId);
                values.put("message_body_id", terminalMessage.messageBodyId);
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
                values.put("message_to_unique_no", terminalMessage.messageToUniqueNo);
                values.put("message_from_unique_no", terminalMessage.messageFromUniqueNo);
                values.put("message_status", MessageStatus.valueOf(terminalMessage.messageStatus).getCode());
                db.replace(HISTORY_COMBAT_MESSAGE_LIST, null, values);
            }
            db.setTransactionSuccessful();  //设置事务成功完成
        } catch (Exception e) {
            logger.error(e);
        } finally {
            //结束事务
            try{
                if(db!=null){
                    db.endTransaction();
                }
            }catch (Exception e){
                logger.error(e.toString());
            }
        }


//        db.close();
    }

    @Override
    public synchronized List<TerminalMessage> getMessageList() {
        try{
            SQLiteDatabase db = helper.getReadableDatabase();
            Cursor cursor = db.query(MESSAGE_LIST, null, "current_member_id = ?", new String[]{TerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0) + ""}, null, null, null);
            List<TerminalMessage> terminalMessageList = getTerminalMessageList(db, cursor);
            logger.info("查询消息列表数据：" + terminalMessageList);
            return terminalMessageList;
        }catch (Exception e){
            logger.error(e.toString());
        }
        return new LinkedList<TerminalMessage>();
    }

    @Override
    public synchronized List<TerminalMessage> getCombatMessageList() {
        try{
            SQLiteDatabase db = helper.getReadableDatabase();
            Cursor cursor = db.query(COMBAT_MESSAGE_LIST, null, "current_member_id = ?", new String[]{TerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0) + ""}, null, null, null);
            List<TerminalMessage> terminalMessageList = getTerminalMessageList(db, cursor);
            logger.info("查询合成作战组消息列表数据：" + terminalMessageList);
            return terminalMessageList;
        }catch (Exception e){
            logger.error(e.toString());
        }
        return new LinkedList<TerminalMessage>();
    }

    @Override
    public synchronized List<TerminalMessage> getHistoryCombatMessageList() {
        try{
            SQLiteDatabase db = helper.getReadableDatabase();
            Cursor cursor = db.query(HISTORY_COMBAT_MESSAGE_LIST, null, "current_member_id = ?", new String[]{TerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0) + ""}, null, null, null);
            List<TerminalMessage> terminalMessageList = getTerminalMessageList(db, cursor);
            logger.info("查询合成作战组历史消息列表数据：" + terminalMessageList);
            return terminalMessageList;
        }catch (Exception e){
            logger.error(e.toString());
        }
        return new LinkedList<TerminalMessage>();
    }

    @Override
    public void deleteMessageList() {
        try{
            SQLiteDatabase db = helper.getWritableDatabase();
            db.execSQL("DELETE FROM messageList");
        }catch (Exception e){
            logger.error(e.toString());
        }
    }

    private synchronized CopyOnWriteArrayList<Member> getMemberList(SQLiteDatabase db, Cursor cursor) {
        CopyOnWriteArrayList<Member> memberList = new CopyOnWriteArrayList<>();
        try{
            if (cursor != null && cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    Member member = new Member();

                    member.id = cursor.getInt(cursor.getColumnIndex("member_id"));
                    member.setNo(cursor.getInt(cursor.getColumnIndex("member_no")));
                    member.setName(cursor.getString(cursor.getColumnIndex("member_name")));
                    member.pinyin = cursor.getString(cursor.getColumnIndex("member_pinyin"));
                    member.phone = cursor.getString(cursor.getColumnIndex("member_phone"));
                    member.departmentName = cursor.getString(cursor.getColumnIndex("department_name"));
                    member.setUniqueNo(cursor.getLong(cursor.getColumnIndex("unique_no")));
                    member.setUniqueNoStr(member.getUniqueNo()+"");
                    memberList.add(member);
                }
                cursor.close();
            }
            //        db.close();
        }catch (Exception e){
            logger.error(e.toString());
        }
        return memberList;
    }

    @Override
    public synchronized void addFolder(CopyOnWriteArrayList<Folder> folders) {
        try{
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
        }catch (Exception e){
            logger.error(e.toString());
        }
    }

    @Override
    public synchronized void updateFolder(Folder folder) {
        try{
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
        }catch (Exception e){
            logger.error(e.toString());
        }
    }

    @Override
    public synchronized void deleteFolder() {
        try{
            SQLiteDatabase db = helper.getWritableDatabase();
            db.execSQL("DELETE FROM folderGroup");
            //        db.close();
        }catch (Exception e){
            logger.error(e.toString());
        }

    }

    @Override
    public synchronized CopyOnWriteArrayList<Folder> getFolder() {
        try{
            SQLiteDatabase db = helper.getReadableDatabase();
            Cursor cursor = db.query(FOLDER_GROUP, null, null, null, null, null, null);
            return getFolderList(db, cursor);
        }catch (Exception e){
            logger.error(e.toString());
        }
        return new CopyOnWriteArrayList<Folder>();
    }

    @Override
    public void addGroup(CopyOnWriteArrayList<Group> groups) {
        try{
            SQLiteDatabase db = helper.getWritableDatabase();
            for (Group group : groups) {
                ContentValues values = new ContentValues();
                values.put("group_id", group.getId());
                values.put("group_no", group.getNo());
                values.put("group_name", group.getName());
                values.put("department_name", group.getDepartmentName());
                values.put("group_type", group.getGroupType());
                values.put("response_group_type", group.getResponseGroupType());
                values.put("group_unique_no", group.getUniqueNo());
                db.replace(GROUP_DATA, null, values);
            }
        }catch (Exception e){
            logger.error(e.toString());
        }

    }

    @Override
    public void deleteGroup() {
        try{
            SQLiteDatabase db = helper.getWritableDatabase();
            db.execSQL("DELETE FROM groupData");
        }catch (Exception e){
            logger.error(e.toString());
        }
    }

    @Override
    public void updateGroup(Group group) {
        try{
            SQLiteDatabase db = helper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("group_name", group.getName());
            values.put("group_no", group.getNo());
            values.put("department_name", group.getDepartmentName());
            values.put("group_type", group.getGroupType());
            values.put("response_group_type", group.getResponseGroupType());
            values.put("group_unique_no", group.getUniqueNo());
            db.update(GROUP_DATA, values, "group_id = ?", new String[]{group.id + ""});
        }catch (Exception e){
            logger.error(e.toString());
        }
    }

    @Override
    public CopyOnWriteArrayList<Group> getGroup() {
        try{
            SQLiteDatabase db = helper.getReadableDatabase();
            Cursor cursor = db.query(GROUP_DATA, null, null, null, null, null, null);
            return getGroupList(db, cursor);
        }catch (Exception e){
            logger.error(e.toString());
        }
        return new CopyOnWriteArrayList<Group>();
    }

    private synchronized CopyOnWriteArrayList<Group> getGroupList(SQLiteDatabase db, Cursor cursor) {
        CopyOnWriteArrayList<Group> groupList = new CopyOnWriteArrayList<>();
        try{
            if (cursor != null && cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    Group group = new Group();
                    group.setNo(cursor.getInt(cursor.getColumnIndex("group_no")));
                    group.setId(cursor.getInt(cursor.getColumnIndex("group_id")));
                    group.setName(cursor.getString(cursor.getColumnIndex("group_name")));
                    group.setDepartmentName(cursor.getString(cursor.getColumnIndex("department_name")));
                    group.setGroupType((cursor.getString(cursor.getColumnIndex("group_type"))));
                    group.setResponseGroupType(cursor.getString(cursor.getColumnIndex("response_group_type")));
                    group.setUniqueNo(cursor.getLong(cursor.getColumnIndex("group_unique_no")));
                    groupList.add(group);
                }
                cursor.close();
            }
        }catch (Exception e){
            logger.error(e.toString());
        }
        return groupList;
    }

    @Override
    public void addWarningRecord(WarningRecord warningRecord) {
        try{
            SQLiteDatabase db = helper.getReadableDatabase();
            ContentValues values = getWarningRecordContentValues(warningRecord);
            db.replace(WARNING_RECORD, null, values);
        }catch (Exception e){
            logger.error(e.toString());
        }
    }

    @Override
    public Map<String, Integer> getWarningRecordsNo(int page, int pageSize) {
        try{
            SQLiteDatabase db = helper.getReadableDatabase();
            StringBuffer sql = new StringBuffer("select * from warningRecord ");
            sql.append(" order by alarm_time limit " + pageSize + " offset " + (page - 1) * pageSize);
            Cursor cursor = db.rawQuery(sql.toString(), null);
            //        Cursor cursor = db.query(WARNING_RECORD, new String[]{"alarm_no"}, null, null, null, null, "alarm_time",);
            return getWarningRecordMap(db, cursor);
        }catch (Exception e){
            logger.error(e.toString());
        }
        return  new HashMap<String, Integer>();
    }

    @Override
    public void updateWarningRecord(WarningRecord warningRecord) {
        try{
            SQLiteDatabase db = helper.getWritableDatabase();
            ContentValues values = getWarningRecordContentValues(warningRecord);
            db.update(WARNING_RECORD, values, "alarm_no = ?", new String[]{warningRecord.getAlarmNo() + ""});
        }catch (Exception e){
            logger.error(e.toString());
        }

    }

    @Override
    public WarningRecord getWarningRecordByAlarmNo(String alarmNo) {
        WarningRecord record = null;
        try{
            SQLiteDatabase db = helper.getWritableDatabase();
            Cursor cursor = db.query(WARNING_RECORD, null, "alarm_no=?", new String[]{alarmNo}, null, null, null);

            while (cursor.moveToNext()) {
                record = getWarningRecord(cursor);
            }
            cursor.close();
        }catch (Exception e){
            logger.error(e.toString());
        }
        return record;
    }

    /**
     * @param cursor
     * @return
     */
    private WarningRecord getWarningRecord(Cursor cursor) {
        WarningRecord record = new WarningRecord();
        try{
            record.setAlarmNo(cursor.getString(cursor.getColumnIndex("alarm_no")));
            record.setLevels(cursor.getInt(cursor.getColumnIndex("levels")));
            record.setStatus(cursor.getInt(cursor.getColumnIndex("status")));
            record.setAddress(cursor.getString(cursor.getColumnIndex("address")));
            record.setApersonPhone(cursor.getString(cursor.getColumnIndex("apersonphone")));
            record.setAperson(cursor.getString(cursor.getColumnIndex("aperson")));
            record.setRecvperson(cursor.getString(cursor.getColumnIndex("recvperson")));
            record.setRecvphone(cursor.getString(cursor.getColumnIndex("recvphone")));
            record.setSummary(cursor.getString(cursor.getColumnIndex("summary")));
            record.setAlarmTime(cursor.getString(cursor.getColumnIndex("alarm_time")));
            record.setDate(cursor.getString(cursor.getColumnIndex("date")));
            record.setUnRead(cursor.getInt(cursor.getColumnIndex("unread")));
        }catch (Exception e){
            logger.error(e.toString());
        }
        return record;
    }

    private synchronized List<String> getWarningRecordList(SQLiteDatabase db, Cursor cursor) {
        List<String> warningRecordNoList = new ArrayList<>();
        try{
            if (cursor != null && cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    if (!TextUtils.isEmpty(cursor.getString(cursor.getColumnIndex("alarm_no")))) {
                        warningRecordNoList.add(cursor.getString(cursor.getColumnIndex("alarm_no")));
                    }
                }
                cursor.close();
            }
        }catch (Exception e){
            logger.error(e.toString());
        }

        logger.info("查询本地数据库WarningRecord结果：" + warningRecordNoList);
        return warningRecordNoList;
    }

    private synchronized Map<String, Integer> getWarningRecordMap(SQLiteDatabase db, Cursor cursor) {
        Map<String, Integer> map = new HashMap<>();
        try{
            if (cursor != null && cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    if (!TextUtils.isEmpty(cursor.getString(cursor.getColumnIndex("alarm_no")))) {
                        map.put(cursor.getString(cursor.getColumnIndex("alarm_no")),
                            cursor.getInt(cursor.getColumnIndex("unread")));
                    }
                }
                cursor.close();
            }
        }catch (Exception e){
            logger.error(e.toString());
        }
        logger.info("查询本地数据库getWarningRecordMap结果：" + map);
        return map;
    }

    private ContentValues getWarningRecordContentValues(WarningRecord warningRecord) {
        ContentValues values = new ContentValues();
        try{
            values.put("alarm_no", warningRecord.getAlarmNo());
            if (warningRecord.getStatus() != -1) {
                values.put("status", warningRecord.getStatus());
            }
            if (warningRecord.getLevels() != -1) {
                values.put("levels", warningRecord.getLevels());
            }
            if (!TextUtils.isEmpty(warningRecord.getAlarmTime())) {
                values.put("alarm_time", warningRecord.getAlarmTime());
            }
            if (!TextUtils.isEmpty(warningRecord.getAddress())) {
                values.put("address", warningRecord.getAddress());
            }
            if (!TextUtils.isEmpty(warningRecord.getSummary())) {
                values.put("summary", warningRecord.getSummary());
            }
            if (!TextUtils.isEmpty(warningRecord.getApersonPhone())) {
                values.put("apersonphone", warningRecord.getApersonPhone());
            }
            if (!TextUtils.isEmpty(warningRecord.getAperson())) {
                values.put("aperson", warningRecord.getAperson());
            }
            if (!TextUtils.isEmpty(warningRecord.getRecvperson())) {
                values.put("recvperson", warningRecord.getRecvperson());
            }
            if (!TextUtils.isEmpty(warningRecord.getRecvphone())) {
                values.put("recvphone", warningRecord.getRecvphone());
            }
            if (!TextUtils.isEmpty(warningRecord.getDate())) {
                values.put("date", warningRecord.getDate());
            }
            if (warningRecord.getStatus() != -1) {
                values.put("unread", warningRecord.getUnRead());
            }
        }catch (Exception e){
            logger.error(e.toString());
        }
        return values;
    }

    private synchronized CopyOnWriteArrayList<Folder> getFolderList(SQLiteDatabase db, Cursor cursor) {
        CopyOnWriteArrayList<Folder> folderList;
        Map<Integer, Folder> folderMap = new HashMap<>();
        try{
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
                        } else {
                            Folder folder = setFolder(cursor);
                            folderMap.put(folder.id, folder);
                        }
                    }
                }
                cursor.close();
            }
            //        db.close();

        }catch (Exception e){
            logger.error(e.toString());
        }
        folderList = new CopyOnWriteArrayList<>(folderMap.values());
        return folderList;
    }

    @NonNull
    private Folder setFolder(Cursor cursor) {
        Folder folder = new Folder();
        try{
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
        }catch (Exception e){
            logger.error(e.toString());
        }
        return folder;
    }

    //通话记录
    @Override
    public void addCallRecord(CopyOnWriteArrayList<CallRecord> callRecords) {
        try{
            SQLiteDatabase db = helper.getWritableDatabase();
            for (CallRecord callRecord : callRecords) {
                ContentValues values = new ContentValues();
                values.put("call_id", callRecord.getCallId());
                values.put("member_name", callRecord.getMemberName());
                values.put("call_phone", callRecord.getPhone());
                values.put("call_records", callRecord.getCallRecords());
                values.put("call_time", callRecord.getTime());
                values.put("call_path", callRecord.getPath());
                values.put("call_download", callRecord.isDownLoad() ? "true" : "false");
                values.put("call_playing", callRecord.isPlaying() ? "true" : "false");
                db.replace(CALL_RECORD, null, values);
            }
        }catch (Exception e){
            logger.error(e.toString());
        }

    }

    @Override
    public void deleteCallRecord() {
        try{
            SQLiteDatabase db = helper.getWritableDatabase();
            db.execSQL("DELETE FROM callRecord");
        }catch (Exception e){
            logger.error(e.toString());
        }

    }

    @Override
    public void updateCallRecord(CallRecord callRecord) {
        try{
            SQLiteDatabase db = helper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("call_id", callRecord.getCallId());
            values.put("member_name", callRecord.getMemberName());
            values.put("call_phone", callRecord.getPhone());
            values.put("call_records", callRecord.getCallRecords());
            values.put("call_time", callRecord.getTime());
            values.put("call_path", callRecord.getPath());
            values.put("call_download", callRecord.isDownLoad());
            values.put("call_playing", callRecord.isPlaying());
            db.update(CALL_RECORD, values, "call_id = ?", new String[]{callRecord.callId + ""});
        }catch (Exception e){
            logger.error(e.toString());
        }

    }

    @Override
    public CopyOnWriteArrayList<CallRecord> getCallRecords() {
        try{
            SQLiteDatabase db = helper.getReadableDatabase();
            Cursor cursor = db.query(CALL_RECORD, null, null, null, null, null, null);
            return getCallRecordList(db, cursor);
        }catch (Exception e){
            logger.error(e.toString());
        }
        return new CopyOnWriteArrayList<CallRecord>();
    }

    private synchronized CopyOnWriteArrayList<CallRecord> getCallRecordList(SQLiteDatabase db, Cursor cursor) {
        CopyOnWriteArrayList<CallRecord> callRecordList = new CopyOnWriteArrayList<>();
        try{
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
        }catch (Exception e){
            logger.error(e.toString());
        }
        logger.info("查询本地数据库CallRecord结果：" + callRecordList);
        return callRecordList;
    }

    /**
     * 添加比特星生成的本地文件
     *
     * @param record
     */
    @Override
    public void addBitStarFileRecord(BitStarFileRecord record) {
        try{
            logger.info("addBitStarFileRecord" + record);
            SQLiteDatabase db = helper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("file_duration", record.getDuration());
            values.put("file_width", record.getWidth());
            values.put("file_height", record.getHeight());
            values.put("file_date", record.getDate());
            values.put("file_name", record.getFileName());
            values.put("file_path", record.getFilePath());
            values.put("file_type", record.getFileType());
            values.put("file_time", record.getFileTime());
            values.put("file_state", record.getFileState());
            db.replace(BIT_STAR_FILE_RECORD, null, values);
        }catch (Exception e){
            logger.error(e.toString());
        }

    }

    /**
     * 根据文件名字删除本地文件
     *
     * @param name
     */
    @Override
    public void deleteBitStarFileRecord(String name) {
        try{
            SQLiteDatabase db = helper.getWritableDatabase();
            String sql = "DELETE FROM bitStarFileRecord WHERE file_name = ?";
            db.execSQL(sql, new String[]{"" + name});
        }catch (Exception e){
            logger.error(e.toString());
        }

    }

    /**
     * 一次删除多条数据
     *
     * @param names
     */
    @Override
    public void deleteBitStarFileRecords(String[] names) {
        try{
            SQLiteDatabase db = helper.getWritableDatabase();
            String placeHolder = getPlaceHolder(names.length);
            String sql = "DELETE FROM bitStarFileRecord WHERE file_name in (" + placeHolder + ")";
            db.execSQL(sql, names);
        }catch (Exception e){
            logger.error(e.toString());
        }

    }

    /**
     * 更新文件的上传状态
     *
     * @param name
     * @param fileState
     */
    @Override
    public void updateBitStarFileRecordState(String name, int fileState) {
        try{
            SQLiteDatabase db = helper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("file_state", fileState);
            db.update(BIT_STAR_FILE_RECORD, values, "file_name = ?", new String[]{name});
        }catch (Exception e){
            logger.error(e.toString());
        }
    }

    /**
     * 根据文件名字获取文件信息
     *
     * @param name
     * @return
     */
    @Override
    public BitStarFileRecord getBitStarFileRecord(String name) {
        BitStarFileRecord record = null;
        try{
            SQLiteDatabase db = helper.getWritableDatabase();
            Cursor cursor = db.query(BIT_STAR_FILE_RECORD, null, "file_name=?", new String[]{name}, null, null, null);

            while (cursor.moveToNext()) {
                record = getBitStarFileRecord(cursor);
            }
            cursor.close();
        }catch (Exception e){
            logger.error(e.toString());
        }
        logger.info("查询本地数据库getBitStarFileRecord结果：" + record);
        return record;
    }

    /**
     * 根据多个文件名字获取多个文件信息
     *
     * @param names
     * @return
     */
    @Override
    public CopyOnWriteArrayList<BitStarFileRecord> getBitStarFileRecords(String[] names) {
        CopyOnWriteArrayList<BitStarFileRecord> list = new CopyOnWriteArrayList<>();
        try{
            SQLiteDatabase db = helper.getWritableDatabase();
            String placeHolder = getPlaceHolder(names.length);
            String sql = "SELECT * FROM bitStarFileRecord WHERE file_name in (" + placeHolder + ")";
            Cursor cursor = db.rawQuery(sql, names);
            while (cursor.moveToNext()) {
                list.add(getBitStarFileRecord(cursor));
            }
            cursor.close();
        }catch (Exception e){
            logger.error(e.toString());
        }

        logger.info("查询本地数据库getBitStarFileRecords结果：" + list);
        return list;
    }

    /**
     * 根据文件的状态获取文件的信息列表
     *
     * @return
     */
    @Override
    public CopyOnWriteArrayList<BitStarFileRecord> getBitStarFileRecordByState(int fileState) {
        CopyOnWriteArrayList<BitStarFileRecord> list = new CopyOnWriteArrayList<>();
        try{
            SQLiteDatabase db = helper.getReadableDatabase();
            Cursor cursor = db.query(BIT_STAR_FILE_RECORD, null, "file_state=?", new String[]{fileState + ""}, null, null, "file_time ASC");
            if (cursor != null && cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    list.add(getBitStarFileRecord(cursor));
                }
                cursor.close();
            }
        }catch (Exception e){
            logger.error(e.toString());
        }

        logger.info("查询本地数据库getBitStarFileRecordByState结果：fileState：" + fileState + "--list--" + list);
        return list;
    }

    /**
     * 根据文件的状态获取最早的一条数据
     *
     * @param fileState
     * @return
     */
    @Override
    public BitStarFileRecord getBitStarFileRecordByStateAndFirst(int fileState) {
        BitStarFileRecord record = null;
        try{
            SQLiteDatabase db = helper.getWritableDatabase();
            String sql = "SELECT * FROM bitStarFileRecord WHERE file_state = ?  ORDER BY file_time ASC LIMIT 1";
            Cursor cursor = db.rawQuery(sql, new String[]{fileState + ""});

            while (cursor.moveToNext()) {
                record = getBitStarFileRecord(cursor);
            }
            cursor.close();
        }catch (Exception e){
            logger.error(e.toString());
        }

        logger.info("查询本地数据库getBitStarFileRecordByStateAndFirst结果：" + record);
        return record;
    }

    @Override
    public CopyOnWriteArrayList<BitStarFileRecord> getBitStarFileRecordByAll() {
        CopyOnWriteArrayList<BitStarFileRecord> list = new CopyOnWriteArrayList<>();
        try{
            SQLiteDatabase db = helper.getReadableDatabase();
            Cursor cursor = db.query(BIT_STAR_FILE_RECORD, null, null, null, null, null, null);
            if (cursor != null && cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    list.add(getBitStarFileRecord(cursor));
                }
                cursor.close();
            }
        }catch (Exception e){
            logger.error(e.toString());
        }

        logger.info("查询本地数据库getBitStarFileRecordByAll结果：" + list);
        return list;
    }

    @Override
    public CopyOnWriteArrayList<String> getFileDates(int page, int pageSize) {
        CopyOnWriteArrayList<String> list = new CopyOnWriteArrayList<>();
        try{
            SQLiteDatabase db = helper.getReadableDatabase();
            StringBuffer sql = new StringBuffer("select file_date from bitStarFileRecord GROUP BY file_date ORDER BY file_date desc limit " + pageSize + " offset " + (page - 1) * pageSize);
            Cursor cursor = db.rawQuery(sql.toString(), null);
            if (cursor != null && cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    list.add(cursor.getString(cursor.getColumnIndex("file_date")));
                }
                cursor.close();
            }
        }catch (Exception e){
            logger.error(e.toString());
        }

        return list;
    }

    @Override
    public CopyOnWriteArrayList<BitStarFileRecord> getBitStarFileRecords(String date, String fileType) {
        CopyOnWriteArrayList<BitStarFileRecord> list = new CopyOnWriteArrayList<>();
        try{
            SQLiteDatabase db = helper.getReadableDatabase();
            StringBuffer sql = new StringBuffer("select * from bitStarFileRecord where file_date = ");
            sql.append("\'").append(date).append("\'");
            if (!TextUtils.isEmpty(fileType)) {
                sql.append(" AND file_type = ");
                sql.append("\'").append(fileType).append("\'");
            }
            sql.append(" ORDER BY file_date desc");
            Cursor cursor = db.rawQuery(sql.toString(), null);
            if (cursor != null && cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    list.add(getBitStarFileRecord(cursor));
                }
                cursor.close();
            }
        }catch (Exception e){
            logger.error(e.toString());
        }

        logger.info("查询本地数据库getBitStarFileRecord结果：" + list);
        return list;
    }

    /**
     * 分组分页查询存放的文件
     *
     * @param page     页数，从1开始
     * @param pageSize 每页数据数量
     * @return
     */
    @Override
    public CopyOnWriteArrayList<BitStarFileRecord> getBitStarFileRecords(int page, int pageSize, String fileType) {
        CopyOnWriteArrayList<BitStarFileRecord> list = new CopyOnWriteArrayList<>();
        try{
            SQLiteDatabase db = helper.getReadableDatabase();
            StringBuffer sql = new StringBuffer("select * from bitStarFileRecord ");
            if (!TextUtils.isEmpty(fileType)) {
                sql.append("where file_type = " + fileType);
            }
            sql.append(" GROUP BY file_date " + "ORDER BY file_date desc limit " + pageSize + " offset " + (page - 1) * pageSize);
            Cursor cursor = db.rawQuery(sql.toString(), null);
            if (cursor != null && cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    list.add(getBitStarFileRecord(cursor));
                }
                cursor.close();
            }
        }catch (Exception e){
            logger.error(e.toString());
        }

        logger.info("查询本地数据库getBitStarFileRecord结果：" + list);
        return list;
    }

    /**
     * 获取单个BitStarFileRecord
     *
     * @param cursor
     * @return
     */
    public BitStarFileRecord getBitStarFileRecord(Cursor cursor) {
        BitStarFileRecord record = new BitStarFileRecord();
        try{
            record.setDuration(cursor.getInt(cursor.getColumnIndex("file_duration")));
            record.setWidth(cursor.getInt(cursor.getColumnIndex("file_width")));
            record.setHeight(cursor.getInt(cursor.getColumnIndex("file_height")));
            record.setDate(cursor.getString(cursor.getColumnIndex("file_date")));
            record.setFileName(cursor.getString(cursor.getColumnIndex("file_name")));
            record.setFilePath(cursor.getString(cursor.getColumnIndex("file_path")));
            record.setFileType(cursor.getString(cursor.getColumnIndex("file_type")));
            record.setFileTime(cursor.getLong(cursor.getColumnIndex("file_time")));
            record.setFileState(cursor.getInt(cursor.getColumnIndex("file_state")));
        }catch (Exception e){
            logger.error(e.toString());
        }

        return record;
    }

    /**
     * 获取占位符
     *
     * @param size
     * @return
     */
    private String getPlaceHolder(int size) {
        try{
            if (size < 1) {
                return "";
            } else {
                StringBuilder sb = new StringBuilder(size * 2 - 1);
                sb.append("?");
                for (int i = 1; i < size; i++) {
                    sb.append(",?");
                }
                return sb.toString();
            }
        }catch (Exception e){
            logger.error(e.toString());
        }
        return "";
    }

    @Override
    public void updateAllGroup(List<Group> groups,boolean deleteData) {
        logger.info("保存组数据:" + groups);
        SQLiteDatabase db = null;
        try {
            db = helper.getWritableDatabase();
            //开始事务
            db.beginTransaction();
            if(deleteData){
                db.execSQL("DELETE FROM allGroup");
            }
            for (Group group : groups) {
                ContentValues values = new ContentValues();
                values.put("current_member_id", TerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0));
                values.put("group_id", group.getId());
                values.put("group_no", group.getNo());
                values.put("group_name", group.getName());
                values.put("group_name_py", PinyinUtils.getPingYin(group.getName()));
//                values.put("group_name_first_py",PinyinUtils.converterToFirstSpell(group.getName()));
                values.put("department_name", group.getDepartmentName());
                values.put("temp_group_type", group.getTempGroupType());
                values.put("business_id", group.getBusinessId());
                values.put("group_type", group.getGroupType());
                values.put("unique_no", group.getUniqueNo());
                values.put("dept_id", group.getDeptId());
                values.put("created_member_unique_no", group.getCreatedMemberUniqueNo());
                values.put("response_group_type", group.getResponseGroupType());
                values.put("high_user", group.isHighUser() ? 1 : 0);
                values.put("processing_state", group.getProcessingState());
                values.put("created_member_name", group.getCreatedMemberName());
                values.put("created_member_no", group.getCreatedMemberNo());
                db.replace(ALL_GROUP, null, values);
            }
            db.setTransactionSuccessful();  //设置事务成功完成
        } catch (Exception e) {
            logger.error(e);
        } finally {
            //结束事务
            try{
                if(db!=null){
                    db.endTransaction();
                }
            }catch (Exception e){
                logger.error(e.toString());
            }
        }
    }

    @Override
    public List<GroupSearchBean> getAllGroupFirst() {
        List<GroupSearchBean> groups = new ArrayList<>();
        try{
            SQLiteDatabase db = helper.getReadableDatabase();
            int memberId = TerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0);
            String sql = "SELECT * FROM allGroup WHERE current_member_id = "+memberId+ " LIMIT 1";
            Cursor cursor = db.rawQuery(sql, new String[]{});
            GroupSearchBean group = null;
            while (cursor.moveToNext()) {
                group = new GroupSearchBean();
                group.setId(cursor.getInt(cursor.getColumnIndex("group_id")));
                group.setNo(cursor.getInt(cursor.getColumnIndex("group_no")));
                group.setBusinessId(cursor.getString(cursor.getColumnIndex("business_id")));
                group.setCreatedMemberName(cursor.getString(cursor.getColumnIndex("created_member_name")));
                group.setCreatedMemberNo(cursor.getInt(cursor.getColumnIndex("created_member_no")));
                group.setCreatedMemberUniqueNo(cursor.getLong(cursor.getColumnIndex("created_member_unique_no")));
                group.setDepartmentName(cursor.getString(cursor.getColumnIndex("department_name")));
                group.setDeptId(cursor.getInt(cursor.getColumnIndex("dept_id")));
                group.setGroupType(cursor.getString(cursor.getColumnIndex("group_type")));
                group.setHighUser(cursor.getInt(cursor.getColumnIndex("high_user")) == 1);
                group.setName(cursor.getString(cursor.getColumnIndex("group_name")));
                group.setProcessingState(cursor.getString(cursor.getColumnIndex("processing_state")));
                group.setResponseGroupType(cursor.getString(cursor.getColumnIndex("response_group_type")));
                group.setTempGroupType(cursor.getString(cursor.getColumnIndex("temp_group_type")));
                group.setUniqueNo(cursor.getLong(cursor.getColumnIndex("unique_no")));

                //T9搜索
                //            group.getLabelPinyinSearchUnit().setBaseData(group.getName());
                //            PinyinUtil.parse(group.getLabelPinyinSearchUnit());
                //            String sortKey = PinyinUtil.getSortKey(group.getLabelPinyinSearchUnit()).toUpperCase();
                //            group.setSortKey(praseSortKey(sortKey));
            }
            cursor.close();
            logger.info("查询本地数据库getBitStarFileRecordByStateAndFirst结果：" + group);
            if (group != null) {
                groups.add(group);
            }
        }catch (Exception e){
            logger.error(e.toString());
        }
        return groups;
    }

    /**
     * 根据GroupNo删除Group
     * @param groupNo
     */
    @Override public void deleteGroupByNo(int groupNo) {
        logger.info("删除组数据:" + groupNo);
        SQLiteDatabase db = null;
        try {
            db = helper.getWritableDatabase();
            db.delete(ALL_GROUP,"current_member_id = ? AND group_no = ?",
                new String[]{TerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0)+"",groupNo + ""});
        } catch (Exception e) {
            logger.error(e);
        } finally {
            //结束事务
            try{
                if(db!=null){
                    db.close();
                }
            }catch (Exception e){
                logger.error(e.toString());
            }
        }
    }

    @Override
    public void deleteAllGroup() {
        try{
            SQLiteDatabase db = helper.getWritableDatabase();
            db.execSQL("DELETE FROM allGroup");
        }catch (Exception e){
            logger.error(e.toString());
        }
    }

    @Override
    public synchronized List<GroupSearchBean> getAllGroup(List<GroupSearchBean> groups, int index) {
        logger.info("分页查询 group index:" + index);
        long start = System.currentTimeMillis();
        int cursorSize = 0;

        try {
            SQLiteDatabase db = helper.getReadableDatabase();
            Cursor cursor = db.query(ALL_GROUP, null, "current_member_id = ?", new String[]{TerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0) + ""}, null, null, "group_no ASC LIMIT " + pageSize + " offset " + index);
//            Cursor cursor = db.query(ALL_GROUP, null, null,null, null, null, "group_no ASC LIMIT " + pageSize + " offset " + index);
            if (cursor != null && cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    index++;
                    cursorSize++;
                    GroupSearchBean group = new GroupSearchBean();
                    group.setId(cursor.getInt(cursor.getColumnIndex("group_id")));
                    group.setNo(cursor.getInt(cursor.getColumnIndex("group_no")));
                    group.setBusinessId(cursor.getString(cursor.getColumnIndex("business_id")));
                    group.setCreatedMemberName(cursor.getString(cursor.getColumnIndex("created_member_name")));
                    group.setCreatedMemberNo(cursor.getInt(cursor.getColumnIndex("created_member_no")));
                    group.setCreatedMemberUniqueNo(cursor.getLong(cursor.getColumnIndex("created_member_unique_no")));
                    group.setDepartmentName(cursor.getString(cursor.getColumnIndex("department_name")));
                    group.setDeptId(cursor.getInt(cursor.getColumnIndex("dept_id")));
                    group.setGroupType(cursor.getString(cursor.getColumnIndex("group_type")));
                    group.setHighUser(cursor.getInt(cursor.getColumnIndex("high_user")) == 1);
                    group.setName(cursor.getString(cursor.getColumnIndex("group_name")));
                    group.setProcessingState(cursor.getString(cursor.getColumnIndex("processing_state")));
                    group.setResponseGroupType(cursor.getString(cursor.getColumnIndex("response_group_type")));
                    group.setTempGroupType(cursor.getString(cursor.getColumnIndex("temp_group_type")));
                    group.setUniqueNo(cursor.getLong(cursor.getColumnIndex("unique_no")));
                    group.setCurrentMemberId(cursor.getInt(cursor.getColumnIndex("current_member_id")));

                    //T9搜索
                    group.getLabelPinyinSearchUnit().setBaseData(group.getName()+ group.getNo());
                    PinyinUtil.parse(group.getLabelPinyinSearchUnit());
                    String sortKey = PinyinUtil.getSortKey(group.getLabelPinyinSearchUnit()).toUpperCase();
                    group.setSortKey(praseSortKey(sortKey));

                    groups.add(group);
                }
                cursor.close();
                long end = System.currentTimeMillis();
                logger.info("获取数据库数据所耗时间getAllGroup：" + (end - start));
            }
        } catch (Exception e) {
            logger.error(e.toString());
        }

        if (cursorSize >= pageSize) {
            getAllGroup(groups, index);
        }
        return groups;
    }

    @Override
    public GroupSearchBean getGroupByNo(int groupNo) {
        GroupSearchBean group = null;
        try {
            SQLiteDatabase db = helper.getReadableDatabase();
            Cursor cursor = db.query(ALL_GROUP, null, "group_no = ?", new String[]{groupNo + ""}, null, null, null);
            if (cursor != null && cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    group = new GroupSearchBean();
                    group.setId(cursor.getInt(cursor.getColumnIndex("group_id")));
                    group.setNo(cursor.getInt(cursor.getColumnIndex("group_no")));
                    group.setBusinessId(cursor.getString(cursor.getColumnIndex("business_id")));
                    group.setCreatedMemberName(cursor.getString(cursor.getColumnIndex("created_member_name")));
                    group.setCreatedMemberNo(cursor.getInt(cursor.getColumnIndex("created_member_no")));
                    group.setCreatedMemberUniqueNo(cursor.getLong(cursor.getColumnIndex("created_member_unique_no")));
                    group.setDepartmentName(cursor.getString(cursor.getColumnIndex("department_name")));
                    group.setDeptId(cursor.getInt(cursor.getColumnIndex("dept_id")));
                    group.setGroupType(cursor.getString(cursor.getColumnIndex("group_type")));
                    group.setHighUser(cursor.getInt(cursor.getColumnIndex("high_user")) == 1);
                    group.setName(cursor.getString(cursor.getColumnIndex("group_name")));
                    group.setProcessingState(cursor.getString(cursor.getColumnIndex("processing_state")));
                    group.setResponseGroupType(cursor.getString(cursor.getColumnIndex("response_group_type")));
                    group.setTempGroupType(cursor.getString(cursor.getColumnIndex("temp_group_type")));
                    group.setUniqueNo(cursor.getLong(cursor.getColumnIndex("unique_no")));

                    //T9搜索
                    group.getLabelPinyinSearchUnit().setBaseData(group.getName()+ group.getNo());
                    PinyinUtil.parse(group.getLabelPinyinSearchUnit());
                    String sortKey = PinyinUtil.getSortKey(group.getLabelPinyinSearchUnit()).toUpperCase();
                    group.setSortKey(praseSortKey(sortKey));
                }
                cursor.close();

            }
        } catch (Exception e) {
            logger.error(e.toString());
        }
        logger.info("getGroupByNo--groupNo:" + groupNo+"--group:"+group);
        return group;
    }

    @Override
    public void updateAllAccount(List<Account> accounts) {
        logger.info("保存账号数据:" + accounts);
        SQLiteDatabase db =null;
        try {
            db = helper.getWritableDatabase();
            //开始事务
            db.beginTransaction();
            db.execSQL("DELETE FROM allAccount");
            for (Account account : accounts) {
                ContentValues values = new ContentValues();
                values.put("current_member_id", TerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0));
                values.put("account_id", account.getId());
                values.put("account_no", account.getNo());
                values.put("account_name", account.getName());
                values.put("name_pinyin", PinyinUtils.getPingYin(account.getName()));
//                values.put("name_first_pinyin", PinyinUtils.converterToFirstSpell(account.getName()));
                values.put("account_phone", getPhone(account));
                values.put("dept_id", account.getDeptId());
                values.put("members", MyGsonUtil.list2String(true, account.getMembers()));
                values.put("department_name", account.getDepartmentName());
                values.put("update_time", 0);//默认记录当前时间为 0
                db.replace(ALL_ACCOUNT, null, values);
            }
            db.setTransactionSuccessful();  //设置事务成功完成
        } catch (Exception e) {
            logger.error(e);
        } finally {
            //结束事务
            try{
                if(db!=null){
                    db.endTransaction();
                }
            }catch (Exception e){
                logger.error(e.toString());
            }
        }
    }

    private String getPhone(Account account){
        try{

            String phone = account.getPhone();
            String phoneNumber = account.getPhoneNumber();

            if(!TextUtils.isEmpty(phone)){
                return phone;
            }
            if(!TextUtils.isEmpty(phoneNumber)){
                return phoneNumber;
            }

            for (Member member : account.getMembers()){
                String phone1 = member.getPhone();
                if(TextUtils.isEmpty(phone1)){
                    return phone1;
                }
            }
        }catch (Exception e){
            logger.error(e.toString());
        }

        return "";
    }



    @Override
    public List<MemberSearchBean> getAllAccountFirst() {
        List<MemberSearchBean> memberList = new ArrayList<>();
        try{
            SQLiteDatabase db = helper.getReadableDatabase();
            int memberId = TerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0);
            String sql = "SELECT * FROM allAccount WHERE current_member_id = "+memberId+ " LIMIT 1";
            Cursor cursor = db.rawQuery(sql, new String[]{});
            MemberSearchBean account = null;
            while (cursor.moveToNext()) {
                account = new MemberSearchBean();
                account.setId(cursor.getInt(cursor.getColumnIndex("account_id")));
                account.setNo(cursor.getInt(cursor.getColumnIndex("account_no")));
                account.setName(cursor.getString(cursor.getColumnIndex("account_name")));
                account.setPhone(cursor.getString(cursor.getColumnIndex("account_phone")));
                account.setDeptId(cursor.getInt(cursor.getColumnIndex("dept_id")));
                account.setDepartmentName(cursor.getString(cursor.getColumnIndex("department_name")));
                String members = cursor.getString(cursor.getColumnIndex("members"));
                account.setMembers(MyGsonUtil.getList(true, members, new ArrayList<>(), Member.class));
                account.setUpdateTime(cursor.getLong(cursor.getColumnIndex("update_time")));

                account.getLabelPinyinSearchUnit().setBaseData(account.getName() + account.getNo());
                PinyinUtil.parse(account.getLabelPinyinSearchUnit());
                String sortKey = PinyinUtil.getSortKey(account.getLabelPinyinSearchUnit()).toUpperCase();
                account.setSortKey(praseSortKey(sortKey));
            }
            cursor.close();
            logger.info("查询本地数据库getAllAccountFirst结果：" + account);
            if (account != null) {
                memberList.add(account);
            }
        }catch (Exception e){
            logger.error(e.toString());
        }

        return memberList;
    }

    @Override
    public void deleteAllAccount() {
        try{
            SQLiteDatabase db = helper.getWritableDatabase();
            db.execSQL("DELETE FROM allAccount");
        }catch (Exception e){
            logger.error(e.toString());
        }
    }

    @Override public void updateAccountUseTime(int accountNo) {
        logger.info("记录此联系人的 使用时间:" + accountNo);
        SQLiteDatabase db = null;
        try {
            db = helper.getWritableDatabase();
            //开始事务
            db.beginTransaction();

            ContentValues cv = new ContentValues();
            cv.put("update_time", System.currentTimeMillis());
            String[] args = {accountNo + ""};
            db.update(ALL_ACCOUNT, cv, "account_no=?", args);
            db.setTransactionSuccessful();  //设置事务成功完成
        } catch (Exception e) {
            logger.error(e);
        } finally {
            //结束事务
            try{
                if(db!=null){
                    db.endTransaction();
                }
            }catch (Exception e){
                logger.error(e.toString());
            }
        }
    }

    @Override public List<MemberSearchBean> getTop5ContactsAccount() {
        List<MemberSearchBean> memberList = new ArrayList<>();
        try{
            SQLiteDatabase db = helper.getReadableDatabase();
            String sql = "SELECT * FROM allAccount WHERE update_time<>0 ORDER BY update_time desc LIMIT 5 ";
            Cursor cursor = db.rawQuery(sql, new String[]{});

            while (cursor.moveToNext()) {
                MemberSearchBean account = null;
                account = new MemberSearchBean();
                account.setId(cursor.getInt(cursor.getColumnIndex("account_id")));
                account.setNo(cursor.getInt(cursor.getColumnIndex("account_no")));
                account.setName(cursor.getString(cursor.getColumnIndex("account_name")));
                account.setPhone(cursor.getString(cursor.getColumnIndex("account_phone")));
                account.setDeptId(cursor.getInt(cursor.getColumnIndex("dept_id")));
                account.setDepartmentName(cursor.getString(cursor.getColumnIndex("department_name")));
                String members = cursor.getString(cursor.getColumnIndex("members"));
                account.setMembers(MyGsonUtil.getList(true, members, new ArrayList<>(), Member.class));
                //记录时间
                account.setUpdateTime(cursor.getLong(cursor.getColumnIndex("update_time")));
//            account.getLabelPinyinSearchUnit().setBaseData(account.getName() + account.getNo());
//            PinyinUtil.parse(account.getLabelPinyinSearchUnit());
//            String sortKey = PinyinUtil.getSortKey(account.getLabelPinyinSearchUnit()).toUpperCase();
//            account.setSortKey(praseSortKey(sortKey));

                if (account != null) {
                    memberList.add(account);
                }
            }
            cursor.close();
        }catch (Exception e){
            e.printStackTrace();
        }
        logger.info("常用联系人前5条查询结果：" + memberList);
        return memberList;
    }

    @Override public Long addBindDevice(TianjinDeviceBean device) {
        return null;
    }

    @Override public List<TianjinDeviceBean> getTop5TianjinDevice(String type) {
        return null;
    }

    private int pageSize = 500;

    @Override
    public List<MemberSearchBean> getAllAccount(List<MemberSearchBean> accounts, int index) {
//        logger.info("分页查询 Account index:" + index);
//        long start = System.currentTimeMillis();
        int cursorSize = 0;
        try {
            SQLiteDatabase db = helper.getReadableDatabase();
            Cursor cursor = db.query(ALL_ACCOUNT, null, "current_member_id = ?", new String[]{TerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0) + ""}, null, null, "account_no ASC LIMIT " + pageSize + " offset " + index);
            if (cursor != null && cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    index++;
                    cursorSize++;
                    MemberSearchBean account = new MemberSearchBean();
                    account.setId(cursor.getInt(cursor.getColumnIndex("account_id")));
                    account.setNo(cursor.getInt(cursor.getColumnIndex("account_no")));
                    account.setName(cursor.getString(cursor.getColumnIndex("account_name")));
                    account.setPhone(cursor.getString(cursor.getColumnIndex("account_phone")));
                    account.setDeptId(cursor.getInt(cursor.getColumnIndex("dept_id")));
                    account.setDepartmentName(cursor.getString(cursor.getColumnIndex("department_name")));
                    String members = cursor.getString(cursor.getColumnIndex("members"));
                    account.setMembers(MyGsonUtil.getList(true, members, new ArrayList<>(), Member.class));

                    account.setUpdateTime(cursor.getLong(cursor.getColumnIndex("update_time")));

                    String no = handleId(account.getNo());//去掉88 86

                    String phone = account.getPhone();

                    account.getLabelPinyinSearchUnit().setBaseData(account.getName() + no + phone);
                    PinyinUtil.parse(account.getLabelPinyinSearchUnit());
                    String sortKey = PinyinUtil.getSortKey(account.getLabelPinyinSearchUnit()).toUpperCase();
                    account.setSortKey(praseSortKey(sortKey));

                    accounts.add(account);
                }
                cursor.close();

//                long end = System.currentTimeMillis();
//                logger.info("获取数据库数据所耗时间getAllAccount：" + (end - start));
            }
        } catch (Exception e) {
            logger.error(e.toString());
        }

        if (cursorSize >= pageSize) {
            getAllAccount(accounts, index);
        }

        return accounts;
    }

    public static String handleId(int memberId) {
        String account = "";
        try{
            String s = memberId + "";

            if (!Util.isEmpty(s) && s.length() > 2 && ("88".equals(s.substring(0, 2)) || "86".equals(s.substring(0, 2)) || "87".equals(s.substring(0, 2)))) {
                account = s.substring(2);
            } else {
                account = s;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return account;
    }


    private static String praseSortKey(String sortKey) {
        try{
            if (null == sortKey || sortKey.length() <= 0) {
                return null;
            }
            if ((sortKey.charAt(0) >= 'a' && sortKey.charAt(0) <= 'z') || (sortKey.charAt(0) >= 'A' && sortKey.charAt(0) <= 'Z')) {
                return sortKey;
            }
            return String.valueOf(/*QuickAlphabeticBar.DEFAULT_INDEX_CHARACTER*/'#')
                + sortKey;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 存储视频会商消息
     * @param message
     */
    @Override public synchronized void addVideoMeetingMessage(VideoMeetingMessage message) {
        logger.info("向VideoMeetingMessage表存消息：" + message);
        SQLiteDatabase db = null;
        try {
        VideoMeetingMessage meetingMessage = getMeetingVideoMeetingMessageByRoomId(message.getRoomId());
         db = helper.getWritableDatabase();
         db.replace(VIDEO_MEETING_MESSAGE, null, getAddVideoMeetingMessageContentValues(message,meetingMessage));
        }catch (Exception e){
            logger.error(e.toString());
        } finally {
            try{
                if(db!=null){
                    db.close();
                }
            }catch (Exception e){
                logger.error(e.toString());
            }
        }
    }

    /**
     * 更新视频会商的消息
     * @param messages
     */
    @Override public synchronized void updateVideoMeetingMessageList(CopyOnWriteArrayList<VideoMeetingMessage> messages) {
        logger.info("向VideoMeetingMessage表更新消息size：" + messages.size());
        SQLiteDatabase db = null;
        try{
            db = helper.getWritableDatabase();
            for (VideoMeetingMessage meetingMessage:messages){
                if(meetingMessage!=null){
                    ContentValues values  =  getAddVideoMeetingMessageContentValues(meetingMessage,null);
                    db.update(VIDEO_MEETING_MESSAGE, values, "current_member_id = ? AND room_id = ?", new String[]{TerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0) + "",meetingMessage.getRoomId()+""});
                }
            }
        }catch (Exception e){
            logger.error(e.toString());
        } finally {
            try{
                if(db!=null){
                    db.close();
                }
            }catch (Exception e){
                logger.error(e.toString());
            }
        }
    }

    @Override public synchronized void removeVideoMeetingMessageByRoomId(long roomId) {
        logger.error("删除roomId" + roomId + "消息");
        SQLiteDatabase db =null;
        try{
            db = helper.getWritableDatabase();
            db.delete(VIDEO_MEETING_MESSAGE,"current_member_id = ? AND room_id = ?", new String[]{TerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0) + "",roomId+""});
        }catch (Exception e){
            logger.error(e.toString());
        } finally {
            try{
                if(db!=null){
                    db.close();
                }
            }catch (Exception e){
                logger.error(e.toString());
            }
        }
    }

    @Override
    public synchronized void updateVideoMeetingMessageList(List<VideoMeetingDataBean> data) {
        logger.info("向VideoMeetingMessage表更新消息size：" + data.size());
        SQLiteDatabase db = null;
        try{
            db = helper.getWritableDatabase();
            for (VideoMeetingDataBean bean:data){
                if(bean!=null){
                    ContentValues values  =  getUpdateVideoMeetingMessageContentValues(bean);
                    db.update(VIDEO_MEETING_MESSAGE, values, "current_member_id = ? AND room_id = ?", new String[]{TerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0) + "",bean.getId()+""});
                }
            }
        }catch (Exception e){
            logger.error(e.toString());
        } finally {
            try{
                if(db!=null){
                    db.close();
                }
            }catch (Exception e){
                logger.error(e.toString());
            }
        }
    }

    @Override public synchronized void updateVideoMeetingMessageListToNoMeeting(List<Long> data) {
        logger.info("向VideoMeetingMessage表更新消息到结束状态size：" + data.size());
        SQLiteDatabase db = null;
        try{
            db = helper.getWritableDatabase();
            for (Long roomId:data){
                ContentValues values  =  getUpdateVideoMeetingMessageToNoNeetingContentValues(false);
                db.update(VIDEO_MEETING_MESSAGE, values, "current_member_id = ? AND room_id = ?", new String[]{TerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0) + "",roomId+""});
            }
        }catch (Exception e){
            logger.error(e.toString());
        } finally {
            try{
                if(db!=null){
                    db.close();
                }
            }catch (Exception e){
                logger.error(e.toString());
            }
        }
    }

    /**
     * 获取正在进行的视频会商
     * @return
     */
    @Override public synchronized CopyOnWriteArrayList<VideoMeetingMessage> getMeetingVideoMeetingMessage() {
        CopyOnWriteArrayList<VideoMeetingMessage> list = new CopyOnWriteArrayList<>();
        SQLiteDatabase db = null;
        try{
            db = helper.getWritableDatabase();
            Cursor cursor = db.query(VIDEO_MEETING_MESSAGE, null, "current_member_id = ? AND is_meeting = ?", new String[]{TerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0) + "","1"}, null, null, null);
            list.clear();
            list.addAll(getVideoMeetingMessageList(cursor));
        }catch (Exception e){
            logger.error(e.toString());
        }finally {
            try{
                if(db!=null){
                    db.close();
                }
            }catch (Exception e){
                logger.error(e.toString());
            }
        }
        logger.info("向VideoMeetingMessage表获取正在进行的会议消息size："+list.size());
        return list;
    }

    @Override public synchronized CopyOnWriteArrayList<VideoMeetingMessage> getAllVideoMeetingMessage() {
        CopyOnWriteArrayList<VideoMeetingMessage> list = new CopyOnWriteArrayList<>();
        SQLiteDatabase db = null;
        try{
            db = helper.getWritableDatabase();
            Cursor cursor = db.query(VIDEO_MEETING_MESSAGE, null, "current_member_id = ? ", new String[]{TerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0) + ""}, null, null, "send_time");
            list.clear();
            list.addAll(getVideoMeetingMessageList(cursor));
        }catch (Exception e){
            logger.error(e.toString());
        }finally {
            try{
                if(db!=null){
                    db.close();
                }
            }catch (Exception e){
                logger.error(e.toString());
            }
        }
        logger.info("向VideoMeetingMessage表获取所以会议消息size："+list.size());
        return list;
    }

    @Override public synchronized VideoMeetingMessage getMeetingVideoMeetingMessageByRoomId(long roomId) {
        CopyOnWriteArrayList<VideoMeetingMessage> list = new CopyOnWriteArrayList<>();
        SQLiteDatabase db = null;
        try{
            db = helper.getWritableDatabase();
            Cursor cursor = db.query(VIDEO_MEETING_MESSAGE, null, "current_member_id = ? AND room_id = ?", new String[]{TerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0) + "",roomId+""}, null, null, null);
            list.clear();
            list.addAll(getVideoMeetingMessageList(cursor));
        }catch (Exception e){
            logger.error(e.toString());
        }finally {
            try{
                if(db!=null){
                    db.close();
                }
            }catch (Exception e){
                logger.error(e.toString());
            }
        }
        logger.info("向VideoMeetingMessage表获取会议消息roomId："+roomId);
        return (list.isEmpty())?null:list.get(0);
    }

    @Override
    public synchronized CopyOnWriteArrayList<VideoMeetingMessage> getVideoMeetingMessageBySendTime(long index,int pageLimit) {
        CopyOnWriteArrayList<VideoMeetingMessage> list = new CopyOnWriteArrayList<>();
        SQLiteDatabase db = null;
        try{
            db = helper.getWritableDatabase();
            Cursor cursor;
            String sql = "SELECT * FROM videoMeetingMessage WHERE current_member_id = ? ORDER BY is_meeting DESC ,send_time DESC LIMIT "+index+","+pageLimit;
            //                cursor = db.query(TABLE_TERMINAL_MESSAGE, null,"current_member_id = ? and message_to_id = ? and message_category = ?",new String[]{TerminalFactory.getSDK().getParam(Params.MEMBER_ID,0)+"",targetId + "", "2"},null,null,"send_time DESC","0,10");
            cursor = db.rawQuery(sql, new String[]{TerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0) + ""});
            //if (sendTime == 0) {
            //    String sql = "SELECT * FROM videoMeetingMessage WHERE current_member_id = ? ORDER BY is_meeting DESC ,send_time DESC LIMIT 0,"+pageLimit;
            //    //                cursor = db.query(TABLE_TERMINAL_MESSAGE, null,"current_member_id = ? and message_to_id = ? and message_category = ?",new String[]{TerminalFactory.getSDK().getParam(Params.MEMBER_ID,0)+"",targetId + "", "2"},null,null,"send_time DESC","0,10");
            //    cursor = db.rawQuery(sql, new String[]{TerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0) + ""});
            //} else {
            //    String sql = "SELECT * FROM videoMeetingMessage WHERE current_member_id = ? AND send_time <= ? ORDER BY is_meeting DESC ,send_time DESC LIMIT 0,"+pageLimit;
            //    cursor = db.rawQuery(sql, new String[]{TerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0) + "", sendTime + ""});
            //}
            list.clear();
            list.addAll(getVideoMeetingMessageList(cursor));
        }catch (Exception e){
            logger.error(e.toString());
        }finally {
            try{
                if(db!=null){
                    db.close();
                }
            }catch (Exception e){
                logger.error(e.toString());
            }
        }
        logger.info("向VideoMeetingMessage表获取会议消息size："+list.size()+"-index:"+index);
        return list;
    }

    @Override public synchronized CopyOnWriteArrayList<VideoMeetingMessage> getVideoMeetingMessageLast() {
        CopyOnWriteArrayList<VideoMeetingMessage> list = new CopyOnWriteArrayList<>();
        SQLiteDatabase db = null;
        try{
            db = helper.getWritableDatabase();
            String sql = "SELECT * FROM videoMeetingMessage WHERE current_member_id = ? ORDER BY send_time DESC LIMIT 0,1";
            Cursor cursor = db.rawQuery(sql, new String[]{TerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0) + ""});
            list.clear();
            list.addAll(getVideoMeetingMessageList(cursor));
        }catch (Exception e){
            logger.error(e.toString());
        }finally {
            try{
                if(db!=null){
                    db.close();
                }
            }catch (Exception e){
                logger.error(e.toString());
            }
        }
        logger.info("向VideoMeetingMessage表获取收到最新的会议消息size："+list.size());
        return list;
    }

    /**
     * 获取添加或者更新单个视频会商消息的ContentValues
     * @param message
     * @return
     */
    private ContentValues getAddVideoMeetingMessageContentValues(VideoMeetingMessage message,VideoMeetingMessage isAddedMessage){
        ContentValues values = new ContentValues();
        try{
            if(message!=null){
                values.put("current_member_id", TerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0));
                values.put("room_id", message.getRoomId());
                values.put("create_terminal_unqieno", message.getCreateTerminalUnqieno());
                values.put("create_terminal_no", message.getCreateTerminalNo());
                values.put("create_terminal_name", message.getCreateTerminalName());
                values.put("add_or_out_meeting", (message.isAddOrOutMeeting()?1:0));
                values.put("meeting_describe", message.getMeetingDescribe());
                //判断是否是已经收到过邀请的通知
                if(isAddedMessage!=null){
                    values.put("send_time",isAddedMessage.getSendTime());
                }else{
                    //没有添加过，要判断是否是邀请的通知，
                    values.put("send_time",message.getSendTime());
                }
                values.put("is_meeting", (message.isMeeting()?1:0));
            }
        }catch (Exception e){
            logger.error(e.toString());
        }
        return values;
    }

    /**
     * 获取添加或者更新单个视频会商消息的ContentValues
     * @param bean
     * @return
     */
    private ContentValues getUpdateVideoMeetingMessageContentValues(VideoMeetingDataBean bean){
        ContentValues values = new ContentValues();
        try{
            if(bean!=null){
                values.put("meeting_describe", JSONObject.toJSON(bean).toString());
                values.put("is_meeting", (bean.getStatus()==2?0:1));
            }
        }catch (Exception e){
            logger.error(e.toString());
        }

        return values;
    }

    /**
     * 获取添加或者更新单个视频会商消息到结束会议的状态的ContentValues
     * @param isMeeting
     * @return
     */
    private ContentValues getUpdateVideoMeetingMessageToNoNeetingContentValues(boolean isMeeting){
        ContentValues values = new ContentValues();
        try{
            values.put("is_meeting", (isMeeting?1:0));
        }catch (Exception e){
            logger.error(e.toString());
        }
        return values;
    }

    /**
     * 获取视频会商消息
     * @param cursor
     * @return
     */
    private synchronized CopyOnWriteArrayList<VideoMeetingMessage> getVideoMeetingMessageList(Cursor cursor) {
        CopyOnWriteArrayList<VideoMeetingMessage> list = new CopyOnWriteArrayList<>();
        try {
            if (cursor != null && cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    VideoMeetingMessage message = new VideoMeetingMessage();
                    message.setRoomId(cursor.getLong(cursor.getColumnIndex("room_id")));
                    message.setCreateTerminalUnqieno(cursor.getLong(cursor.getColumnIndex("create_terminal_unqieno")));
                    message.setCreateTerminalNo(cursor.getInt(cursor.getColumnIndex("create_terminal_no")));
                    message.setCreateTerminalName(cursor.getString(cursor.getColumnIndex("create_terminal_name")));
                    message.setAddOrOutMeeting(cursor.getInt(cursor.getColumnIndex("add_or_out_meeting"))==1);
                    message.setMeetingDescribe(cursor.getString(cursor.getColumnIndex("meeting_describe")));
                    message.setSendTime(cursor.getLong(cursor.getColumnIndex("send_time")));
                    message.setMeeting(cursor.getInt(cursor.getColumnIndex("is_meeting"))==1);
                    list.add(message);
                }
            }
        } catch (Exception e) {
            logger.error(e.toString());
        }
        return list;
    }
}
