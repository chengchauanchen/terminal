package cn.vsx.vc.service;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.TextViewCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;

import org.apache.http.util.TextUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import cn.vsx.hamster.common.Authority;
import cn.vsx.hamster.common.MessageSendStateEnum;
import cn.vsx.hamster.common.MessageType;
import cn.vsx.hamster.common.TerminalMemberType;
import cn.vsx.hamster.common.util.JsonParam;
import cn.vsx.hamster.common.util.NoCodec;
import cn.vsx.hamster.errcode.BaseCommonCode;
import cn.vsx.hamster.errcode.module.TerminalErrorCode;
import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.model.Department;
import cn.vsx.hamster.terminalsdk.model.Member;
import cn.vsx.hamster.terminalsdk.model.MemberResponse;
import cn.vsx.hamster.terminalsdk.model.TerminalMessage;
import cn.vsx.hamster.terminalsdk.model.VideoMember;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveGetTerminalHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveNotifyLivingStoppedHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveUpdatePhoneMemberHandler;
import cn.vsx.hamster.terminalsdk.tools.Params;
import cn.vsx.hamster.terminalsdk.tools.Util;
import cn.vsx.vc.R;
import cn.vsx.vc.adapter.MemberListAdapter;
import cn.vsx.vc.adapter.SelectAdapter;
import cn.vsx.vc.adapter.SelectedListAdapter;
import cn.vsx.vc.adapter.TempGroupSearchAdapter;
import cn.vsx.vc.application.MyApplication;
import cn.vsx.vc.model.CatalogBean;
import cn.vsx.vc.model.ContactItemBean;
import cn.vsx.vc.model.PushLiveMemberList;
import cn.vsx.vc.receiveHandle.ReceiveRemoveSwitchCameraViewHandler;
import cn.vsx.vc.utils.Constants;
import cn.vsx.vc.utils.DataUtil;
import cn.vsx.vc.utils.InputMethodUtil;
import ptt.terminalsdk.context.MyTerminalFactory;
import ptt.terminalsdk.tools.ToastUtil;

public class InviteMemberService extends BaseService {
    //编辑主题
    private LinearLayout mLlEditTheme;
    private ImageView mIvLiveEditReturn;
    private EditText mEtLiveEditImportTheme;
    private Button mBtnLiveEditConfirm;

    //搜索
    private LinearLayout mLlSearchMember;
    private ImageView mIvBack;
    private EditText mEtSearchAllcontacts;
    private ImageView mIvDeleteEdittext;
    private Button mBtnSearchAllcontacts;
    private TextView mTvSearchNothing;
    private ListView mLvSearchAllcontacts;
    private RelativeLayout mRlSearchResult;

    //选择布局
    private LinearLayout mLlSelectMember;
    private ImageView mIvLiveSelectmemberReturn;
    private Button mBtnLiveSelectmemberStart;

    //选择布局
    private LinearLayout mLlAllSelect;
    private LinearLayout mLlLiveSelectmemberTheme;
    private TextView mTvLiveSelectmemberTheme;
    private LinearLayout llSelect;
    private RecyclerView selectRecyclerview;
    private List<TextView> tabs = new ArrayList<>();
    private int[] tabIds = new int[]{R.id.pc_tv, R.id.jingwutong_tv, R.id.uav_tv, R.id.recoder_tv};

    private List<View> lines = new ArrayList<>();
    private int[] lineIds = new int[]{R.id.pc_line, R.id.jingwutong_line, R.id.uav_line, R.id.recoder_line};

    private List<SwipeRefreshLayout> srls = new ArrayList<>();
    private int[] srlIds = new int[]{R.id.srl_one, R.id.srl_two, R.id.srl_three, R.id.srl_four};

    private List<RecyclerView> rls = new ArrayList<>();
    private int[] rlIds = new int[]{R.id.rv_one, R.id.rv_two, R.id.rv_three, R.id.rv_four};
    //已选择
    private LinearLayout mLlAllSelected;
    private RecyclerView mRvSelected;

    private String type;
    private boolean pushing;
    private boolean pulling;

    private SelectAdapter selectAdapter;
    private SelectedListAdapter selectedListAdapter;
    private List<Member> selectedMembers = new ArrayList<>();
    private ArrayList<VideoMember> watchingmembers;
    private int livingMemberId;
    private boolean gb28181Pull;
    private boolean isGroupPushLive;
    private TerminalMessage oldTerminalMessage;

    //请求图像的类型
    private List<String> pullTypes = Arrays.asList(TerminalMemberType.TERMINAL_PC.toString(), TerminalMemberType.TERMINAL_PHONE.toString(),
                                                   TerminalMemberType.TERMINAL_UAV.toString(),TerminalMemberType.TERMINAL_BODY_WORN_CAMERA.toString());
    //上报图像的类型
    private List<String> pushTypes = Arrays.asList(TerminalMemberType.TERMINAL_PC.toString(), TerminalMemberType.TERMINAL_PHONE.toString(),
                                                   TerminalMemberType.TERMINAL_UAV.toString(),TerminalMemberType.TERMINAL_HDMI.toString());
    private List<List<ContactItemBean>> mAllDatas = new ArrayList<>();
    private List<MemberListAdapter> contactAdapter = new ArrayList<>();

    private List<ContactItemBean> mOneDatas = new ArrayList<>();
    private List<ContactItemBean> mTwoDatas = new ArrayList<>();
    private List<ContactItemBean> mThreeDatas = new ArrayList<>();
    private List<ContactItemBean> mFourDatas = new ArrayList<>();

    private LinearLayout layout_search;

    private String keyWord;
    private List<Member> searchList = new ArrayList<>();
    private TempGroupSearchAdapter tempGroupSearchAdapter;
    private List<Member> searchMemberListExceptMe = new ArrayList<>();
    /**
     * 搜索到的结果集合
     */

    private static final int TAB_COUNT = 4;
    //当前显示的列表
    private int currentIndex = 0;

    public InviteMemberService() {
    }

    @SuppressLint("InflateParams")
    @Override
    protected void setRootView() {
        rootView = LayoutInflater.from(MyTerminalFactory.getSDK().application).inflate(R.layout.layout_invite_member, null);
    }

    @Override
    protected void findView() {
        //编辑主题
        mLlEditTheme = rootView.findViewById(R.id.ll_live_edit_theme);
        mIvLiveEditReturn = rootView.findViewById(R.id.iv_live_edit_return);
        mEtLiveEditImportTheme = rootView.findViewById(R.id.et_live_edit_import_theme);
        mBtnLiveEditConfirm = rootView.findViewById(R.id.btn_live_edit_confirm);

        //搜索
        mLlSearchMember = rootView.findViewById(R.id.ll_search_member);
        mIvBack = rootView.findViewById(R.id.iv_back);
        mEtSearchAllcontacts = rootView.findViewById(R.id.et_search_allcontacts);
        mIvDeleteEdittext = rootView.findViewById(R.id.iv_delete_edittext);
        mBtnSearchAllcontacts = rootView.findViewById(R.id.btn_search_allcontacts);
        mTvSearchNothing = rootView.findViewById(R.id.tv_search_nothing);
        mLvSearchAllcontacts = rootView.findViewById(R.id.lv_search_allcontacts);
        mRlSearchResult = rootView.findViewById(R.id.rl_search_result);

        //选择布局
        mLlSelectMember = rootView.findViewById(R.id.live_select_member);
        mIvLiveSelectmemberReturn = rootView.findViewById(R.id.iv_live_selectmember_return);
        mBtnLiveSelectmemberStart = rootView.findViewById(R.id.btn_live_selectmember_start);

        //选择
        mLlAllSelect = rootView.findViewById(R.id.ll_all_select);
        mLlLiveSelectmemberTheme = rootView.findViewById(R.id.ll_live_selectmember_theme);
        mTvLiveSelectmemberTheme = rootView.findViewById(R.id.tv_live_selectmember_theme);
        llSelect = rootView.findViewById(R.id.ll_select);
        selectRecyclerview = rootView.findViewById(R.id.select_recyclerview);
        //已选择
        mLlAllSelected = rootView.findViewById(R.id.ll_all_selected);
        mRvSelected = rootView.findViewById(R.id.rv_selected);

        tabs.clear();
        lines.clear();
        srls.clear();
        rls.clear();
        for (int i = 0; i < TAB_COUNT; i++) {
            TextView textView = rootView.findViewById(tabIds[i]);
            textView.setOnClickListener(new MyTabOnClickListener(i));
            tabs.add(textView);
            lines.add(rootView.findViewById(lineIds[i]));
            SwipeRefreshLayout srl = rootView.findViewById(srlIds[i]);
            srl.setOnRefreshListener(new MySwipeRefreshLayoutOnRefreshListener(srl));
            srls.add(srl);
            rls.add(rootView.findViewById(rlIds[i]));

        }
        mAllDatas.add(mOneDatas);
        mAllDatas.add(mTwoDatas);
        mAllDatas.add(mThreeDatas);
        mAllDatas.add(mFourDatas);
        //搜索布局
        layout_search = rootView.findViewById(R.id.ll_layout_search);
        //设置tab切换之后view的改变
        setTabView(currentIndex);
    }

    protected void initListener() {
        mIvLiveSelectmemberReturn.setOnClickListener(returnOnClickListener);
        mBtnLiveEditConfirm.setOnClickListener(editThemeConfirmOnClickListener);
        mLlLiveSelectmemberTheme.setOnClickListener(editThemeOnClickListener);
        mIvLiveEditReturn.setOnClickListener(editThemeReturnOnClickListener);
        mBtnLiveSelectmemberStart.setOnClickListener(startOnClickListener);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveNotifyLivingStoppedHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveUpdatePhoneMemberHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveRemoveSwitchCameraViewHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveGetTerminalHandler);
        layout_search.setOnClickListener(searchOnClickListener);

        mIvBack.setOnClickListener(searchBackOnClickListener);
        mIvDeleteEdittext.setOnClickListener(deleteEditTextOnClickListener);
        mBtnSearchAllcontacts.setOnClickListener(doSearchOnClickListener);
        mEtSearchAllcontacts.addTextChangedListener(editTextChangedListener);
        mEtSearchAllcontacts.setOnEditorActionListener(onEditorActionListener);
        mLvSearchAllcontacts.setOnItemClickListener(searchItemclickListener);
        llSelect.setOnClickListener(selectedOnClickListener);

    }

    @Override
    protected void initView(Intent intent) {
        gb28181Pull = intent.getBooleanExtra(Constants.GB28181_PULL, false);
        isGroupPushLive = intent.getBooleanExtra(Constants.IS_GROUP_PUSH_LIVING, false);
        if (gb28181Pull) {
            oldTerminalMessage = (TerminalMessage) intent.getSerializableExtra(Constants.TERMINALMESSAGE);
        }
        filterMember(intent);
        if (Constants.PULL.equals(type) || pushing) {
            mLlLiveSelectmemberTheme.setVisibility(View.GONE);
        }
    }

    @Override
    protected void showPopMiniView() {
    }

    @Override
    protected void handleMesage(Message msg) {
    }

    @Override
    protected void onNetworkChanged(boolean connected) {
        if (!connected) {
            stopBusiness();
        }
    }

    @Override
    protected void initData() {
        selectRecyclerview.setLayoutManager(new LinearLayoutManager(MyTerminalFactory.getSDK().application, OrientationHelper.HORIZONTAL, false));
        selectAdapter = new SelectAdapter(this, selectedMembers);
        selectRecyclerview.setAdapter(selectAdapter);
        contactAdapter.clear();
        for (int i = 0; i < rls.size(); i++) {
            MemberListAdapter adapter = new MemberListAdapter(MyTerminalFactory.getSDK().application, mAllDatas.get(i));
            adapter.setItemClickListener(new MyItemClickListener(i));
            rls.get(i).setLayoutManager(new LinearLayoutManager(MyTerminalFactory.getSDK().application));
            rls.get(i).setAdapter(adapter);
            contactAdapter.add(adapter);
        }

        searchMemberListExceptMe.addAll(TerminalFactory.getSDK().getConfigManager().getPhoneMemberExceptMe());
        tempGroupSearchAdapter = new TempGroupSearchAdapter(MyTerminalFactory.getSDK().application, searchList);
        mLvSearchAllcontacts.setAdapter(tempGroupSearchAdapter);

        //已经选择的成员列表
        mRvSelected.setLayoutManager(new LinearLayoutManager(MyTerminalFactory.getSDK().application, OrientationHelper.HORIZONTAL, false));
        selectedListAdapter = new SelectedListAdapter(this,selectedMembers);
        mRvSelected.setAdapter(selectedListAdapter);
        selectedListAdapter.setItemClickListener(new MyItemDeleteClickListener());

        TerminalFactory.getSDK().getConfigManager().getTerminal(TerminalFactory.getSDK().getParam(Params.DEP_ID, 0), type);

    }

    @Override
    protected void initBroadCastReceiver() {
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveNotifyLivingStoppedHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveUpdatePhoneMemberHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveRemoveSwitchCameraViewHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveGetTerminalHandler);
        mHandler.removeCallbacksAndMessages(null);

    }

    /**************************************************************************listener***********************************************************************************************/

    private ReceiveRemoveSwitchCameraViewHandler receiveRemoveSwitchCameraViewHandler = () -> {
        mHandler.post(this::removeView);
    };

    private ReceiveUpdatePhoneMemberHandler receiveUpdatePhoneMemberHandler = PhoneMember -> mHandler.post(() -> {
//        MemberResponse memberResponse = TerminalFactory.getSDK().getConfigManager().getPhoneMemeberInfo();
//        if(null == memberResponse){
//            return;
//        }
//        // TODO: 2018/12/28 下拉刷新之后之前选中的人没了，要改
//        List<CatalogBean> catalogBeanList = new ArrayList<>();
//        CatalogBean bean = new CatalogBean();
//        bean.setName(memberResponse.getName());
//        bean.setBean(memberResponse);
//        catalogBeanList.add(bean);
//        updateData(memberResponse,catalogBeanList);
    });
    /**
     * 通知直播停止 通知界面关闭视频页
     **/
    private ReceiveNotifyLivingStoppedHandler receiveNotifyLivingStoppedHandler = (liveMemberId, callId, methodResult, resultDesc) -> {
        ToastUtil.showToast(MyTerminalFactory.getSDK().application, getResources().getString(R.string.push_stoped));
        stopBusiness();
    };

    private View.OnClickListener editThemeOnClickListener = v -> {
        mLlEditTheme.setVisibility(View.VISIBLE);
        mLlSearchMember.setVisibility(View.GONE);
        mLlSelectMember.setVisibility(View.GONE);
    };

    private View.OnClickListener editThemeConfirmOnClickListener = v -> {
        if (!TextUtils.isEmpty(mEtLiveEditImportTheme.getText().toString().trim())) {
            mLlEditTheme.setVisibility(View.GONE);
            mLlSearchMember.setVisibility(View.GONE);
            mLlSelectMember.setVisibility(View.VISIBLE);
            mTvLiveSelectmemberTheme.setText(mEtLiveEditImportTheme.getText().toString().trim());
            InputMethodUtil.hideInputMethod(InviteMemberService.this, mEtLiveEditImportTheme);
        } else {
            ToastUtil.showToast(MyTerminalFactory.getSDK().application, getResources().getString(R.string.theme_cannot_empty));
        }
    };
    /**
     * 返回按钮
     */
    private View.OnClickListener returnOnClickListener = v -> {
        if(mLlAllSelected.getVisibility() == View.VISIBLE){
            mLlAllSelect.setVisibility(View.VISIBLE);
            mLlAllSelected.setVisibility(View.GONE);
            mBtnLiveSelectmemberStart.setVisibility(View.VISIBLE);
        }else{
            removeView();
        }
    };

    private View.OnClickListener editThemeReturnOnClickListener = v -> {
        mLlEditTheme.setVisibility(View.GONE);
        mLlSearchMember.setVisibility(View.GONE);
        mLlSelectMember.setVisibility(View.VISIBLE);
    };
    /**
     * 跳转到已经选择成员的信息
     */
    private View.OnClickListener selectedOnClickListener =  v -> {
        goToSelectedView();
    };

    private View.OnClickListener startOnClickListener = v -> {
        if (Constants.PUSH.equals(type)) {
            if (pushing) {
                inviteToWatchLive();
            } else {
                if (MyApplication.instance.usbAttached) {
                    requestStartLive(Constants.UVC_PUSH);
                } else {
                    if (Constants.HYTERA.equals(Build.MODEL)) {
                        requestStartLive(Constants.RECODER_PUSH);
                    } else {
                        requestStartLive(Constants.PHONE_PUSH);
                    }
                }
            }
        } else if (Constants.PULL.equals(type)) {
            if (pulling) {
                if (gb28181Pull) {
                    inviteOtherMemberToWatch(getSelectMembersNo(),getSelectMembersUniqueNo());
                } else {
                    inviteToWatchLive();
                }
            } else {
                requestOtherStartLive();
            }
        }
    };

    /**
     * 获取到数据
     */
    private ReceiveGetTerminalHandler receiveGetTerminalHandler = (depId, type, departments, members) -> {
        mHandler.post(()-> updateData(depId,type, departments, members));
    };

    /**
     * tab点击事件
     */
    private class MyTabOnClickListener implements View.OnClickListener {

        private int index = 0;

        public MyTabOnClickListener(int index) {
            this.index = index;
        }

        @Override
        public void onClick(View v) {
            currentIndex = index;
            setTabView(index);
            loadData(TerminalFactory.getSDK().getParam(Params.DEP_ID, 0),true);
        }
    }

    /**
     * 选择成员
     */
    private class MyItemClickListener implements MemberListAdapter.ItemClickListener {

        private int index;

        public MyItemClickListener(int index) {
            this.index = index;
        }

        @Override
        public void itemClick(int adapterType, int position) {
            if(adapterType == Constants.TYPE_USER){
                //请求图像只能选单个设备，上报图像可选多个
                checkSelectSingleOrMultiple(index,position);
            }else if(adapterType == Constants.TYPE_FOLDER){
                Department department = (Department) mAllDatas.get(index).get(position).getBean();
                loadData(department.getId(),false);
            }
        }
    }

    /**
     * 删除已经选择的成员
     */
    private class MyItemDeleteClickListener implements SelectedListAdapter.ItemClickListener {

        @Override
        public void itemClick( int position) {
            if(position>=0&&position<selectedMembers.size()){
                selectedMembers.remove(position);
                if(selectAdapter!=null){
                    selectAdapter.notifyDataSetChanged();
                }
                if(selectedListAdapter!=null){
                    selectedListAdapter.notifyDataSetChanged();
                }
                //刷新选择状态
                refreshAllSelectedStatus();
            }
        }
    }

    private AdapterView.OnItemClickListener searchItemclickListener = (parent, view, position, id) -> {
        List<Integer> selectMember = getSelectMembersNo();
        if (!selectMember.isEmpty() && selectMember.contains(searchList.get(position).getNo())) {
            ToastUtil.showToast(MyTerminalFactory.getSDK().application, getString(R.string.text_you_have_added_this_member));
            return;
        }
        if(currentIndex>=0&&currentIndex<contactAdapter.size()){
//            contactAdapter.get(currentIndex).setSelectMember(searchList.get(position).getNo());
        }

        mEtSearchAllcontacts.setText("");
        InputMethodUtil.hideInputMethod(MyTerminalFactory.getSDK().application, mEtSearchAllcontacts);
//        searchMemberListExceptMe.clear();
        searchList.clear();
        mTvSearchNothing.setVisibility(View.VISIBLE);
        mRlSearchResult.setVisibility(View.GONE);

        mLlEditTheme.setVisibility(View.GONE);
        mLlSelectMember.setVisibility(View.VISIBLE);
        mLlSearchMember.setVisibility(View.GONE);
    };

    private TextView.OnEditorActionListener onEditorActionListener = (v, actionId, event) -> {
        if (actionId == EditorInfo.IME_ACTION_SEARCH) {
            doSearch();
        }
        return false;
    };

    private TextWatcher editTextChangedListener = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (s.length() > 0 && !DataUtil.isLegalSearch(s)) {
                ToastUtil.showToast(MyTerminalFactory.getSDK().application, getString(R.string.text_search_content_is_illegal));
            }
            if (android.text.TextUtils.isEmpty(s.toString())) {
                mIvDeleteEdittext.setVisibility(View.GONE);
                mBtnSearchAllcontacts.setBackgroundResource(R.drawable.rectangle_with_corners_shape1);
                mBtnSearchAllcontacts.setTextColor(ContextCompat.getColor(MyTerminalFactory.getSDK().application, R.color.search_button_text_color1));
                mBtnSearchAllcontacts.setEnabled(false);
            } else {
                mIvDeleteEdittext.setVisibility(View.VISIBLE);
                mBtnSearchAllcontacts.setBackgroundResource(R.drawable.rectangle_with_corners_shape2);
                mBtnSearchAllcontacts.setTextColor(ContextCompat.getColor(MyTerminalFactory.getSDK().application, R.color.white));
                mBtnSearchAllcontacts.setEnabled(true);
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    };

    private View.OnClickListener doSearchOnClickListener = v -> {
        doSearch();
    };

    private View.OnClickListener deleteEditTextOnClickListener = v -> {
        mEtSearchAllcontacts.setText("");
    };

    private View.OnClickListener searchBackOnClickListener = v -> {
        searchList.clear();
        mEtSearchAllcontacts.setText("");
        tempGroupSearchAdapter.notifyDataSetChanged();
        mLlEditTheme.setVisibility(View.GONE);
        mLlSearchMember.setVisibility(View.GONE);
        InputMethodUtil.hideInputMethod(MyTerminalFactory.getSDK().application, mEtSearchAllcontacts);
        mLlSelectMember.setVisibility(View.VISIBLE);
    };

    private View.OnClickListener searchOnClickListener = v -> {
        mLlEditTheme.setVisibility(View.GONE);
        mLlSelectMember.setVisibility(View.GONE);
        mLlSearchMember.setVisibility(View.VISIBLE);
    };

    /**
     * 下拉刷新
     */
    private class MySwipeRefreshLayoutOnRefreshListener implements SwipeRefreshLayout.OnRefreshListener {

        private SwipeRefreshLayout srl;

        public MySwipeRefreshLayoutOnRefreshListener(SwipeRefreshLayout srl) {
            this.srl = srl;
        }

        @Override
        public void onRefresh() {
            loadData(TerminalFactory.getSDK().getParam(Params.DEP_ID, 0),false);
            mHandler.postDelayed(()-> srl.setRefreshing(false),1200);
        }
    }

    /**************************************************************************listener***********************************************************************************************/


    /**
     * 搜索
     */
    private void doSearch() {
        InputMethodUtil.hideInputMethod(MyTerminalFactory.getSDK().application, mEtSearchAllcontacts);
        keyWord = mEtSearchAllcontacts.getText().toString();
        tempGroupSearchAdapter.setFilterKeyWords(keyWord);

        mTvSearchNothing.setVisibility(View.VISIBLE);
        mTvSearchNothing.setText(R.string.text_search_contact);
        mRlSearchResult.setVisibility(View.GONE);
        searchList.clear();

        if (android.text.TextUtils.isEmpty(keyWord)) {
            ToastUtil.showToast(MyTerminalFactory.getSDK().application, getString(R.string.text_search_content_can_not_empty));
        } else {
            searchMemberFromGroup();
        }
    }

    /**
     * 搜索
     **/
    private void searchMemberFromGroup() {
        searchList.clear();
        for (Member member : searchMemberListExceptMe) {
            String name = member.getName();
            String id = String.valueOf(member.getNo());
            if (!Util.isEmpty(name) && !Util.isEmpty(keyWord) && name.toLowerCase().contains(keyWord.toLowerCase())) {
                searchList.add(member);
            } else if (!Util.isEmpty(id) && !Util.isEmpty(keyWord) && id.contains(keyWord)) {
                searchList.add(member);
            }
        }
        if (searchList.size() == 0) {
            mTvSearchNothing.setText(R.string.text_contact_is_not_exist);
            mRlSearchResult.setVisibility(View.GONE);
        } else {
            mTvSearchNothing.setVisibility(View.GONE);
            mRlSearchResult.setVisibility(View.VISIBLE);

            if (tempGroupSearchAdapter != null) {
                tempGroupSearchAdapter.notifyDataSetChanged();
                mLvSearchAllcontacts.setSelection(0);
            }
        }
    }

    /**
     * 跳转到已经选择成员的信息
     */
    private void goToSelectedView(){
        if(!selectedMembers.isEmpty()){
            mLlAllSelect.setVisibility(View.GONE);
            mLlAllSelected.setVisibility(View.VISIBLE);
            selectedListAdapter.notifyDataSetChanged();
            mBtnLiveSelectmemberStart.setVisibility(View.GONE);
        }
    }

    /**
     * 设置数据
     */
    private void updateData(MemberResponse memberResponse, List<CatalogBean> catalogBeanList) {
//        mDatas.clear();
//        mCatalogList.clear();
//        mCatalogList.addAll(catalogBeanList);
//        addData(memberResponse);
//        mContactAdapter.notifyDataSetChanged();
//        mCatalogAdapter.notifyDataSetChanged();
//        mCatalogRecyclerview.scrollToPosition(mCatalogList.size() - 1);

    }

    private void addData(MemberResponse memberResponse) {
        if (memberResponse != null) {
            addItemMember(memberResponse);
            addItemDepartment(memberResponse);
        }
    }

    /**
     * 添加子成员
     */
    @SuppressWarnings("unchecked")
    private void addItemMember(MemberResponse memberResponse) {
//        //子成员
//        List<Member> memberList = new ArrayList<>();
//        List<Member> members = memberResponse.getMembers();
//        for (Member member : members) {
//            Member cloneMember = new Member();
//            //如果没有名字显示No
//            if (member.getName() == null || member.getName().equals("")) {
//                cloneMember.setName(String.valueOf(member.getNo()));
//            } else {
//                cloneMember.setName(member.getName());
//            }
//            //memberId终端用不上，代码里的id都是指No
//            cloneMember.setId(member.getNo());
//            cloneMember.setNo(member.getNo());
////            cloneMember.setTerminalMemberTypeEnum(TerminalMemberType.getInstanceByCode(member.type));
//            cloneMember.setPhone(member.getPhone());
//            cloneMember.setDepartmentName(memberResponse.getName());
//            memberList.add(cloneMember);
//        }
//        if (!memberList.isEmpty()) {
//            List<ContactItemBean> itemMemberList = new ArrayList<>();
//            if (Constants.PUSH.equals(type)) {
//                filterPushMember(memberList);
//            } else if (Constants.PULL.equals(type)) {
//                filterPullMember(memberList);
//            }
//
//            for (Member member : memberList) {
//                if (member.getName() == null) {
//                    continue;
//                }
//                member.setChecked(false);
//                ContactItemBean<Member> bean = new ContactItemBean<>();
//                bean.setBean(member);
//                bean.setType(Constants.TYPE_USER);
//                itemMemberList.add(bean);
//            }
//            Collections.sort(itemMemberList);
//            mDatas.addAll(itemMemberList);
//        }
    }

    private void filterPullMember(List<Member> memberList) {
        Iterator<Member> iterator = memberList.iterator();
        while (iterator.hasNext()) {
            Member member = iterator.next();
            if (MyTerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0) == member.getNo()) {
                iterator.remove();
                continue;
            }
            if (livingMemberId != 0 && livingMemberId == member.getNo()) {
                iterator.remove();
            }
        }
    }

    private void filterPushMember(List<Member> memberList) {
        //去掉正在观看的人和自己
        Iterator<Member> iterator = memberList.iterator();
        while (iterator.hasNext()) {
            Member member = iterator.next();
            if (MyTerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0) == member.getNo()) {
                iterator.remove();
                continue;
            }
            if (null != watchingmembers) {
                for (VideoMember watchingmember : watchingmembers) {
                    if (watchingmember.getId() == member.getNo()) {
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
    private void addItemDepartment(MemberResponse memberResponse) {
//        List<MemberResponse> data = memberResponse.getChildren();
//        if (data != null && !data.isEmpty()) {
//            for (MemberResponse next : data) {
//                if (next.getName() == null) {
//                    continue;
//                }
//                ContactItemBean<MemberResponse> bean = new ContactItemBean<>();
//                bean.setType(Constants.TYPE_DEPARTMENT);
//                bean.setName(next.getName());
//                bean.setBean(next);
//                mDatas.add(bean);
//                //                Collections.sort(mDatas);
//            }
//        }
    }


    @SuppressWarnings("unchecked")
    private void filterMember(Intent intent) {
        type = intent.getStringExtra(Constants.TYPE);

        setTabTextView(type);
        pushing = intent.getBooleanExtra(Constants.PUSHING, false);
        watchingmembers = (ArrayList<VideoMember>) intent.getSerializableExtra(Constants.WATCHING_MEMBERS);

        pulling = intent.getBooleanExtra(Constants.PULLING, false);
        livingMemberId = intent.getIntExtra(Constants.LIVING_MEMBER_ID, 0);

        loadData(TerminalFactory.getSDK().getParam(Params.DEP_ID, 0),true);
    }

    /**
     * 加载数据
     */
    private void loadData(int depId,boolean isNeedCheck){
        if(checkDataIsNeedRequest(isNeedCheck)){
            TerminalFactory.getSDK().getConfigManager().getTerminal(depId, getTerminalType());
        }
    }

    /**
     * 更新数据
     * @param depId
     * @param type
     * @param departments
     * @param members
     */
    private void updateData(int depId,String type, List<Department> departments, List<Member> members){
        int index = getTerminalTypeListByType(type);
        if(index>=0&&index<mAllDatas.size()){
            mAllDatas.get(index).clear();
            for(Member member : members){
                ContactItemBean<Member> contactItemBean = new ContactItemBean<>();
                contactItemBean.setBean(member);
                contactItemBean.setType(Constants.TYPE_USER);
                mAllDatas.get(index).add(contactItemBean);
            }
            for(Department department : departments){
                ContactItemBean<Department> contactItemBean = new ContactItemBean<>();
                contactItemBean.setBean(department);
                contactItemBean.setType(Constants.TYPE_FOLDER);
                mAllDatas.get(index).add(contactItemBean);
            }
            //还原选择状态
            restoreSelectStatus(mAllDatas.get(index),index);
            if(index<contactAdapter.size()&&contactAdapter.get(index)!=null){
                contactAdapter.get(index).notifyDataSetChanged();
            }
        }
    }

    /**
     * 还原选择的状态
     * @param contactItemBeans
     */
    private void restoreSelectStatus(List<ContactItemBean> contactItemBeans,int index) {
        if(selectedMembers.isEmpty()){
            for (ContactItemBean bean: contactItemBeans) {
                if(bean.getType() == Constants.TYPE_USER){
                    Member member1 = (Member) bean.getBean();
                    member1.setChecked(false);
                }
            }
        }else{
            for (Member member: selectedMembers) {
                for (ContactItemBean bean: contactItemBeans) {
                    if(bean.getType() == Constants.TYPE_USER){
                        Member member1 = (Member) bean.getBean();
                        member1.setChecked((member.getNo() == member1.getNo()));
                    }
                }
            }
        }

        if(index>=0&&index<contactAdapter.size()){
            contactAdapter.get(index).notifyDataSetChanged();
        }
    }

    /**
     * 检查列表是否需要获取数据
     * @return
     */
    private boolean checkDataIsNeedRequest(boolean isNeedCheck) {
       return (!isNeedCheck) || mAllDatas.get(currentIndex).isEmpty();
    }

    /**
     * 获取设备类型
     * @return
     */
    private String getTerminalType(){
        return ((android.text.TextUtils.equals(type,Constants.PULL))?pullTypes:pushTypes).get(currentIndex);
    }

    /**
     * 根据类型名称获取列表的index
     * @param type
     * @return
     */
    private int getTerminalTypeListByType(String type){
        List<String> types = android.text.TextUtils.equals(type,Constants.PULL)?pullTypes:pushTypes;
        if(types.contains(type)){
            return types.indexOf(type);
        }
        return -1;
    }

    /**
     * 根据type设置tab文字信息
     * @param type
     */
    private void setTabTextView(String type) {
        if(tabs.size()>0){
            TextView textView = tabs.get(tabs.size()-1);
            textView.setText(getString((android.text.TextUtils.equals(type,Constants.PUSH))?
                    R.string.text_hdmi:R.string.text_recoder));
        }
    }

    /**
     * 获取选择成员的No
     * @return
     */
    private ArrayList<Integer> getSelectMembersNo(){
        ArrayList<Integer> result = new ArrayList<>();
        for (Member member:selectedMembers) {
            result.add(member.getNo());
        }
        return result;
    }

    /**
     * 获取选择成员的uniqueNo
     * @return
     */
    private ArrayList<Long> getSelectMembersUniqueNo(){
        ArrayList<Long> result = new ArrayList<>();
        for (Member member:selectedMembers) {
            result.add(member.getUniqueNo());
        }
        return result;
    }

    /**
     * 获取选择成员
     * @return
     */
    private Member getLiveMember(){
        Member member  = null;
        if(!selectedMembers.isEmpty()){
            member = selectedMembers.get(0);
        }
        return member;
    }

    /**
     * 刷新所有列表的选择状态
     */
    private void refreshAllSelectedStatus() {
        for (int i = 0; i < TAB_COUNT; i++) {
            restoreSelectStatus(mAllDatas.get(i),i);
        }
    }

    /**
     * 检查选择单个设备还是选择多个设备
     *
     * @param position
     */
    private void checkSelectSingleOrMultiple(int index,int position) {
        if(android.text.TextUtils.equals(type,Constants.PULL)){
            Member member = (Member) mAllDatas.get(index).get(position).getBean();
            //请求图像，只能选择单个设备
            if(selectedMembers.isEmpty()|| member.isChecked()){
                //还没有选择设备
                member.setChecked(!member.isChecked());
                updateSelectMember(member);
            }else{
                member.setChecked(false);
                if(index>=0&&index<contactAdapter.size()){
                    contactAdapter.get(index).notifyItemChanged(position);
                }
                ToastUtil.showToast(this,getString(R.string.only_choose_one_device));
            }
        }else{
            //上报图像，选择多个设备
            Member member = (Member) mAllDatas.get(index).get(position).getBean();
            member.setChecked(!member.isChecked());
            updateSelectMember(member);
        }
    }

    /**
     *设置选择的成员
     * @param member
     */
    private void updateSelectMember(Member member) {
        if(member.isChecked){
            //添加
            selectedMembers.add(member);
        }else{
            //删除
            selectedMembers.remove(member);
        }
        selectAdapter.notifyDataSetChanged();
        int count = searchList.size();
        if (count == 0) {
            mBtnLiveSelectmemberStart.setText(getResources().getString(R.string.confirm));
        } else {
            mBtnLiveSelectmemberStart.setText(String.format("确定(%s)", count));
        }
    }

    /**
     * 设置tab切换之后view的改变
     *
     * @param currentIndex
     */
    private void setTabView(int currentIndex) {
        for (int i = 0; i < tabs.size(); i++) {
            if (currentIndex == i) {
                TextViewCompat.setTextAppearance(tabs.get(i), R.style.contacts_title_checked_text);
                tabs.get(i).setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
                lines.get(i).setVisibility(View.VISIBLE);
                srls.get(i).setVisibility(View.VISIBLE);
            } else {
                TextViewCompat.setTextAppearance(tabs.get(i), R.style.contacts_title_unchecked_text);
                tabs.get(i).setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
                lines.get(i).setVisibility(View.GONE);
                srls.get(i).setVisibility(View.GONE);
            }
        }
    }

    /**
     * 邀请别人来观看
     */
    private void inviteToWatchLive() {
        logger.info("通知别人来观看的列表：" + getSelectMembersUniqueNo());
        if (!MyTerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_VIDEO_PUSH.name())) {
            ToastUtil.showToast(MyApplication.instance, getString(R.string.no_push_authority));
            return;
        }
        if (!getSelectMembersUniqueNo().isEmpty()) {
            MyTerminalFactory.getSDK().getLiveManager().requestNotifyWatch(getSelectMembersUniqueNo(),
                    MyTerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0),
                    TerminalFactory.getSDK().getParam(Params.MEMBER_UNIQUENO, 0l));
        }
        removeView();
    }


    /**
     * 请求自己开始上报
     */
    private void requestStartLive(String type) {
        String theme = mTvLiveSelectmemberTheme.getText().toString().trim();
        Intent intent = new Intent();
        intent.putExtra(Constants.THEME, theme);
        intent.putExtra(Constants.TYPE, Constants.ACTIVE_PUSH);
        intent.putExtra(Constants.IS_GROUP_PUSH_LIVING, isGroupPushLive);
        intent.putExtra(Constants.PUSH_MEMBERS,new PushLiveMemberList());
        switch (type) {
            case Constants.PHONE_PUSH:
                intent.setClass(this, PhonePushService.class);
                break;
            case Constants.UVC_PUSH:
                intent.setClass(this, SwitchCameraService.class);
                intent.putExtra(Constants.CAMERA_TYPE, Constants.UVC_CAMERA);
                break;
            case Constants.RECODER_PUSH:
                intent.setClass(this, SwitchCameraService.class);
                intent.putExtra(Constants.CAMERA_TYPE, Constants.RECODER_CAMERA);
                break;
        }
        startService(intent);
        removeView();
    }

    /**
     * 请求别人上报
     */
    private void requestOtherStartLive() {
        Member member = getLiveMember();
        if (null != member) {
            long liveUniqueNo = 0l;
            int requestCode = MyTerminalFactory.getSDK().getLiveManager().requestMemberLive(member.getNo(), liveUniqueNo, "");
            logger.error("请求图像：requestCode=" + requestCode);
            if (requestCode == BaseCommonCode.SUCCESS_CODE) {

                Intent intent = new Intent(InviteMemberService.this, LiveRequestService.class);
                intent.putExtra(Constants.MEMBER_NAME, member.getName());
                intent.putExtra(Constants.MEMBER_ID, member.getNo());
                intent.putExtra(Constants.UNIQUE_NO, member.getUniqueNo());
                startService(intent);
                removeView();
            } else {
                ToastUtil.livingFailToast(InviteMemberService.this, requestCode, TerminalErrorCode.LIVING_REQUEST.getErrorCode());
            }
        } else {
            ToastUtil.showToast(MyTerminalFactory.getSDK().application, getResources().getString(R.string.please_select_live_member));
        }
    }

    /**
     * 邀请别人去观看
     * @param memberNos
     * @param uniqueNos
     */
    private void inviteOtherMemberToWatch(List<Integer> memberNos,List<Long> uniqueNos) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(JsonParam.SEND_STATE, MessageSendStateEnum.SENDING);
        jsonObject.put(JsonParam.DEVICE_ID, oldTerminalMessage.messageBody.getString(JsonParam.DEVICE_ID));
        jsonObject.put(JsonParam.GB28181_RTSP_URL, oldTerminalMessage.messageBody.getString(JsonParam.GB28181_RTSP_URL));
        jsonObject.put(JsonParam.DEVICE_NAME, oldTerminalMessage.messageBody.getString(JsonParam.DEVICE_NAME));
        jsonObject.put(JsonParam.DEVICE_DEPT_ID, oldTerminalMessage.messageBody.getString(JsonParam.DEVICE_DEPT_ID));
        jsonObject.put(JsonParam.DEVICE_DEPT_NAME, oldTerminalMessage.messageBody.getString(JsonParam.DEVICE_DEPT_NAME));
        jsonObject.put(JsonParam.TOKEN_ID, MyTerminalFactory.getSDK().getMessageSeq());
        TerminalMessage mTerminalMessage = new TerminalMessage();
        mTerminalMessage.messageFromId = MyTerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0);
        mTerminalMessage.messageFromName = MyTerminalFactory.getSDK().getParam(Params.MEMBER_NAME, "");
        mTerminalMessage.messageToId = NoCodec.encodeMemberNo(0);
        mTerminalMessage.messageToName = "";
        mTerminalMessage.sendTime = System.currentTimeMillis();
        mTerminalMessage.messageType = MessageType.GB28181_RECORD.getCode();
        mTerminalMessage.messageBody = jsonObject;
        MyTerminalFactory.getSDK().getTerminalMessageManager().uploadDataByDDPUSH("", mTerminalMessage, memberNos, uniqueNos);
        removeView();
    }
}
