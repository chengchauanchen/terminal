package cn.vsx.vc;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {


    private Button btn_log;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        btn_log = findViewById(R.id.btn_log);
        btn_log.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

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
