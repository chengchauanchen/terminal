package cn.vsx.vc.service;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;

import org.apache.http.util.TextUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import cn.vsx.hamster.common.Authority;
import cn.vsx.hamster.common.MessageSendStateEnum;
import cn.vsx.hamster.common.MessageType;
import cn.vsx.hamster.common.util.JsonParam;
import cn.vsx.hamster.common.util.NoCodec;
import cn.vsx.hamster.errcode.BaseCommonCode;
import cn.vsx.hamster.errcode.module.TerminalErrorCode;
import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.model.Member;
import cn.vsx.hamster.terminalsdk.model.MemberResponse;
import cn.vsx.hamster.terminalsdk.model.TerminalMessage;
import cn.vsx.hamster.terminalsdk.model.VideoMember;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveNotifyLivingStoppedHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveUpdatePhoneMemberHandler;
import cn.vsx.hamster.terminalsdk.tools.Params;
import cn.vsx.vc.R;
import cn.vsx.vc.adapter.CatalogAdapter;
import cn.vsx.vc.adapter.LiveRecyclerViewAdapter;
import cn.vsx.vc.application.MyApplication;
import cn.vsx.vc.model.CatalogBean;
import cn.vsx.vc.model.ContactItemBean;
import cn.vsx.vc.receiveHandle.ReceiveRemoveSwitchCameraViewHandler;
import cn.vsx.vc.utils.Constants;
import cn.vsx.vc.utils.InputMethodUtil;
import ptt.terminalsdk.context.MyTerminalFactory;
import ptt.terminalsdk.tools.ToastUtil;

public class InviteMemberService extends BaseService{


    private ImageView mIvLiveEditReturn;
    private EditText mEtLiveEditImportTheme;
    private Button mBtnLiveEditConfirm;
    private ImageView mIvLiveSelectmemberReturn;
    private Button mBtnLiveSelectmemberStart;
    private LinearLayout mLlLiveSelectmemberTheme;
    private TextView mTvLiveSelectmemberTheme;
    private LinearLayout mLlNoInfo;
    private TextView mTvNoUser;
    private ImageView mIvSearch;
    private RecyclerView mCatalogRecyclerview;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView mRecyclerview;
    private LinearLayout mLlEditTheme;
    private LinearLayout mLlSelectMember;
    private String type;
    private boolean pushing;
    private boolean pulling;

    private List<CatalogBean> mCatalogList=new ArrayList<>();
    private List<ContactItemBean> mDatas=new ArrayList<>();
    private CatalogAdapter mCatalogAdapter;
    private LiveRecyclerViewAdapter mContactAdapter;
    private List<CatalogBean> mInitCatalogList=new ArrayList<>();
    private ArrayList<VideoMember> watchingmembers;
    private int livingMemberId;
    private boolean gb28181Pull;
    private TerminalMessage oldTerminalMessage;

    public InviteMemberService(){}

    @SuppressLint("InflateParams")
    @Override
    protected void setRootView(){
        rootView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.layout_invite_member, null);
    }

    @Override
    protected void findView(){
        mLlEditTheme = rootView.findViewById(R.id.ll_live_edit_theme);
        mIvLiveEditReturn = rootView.findViewById(R.id.iv_live_edit_return);
        mEtLiveEditImportTheme = rootView.findViewById(R.id.et_live_edit_import_theme);
        mBtnLiveEditConfirm = rootView.findViewById(R.id.btn_live_edit_confirm);
        mLlSelectMember = rootView.findViewById(R.id.live_select_member);
        mIvLiveSelectmemberReturn = rootView.findViewById(R.id.iv_live_selectmember_return);
        mBtnLiveSelectmemberStart = rootView.findViewById(R.id.btn_live_selectmember_start);
        mLlLiveSelectmemberTheme = rootView.findViewById(R.id.ll_live_selectmember_theme);
        mTvLiveSelectmemberTheme = rootView.findViewById(R.id.tv_live_selectmember_theme);

        mLlNoInfo = rootView.findViewById(R.id.ll_no_info);
        mTvNoUser = rootView.findViewById(R.id.tv_no_user);

        mCatalogRecyclerview =  rootView.findViewById(R.id.catalog_recyclerview);
        mIvSearch =  rootView.findViewById(R.id.iv_search);
        mSwipeRefreshLayout =  rootView.findViewById(R.id.swipeRefreshLayout);
        mRecyclerview =  rootView.findViewById(R.id.recyclerview);
    }

    protected void initListener(){
        mIvLiveSelectmemberReturn.setOnClickListener(returnOnClickListener);
        mBtnLiveEditConfirm.setOnClickListener(editThemeConfirmOnClickListener);
        mLlLiveSelectmemberTheme.setOnClickListener(editThemeOnClickListener);
        mIvLiveEditReturn.setOnClickListener(editThemeReturnOnClickListener);
        mBtnLiveSelectmemberStart.setOnClickListener(startOnClickListener);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveNotifyLivingStoppedHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveUpdatePhoneMemberHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveRemoveSwitchCameraViewHandler);

        mCatalogAdapter.setOnItemClick(catalogItemClickListener);
        mContactAdapter.setOnItemClickListener(contractItemClickListener);
        mSwipeRefreshLayout.setOnRefreshListener(onRefreshListener);
    }

    @Override
    protected void initView(Intent intent){
        gb28181Pull = intent.getBooleanExtra(Constants.GB28181_PULL,false);
        if(gb28181Pull){
            oldTerminalMessage = (TerminalMessage) intent.getSerializableExtra(Constants.TERMINALMESSAGE);
        }
        filterMember(intent);
        if(Constants.PULL.equals(type) || pushing){
            mLlLiveSelectmemberTheme.setVisibility(View.GONE);
        }
    }

    @Override
    protected void showPopMiniView(){
    }

    @Override
    protected void handleMesage(Message msg){
    }

    @Override
    protected void initData(){
        mCatalogRecyclerview.setLayoutManager(new LinearLayoutManager(getApplicationContext(), OrientationHelper.HORIZONTAL,false));
        mCatalogAdapter=new CatalogAdapter(getApplicationContext(),mCatalogList);
        mCatalogRecyclerview.setAdapter(mCatalogAdapter);
        mRecyclerview.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        mContactAdapter=new LiveRecyclerViewAdapter(getApplicationContext(),mDatas,type);
        mRecyclerview.setAdapter(mContactAdapter);
    }

    @Override
    protected void initBroadCastReceiver(){
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        mContactAdapter.getSelectMember().clear();
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveNotifyLivingStoppedHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveUpdatePhoneMemberHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveRemoveSwitchCameraViewHandler);

    }

    private ReceiveRemoveSwitchCameraViewHandler receiveRemoveSwitchCameraViewHandler = this::removeView;

    private ReceiveUpdatePhoneMemberHandler receiveUpdatePhoneMemberHandler = PhoneMember -> mHandler.post(() -> {
        MemberResponse memberResponse = TerminalFactory.getSDK().getConfigManager().getPhoneMemeberInfo();
        if(null == memberResponse){
            return;
        }
        // TODO: 2018/12/28 下拉刷新之后之前选中的人没了，要改
        List<CatalogBean> catalogBeanList = new ArrayList<>();
        CatalogBean bean = new CatalogBean();
        bean.setName(memberResponse.getName());
        bean.setBean(memberResponse);
        catalogBeanList.add(bean);
        updateData(memberResponse,catalogBeanList);
    });
    /**
     * 通知直播停止 通知界面关闭视频页
     **/
    private ReceiveNotifyLivingStoppedHandler receiveNotifyLivingStoppedHandler = (methodResult, resultDesc) -> {
        ToastUtil.showToast(getApplicationContext(),getResources().getString(R.string.push_stoped));
        removeView();
    };

    private View.OnClickListener editThemeOnClickListener = v->{
        mLlEditTheme.setVisibility(View.VISIBLE);
        mLlSelectMember.setVisibility(View.GONE);
    };

    private View.OnClickListener editThemeConfirmOnClickListener = v->{
        if(!TextUtils.isEmpty(mEtLiveEditImportTheme.getText().toString().trim())){
            mLlEditTheme.setVisibility(View.GONE);
            mLlSelectMember.setVisibility(View.VISIBLE);
            mTvLiveSelectmemberTheme.setText(mEtLiveEditImportTheme.getText().toString().trim());
            InputMethodUtil.hideInputMethod(InviteMemberService.this, mEtLiveEditImportTheme);
        }else{
            ToastUtil.showToast(getApplicationContext(),getResources().getString(R.string.theme_cannot_empty));
        }
    };

    private View.OnClickListener returnOnClickListener = v-> removeView();

    private View.OnClickListener editThemeReturnOnClickListener = v->{
        windowManager.updateViewLayout(rootView,layoutParams);
        mLlEditTheme.setVisibility(View.GONE);
        mLlSelectMember.setVisibility(View.VISIBLE);
    };

    private View.OnClickListener startOnClickListener = v->{
        if(Constants.PUSH.equals(type)){
            if(pushing){
                inviteToWatchLive();
            }else{
                if(MyApplication.instance.usbAttached){
                    requestStartLive(Constants.UVC_PUSH);
                }else{
                    if(Constants.HYTERA.equals(Build.MODEL)){
                        requestStartLive(Constants.RECODER_PUSH);
                    }else{
                        requestStartLive(Constants.PHONE_PUSH);
                    }
                }
            }
        }else if(Constants.PULL.equals(type)){
            if(pulling){
                if(gb28181Pull){
                    inviteOtherMemberToWatch(mContactAdapter.getSelectMember());
                }else {
                    inviteToWatchLive();
                }
            }else{
                requestOtherStartLive();
            }
        }
    };


    private CatalogAdapter.ItemClickListener catalogItemClickListener = (view, position) -> {
        MemberResponse memberResponse=mCatalogList.get(position).getBean();
        List<CatalogBean> catalogList = new ArrayList<>(mCatalogList.subList(0, position + 1));
        updateData(memberResponse,catalogList);
    };

    private LiveRecyclerViewAdapter.ItemClickListener contractItemClickListener = (postion, adapterType) -> {
        if (adapterType==Constants.TYPE_DEPARTMENT){
            MemberResponse memberResponse= (MemberResponse) mDatas.get(postion).getBean();
            CatalogBean catalog=new CatalogBean();
            catalog.setName(memberResponse.getName());
            catalog.setBean(memberResponse);
            mCatalogList.add(catalog);
            List<CatalogBean> catalogBeanList = new ArrayList<>(mCatalogList);
            updateData(memberResponse,catalogBeanList);
        }else if(adapterType == Constants.TYPE_USER){
            if(mContactAdapter.getSelectMember().isEmpty()){
                mBtnLiveSelectmemberStart.setText(getResources().getString(R.string.confirm));
            }else {
                mBtnLiveSelectmemberStart.setText(String.format("确定(%s)",mContactAdapter.getSelectMember().size()));
            }
        }
    };

    private SwipeRefreshLayout.OnRefreshListener onRefreshListener = () -> {
        TerminalFactory.getSDK().getConfigManager().updataPhoneMemberInfo();
        mHandler.postDelayed(() -> {
            // 加载完数据设置为不刷新状态，将下拉进度收起来
            mSwipeRefreshLayout.setRefreshing(false);
            // 加载完数据设置为不刷新状态，将下拉进度收起来

        }, 1200);
    };


    /**
     * 设置数据
     */
    private void updateData(MemberResponse memberResponse, List<CatalogBean> catalogBeanList){
        mDatas.clear();
        mCatalogList.clear();
        mCatalogList.addAll(catalogBeanList);
        addData(memberResponse);
        mContactAdapter.notifyDataSetChanged();
        mCatalogAdapter.notifyDataSetChanged();
        mCatalogRecyclerview.scrollToPosition(mCatalogList.size() - 1);

    }

    private void addData(MemberResponse memberResponse){
        if (memberResponse != null){
            addItemMember(memberResponse);
            addItemDepartment(memberResponse);
        }
    }

    /**
     * 添加子成员
     */
    @SuppressWarnings("unchecked")
    private void addItemMember(MemberResponse memberResponse){
        //子成员
        List<Member> memberList = new ArrayList<>(memberResponse.getMembers());
        if(!memberList.isEmpty()){
            List<ContactItemBean> itemMemberList = new ArrayList<>();
            if(Constants.PUSH.equals(type)){
                filterPushMember(memberList);
            }else if(Constants.PULL.equals(type)){
                filterPullMember(memberList);
            }

            for(Member member : memberList){
                if(member.getName()==null){
                    continue;
                }
                member.setChecked(false);
                ContactItemBean<Member> bean = new ContactItemBean<>();
                bean.setBean(member);
                bean.setType(Constants.TYPE_USER);
                itemMemberList.add(bean);
            }
            Collections.sort(itemMemberList);
            mDatas.addAll(itemMemberList);
        }
    }

    private void filterPullMember(List<Member> memberList){
        Iterator<Member> iterator = memberList.iterator();
        while(iterator.hasNext()){
            Member member = iterator.next();
            if(MyTerminalFactory.getSDK().getParam(Params.MEMBER_ID,0) == member.getNo()){
                iterator.remove();
                continue;
            }
            if(livingMemberId !=0 && livingMemberId == member.getNo()){
                iterator.remove();
            }
        }
    }

    private void filterPushMember(List<Member> memberList){
        //去掉正在观看的人和自己
        Iterator<Member> iterator = memberList.iterator();
        while(iterator.hasNext()){
            Member member = iterator.next();
            if(MyTerminalFactory.getSDK().getParam(Params.MEMBER_ID,0) == member.getNo()){
                iterator.remove();
                continue;
            }
            if(null != watchingmembers){
                for(VideoMember watchingmember : watchingmembers){
                    if(watchingmember.getId() == member.getNo()){
                        iterator.remove();
                    }
                }
            }
        }
    }

    /**
     * 添加子部门
     */
    @SuppressWarnings("unchecked")
    private void addItemDepartment(MemberResponse memberResponse){
        List<MemberResponse> data = memberResponse.getChildren();
        if(data!=null && !data.isEmpty()){
            for(MemberResponse next : data){
                if(next.getName() ==null){
                    continue;
                }
                ContactItemBean<MemberResponse> bean = new ContactItemBean<>();
                bean.setType(Constants.TYPE_DEPARTMENT);
                bean.setName(next.getName());
                bean.setBean(next);
                mDatas.add(bean);
                //                Collections.sort(mDatas);
            }
        }
    }


       @SuppressWarnings("unchecked")
    private void filterMember(Intent intent){
        type = intent.getStringExtra(Constants.TYPE);
        mContactAdapter.setType(type);
        pushing = intent.getBooleanExtra(Constants.PUSHING,false);
        watchingmembers = (ArrayList<VideoMember>) intent.getSerializableExtra(Constants.WATCHING_MEMBERS);

        pulling = intent.getBooleanExtra(Constants.PULLING,false);
        livingMemberId = intent.getIntExtra(Constants.LIVING_MEMBER_ID,0);

        MemberResponse mMemberResponse = TerminalFactory.getSDK().getConfigManager().getPhoneMemeberInfo();
        if(mMemberResponse ==null){
            ToastUtil.showToast(getApplicationContext(),getResources().getString(R.string.no_members_data));
            return;
        }
        CatalogBean catalog=new CatalogBean();
        catalog.setName(mMemberResponse.getName());
        catalog.setBean(mMemberResponse);
        mInitCatalogList.add(catalog);
        updateData(mMemberResponse,mInitCatalogList);


    }


    /**
     * 邀请别人来观看
     */
    private void inviteToWatchLive(){
        logger.info("通知别人来观看的列表：" + mContactAdapter.getSelectMember());
        if(!MyTerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_VIDEO_PUSH.name())){
            ToastUtil.showToast(MyApplication.instance, "没有图像推送权限");
            return;
        }
        if(!mContactAdapter.getSelectMember().isEmpty()){

            MyTerminalFactory.getSDK().getLiveManager().requestNotifyWatch(mContactAdapter.getSelectMember(), MyTerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0));
        }
        removeView();
    }


    /**
     * 请求自己开始上报
     */
    private void requestStartLive(String type){
        String theme = mTvLiveSelectmemberTheme.getText().toString().trim();
        Intent intent = new Intent();
        intent.putExtra(Constants.THEME,theme);
        intent.putExtra(Constants.TYPE,Constants.ACTIVE_PUSH);

        intent.putExtra(Constants.PUSH_MEMBERS,mContactAdapter.getSelectMember());
        switch(type){
            case Constants.PHONE_PUSH:
                intent.setClass(this, PhonePushService.class);
                break;
            case Constants.UVC_PUSH:
                intent.setClass(this, SwitchCameraService.class);
                intent.putExtra(Constants.CAMERA_TYPE,Constants.UVC_CAMERA);
                break;
            case Constants.RECODER_PUSH:
                intent.setClass(this, SwitchCameraService.class);
                intent.putExtra(Constants.CAMERA_TYPE,Constants.RECODER_CAMERA);
                break;
        }
        startService(intent);
        removeView();
    }

    /**
     * 请求别人上报
     */
    private void requestOtherStartLive(){
        if(null != mContactAdapter.getLiveMember()){
            int requestCode = MyTerminalFactory.getSDK().getLiveManager().requestMemberLive(mContactAdapter.getLiveMember().getNo(), "");
            logger.error("请求图像：requestCode=" + requestCode);
            if(requestCode == BaseCommonCode.SUCCESS_CODE){

                Intent intent = new Intent(InviteMemberService.this,LiveRequestService.class);
                intent.putExtra(Constants.MEMBER_NAME,mContactAdapter.getLiveMember().getName());
                intent.putExtra(Constants.MEMBER_ID,mContactAdapter.getLiveMember().getNo());
                startService(intent);
                removeView();
            }else{
                ToastUtil.livingFailToast(InviteMemberService.this, requestCode, TerminalErrorCode.LIVING_REQUEST.getErrorCode());
            }
        }else{
            ToastUtil.showToast(getApplicationContext(),getResources().getString(R.string.please_select_live_member));
        }
    }

    private void inviteOtherMemberToWatch(List<Integer>members){
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(JsonParam.SEND_STATE,MessageSendStateEnum.SENDING);
        jsonObject.put(JsonParam.DEVICE_ID, oldTerminalMessage.messageBody.getString(JsonParam.DEVICE_ID));
        jsonObject.put(JsonParam.GB28181_RTSP_URL, oldTerminalMessage.messageBody.getString(JsonParam.GB28181_RTSP_URL));
        jsonObject.put(JsonParam.DEVICE_NAME, oldTerminalMessage.messageBody.getString(JsonParam.DEVICE_NAME));
        jsonObject.put(JsonParam.DEVICE_DEPT_ID, oldTerminalMessage.messageBody.getString(JsonParam.DEVICE_DEPT_ID));
        jsonObject.put(JsonParam.DEVICE_DEPT_NAME, oldTerminalMessage.messageBody.getString(JsonParam.DEVICE_DEPT_NAME));
        jsonObject.put(JsonParam.TOKEN_ID, MyTerminalFactory.getSDK().getMessageSeq());
        TerminalMessage mTerminalMessage = new TerminalMessage();
        mTerminalMessage.messageFromId =  MyTerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0);
        mTerminalMessage.messageFromName = MyTerminalFactory.getSDK().getParam(Params.MEMBER_NAME, "");
        mTerminalMessage.messageToId = NoCodec.encodeMemberNo(0);
        mTerminalMessage.messageToName = "";
        mTerminalMessage.sendTime = System.currentTimeMillis();
        mTerminalMessage.messageType = MessageType.GB28181_RECORD.getCode();
        mTerminalMessage.messageBody = jsonObject;
        MyTerminalFactory.getSDK().getTerminalMessageManager().uploadDataByDDPUSH("",mTerminalMessage,members);
        removeView();
    }





//    private List<Member> searchContent(String content){
//        List<Member> list = new ArrayList<>();
//        for(int i = 0; i < memberList.size(); i++){
//            if(String.valueOf(memberList.get(i).getNo()).contains(content)){
//                list.add(memberList.get(i));
//            }else{
//                String name = memberList.get(i).getName();
//                if(!Util.isEmpty(name) && !Util.isEmpty(content) && name.toLowerCase().contains(content.toLowerCase())){
//                    list.add(memberList.get(i));
//                }
//            }
//        }
//        return list;
//    }
}
