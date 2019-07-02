package cn.vsx.vsxsdk;

import android.util.Log;

import java.util.HashMap;
import java.util.Map;

import cn.vsx.vsxsdk.Interf.JumpInterface;
import cn.vsx.vsxsdk.constant.ParamKey;


public class JumpSDK implements JumpInterface {


    @Override
    public void activeStartLive(int memberNo) {
        try{
            Map map= new HashMap();
            map.put(ParamKey.MEMBER_NO,memberNo);
            VsxSDK.getIJump().jumpPage(map,5);//;
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
            VsxSDK.getIJump().jumpPage(map,5);//;
        }catch (Exception e){
            Log.e("JumpSDK",e.toString());
        }
    }

    @Override
    public void requestOtherLive(int memberNo) {
        try{
            Map map= new HashMap();
            map.put(ParamKey.MEMBER_NO,memberNo);
            VsxSDK.getIJump().jumpPage(map,3);//;
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
            VsxSDK.getIJump().jumpPage(map,3);//;
        }catch (Exception e){
            Log.e("JumpSDK",e.toString());
        }
    }

    @Override
    public void activeIndividualCall(int memberNo) {
        try{
            Map map= new HashMap();
            map.put(ParamKey.MEMBER_NO,memberNo);
            VsxSDK.getIJump().jumpPage(map,2);//;
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
            VsxSDK.getIJump().jumpPage(map,2);//;
        }catch (Exception e){
            Log.e("JumpSDK",e.toString());
        }
    }

    @Override
    public void jumpPersonChatActivity(int memberNo) {
        try{
            Map map= new HashMap();
            map.put(ParamKey.MEMBER_NO,memberNo);
            VsxSDK.getIJump().jumpPage(map,4);//;
        }catch (Exception e){
            Log.e("JumpSDK",e.toString());
        }
    }

    @Override
    public void jumpGroupChatActivity(int groupNo) {
        try{
            Map map= new HashMap();
            map.put(ParamKey.GROUP_NO,groupNo);
            VsxSDK.getIJump().jumpPage(map,1);//;
        }catch (Exception e){
            Log.e("JumpSDK",e.toString());
        }
    }
}
