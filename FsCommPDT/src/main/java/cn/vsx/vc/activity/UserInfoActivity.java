package cn.vsx.vc.activity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
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
import cn.vsx.hamster.common.MemberChangeType;
import cn.vsx.hamster.errcode.BaseCommonCode;
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
import ptt.terminalsdk.tools.PhoneAdapter;
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
//    @Bind(R.id.user_ID_number)
//    TextView user_ID_number;
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
    private UserInfoMenuAdapter mAdapter;

    private Member member;
    private String userName;
    private String avatarUrl;
    private int userId;
    private int VOIP=0;
    private int TELEPHONE=1;

    @Override
    public int getLayoutResId() {
        return R.layout.activity_user_info;
    }

    @Override
    public void initView() {
        barTitle.setText("个人信息");
        rightBtn.setVisibility(View.GONE);
        btnOk.setVisibility(View.GONE);
//        OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiveVolumeOffCallHandler.class, true,0);
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
        avatarUrl = getIntent().getStringExtra("avatarUrl");
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
        mAdapter=new UserInfoMenuAdapter(UserInfoActivity.this,mPictures);

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


        mAdapter.setOnItemClickListener(new UserInfoMenuAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int postion, Picture picture) {
                if (picture.getTitle().equals("发消息")){
                    IndividualNewsActivity.startCurrentActivity(UserInfoActivity.this, userId, userName);

                }else if (picture.getTitle().equals("个呼")){
                    if(!MyTerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_CALL_PRIVATE.name())){
                        ToastUtil.showToast(UserInfoActivity.this,"没有个呼功能权限");
                    }else {
                        activeIndividualCall();
                    }


                } else if (picture.getTitle().equals("电话")) {
                    if (!TextUtils.isEmpty(member.phone)) {
                        ItemAdapter adapter = new ItemAdapter(UserInfoActivity.this, cn.vsx.vc.adapter.ItemAdapter.iniDatas());
                        AlertDialog.Builder builder = new AlertDialog.Builder(UserInfoActivity.this);
                        //设置标题
                        builder.setTitle("拨打电话");
                        builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int position) {
                                if (position == VOIP) {//voip电话
                                    if (MyTerminalFactory.getSDK().getParam(Params.VOIP_SUCCESS, false)) {
                                        Intent intent = new Intent(UserInfoActivity.this, VoipPhoneActivity.class);
                                        intent.putExtra("member", member);
                                        UserInfoActivity.this.startActivity(intent);
                                    } else {
                                        ToastUtil.showToast(UserInfoActivity.this, "voip注册失败，请检查服务器配置");
                                    }
                                } else if (position == TELEPHONE) {//普通电话

                                    CallPhoneUtil.callPhone(UserInfoActivity.this, member.phone);

                                }

                            }
                        });
                        builder.create();
                        builder.show();
                    } else {
                        ToastUtil.showToast(UserInfoActivity.this, "暂无该用户电话号码");
                    }
                }else if (picture.getTitle().equals("图像回传")){
                    if(!MyTerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_VIDEO_ASK.name())){
                        ToastUtil.showToast(UserInfoActivity.this,"没有图像请求功能权限");
                    }else {
                        pullVideo();
                    }

                }else if (picture.getTitle().equals("图像上报")){
                    if(!MyTerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_VIDEO_UP.name())){
                        ToastUtil.showToast(UserInfoActivity.this,"没有图像上报功能权限");
                    }else {
                        pushVideo();
                    }
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

        List<Integer> memberIds = new ArrayList<>();
        memberIds.add(userId);
        OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiverActivePushVideoHandler.class, memberIds);
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
            int resultCode = MyTerminalFactory.getSDK().getIndividualCallManager().requestIndividualCall(userId, "");
            if (resultCode == BaseCommonCode.SUCCESS_CODE) {
                if (!PhoneAdapter.isF25()) {
                }
                if(member!=null){
                    OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiveCurrentGroupIndividualCallHandler.class, member);
                    MyApplication.instance.isPopupWindowShow = true;
                }else {
                    ToastUtil.showToast(UserInfoActivity.this,"获取人员信息失败");
                }
            }else {
                ToastUtil.individualCallFailToast(UserInfoActivity.this, resultCode);
//                ToastUtil.showToast(this, "组成员列表。。。请求个呼失败");
            }
        } else {
            ToastUtil.showToast(this, "网络连接异常，请检查网络！");
        }
    }

    @OnClick({R.id.add_note,  R.id.news_bar_back})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.news_bar_back:
                finish();
                break;
//            case R.id.add_note:
//                if (!DataUtil.isExistContacts(member)) {//不在个呼通讯录中
//                    MyTerminalFactory.getSDK().getContactsManager().modifyContacts(Operation4PrivateAddressList.ADD.getCode(),member.id);
//                    add_note.setText("从通讯录移除");
//                }else {
//                    if (member.id != MyTerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0)) {//自己不能移除
//                        MyTerminalFactory.getSDK().getContactsManager().modifyContacts(Operation4PrivateAddressList.REMOVE.getCode(),member.id);
//                        add_note.setText("添加到通讯录 ");
//                    }
//                }
//                break;
        }
    }

    private void setNoVideo(){
        mPictures.clear();
        mPictures.add(new Picture("发消息",R.drawable.ic_message));
        mPictures.add(new Picture("电话",R.drawable.ic_mobile));
        mPictures.add(new Picture("个呼",R.drawable.ic_call));
    }
    
    private void setHasVideo(){
        mPictures.clear();
        mPictures.add(new Picture("发消息",R.drawable.ic_message));
        mPictures.add(new Picture("个呼",R.drawable.ic_call));
        mPictures.add(new Picture("电话",R.drawable.ic_mobile));
        mPictures.add(new Picture("图像上报",R.drawable.ic_picture_up));
        mPictures.add(new Picture("图像回传",R.drawable.ic_picture_back));
    }
    private Handler myHandler = new Handler();
    /**更新所有成员列表*/
    private ReceiveNotifyMemberChangeHandler receiveNotifyMemberChangeHandler = new ReceiveNotifyMemberChangeHandler() {
        @Override
        public void handler(final MemberChangeType memberChangeType) {
            myHandler.post(new Runnable() {
                @Override
                public void run() {
                    initBottomMenu();
                }
            });
        }
    };

    private ReceiveUpdateConfigHandler receiveUpdateConfigHandler = new ReceiveUpdateConfigHandler() {
        @Override
        public void handler() {
            myHandler.post(new Runnable() {
                @Override
                public void run() {
                    userPhone.setText(MyTerminalFactory.getSDK().getParam(Params.PHONE_NO, ""));
                }
            });
        }
    };

}
