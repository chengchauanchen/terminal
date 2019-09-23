package cn.vsx.vc.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.blankj.utilcode.util.ToastUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.zectec.imageandfileselector.utils.OperateReceiveHandlerUtilSync;

import java.util.ArrayList;
import java.util.List;

import cn.vsx.hamster.common.Authority;
import cn.vsx.hamster.common.ReceiveObjectMode;
import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.model.Account;
import cn.vsx.hamster.terminalsdk.model.Member;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveCurrentGroupIndividualCallHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveNotifyMemberChangeHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveUpdateConfigHandler;
import cn.vsx.hamster.terminalsdk.tools.DataUtil;
import cn.vsx.hamster.terminalsdk.tools.Params;
import cn.vsx.vc.R;
import cn.vsx.vc.adapter.UserInfoMenuAdapter;
import cn.vsx.vc.application.MyApplication;
import cn.vsx.vc.dialog.ChooseDevicesDialog;
import cn.vsx.vc.model.Picture;
import cn.vsx.vc.receiveHandle.ReceiverActivePushVideoHandler;
import cn.vsx.vc.receiveHandle.ReceiverRequestVideoHandler;
import cn.vsx.vc.utils.BitmapUtil;
import cn.vsx.vc.utils.CallPhoneUtil;
import cn.vsx.vc.utils.HandleIdUtil;
import cn.vsx.vc.utils.MyDataUtil;
import cn.vsx.vc.view.VolumeViewLayout;
import ptt.terminalsdk.context.MyTerminalFactory;
import ptt.terminalsdk.manager.audio.CheckMyPermission;
import ptt.terminalsdk.tools.ToastUtil;

/**
 * Created by Administrator on 2017/3/16 0016.
 */

public class UserInfoActivity extends BaseActivity implements View.OnClickListener{

    Button btnOk;

    ImageView userLogo;

    TextView user_Name;

    TextView user_no;

    LinearLayout userAddress;

    TextView userPhone;

    ImageView newsBarBack;

    TextView barTitle;

    ImageView rightBtn;

    TextView add_note;

    VolumeViewLayout volumeViewLayout;

    RecyclerView mRecyclerView;

    TextView tvUnit;


    private List<Picture> mPictures=new ArrayList<>();

    private Account account;
    private String userName;
    private int userId;

    @Override
    public int getLayoutResId() {
        return R.layout.activity_user_info;
    }

    @Override
    public void initView() {
        tvUnit = (TextView) findViewById(R.id.tv_unit);
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerview);
        volumeViewLayout = (VolumeViewLayout) findViewById(R.id.volume_layout);
        add_note = (TextView) findViewById(R.id.add_note);
        rightBtn = (ImageView) findViewById(R.id.right_btn);
        barTitle = (TextView) findViewById(R.id.bar_title);
        newsBarBack = (ImageView) findViewById(R.id.news_bar_back);
        userPhone = (TextView) findViewById(R.id.user_phone);
        userAddress = (LinearLayout) findViewById(R.id.user_address);
        user_no = (TextView) findViewById(R.id.user_no);
        user_Name = (TextView) findViewById(R.id.user_name);
        userLogo = (ImageView) findViewById(R.id.user_logo);
        btnOk = (Button) findViewById(R.id.ok_btn);
        barTitle.setText(R.string.text_personal_information);
        findViewById(R.id.news_bar_back).setOnClickListener(this);
        findViewById(R.id.user_phone).setOnClickListener(this);
        rightBtn.setVisibility(View.GONE);
        btnOk.setVisibility(View.GONE);
    }

    @Override
    public void initListener() {
        MyTerminalFactory.getSDK().registReceiveHandler(receiveNotifyMemberChangeHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveUpdateConfigHandler);
    }

    @Override
    public void initData() {
        userId = getIntent().getIntExtra("userId", 0);
        userName = getIntent().getStringExtra("userName");
        String avatarUrl = getIntent().getStringExtra("avatarUrl");
        loadData();
    }

    /**
     * 获取Account数据
     */
    private void loadData(){
        showProgressDialog();
        TerminalFactory.getSDK().getThreadPool().execute(() -> {
            account = DataUtil.getAccountByMemberNo(userId,true);
            myHandler.post(() -> {
                dismissProgressDialog();
                if(account == null){
                    ToastUtil.showToast(UserInfoActivity.this,getString(R.string.text_has_no_found_this_user));
                    myHandler.postDelayed(() -> finish(),500);
                    return;
                }else{
                    setViewData();
                }
            });
        });

    }

    /**
     * 布局设置数据
     */
    private void setViewData() {
        if (!TextUtils.isEmpty(account.getDepartmentName())){
            tvUnit.setText(account.getDepartmentName());
        }
        user_no.setText(HandleIdUtil.handleId(account.getNo()));
        user_Name.setText(HandleIdUtil.handleName(userName));
        if(!TextUtils.isEmpty(account.getPhone())){
            userPhone.setText(account.getPhone());
        }
        int drawable = BitmapUtil.getUserPhoto();
//        Glide.with(UserInfoActivity.this)
//                .load(drawable)
//                .asBitmap()
//                .placeholder(drawable)//加载中显示的图片
//                .error(drawable)//加载失败时显示的图片
//                .into(userLogo);

        Glide.with(UserInfoActivity.this)
                .load(drawable)
                .apply(new RequestOptions().placeholder(drawable).error(drawable))
                .into(userLogo);

        initBottomMenu();
    }

    private void initBottomMenu() {
        mRecyclerView.setLayoutManager(new GridLayoutManager(UserInfoActivity.this,4));
        UserInfoMenuAdapter mAdapter = new UserInfoMenuAdapter(UserInfoActivity.this, mPictures);

        if (userId == MyTerminalFactory.getSDK().getParam(Params.MEMBER_ID,0)){//如果是自己，则不显示操作菜单
            mRecyclerView.setVisibility(View.GONE);
        }else {

            setNoVideo();
            for(Member member :MyTerminalFactory.getSDK().getConfigManager().getCurrentGroupMembers()){
                if(member.id==userId){
                    setHasVideo();
                }
            }
            logger.info("个人信息界面ICON"+mPictures.toString());
            mRecyclerView.setAdapter(mAdapter);
        }

        mAdapter.setOnItemClickListener((postion, picture) -> {
            if (picture.getTitle().equals("发消息")){
                IndividualNewsActivity.startCurrentActivity(UserInfoActivity.this, userId, userName,0);

            }else if (picture.getTitle().equals("个呼")){
                if(!MyTerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_CALL_PRIVATE.name())){
                    ToastUtil.showToast(UserInfoActivity.this,getString(R.string.text_no_call_permission));
                }else {
                    goToChooseDevices(ChooseDevicesDialog.TYPE_CALL_PRIVATE);
                }
            } else if (picture.getTitle().equals("电话")) {
                goToChooseDevices(ChooseDevicesDialog.TYPE_CALL_PHONE);
            }else if (picture.getTitle().equals("图像回传")){
                if(!MyTerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_VIDEO_ASK.name())){
                    ToastUtil.showToast(UserInfoActivity.this,getString(R.string.text_has_no_image_request_authority));
                }else {
                    goToChooseDevices(ChooseDevicesDialog.TYPE_PULL_LIVE);
                }
            }else if (picture.getTitle().equals("图像上报")){
                if(!MyTerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_VIDEO_UP.name())){
                    ToastUtil.showToast(UserInfoActivity.this,getString(R.string.text_has_no_image_report_authority));
                }else {
                    goToChooseDevices(ChooseDevicesDialog.TYPE_PUSH_LIVE);
                }
            }
        });

    }

    /**
     * 选择设备进行相应的操作
     * @param type
     */
    private void goToChooseDevices(int type){
        new ChooseDevicesDialog(this,type, account, (dialog, member) -> {
            switch (type){
                case ChooseDevicesDialog.TYPE_CALL_PRIVATE:
                    activeIndividualCall(member);
                    break;
                case ChooseDevicesDialog.TYPE_CALL_PHONE:
                    goToCall(member);
                    break;
                case ChooseDevicesDialog.TYPE_PULL_LIVE:
                    pullVideo(member);
                    break;
                case ChooseDevicesDialog.TYPE_PUSH_LIVE:
                    pushVideo(member);
                    break;
            }
            dialog.dismiss();
        }).showDialog();
    }

    /**
     * 拨打电话
     */
    private void goToCall(Member member) {
        if(TextUtils.isEmpty(member.getPhone())){
            ToastUtils.showShort(R.string.text_has_no_member_phone_number);
            return;
        }
        if(member.getUniqueNo() == 0){
            //普通电话
            CallPhoneUtil.callPhone( UserInfoActivity.this, member.getPhone());
        }else{
            if(MyTerminalFactory.getSDK().getParam(Params.VOIP_SUCCESS,false)){
                Intent intent = new Intent(UserInfoActivity.this, VoipPhoneActivity.class);
                intent.putExtra("member",member);
                startActivity(intent);
            }else {
                ToastUtil.showToast(UserInfoActivity.this,getString(R.string.text_voip_regist_fail_please_check_server_configure));
            }
        }
    }

    /**
     * 请求图像
     * @param member
     */
    private void pullVideo(Member member) {
        if (!CheckMyPermission.selfPermissionGranted(UserInfoActivity.this, Manifest.permission.RECORD_AUDIO)) {//没有录音权限
            CheckMyPermission.permissionPrompt((Activity) UserInfoActivity.this, Manifest.permission.RECORD_AUDIO);
            return;
        }
        OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiverRequestVideoHandler.class, member);
    }

    /**
     * 上报图像
     * @param member
     */
    private void pushVideo(Member member) {
        if (!CheckMyPermission.selfPermissionGranted(UserInfoActivity.this, Manifest.permission.RECORD_AUDIO)) {//没有录音权限
            CheckMyPermission.permissionPrompt( UserInfoActivity.this, Manifest.permission.RECORD_AUDIO);
            return;
        }
        if (!CheckMyPermission.selfPermissionGranted(UserInfoActivity.this, Manifest.permission.CAMERA)) {//没有相机权限
            CheckMyPermission.permissionPrompt(UserInfoActivity.this, Manifest.permission.CAMERA);
            return;
        }

        OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiverActivePushVideoHandler.class,
                MyDataUtil.getPushInviteMemberData(member.getUniqueNo(), ReceiveObjectMode.MEMBER.toString()),false);
    }


    @Override
    public void doOtherDestroy() {
        if (volumeViewLayout!= null){
            volumeViewLayout.unRegistLintener();
        }
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveNotifyMemberChangeHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveUpdateConfigHandler);
        myHandler.removeCallbacksAndMessages(null);
    }

    /**
     * 请求个呼
     */
    private void activeIndividualCall(Member member) {
        MyApplication.instance.isCallState = true;
        boolean network = MyTerminalFactory.getSDK().hasNetwork();
        if (network) {
            if(member!=null){
                OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiveCurrentGroupIndividualCallHandler.class, member);
            }else {
                ToastUtil.showToast(UserInfoActivity.this,getString(R.string.text_get_personal_info_fail));
            }
        } else {
            ToastUtil.showToast(this, getString(R.string.text_network_connection_abnormal_please_check_the_network));
        }
    }


    public void onClick(View view) {
        int i = view.getId();
        if(i == R.id.news_bar_back){
            finish();
        }else if(i == R.id.user_phone){
            goToChooseDevices(ChooseDevicesDialog.TYPE_CALL_PHONE);
        }
    }

    private void setNoVideo(){
        mPictures.clear();
        mPictures.add(new Picture(getString(R.string.text_send_message),R.drawable.ic_message));
        mPictures.add(new Picture(getString(R.string.text_personal_call),R.drawable.ic_call));
    }

    private void setHasVideo(){
        mPictures.clear();
        mPictures.add(new Picture(getString(R.string.text_send_message),R.drawable.ic_message));
        mPictures.add(new Picture(getString(R.string.text_personal_call),R.drawable.ic_call));
        mPictures.add(new Picture(getString(R.string.text_image_report),R.drawable.ic_picture_up));
        mPictures.add(new Picture(getString(R.string.image_request),R.drawable.ic_picture_back));
    }
    private Handler myHandler = new Handler();
    /**更新所有成员列表*/
    private ReceiveNotifyMemberChangeHandler receiveNotifyMemberChangeHandler = memberChangeType -> myHandler.post(() -> initBottomMenu());

    private ReceiveUpdateConfigHandler receiveUpdateConfigHandler = new ReceiveUpdateConfigHandler() {
        @Override
        public void handler() {
            myHandler.post(() -> {
                if(null != userPhone){
                    userPhone.setText(MyTerminalFactory.getSDK().getParam(Params.PHONE_NO, ""));
                }
            });
        }
    };
}
