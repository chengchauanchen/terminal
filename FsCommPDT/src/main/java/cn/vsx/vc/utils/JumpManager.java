package cn.vsx.vc.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.blankj.utilcode.util.ToastUtils;
import com.zectec.imageandfileselector.utils.OperateReceiveHandlerUtilSync;

import java.util.ArrayList;
import java.util.List;

import cn.vsx.hamster.common.ReceiveObjectMode;
import cn.vsx.hamster.common.UrlParams;
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
import cn.vsx.vc.activity.RegistActivity;
import cn.vsx.vc.application.MyApplication;
import cn.vsx.vc.receiveHandle.ReceiverActivePushVideoHandler;
import cn.vsx.vc.receiveHandle.ReceiverRequestVideoHandler;
import ptt.terminalsdk.context.MyTerminalFactory;
import ptt.terminalsdk.tools.StringUtil;

import static cn.vsx.hamster.common.UrlParams.*;


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
            String appKey = intent.getStringExtra(UrlParams.APP_KEY);
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
            String appkey = uri.getQueryParameter(UrlParams.APP_KEY);

            if(isLaunched){
                jump(key, value);
            }else{
                jumpToLaunch(context, key, value,appkey);
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
                        JSONObject jsonObject = JSONObject.parseObject(value);
                        List<Integer> members = new ArrayList<>();
                        if(jsonObject.containsKey("memberNos")){
                            JSONArray memberNos = jsonObject.getJSONArray("memberNos");
                            for(int i = 0; i < memberNos.size(); i++){
                                Integer memberNo = (Integer) memberNos.get(i);
                                members.add(memberNo);
                            }
                        }
                        List<Integer> groups = new ArrayList<>();
                        if(jsonObject.containsKey("groupNos")){
                            JSONArray groupNos = jsonObject.getJSONArray("groupNos");
                            for(int i = 0; i < groupNos.size(); i++){
                                Integer memberNo = (Integer) groupNos.get(i);
                                groups.add(memberNo);
                            }
                        }
                        
                        TerminalFactory.getSDK().getThreadPool().execute(()->{
                            List<String> result = new ArrayList<>();
                            if(!members.isEmpty()){
                                List<Account> accounts = DataUtil.getAccountsByMemberNos(members);
                                for(Account account : accounts){
                                    result.add(MyDataUtil.getPushInviteMemberData(account.getMembers().get(0).getUniqueNo(), ReceiveObjectMode.MEMBER.toString()));
                                }
                            }
                            if(!groups.isEmpty()){
                                for(Integer groupNo : groups){
                                    result.add(MyDataUtil.getPushInviteMemberData(groupNo, ReceiveObjectMode.GROUP.toString()));
                                }
                            }
                            activeStartLive(context,result);
                        });
                        
                    }
                    break;
                case UrlParams.JUMP_GROUP_CHAT:
                    if(!TextUtils.isEmpty(value)){
                        jumpGroupChatActivity(context,Integer.valueOf(value));
                    }
                    break;
                case UrlParams.JUMP_PERSON_CHAT:
                    if(!TextUtils.isEmpty(value)){
                        jumpPersonChatActivity(context,Integer.valueOf(value));
                    }
                    break;
                case JUMP_GROUP_CALL:
                    if(!TextUtils.isEmpty(value)){
                        startGroupCall(context,Integer.valueOf(value));
                    }
                    break;
                case JUMP_CEASE_GROUP_CALL:
                    ceaseGroupCall();
                    break;
                case JUMP_VOIP_CALL:
                    if(!TextUtils.isEmpty(value)){
                        callVoip(context,Integer.valueOf(value));
                    }
                    break;
                default:
                    break;
            }
        }
    }

    private static void callVoip(Context context, Integer phoneNo){
        
    }

    private static void ceaseGroupCall(){
        MyTerminalFactory.getSDK().getGroupCallManager().ceaseGroupCall();
    }

    private static void startGroupCall(Context context, Integer groupNo){
        int resultCode = MyTerminalFactory.getSDK().getGroupCallManager().requestGroupCall("",groupNo);
        if(resultCode != BaseCommonCode.SUCCESS_CODE){
            ToastUtils.showShort("发起组呼失败");
        }
    }

    private static void jumpPersonChatActivity(Context context, Integer memberNo){
        TerminalFactory.getSDK().getThreadPool().execute(()->{
            Account account = DataUtil.getAccountByMemberNo(memberNo, true);
            if(account != null){
                IndividualNewsActivity.startCurrentActivity(context,memberNo,account.getName(),true,0);
            }
        });
    }

    /**
     * 自己上报，邀请别人来观看
     *
     * @param context
     * 推送给一个人
     */
    
    private static void activeStartLive(Context context,String uniqueNoAndType){
        boolean network = MyTerminalFactory.getSDK().hasNetwork();
        if(network){
            OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiverActivePushVideoHandler.class,uniqueNoAndType,false);
        }else{
            ToastUtil.showToast(context, "网络连接异常，请检查网络！");
        }
    }

    /**
     * 推送给多个人
     * @param context
     * @param uniqueNoAndTypes
     */
    private static void activeStartLive(Context context,List<String> uniqueNoAndTypes){
        boolean network = MyTerminalFactory.getSDK().hasNetwork();
        if(network){
            // TODO: 2019/7/2  
//            OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiverActivePushVideoHandler.class,uniqueNoAndTypes,false);
        }else{
            ToastUtil.showToast(context, "网络连接异常，请检查网络！");
        }
    }

    /**
     * 跳转到组会话
     * @param groupNo
     */
    public static void jumpGroupChatActivity(Context context,int groupNo){
        Group group = TerminalFactory.getSDK().getGroupByGroupNo(groupNo);
        GroupCallNewsActivity.startCurrentActivity(context,groupNo,group.getName(),0,"",true);
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
            OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiveCurrentGroupIndividualCallHandler.class, member);
        }else{
            ToastUtils.showShort(R.string.text_network_connection_abnormal_please_check_the_network);
        }
    }

    public static void jumpToLaunch(Context context, String key, String value,String appKey){
        Intent intentLaunch = new Intent(context, RegistActivity.class);
        intentLaunch.putExtra(UrlParams.JUMP_KEY, key);
        intentLaunch.putExtra(UrlParams.JUMP_VALUE, value);
        intentLaunch.putExtra(UrlParams.APP_KEY, appKey);
        context.startActivity(intentLaunch);
    }
}
