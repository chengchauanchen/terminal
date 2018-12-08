package cn.vsx.vc.activity;

import android.content.Intent;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
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

import butterknife.Bind;
import butterknife.OnClick;
import cn.vsx.hamster.errcode.BaseCommonCode;
import cn.vsx.hamster.terminalsdk.model.Member;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveGetGroupCurrentOnlineMemberListHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveResponseAddMemberToTempGroupMessageHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveResponseRemoveMemberToTempGroupMessageHandler;
import cn.vsx.hamster.terminalsdk.tools.Params;
import cn.vsx.vc.R;
import cn.vsx.vc.adapter.GroupMemberAdapter;
import cn.vsx.vc.application.MyApplication;
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
//    @Bind(R.id.select_all)
//    LinearLayout selectAll;
//    @Bind(R.id.find_history)
//    LinearLayout findHistory;
    @Bind(R.id.volume_layout)
    VolumeViewLayout volumeViewLayout;
    private WindowManager windowManager;
    private GroupMemberAdapter sortAdapter;
    private Handler myHandler = new Handler();
    private List<Member> currentGroupMembers = new ArrayList<>();
    private int listviewHight;
    private int screenWidth;
    private int groupId;
    String groupName;
    private boolean isTemporaryGroup;
    private int total=0;

    public void setListViewHeightBasedOnChildren(ListView listView) {
        if (listView == null)
            return;
        //sortAdapter = (GroupMemberAdapter) listView.getAdapter();
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
        listviewHight = params.height;
//        listView.setLayoutParams(params);


    }

    @Override
    public int getLayoutResId() {
        return R.layout.activity_group_member;
    }

    @Override
    public void initView() {
        // 获取屏幕宽度
        windowManager = (WindowManager) this.getSystemService(WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(dm);
        screenWidth = dm.heightPixels;

//        OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiveVolumeOffCallHandler.class, false, 0);
//        sortAdapter = new GroupMemberAdapter(GroupMemberActivity.this, currentGroupMembers, true);
//        memberList.setAdapter(sortAdapter);
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
        isTemporaryGroup = DataUtil.getGroupByGroupNo(groupId).getDepartmentId() == -1;
        rightBtn.setVisibility(View.GONE);
        ok_btn.setVisibility(View.GONE);

        if(isTemporaryGroup){
            in_title_bar.setVisibility(View.GONE);
            temp_title_bar.setVisibility(View.VISIBLE);
            rightBtn.setVisibility(View.GONE);
            ok_btn.setVisibility(View.GONE);
        }else {
            in_title_bar.setVisibility(View.VISIBLE);
            temp_title_bar.setVisibility(View.GONE);
        }

        sortAdapter = new GroupMemberAdapter(GroupMemberActivity.this, currentGroupMembers, false);
//        memberList.addFooterView(new View(GroupMemberActivity.this), null, true);
//        memberList.setFooterDividersEnabled(false);
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
    private long currentTime=0;

    @OnClick({R.id.news_bar_back, R.id.right_btn,R.id.news_bar_back_temp,R.id.delete_btn,R.id.add_btn,R.id.cancel_text,R.id.delete_text})
    public void onClick(View view) {
        currentTime=System.currentTimeMillis();
        if( currentTime- lastSearchTime<1000){
            return;
        }
        lastSearchTime=currentTime;

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
                Intent intent = new Intent(GroupMemberActivity.this, IncreaseTemporaryGroupMemberActivity.class);
                intent.putExtra("type",1);
                startActivity(intent);
                break;
            case R.id.delete_btn:
                add_btn.setVisibility(View.GONE);
                delete_btn.setVisibility(View.GONE);
                delete_text.setVisibility(View.VISIBLE);
                cancel_text.setVisibility(View.VISIBLE);

                sortAdapter = new GroupMemberAdapter(GroupMemberActivity.this, currentGroupMembers, true);
                sortAdapter.setOnItemClickListener(new GroupMemberAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position, boolean checked, Member member) {
                        if(checked){
                            total++;
                        }else {
                            total--;
                        }
                        if(total>0){
                            delete_text.setText("删除(" + total + ")");
                        }else {
                            delete_text.setText("删除");
                        }
                    }
                });
                memberList.setAdapter(sortAdapter);
                sortAdapter.notifyDataSetChanged();
                break;
            case R.id.delete_text:
                if(MyTerminalFactory.getSDK().getConfigManager().getCurrentGroupMembers().size()<2){//当前组仅剩创建者本身的时候点击删除销临时组
                    MyTerminalFactory.getSDK().getTempGroupManager().destroyTempGroup4PC(MyTerminalFactory.getSDK().getParam(Params.CURRENT_GROUP_ID,0));
                    ToastUtil.showToast(getApplicationContext(),"临时组已销毁");
                    finish();
                }else {
                    List<Member> deleteMemberList = sortAdapter.getDeleteMemberList();
                    MyTerminalFactory.getSDK().getTempGroupManager().removeMemberToTempGroup(MyTerminalFactory.getSDK().getParam(Params.CURRENT_GROUP_ID,0),MyTerminalFactory.getSDK().getParam(Params.MEMBER_ID,0),deleteMemberList);
                    add_btn.setVisibility(View.VISIBLE);
                    delete_btn.setVisibility(View.VISIBLE);
                    delete_text.setVisibility(View.GONE);
                    cancel_text.setVisibility(View.GONE);
                }


//                sortAdapter = new GroupMemberAdapter(GroupMemberActivity.this, currentGroupMembers, false);
//                memberList.setAdapter(sortAdapter);
//                sortAdapter.notifyDataSetChanged();
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
        public void handler(final List<Member> memberList,String type) {
            myHandler.post(new Runnable() {
                @Override
                public void run() {
                    if(isFinishing()){
                        return;
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

                    if(isTemporaryGroup){
                        in_title_bar.setVisibility(View.GONE);
                        temp_title_bar.setVisibility(View.VISIBLE);
                        barTitle.setText("组内成员");
                        memberNum.setText("组内成员" + (currentGroupMembers.size()) + "人");
                        logger.info("组内成员：" + currentGroupMembers.size());
                    }else {
                        in_title_bar.setVisibility(View.VISIBLE);
                        temp_title_bar.setVisibility(View.GONE);
                        barTitle.setText("组内在线成员");
                        memberNum.setText("组内在线成员" + (currentGroupMembers.size()) + "人");
                        logger.info("组内在线成员：" + currentGroupMembers.size());
                    }
//                    if (memberList.size() > 5){
//                        selectAll.setVisibility(View.VISIBLE);
//                    }
//                    else{
//                        selectAll.setVisibility(View.GONE);
//                    }
                }
            });
        }
    };

    private ReceiveResponseRemoveMemberToTempGroupMessageHandler mReceiveResponseRemoveMemberToTempGroupMessageHandler = new ReceiveResponseRemoveMemberToTempGroupMessageHandler(){
        @Override
        public void handler(int methodResult, String resultDesc, int tempGroupNo){
            if(methodResult == BaseCommonCode.SUCCESS_CODE){
                if(tempGroupNo == groupId){
                    myHandler.postDelayed(new Runnable(){
                        @Override
                        public void run(){
                            MyTerminalFactory.getSDK().getGroupManager().getGroupCurrentOnlineMemberList(groupId, true);
                            ToastUtil.showToast(GroupMemberActivity.this,"删除成功");
                        }
                    },2000);

                }
            }
        }
    };

    private ReceiveResponseAddMemberToTempGroupMessageHandler mReceiveResponseAddMemberToTempGroupMessageHandler = new ReceiveResponseAddMemberToTempGroupMessageHandler(){
        @Override
        public void handler(int methodResult, String resultDesc, int tempGroupNo){
            if(methodResult == BaseCommonCode.SUCCESS_CODE){
                if(tempGroupNo == groupId){
                    if(isTemporaryGroup){
                        myHandler.postDelayed(new Runnable(){
                            @Override
                            public void run(){
                                MyTerminalFactory.getSDK().getGroupManager().getGroupCurrentOnlineMemberList(groupId, true);
                                ToastUtil.showToast(GroupMemberActivity.this,"添加成功");
                            }
                        },2000);
                    }
                }
            }
        }
    };
}
