package cn.vsx.vc.jump;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.zectec.imageandfileselector.utils.OperateReceiveHandlerUtilSync;

import java.util.List;

import cn.vsx.hamster.errcode.BaseCommonCode;
import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.model.Account;
import cn.vsx.hamster.terminalsdk.model.Member;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveCurrentGroupIndividualCallHandler;
import cn.vsx.hamster.terminalsdk.tools.DataUtil;
import cn.vsx.vc.receiveHandle.ReceiverActivePushVideoHandler;
import cn.vsx.vc.receiveHandle.ReceiverRequestVideoHandler;
import cn.vsx.vc.utils.ToastUtil;
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
     *
     * @param context
     * @param memberNos
     */
    private static void activeStartLive(Context context, List<Integer> memberNos){
        boolean network = MyTerminalFactory.getSDK().hasNetwork();
        if(network){

            OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiverActivePushVideoHandler.class,"",false);
        }else{
            ToastUtil.showToast(context, "网络连接异常，请检查网络！");
        }
    }

    /**
     * 请求别人上报
     *
     * @param context
     * @param memberNo
     */
    private static void requestOtherLive(Context context, int memberNo){
        boolean network = MyTerminalFactory.getSDK().hasNetwork();
        if(network){
            TerminalFactory.getSDK().getThreadPool().execute(() -> {
                Account account = DataUtil.getAccountByMemberNo(memberNo, true);
                if(account != null){
                    if(!account.getMembers().isEmpty()){
                        List<Member> members = account.getMembers();
                        if(!members.isEmpty()){
                            OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiverRequestVideoHandler.class,members.get(0));
                        }
                    }
                }
            });

        }else{
            ToastUtil.showToast(context, "网络连接异常，请检查网络！");
        }
    }

    /**
     * 个呼
     *
     * @param context
     * @param member
     */
    public static void activeIndividualCall(Context context, Member member){
        boolean network = MyTerminalFactory.getSDK().hasNetwork();
        if(network){
            int resultCode = MyTerminalFactory.getSDK().getIndividualCallManager().requestIndividualCall(member.getNo(),member.getUniqueNo(),"");
            if(resultCode == BaseCommonCode.SUCCESS_CODE){
                OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiveCurrentGroupIndividualCallHandler.class, member);
            }else{
                ToastUtil.individualCallFailToast(context, resultCode);
            }
        }else{
            ToastUtil.showToast(context, "网络连接异常，请检查网络！");
        }
    }
}
