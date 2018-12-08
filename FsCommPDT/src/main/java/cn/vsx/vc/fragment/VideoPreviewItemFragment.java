package cn.vsx.vc.fragment;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.MediaController;

import butterknife.Bind;
import butterknife.OnClick;
import cn.vsx.vc.R;
import cn.vsx.vc.activity.ChatBaseActivity;
import cn.vsx.vc.view.CustomerVideoView;

/**
 * 看小视频fragment
 */
public class VideoPreviewItemFragment extends BaseFragment{

    @Bind(R.id.videoView)
    CustomerVideoView videoView;

    private FrameLayout fragment_contener;
    public VideoPreviewItemFragment(){
        // Required empty public constructor
    }

    public static VideoPreviewItemFragment newInstance(String filePath){
        VideoPreviewItemFragment fragment = new VideoPreviewItemFragment();
        Bundle bundle = new Bundle();
        bundle.putString("filePath",filePath);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public int getContentViewId(){
        return R.layout.fragment_video_preview_item;
    }

    @Override
    public void initView(){
        ((ChatBaseActivity) getActivity()).setBackListener(new ChatBaseActivity.OnBackListener(){
            @Override
            public void onBack(){
                if(null !=getActivity() && !isDetached()){
                    if(null != videoView && videoView.isPlaying()){
                        videoView.stopPlayback();
                    }
                    popBack();
                }
            }
        });
    }

    @Override
    public void initListener(){
    }

    @OnClick(R.id.iv_close)
    public void close(){
        popBack();
    }

    @Override
    public void initData(){
        String filePath = getArguments().getString("filePath");
        Log.e("文件路径：", filePath);
        videoView.setVideoPath(filePath);
        MediaController mediaController = new MediaController(getActivity());
        videoView.setMediaController(mediaController);
        mediaController.setMediaPlayer(videoView);

        videoView.start();
        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener(){
            @Override
            public void onCompletion(MediaPlayer mp){
                popBack();
            }
        });
    }

    @Override
    public void onDestroyView(){
        super.onDestroyView();
        if (videoView != null) {
            videoView.suspend();
        }
    }

    public void popBack(){
        if(null !=fragment_contener){
            fragment_contener.setVisibility(View.GONE);
        }
        if(null != getActivity() && !isDetached()){
            getActivity().getSupportFragmentManager().beginTransaction().remove(this).commit();
            getActivity().getSupportFragmentManager().popBackStack();
            ((ChatBaseActivity) getActivity()).setBackListener(null);
        }
    }

    public void setFragment_contener(FrameLayout fragmentContener){
        this.fragment_contener = fragmentContener;
    }
}
