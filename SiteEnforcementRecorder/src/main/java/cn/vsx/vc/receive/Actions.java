package cn.vsx.vc.receive;

/**
 * Created by jamie on 2017/10/17.
 * 广播动作
 */

public interface Actions {
    public static final String ACT_SHOW_FULL_SCREEN = "act_show_full_screen";
    public static final String ACT_DISMISS_FULL_SCREEN = "act_dismiss_full_screen";
    public static final String CALL_COMING_NAME = "call_coming_name";
    public static final String CALL_REFUSE_TO_ANSWER = "call_refuse_to_answer";
    public static final String CALL_STOPPED = "call_stopped";
    public static final String STOP_INDIVDUALCALL_SERVEIC = "stop_indivdualcall_service";
    public static final String KILL_ACT_CALL = "kill_act_call";
    public static final String SEND_LIVE_THEME = "send_live_theme";
    public static final String HIDE_KEY = "hide_key";
}
