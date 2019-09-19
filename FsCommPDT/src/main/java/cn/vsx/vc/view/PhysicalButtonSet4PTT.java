package cn.vsx.vc.view;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveCeaseGroupCallConformationHander;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveChangeGroupHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveForceChangeGroupHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveRequestGroupCallConformationHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveSetCurrentGroupHandler;
import cn.vsx.hamster.terminalsdk.tools.Params;
import cn.vsx.vc.R;
import cn.vsx.vc.activity.SetSecondGroupActivity;
import ptt.terminalsdk.context.MyTerminalFactory;
import skin.support.widget.SkinCompatLinearLayout;

/**
 * 设置界面的“PTT物理按键设置”组件
 * Created by gt358 on 2017/8/8.
 */

public class PhysicalButtonSet4PTT extends SkinCompatLinearLayout{

    LinearLayout ll_voice;

    MToggleButton pptButtonAdd;

    MToggleButton pptButtonCut;

    MToggleButton btn_pttphysicalset;


    TextView tv_pttphysicalset;

    TextView up_ptt;

    TextView down_ptt;
    TextView tv_current_group_name;
    TextView tv_last_group_name;

    private Context context;
    private android.os.Handler myHandler = new android.os.Handler();
    private boolean[] selected;

    public PhysicalButtonSet4PTT(Context context) {
        this(context, null);
    }

    public PhysicalButtonSet4PTT(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PhysicalButtonSet4PTT(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        initView();
        initData();
        initLinstener();
    }

    private void initView () {
//        String infServie = Context.LAYOUT_INFLATER_SERVICE;
//        layoutInflater =  (LayoutInflater) context.getSystemService(infServie);
        LayoutInflater layoutInflater = LayoutInflater.from(context);

        View view = layoutInflater.inflate(R.layout.layout_physicalset, this, true);
        ll_voice = view.findViewById(R.id.ll_voice);
        pptButtonAdd = view.findViewById(R.id.ppt_button_add);
        pptButtonCut = view.findViewById(R.id.ppt_button_cut);
        btn_pttphysicalset = view.findViewById(R.id.btn_pttphysicalset);
        tv_pttphysicalset = view.findViewById(R.id.tv_pttphysicalset);
        up_ptt = view.findViewById(R.id.up_ptt);
        down_ptt = view.findViewById(R.id.down_ptt);
        tv_current_group_name = view.findViewById(R.id.tv_current_group_name);
        tv_last_group_name = view.findViewById(R.id.tv_last_group_name);


    }

    private void initData () {
        if(!MyTerminalFactory.getSDK().contains(Params.SHOW_PTT_PHYSICAL_SET)) {
            //当第一次进入或者删除数据之后，初始化ptt物理按键设置按钮的状态
            MyTerminalFactory.getSDK().putParam(Params.SHOW_PTT_PHYSICAL_SET, false);
            MyTerminalFactory.getSDK().putParam(Params.VOLUME_UP, false);
            MyTerminalFactory.getSDK().putParam(Params.VOLUME_DOWN, false);
        }

        selected = new boolean[]{MyTerminalFactory.getSDK().getParam(Params.SHOW_PTT_PHYSICAL_SET, false),MyTerminalFactory.getSDK().getParam(Params.VOLUME_UP, false), MyTerminalFactory.getSDK().getParam(Params.VOLUME_DOWN, false)};
        int item = MyTerminalFactory.getSDK().getParam(Params.PTTFLOAT_HIDE_OR_SHOW, 0);//0隐藏/1显示
        btn_pttphysicalset.initToggleState(selected[0]);
        pptButtonAdd.initToggleState(selected[1]);
        pptButtonCut.initToggleState(selected[2]);
        if(selected[0]) {
//            tv_pttphysicalset.setTextColor(context.getResources().getColor(R.color.setting_text_black));
            ll_voice.setVisibility(View.VISIBLE);
            if (selected[1]){
//                up_ptt.setTextColor(context.getResources().getColor(R.color.setting_text_black));
            }else {
//                up_ptt.setTextColor(context.getResources().getColor(R.color.setting_text_gray));
            }
            if (selected[2]){
//                down_ptt.setTextColor(context.getResources().getColor(R.color.setting_text_black));
            }else {
//                down_ptt.setTextColor(context.getResources().getColor(R.color.setting_text_gray));
            }
        }else {
//            tv_pttphysicalset.setTextColor(context.getResources().getColor(R.color.setting_text_gray));
            ll_voice.setVisibility(View.GONE);
        }
        setLastGroupName();
        setCurrentGroupName();
    }

    private void setLastGroupName(){
        int lastGroupId = TerminalFactory.getSDK().getParam(Params.OLD_CURRENT_GROUP_ID, 0);
        Log.e("PhysicalButtonSet4PTT", "lastGroupId:" + lastGroupId);
        if(lastGroupId == 0){
            //没有设置上一个当前组,下音量键关闭
            pptButtonCut.initToggleState(false);
        }else {
            tv_last_group_name.setText(TerminalFactory.getSDK().getGroupByGroupNo(lastGroupId).getName());
        }
    }

    private void setCurrentGroupName(){
        int currentGroupId = TerminalFactory.getSDK().getParam(Params.CURRENT_GROUP_ID, 0);
        tv_current_group_name.setText(TerminalFactory.getSDK().getGroupByGroupNo(currentGroupId).getName());
    }

    private void initLinstener () {
        MyTerminalFactory.getSDK().registReceiveHandler(receiveChangeGroupHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveForceChangeGroupHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveSetCurrentGroupHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveRequestGroupCallConformationHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveCeaseGroupCallConformationHander);

        //上音量键
        pptButtonAdd.setOnBtnClick(currState -> {
            selected[1] = currState;
            if (currState) {
//                    up_ptt.setTextColor(context.getResources().getColor(R.color.setting_text_black));
                MyTerminalFactory.getSDK().putParam(Params.VOLUME_UP, true);
            } else {
//                    up_ptt.setTextColor(context.getResources().getColor(R.color.setting_text_gray));
                MyTerminalFactory.getSDK().putParam(Params.VOLUME_UP, false);
                if(!selected[2]) {
//                        tv_pttphysicalset.setTextColor(context.getResources().getColor(R.color.setting_text_gray));
                    ll_voice.setVisibility(View.GONE);
                    btn_pttphysicalset.initToggleState(false);
                    MyTerminalFactory.getSDK().putParam(Params.SHOW_PTT_PHYSICAL_SET, false);
                }
            }
        });
        //下音量键
        pptButtonCut.setOnBtnClick(currState -> {
            selected[2] = currState;
            if (currState) {
                //如果打开时没有上一个组，弹窗选择
                int lastGroupId = TerminalFactory.getSDK().getParam(Params.OLD_CURRENT_GROUP_ID, 0);
                if(lastGroupId == 0){
                    Intent intent = new Intent(context, SetSecondGroupActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                }else {
                    tv_last_group_name.setText(TerminalFactory.getSDK().getGroupByGroupNo(lastGroupId).getName());
                    //                    down_ptt.setTextColor(context.getResources().getColor(R.color.setting_text_black));
                    MyTerminalFactory.getSDK().putParam(Params.VOLUME_DOWN, true);
                }
            } else {
//                    down_ptt.setTextColor(context.getResources().getColor(R.color.setting_text_gray));
                tv_last_group_name.setText("");
                MyTerminalFactory.getSDK().putParam(Params.VOLUME_DOWN, false);
                if(!selected[1]) {
//                        tv_pttphysicalset.setTextColor(context.getResources().getColor(R.color.setting_text_gray));
                    ll_voice.setVisibility(View.GONE);
                    btn_pttphysicalset.initToggleState(false);
                    MyTerminalFactory.getSDK().putParam(Params.SHOW_PTT_PHYSICAL_SET, false);
                }
            }
        });

        btn_pttphysicalset.setOnBtnClick(currState -> {
            selected[1] = currState;
            selected[2] = currState;
            if(currState) {
//                    tv_pttphysicalset.setTextColor(context.getResources().getColor(R.color.setting_text_black));
//                    down_ptt.setTextColor(context.getResources().getColor(R.color.setting_text_black));
//                    up_ptt.setTextColor(context.getResources().getColor(R.color.setting_text_black));
                ll_voice.setVisibility(View.VISIBLE);
                pptButtonAdd.initToggleState(true);
                pptButtonCut.initToggleState(true);
                MyTerminalFactory.getSDK().putParam(Params.SHOW_PTT_PHYSICAL_SET, true);
                MyTerminalFactory.getSDK().putParam(Params.VOLUME_UP, true);
                MyTerminalFactory.getSDK().putParam(Params.VOLUME_DOWN, true);
            }
            else {
//                    tv_pttphysicalset.setTextColor(context.getResources().getColor(R.color.setting_text_gray));
                ll_voice.setVisibility(View.GONE);
                MyTerminalFactory.getSDK().putParam(Params.SHOW_PTT_PHYSICAL_SET, false);
                MyTerminalFactory.getSDK().putParam(Params.VOLUME_UP, false);
                MyTerminalFactory.getSDK().putParam(Params.VOLUME_DOWN, false);
            }
        });
        tv_last_group_name.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View view){
                Intent intent = new Intent(context, SetSecondGroupActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        });
    }

    /**
     * 转组
     */
    private ReceiveChangeGroupHandler receiveChangeGroupHandler = new ReceiveChangeGroupHandler(){
        @Override
        public void handler(int errorCode, String errorDesc){
            myHandler.post(()->{
                setLastGroupName();
                setCurrentGroupName();
            });
        }
    };

    private ReceiveForceChangeGroupHandler receiveForceChangeGroupHandler = new ReceiveForceChangeGroupHandler(){
        @Override
        public void handler(int memberId, int toGroupId, boolean forceSwitchGroup, String tempGroupType){
            myHandler.post(()->{
                setLastGroupName();
                setCurrentGroupName();
            });
        }
    };

    private ReceiveSetCurrentGroupHandler receiveSetCurrentGroupHandler = new ReceiveSetCurrentGroupHandler(){
        @Override
        public void handler(int currentGroupId, int errorCode, String errorDesc){
            myHandler.post(()->{
                setLastGroupName();
                setCurrentGroupName();
            });
        }
    };

    /**主动方请求组呼的消息*/
    private ReceiveRequestGroupCallConformationHandler receiveRequestGroupCallConformationHandler = new ReceiveRequestGroupCallConformationHandler() {
        @Override
        public void handler(final int methodResult, String resultDesc,int groupId) {
            myHandler.post(() -> {
                if (methodResult == 0) {//请求成功，开始组呼
                    pptButtonCut.setEnabled(false);
                    pptButtonAdd.setEnabled(false);
                    btn_pttphysicalset.setEnabled(false);
                }
            });
        }
    };

    private ReceiveCeaseGroupCallConformationHander receiveCeaseGroupCallConformationHander = new ReceiveCeaseGroupCallConformationHander() {

        @Override
        public void handler(int resultCode, String resultDesc) {
            myHandler.post(() -> {
                pptButtonCut.setEnabled(true);
                pptButtonAdd.setEnabled(true);
                btn_pttphysicalset.setEnabled(true);
            });
        }
    };
    public void unregist(){
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveChangeGroupHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveForceChangeGroupHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveSetCurrentGroupHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveRequestGroupCallConformationHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveCeaseGroupCallConformationHander);
    }
}
