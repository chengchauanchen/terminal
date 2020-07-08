package cn.vsx.vc.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import com.blankj.utilcode.util.ScreenUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.zectec.imageandfileselector.utils.OperateReceiveHandlerUtilSync;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.vsx.hamster.terminalsdk.manager.audio.IAudioProxy;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveVolumeOffCallHandler;
import cn.vsx.vc.R;
import cn.vsx.vc.listener.PlayVideoStateListener;
import cn.vsx.vc.receiveHandle.ReceiverFragmentPopBackStackHandler;
import cn.vsx.vc.utils.Constants;
import ptt.terminalsdk.context.MyTerminalFactory;
import ptt.terminalsdk.tools.StringUtil;

/**
 * Created by gt358 on 2017/10/20.
 */

public class FileVideoShowFragment extends Fragment implements View.OnClickListener {

    @Bind(R.id.iv_return)
    ImageView ivReturn;
    @Bind(R.id.tv_title)
    TextView tvTitle;
    @Bind(R.id.iv_close)
    ImageView ivClose;


    @Bind(R.id.preview_pager)
    ViewPager viewPager;
//    @Bind(R.id.video_view)
//    VideoView video_view;

    //音量
//    @Bind(R.id.volume_layout)
//    VolumeViewLayout volumeViewLayout;

    @Bind(R.id.tv_quiet_play)
    TextView mTvQuietPlay;

    private MyAdapter myAdapter;
    private ArrayList<String> filePaths = new ArrayList<>();
    private ArrayList<VideoView> videoViews = new ArrayList<>();
    private int fileIndex = 0;

    private static final int RECEIVEVOICECHANGED = 1;
    private static final int UPDATEQUITPLAY = 4;



    private PlayVideoStateListener mListener;

    private Handler mHandler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg){
            super.handleMessage(msg);
            switch(msg.what){
                case RECEIVEVOICECHANGED:
//                    if(volumeViewLayout!=null){
//                        volumeViewLayout.setVisibility(View.GONE);
//                    }
                    break;
                case UPDATEQUITPLAY:
                    removeMessages(UPDATEQUITPLAY);
//                    updateQuitPlay();
                    break;
                default:
                    break;
            }
        }
    };
    public Logger logger = Logger.getLogger(getClass());

    public static FileVideoShowFragment newInstance() {
        return new FileVideoShowFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try{
            if (getArguments() != null) {
                ArrayList<String> filePathsList = getArguments().getStringArrayList(Constants.FILE_PATHS);
                fileIndex = getArguments().getInt(Constants.FILE_INDEX);
                filePaths.clear();
                if(filePathsList!=null){
                    filePaths.addAll(filePathsList);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_file_video_show, container, false);
        ButterKnife.bind(this, view);
        initView();
        initListener();
        initData();
        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (PlayVideoStateListener) activity;
        } catch (Exception e) {
           e.printStackTrace();
        }
    }

    /**
     * 初始化布局
     */
    private void initView() {
        ivReturn.setVisibility(View.VISIBLE);
        tvTitle.setPadding(0, 0, 0, 0);
        ivClose.setVisibility(View.INVISIBLE);
        ivClose.setEnabled(false);
        //判断数据是否为空
        if(filePaths.isEmpty()){
            tvTitle.setText(String.format(getString(R.string.text_show_picture_index), 0, 0));
            return;
        }
        //判断异常情况
        if(fileIndex<0|| fileIndex >= filePaths.size()){
            fileIndex = 0;
        }
        tvTitle.setText(String.format(getString(R.string.text_show_picture_index), (fileIndex+1), filePaths.size()));
        myAdapter =  new MyAdapter(filePaths);
        viewPager.setAdapter(myAdapter);
        viewPager.setOffscreenPageLimit(2);
        viewPager.addOnPageChangeListener(onPageChangeListener);
        viewPager.setOnTouchListener(new OnTouchListenerImpChengeVolume());
        viewPager.setCurrentItem(fileIndex);
        clearPlayOthers(fileIndex);
        viewPager.post(() -> onPageChangeListener.onPageSelected(fileIndex));
    }

    /**
     * 添加监听
     */
    private void initListener() {
    }

    private ViewPager.OnPageChangeListener onPageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrollStateChanged(int arg0) { }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) { }

        @Override
        public void onPageSelected(int position) {
            logger.info("onPageSelected--position:"+position);
            fileIndex = position;
            try{
                tvTitle.setText(String.format(getString(R.string.text_show_picture_index), (position + 1), filePaths.size()));
                clearPlayOthers(position);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    };

    /**
     * 获取数据
     */
    private void initData() {
    }

    @Override
    public void onResume(){
        super.onResume();
    }

    @OnClick({R.id.iv_return,R.id.tv_quiet_play})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_return:
                //关闭页面
                OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiverFragmentPopBackStackHandler.class);
                break;
            case R.id.tv_quiet_play:
                //静音预览
                ToastUtils.showShort(R.string.uav_push_quiet_play);
                break;
                default:break;
        }
    }
//    /**
//     * 更新是否静音播放
//     */
//    private ReceiverUpdatePlayVideoStateHandler receiverUpdatePlayVideoStateHandler = new ReceiverUpdatePlayVideoStateHandler() {
//        @Override
//        public void handler() {
//            mHandler.sendEmptyMessageDelayed(UPDATEQUITPLAY,500);
//        }
//    };

    /**
     * 更新是否静音播放的状态
     */
    private void updateQuitPlay() {
        if(mListener!=null && mListener.canQuitPlay()){
//            if(mediaPlayer!=null) {
//                mediaPlayer.setVolume(0.0f, 0.0f);
//            }
            if(mTvQuietPlay!=null){
                mTvQuietPlay.setVisibility(View.VISIBLE);
            }
        }else{
//            if(mediaPlayer!=null) {
//                mediaPlayer.setVolume(0.5f,0.5f);
//            }
            if(mTvQuietPlay!=null){
                mTvQuietPlay.setVisibility(View.GONE);
            }
        }
    }

    /**
     * 设置
     * @param path
     */
    public void setTextureViewSize(VideoView videoView,String path){
        if(TextUtils.isEmpty(path)){
            return;
        }
        android.media.MediaMetadataRetriever mmr = new android.media.MediaMetadataRetriever();
        try {
            mmr.setDataSource(path);
            int width = StringUtil.stringToInt(mmr.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH));//宽
            int height = StringUtil.stringToInt(mmr.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT));//高
            int screenWidth = ScreenUtils.getScreenWidth();
            if(videoView!=null&&width>0){
                android.view.ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(screenWidth, screenWidth*height/width);
//                lp.width = screenWidth;
//                lp.height = screenWidth*height/width;
                videoView.setLayoutParams(lp);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            mmr.release();
        }
    }

    class MyAdapter extends PagerAdapter {
        private List<String> data ;

        MyAdapter(List<String> data){
            this.data = data;
        }
        @Override
        public int getCount(){
            return data.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object){
            return view == object;
        }

        @Override
        public View instantiateItem(ViewGroup container, int position){
            try{
                String path = data.get(position);
                VideoView videoView = getVideoViewByPath(path);
                if(videoView!=null){
                    videoView.setTag(path);
                    videoViews.add(videoView);
                    container.addView(videoView);
                }
                return videoView;
            }catch (Exception e){
                e.printStackTrace();
            }
            return null;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object){
            try{
                container.removeView((View) object);
                videoViews.remove(object);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    /**
     * 上下滑动改变音量
     */
    private final class OnTouchListenerImpChengeVolume implements View.OnTouchListener {
        private float downY;
        private int downCurrentVolumeC;
        private float downX;
        private boolean canControl = true;

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            logger.info("onTouch---event：" + event.getAction());
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    downY = event.getY();
                    downX = event.getX();
                    downCurrentVolumeC = MyTerminalFactory.getSDK().getAudioProxy().getVolume();
                    canControl = true;
                    break;
                case MotionEvent.ACTION_MOVE:
                    float moveY = event.getY();
                    float moveX = event.getX();
                    //位移距离
                    float disY = moveY - downY;
                    float disX = moveX - downX;

                    if (Math.abs(disY) > Math.abs(disX)) {

                        //手指在屏幕上划过距离百分比 = 划过距离/屏幕高度
                        float disPercent = -disY / (getResources().getDisplayMetrics().heightPixels / 3);
                        //偏移音量 = 手指在屏幕上划过距离百分比*最大音量
                        float disVolumeC = disPercent * IAudioProxy.VOLUME_MAX;
                        int endVolumeC = ((int) (downCurrentVolumeC + disVolumeC)) / IAudioProxy.VOLUME_STEP * IAudioProxy.VOLUME_STEP;
                        if (endVolumeC > IAudioProxy.VOLUME_MAX) {
                            endVolumeC = IAudioProxy.VOLUME_MAX;
                        } else if (endVolumeC < 0) {
                            endVolumeC = 0;
                        }

                        int screenWidth = getResources().getDisplayMetrics().widthPixels;
                        if (moveX < screenWidth / 3 || moveX > 2 * screenWidth / 3) {// 左右两侧进行通话音量的设置
                            MyTerminalFactory.getSDK().getAudioProxy().setVolume(endVolumeC);
                            if (endVolumeC == 0) {
                                OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiveVolumeOffCallHandler.class, true, 1);
                            } else {
                                OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiveVolumeOffCallHandler.class, false, 1);
                            }
                        }
                        canControl = true;
                    }else{
                        canControl = false;
                    }
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    v.performClick();
                    break;
                default:
                    break;
            }
            return canControl;
        }
    }

    /**
     * 清空其他的状态，开始播放当前的videoview
     * @param position
     */
    private void clearPlayOthers(int position) {
        try{
            String path = "";

            if(position>=0 && position<filePaths.size()){
                path =  filePaths.get(position);

            }
            logger.info("-----clearPlayOthers:"+path);
            for (int i = 0; i < videoViews.size(); i++) {
                VideoView videoView = videoViews.get( i);
                if(videoView!=null){
                    logger.info("-----clearPlayOthers-videoView:"+videoView.getTag());
                    if(!TextUtils.isEmpty(path)&&TextUtils.equals((String) videoView.getTag(),path)){
                        videoView.start();
                    }else{
                        videoView.pause();
                    }

                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private VideoView getVideoViewByPath(String path){
        try{
            if(!TextUtils.isEmpty(path)){
                VideoView videoView = new VideoView(FileVideoShowFragment.this.getContext());
                videoView.setMediaController(new MediaController(FileVideoShowFragment.this.getContext()));
                videoView.setVideoPath(path);
                videoView.seekTo(0);
                setTextureViewSize(videoView,path);
                videoView.setOnErrorListener((mp, what, extra) -> {
                    videoView.stopPlayback();
                    return true;
                });
//                videoViews.add(videoView);
                return videoView;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void onPause(){
        super.onPause();
        try{
            clearPlayOthers(-1);
        }catch(IllegalStateException e){
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        try{
            ButterKnife.unbind(this);
//            if(volumeViewLayout!=null){
//                volumeViewLayout.unRegistLintener();
//            }
            mHandler.removeCallbacksAndMessages(null);
            MyTerminalFactory.getSDK().getAudioProxy().setVolume(IAudioProxy.VOLUME_DEFAULT);
        }catch(IllegalStateException e){
            e.printStackTrace();
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
