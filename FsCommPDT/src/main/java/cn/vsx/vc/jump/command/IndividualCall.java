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
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveCurrentGroupIndividualCallHandler;
import cn.vsx.hamster.terminalsdk.tools.DataUtil;
import cn.vsx.vc.R;
import cn.vsx.vc.jump.constant.CommandEnum;
import cn.vsx.vc.jump.constant.ParamKey;
import ptt.terminalsdk.context.MyTerminalFactory;

/**
 * 发起个呼
 */
public class IndividualCall extends BaseCommand implements IJumpCommand {

    public IndividualCall(Context context) {
        super(context);
    }

    @Override
    public CommandEnum getCommandType(CommandEnum commandType) {
        return CommandEnum.IndividualCall;
    }


    @Override
    public void jumpPage(Map<Object, Object> map) {
        int memberNo = (int) map.get(ParamKey.MEMBER_NO);
//        int terminalType = (int) map.get(ParamKey.TERMINAL_TYPE);
        activeIndividualCall(memberNo);
    }

    /**
     * 发起个呼
     *
     * @param memberNo
     */
    public static void activeIndividualCall(int memberNo) {
        activeIndividualCall(memberNo, TerminalMemberType.TERMINAL_PHONE.getCode());
    }

    /**
     * 发起个呼
     *
     * @param memberNo
     * @param type     终端类型 1：手机   6 PC
     */
    public static void activeIndividualCall(int memberNo, int type) {
        int memberNum = checkMemberNo(memberNo);
        TerminalFactory.getSDK().getThreadPool().execute(() -> {
            Account account = DataUtil.getAccountByMemberNo(memberNum, true);
            if (account != null) {
                if (!account.getMembers().isEmpty()) {
                    List<Member> members = account.getMembers();
                    for (Member member : members) {
                        if (type == member.getType()) {
                            activeIndividualCall(member);
                            break;
                        }
                    }
                }
            }
        });
    }


    /**
     * 如果memberNo为6位，默认加"88"
     * @param memberNo
     * @return
     */
    private static int checkMemberNo(int memberNo){
        String memberNoStr = memberNo+"";
        if(length(memberNo)<=6){
            memberNoStr = "88"+memberNoStr;
        }else{
            return memberNo;
        }
        return Integer.parseInt(memberNoStr);
    }


    public static int length(int number) {
        int length = (number + " ").length();
        return length;
    }


    /**
     * 个呼
     *
     * @param member
     */
    private static void activeIndividualCall(Member member) {
        boolean network = MyTerminalFactory.getSDK().hasNetwork();
        if(network){
            OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiveCurrentGroupIndividualCallHandler.class, member);
        }else{
            ToastUtils.showShort(R.string.text_network_connection_abnormal_please_check_the_network);
        }
    }
}
