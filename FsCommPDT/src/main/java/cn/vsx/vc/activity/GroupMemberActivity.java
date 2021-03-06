package cn.vsx.vc.activity;

import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
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

import com.blankj.utilcode.util.ToastUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cn.vsx.hamster.common.GroupType;
import cn.vsx.hamster.common.TempGroupType;
import cn.vsx.hamster.common.TerminalMemberStatusEnum;
import cn.vsx.hamster.errcode.BaseCommonCode;
import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.model.Group;
import cn.vsx.hamster.terminalsdk.model.Member;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveGetGroupCurrentOnlineMemberListHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveMemberAboutTempGroupHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveResponseAddMemberToTempGroupMessageHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveResponseDestroyTempGroup4PCHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveResponseRemoveMemberToTempGroupMessageHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveTempGroupMembersHandler;
import cn.vsx.hamster.terminalsdk.tools.DataUtil;
import cn.vsx.hamster.terminalsdk.tools.Params;
import cn.vsx.vc.R;
import cn.vsx.vc.adapter.GroupMemberAdapter;
import cn.vsx.vc.application.MyApplication;
import cn.vsx.vc.dialog.DestroyTemporaryGroupsDialog;
import cn.vsx.vc.utils.CallPhoneUtil;
import cn.vsx.vc.utils.Constants;
import cn.vsx.vc.utils.ToastUtil;
import cn.vsx.vc.view.VolumeViewLayout;
import ptt.terminalsdk.context.MyTerminalFactory;

/**
 * Created by Administrator on 2017/3/15 0015.
 */

public class GroupMemberActivity extends BaseActivity implements View.OnClickListener{


    ImageView newsBarBack;
    ImageView newsBarBackTemp;
    TextView barTitle;
    TextView temp_bar_title;
    LinearLayout in_title_bar;
    ImageView rightBtn;
    Button ok_btn;
    LinearLayout temp_title_bar;
    ImageView add_btn;
    ImageView delete_btn;
    TextView cancel_text;
    TextView delete_text;
    LinearLayout ll_member_num;
    TextView memberNum;
    ListView memberList;

    VolumeViewLayout volumeViewLayout;
    private GroupMemberAdapter sortAdapter;
    private Handler myHandler = new Handler(Looper.getMainLooper());
    private List<Member> currentGroupMembers = new ArrayList<>();
    private int groupId;
    String groupName;
    private boolean canDelete;//只有自己创建的临时组才能删除人
    private boolean isTemporaryGroup;
    private DestroyTemporaryGroupsDialog destroyDialog;
    private boolean isDestroyTempGroup = false;
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
        newsBarBack = (ImageView) findViewById(R.id.news_bar_back);
        newsBarBackTemp = (ImageView) findViewById(R.id.news_bar_back_temp);
        barTitle = (TextView) findViewById(R.id.bar_title);
        temp_bar_title = (TextView) findViewById(R.id.temp_bar_title);
        in_title_bar = (LinearLayout) findViewById(R.id.in_title_bar);
        rightBtn = (ImageView) findViewById(R.id.right_btn);
        ok_btn = (Button) findViewById(R.id.ok_btn);
        temp_title_bar = (LinearLayout) findViewById(R.id.temp_title_bar);
        add_btn = (ImageView) findViewById(R.id.add_btn);




        delete_btn = (ImageView) findViewById(R.id.delete_btn);
        cancel_text = (TextView) findViewById(R.id.cancel_text);
        delete_text = (TextView) findViewById(R.id.delete_text);
        ll_member_num = (LinearLayout) findViewById(R.id.ll_member_num);
        memberNum = (TextView) findViewById(R.id.member_num);
        memberList = (ListView) findViewById(R.id.member_list);
        volumeViewLayout = (VolumeViewLayout) findViewById(R.id.volume_layout);
        WindowManager windowManager = (WindowManager) this.getSystemService(WINDOW_SERVICE);
        findViewById(R.id.delete_text).setOnClickListener(this);
        findViewById(R.id.cancel_text).setOnClickListener(this);
        findViewById(R.id.add_btn).setOnClickListener(this);
        findViewById(R.id.delete_btn).setOnClickListener(this);
        findViewById(R.id.news_bar_back_temp).setOnClickListener(this);
        findViewById(R.id.right_btn).setOnClickListener(this);
        findViewById(R.id.news_bar_back).setOnClickListener(this);
        DisplayMetrics dm = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(dm);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(isTemporaryGroup){
            TerminalFactory.getSDK().getThreadPool().execute(() -> TerminalFactory.getSDK().getDataManager().getMemberByTempNo(groupId));
        }else {
            MyTerminalFactory.getSDK().getGroupManager().getGroupCurrentOnlineMemberListNewMethod(groupId, TerminalMemberStatusEnum.ONLINE.toString());
        }
    }

    @Override
    public void initListener() {
        MyTerminalFactory.getSDK().registReceiveHandler(mReceiveGetGroupCurrentOnlineMemberListHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveTempGroupMembersHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(mReceiveResponseRemoveMemberToTempGroupMessageHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(mReceiveResponseAddMemberToTempGroupMessageHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveMemberAboutTempGroupHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveResponseDestroyTempGroup4PCHandler);
    }

    @Override
    public void initData() {
        isDestroyTempGroup = false;
        groupId = getIntent().getIntExtra("groupId", 0);
        groupName = getIntent().getStringExtra("groupName");
        Group group = DataUtil.getTempGroupByGroupNo(groupId);
        if(null ==group){
            group = DataUtil.getGroupByGroupNo(groupId);
        }
        logger.info( "临时组---group:" + group);
        if(group != null){
            isTemporaryGroup = GroupType.TEMPORARY.toString().equals(group.getGroupType());
            if(group.getCreatedMemberUniqueNo() == MyTerminalFactory.getSDK().getParam(Params.MEMBER_UNIQUENO,-1L) && isTemporaryGroup){
                canDelete = true;
            }
            rightBtn.setVisibility(View.GONE);
            ok_btn.setVisibility(View.GONE);
            logger.info( "是否为我创建的临时组:" + canDelete+"-----groupNo："+groupId);
            if(isTemporaryGroup){
                in_title_bar.setVisibility(View.GONE);
                temp_title_bar.setVisibility(View.VISIBLE);
                rightBtn.setVisibility(View.GONE);
                ok_btn.setVisibility(View.GONE);
                add_btn.setVisibility(View.VISIBLE);
                if(!canDelete){
                    delete_btn.setVisibility(View.GONE);
                }else {
                    delete_btn.setVisibility(View.VISIBLE);
                }
                temp_bar_title.setText(String.format(getString(R.string.text_temp_group_members_title),0,0));
                ll_member_num.setVisibility(View.GONE);
            }else {
                add_btn.setVisibility(View.GONE);
                delete_btn.setVisibility(View.GONE);
                in_title_bar.setVisibility(View.VISIBLE);
                temp_title_bar.setVisibility(View.GONE);
                ll_member_num.setVisibility(View.VISIBLE);
            }

            sortAdapter = new GroupMemberAdapter(GroupMemberActivity.this, currentGroupMembers, false,isTemporaryGroup);
            memberList.setAdapter(sortAdapter);
            //只有自己创建的临时组才能添加和删除人
        }
    }

    @Override
    public void doOtherDestroy() {
        dismissDialog();
        MyTerminalFactory.getSDK().unregistReceiveHandler(mReceiveGetGroupCurrentOnlineMemberListHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveTempGroupMembersHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(mReceiveResponseRemoveMemberToTempGroupMessageHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(mReceiveResponseAddMemberToTempGroupMessageHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveMemberAboutTempGroupHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveResponseDestroyTempGroup4PCHandler);
        if (volumeViewLayout != null) {
            volumeViewLayout.unRegistLintener();
        }
    }

    private long lastSearchTime=0;


    @Override
    public void onClick(View view) {
        long currentTime = System.currentTimeMillis();
        if( currentTime - lastSearchTime<1000){
            return;
        }
        lastSearchTime= currentTime;
        int i = view.getId();
        if(i == R.id.news_bar_back){
            finish();
        }else if(i == R.id.news_bar_back_temp){
            finish();
        }else if(i == R.id.add_btn){
            IncreaseTemporaryGroupMemberActivity.startActivity(GroupMemberActivity.this, Constants.INCREASE_MEMBER, groupId);
        }else if(i == R.id.delete_btn){
            add_btn.setVisibility(View.GONE);
            delete_btn.setVisibility(View.GONE);
            delete_text.setVisibility(View.VISIBLE);
            cancel_text.setVisibility(View.VISIBLE);
            clearCheckStatus();
            sortAdapter = new GroupMemberAdapter(GroupMemberActivity.this, currentGroupMembers, true,isTemporaryGroup);
            sortAdapter.setOnItemClickListener((view1,position,member) -> {
                setDeleteCountText();
            });
            memberList.setAdapter(sortAdapter);
            sortAdapter.notifyDataSetChanged();
            setDeleteCountText();
        }else if(i == R.id.delete_text){
            if (sortAdapter!=null) {
                List<Long> deleteMemberList = sortAdapter.getDeleteMemberList();
                //判断是否选择了账号
                if(deleteMemberList.isEmpty()) {
                    ToastUtils.showShort(R.string.please_select_delete_member);
                    return;
                }
                List<Member> tempList = new ArrayList<>(currentGroupMembers);
                //删除自己
                tempList.remove(new Member(MyTerminalFactory.getSDK().getParam(Params.MEMBER_UNIQUENO,0L)));
                //删除选择的
                for (Long uniqueNo: deleteMemberList) {
                    tempList.remove(new Member(uniqueNo));
                }
                //判断是否为空
                if(tempList.isEmpty()){
                    showDestroyTempGroupDialog();
                }else{
                    MyTerminalFactory.getSDK().getTempGroupManager().removeMemberToTempGroup(groupId, MyTerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0),
                            MyTerminalFactory.getSDK().getParam(Params.MEMBER_UNIQUENO, 0L), deleteMemberList);
                    add_btn.setVisibility(View.VISIBLE);
                    delete_btn.setVisibility(View.VISIBLE);
                    delete_text.setVisibility(View.GONE);
                    cancel_text.setVisibility(View.GONE);
                }
            }
        }else if(i == R.id.cancel_text){
            add_btn.setVisibility(View.VISIBLE);
            delete_btn.setVisibility(View.VISIBLE);
            delete_text.setVisibility(View.GONE);
            cancel_text.setVisibility(View.GONE);
            sortAdapter = new GroupMemberAdapter(GroupMemberActivity.this, currentGroupMembers, false,isTemporaryGroup);
            memberList.setAdapter(sortAdapter);
            sortAdapter.notifyDataSetChanged();
        }
    }

    private void setDeleteCountText(){
        if(sortAdapter!=null && !sortAdapter.getDeleteMemberList().isEmpty()){
            if(delete_text!=null){
                delete_text.setText(String.format(getString(R.string.button_delete_number), sortAdapter.getDeleteMemberList().size()));
            }
        }else{
            if(delete_text!=null){
                delete_text.setText(R.string.text_delete);
            }
        }
    }

    /**
     * 获取会话组当前成员列表
     **/
    private ReceiveGetGroupCurrentOnlineMemberListHandler mReceiveGetGroupCurrentOnlineMemberListHandler = new ReceiveGetGroupCurrentOnlineMemberListHandler() {
        @Override
        public void handler(final List<Member> memberList,final String status,int groupId) {
            myHandler.post(() -> {
                if(isFinishing()){
                    return;
                }
                if(groupId == GroupMemberActivity.this.groupId && !isTemporaryGroup){
                    Group group =DataUtil.getTempGroupByGroupNo(groupId);
                    if(null ==group){
                        group = DataUtil.getGroupByGroupNo(groupId);
                    }
                    if(group !=null){
                        isTemporaryGroup = GroupType.TEMPORARY.toString().equals(group.getGroupType());
                        //只有自己创建的临时组才能添加和删除人
                        if(group.getCreatedMemberUniqueNo() == MyTerminalFactory.getSDK().getParam(Params.MEMBER_UNIQUENO,-1L) && isTemporaryGroup){
                            canDelete = true;
                        }

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
//
                    add_btn.setVisibility(View.GONE);
                    delete_btn.setVisibility(View.GONE);
                    in_title_bar.setVisibility(View.VISIBLE);
                    temp_title_bar.setVisibility(View.GONE);
                    barTitle.setText(R.string.text_intra_group_line_members);
                    memberNum.setText(String.format(getString(R.string.text_intra_group_line_members_number),currentGroupMembers.size()));
                    logger.info("组内在线成员：" + currentGroupMembers.size());
                }
            });
        }
    };

    /**
     * 获取临时组的成员列表
     */
    private ReceiveTempGroupMembersHandler receiveTempGroupMembersHandler = new ReceiveTempGroupMembersHandler(){
        @Override
        public void handler(int groupId,List<Member> members,int total,int onlineNumber,int offlineNumber){
            myHandler.post(() -> {
                if(isFinishing()){
                    return;
                }
                if(groupId == GroupMemberActivity.this.groupId && isTemporaryGroup){
                    currentGroupMembers.clear();
                    currentGroupMembers.addAll(members);
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
                    in_title_bar.setVisibility(View.GONE);
                    temp_title_bar.setVisibility(View.VISIBLE);
                    temp_bar_title.setText(String.format(getString(R.string.text_temp_group_members_title),onlineNumber,total));
                    ll_member_num.setVisibility(View.GONE);
                    if(delete_text.getVisibility() == View.VISIBLE && cancel_text.getVisibility() == View.VISIBLE){
                        setDeleteCountText();
                        add_btn.setVisibility(View.GONE);
                        delete_btn.setVisibility(View.GONE);
                    }else{
                        add_btn.setVisibility(View.VISIBLE);
                        delete_btn.setVisibility(canDelete?View.VISIBLE:View.GONE);
                    }
                }
            });
        }
    };

    private ReceiveResponseRemoveMemberToTempGroupMessageHandler mReceiveResponseRemoveMemberToTempGroupMessageHandler = new ReceiveResponseRemoveMemberToTempGroupMessageHandler(){
        @Override
        public void handler(int methodResult, String resultDesc, int tempGroupNo){
            if(methodResult == BaseCommonCode.SUCCESS_CODE){
                if(tempGroupNo == groupId && canDelete){
                    TerminalFactory.getSDK().getDataManager().getMemberByTempNo(groupId);
                    myHandler.post(()->{
                        add_btn.setVisibility(View.VISIBLE);
                        delete_btn.setVisibility(View.VISIBLE);
                        delete_text.setVisibility(View.GONE);
                        cancel_text.setVisibility(View.GONE);
                        sortAdapter.setDelete(false);
                        sortAdapter.notifyDataSetChanged();
                    });
                    ToastUtil.showToast(GroupMemberActivity.this,getString(R.string.text_delete_success));
                }
            }
        }
    };

    private ReceiveResponseAddMemberToTempGroupMessageHandler mReceiveResponseAddMemberToTempGroupMessageHandler = new ReceiveResponseAddMemberToTempGroupMessageHandler(){
        @Override
        public void handler(int methodResult, String resultDesc, int tempGroupNo){
            if(methodResult == BaseCommonCode.SUCCESS_CODE){
                if(tempGroupNo == groupId && canDelete){
                    myHandler.postDelayed(() -> {
                        if(isTemporaryGroup){
                            TerminalFactory.getSDK().getThreadPool().execute(() -> TerminalFactory.getSDK().getDataManager().getMemberByTempNo(groupId));
                        }else {
                            MyTerminalFactory.getSDK().getGroupManager().getGroupCurrentOnlineMemberListNewMethod(groupId, TerminalMemberStatusEnum.ONLINE.toString());
                        }
                        ToastUtil.showToast(GroupMemberActivity.this,getString(R.string.text_add_success));
                    },2000);
                }
            }
        }
    };

    private ReceiveMemberAboutTempGroupHandler receiveMemberAboutTempGroupHandler = new ReceiveMemberAboutTempGroupHandler() {
        @Override
        public void handler(boolean isAdd, boolean isLocked, boolean isScan, boolean isSwitch, int tempGroupNo, String tempGroupName, String tempGroupType) {
            if (!TempGroupType.ACTIVITY_TEAM_GROUP.toString().equals(tempGroupType)) {
                if (!isAdd && tempGroupNo == groupId) {
                    try{
                        if(!isDestroyTempGroup){
                            myHandler.post(GroupMemberActivity.this::finish);
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
        }
    };
    private ReceiveResponseDestroyTempGroup4PCHandler receiveResponseDestroyTempGroup4PCHandler = new ReceiveResponseDestroyTempGroup4PCHandler() {
        @Override
        public void handler(int tempGroupNo, int resultCode, String resultDesc) {
            if(resultCode == BaseCommonCode.SUCCESS_CODE){
                myHandler.post(() -> updateDialog(DestroyTemporaryGroupsDialog.STATE_SUCCESS, ""));
                myHandler.postDelayed(() -> { dismissDialog();GroupMemberActivity.this.finish(); },1000);
            }else{
                myHandler.post(() -> updateDialog(DestroyTemporaryGroupsDialog.STATE_FAIL, resultDesc));
                myHandler.postDelayed(() -> { dismissDialog(); },1000);
            }
        }
    };


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults){
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == CallPhoneUtil.PHONE_PERMISSIONS_REQUEST_CODE){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //同意，拨打电话
                CallPhoneUtil.callPhone( GroupMemberActivity.this, TerminalFactory.getSDK().getParam(Params.TEMP_CALL_PHONE_NUMBER,""));
            }else {
                //不同意，提示
                cn.vsx.vc.utils.ToastUtil.showToast(MyApplication.instance, getString(R.string.text_call_phone_not_open_call_is_unenabled));
            }
        }
    }

    /**
     * 清空选择的状态
     */
    private void clearCheckStatus() {
        for (Member member: currentGroupMembers) {
            if(member!=null){
                member.isChecked = false;
            }
        }
    }

    /**
     * 提示销毁临时组
     */
    private void showDestroyTempGroupDialog() {
        try{
            //当前组仅剩创建者本身的时候点击删除销临时组
            isDestroyTempGroup = false;
            destroyDialog = new DestroyTemporaryGroupsDialog(GroupMemberActivity.this,
                    new DestroyTemporaryGroupsDialog.OnClickListener() {
                @Override
                public void onConfirm() {
                    isDestroyTempGroup = true;
                    destroyTempGroup();
                }

                @Override
                public void onCancel() {
                    dismissDialog();
                }
            });
            updateDialog(DestroyTemporaryGroupsDialog.STATE_INIT,"");
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 发送销毁临时组
     */
    private void destroyTempGroup() {
        TerminalFactory.getSDK().getThreadPool().execute(() -> {
            MyTerminalFactory.getSDK().getTempGroupManager().destroyTempGroup(groupId, (sendOk, uuid) -> {
                myHandler.post(() -> {
                    if(sendOk){
                        updateDialog(DestroyTemporaryGroupsDialog.STATE_DESTROYING, "");
                    }else{
                        updateDialog(DestroyTemporaryGroupsDialog.STATE_FAIL,getResources().getString(R.string.text_disbanded_fail_by_send_fail));
                        myHandler.postDelayed(this::dismissDialog,1000);
                    }
                });
            });
        });
    }

    /**
     * 检查弹窗是否正在显示
     * @return
     */
    private boolean checkDialogShowing(){
        return (destroyDialog!=null&&destroyDialog.isShowing());
    }

    /**
     * 检查是否可以显示弹窗
     * @return
     */
    private boolean checkCanShow(){
        return (!GroupMemberActivity.this.isFinishing() && destroyDialog!=null);
    }

    private void updateDialog(int type,String failMessage){
        if(checkCanShow()){
            destroyDialog.updateDialog(type, failMessage);
        }
    }

    /**
     * 关闭弹窗
     */
    private void dismissDialog(){
        if(destroyDialog!=null){
            destroyDialog.dismiss();
            destroyDialog = null;
        }
        isDestroyTempGroup = false;
    }

}
