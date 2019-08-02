package cn.vsx.uav.activity;

import cn.vsx.uav.R;
import cn.vsx.vc.activity.BaseActivity;

public class UavPushActivity extends BaseActivity{

    protected void setOritation() {
        this.oritationPort = false;
    }

    @Override
    public int getLayoutResId(){
        return R.layout.activity_uav_push;
    }

    @Override
    public void initView(){

    }

    @Override
    public void initListener(){
    }

    @Override
    public void initData(){
    }

    @Override
    public void doOtherDestroy(){
    }
}
