package cn.vsx.vc.activity;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.Bind;
import butterknife.OnClick;
import cn.vsx.vc.R;

public class AboutActivity extends BaseActivity {


    @Bind(R.id.bar_title)
    TextView barTitle;
    @Bind(R.id.right_btn)
    ImageView rightBtn;
    @Bind(R.id.ok_btn)
    Button ok_btn;
    @Bind(R.id.tv_version)
    TextView tv_version;

    @Override
    public int getLayoutResId() {
        return R.layout.activity_about;
    }

    @Override
    public void initView() {
        barTitle.setText(getResources().getString(R.string.text_about_app));
        rightBtn.setVisibility(View.INVISIBLE);
        ok_btn.setVisibility(View.GONE);
        tv_version.setText(String.format(getResources().getString(R.string.activity_about_version),getVersionName()));
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

    @OnClick({ R.id.help, R.id.tv_update, R.id.news_bar_back})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.help:
                // 每次更新内容展示
                // TODO: 2018/5/22
                startActivity(new Intent(this,FunctionIntroduceActivity.class));
                break;
            case R.id.tv_update:
//                UpdateManager manager = new UpdateManager(this);
//                manager.checkUpdate(MyTerminalFactory.getSDK().getParam(Params.UPDATE_URL,""),true);
                Toast.makeText(this, getResources().getString(R.string.text_is_the_latest_version), Toast.LENGTH_SHORT).show();
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
