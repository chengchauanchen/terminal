package cn.vsx.vc.activity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
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
                Uri uri = getIntent().getData();
                if(uri!=null){
                    String third_app_package_name = intent.getData().getQueryParameter(UrlParams.THIRD_APP_PACKAGE_NAME);

                    String type = MyTerminalFactory.getSDK().getParam(UrlParams.TERMINALMEMBERTYPE, "");
                    if(TerminalMemberType.TERMINAL_UAV.toString().equals(type)){
                        //JumpManager.checkJump(intent.getData(), SystemUtil.isLaunchedActivity(this, Class.forName("cn.vsx.uav.activity.UavMainActivity")));
                    }else {
                        //JumpManager.checkJump(intent.getData(), SystemUtil.isLaunchedActivity(this, Class.forName("cn.vsx.vc.activity.NewMainActivity")));
                        launchedActivity(third_app_package_name);
                    }
                }
            }
            finish();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private void launchedActivity(String third_app_package_name){
        try{

            if(SystemUtil.isLaunchedActivity(this, Class.forName("cn.vsx.vc.activity.NewMainActivity"))){
                startNewMainActivity(third_app_package_name);
            }else{
                jumpToLaunch(third_app_package_name);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    private void startNewMainActivity(String third_app_package_name){
        Intent intent = new Intent(this, NewMainActivity.class);
        intent.putExtra(UrlParams.THIRD_APP_PACKAGE_NAME,third_app_package_name);
        startActivity(new Intent(this,NewMainActivity.class));
    }

    public void jumpToLaunch(String third_app_package_name){
        Intent intentLaunch = new Intent(this, RegistActivity.class);
        intentLaunch.putExtra(UrlParams.THIRD_APP_PACKAGE_NAME,third_app_package_name);
        intentLaunch.putExtra(UrlParams.THIRD_APP_PACKAGE_NAME,third_app_package_name);
        startActivity(intentLaunch);
    }

//    private void initData() {
//        try{
//            Intent intent = getIntent();
//            if (intent != null) {
//                String type = MyTerminalFactory.getSDK().getParam(UrlParams.TERMINALMEMBERTYPE, "");
//                if(TerminalMemberType.TERMINAL_UAV.toString().equals(type)){
//                    JumpManager.checkJump(intent.getData(), SystemUtil.isLaunchedActivity(this, Class.forName("cn.vsx.uav.activity.UavMainActivity")));
//                }else {
//                    JumpManager.checkJump(intent.getData(), SystemUtil.isLaunchedActivity(this, Class.forName("cn.vsx.vc.activity.NewMainActivity")));
//                }
//            }
//            finish();
//        }catch(Exception e){
//            e.printStackTrace();
//        }
//    }
}
