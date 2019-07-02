package cn.vsx.vc.jump;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.blankj.utilcode.util.ToastUtils;
import com.zectec.imageandfileselector.utils.OperateReceiveHandlerUtilSync;

import java.util.List;

import cn.vsx.hamster.common.ReceiveObjectMode;
import cn.vsx.hamster.common.TerminalMemberType;
import cn.vsx.hamster.errcode.BaseCommonCode;
import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.model.Account;
import cn.vsx.hamster.terminalsdk.model.Group;
import cn.vsx.hamster.terminalsdk.model.Member;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveCurrentGroupIndividualCallHandler;
import cn.vsx.hamster.terminalsdk.tools.DataUtil;
import cn.vsx.vc.R;
import cn.vsx.vc.activity.GroupCallNewsActivity;
import cn.vsx.vc.activity.IndividualNewsActivity;
import cn.vsx.vc.receiveHandle.ReceiverActivePushVideoHandler;
import cn.vsx.vc.receiveHandle.ReceiverRequestVideoHandler;
import cn.vsx.vc.utils.MyDataUtil;
import ptt.terminalsdk.context.MyTerminalFactory;

/**
 * 作者：ly-xuxiaolong
 * 版本：1.0
 * 创建日期：2019/7/1
 * 描述：
 * 修订历史：
 */
public class JumpService extends Service{

    @Nullable
    @Override
    public IBinder onBind(Intent intent){
        return null;
    }

    /**
     * 自己上报，邀请别人来观看
     */
    private static void activeStartLive(int memberNo){
        activeStartLive(memberNo,TerminalMemberType.TERMINAL_PHONE.getCode());
    }

    /**
     * 自己上报，邀请别人来观看
     * @param type 终端类型 1：手机   6 PC
     */
    private static void activeStartLive(int memberNo,int type){
        boolean network = MyTerminalFactory.getSDK().hasNetwork();
        if(network){
            TerminalFactory.getSDK().getThreadPool().execute(() ->{
                //根据no查询uniqueNo
                Account account = DataUtil.getAccountByMemberNo(memberNo, true);
                if(account != null){
                    if(!account.getMembers().isEmpty()){
                        List<Member> members = account.getMembers();
                        for(Member member : members){
                            if(member.getType() == type){
                                OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiverActivePushVideoHandler.class,
                                        MyDataUtil.getPushInviteMemberData(member.getUniqueNo(), ReceiveObjectMode.MEMBER.toString()),false);
                                break;
                            }
                        }
                    }
                }

            });
        }else{
            ToastUtils.showShort(R.string.text_network_connection_abnormal_please_check_the_network);
        }
    }

    /**
     * 请求别人上报
     * @param memberNo
     */
    private static void requestOtherLive(int memberNo){
        requestOtherLive(memberNo, TerminalMemberType.TERMINAL_PHONE.getCode());
    }

    /**
     * 请求别人上报
     * @param memberNo
     * @param type 终端类型 1：手机   6 PC
     */
    private static void requestOtherLive(int memberNo,int type){
        boolean network = MyTerminalFactory.getSDK().hasNetwork();
        if(network){
            TerminalFactory.getSDK().getThreadPool().execute(() -> {
                Account account = DataUtil.getAccountByMemberNo(memberNo, true);
                if(account != null){
                    if(!account.getMembers().isEmpty()){
                        List<Member> members = account.getMembers();
                        for(Member member : members){
                            if(type ==member.getType()){
                                OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiverRequestVideoHandler.class,members.get(0));
                            }
                        }
                    }
                }
            });

        }else{
            ToastUtils.showShort(R.string.text_network_connection_abnormal_please_check_the_network);
        }
    }

    /**
     * 发起个呼
     * @param memberNo
     */
    public static void activeIndividualCall(int memberNo){
        activeIndividualCall(memberNo,TerminalMemberType.TERMINAL_PHONE.getCode());
    }

    /**
     * 发起个呼
     * @param memberNo
     * @param type 终端类型 1：手机   6 PC
     */
    public static void activeIndividualCall(int memberNo,int type){
        TerminalFactory.getSDK().getThreadPool().execute(() -> {
            Account account = DataUtil.getAccountByMemberNo(memberNo, true);
            if(account != null){
                if(!account.getMembers().isEmpty()){
                    List<Member> members = account.getMembers();
                    for(Member member : members){
                        if(type == member.getType()){
                            activeIndividualCall(member);
                            break;
                        }
                    }
                }
            }
        });
    }

    /**
     * 个呼
     * @param member
     */
    private static void activeIndividualCall( Member member){
        boolean network = MyTerminalFactory.getSDK().hasNetwork();
        if(network){
            int resultCode = MyTerminalFactory.getSDK().getIndividualCallManager().requestIndividualCall(member.getNo(),member.getUniqueNo(),"");
            if(resultCode == BaseCommonCode.SUCCESS_CODE){
                OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiveCurrentGroupIndividualCallHandler.class, member);
            }else{
                ToastUtils.showShort(resultCode);
            }
        }else{
            ToastUtils.showShort(R.string.text_network_connection_abnormal_please_check_the_network);
        }
    }

    /**
     * 跳转到个人会话
     * @param memberNo
     */
    public void jumpPersonChatActivity(int memberNo){
        TerminalFactory.getSDK().getThreadPool().execute(() -> {
            Account account = DataUtil.getAccountByMemberNo(memberNo, true);
            if(account != null){
                IndividualNewsActivity.startCurrentActivity(getApplicationContext(),account.getNo(),account.getName(),true,0);
            }
        });
    }

    /**
     * 跳转到组会话
     * @param groupNo
     */
    public void jumpGroupChatActivity(int groupNo){
        Group group = TerminalFactory.getSDK().getGroupByGroupNo(groupNo);
        GroupCallNewsActivity.startCurrentActivity(getApplicationContext(),groupNo,group.getName(),0,"",true);
    }
}
