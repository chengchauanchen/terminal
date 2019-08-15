package com.vsxin.terminalpad.mvp.ui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.SurfaceTexture;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ixiaoma.xiaomabus.architecture.mvp.view.layout.MvpLinearLayout;
import com.vsxin.terminalpad.R;
import com.vsxin.terminalpad.mvp.contract.presenter.PullLivePlayerPresenter;
import com.vsxin.terminalpad.mvp.contract.view.IPullLivePlayerView;

/**
 * @author qzw
 * <p>
 * 播放直播控件
 */
public class PullLivePlayer extends MvpLinearLayout<IPullLivePlayerView, PullLivePlayerPresenter> implements IPullLivePlayerView {

    private TextureView ttv_live;
    private ImageView iv_break_live;
    private RelativeLayout rl_live_view;
    private TextView tv_loading;
    private SurfaceTexture surface;

    public PullLivePlayer(Context context) {
        super(context);
        initView();
        initCustomAttrs(context, null);
    }

    public PullLivePlayer(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
        initCustomAttrs(context, attrs);
    }

    public PullLivePlayer(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
        initCustomAttrs(context, attrs);
    }

    private void initView() {
        LayoutInflater inflater = (LayoutInflater) getSuperContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View mView = inflater.inflate(R.layout.widget_pull_live_player_layout, this, true);
        rl_live_view = mView.findViewById(R.id.rl_live_view);
        ttv_live = mView.findViewById(R.id.ttv_live);
        iv_break_live = mView.findViewById(R.id.iv_break_live);
        tv_loading = mView.findViewById(R.id.tv_loading);

        iv_break_live.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                getPresenter().stopPlay();
            }
        });
    }

    private void initCustomAttrs(Context context, AttributeSet attrs) {
        //获取自定义属性。
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.PullLivePlayer);
        //获取文字内容
        String text = ta.getString(R.styleable.PullLivePlayer_loadText);
        ta.recycle();
        tv_loading.setText(text);
    }

    /**
     * 初始化 TextureView
     */
    private void initTextureView(){
        //只有显示(本身，父view都要显示)的时候，才会回调
        getPresenter().setSurfaceTextureListener(ttv_live);
    }

    @Override
    public void show(boolean isShow){
        rl_live_view.setVisibility(isShow?VISIBLE:GONE);
        if(isShow){
            initTextureView();
        }
    }

    @Override
    public SurfaceTexture getSurfaceTexture() {
        return this.surface;
    }

    @Override
    public PullLivePlayerPresenter createPresenter() {
        return new PullLivePlayerPresenter(getSuperContext());
    }


    /****************推流时需要手指滑动缩放摄像头**************/

//    private float oldDist;
//    private View.OnTouchListener svOnTouchListener = (v, event) -> {
//
//        if (event.getPointerCount() == 1) {
//            if (mMediaStream != null) {
//                handleFocus(mMediaStream.getCamera());
//            }
//        } else {
//            switch (event.getAction() & MotionEvent.ACTION_MASK) {
//                case MotionEvent.ACTION_POINTER_DOWN:
//                    oldDist = getFingerSpacing(event);
//                    break;
//                case MotionEvent.ACTION_MOVE:
//                    float newDist = getFingerSpacing(event);
//                    if (Math.abs(newDist - oldDist) > 5f) {
//                        if (newDist > oldDist) {
//                            handleZoom(false);
//                        } else if (newDist < oldDist) {
//                            handleZoom(true);
//                        }
//                        oldDist = newDist;
//                    }
//                    break;
//            }
//        }
//        return true;
//    };
//
//
//    private void handleFocus(Camera camera) {
//        if (null != mMediaStream && mMediaStream.isStreaming() && camera != null && mMediaStream.isPreView()) {
//            camera.autoFocus(null);//屏幕聚焦
//        }
//    }
//
//    private float getFingerSpacing(MotionEvent event) {
//        return (float) Math.sqrt(event.getX(0) * event.getX(1) + event.getY(0) * event.getY(1));
//    }
//
//    private void handleZoom(boolean isScale) {
//        if (null != mMediaStream && mMediaStream.isStreaming()) {
//            mMediaStream.ZoomOrReduceVideo(isScale);
//        }
//    }
}
