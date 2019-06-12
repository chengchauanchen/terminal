package cn.vsx.vc.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import cn.vsx.hamster.terminalsdk.model.Member;
import cn.vsx.hamster.terminalsdk.model.WarningRecord;
import cn.vsx.hamster.terminalsdk.tools.DataUtil;
import cn.vsx.hamster.terminalsdk.tools.Params;
import cn.vsx.vc.R;
import cn.vsx.vc.adapter.ItemAdapter;
import cn.vsx.vc.utils.CallPhoneUtil;
import cn.vsx.vc.utils.ToastUtil;
import ptt.terminalsdk.context.MyTerminalFactory;

public class WarningMessageDetailActivity extends BaseActivity{

    private TextView mTvAperson;
    private TextView mTvApersonPhone;
    private TextView mTvRecePerson;
    private TextView mTvRecePersonPhone;
    private TextView mTvAlarmTime;
    private TextView mTvAddress;
    private TextView mTvSummary;
    private TextView mTvTitle;
    private static int VOIP=0;
    private static int TELEPHONE=1;
    private ImageView mReturn;
    @SuppressWarnings("handlerLeak")
    private Handler mHandler = new Handler();

    @Override
    public int getLayoutResId(){
        return R.layout.activity_warning_message_detail;
    }

    @Override
    public void initView(){
        mReturn = findViewById(R.id.news_bar_return);
        mTvTitle = findViewById(R.id.tv_title);
        mTvAperson = findViewById(R.id.tv_aperson);
        mTvApersonPhone = findViewById(R.id.tv_aperson_phone);
        mTvRecePerson = findViewById(R.id.tv_rece_person);
        mTvRecePersonPhone = findViewById(R.id.tv_rece_person_phone);
        mTvAlarmTime = findViewById(R.id.tv_alarm_time);
        mTvAddress = findViewById(R.id.tv_address);
        mTvSummary = findViewById(R.id.tv_summary);
    }

    @Override
    public void initListener(){
        mTvRecePersonPhone.setOnClickListener(recePersonOnClickListener);
        mTvApersonPhone.setOnClickListener(apersonphoneOnClickListener);
        mReturn.setOnClickListener(returnOnClickListener);
    }

    private View.OnClickListener returnOnClickListener = v -> finish();

    private View.OnClickListener apersonphoneOnClickListener = v -> {
        if (!TextUtils.isEmpty(mTvApersonPhone.getText().toString().trim())){
            showDailog(mTvApersonPhone.getText().toString().trim());
        }
    };

    private View.OnClickListener recePersonOnClickListener = v -> {
        if (!TextUtils.isEmpty(mTvRecePersonPhone.getText().toString().trim())){
            showDailog(mTvRecePersonPhone.getText().toString().trim());
        }
    };

    private void showDailog(String phone){
        if(TextUtils.isEmpty(phone)){
            return;
        }
        ItemAdapter adapter = new ItemAdapter(WarningMessageDetailActivity.this, ItemAdapter.iniDatas());
        AlertDialog.Builder builder = new AlertDialog.Builder(WarningMessageDetailActivity.this);
        //设置标题
        builder.setTitle("拨打电话");
        builder.setAdapter(adapter, (dialogInterface, position) -> {
            if(position == VOIP){//voip电话
                if(MyTerminalFactory.getSDK().getParam(Params.VOIP_SUCCESS, false)){
                    Intent intent = new Intent(WarningMessageDetailActivity.this, VoipPhoneActivity.class);
                    Member member;
                    long number = Long.parseLong(phone);
                    if(number>Integer.MAX_VALUE){
                        member = new Member((int) number,phone);
                        member.setPhone(phone);
                    }else {
                        member = DataUtil.getMemberByMemberNo((int) number);
                        if(TextUtils.isEmpty(member.getPhone())){
                            member.setPhone(phone);
                        }
                    }
                    intent.putExtra("member", member);
                    WarningMessageDetailActivity.this.startActivity(intent);
                }else{
                    ToastUtil.showToast(WarningMessageDetailActivity.this, "voip注册失败，请检查服务器配置");
                }
            }else if(position == TELEPHONE){//普通电话
                CallPhoneUtil.callPhone(WarningMessageDetailActivity.this, phone);
            }
        });
        builder.create();
        builder.show();
    }

    @Override
    public void initData(){
        setView();
    }

    private void setView(){
        WarningRecord warningRecord = (WarningRecord) getIntent().getSerializableExtra("warningRecord");
        if(null == warningRecord){
            finish();
        }else {
            mTvAperson.setText(warningRecord.getAperson());
            mTvApersonPhone.setText(warningRecord.getApersonPhone());
            mTvRecePerson.setText(warningRecord.getRecvperson());
            mTvRecePersonPhone.setText(warningRecord.getRecvphone());
            mTvAlarmTime.setText(warningRecord.getAlarmTime());
            mTvAddress.setText(warningRecord.getAddress());
            mTvSummary.setText(warningRecord.getSummary());
            mTvTitle.setText(warningRecord.getSummary());
        }
    }

    @Override
    public void doOtherDestroy(){
        mHandler.removeCallbacksAndMessages(null);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        setView();
    }
}
