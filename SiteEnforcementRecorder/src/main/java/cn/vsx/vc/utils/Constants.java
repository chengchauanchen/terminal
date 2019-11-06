package cn.vsx.vc.utils;

/**
 * 作者：xuxiaolong
 * 版本：1.0
 * 创建日期：2018/1/22
 * 描述：常量类
 * 修订历史：
 */

public class Constants {


    public static final String MEMBER_DATA="member_data";
    public static final String CATALOG_DATA="catalog_data";
    public static final String GROUP_DATA="group_data";
    public static final String GROUP_CATALOG_DATA="group_catalog_data";
    public static final String LAWRECODER = "rtsp://192.168.0.100/layout_live";
//    public static final String LAWRECODER = "rtsp://192.168.1.100:10555/10000236_4104013701587779760.sdp";
    //测试url
//    public static final String LAWRECODER = "rtsp://184.72.239.149/vod/mp4://BigBuckBunny_175k.mov";
//    public final static int TYPE_TEMP_FOLDER = 98;

    public final static int TYPE_TEMP_TITLE = 98;
    public final static int TYPE_TITLE = 99;
    public final static int TYPE_FOLDER=100;
    public final static int TYPE_GROUP=101;
    public final static int TYPE_DEPARTMENT=102;
    public final static int TYPE_USER=103;
    public final static int TYPE_TEMP_GROUP=104;

    public final static int FORCE_KILL = 0;
    public final static int LOGINED = 1;

    public final static int APP_RUN_STATE_NO = 0;
    public final static int APP_RUN_STATE_BACKGROUND = 1;
    public final static int APP_RUN_STATE_FOREGROUND = 2;

    public final static String PTTEVEVT_ACTION = "ptt_event_action";
    public static final String PTTEVEVT_ACTION_DOWN = "android.intent.action.PPTEVEVT_ACTION_DOWN";
    public static final String PTTEVEVT_ACTION_UP = "android.intent.action.PPTEVEVT_ACTION_UP";


    public static final int LOGIN_BIND_STATE_IDLE = 0;//未登录
    public static final int LOGIN_BIND_STATE_LOGIN = 1;//已登录
    public static final int LOGIN_BIND_STATE_BIND = 2;//已绑定

    public static final String FRAGMENT_TAG_MENU = "menuFragment";//menuFragment
    public static final String FRAGMENT_TAG_BIND = "bindFragment";//bindFragment
    public static final String FRAGMENT_TAG_NFC = "nfcFragment";//nfcFragment
    public static final String FRAGMENT_TAG_QR = "qrScanFragment";//qrScanFragment
    public static final String FRAGMENT_TAG_INPUT = "inputPoliceIdFragment";//inputPoliceIdFragment
    public static final String FRAGMENT_TAG_SET = "setFragment";//setFragment
    public static final String FRAGMENT_TAG_SET_SERVER = "setServerFragment";//setServerFragment
    public static final String FRAGMENT_TAG_GROUP_CHANGE = "groupChangeFragment";//groupChangeFragment
    public static final String FRAGMENT_TAG_GROUP_SEARCH = "groupSearchFragment";//groupSearchFragment
    public static final String FRAGMENT_TAG_SET_LIVING_STOP_TIME = "setLivingStopTimeFragment";//setLivingStopTimeFragment
    public static final String FRAGMENT_TAG_INFRA_RED = "infraRedFragment";//infraRedFragment
    public static final String FRAGMENT_TAG_APP_LIST = "appListFragment";//appListFragment

    public static final long LIVING_STOP_DELAY_TIME = 2*60*1000;//上报停止延时时间


}
