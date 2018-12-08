package cn.vsx.vc.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import cn.vsx.vc.R;
import cn.vsx.vc.dialog.ProgressDialog;

public class SplashFragment extends Fragment{

    private Handler mHandler = new Handler();
    private ProgressDialog myProgressDialog;
    public SplashFragment(){
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        return inflater.inflate(R.layout.fragment_splash, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);
        if (myProgressDialog == null) {
            myProgressDialog = new ProgressDialog(getContext());
//            myProgressDialog.setLoadingRes(R.drawable.loading2);
//            myProgressDialog.setLoadingBgRes(R.drawable.loading_bg2);
//            myProgressDialog.setTextColor(R.color.dialog_text);
            myProgressDialog.setCancelable(false);
            myProgressDialog.setMsg("正在加载...");
            myProgressDialog.show();
        }
        mHandler.postDelayed(new Runnable(){
            @Override
            public void run(){
                myProgressDialog.dismiss();
            }
        },3000);

    }
}
