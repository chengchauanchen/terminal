package cn.vsx.vc.activity;

import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import com.liuguangqiang.swipeback.SwipeBackLayout;

import butterknife.ButterKnife;
import cn.vsx.vc.utils.ActivityCollector;

/**
 * Created by zckj on 2017/6/13.
 */

public abstract class SwipeBackActivity extends AppCompatActivity implements SwipeBackLayout.SwipeBackListener{
    private SwipeBackLayout swipeBackLayout;
    private ImageView ivShadow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 没有标题栏
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //隐藏状态栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //屏幕常亮
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        //不可横屏
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        //	         getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }

        setContentView(getLayoutResId());

        ButterKnife.bind(this);

        ActivityCollector.addActivity(this, getClass());

        initView();

        initData();

        initListener();
    }

    //    @Override
//    public void setContentView(int layoutResID) {
//        // 没有标题栏
//        requestWindowFeature(Window.FEATURE_NO_TITLE);
//        //不可横屏
//        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
//
//        super.setContentView(getContainer());
//        View view = LayoutInflater.from(this).inflate(layoutResID, null);
//        swipeBackLayout.addView(view);
//        ButterKnife.bind(this);
//
//        initView();
//
//        initData();
//
//        initListener();
//    }
//
//    private View getContainer() {
//        RelativeLayout container = new RelativeLayout(this);
//        swipeBackLayout = new SwipeBackLayout(this);
//        swipeBackLayout.setOnSwipeBackListener(this);
//        ivShadow = new ImageView(this);
//        ivShadow.setBackgroundColor(getResources().getColor(R.color.black));
//        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
//        container.addView(ivShadow, params);
//        container.addView(swipeBackLayout);
//        return container;
//    }

    public void setDragEdge(SwipeBackLayout.DragEdge dragEdge) {
        swipeBackLayout.setDragEdge(dragEdge);
    }

    public SwipeBackLayout getSwipeBackLayout() {
        return swipeBackLayout;
    }

    @Override
    public void onViewPositionChanged(float fractionAnchor, float fractionScreen) {
        ivShadow.setAlpha(1 - fractionScreen);
    }

    @Override
    protected void onDestroy() {

        doOtherDestroy();
        ButterKnife.unbind(this);//解除绑定，官方文档只对fragment做了解绑
        ActivityCollector.removeActivity(this);
        super.onDestroy();
    }



    /**
     * 获取当前界面的布局
     */
    public abstract int getLayoutResId();

    /**
     * 初始化界面
     */
    public abstract void initView();

    /**
     * 给控件添加监听
     */
    public abstract void initListener();

    /**
     * 初始化数据 给控件填充内容
     */
    public abstract void initData();

    /**
     * 子类activity处理自己的destroy()
     */
    public abstract void doOtherDestroy();
}
