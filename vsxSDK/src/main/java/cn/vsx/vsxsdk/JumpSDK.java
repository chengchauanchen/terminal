package cn.vsx.vsxsdk;

import android.util.Log;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.vsx.vsxsdk.Interf.JumpInterface;
import cn.vsx.vsxsdk.constant.CommandEnum;
import cn.vsx.vsxsdk.constant.ParamKey;


public class JumpSDK implements JumpInterface {

    @Override
    public void activeStartLive(){
        try{
            Map map= new HashMap();
            VsxSDK.getIJump().jumpPage(map,CommandEnum.SelfLive.getType());//;
        }catch (Exception e){
            Log.e("JumpSDK",e.toString());
        }
    }

    @Override
    public void activeStartLive(int memberNo) {
        try{
            Map map= new HashMap();
            map.put(ParamKey.MEMBER_NO,memberNo);
            VsxSDK.getIJump().jumpPage(map,CommandEnum.SelfLive.getType());//;
        }catch (Exception e){
            Log.e("JumpSDK",e.toString());
        }
    }

    @Override
    public void activeStartLive(int memberNo, int type) {
        try{
            Map map= new HashMap();
            map.put(ParamKey.MEMBER_NO,memberNo);
            map.put(ParamKey.TERMINAL_TYPE,type);
            VsxSDK.getIJump().jumpPage(map,CommandEnum.SelfLive.getType());//;
        }catch (Exception e){
            Log.e("JumpSDK",e.toString());
        }
    }

    @Override
    public void requestOtherLive(int memberNo) {
        try{
            Map map= new HashMap();
            map.put(ParamKey.MEMBER_NO,memberNo);
            VsxSDK.getIJump().jumpPage(map,CommandEnum.OtherLive.getType());//;
        }catch (Exception e){
            Log.e("JumpSDK",e.toString());
        }
    }

    @Override
    public void requestOtherLive(int memberNo, int type) {
        try{
            Map map= new HashMap();
            map.put(ParamKey.MEMBER_NO,memberNo);
            map.put(ParamKey.TERMINAL_TYPE,type);
            VsxSDK.getIJump().jumpPage(map,CommandEnum.OtherLive.getType());//;
        }catch (Exception e){
            Log.e("JumpSDK",e.toString());
        }
    }

    @Override
    public void activeIndividualCall(int memberNo) {
        try{
            Map map= new HashMap();
            map.put(ParamKey.MEMBER_NO,memberNo);
            VsxSDK.getIJump().jumpPage(map,CommandEnum.IndividualCall.getType());//;
        }catch (Exception e){
            Log.e("JumpSDK",e.toString());
        }
    }

    @Override
    public void activeIndividualCall(int memberNo, int type) {
        try{
            Map map= new HashMap();
            map.put(ParamKey.MEMBER_NO,memberNo);
            map.put(ParamKey.TERMINAL_TYPE,type);
            VsxSDK.getIJump().jumpPage(map,CommandEnum.IndividualCall.getType());//;
        }catch (Exception e){
            Log.e("JumpSDK",e.toString());
        }
    }

    @Override
    public void jumpPersonChatActivity(int memberNo) {
        try{
            Map map= new HashMap();
            map.put(ParamKey.MEMBER_NO,memberNo);
            VsxSDK.getIJump().jumpPage(map,CommandEnum.PersonChat.getType());//;
        }catch (Exception e){
            Log.e("JumpSDK",e.toString());
        }
    }

    @Override
    public void jumpGroupChatActivity(int groupNo) {
        try{
            Map map= new HashMap();
            map.put(ParamKey.GROUP_NO,groupNo);
            VsxSDK.getIJump().jumpPage(map,CommandEnum.GroupChat.getType());//;
        }catch (Exception e){
            Log.e("JumpSDK",e.toString());
        }
    }

    @Override
    public void voipCall(String phoneNo, String appkey){
        try{
            Map map= new HashMap();
            map.put(ParamKey.PHONE_NO,phoneNo);
            map.put(ParamKey.APP_KEY,appkey);
            VsxSDK.getIJump().jumpPage(map, CommandEnum.VoipCall.getType());
        }catch (Exception e){
            Log.e("JumpSDK",e.toString());
        }
    }

    @Override
    public void createTemporaryGroup(String appkey){
        try{
            Map map= new HashMap();
            VsxSDK.getIJump().jumpPage(map, CommandEnum.CreateTempGroup.getType());
        }catch (Exception e){
            Log.e("JumpSDK",e.toString());
        }
    }

    @Override
    public void changeGroup(String groupNo, String appkey){
    }

    @Override
    public void monitorGroup(String groupNo, String appkey){
    }

    @Override
    public void pushVideoLive(List<String> numberNos, List<String> groupNos, String appkey){
        try{
            Map map= new HashMap();
            map.put(ParamKey.MEMBER_NOS,numberNos);
            map.put(ParamKey.GROUP_NOS,groupNos);
            map.put(ParamKey.APP_KEY,appkey);
            VsxSDK.getIJump().jumpPage(map, CommandEnum.pushVideoLive.getType());
        }catch (Exception e){
            Log.e("JumpSDK",e.toString());
        }
    }
}
