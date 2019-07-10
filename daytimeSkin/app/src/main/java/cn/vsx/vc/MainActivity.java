package cn.vsx.vc;

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

public class MainActivity extends AppCompatActivity {


    private Button btn_log1;
    private Button btn_log2;
    private Button btn_log3;
    private Button btn_log4;
    private Button btn_log5;
    private Button btn_log6;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        VsxSDK.getInstance().getJumpSDK().registerConnectJumpReceiver(this);
        initView();
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
                VsxSDK.getInstance().getJumpSDK().activeStartLive("021222");
            }
        });
        //请求别人上报
        btn_log2.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                VsxSDK.getInstance().getJumpSDK().requestOtherLive("021222");
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
                VsxSDK.getInstance().getJumpSDK().jumpGroupChatActivity("0");
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
                Log.d("MainActivity",messageJson);
            }
        });
        //临时组消息
        VsxSDK.getInstance().getRegistMessageListener().setTempGroupListener(new TempGroupListener() {
            @Override
            public void onReceived(String messageJson) {
                Log.d("MainActivity",messageJson);
            }
        });


        //收到强制上报图像的通知 消息监听
        VsxSDK.getInstance().getRegistMessageListener().setEmergencyVideoLiveListener(new EmergencyVideoLiveListener() {
            @Override
            public void onReceived(String messageJson) {
                Log.d("MainActivity",messageJson);
            }
        });

        //观看上报视频
        VsxSDK.getInstance().getRegistMessageListener().setGoWatchRTSPListener(new GoWatchRTSPListener() {
            @Override
            public void onReceived(String messageJson) {
                Log.d("MainActivity",messageJson);
            }
        });

        //个呼消息监听
        VsxSDK.getInstance().getRegistMessageListener().setIndividualCallListener(new IndividualCallListener() {
            @Override
            public void onReceived(String messageJson) {
                Log.d("MainActivity",messageJson);
            }
        });

        //收到别人请求我开启直播的通知
        VsxSDK.getInstance().getRegistMessageListener().setLiveInComeListener(new LiveInComeListener() {
            @Override
            public void onReceived(String messageJson) {
                Log.d("MainActivity",messageJson);
            }
        });

    }


    public void startPush(View view){
        VsxSDK.getInstance().getJumpSDK().activeStartLive();
    }

    public void voipCall(View view){
        VsxSDK.getInstance().getJumpSDK().voipCall("13100715231");
    }

    public void createTempGroup(View view){
        VsxSDK.getInstance().getJumpSDK().createTemporaryGroup();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        VsxSDK.getInstance().getJumpSDK().unregisterConnectJumpReceiver(this);
    }
}
