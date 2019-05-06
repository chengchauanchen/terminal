package cn.vsx.vc.view.view4modularization;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.apache.log4j.Logger;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.vsx.hamster.common.MemberChangeType;
import cn.vsx.hamster.common.UrlParams;
import cn.vsx.hamster.errcode.BaseCommonCode;
import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveChangeNameHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveNotifyMemberChangeHandler;
import cn.vsx.hamster.terminalsdk.tools.Params;
import cn.vsx.vc.R;
import cn.vsx.vc.activity.UserInfoActivity;
import cn.vsx.vc.utils.HandleIdUtil;
import ptt.terminalsdk.context.MyTerminalFactory;

/**
 * 设置界面----个人信息模块
 * Created by gt358 on 2017/8/16.
 */

public class PersonInfoLayout extends LinearLayout {
    @Bind(R.id.user_logo)
    ImageView userLogo;
    @Bind(R.id.user_name)
    TextView userName;
    @Bind(R.id.user_id)
    TextView userId;
    private Logger logger = Logger.getLogger(getClass());
    private Handler myHandler = new Handler();
    public PersonInfoLayout(Context context) {
        this(context, null);
    }

    public PersonInfoLayout(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PersonInfoLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
        initListener();
    }

    private void initView (Context context) {
        String infServie = Context.LAYOUT_INFLATER_SERVICE;
        LayoutInflater layoutInflater;
        layoutInflater =  (LayoutInflater) getContext().getSystemService(infServie);
        View view = layoutInflater.inflate(R.layout.layout_personinfo, this, true);
        ButterKnife.bind(this, view);
        userId.setText("警号:"+ HandleIdUtil.handleId(MyTerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0)));
        userName.setText(MyTerminalFactory.getSDK().getParam(Params.MEMBER_NAME, ""));
        logger.info("用户头像："+TerminalFactory.getSDK().getParam(UrlParams.AVATAR_URL));
        Glide.with(context)
                .load(TerminalFactory.getSDK().getParam(UrlParams.AVATAR_URL))
                .asBitmap()
                .placeholder(R.drawable.user_photo)//加载中显示的图片
                .error(R.drawable.user_photo)//加载失败时显示的图片
                .into(userLogo);
    }

    private void initListener () {
//        MyTerminalFactory.getSDK().registReceiveHandler(receiveChangeNameHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveNotifyMemberChangeHandler);
    }

    public void unInitListener () {
//        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveChangeNameHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveNotifyMemberChangeHandler);
    }


    /**收到修改名字成功的消息*/
    private ReceiveChangeNameHandler receiveChangeNameHandler = new ReceiveChangeNameHandler(){
        @Override
        public void handler(final int resultCode, final int memberId, final String newMemberName) {
            myHandler.post(() -> {
                if (resultCode == BaseCommonCode.SUCCESS_CODE && memberId == TerminalFactory.getSDK().getParam(Params.MEMBER_ID,0)){
                    userName.setText(newMemberName);
                }
            });
        }
    };

    /**更新所有成员列表*/
    private ReceiveNotifyMemberChangeHandler receiveNotifyMemberChangeHandler = new ReceiveNotifyMemberChangeHandler() {
        @Override
        public void handler(final MemberChangeType memberChangeType) {
            myHandler.post(() -> userName.setText(TerminalFactory.getSDK().getParam(Params.MEMBER_NAME, "")));
        }
    };

    @OnClick(R.id.ll_user_info)
    public void toUserInfoActivity (View view) {
        switch (view.getId()) {
            case R.id.ll_user_info:
                Intent intent = new Intent(getContext(), UserInfoActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("userId", MyTerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0));
                intent.putExtra("userName", userName.getText().toString().trim());
                getContext().startActivity(intent);
//                OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiverShowPopupwindowHandler.class, ChangeNamePopupwindow.class.getTitleName());
                break;
        }

    }
}
