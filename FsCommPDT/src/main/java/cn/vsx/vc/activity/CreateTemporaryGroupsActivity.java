package cn.vsx.vc.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.support.v4.widget.TextViewCompat;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bigkoo.pickerview.OptionsPickerView;

import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;

import butterknife.Bind;
import cn.vsx.hamster.errcode.BaseCommonCode;
import cn.vsx.hamster.terminalsdk.model.Group;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveResponseCreateTempGroup4PCHandler;
import cn.vsx.hamster.terminalsdk.tools.Params;
import cn.vsx.vc.R;
import cn.vsx.vc.application.MyApplication;
import cn.vsx.vc.dialog.CreateTemporaryGroupsDialog;
import cn.vsx.vc.utils.StringUtil;
import cn.vsx.vc.utils.ToastUtil;
import ptt.terminalsdk.context.MyTerminalFactory;

public class CreateTemporaryGroupsActivity extends BaseActivity implements View.OnClickListener {


    @Bind(R.id.news_bar_back)
    ImageView news_bar_back;
    @Bind(R.id.bar_title)
    TextView bar_title;
    @Bind(R.id.ok_btn)
    Button ok_btn;
    @Bind(R.id.right_btn)
    ImageView right_btn;
    @Bind(R.id.left_btn)
    ImageView left_btn;

    @Bind(R.id.create_temporary_group_name)
    EditText create_temporary_group_name;
    @Bind(R.id.iv_locked)
    ImageView ivLocked;
    @Bind(R.id.iv_unlocked)
    ImageView ivUnlocked;
    @Bind(R.id.btn_create_temporary_group)
    Button btn_create_temporary_group;
    @Bind(R.id.iv_change)
    ImageView iv_change;
    @Bind(R.id.iv_unchange)
    ImageView iv_unchange;
    @Bind(R.id.exist_time)
    TextView exist_time;
    @Bind(R.id.locked_time)
    TextView locked_time;
    @Bind(R.id.ll_lock_time)
    LinearLayout ll_lock_time;
    @Bind(R.id.tv_locked)
    TextView tv_locked;
    @Bind(R.id.tv_unlocked)
    TextView tv_unlocked;
    @Bind(R.id.tv_change)
    TextView tv_change;
    @Bind(R.id.tv_unchange)
    TextView tv_unchange;

    private Handler myHandler = new Handler();
    private ArrayList<Integer> list;
    private OptionsPickerView optionsPickerViewA;
    private OptionsPickerView optionsPickerViewB;
    private List<Integer>options1Items=new ArrayList<>();
    private List<Integer>options2Items=new ArrayList<>();
    private List<Integer>options3Items=new ArrayList<>();
    private boolean forceSwitchGroup;
    private TimerTask timerTaskLock;
    private List<Integer> signs;
    //创建临时组的弹窗提示
    private CreateTemporaryGroupsDialog createTemporaryGroupsDialog;


    @Override
    public int getLayoutResId() {
        return R.layout.activity_create_temporary_groups;
    }

    @Override
    public void initView() {
        bar_title.setText("创建临时组");
        right_btn.setVisibility(View.GONE);
        left_btn.setVisibility(View.GONE);
        ok_btn.setVisibility(View.GONE);
        iv_change.setSelected(true);
        iv_unchange.setSelected(false);
        ivLocked.setSelected(false);
        ivUnlocked.setSelected(true);
        ll_lock_time.setVisibility(View.GONE);
        initOptionsPickerViewA();
        initOptionsPickerViewB();
        createTemporaryGroupsDialog = new CreateTemporaryGroupsDialog(this);
    }

    private void initOptionsPickerViewA() {

        optionsPickerViewA = new OptionsPickerView.Builder(this, (options1, options2, options3, v) -> {
            existHour=options1;
            existMin=options2;
            existSec=options3;

            logger.info("存在时间"+options1+"时"+options2+"分"+options3+"秒");
            myHandler.post(() -> exist_time.setText(options1+"时"+options2+"分"+options3+"秒"));
        }).setLabels("时", "分", "秒").setSubmitText("确定")//确定按钮文字
                .setCancelText("取消")//取消按钮文字
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
        optionsPickerViewA.setNPicker(options1Items,options2Items,options3Items);

    }

    private void initOptionsPickerViewB() {

        optionsPickerViewB = new OptionsPickerView.Builder(this, (options1, options2, options3, v) -> {
            lockedHour=options1;
            lockedMin=options2;
            lockedSec=options3;

            logger.info("锁定时间"+options1+"时"+options2+"分"+options3+"秒");
            myHandler.post(() -> locked_time.setText(options1+"时"+options2+"分"+options3+"秒"));

        }).setLabels("时", "分", "秒").setSubmitText("确定")//确定按钮文字
                .setCancelText("取消")//取消按钮文字
                .setTitleText("")//标题
                .setSubCalSize(15)//确定和取消文字大小
                .setSubmitColor(Color.BLACK)//确定按钮文字颜色
                .setCancelColor(Color.BLACK)//取消按钮文字颜色
                .setTitleBgColor(Color.WHITE)//标题背景颜色 Night mode
                .setBgColor(Color.WHITE)//滚轮背景颜色 Night mode
                .setTextColorCenter(Color.BLACK)
                .setContentTextSize(18)//滚轮文字大小
                .setLinkage(false)//设置是否联动，默认 true
                .isCenterLabel(false) //是否只显示中间选中项的 label 文字，false 则每项 item 全部都带有 label。
                .setCyclic(false, false, false)//循环与否
                .setSelectOptions(0, 10, 0)  //设置默认选中项
                .setOutSideCancelable(false)//点击外部 dismiss default true
                .isDialog(false)//是否显示为对话框样式
                .build();

        optionsPickerViewB.setNPicker(options1Items,options2Items,options3Items);

    }


    @Override
    public void initListener() {
        MyTerminalFactory.getSDK().registReceiveHandler(receiveResponseCreateTempGroup4PCHandler);

        //创建临时组
        news_bar_back.setOnClickListener(this);
        ivLocked.setOnClickListener(v -> {
            if (ivLocked.isSelected()) {
                ivLocked.setSelected(false);
                ivUnlocked.setSelected(true);
                ll_lock_time.setVisibility(View.GONE);
                TextViewCompat.setTextAppearance(tv_locked,R.style.temp_group_unchecked);
                TextViewCompat.setTextAppearance(tv_unlocked,R.style.temp_group_checked);
            } else {
                TextViewCompat.setTextAppearance(tv_locked,R.style.temp_group_checked);
                TextViewCompat.setTextAppearance(tv_unlocked,R.style.temp_group_unchecked);
                TextViewCompat.setTextAppearance(tv_change,R.style.temp_group_checked);
                TextViewCompat.setTextAppearance(tv_unchange,R.style.temp_group_unchecked);
                ivLocked.setSelected(true);
                ivUnlocked.setSelected(false);
                iv_change.setSelected(true);
                iv_unchange.setSelected(false);
                ll_lock_time.setVisibility(View.VISIBLE);
            }
        });
        ivUnlocked.setOnClickListener(v -> {
            if (ivUnlocked.isSelected()) {
                ivUnlocked.setSelected(false);
                ivLocked.setSelected(true);
                iv_change.setSelected(true);
                iv_unchange.setSelected(false);
                ll_lock_time.setVisibility(View.VISIBLE);
                TextViewCompat.setTextAppearance(tv_locked,R.style.temp_group_checked);
                TextViewCompat.setTextAppearance(tv_unlocked,R.style.temp_group_unchecked);
                TextViewCompat.setTextAppearance(tv_change,R.style.temp_group_checked);
                TextViewCompat.setTextAppearance(tv_unchange,R.style.temp_group_unchecked);
            } else {
                ivUnlocked.setSelected(true);
                ivLocked.setSelected(false);
                ll_lock_time.setVisibility(View.GONE);
                TextViewCompat.setTextAppearance(tv_locked,R.style.temp_group_unchecked);
                TextViewCompat.setTextAppearance(tv_unlocked,R.style.temp_group_checked);
            }
        });
        iv_change.setOnClickListener(v -> {
            if(iv_change.isSelected()){
                if(ivLocked.isSelected()){
                    ivLocked.setSelected(true);
                    ivUnlocked.setSelected(false);
                    iv_change.setSelected(true);
                    iv_unchange.setSelected(false);
                    TextViewCompat.setTextAppearance(tv_locked,R.style.temp_group_checked);
                    TextViewCompat.setTextAppearance(tv_unlocked,R.style.temp_group_unchecked);
                    TextViewCompat.setTextAppearance(tv_change,R.style.temp_group_checked);
                    TextViewCompat.setTextAppearance(tv_unchange,R.style.temp_group_unchecked);
                }else {
                    iv_change.setSelected(false);
                    iv_unchange.setSelected(true);
                    TextViewCompat.setTextAppearance(tv_change,R.style.temp_group_unchecked);
                    TextViewCompat.setTextAppearance(tv_unchange,R.style.temp_group_checked);
                }
            }else {
                iv_change.setSelected(true);
                iv_unchange.setSelected(false);
                TextViewCompat.setTextAppearance(tv_change,R.style.temp_group_checked);
                TextViewCompat.setTextAppearance(tv_unchange,R.style.temp_group_unchecked);
            }
        });

        iv_unchange.setOnClickListener(v -> {
            if(iv_change.isSelected()){
                if(ivLocked.isSelected()){
                    ivLocked.setSelected(true);
                    ivUnlocked.setSelected(false);
                    iv_change.setSelected(true);
                    iv_unchange.setSelected(false);
                    TextViewCompat.setTextAppearance(tv_locked,R.style.temp_group_checked);
                    TextViewCompat.setTextAppearance(tv_unlocked,R.style.temp_group_unchecked);
                    TextViewCompat.setTextAppearance(tv_change,R.style.temp_group_checked);
                    TextViewCompat.setTextAppearance(tv_unchange,R.style.temp_group_unchecked);
                    ToastUtil.showToast(CreateTemporaryGroupsActivity.this,"锁定状态下自动将组员切换到所创建的临时组");
                }else {
                    iv_change.setSelected(false);
                    iv_unchange.setSelected(true);
                    TextViewCompat.setTextAppearance(tv_change,R.style.temp_group_unchecked);
                    TextViewCompat.setTextAppearance(tv_unchange,R.style.temp_group_checked);
                }
            }else {
                iv_change.setSelected(true);
                iv_unchange.setSelected(false);
                TextViewCompat.setTextAppearance(tv_change,R.style.temp_group_checked);
                TextViewCompat.setTextAppearance(tv_unchange,R.style.temp_group_unchecked);
            }
        });

        btn_create_temporary_group.setOnClickListener(new OnClickListenerImplementation());

        exist_time.setOnClickListener(v -> optionsPickerViewA.show());

        locked_time.setOnClickListener(v -> optionsPickerViewB.show());

    }

    @Override
    public void initData() {

        list = getIntent().getIntegerArrayListExtra("list");
        logger.info("创建临时组成员列表："+list.toString());
        int index1=0;
        for(int i=0;i<=23;i++){
            options1Items.add(index1);
            index1++;
        }
        int index2=0;
        for(int i=0;i<=59;i++){
            options2Items.add(index2);
            options3Items.add(index2);
            index2++;
        }
        signs = new ArrayList<>();

        for(Group group :MyTerminalFactory.getSDK().getConfigManager().getAllGroups()){
            if(group.getDepartmentId() !=-1){
                continue;
            }
            if(group.getName().contains("-")){
                String[] split = group.getName().split("-");
                String name = split[0];
                if(name.startsWith("临时组") && name.length()>=3){
                    String s = name.substring(3);
                    if(StringUtil.isNumber(s)){
                        int number = Integer.valueOf(s);
                        signs.add(number);
                    }
                }
            }
        }

        //从1开始判断
        int sign = getSign(1);
        int memberId = MyTerminalFactory.getSDK().getParam(Params.MEMBER_ID,0);
        String s = String.valueOf(memberId);
        create_temporary_group_name.setText("临时组"+ sign +"-"+s.substring(s.length()-4));
        exist_time.setText("23时59分59秒");
        locked_time.setText("0时10分0秒");
        ivUnlocked.setSelected(true);
        ivLocked.setSelected(false);
    }

    //获取临时组名字里的编号
    private int getSign(int sign){
        int number = sign;
        if(signs.contains(sign)){
            sign ++;
            number = getSign(sign);
        }
        return number;
    }

    @Override
    public void doOtherDestroy() {
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveResponseCreateTempGroup4PCHandler);
    }


/**
 * =================================================================================================handler======================================================================================================================
 **/
    private ReceiveResponseCreateTempGroup4PCHandler receiveResponseCreateTempGroup4PCHandler=new ReceiveResponseCreateTempGroup4PCHandler() {
    @Override
    public void handler(int tempGroupNo, final int resultCode, final String resultDesc) {

        myHandler.post(() -> {
            checkDialogIsNotNull();
            if(resultCode== BaseCommonCode.SUCCESS_CODE){
                createTemporaryGroupsDialog.updateTemporaryGroupDialog(CreateTemporaryGroupsDialog.CREATE_GROUP_STATE_SUCCESS,"",forceSwitchGroup);
                myHandler.postDelayed(() -> {
                    //刷新通讯录组群列表
                    MyTerminalFactory.getSDK().getConfigManager().updateAllGroups();
                    dismissTemporaryGroupDialog();
                    startActivity(new Intent(CreateTemporaryGroupsActivity.this,NewMainActivity.class));
                    CreateTemporaryGroupsActivity.this.finish();
                },2000);
            }else {
                createTemporaryGroupsDialog.updateTemporaryGroupDialog(CreateTemporaryGroupsDialog.CREATE_GROUP_STATE_FAIL,resultDesc,forceSwitchGroup);
                myHandler.postDelayed(() -> dismissTemporaryGroupDialog(),2000);

            }

        });
    }
};

    /**
     * =================================================================================================Listener======================================================================================================================
     **/
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.news_bar_back:
                finish();
                break;

        }
    }

    private int existHour = 23;
    private int existMin = 59;
    private int existSec = 59;//默认存活时间23小时59分59秒
    private int lockedHour = 0;
    private int lockedMin = 10;//默认锁定时间10分钟
    private int lockedSec = 0;


    private final class OnClickListenerImplementation implements View.OnClickListener {

        @Override
        public void onClick(View v) {

            final String temporaryGroupsName = create_temporary_group_name.getText().toString().trim();
            final long existTime = existHour * 3600  + existMin * 60  +existSec ;
            final long lockedTime = lockedHour * 3600  + lockedMin * 60 +lockedSec;
            forceSwitchGroup = iv_change.isSelected();
            final boolean finalIslock = ivLocked.isSelected();
            if(TextUtils.isEmpty(temporaryGroupsName)){
                ToastUtil.showToast(MyApplication.instance.getApplicationContext(),"临时组名称不可为空");
                return;
            }
            if(existTime<2*60){
                ToastUtil.showToast(MyApplication.instance.getApplicationContext(),"存活时间不能低于2分钟");
                return;
            }
            if(existTime>24*60*60-1){
                ToastUtil.showToast(MyApplication.instance.getApplicationContext(),"存活时间不能大于23时59分59秒");
                return;
            }
            if(lockedTime>24*60*60-1){
                ToastUtil.showToast(MyApplication.instance.getApplicationContext(),"锁定时间不能大于23时59分59秒");
                return;
            }
            if(finalIslock && lockedTime>existTime){
                ToastUtil.showToast(MyApplication.instance.getApplicationContext(),"锁定时间不能大于存活时间");
                return;
            }
            if(StringUtil.isEmoji(temporaryGroupsName)){
                ToastUtil.showToast(MyApplication.instance.getApplicationContext(), "请输入合法的临时组名称");
                return;
            }
            if (temporaryGroupsName.length() > 17 || temporaryGroupsName.length() < 3) {
                ToastUtil.showToast(MyApplication.instance.getApplicationContext(), "名称不合法");
                return;
            }
           //显示提示框
            checkDialogIsNotNull();
            createTemporaryGroupsDialog.updateTemporaryGroupDialog(CreateTemporaryGroupsDialog.CREATE_GROUP_STATE_CREATTING,"",forceSwitchGroup);
            logger.error("创建临时组：" + "forceSwitchGroup" + forceSwitchGroup + ",temporaryGroupsName" + temporaryGroupsName + ",pushMemberList" + list.toString() + ",existTime" + existTime + ",islock" + finalIslock + ",lockedTime" + lockedTime);
            //延时创建临时组
            if (timerTaskLock != null) {
                timerTaskLock.cancel();
                timerTaskLock = null;
            }
            timerTaskLock = new TimerTask() {
                @Override
                public void run() {
                    MyTerminalFactory.getSDK().getTempGroupManager().createTempGroup(forceSwitchGroup,temporaryGroupsName,list,existTime, finalIslock,lockedTime);
                }
            };
            MyTerminalFactory.getSDK().getTimer().schedule(timerTaskLock, 2000);
        }
    }

    /**
     * 创建临时组的弹窗非空规避
     */
    private void checkDialogIsNotNull(){
        if(createTemporaryGroupsDialog==null){
            createTemporaryGroupsDialog = new CreateTemporaryGroupsDialog(CreateTemporaryGroupsActivity.this);
        }
    }

    /**
     * 关闭创建临时组的弹窗
     */
    private void dismissTemporaryGroupDialog(){
        if(createTemporaryGroupsDialog!=null){
            createTemporaryGroupsDialog.dismiss();
        }
    }
}
