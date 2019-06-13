package cn.vsx.vc.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.zectec.imageandfileselector.utils.OperateReceiveHandlerUtilSync;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import cn.vsx.hamster.common.UrlParams;
import cn.vsx.hamster.errcode.BaseCommonCode;
import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.model.Account;
import cn.vsx.hamster.terminalsdk.model.Member;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveCurrentGroupIndividualCallHandler;
import cn.vsx.hamster.terminalsdk.tools.DataUtil;
import cn.vsx.vc.activity.RegistActivity;
import cn.vsx.vc.application.MyApplication;
import cn.vsx.vc.receiveHandle.ReceiverActivePushVideoHandler;
import cn.vsx.vc.receiveHandle.ReceiverRequestVideoHandler;
import ptt.terminalsdk.context.MyTerminalFactory;

/**
 * @author martian on 2018/11/20.
 */
public class JumpManager{

    public static final String TAG = "JumpManager";

    public static boolean isJump(Intent intent){
        if(intent != null){
            String jumpKey = intent.getStringExtra(UrlParams.JUMP_KEY);
            return (jumpKey != null);
        }
        return false;
    }

    public static Context getContext(){
        return MyApplication.instance.getApplicationContext();
    }

    public static void jump(Intent intent){
        if(intent != null){
            String jumpKey = intent.getStringExtra(UrlParams.JUMP_KEY);
            String jumpValue = intent.getStringExtra(UrlParams.JUMP_VALUE);
            jump(jumpKey, jumpValue);
        }
    }

    /**
     * 检查跳转的流程
     *
     * @param uri
     * @param isLaunched
     */
    public static void checkJump(Uri uri, boolean isLaunched){
        Context context = getContext();
        if(uri != null){
            String key = uri.getQueryParameter(UrlParams.JUMP_KEY);
            String value = uri.getQueryParameter(UrlParams.JUMP_VALUE);
            if(isLaunched){
                jump(key, value);
            }else{
                jumpToLaunch(context, key, value);
            }
        }
    }

    public static void jump(String key, String value){
        Context context = getContext();
        Log.e(TAG, "key:" + key + "------value:" + value);
        if(key != null){
            switch(key){
                case UrlParams.JUMP_KEY_PRIVATE_CALL://个呼
                    if(!TextUtils.isEmpty(value)){
                        TerminalFactory.getSDK().getThreadPool().execute(()->{
                            Account account = DataUtil.getAccountByMemberNo(StringUtil.stringToInt(value),true);
                            if(account != null && !account.getMembers().isEmpty()){
                                activeIndividualCall(context, account.getMembers().get(0));
                            }
                        });
                    }
                    break;
                case UrlParams.JUMP_KEY_REQUEST_LIVE://请求别人上报
                    if(!TextUtils.isEmpty(value)){
                        requestOtherLive(context, StringUtil.stringToInt(value));
                    }
                    break;
                case UrlParams.JUMP_KEY_ACTIVE_LIVE://自己发起上报
                    if(!TextUtils.isEmpty(value)){
                        List<Integer> memberNos = new ArrayList<>();
                        try{
                            JSONArray jsonArray = new JSONArray(value);
                            for(int i = 0; i < jsonArray.length(); i++){
                                memberNos.add((Integer) jsonArray.get(i));
                            }
                        }catch(JSONException e){
                            e.printStackTrace();
                        }
                        activeStartLive(context, memberNos);
                    }
                    break;
                default:
                    break;
            }
        }
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

    public static void jumpToLaunch(Context context, String key, String value){
        Intent intentLaunch = new Intent(context, RegistActivity.class);
        intentLaunch.putExtra(UrlParams.JUMP_KEY, key);
        intentLaunch.putExtra(UrlParams.JUMP_VALUE, value);
        context.startActivity(intentLaunch);
    }
}
