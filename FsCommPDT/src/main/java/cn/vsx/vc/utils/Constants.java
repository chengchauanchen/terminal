package cn.vsx.vc.utils;

/**
 * 作者：xuxiaolong
 * 版本：1.0
 * 创建日期：2018/1/22
 * 描述：常量类
 * 修订历史：
 */

public class Constants{


    public static final String MEMBER_DATA="member_data";
    public static final String CATALOG_DATA="catalog_data";
    public static final String GROUP_DATA="group_data";
    public static final String GROUP_CATALOG_DATA="group_catalog_data";
    public static final String LAWRECODER = "rtsp://192.168.0.100/live";
//    public static final String LAWRECODER = "rtsp://192.168.1.100:10555/10000220_2428640747588420616.sdp";
    //测试url
//    public static final String LAWRECODER = "rtsp://184.72.239.149/vod/mp4://BigBuckBunny_175k.mov";
//    public final static int TYPE_TEMP_FOLDER = 98;

    public final static int TYPE_TEMP_TITLE = 98;
    public final static int TYPE_TITLE = 99;
    public final static int TYPE_FOLDER=100;
    public final static int TYPE_GROUP=101;
    public final static int TYPE_TEMP_GROUP=102;
    public final static int TYPE_DEPARTMENT=103;
    public final static int TYPE_USER=104;
    public final static int TYPE_ACCOUNT=105;
    public final static int TYPE_FREQUENT=106;
    public final static int TYPE_LTE=107;
    public final static int TYPE_RECORDER=108;
    public final static int TYPE_TERMINAL=109;
    //搜索 通讯录的组
    public final static int TYPE_CONTRACT_GROUP=107;
    //搜索 通讯录的成员
    public final static int TYPE_CONTRACT_MEMBER=108;
    //搜索 通讯录的PDT
    public final static int TYPE_CONTRACT_PDT=109;
    public final static int TYPE_CONTRACT_TERMINAL=11;
    //搜索 通讯录的LTE
    public final static int TYPE_CONTRACT_LTE=117;
    //搜索 通讯录的recorder
    public final static int TYPE_CONTRACT_RECORDER=119;
    //搜索 勾选组
    public final static int TYPE_CHECK_SEARCH_GROUP=110;
    public final static int TYPE_CHECK_SEARCH_POLICE=111;
    public final static int TYPE_CHECK_SEARCH_HDMI=112;
    public final static int TYPE_CHECK_SEARCH_RECODER=113;
    public final static int TYPE_CHECK_SEARCH_UAV=114;
    public final static int TYPE_CHECK_SEARCH_PC=115;
    public final static int TYPE_CHECK_SEARCH_ACCOUNT=116;
    public final static int TYPE_CHECK_SEARCH_LTE=118;
    public final static int TYPE_CHECK_SEARCH_BUTTON_GROUP=120;

    public final static String TYPE_GROUP_STRING = "groupType";

    public final static int FORCE_KILL = 0;
    public final static int LOGINED = 1;

    public final static String ACTIVE_CALL = "activeCall";
    public final static String RECEIVE_CALL = "receiveCall";

    public static final String SYSTEM_DIALOG_REASON_KEY = "reason";
    public static final String SYSTEM_DIALOG_REASON_HOME_KEY = "homekey";
    public static final String SYSTEM_DIALOG_REASON_RECENT_APPS = "recentapps";

    public static final String MEMBER_NAME = "memberName";
    public static final String MEMBER_ID = "memberId";
    public static final String PUSH_MEMBERS = "push_members";

    public static final String HYTERA = "PDC760";

    public static final String PUSH = "push";
    public static final String PULL = "pull";

    public static final String PHONE_PUSH = "phonePush";
    public static final String UVC_PUSH = "uvcPush";
    public static final String RECODER_PUSH = "recoderPush";

    public static final String CALL_TYPE = "callType";
    public static final String TYPE = "type";
    public static final String WATCHING_MEMBERS = "watchMembers";
    public static final String PUSHING = "pushing";
    public static final String LIVING_MEMBER_ID = "livingMemberId";
    public static final String PULLING = "pulling";
    public static final String GB28181_PULL = "gb28181Pull";
    public static final String THEME = "theme";
    public static final String ACTIVE_PUSH = "activePush";
    public static final String RECEIVE_PUSH = "receivePush";
    public static final String RTSP_URL = "rtspUrl";
    public static final String LIVE_MEMBER = "liveMember";

    public static final String WATCH_TYPE = "watch_Type";
    public static final String ACTIVE_WATCH = "activeWatch";
    public static final String RECEIVE_WATCH = "receiveWatch";
    public static final String TERMINALMESSAGE = "terminalMessage";

    public static final String CAMERA_TYPE = "cameraType";
    public static final String UVC_CAMERA = "uvcCamera";
    public static final String RECODER_CAMERA = "recoderCamera";
    public static final String ORIENTATION = "orientation";

    public static final String FINISH_TRANSPARENT = "FINISH_TRANSPARENT";


    public static final String USER_NAME = "userName";
    public static final String SECOND_GROUP_ID = "secondGroupId";

    public static String OLD_CURRENT_GROUP = "oldCurrentGroup";

    //是否是组内正在上报的列表
    public static String IS_GROUP_VIDEO_LIVING = "isGroupVideoLiving";
    public static String IS_GROUP_PUSH_LIVING = "isGroupPushLiving";
    public static String GROUP_ID = "groupId";

    public static String IS_GROUP = "isGroup";
    public static String USER_ID = "userId";
    public static String UNIQUE_NO = "uniqueNo";

    public static String DEVICE_TYPE = "deviceType";
    public static String BUSINESS_TYPE = "businessType";

    public static String SCAN_DATA = "scanData";
    public static String TRANSPON_SELECTED_BEAN = "transponSelectedBean";

    public static String TRANSPON_TYPE = "transponType";
    public static int TRANSPON_TYPE_ONE = 1;
    public static int TRANSPON_TYPE_MORE = 2;

    public static String INVITE_MEMBER_EXCEPT_UNIQUE_NO = "inviteMemberExceptUniqueNo";

    public static int CREATE_TEMP_GROUP = 0;
    public static int INCREASE_MEMBER = 1;
   //是否是强制类型
    public static final String EMERGENCY_TYPE = "emergencyType";

    //天津从统一认证客户端获取票据的URI
    public static final String AUTH_TIAN_JIN_TOKEN_URI = "content://com.xdja.app.pj/cn.vsx.vc";

    public static final String PTT_DOWN_EVENT = "com.chivin.action.MEDIA_PTT_DOWN";
    public static final String PTT_UP_EVENT = "com.chivin.action.MEDIA_PTT_UP";

    //视频会商的会议id
    public static final String ROOM_ID = "roomId";
    //视频会商的类型
    public static final String VIDEO_MEETING_TYPE = "videoMeetingType";

    //视频会商的组No
    public static final String VIDEO_MEETING_GROUP_NO = "videoMeetingGroupNo";

    public static final String VIDEO_MEETING_URL = "videoMeetingUrl";
    //视频会议的消息
    public static final String VIDEO_MEETING_MESSAGE = "videoMeetingMessage";

    //跳转到注册页面
    public static final String GO_TO_REGIST_TYPE = "goToRegistType";
    public static final int GO_TO_REGIST_TYPE_DEFLAT = 0;
    public static final int GO_TO_REGIST_TYPE_CLEAR = 1;
}
