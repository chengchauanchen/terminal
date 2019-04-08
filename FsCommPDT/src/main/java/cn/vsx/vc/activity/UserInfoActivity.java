package cn.vsx.vc.activity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
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

import com.bumptech.glide.Glide;
import com.zectec.imageandfileselector.utils.OperateReceiveHandlerUtilSync;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;
import cn.vsx.hamster.common.Authority;
import cn.vsx.hamster.terminalsdk.model.Member;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveCurrentGroupIndividualCallHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveNotifyMemberChangeHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveUpdateConfigHandler;
import cn.vsx.hamster.terminalsdk.tools.Params;
import cn.vsx.vc.R;
import cn.vsx.vc.adapter.ItemAdapter;
import cn.vsx.vc.adapter.UserInfoMenuAdapter;
import cn.vsx.vc.application.MyApplication;
import cn.vsx.vc.model.Picture;
import cn.vsx.vc.receiveHandle.ReceiverActivePushVideoHandler;
import cn.vsx.vc.receiveHandle.ReceiverRequestVideoHandler;
import cn.vsx.vc.utils.CallPhoneUtil;
import cn.vsx.vc.utils.DataUtil;
import cn.vsx.vc.utils.HandleIdUtil;
import cn.vsx.vc.view.VolumeViewLayout;
import ptt.terminalsdk.context.MyTerminalFactory;
import ptt.terminalsdk.manager.audio.CheckMyPermission;
import ptt.terminalsdk.tools.ToastUtil;

/**
 * Created by Administrator on 2017/3/16 0016.
 */

public class UserInfoActivity extends BaseActivity {
    @Bind(R.id.ok_btn)
    Button btnOk;
    @Bind(R.id.user_logo)
    ImageView userLogo;
    @Bind(R.id.user_name)
    TextView user_Name;
    @Bind(R.id.user_no)
    TextView user_no;
    @Bind(R.id.user_address)
    LinearLayout userAddress;
    @Bind(R.id.user_phone)
    TextView userPhone;
    @Bind(R.id.news_bar_back)
    ImageView newsBarBack;
    @Bind(R.id.bar_title)
    TextView barTitle;
    @Bind(R.id.right_btn)
    ImageView rightBtn;
    @Bind(R.id.add_note)
    TextView add_note;
    @Bind(R.id.volume_layout)
    VolumeViewLayout volumeViewLayout;
    @Bind(R.id.recyclerview)
    RecyclerView mRecyclerView;
    @Bind(R.id.tv_unit)
    TextView tvUnit;


    private List<Picture> mPictures=new ArrayList<>();

    private Member member;
    private String userName;
    private int userId;
    private int VOIP=0;
    private int TELEPHONE=1;

    @Override
    public int getLayoutResId() {
        return R.layout.activity_user_info;
    }

    @Override
    public void initView() {
        barTitle.setText(R.string.text_personal_information);
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
        member = DataUtil.getMemberByMemberNo(userId);


        if (!TextUtils.isEmpty(member.getDepartmentName())){
            tvUnit.setText(member.getDepartmentName());
        }
        user_no.setText(HandleIdUtil.handleId(member.getNo()));
        user_Name.setText(HandleIdUtil.handleName(userName));
        if(!TextUtils.isEmpty(member.getPhone())){
            userPhone.setText(member.getPhone());
        }

        Glide.with(UserInfoActivity.this).load(DataUtil.getMemberByMemberNo(userId).avatarUrl).asBitmap().placeholder(R.drawable.user_photo)//加载中显示的图片
                .error(R.drawable.user_photo)//加载失败时显示的图片
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
                IndividualNewsActivity.startCurrentActivity(UserInfoActivity.this, userId, userName);

            }else if (picture.getTitle().equals("个呼")){
                if(!MyTerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_CALL_PRIVATE.name())){
                    ToastUtil.showToast(UserInfoActivity.this,getString(R.string.text_no_call_permission));
                }else {
                    activeIndividualCall();
                }


            } else if (picture.getTitle().equals("电话")) {
                if (!TextUtils.isEmpty(member.phone)) {
                    ItemAdapter adapter = new ItemAdapter(UserInfoActivity.this, ItemAdapter.iniDatas());
                    AlertDialog.Builder builder = new AlertDialog.Builder(UserInfoActivity.this);
                    //设置标题
                    builder.setTitle("拨打电话");
                    builder.setAdapter(adapter, (dialogInterface, position) -> {
                        if (position == VOIP) {//voip电话
                            if (MyTerminalFactory.getSDK().getParam(Params.VOIP_SUCCESS, false)) {
                                Intent intent = new Intent(UserInfoActivity.this, VoipPhoneActivity.class);
                                intent.putExtra("member", member);
                                UserInfoActivity.this.startActivity(intent);
                            } else {
                                ToastUtil.showToast(UserInfoActivity.this, getString(R.string.text_voip_regist_fail_please_check_server_configure));
                            }
                        } else if (position == TELEPHONE) {//普通电话

                            CallPhoneUtil.callPhone(UserInfoActivity.this, member.phone);

                        }

                    });
                    builder.create();
                    builder.show();
                } else {
                    ToastUtil.showToast(UserInfoActivity.this, getString(R.string.text_has_no_member_phone_number));
                }
            }else if (picture.getTitle().equals("图像回传")){
                if(!MyTerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_VIDEO_ASK.name())){
                    ToastUtil.showToast(UserInfoActivity.this,getString(R.string.text_has_no_image_request_authority));
                }else {
                    pullVideo();
                }

            }else if (picture.getTitle().equals("图像上报")){
                if(!MyTerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_VIDEO_UP.name())){
                    ToastUtil.showToast(UserInfoActivity.this,getString(R.string.text_has_no_image_report_authority));
                }else {
                    pushVideo();
                }
            }
        });

    }

    private void pullVideo() {
        if (!CheckMyPermission.selfPermissionGranted(UserInfoActivity.this, Manifest.permission.RECORD_AUDIO)) {//没有录音权限
            CheckMyPermission.permissionPrompt((Activity) UserInfoActivity.this, Manifest.permission.RECORD_AUDIO);
            return;
        }
        OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiverRequestVideoHandler.class, member);
    }

    private void pushVideo() {
        if (!CheckMyPermission.selfPermissionGranted(UserInfoActivity.this, Manifest.permission.RECORD_AUDIO)) {//没有录音权限
            CheckMyPermission.permissionPrompt( UserInfoActivity.this, Manifest.permission.RECORD_AUDIO);
            return;
        }
        if (!CheckMyPermission.selfPermissionGranted(UserInfoActivity.this, Manifest.permission.CAMERA)) {//没有相机权限
            CheckMyPermission.permissionPrompt(UserInfoActivity.this, Manifest.permission.CAMERA);
            return;
        }


        OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiverActivePushVideoHandler.class, userId,false);
    }


    @Override
    public void doOtherDestroy() {
        if (volumeViewLayout!= null){
            volumeViewLayout.unRegistLintener();
        }
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveNotifyMemberChangeHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveUpdateConfigHandler);
    }

    /**
     * 请求个呼
     */
    private void activeIndividualCall() {


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

    @OnClick({R.id.add_note,  R.id.news_bar_back})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.news_bar_back:
                finish();
                break;
        }
    }

    private void setNoVideo(){
        mPictures.clear();
        mPictures.add(new Picture(getString(R.string.text_send_message),R.drawable.ic_message));
        mPictures.add(new Picture(getString(R.string.text_phone),R.drawable.ic_mobile));
        mPictures.add(new Picture(getString(R.string.text_personal_call),R.drawable.ic_call));
    }

    private void setHasVideo(){
        mPictures.clear();
        mPictures.add(new Picture(getString(R.string.text_send_message),R.drawable.ic_message));
        mPictures.add(new Picture(getString(R.string.text_personal_call),R.drawable.ic_call));
        mPictures.add(new Picture(getString(R.string.text_phone),R.drawable.ic_mobile));
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
