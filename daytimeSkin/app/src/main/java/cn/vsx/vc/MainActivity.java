package cn.vsx.vc;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import cn.vsx.vsxsdk.VsxSDK;

public class MainActivity extends AppCompatActivity {


    private Button btn_log1;
    private Button btn_log2;
    private Button btn_log3;
    private Button btn_log4;
    private Button btn_log5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        btn_log1 = findViewById(R.id.btn_log1);
        btn_log2 = findViewById(R.id.btn_log2);
        btn_log3 = findViewById(R.id.btn_log3);
        btn_log4 = findViewById(R.id.btn_log4);
        btn_log5 = findViewById(R.id.btn_log5);
        //自己上报，邀请别人来观看
        btn_log1.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                VsxSDK.getJumpSDK().activeStartLive(10000120);
            }
        });
        //请求别人上报
        btn_log2.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                VsxSDK.getJumpSDK().requestOtherLive(10000120);
            }
        });
        //个呼
        btn_log3.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                VsxSDK.getJumpSDK().activeIndividualCall(10000120);
            }
        });
        //个人会话
        btn_log4.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                VsxSDK.getJumpSDK().jumpPersonChatActivity(10000120);
            }
        });
        //组会话
        btn_log5.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                VsxSDK.getJumpSDK().jumpGroupChatActivity(100040);
            }
        });
    }

//    private void initData() {
//        new Thread() {
//            @Override
//            public void run() {
//                synchronized (this) {
//                    System.out.println("1111111");
//                    try {
//                        wait(2000);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                    System.out.println("222222222");
//                }
//
//            }
//        }.start();
//
//        double longitude=0.0;
//        double latitude=0.0;
//
//        if (longitude != 0 && latitude != 0) {
//
//            System.out.println("出错了！！！！！");
//        }else
//        {
//            System.out.println("正常！！！！！！！！");
//        }
//    }
}
