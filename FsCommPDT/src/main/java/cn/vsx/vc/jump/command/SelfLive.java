package cn.vsx.vc.jump.command;

import android.content.Context;

import com.blankj.utilcode.util.ToastUtils;
import com.zectec.imageandfileselector.utils.OperateReceiveHandlerUtilSync;

import java.util.List;

import cn.vsx.hamster.common.ReceiveObjectMode;
import cn.vsx.hamster.common.TerminalMemberType;
import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.model.Account;
import cn.vsx.hamster.terminalsdk.model.Member;
import cn.vsx.hamster.terminalsdk.tools.DataUtil;
import cn.vsx.vc.R;
import cn.vsx.vc.jump.bean.SendBean;
import cn.vsx.vc.jump.constant.CommandEnum;
import cn.vsx.vc.jump.utils.MemberUtil;
import cn.vsx.vc.receiveHandle.ReceiverActivePushVideoHandler;
import cn.vsx.vc.utils.MyDataUtil;
import ptt.terminalsdk.context.MyTerminalFactory;

/**
 * 自己上报，邀请别人来观看
 */
public class SelfLive extends BaseCommand {

    public SelfLive(Context context) {
        super(context);
    }

    @Override
    public CommandEnum getCommandType(CommandEnum commandType) {
        return CommandEnum.SelfLive;
    }

    @Override
    protected void jumpPage(SendBean sendBean) {
        int memberNo=MemberUtil.strToInt(sendBean.getMemberNo());
        int terminalType=sendBean.getTerminalType();

        if(terminalType==-1){
            if(memberNo==0){
                activeStartLive();
            }else {
                activeStartLive(memberNo);
            }
        }else {
            if(memberNo==0){
                activeStartLive();
            }else {
                activeStartLive(memberNo,terminalType);
            }
        }
    }

    /**
     * 发起上报，跳到邀请界面
     */
    private static void activeStartLive(){
        OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiverActivePushVideoHandler.class,"",false);
    }



    /**
     * 自己上报，邀请别人来观看
     */
    private  void activeStartLive(int memberNo) {
        activeStartLive(memberNo, TerminalMemberType.TERMINAL_PHONE.getCode());
    }

    /**
     * 自己上报，邀请别人来观看
     *
     * @param type 终端类型 1：手机   6 PC
     */
    private void activeStartLive(int memberNo, int type) {
        boolean network = MyTerminalFactory.getSDK().hasNetwork();
        if (network) {
            TerminalFactory.getSDK().getThreadPool().execute(() -> {
                //根据no查询uniqueNo
                Account account = DataUtil.getAccountByMemberNo(memberNo, true);
                if (account != null) {
                    if (!account.getMembers().isEmpty()) {
                        List<Member> members = account.getMembers();
                        for (Member member : members) {
                            if (member.getType() == type) {
                                OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiverActivePushVideoHandler.class,
                                        MyDataUtil.getPushInviteMemberData(member.getUniqueNo(), ReceiveObjectMode.MEMBER.toString()), false);
                                break;
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
