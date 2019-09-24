package cn.vsx.uav.activity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.blankj.utilcode.util.AdaptScreenUtils;
import com.blankj.utilcode.util.ToastUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cn.vsx.hamster.common.MessageSendStateEnum;
import cn.vsx.hamster.common.MessageType;
import cn.vsx.hamster.common.util.JsonParam;
import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.model.TerminalMessage;
import cn.vsx.hamster.terminalsdk.tools.Params;
import cn.vsx.uav.R;
import cn.vsx.uav.bean.FileBean;
import cn.vsx.uav.fragment.PlayVideoFragment;
import cn.vsx.uav.fragment.PrePhotoFragment;
import cn.vsx.uav.fragment.UavAllFileFragment;
import cn.vsx.uav.fragment.UavPictureFileFragment;
import cn.vsx.uav.fragment.UavVideoFileFragment;
import cn.vsx.uav.receiveHandler.ReceiveFileSelectChangeHandler;
import cn.vsx.uav.receiveHandler.ReceiveSendFileFinishHandler;
import cn.vsx.uav.receiveHandler.ReceiveShowCheckboxHandler;
import cn.vsx.uav.receiveHandler.ReceiveShowPreViewHandler;
import cn.vsx.vc.activity.BaseActivity;
import cn.vsx.vc.activity.TransponActivity;
import cn.vsx.vc.fragment.BaseFragment;
import cn.vsx.vc.model.ContactItemBean;
import cn.vsx.vc.model.TransponSelectedBean;
import cn.vsx.vc.model.TransponToBean;
import cn.vsx.vc.utils.BitmapUtil;
import cn.vsx.vc.utils.Constants;
import cn.vsx.vc.utils.MyDataUtil;
import cn.vsx.vc.utils.ScreenState;
import cn.vsx.vc.utils.ScreenSwitchUtils;
import ptt.terminalsdk.context.MyTerminalFactory;
import ptt.terminalsdk.tools.HttpUtil;
import ptt.terminalsdk.tools.VideoFileUtil;

/**
 * 作者：ly-xuxiaolong
 * 版本：1.0
 * 创建日期：2019/8/22
 * 描述：
 * 修订历史：
 */
public class UavFileListActivity extends BaseActivity implements View.OnClickListener{

    private RelativeLayout mUavTitle;
    private ImageView mIvBack;
    private TextView mTvUavForward;
    private TextView mTvUavChoice;
    private LinearLayout mLlUavAll;
    private View mVUavAll;
    private LinearLayout mLlUavPicture;
    private View mVUavPicture;
    private LinearLayout mLlUavVideo;
    private View mVUavVideo;
    public static final int CODE_TRANSPON_REQUEST = 0x16;//转发
    private List<FileBean> selectFiles = new ArrayList<>();
    private boolean showCheckbox;
    private BaseFragment currentFragment;
    private UavAllFileFragment uavAllFileFragment;
    private UavPictureFileFragment uavPictureFileFragment;
    private UavVideoFileFragment uavVideoFileFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        //去除title
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //去掉Activity上面的状态栏
        getWindow().setFlags(WindowManager.LayoutParams. FLAG_FULLSCREEN , WindowManager.LayoutParams. FLAG_FULLSCREEN);
        ScreenSwitchUtils.init(this).setPortraitEnable(false);

        super.onCreate(savedInstanceState);
    }

    @Override
    public Resources getResources(){
        return AdaptScreenUtils.adaptWidth(super.getResources(),1200);
    }

    @Override
    protected void setOrientation(){
        int oritation = getIntent().getIntExtra(Constants.ORIENTATION, ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
        setRequestedOrientation(oritation);
    }

    @Override
    public int getLayoutResId(){
        return R.layout.activity_uav_file;
    }

    @Override
    protected void onResume(){
        super.onResume();
        ScreenSwitchUtils.init(this).setCurrentState(ScreenState.getInstanceByCode(getRequestedOrientation()));
        ScreenSwitchUtils.init(this).start(this);
    }

    @Override
    protected void onPause(){
        super.onPause();
        ScreenSwitchUtils.init(this).stop();
    }

    @Override
    public void initView(){
        mUavTitle = findViewById(R.id.uav_title);
        mIvBack = findViewById(R.id.iv_back);
        mTvUavForward = findViewById(R.id.tv_uav_forward);
        mTvUavChoice = findViewById(R.id.tv_uav_choice);
        mLlUavAll = findViewById(R.id.ll_uav_all);
        mVUavAll = findViewById(R.id.v_uav_all);
        mLlUavPicture = findViewById(R.id.ll_uav_picture);
        mVUavPicture = findViewById(R.id.v_uav_picture);
        mLlUavVideo = findViewById(R.id.ll_uav_video);
        mVUavVideo = findViewById(R.id.v_uav_video);
    }

    @Override
    public void initListener(){
        mIvBack.setOnClickListener(this);
        mTvUavChoice.setOnClickListener(this);
        mTvUavForward.setOnClickListener(this);
        mLlUavAll.setOnClickListener(this);
        mLlUavPicture.setOnClickListener(this);
        mLlUavVideo.setOnClickListener(this);
        TerminalFactory.getSDK().registReceiveHandler(receiveFileSelectChangeHandler);
        TerminalFactory.getSDK().registReceiveHandler(receiveShowPreViewHandler);
    }

    @Override
    public void initData(){
        uavAllFileFragment = UavAllFileFragment.newInstance(false);
        getSupportFragmentManager().beginTransaction().add(R.id.uav_file_list_framelayout, uavAllFileFragment,"uavAllFileFragment").commit();
        currentFragment = uavAllFileFragment;
    }

    @Override
    public void doOtherDestroy(){
        AdaptScreenUtils.closeAdapt(getResources());
        TerminalFactory.getSDK().unregistReceiveHandler(receiveFileSelectChangeHandler);
        TerminalFactory.getSDK().unregistReceiveHandler(receiveShowPreViewHandler);
    }

    @Override
    public void onClick(View v){
        if(v.getId() == R.id.iv_back){
            Intent intent = new Intent();
            intent.putExtra(Constants.ORIENTATION,getResources().getConfiguration().orientation);
            setResult(RESULT_OK,intent);
            finish();
        }else if(v.getId() == R.id.tv_uav_choice){
            if(showCheckbox){
                mTvUavChoice.setText(getString(R.string.uav_choice));
                mTvUavForward.setVisibility(View.GONE);
                for(FileBean selectFile : selectFiles){
                    selectFile.setSelected(false);
                }
                showCheckbox = false;
                selectFiles.clear();
                TerminalFactory.getSDK().notifyReceiveHandler(ReceiveShowCheckboxHandler.class,false);
            }else {
                mTvUavChoice.setText(getString(R.string.text_cancel)+"("+selectFiles.size()+")");
                mTvUavForward.setVisibility(View.VISIBLE);
                showCheckbox = true;
                TerminalFactory.getSDK().notifyReceiveHandler(ReceiveShowCheckboxHandler.class,true);
            }
        }else if(v.getId() == R.id.tv_uav_forward){
            if(selectFiles.isEmpty()){
                ToastUtils.showShort(R.string.uav_please_select_file);
            }else {
                Intent intent = new Intent(this, TransponActivity.class);
                intent.putExtra(cn.vsx.vc.utils.Constants.TRANSPON_TYPE, Constants.TRANSPON_TYPE_ONE);
                startActivityForResult(intent, CODE_TRANSPON_REQUEST);
            }
        }else if(v.getId() == R.id.ll_uav_all){
            if(currentFragment != uavAllFileFragment){
                if(uavAllFileFragment !=null){
                    uavAllFileFragment = (UavAllFileFragment) getSupportFragmentManager().findFragmentByTag("uavAllFileFragment");
                }else {
                    uavAllFileFragment = UavAllFileFragment.newInstance(showCheckbox);
                }
                uavAllFileFragment.setShowCheckbox(showCheckbox);
                if(uavAllFileFragment.isAdded()){
                    getSupportFragmentManager().beginTransaction().hide(currentFragment).show(uavAllFileFragment).commit();
                }else {
                    getSupportFragmentManager().beginTransaction().hide(currentFragment).add(R.id.uav_file_list_framelayout,uavAllFileFragment,"uavAllFileFragment").commit();
                }
                currentFragment = uavAllFileFragment;
            }
            mVUavAll.setVisibility(View.VISIBLE);
            mVUavVideo.setVisibility(View.GONE);
            mVUavPicture.setVisibility(View.GONE);
        }else if(v.getId() == R.id.ll_uav_picture){
            if(currentFragment != uavPictureFileFragment){
                if(uavPictureFileFragment != null){
                    uavPictureFileFragment = (UavPictureFileFragment) getSupportFragmentManager().findFragmentByTag("uavPictureFileFragment");
                }else {
                    uavPictureFileFragment = UavPictureFileFragment.newInstance(showCheckbox);
                }
                uavPictureFileFragment.setShowCheckbox(showCheckbox);
                if(uavPictureFileFragment.isAdded()){
                    getSupportFragmentManager().beginTransaction().hide(currentFragment).show(uavPictureFileFragment).commit();
                }else {
                    getSupportFragmentManager().beginTransaction().hide(currentFragment).add(R.id.uav_file_list_framelayout,uavPictureFileFragment,"uavPictureFileFragment").commit();
                }
                currentFragment = uavPictureFileFragment;
            }
            mVUavAll.setVisibility(View.GONE);
            mVUavVideo.setVisibility(View.GONE);
            mVUavPicture.setVisibility(View.VISIBLE);
        }else if(v.getId() == R.id.ll_uav_video){
            if(currentFragment != uavVideoFileFragment){
                if(uavVideoFileFragment !=null){
                    uavVideoFileFragment = (UavVideoFileFragment) getSupportFragmentManager().findFragmentByTag("uavVideoFileFragment");
                }else {
                    uavVideoFileFragment = UavVideoFileFragment.newInstance(showCheckbox);
                }
                uavVideoFileFragment.setShowCheckbox(showCheckbox);
                if(uavVideoFileFragment.isAdded()){
                    getSupportFragmentManager().beginTransaction().hide(currentFragment).show(uavVideoFileFragment).commit();
                }else {
                    getSupportFragmentManager().beginTransaction().hide(currentFragment).add(R.id.uav_file_list_framelayout, uavVideoFileFragment,"uavVideoFileFragment").commit();
                }
                currentFragment = uavVideoFileFragment;
            }
            mVUavAll.setVisibility(View.GONE);
            mVUavVideo.setVisibility(View.VISIBLE);
            mVUavPicture.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == CODE_TRANSPON_REQUEST){
            if (resultCode == RESULT_OK) {
                //转发返回结果
                TransponSelectedBean bean = (TransponSelectedBean) data.getSerializableExtra(cn.vsx.vc.utils.Constants.TRANSPON_SELECTED_BEAN);
                if (bean != null && bean.getList() != null && !bean.getList().isEmpty()) {
                    int type = data.getIntExtra(cn.vsx.vc.utils.Constants.TRANSPON_TYPE, cn.vsx.vc.utils.Constants.TRANSPON_TYPE_ONE);
                    if (type == Constants.TRANSPON_TYPE_ONE) {
                        for(FileBean selectFile : selectFiles){
                            if(!selectFile.isVideo()){

                                JSONObject jsonObject = new JSONObject();
                                jsonObject.put(JsonParam.SEND_STATE, MessageSendStateEnum.SEND_PRE);
                                jsonObject.put(JsonParam.PICTURE_NAME, selectFile.getName());
                                jsonObject.put(JsonParam.PICTURE_SIZE, selectFile.getFileSize());
                                jsonObject.put(JsonParam.TOKEN_ID, MyTerminalFactory.getSDK().getMessageSeq());
                                TerminalMessage mTerminalMessage = new TerminalMessage();
                                mTerminalMessage.messageFromId = MyTerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0);
                                mTerminalMessage.messageFromName = MyTerminalFactory.getSDK().getParam(Params.MEMBER_NAME, "");
                                mTerminalMessage.messagePath = selectFile.getPath();
                                mTerminalMessage.sendTime = System.currentTimeMillis();
                                mTerminalMessage.messageType = MessageType.PICTURE.getCode();
                                mTerminalMessage.messageBody = jsonObject;
                                //单个转发
                                transponMessage(mTerminalMessage,bean.getList());
                            }else {
                                //发送文件
                                JSONObject jsonObject = new JSONObject();
                                jsonObject.put(JsonParam.FILE_NAME, selectFile.getName());
                                jsonObject.put(JsonParam.FILE_SIZE, selectFile.getFileSize());
                                jsonObject.put(JsonParam.SEND_STATE, MessageSendStateEnum.SEND_PRE);
                                jsonObject.put(JsonParam.VIDEO_TIME, VideoFileUtil.getVideoDuration(selectFile.getPath()));
                                jsonObject.put(JsonParam.TOKEN_ID, MyTerminalFactory.getSDK().getMessageSeq());
                                Bitmap bitmap = BitmapUtil.createVideoThumbnail(selectFile.getPath());
                                String picture = HttpUtil.saveFileByBitmap(MyTerminalFactory.getSDK().getPhotoRecordDirectory(), System.currentTimeMillis() + ".jpg", bitmap);
                                jsonObject.put(JsonParam.PICTURE_THUMB_URL, picture);
                                TerminalMessage mTerminalMessage = new TerminalMessage();
                                mTerminalMessage.messageType = MessageType.VIDEO_CLIPS.getCode();
                                mTerminalMessage.sendTime = System.currentTimeMillis();
                                mTerminalMessage.messagePath = selectFile.getPath();
                                mTerminalMessage.messageFromId = MyTerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0);
                                mTerminalMessage.messageFromName = MyTerminalFactory.getSDK().getParam(Params.MEMBER_NAME, "");
                                mTerminalMessage.messageBody = jsonObject;
                                transponMessage(mTerminalMessage,bean.getList());
                            }
                        }
                        //刷新adapter
                        for(FileBean selectFile : selectFiles){
                            selectFile.setSelected(false);
                        }
                        TerminalFactory.getSDK().notifyReceiveHandler(ReceiveSendFileFinishHandler.class);
                        showCheckbox = false;
                        selectFiles.clear();
                        mTvUavChoice.setText(R.string.uav_choice);
                        mTvUavForward.setVisibility(View.GONE);
                    }
                }
            }
        }
    }

    private ReceiveShowPreViewHandler receiveShowPreViewHandler = (show, fileBean) -> myHandler.post(()->{
        if(show){
            mUavTitle.setVisibility(View.GONE);
            if(fileBean.isVideo()){
                PlayVideoFragment playVideoFragment = PlayVideoFragment.newInstance(fileBean);

                getSupportFragmentManager().beginTransaction().hide(currentFragment).add(R.id.uav_pre_file_frame_layout,playVideoFragment).addToBackStack(null).commit();
            }else {
                PrePhotoFragment prePhotoFragment = PrePhotoFragment.newInstance(fileBean);
                getSupportFragmentManager().beginTransaction().hide(currentFragment).add(R.id.uav_pre_file_frame_layout,prePhotoFragment).addToBackStack(null).commit();
            }
        }else {
            mUavTitle.setVisibility(View.VISIBLE);
            getSupportFragmentManager().popBackStack();
        }
    });

    private ReceiveFileSelectChangeHandler receiveFileSelectChangeHandler = (selected, fileBean) -> {
        if(selected){
            if(!selectFiles.contains(fileBean)){
                selectFiles.add(fileBean);
            }
        }else {
            selectFiles.remove(fileBean);
        }
        mTvUavChoice.setText(getString(R.string.text_cancel)+"("+selectFiles.size()+")");
    };

    public void transponMessage(TerminalMessage transponMessage,ArrayList<ContactItemBean> list) {
        //单个转发
        List<Integer> toIds = MyDataUtil.getToIdsTranspon(list);
        TransponToBean bean = MyDataUtil.getToNamesTranspon(list);
        List<Long> toUniqueNos = MyDataUtil.getToUniqueNoTranspon(list);
        if(bean!=null){
            transponMessage.messageToId = bean.getNo();
            transponMessage.messageToName = bean.getName();
        }

        logger.info("发送消息，transponMessage:" + transponMessage);
        transponMessage(transponMessage, toIds,toUniqueNos);
    }

    public List<FileBean> getSelectFileBean(){
        return selectFiles;
    }

    /**
     * 转发图片消息
     **/
    private void transponMessage(TerminalMessage terminalMessage, List<Integer> list , List<Long> toUniqueNos) {
        terminalMessage.messageBody.put(JsonParam.SEND_STATE, MessageSendStateEnum.SENDING);
        File file = new File(terminalMessage.messagePath);
        MyTerminalFactory.getSDK().upload(list,toUniqueNos, file, terminalMessage, true);
    }

    @Override
    public void onBackPressed(){
        Log.e("UavFileListActivity", "getSupportFragmentManager().getBackStackEntryCount():" + getSupportFragmentManager().getBackStackEntryCount());
        if(getSupportFragmentManager().getBackStackEntryCount() !=0){
            mUavTitle.setVisibility(View.VISIBLE);
            getSupportFragmentManager().popBackStack();
        }else {
            Intent intent = new Intent();
            intent.putExtra(Constants.ORIENTATION,getRequestedOrientation());
            setResult(RESULT_OK,intent);
            super.onBackPressed();
        }
    }
}
