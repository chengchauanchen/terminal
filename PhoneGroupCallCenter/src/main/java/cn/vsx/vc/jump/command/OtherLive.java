package cn.vsx.vc.jump.command;

import android.content.Context;

import com.blankj.utilcode.util.ToastUtils;
import com.zectec.imageandfileselector.utils.OperateReceiveHandlerUtilSync;

import java.util.List;

import cn.vsx.hamster.common.TerminalMemberType;
import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.model.Account;
import cn.vsx.hamster.terminalsdk.model.Member;
import cn.vsx.hamster.terminalsdk.tools.DataUtil;
import cn.vsx.vc.R;
import cn.vsx.vc.jump.bean.SendBean;
import cn.vsx.vc.jump.constant.CommandEnum;
import cn.vsx.vc.jump.utils.AppKeyUtils;
import cn.vsx.vc.jump.utils.MemberUtil;
import cn.vsx.vc.receiveHandle.ReceiverRequestVideoHandler;
import ptt.terminalsdk.context.MyTerminalFactory;

/**
 * 请求别人上报
 */
public class OtherLive extends BaseCommand {

    public OtherLive(Context context) {
        super(context);
    }

    @Override
    public CommandEnum getCommandType(CommandEnum commandType) {
        return CommandEnum.OtherLive;
    }

    @Override
    protected void jumpPage(SendBean sendBean) {
        int memberNo = MemberUtil.strToInt(sendBean.getMemberNo());
        int terminalType = sendBean.getTerminalType();

        if (terminalType == -1) {
            requestOtherLive(memberNo);
        } else {
            requestOtherLive(memberNo, terminalType);
        }
    }

    /**
     * 请求别人上报
     *
     * @param memberNo
     */
    private void requestOtherLive(int memberNo) {
        requestOtherLive(memberNo, TerminalMemberType.TERMINAL_PHONE.getCode());
    }

    /**
     * 请求别人上报
     *
     * @param memberNo
     * @param type     终端类型 1：手机   6 PC
     */
    private void requestOtherLive(int memberNo, int type) {
        boolean network = MyTerminalFactory.getSDK().hasNetwork();
        if (network) {
            TerminalFactory.getSDK().getThreadPool().execute(() -> {
                boolean isTypeEquals = false;//判断是否进入的个呼方法，没进入需要干掉AppKey
                Account account = DataUtil.getAccountByMemberNo(memberNo, true);
                if (account != null && !account.getMembers().isEmpty()) {
                    List<Member> members = account.getMembers();
                    for (Member member : members) {
                        if (type == member.getType()) {
                            isTypeEquals = true;
                            OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiverRequestVideoHandler.class, members.get(0));
                        }
                    }
                    if (!isTypeEquals) {
                        AppKeyUtils.setAppKey(null);
                    }
                } else {
                    AppKeyUtils.setAppKey(null);
                }
            });

        } else {
            AppKeyUtils.setAppKey(null);
            ToastUtils.showShort(R.string.text_network_connection_abnormal_please_check_the_network);
        }
    }
}
