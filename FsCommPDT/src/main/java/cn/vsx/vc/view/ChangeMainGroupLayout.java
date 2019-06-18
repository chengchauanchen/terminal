package cn.vsx.vc.view;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.apache.log4j.Logger;



import butterknife.OnClick;
import cn.vsx.hamster.common.Authority;
import cn.vsx.hamster.common.GroupScanType;
import cn.vsx.hamster.errcode.BaseCommonCode;
import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveCeaseGroupCallConformationHander;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveGroupScanResultHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveRequestGroupCallConformationHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveUpdateConfigHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveUpdateFoldersAndGroupsHandler;
import cn.vsx.hamster.terminalsdk.tools.Params;
import cn.vsx.vc.R;
import cn.vsx.vc.activity.SetSweepActivity;
import ptt.terminalsdk.context.MyTerminalFactory;
import ptt.terminalsdk.tools.ToastUtil;
import skin.support.widget.SkinCompatLinearLayout;

/**
 * 更改主通话组和组扫描  组件化
 * Created by gt358 on 2017/8/9.
 */
public class ChangeMainGroupLayout extends SkinCompatLinearLayout implements View.OnClickListener{


    RelativeLayout rl_start_group_sweep;

    MToggleButton openSweep;

    TextView tv_start_group_sweep;

    RelativeLayout rl_group_sweep_setting;

    private Context context;
    private Logger logger = Logger.getLogger(getClass());

    private Handler myHandler = new Handler();
    public ChangeMainGroupLayout(Context context) {
        this(context, null);
    }

    public ChangeMainGroupLayout(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ChangeMainGroupLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        initView();
        initData();
        initListener();
    }

    private void initView () {
        String infServie = Context.LAYOUT_INFLATER_SERVICE;
        LayoutInflater layoutInflater;
        layoutInflater =  (LayoutInflater) getContext().getSystemService(infServie);
        View view = layoutInflater.inflate(R.layout.layout_changemaingroup, this, true);
        rl_start_group_sweep = view.findViewById(R.id.rl_start_group_sweep);
        openSweep = view.findViewById(R.id.open_sweep);
        tv_start_group_sweep = view.findViewById(R.id.tv_start_group_sweep);
        rl_group_sweep_setting = view.findViewById(R.id.rl_group_sweep_setting);
        rl_group_sweep_setting.setOnClickListener(this);
    }

    private void initData () {
//        work_group_name.setText(DataUtil.getGroupByGroupId(MyTerminalFactory.getSDK().getParam(Params.MAIN_GROUP_ID, 0)).name);
        if (TerminalFactory.getSDK().getParam(Params.GROUP_SCAN, false)) {
            setGroupScanBlack();
            setGuardMainGroupBlack();
        } else {
            setGroupScanGray();
            if (TerminalFactory.getSDK().getParam(Params.GUARD_MAIN_GROUP, false)) {
                setGuardMainGroupBlack();
            } else {
                setGuardMainGroupGray();
            }
        }
        openSweep.initToggleState(MyTerminalFactory.getSDK().getParam(Params.GROUP_SCAN, false));
//        btnIsOpenWorkGroup.initToggleState(MyTerminalFactory.getSDK().getParam(Params.GUARD_MAIN_GROUP, false));
        if(MyTerminalFactory.getSDK().getParam(Params.GROUP_SCAN, false)){
//            btnIsOpenWorkGroup.setClickable(false);
        }
        if(MyTerminalFactory.getSDK().getParam(Params.GROUP_SCAN, false)){
//            btnIsOpenWorkGroup.setClickable(false);
        }
    }

    private void initListener () {
        MyTerminalFactory.getSDK().registReceiveHandler(receiveUpdateFoldersAndGroupsHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveUpdateConfigHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveGroupScanResultHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveRequestGroupCallConformationHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveCeaseGroupCallConformationHander);
        //是否打开值守主组
//        btnIsOpenWorkGroup.setOnBtnClick(new MToggleButton.OnBtnClickListener() {
//            @Override
//            public void onBtnClick(boolean currState) {
//                if(currState) {
//                    setGuardMainGroupBlack();
//                }
//                else {
//                    setGuardMainGroupGray();
//                }
//                if (MyTerminalFactory.getSDK().hasNetwork()){
//                    MyTerminalFactory.getSDK().getGroupScanManager().groupScan(currState, GroupScanType.GUARD_MAIN_GROUP.getCode());
//                } else {
//                    ToastUtil.showToast(context, "网络连接异常，请检查网络！");
//                    btnIsOpenWorkGroup.initToggleState(!currState);
//                }
//            }
//        });

        //是否打开组扫描
        openSweep.setOnBtnClick(currState -> {
            if(currState) {
                setGroupScanBlack();
                setGuardMainGroupBlack();
            }
            else {
                setGroupScanGray();
                if (TerminalFactory.getSDK().getParam(Params.GUARD_MAIN_GROUP, false)) {
                    setGuardMainGroupBlack();
                } else {
                    setGuardMainGroupGray();
                }
            }
            if (MyTerminalFactory.getSDK().hasNetwork()){
                MyTerminalFactory.getSDK().getGroupScanManager().groupScan(currState, GroupScanType.GROUP_SCANNING.getCode());
            } else {
                ToastUtil.showToast(context, context.getString(R.string.text_network_connection_abnormal_please_check_the_network));
                openSweep.initToggleState(!currState);
            }
        });

    }

    private void setGroupScanGray() {
//        setTextColor(tv_start_group_sweep, R.color.setting_text);
        rl_group_sweep_setting.setVisibility(View.GONE);
    }

    public void unRegistListener () {
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveUpdateFoldersAndGroupsHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveUpdateConfigHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveGroupScanResultHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveRequestGroupCallConformationHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveCeaseGroupCallConformationHander);
    }

    @Override
    public void onClick (View view) {
        int i = view.getId();//            case R.id.change_main_group:
        //                Intent intent = new Intent(context, ChangeGroupActivity.class);
        //                intent.putExtra("INTENTFROM", 1);
        //                context.startActivity(intent);
        //                break;
        if(i == R.id.rl_group_sweep_setting){
            if(!MyTerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_GROUP_SCAN.name())){
                ToastUtil.showToast(context, context.getString(R.string.text_no_set_sweep_group_authority));
                return;
            }
            context.startActivity(new Intent(context, SetSweepActivity.class));
        }else{
        }
    }

    private void setTextColor (TextView tv, int id) {
        tv.setTextColor(context.getResources().getColor(id));
    }

    /**更新文件夹和组列表数据*/
    private ReceiveUpdateFoldersAndGroupsHandler receiveUpdateFoldersAndGroupsHandler = () -> myHandler.post(() -> {
//                    work_group_name.setText(DataUtil.getGroupByGroupId(MyTerminalFactory.getSDK().getParam(Params.MAIN_GROUP_ID, 0)).name);
    });

    /**更新配置信息*/
    private ReceiveUpdateConfigHandler receiveUpdateConfigHandler = new ReceiveUpdateConfigHandler() {
        @Override
        public void handler() {//更新组扫描的开关，主组名字
            myHandler.post(() -> {
                openSweep.initToggleState(MyTerminalFactory.getSDK().getParam(Params.GROUP_SCAN, false));
//                    btnIsOpenWorkGroup.initToggleState(MyTerminalFactory.getSDK().getParam(Params.GUARD_MAIN_GROUP, false));
//                    work_group_name.setText(DataUtil.getGroupByGroupId(MyTerminalFactory.getSDK().getParam(Params.MAIN_GROUP_ID, 0)).name);
            });
        }
    };

    /**组扫描的消息*/
    private ReceiveGroupScanResultHandler receiveGroupScanResultHandler = new ReceiveGroupScanResultHandler() {
        @Override
        public void handler(final int  groupScanType, final boolean enable,  final int errorCode, String errorDesc) {
            myHandler.post(() -> {
                if (errorCode == BaseCommonCode.SUCCESS_CODE) {
                    if (groupScanType == GroupScanType.GROUP_SCANNING.getCode()) {
                        if (enable) {//打开组扫描
                            MyTerminalFactory.getSDK().putParam(Params.GROUP_SCAN, true);
                            MyTerminalFactory.getSDK().putParam(Params.GUARD_MAIN_GROUP, true);
                            openSweep.initToggleState(true);
//                                btnIsOpenWorkGroup.initToggleState(true);
//                                btnIsOpenWorkGroup.setClickable(false);
                            setGroupScanBlack();
                        } else {//关闭组扫描
                            MyTerminalFactory.getSDK().putParam(Params.GROUP_SCAN, false);
                            openSweep.initToggleState(false);
//                                btnIsOpenWorkGroup.setClickable(true);
                            setGroupScanGray();
                        }
                    }else if (groupScanType == GroupScanType.GUARD_MAIN_GROUP.getCode()) {//
//                            if (mainGroupEnable) {//打开主组值守
//                                logger.info("打开值守主组了回调！！！！！！！");
//                                MyTerminalFactory.getSDK().putParam(Params.GROUP_SCAN, false);
//                                MyTerminalFactory.getSDK().putParam(Params.GUARD_MAIN_GROUP, true);
//                                openSweep.initToggleState(false);
////                                btnIsOpenWorkGroup.initToggleState(true);
//                                setGuardMainGroupBlack();
//                            } else {//关闭主组值守
//                                MyTerminalFactory.getSDK().putParam(Params.GUARD_MAIN_GROUP, false);
////                                btnIsOpenWorkGroup.initToggleState(false);
//                                setGuardMainGroupGray();
//                            }
                    }
                }
            });
        }
    };

    private void setGroupScanBlack() {
//        setTextColor(tv_start_group_sweep, R.color.setting_text);
        rl_group_sweep_setting.setVisibility(View.VISIBLE);

    }

    private void setGuardMainGroupGray() {
//        setTextColor(work_group_name, R.color.setting_text_gray);
//        setTextColor(tv_work_group, R.color.setting_text_gray);
    }

    private void setGuardMainGroupBlack() {
//        setTextColor(work_group_name, R.color.setting_text_black);
//        setTextColor(tv_work_group, R.color.setting_text_black);
    }

    /**主动方请求组呼的消息*/
    private ReceiveRequestGroupCallConformationHandler receiveRequestGroupCallConformationHandler = new ReceiveRequestGroupCallConformationHandler() {
        @Override
        public void handler(final int methodResult, String resultDesc,int groupId) {
            myHandler.post(() -> {
                if (methodResult == 0) {//请求成功，开始组呼
//                        ll_change_name.setEnabled(false);
//                        ll_log_upload.setEnabled(false);
//                        ll_individuation.setEnabled(false);
//                        ll_ptt_setting.setEnabled(false);
//						ll_pttfloat_setting.setEnabled(false);
                    rl_start_group_sweep.setEnabled(false);
//                        rl_work_group.setEnabled(false);
//                        ll_exit.setEnabled(false);
//                        btnIsOpenWorkGroup.setEnabled(false);
                    openSweep.setEnabled(false);
                }
            });
        }
    };

    private ReceiveCeaseGroupCallConformationHander receiveCeaseGroupCallConformationHander = new ReceiveCeaseGroupCallConformationHander() {

        @Override
        public void handler(int resultCode, String resultDesc) {
            myHandler.post(() -> {
//                    ll_change_name.setEnabled(true);
//                    ll_log_upload.setEnabled(true);
//                    ll_individuation.setEnabled(true);
//                    ll_ptt_setting.setEnabled(true);
//					ll_pttfloat_setting.setEnabled(true);
                rl_start_group_sweep.setEnabled(true);
//                    rl_work_group.setEnabled(true);
//                    ll_exit.setEnabled(true);
//                    btnIsOpenWorkGroup.setEnabled(true);
                openSweep.setEnabled(true);
            });
        }
    };
}
