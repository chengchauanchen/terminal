package cn.vsx.vc.jump.command;

import android.content.Context;

import com.blankj.utilcode.util.ToastUtils;
import com.zectec.imageandfileselector.utils.OperateReceiveHandlerUtilSync;

import java.util.List;
import java.util.Map;

import cn.vsx.hamster.common.TerminalMemberType;
import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.model.Account;
import cn.vsx.hamster.terminalsdk.model.Member;
import cn.vsx.hamster.terminalsdk.tools.DataUtil;
import cn.vsx.vc.R;
import cn.vsx.vc.jump.constant.CommandEnum;
import cn.vsx.vc.jump.constant.ParamKey;
import cn.vsx.vc.receiveHandle.ReceiverRequestVideoHandler;
import ptt.terminalsdk.context.MyTerminalFactory;

/**
 * 请求别人上报
 */
public class OtherLive extends BaseCommand implements IJumpCommand {

    public OtherLive(Context context) {
        super(context);
    }

    @Override
    public CommandEnum getCommandType(CommandEnum commandType) {
        return CommandEnum.OtherLive;
    }

    @Override
    public void jumpPage(Map<Object, Object> map) {
        int memberNo = (int) map.get(ParamKey.MEMBER_NO);
//        int terminalType = (int) map.get(ParamKey.TERMINAL_TYPE);
        requestOtherLive(memberNo);
    }

    /**
     * 请求别人上报
     *
     * @param memberNo
     */
    private static void requestOtherLive(int memberNo) {
        requestOtherLive(memberNo, TerminalMemberType.TERMINAL_PHONE.getCode());
    }

    /**
     * 请求别人上报
     *
     * @param memberNo
     * @param type     终端类型 1：手机   6 PC
     */
    private static void requestOtherLive(int memberNo, int type) {
        boolean network = MyTerminalFactory.getSDK().hasNetwork();
        if (network) {
            TerminalFactory.getSDK().getThreadPool().execute(() -> {
                Account account = DataUtil.getAccountByMemberNo(memberNo, true);
                if (account != null) {
                    if (!account.getMembers().isEmpty()) {
                        List<Member> members = account.getMembers();
                        for (Member member : members) {
                            if (type == member.getType()) {
                                OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiverRequestVideoHandler.class, members.get(0));
                            }
                        }
                    }
                }
            });

        } else {
            ToastUtils.showShort(R.string.text_network_connection_abnormal_please_check_the_network);
        }
    }
}
