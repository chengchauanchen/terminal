package cn.vsx.vc.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import cn.vsx.vc.utils.JumpManager;
import cn.vsx.vc.utils.SystemUtil;

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
        Intent intent = getIntent();
        if (intent != null) {
            JumpManager.checkJump(intent.getData(), SystemUtil.isLaunchedActivity(this, NewMainActivity.class));
        }
        finish();
    }
}
