package cn.vsx.vc.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.support.v4.widget.TextViewCompat;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bigkoo.pickerview.OptionsPickerView;

import java.util.ArrayList;
import java.util.List;

import cn.vsx.hamster.common.TerminalMemberType;
import cn.vsx.hamster.common.UrlParams;
import cn.vsx.hamster.errcode.BaseCommonCode;
import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.model.Member;
import cn.vsx.hamster.terminalsdk.model.TempGroup;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveForceReloginForUIOperationHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveGetTempGroupListByUniqueNoHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveResponseCreateTempGroup4PCHandler;
import cn.vsx.hamster.terminalsdk.tools.Params;
import cn.vsx.vc.R;
import cn.vsx.vc.application.MyApplication;
import cn.vsx.vc.dialog.CreateTemporaryGroupsDialog;
import cn.vsx.vc.utils.SpaceFilter;
import cn.vsx.vc.utils.ToastUtil;
import ptt.terminalsdk.context.MyTerminalFactory;
import ptt.terminalsdk.tools.AppUtil;
import ptt.terminalsdk.tools.StringUtil;

public class CreateTemporaryGroupsActivity extends BaseActivity implements View.OnClickListener {


    ImageView news_bar_back;

    TextView bar_title;

    Button ok_btn;

    ImageView right_btn;

    ImageView left_btn;


    EditText create_temporary_group_name;


    Button btn_create_temporary_group;

    ImageView iv_scan;

    ImageView iv_unscan;

    TextView exist_time;


    TextView tv_scan;

    TextView tv_unscan;

    private Handler myHandler = new Handler();
    private ArrayList<Member> list;
    private OptionsPickerView optionsPickerViewA;
    private List<Integer> options1Items = new ArrayList<>();
    private List<Integer> options2Items = new ArrayList<>();
    private List<Integer> options3Items = new ArrayList<>();
    private boolean scanGroup;
    //创建临时组的弹窗提示
    private CreateTemporaryGroupsDialog createTemporaryGroupsDialog;


    @Override
    public int getLayoutResId() {
        return R.layout.activity_create_temporary_groups;
    }

    @Override
    public void initView() {
        news_bar_back = (ImageView) findViewById(R.id.news_bar_back);
        bar_title = (TextView) findViewById(R.id.bar_title);
        ok_btn = (Button) findViewById(R.id.ok_btn);
        right_btn = (ImageView) findViewById(R.id.right_btn);
        left_btn = (ImageView) findViewById(R.id.left_btn);
        create_temporary_group_name = (EditText) findViewById(R.id.create_temporary_group_name);
        btn_create_temporary_group = (Button) findViewById(R.id.btn_create_temporary_group);
        iv_scan = (ImageView) findViewById(R.id.iv_scan);
        iv_unscan = (ImageView) findViewById(R.id.iv_unscan);
        exist_time = (TextView) findViewById(R.id.exist_time);
        tv_scan = (TextView) findViewById(R.id.tv_scan);
        tv_unscan = (TextView) findViewById(R.id.tv_unscan);
        bar_title.setText(R.string.text_create_temporary_groups);
        right_btn.setVisibility(View.GONE);
        left_btn.setVisibility(View.GONE);
        ok_btn.setVisibility(View.GONE);
        iv_scan.setSelected(true);
        iv_unscan.setSelected(false);
        initOptionsPickerViewA();
        createTemporaryGroupsDialog = new CreateTemporaryGroupsDialog(this);
        create_temporary_group_name.setFilters(new InputFilter[]{new SpaceFilter()});
    }

    private void initOptionsPickerViewA() {

        optionsPickerViewA = new OptionsPickerView.Builder(this, (options1, options2, options3, v) -> {
            existHour = options1;
            existMin = options2;
            existSec = options3;

            logger.info("存在时间" + options1 + "时" + options2 + "分" + options3 + "秒");
            myHandler.post(() -> exist_time.setText(String.format(getResources().getString(R.string.activity_create_temporary_groups_time), options1, options2, options3)));
        }).setLabels(getResources().getString(R.string.text_hour), getResources().getString(R.string.text_minute), getResources().getString(R.string.text_second))
                .setSubmitText(getResources().getString(R.string.text_sure))//确定按钮文字
                .setCancelText(getResources().getString(R.string.text_cancel))//取消按钮文字
                .setTitleText("")//标题
                .setSubCalSize(15)//确定和取消文字大小
                .setSubmitColor(Color.BLACK)//确定按钮文字颜色
                .setCancelColor(Color.BLACK)//取消按钮文字颜色
                .setTitleBgColor(Color.WHITE)//标题背景颜色 Night mode
                .setTextColorCenter(Color.BLACK)//字体颜色
                .setBgColor(Color.WHITE)//滚轮背景颜色 Night mode
                .setContentTextSize(18)//滚轮文字大小
                .setLinkage(false)//设置是否联动，默认 true
                .isCenterLabel(false) //是否只显示中间选中项的 label 文字，false 则每项 item 全部都带有 label。
                .setCyclic(false, false, false)//循环与否
                .setSelectOptions(23, 59, 59)  //设置默认选中项
                .setOutSideCancelable(false)//点击外部 dismiss default true
                .isDialog(false)//是否显示为对话框样式
                .build();
        optionsPickerViewA.setNPicker(options1Items, options2Items, options3Items);

    }


    @Override
    public void initListener() {
        MyTerminalFactory.getSDK().registReceiveHandler(receiveResponseCreateTempGroup4PCHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveForceReloginForUIOperationHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveGetTempGroupListByUniqueNoHandler);

        //创建临时组
        news_bar_back.setOnClickListener(this);


        iv_scan.setOnClickListener(v -> {
            if (iv_scan.isSelected()) {
                iv_scan.setSelected(false);
                iv_unscan.setSelected(true);
                TextViewCompat.setTextAppearance(tv_scan, R.style.temp_group_unchecked);
                TextViewCompat.setTextAppearance(tv_unscan, R.style.temp_group_checked);

            } else {
                iv_scan.setSelected(true);
                iv_unscan.setSelected(false);
                TextViewCompat.setTextAppearance(tv_scan, R.style.temp_group_checked);
                TextViewCompat.setTextAppearance(tv_unscan, R.style.temp_group_unchecked);
            }
        });

        iv_unscan.setOnClickListener(v -> {
            if (iv_scan.isSelected()) {
                iv_scan.setSelected(false);
                iv_unscan.setSelected(true);
                TextViewCompat.setTextAppearance(tv_scan, R.style.temp_group_unchecked);
                TextViewCompat.setTextAppearance(tv_unscan, R.style.temp_group_checked);
            } else {
                iv_scan.setSelected(true);
                iv_unscan.setSelected(false);
                TextViewCompat.setTextAppearance(tv_scan, R.style.temp_group_checked);
                TextViewCompat.setTextAppearance(tv_unscan, R.style.temp_group_unchecked);
            }
        });

        btn_create_temporary_group.setOnClickListener(new OnClickListenerImplementation());

        exist_time.setOnClickListener(v -> optionsPickerViewA.show());


    }

    @Override
    public void initData() {

        list = (ArrayList<Member>) getIntent().getSerializableExtra("data");
        logger.info("创建临时组成员列表：" + list.toString());
        int index1 = 0;
        for (int i = 0; i <= 23; i++) {
            options1Items.add(index1);
            index1++;
        }
        int index2 = 0;
        for (int i = 0; i <= 59; i++) {
            options2Items.add(index2);
            options3Items.add(index2);
            index2++;
        }
        //获取本地存储的临时组创建市生成的索引
//        showTempGroupName();
//        create_temporary_group_name.setText(String.format(getResources().getString(R.string.activity_create_temporary_groups_name), sign, s.substring(s.length() - 4)));
        exist_time.setText(R.string.activity_create_temporary_groups_tempts_exist_time);
        TerminalFactory.getSDK().getTempGroupManager().getCreateTempGroupListByUniqueNo(TerminalFactory.getSDK().getParam(Params.MEMBER_UNIQUENO, 0L)+"");

    }

    /**
     * 获取本地存储的临时组创建市生成的索引
     */
    private void showTempGroupName(long index) {
        try{
            String stringid = String.valueOf(MyTerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0));
            String tempGroupNameNo = stringid;
            if(stringid.length()>=4){
                tempGroupNameNo = stringid.substring(stringid.length() - 4);
            }
            String finalTempGroupNameNo = tempGroupNameNo;
            myHandler.post(() -> {
                if(create_temporary_group_name!=null){
                    create_temporary_group_name.setText(String.format(getString(R.string.activity_create_temporary_groups_name),index, finalTempGroupNameNo));
                    create_temporary_group_name.setSelection( create_temporary_group_name.getText().toString().length());
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void doOtherDestroy() {
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveResponseCreateTempGroup4PCHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveForceReloginForUIOperationHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveGetTempGroupListByUniqueNoHandler);
    }

    /**
     * =================================================================================================handler======================================================================================================================
     **/
    private ReceiveResponseCreateTempGroup4PCHandler receiveResponseCreateTempGroup4PCHandler = new ReceiveResponseCreateTempGroup4PCHandler() {
        @Override
        public void handler(int tempGroupNo, String alarmNo, String tempGroupType, long uniqueNo, int resultCode, String resultDesc) {
            myHandler.post(() -> {
                checkDialogIsNotNull();
                if (resultCode == BaseCommonCode.SUCCESS_CODE) {
                    if(AppUtil.checkActivityIsRun(CreateTemporaryGroupsActivity.this)){
                        createTemporaryGroupsDialog.updateTemporaryGroupDialog(CreateTemporaryGroupsDialog.CREATE_GROUP_STATE_SUCCESS, "", scanGroup);
                    }
                    //临时组创建成功之后，开启一个定时任务，到期时给用户一个提示
                    myHandler.postDelayed(() -> {
                        //刷新通讯录组群列表
//                    MyTerminalFactory.getSDK().getConfigManager().updateAllGroups();
                        dismissTemporaryGroupDialog();
                        try{
                            Class clazz;
                            String type = MyTerminalFactory.getSDK().getParam(UrlParams.TERMINALMEMBERTYPE, "");
                            if(type.equals(TerminalMemberType.TERMINAL_UAV.toString())){
                                clazz = Class.forName("cn.vsx.uav.activity.UavMainActivity");
                            }else {
                                clazz = Class.forName("cn.vsx.vc.activity.NewMainActivity");
                            }
                            Intent intent = new Intent(CreateTemporaryGroupsActivity.this,clazz);
                            startActivity(intent);
                            CreateTemporaryGroupsActivity.this.finish();
                        }catch(ClassNotFoundException e){
                            e.printStackTrace();
                        }
                    }, 500);
                } else {
                    if(AppUtil.checkActivityIsRun(CreateTemporaryGroupsActivity.this)){
                        createTemporaryGroupsDialog.updateTemporaryGroupDialog(CreateTemporaryGroupsDialog.CREATE_GROUP_STATE_FAIL, resultDesc, scanGroup);
                    }
                    myHandler.postDelayed(() -> dismissTemporaryGroupDialog(), 2000);

                }

            });
        }
    };

    /**
     * 强制重新注册的消息
     */
    private ReceiveForceReloginForUIOperationHandler receiveForceReloginForUIOperationHandler = new ReceiveForceReloginForUIOperationHandler() {
        @Override
        public void handler(String version) {
            myHandler.post(new Runnable() {
                @Override
                public void run() {
                    //如果正在弹窗，取消弹窗
                    if (createTemporaryGroupsDialog != null && createTemporaryGroupsDialog.isShowing()) {
                        createTemporaryGroupsDialog.dismiss();
                    }
                }
            });
        }
    };

    /**
     * 通过uniqueNo获取设备创建临时组列表
     */
    private ReceiveGetTempGroupListByUniqueNoHandler receiveGetTempGroupListByUniqueNoHandler = new ReceiveGetTempGroupListByUniqueNoHandler() {
        @Override
        public void handler(List<TempGroup> tempGroupList) {
            long index = MyTerminalFactory.getSDK().getParam(Params.TEMP_GROUP_NAME_INDEX, 0L);
            if(tempGroupList!=null&&!tempGroupList.isEmpty()){
                long getIndex = TerminalFactory.getSDK().getTempGroupManager().getMaxTempGroupNameIndex(tempGroupList);
                if(getIndex>0){
                    index = getIndex;
                    MyTerminalFactory.getSDK().putParam(Params.TEMP_GROUP_NAME_INDEX, index);
                }
            }
            showTempGroupName(index+1);
        }
    };

    /**
     * =================================================================================================Listener======================================================================================================================
     **/
    @Override
    public void onClick(View v) {
        int i = v.getId();
        if(i == R.id.news_bar_back){
            finish();
        }
    }

    private int existHour = 23;
    private int existMin = 59;
    private int existSec = 59;//默认存活时间23小时59分59秒

    private long existTime;//临时组的存活时间(单位秒)
    String temporaryGroupsName;//临时组的名称


    private final class OnClickListenerImplementation implements View.OnClickListener {

        @Override
        public void onClick(View v) {

            temporaryGroupsName = create_temporary_group_name.getText().toString().trim();
            existTime = existHour * 3600 + existMin * 60 + existSec;
            scanGroup = iv_scan.isSelected();
            if (TextUtils.isEmpty(temporaryGroupsName)) {
                ToastUtil.showToast(MyApplication.instance.getApplicationContext(), getString(R.string.activity_create_temporary_groups_check_data_name_one));
                return;
            }
            if (existTime < 2 * 60) {
                ToastUtil.showToast(MyApplication.instance.getApplicationContext(), getString(R.string.activity_create_temporary_groups_check_data_time_one));
                return;
            }
            if (existTime > 24 * 60 * 60 - 1) {
                ToastUtil.showToast(MyApplication.instance.getApplicationContext(), getString(R.string.activity_create_temporary_groups_check_data_time_two));
                return;
            }

            if (StringUtil.isEmoji(temporaryGroupsName)) {
                ToastUtil.showToast(MyApplication.instance.getApplicationContext(), getString(R.string.activity_create_temporary_groups_check_data_name_two));
                return;
            }
            if (temporaryGroupsName.length() > 17 || temporaryGroupsName.length() < 3) {
                ToastUtil.showToast(MyApplication.instance.getApplicationContext(), getString(R.string.activity_create_temporary_groups_check_data_name_three));
                return;
            }
            //显示提示框
            checkDialogIsNotNull();
            if(AppUtil.checkActivityIsRun(CreateTemporaryGroupsActivity.this)){
                createTemporaryGroupsDialog.updateTemporaryGroupDialog(CreateTemporaryGroupsDialog.CREATE_GROUP_STATE_CREATTING, "", scanGroup);
            }
            logger.error("创建临时组：" + "scanGroup" + scanGroup + ",temporaryGroupsName" + temporaryGroupsName + ",pushMemberList" + list.toString() + ",existTime" + existTime);
            TerminalFactory.getSDK().getThreadPool().execute(() -> {
                List<Long> uniqueNoList = new ArrayList<>();
                List<Integer> memberNos = new ArrayList<>();
                for (Member member : list) {
                    uniqueNoList.add(member.getUniqueNo());
                    memberNos.add(member.getNo());
                }
                MyTerminalFactory.getSDK().getTempGroupManager().createTempGroup(false, temporaryGroupsName, memberNos, existTime, false, 0, scanGroup, "", uniqueNoList);
            });
        }
    }

    /**
     * 创建临时组的弹窗非空规避
     */
    private void checkDialogIsNotNull() {
        if (createTemporaryGroupsDialog == null) {
            createTemporaryGroupsDialog = new CreateTemporaryGroupsDialog(CreateTemporaryGroupsActivity.this);
        }
    }

    /**
     * 关闭创建临时组的弹窗
     */
    private void dismissTemporaryGroupDialog() {
        if (createTemporaryGroupsDialog != null) {
            createTemporaryGroupsDialog.dismiss();
        }
    }

    public static void startActivity(Context context, ArrayList<Member> data) {
        Intent intent = new Intent();
        intent.setClass(context, CreateTemporaryGroupsActivity.class);
        intent.putExtra("data", data);
        context.startActivity(intent);
    }
}
