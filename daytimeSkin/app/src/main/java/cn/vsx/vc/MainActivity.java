package cn.vsx.vc;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import cn.vsx.vsxsdk.Interf.CommonMessageListener;
import cn.vsx.vsxsdk.Interf.EmergencyVideoLiveListener;
import cn.vsx.vsxsdk.Interf.GoWatchRTSPListener;
import cn.vsx.vsxsdk.Interf.IndividualCallListener;
import cn.vsx.vsxsdk.Interf.LiveInComeListener;
import cn.vsx.vsxsdk.Interf.TempGroupListener;
import cn.vsx.vsxsdk.VsxSDK;
import cn.vsx.vsxsdk.utils.download.DownLoadApkUtils;

public class MainActivity extends AppCompatActivity {


    private Button btn_log1;
    private Button btn_log2;
    private Button btn_log3;
    private Button btn_log4;
    private Button btn_log5;
    private Button btn_log6;
    private DownLoadApkUtils loadApkUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        VsxSDK.getInstance().getJumpSDK().registerConnectJumpReceiver(this);
        initView();
//        initDownLoadApk();
        //open();
    }

    private void initDownLoadApk() {
        loadApkUtils = new DownLoadApkUtils(this);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void open(){
            boolean isGranted = this.getPackageManager().canRequestPackageInstalls();
            //安装应用的逻辑(写自己的就可以)
            if(!isGranted){
                new AlertDialog.Builder(this)
                        .setCancelable(false)
                        .setTitle("安装应用需要打开未知来源权限，请去设置中开启权限")
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface d, int w) {
                                //开启列表
                                //Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES);
                                //mAct.startActivityForResult(intent, UNKNOWN_CODE);
                                //开启指定应用 打开 安装未知来源权限
                                Uri packageURI = Uri.parse("package:"+getPackageName());
                                Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,packageURI);
                                startActivity(intent);
                            }
                        })
                        .show();
            }
    }


    public static boolean isGrantExternalRW(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && activity.checkSelfPermission(
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            activity.requestPermissions(new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, 1);
            return false;
        }
        return true;
    }

    private void initView() {
        btn_log1 = findViewById(R.id.btn_log1);
        btn_log2 = findViewById(R.id.btn_log2);
        btn_log3 = findViewById(R.id.btn_log3);
        btn_log4 = findViewById(R.id.btn_log4);
        btn_log5 = findViewById(R.id.btn_log5);
        btn_log6 = findViewById(R.id.btn_log6);
        //自己上报，邀请别人来观看
        btn_log1.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // VsxSDK.getInstance().getJumpSDK().activeStartLive("021222");
                //VsxSDK.getInstance().getJumpSDK().sendStartAppBroadcast(MainActivity.this);
//                VsxSDK.getInstance().getJumpSDK().startVsxService(MainActivity.this);

                if(!isGrantExternalRW(MainActivity.this)){
                    Log.e("MainActivity","没得权限");
                    return;
                }else{
//                    final String url = "http://192.168.1.100:6063/u/phone_common_08-28_1.0.40_40.apk";
//                    loadApkUtils.startDownLoadApk(url);
                    VsxSDK.getInstance().getJumpSDK().autoDownloadApk(MainActivity.this);
                }
            }
        });
        //请求别人上报
        btn_log2.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //VsxSDK.getInstance().getJumpSDK().requestOtherLive("021222");
                startActivity(new Intent(MainActivity.this,AddMemberToTempGroupActivity.class));
            }
        });
        //个呼
        btn_log3.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                VsxSDK.getInstance().getJumpSDK().activeIndividualCall("021222");
            }
        });
        //个人会话
        btn_log4.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                VsxSDK.getInstance().getJumpSDK().jumpPersonChatActivity("021222");
            }
        });
        //组会话
        btn_log5.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //组会话页
//                VsxSDK.getInstance().getJumpSDK().jumpGroupChatActivity("100040");
                VsxSDK.getInstance().getJumpSDK().jumpGroupChatActivityForName("T20190823010188");
            }
        });

        //打开融合通信app
        btn_log6.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                VsxSDK.getInstance().getJumpSDK().launchedVSXApp(MainActivity.this);
            }
        });

        //普通消息监听（文本、语音、图片、视频、定位）
        VsxSDK.getInstance().getRegistMessageListener().setCommonMessageListener(new CommonMessageListener() {
            @Override
            public void onReceived(String messageJson) {
                Log.d("MainActivity", messageJson);
            }
        });
        //临时组消息
        VsxSDK.getInstance().getRegistMessageListener().setTempGroupListener(new TempGroupListener() {
            @Override
            public void onReceived(String messageJson) {
                Log.d("MainActivity", messageJson);
            }
        });


        //收到强制上报图像的通知 消息监听
        VsxSDK.getInstance().getRegistMessageListener().setEmergencyVideoLiveListener(new EmergencyVideoLiveListener() {
            @Override
            public void onReceived(String messageJson) {
                Log.d("MainActivity", messageJson);
            }
        });

        //观看上报视频
        VsxSDK.getInstance().getRegistMessageListener().setGoWatchRTSPListener(new GoWatchRTSPListener() {
            @Override
            public void onReceived(String messageJson) {
                Log.d("MainActivity", messageJson);
            }
        });

        //个呼消息监听
        VsxSDK.getInstance().getRegistMessageListener().setIndividualCallListener(new IndividualCallListener() {
            @Override
            public void onReceived(String messageJson) {
                Log.d("MainActivity", messageJson);
            }
        });

        //收到别人请求我开启直播的通知
        VsxSDK.getInstance().getRegistMessageListener().setLiveInComeListener(new LiveInComeListener() {
            @Override
            public void onReceived(String messageJson) {
                Log.d("MainActivity", messageJson);
            }
        });

    }


    public void startPush(View view) {
        VsxSDK.getInstance().getJumpSDK().activeStartLive();
    }

    public void voipCall(View view) {
        VsxSDK.getInstance().getJumpSDK().voipCall("13100715231");
    }

    public void createTempGroup(View view) {
        VsxSDK.getInstance().getJumpSDK().createTemporaryGroup();
    }

    public void startMyApp(View view){
        VsxSDK.getInstance().getJumpSDK().sendStartAppBroadcast(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        VsxSDK.getInstance().getJumpSDK().unregisterConnectJumpReceiver(this);
        loadApkUtils.onDestroy();
    }
}
