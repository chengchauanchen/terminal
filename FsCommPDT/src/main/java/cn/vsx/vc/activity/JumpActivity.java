package cn.vsx.vc.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import cn.vsx.hamster.common.TerminalMemberType;
import cn.vsx.hamster.common.UrlParams;
import cn.vsx.vc.utils.JumpManager;
import cn.vsx.vc.utils.SystemUtil;
import ptt.terminalsdk.context.MyTerminalFactory;

/**
 * @author martian on 2018/11/20.
 */
public class JumpActivity extends AppCompatActivity{

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initData();
    }
    private void initData() {
        try{
            Intent intent = getIntent();
            if (intent != null) {
                String type = MyTerminalFactory.getSDK().getParam(UrlParams.TERMINALMEMBERTYPE, "");
                if(TerminalMemberType.TERMINAL_UAV.toString().equals(type)){
                    JumpManager.checkJump(intent.getData(), SystemUtil.isLaunchedActivity(this, Class.forName("cn.vsx.uav.activity.UavMainActivity")));
                }else {
                    JumpManager.checkJump(intent.getData(), SystemUtil.isLaunchedActivity(this, Class.forName("cn.vsx.vc.activity.NewMainActivity")));
                }
            }
            finish();
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
