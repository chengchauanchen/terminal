package cn.vsx.vc.receive;

/**
 * Created by jamie on 2017/10/17.
 * 广播动作
 */

public interface Actions {
    String ACT_SHOW_FULL_SCREEN = "act_show_full_screen";
    String ACT_DISMISS_FULL_SCREEN = "act_dismiss_full_screen";
    String CALL_COMING_NAME = "call_coming_name";
    String CALL_REFUSE_TO_ANSWER = "call_refuse_to_answer";
    String CALL_STOPPED = "call_stopped";
    String STOP_INDIVDUALCALL_SERVEIC = "stop_indivdualcall_service";
    String KILL_ACT_CALL = "kill_act_call";
    String SEND_LIVE_THEME = "send_live_theme";
    String HIDE_KEY = "hide_key";
}
