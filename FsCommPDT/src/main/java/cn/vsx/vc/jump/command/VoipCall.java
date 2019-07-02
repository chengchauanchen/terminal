package cn.vsx.vc.jump.command;

import android.content.Context;
import android.content.Intent;

import java.util.Map;

import cn.vsx.hamster.terminalsdk.model.Member;
import cn.vsx.hamster.terminalsdk.tools.Params;
import cn.vsx.vc.R;
import cn.vsx.vc.activity.VoipPhoneActivity;
import cn.vsx.vc.jump.constant.CommandEnum;
import cn.vsx.vc.jump.constant.ParamKey;
import ptt.terminalsdk.context.MyTerminalFactory;
import ptt.terminalsdk.tools.ToastUtil;

/**
 * 个人会话
 */
public class VoipCall extends BaseCommand implements IJumpCommand {

    public VoipCall(Context context) {
        super(context);
    }

    @Override
    public CommandEnum getCommandType(CommandEnum commandType) {
        return CommandEnum.PersonChat;
    }


    @Override
    public void jumpPage(Map<Object, Object> map) {
        String memberNo = (String) map.get(ParamKey.PHONE_NO);
        String appkey = (String) map.get(ParamKey.APP_KEY);
        call(memberNo);
    }

    private void call(String memberNo){
        if(MyTerminalFactory.getSDK().getParam(Params.VOIP_SUCCESS, false)){
            Intent intent = new Intent(context, VoipPhoneActivity.class);
            Member member = new Member(Integer.valueOf(memberNo),memberNo+"");
            intent.putExtra("member", member);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }else{
            ToastUtil.showToast(context, context.getString(R.string.text_voip_regist_fail_please_check_server_configure));
        }
    }
}
