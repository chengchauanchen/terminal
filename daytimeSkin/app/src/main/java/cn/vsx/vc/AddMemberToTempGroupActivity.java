package cn.vsx.vc;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;

import java.util.ArrayList;
import java.util.List;

import cn.vsx.vsxsdk.VsxSDK;

public class AddMemberToTempGroupActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        findViewById(R.id.btn_log1).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                List<String> nos = new ArrayList<>();
                nos.add("10000201");
                nos.add("020446");
                VsxSDK.getInstance().getJumpSDK().addMemberToTempGroup(nos, "T20190823010188");
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
