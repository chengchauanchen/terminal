package cn.vsx.vc.activity;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.tools.Params;
import cn.vsx.vc.R;
import cn.vsx.vc.application.UpdateManager;
import ptt.terminalsdk.context.MyTerminalFactory;

public class AboutActivity extends BaseActivity implements View.OnClickListener{



    TextView barTitle;

    ImageView rightBtn;

    Button ok_btn;

    TextView tv_version;

    @Override
    public int getLayoutResId() {
        return R.layout.activity_about;
    }

    @Override
    public void initView() {
        barTitle = (TextView) findViewById(R.id.bar_title);
        rightBtn = (ImageView) findViewById(R.id.right_btn);
        ok_btn = (Button) findViewById(R.id.ok_btn);
        tv_version = (TextView) findViewById(R.id.tv_version);
        rightBtn.setVisibility(View.INVISIBLE);
        ok_btn.setVisibility(View.GONE);
        barTitle.setText(getResources().getString(R.string.text_about_app));
        tv_version.setText(String.format(getResources().getString(R.string.activity_about_version),getVersionName()));
        findViewById(R.id.help).setOnClickListener(this);
        findViewById(R.id.tv_update).setOnClickListener(this);
        findViewById(R.id.news_bar_back).setOnClickListener(this);
    }

    @Override
    public void initListener() {

    }

    @Override
    public void initData() {

    }

    @Override
    public void doOtherDestroy() {

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.help:
                // 每次更新内容展示
                // TODO: 2018/5/22
                startActivity(new Intent(this,FunctionIntroduceActivity.class));
                break;
            case R.id.tv_update:
                TerminalFactory.getSDK().getThreadPool().execute(()->{
                    UpdateManager manager = new UpdateManager(AboutActivity.this);
                    manager.checkUpdate(MyTerminalFactory.getSDK().getParam(Params.UPDATE_URL,""),true);
                });
//                Toast.makeText(this, getResources().getString(R.string.text_is_the_latest_version), Toast.LENGTH_SHORT).show();
                break;
            case R.id.news_bar_back:
                finish();
                break;
        }
    }

    private String getVersionName(){
        String localVersion = "";
        try{
            PackageInfo packageInfo = getApplicationContext().getPackageManager().getPackageInfo(getPackageName(), 0);
            localVersion = packageInfo.versionName;
        }catch(PackageManager.NameNotFoundException e){
            e.printStackTrace();
        }
        return localVersion;
    }
}
