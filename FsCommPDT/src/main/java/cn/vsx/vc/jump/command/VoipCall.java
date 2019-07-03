package cn.vsx.vc.jump.command;

import android.content.Context;
import android.content.Intent;

import cn.vsx.hamster.terminalsdk.model.Member;
import cn.vsx.hamster.terminalsdk.tools.Params;
import cn.vsx.vc.R;
import cn.vsx.vc.activity.VoipPhoneActivity;
import cn.vsx.vc.jump.bean.SendBean;
import cn.vsx.vc.jump.constant.CommandEnum;
import ptt.terminalsdk.context.MyTerminalFactory;
import ptt.terminalsdk.tools.ToastUtil;

/**
 * 个人会话
 */
public class VoipCall extends BaseCommand {

    public VoipCall(Context context) {
        super(context);
    }

    @Override
    public CommandEnum getCommandType(CommandEnum commandType) {
        return CommandEnum.PersonChat;
    }

    @Override
    protected void jumpPage(SendBean sendBean) {
        call(sendBean.getMemberNo());
    }

    private void call(String memberNo) {
        if (MyTerminalFactory.getSDK().getParam(Params.VOIP_SUCCESS, false)) {
            Intent intent = new Intent(context, VoipPhoneActivity.class);
            Member member = new Member(Integer.valueOf(memberNo), memberNo + "");
            intent.putExtra("member", member);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } else {
            ToastUtil.showToast(context, context.getString(R.string.text_voip_regist_fail_please_check_server_configure));
        }
    }
}
