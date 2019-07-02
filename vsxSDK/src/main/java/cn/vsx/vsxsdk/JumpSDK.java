package cn.vsx.vsxsdk;

import android.util.Log;

import java.util.logging.Logger;

import cn.vsx.vsxsdk.Interf.JumpInterface;


public class JumpSDK implements JumpInterface {


    @Override
    public void activeStartLive(int memberNo) {
        try{
            VsxSDK.getIJump().activeIndividualCall();
        }catch (Exception e){
            Log.e("JumpSDK",e.toString());
        }
    }

    @Override
    public void activeStartLive(int memberNo, int type) {

    }

    @Override
    public void requestOtherLive(int memberNo, int type) {

    }

    @Override
    public void activeIndividualCall(int memberNo) {

    }

    @Override
    public void activeIndividualCall(int memberNo, int type) {

    }

    @Override
    public void jumpPersonChatActivity(int memberNo) {

    }

    @Override
    public void jumpGroupChatActivity(int groupNo) {

    }
}
