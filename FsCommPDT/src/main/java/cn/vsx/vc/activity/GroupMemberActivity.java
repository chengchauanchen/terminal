package cn.vsx.vc.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.HeaderViewListAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TimerTask;

import butterknife.Bind;
import butterknife.OnClick;
import cn.vsx.hamster.errcode.BaseCommonCode;
import cn.vsx.hamster.terminalsdk.model.Group;
import cn.vsx.hamster.terminalsdk.model.Member;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveGetGroupCurrentOnlineMemberListHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveResponseAddMemberToTempGroupMessageHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveResponseRemoveMemberToTempGroupMessageHandler;
import cn.vsx.hamster.terminalsdk.tools.Params;
import cn.vsx.vc.R;
import cn.vsx.vc.adapter.GroupMemberAdapter;
import cn.vsx.vc.fragment.PersonSearchFragment;
import cn.vsx.vc.utils.DataUtil;
import cn.vsx.vc.utils.ToastUtil;
import cn.vsx.vc.view.VolumeViewLayout;
import ptt.terminalsdk.context.MyTerminalFactory;

/**
 * Created by Administrator on 2017/3/15 0015.
 */

public class GroupMemberActivity extends BaseActivity {

    @Bind(R.id.news_bar_back)
    ImageView newsBarBack;
    @Bind(R.id.news_bar_back_temp)
    ImageView newsBarBackTemp;
    @Bind(R.id.bar_title)
    TextView barTitle;
    @Bind(R.id.in_title_bar)
    LinearLayout in_title_bar;
    @Bind(R.id.right_btn)
    ImageView rightBtn;
    @Bind(R.id.ok_btn)
    Button ok_btn;

    @Bind(R.id.temp_title_bar)
    LinearLayout temp_title_bar;
    @Bind(R.id.add_btn)
    ImageView add_btn;
    @Bind(R.id.delete_btn)
    ImageView delete_btn;
    @Bind(R.id.cancel_text)
    TextView cancel_text;
    @Bind(R.id.delete_text)
    TextView delete_text;
    @Bind(R.id.member_num)
    TextView memberNum;
    @Bind(R.id.member_list)
    ListView memberList;
    @Bind(R.id.volume_layout)
    VolumeViewLayout volumeViewLayout;
    private GroupMemberAdapter sortAdapter;
    private Handler myHandler = new Handler();
    private List<Member> currentGroupMembers = new ArrayList<>();
    private int groupId;
    String groupName;
    private int total=0;
    private boolean canAdd;//只有自己创建的临时组才能添加和删除人
    private boolean isTemporaryGroup;
    public void setListViewHeightBasedOnChildren(ListView listView) {
        if (listView == null)
            return;
        if (listView.getAdapter() instanceof HeaderViewListAdapter) {
            HeaderViewListAdapter listAdapter = (HeaderViewListAdapter) listView.getAdapter();
            sortAdapter = (GroupMemberAdapter) listAdapter.getWrappedAdapter();
        } else {
            sortAdapter = (GroupMemberAdapter) listView.getAdapter();
        }
        if (sortAdapter == null) {
            return;
        }
        int totalHeight = 0;
        for (int i = 0; i < sortAdapter.getCount(); i++) {
            View listItem = sortAdapter.getView(i, null, listView);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (sortAdapter.getCount() - 1));

    }

    @Override
    public int getLayoutResId() {
        return R.layout.activity_group_member;
    }

    @Override
    public void initView() {
        // 获取屏幕宽度
        WindowManager windowManager = (WindowManager) this.getSystemService(WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(dm);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(isTemporaryGroup){
            MyTerminalFactory.getSDK().getGroupManager().getGroupCurrentOnlineMemberList(groupId, true);
        }else {
            MyTerminalFactory.getSDK().getGroupManager().getGroupCurrentOnlineMemberList(groupId, false);
        }
    }

    @Override
    public void initListener() {
        MyTerminalFactory.getSDK().registReceiveHandler(mReceiveGetGroupCurrentOnlineMemberListHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(mReceiveResponseRemoveMemberToTempGroupMessageHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(mReceiveResponseAddMemberToTempGroupMessageHandler);
    }

    @Override
    public void initData() {
        groupId = getIntent().getIntExtra("groupId", 0);
        groupName = getIntent().getStringExtra("groupName");
        Group group = DataUtil.getGroupByGroupNo(groupId);
        isTemporaryGroup = group.getDepartmentId() == -1;
        //只有自己创建的临时组才能添加和删除人
        if(group.getCreatedMemberNo() == MyTerminalFactory.getSDK().getParam(Params.MEMBER_ID,-1) && isTemporaryGroup){
            canAdd = true;
        }
        rightBtn.setVisibility(View.GONE);
        ok_btn.setVisibility(View.GONE);
        logger.info( "是否为我创建的临时组:" + canAdd+"-----groupNo："+groupId);
        if(isTemporaryGroup){
            in_title_bar.setVisibility(View.GONE);
            temp_title_bar.setVisibility(View.VISIBLE);
            rightBtn.setVisibility(View.GONE);
            ok_btn.setVisibility(View.GONE);
            if(!canAdd){
                add_btn.setVisibility(View.GONE);
                delete_btn.setVisibility(View.GONE);
            }
        }else {
            in_title_bar.setVisibility(View.VISIBLE);
            temp_title_bar.setVisibility(View.GONE);
        }

        sortAdapter = new GroupMemberAdapter(GroupMemberActivity.this, currentGroupMembers, false);
        memberList.setAdapter(sortAdapter);
    }

    @Override
    public void doOtherDestroy() {
        MyTerminalFactory.getSDK().unregistReceiveHandler(mReceiveGetGroupCurrentOnlineMemberListHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(mReceiveResponseRemoveMemberToTempGroupMessageHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(mReceiveResponseAddMemberToTempGroupMessageHandler);
        if (volumeViewLayout != null) {
            volumeViewLayout.unRegistLintener();
        }
    }

    private long lastSearchTime=0;

    @OnClick({R.id.news_bar_back, R.id.right_btn,R.id.news_bar_back_temp,R.id.delete_btn,R.id.add_btn,R.id.cancel_text,R.id.delete_text})
    public void onClick(View view) {
        long currentTime = System.currentTimeMillis();
        if( currentTime - lastSearchTime<1000){
            return;
        }
        lastSearchTime= currentTime;

        switch (view.getId()) {
            case R.id.news_bar_back:
                finish();
                break;
            case R.id.news_bar_back_temp:
                finish();
                break;
            case R.id.right_btn:
                PersonSearchFragment personSearchFragment = new PersonSearchFragment();
                personSearchFragment.setGroupMember(DataUtil.getAllMembersExceptMe(currentGroupMembers));
                personSearchFragment.setInterGroup(true);
                getSupportFragmentManager().beginTransaction().addToBackStack(null).replace(R.id.fl_fragment_container_group_member, personSearchFragment).commit();
                break;
            case R.id.add_btn:
                IncreaseTemporaryGroupMemberActivity.startActivity(GroupMemberActivity.this,1,groupId);
                break;
            case R.id.delete_btn:
                add_btn.setVisibility(View.GONE);
                delete_btn.setVisibility(View.GONE);
                delete_text.setVisibility(View.VISIBLE);
                cancel_text.setVisibility(View.VISIBLE);

                sortAdapter = new GroupMemberAdapter(GroupMemberActivity.this, currentGroupMembers, true);
                sortAdapter.setOnItemClickListener((view1, position, checked, member) -> {
                    if(checked){
                        total++;
                    }else {
                        total--;
                    }
                    if(total>0){
                        delete_text.setText(String.format(getString(R.string.button_delete_number),total));
                    }else {
                        delete_text.setText(R.string.text_delete);
                    }
                });
                memberList.setAdapter(sortAdapter);
                sortAdapter.notifyDataSetChanged();
                break;
            case R.id.delete_text:
                if(MyTerminalFactory.getSDK().getConfigManager().getCurrentGroupMembers().size()<2){//当前组仅剩创建者本身的时候点击删除销临时组

                    final AlertDialog alertDialog = new AlertDialog.Builder(GroupMemberActivity.this).create();
                    alertDialog.show();
                    Display display = getWindowManager().getDefaultDisplay();
                    int heigth = display.getWidth();
                    int width = display.getHeight();
                    Window window = alertDialog.getWindow();
                    WindowManager.LayoutParams layoutParams = window.getAttributes();
                    layoutParams.width=width/2;
                    layoutParams.height=heigth/2;
                    window.setAttributes(layoutParams);
                    window.setContentView(R.layout.dialog_delete_temporary_group);
                    final LinearLayout ll_select = window.findViewById(R.id.ll_select);
                    final LinearLayout  ll_success = window.findViewById(R.id.ll_success);
                    Button btn_confirm = window.findViewById(R.id.btn_confirm);
                    btn_confirm.setOnClickListener(v -> myHandler.post(() -> {
                        ll_success.setVisibility(View.VISIBLE);
                        ll_select.setVisibility(View.GONE);
                        MyTerminalFactory.getSDK().getTempGroupManager().destroyTempGroup4PC(groupId);
                        TimerTask task =new TimerTask() {
                            @Override
                            public void run() {
                                alertDialog.dismiss();
                                finish();
                            }
                        };
                        MyTerminalFactory.getSDK().getTimer().schedule(task,1000);

                    }));
                    Button btn_cancel = window.findViewById(R.id.btn_cancel);
                    btn_cancel.setOnClickListener(v -> alertDialog.dismiss());
                }else {
                    List<Member> deleteMemberList = sortAdapter.getDeleteMemberList();
                    MyTerminalFactory.getSDK().getTempGroupManager().removeMemberToTempGroup(groupId,MyTerminalFactory.getSDK().getParam(Params.MEMBER_ID,0),deleteMemberList);
                    add_btn.setVisibility(View.VISIBLE);
                    delete_btn.setVisibility(View.VISIBLE);
                    delete_text.setVisibility(View.GONE);
                    cancel_text.setVisibility(View.GONE);
                }
                break;
            case R.id.cancel_text:
                add_btn.setVisibility(View.VISIBLE);
                delete_btn.setVisibility(View.VISIBLE);
                delete_text.setVisibility(View.GONE);
                cancel_text.setVisibility(View.GONE);
                sortAdapter = new GroupMemberAdapter(GroupMemberActivity.this, currentGroupMembers, false);
                memberList.setAdapter(sortAdapter);
                sortAdapter.notifyDataSetChanged();
                break;
        }
    }

    /**
     * 获取会话组当前成员列表
     **/
    private ReceiveGetGroupCurrentOnlineMemberListHandler mReceiveGetGroupCurrentOnlineMemberListHandler = new ReceiveGetGroupCurrentOnlineMemberListHandler() {
        @Override
        public void handler(final List<Member> memberList,boolean isAllMember,int groupId) {
            myHandler.post(() -> {
                if(isFinishing()){
                    return;
                }
                GroupMemberActivity.this.groupId = groupId;
                Group group = DataUtil.getGroupByGroupNo(groupId);
                isTemporaryGroup = group.getDepartmentId() == -1;
                //只有自己创建的临时组才能添加和删除人
                if(group.getCreatedMemberNo() == MyTerminalFactory.getSDK().getParam(Params.MEMBER_ID,-1) && isTemporaryGroup){
                    canAdd = true;
                }
                currentGroupMembers.clear();
                currentGroupMembers.addAll(memberList);
                for(Member member : currentGroupMembers){
                    if(member.isChecked()){
                        member.setChecked(false);
                    }
                }
                Collections.sort(currentGroupMembers);
                if (sortAdapter != null) {
                    sortAdapter.notifyDataSetChanged();
                }
                rightBtn.setVisibility(View.GONE);
                ok_btn.setVisibility(View.GONE);
                if(isTemporaryGroup){
                    in_title_bar.setVisibility(View.GONE);
                    temp_title_bar.setVisibility(View.VISIBLE);
                    rightBtn.setVisibility(View.GONE);
                    ok_btn.setVisibility(View.GONE);
                    add_btn.setVisibility(canAdd?View.VISIBLE:View.GONE);
                    barTitle.setText(R.string.text_group_members);
                    memberNum.setText(String.format(getString(R.string.text_group_members_number),currentGroupMembers.size()));
                    logger.info("组内成员：" + currentGroupMembers.size());
                }else {
                    in_title_bar.setVisibility(View.VISIBLE);
                    temp_title_bar.setVisibility(View.GONE);
                    barTitle.setText(R.string.text_intra_group_line_members);
                    memberNum.setText(String.format(getString(R.string.text_intra_group_line_members_number),currentGroupMembers.size()));
                    logger.info("组内在线成员：" + currentGroupMembers.size());
                }
            });
        }
    };

    private ReceiveResponseRemoveMemberToTempGroupMessageHandler mReceiveResponseRemoveMemberToTempGroupMessageHandler = new ReceiveResponseRemoveMemberToTempGroupMessageHandler(){
        @Override
        public void handler(int methodResult, String resultDesc, int tempGroupNo){
            if(methodResult == BaseCommonCode.SUCCESS_CODE){
                if(tempGroupNo == groupId && canAdd){
                    myHandler.postDelayed(() -> {
                        MyTerminalFactory.getSDK().getGroupManager().getGroupCurrentOnlineMemberList(groupId, true);
                        ToastUtil.showToast(GroupMemberActivity.this,getString(R.string.text_delete_success));
                    },2000);

                }
            }
        }
    };

    private ReceiveResponseAddMemberToTempGroupMessageHandler mReceiveResponseAddMemberToTempGroupMessageHandler = new ReceiveResponseAddMemberToTempGroupMessageHandler(){
        @Override
        public void handler(int methodResult, String resultDesc, int tempGroupNo){
            if(methodResult == BaseCommonCode.SUCCESS_CODE){
                if(tempGroupNo == groupId && canAdd){
                    myHandler.postDelayed(() -> {
                        MyTerminalFactory.getSDK().getGroupManager().getGroupCurrentOnlineMemberList(groupId, true);
                        ToastUtil.showToast(GroupMemberActivity.this,getString(R.string.text_add_success));
                    },2000);
                }
            }
        }
    };
}
