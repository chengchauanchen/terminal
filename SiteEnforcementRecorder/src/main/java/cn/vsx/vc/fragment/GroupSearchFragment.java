package cn.vsx.vc.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.zectec.imageandfileselector.utils.OperateReceiveHandlerUtilSync;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.vsx.hamster.errcode.BaseCommonCode;
import cn.vsx.hamster.errcode.module.SignalServerErrorCode;
import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.model.Group;
import cn.vsx.hamster.terminalsdk.model.GroupAndDepartment;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveChangeGroupHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveMemberAboutTempGroupHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveResponseChangeTempGroupProcessingStateHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceivegUpdateGroupHandler;
import cn.vsx.hamster.terminalsdk.tools.Params;
import cn.vsx.vc.R;
import cn.vsx.vc.adapter.GroupAdapter;
import cn.vsx.vc.application.MyApplication;
import cn.vsx.vc.receiveHandle.ReceiverFragmentPopBackStackHandler;
import cn.vsx.vc.utils.Constants;
import cn.vsx.vc.utils.InputMethodUtil;
import ptt.terminalsdk.context.MyTerminalFactory;
import ptt.terminalsdk.tools.ToastUtil;

/**
 * Created by gt358 on 2017/10/20.
 */

public class GroupSearchFragment extends Fragment implements View.OnClickListener {

    @Bind(R.id.iv_return)
    ImageView ivReturn;
    @Bind(R.id.et_search)
    EditText etSearch;

    @Bind(R.id.group_recyclerView)
    RecyclerView groupRecyclerView;
    @Bind(R.id.swipeRefreshLayout)
    SwipeRefreshLayout swipeRefreshLayout;

    @Bind(R.id.tv_search_nothing)
    TextView tvSearchNothing;

    private GroupAdapter groupAdapter;
    //显示在recyclerview上的所有数据
    private List<GroupAndDepartment> datas = new ArrayList<>();

    //记录关键字
    private String keyWords ;

    private Handler mHandler = new Handler(Looper.getMainLooper());
    public Logger logger = Logger.getLogger(getClass());

    public static GroupSearchFragment newInstance() {
        GroupSearchFragment fragment = new GroupSearchFragment();
        Bundle args = new Bundle();
//        args.putInt(TYPE, type);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
//            jumpType = getArguments().getInt(TYPE);
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_group_search, container, false);
        ButterKnife.bind(this, view);
        initView();
        initListener();
//        initData();
        return view;
    }

    /**
     * 初始化布局
     */
    private void initView() {
        ivReturn.setVisibility(View.VISIBLE);
        swipeRefreshLayout.setProgressBackgroundColorSchemeResource(R.color.colorAccent);
        swipeRefreshLayout.setColorSchemeResources(R.color.white);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            doSearch(true);
            mHandler.postDelayed(() -> {
                // 加载完数据设置为不刷新状态，将下拉进度收起来
                swipeRefreshLayout.setRefreshing(false);
                // 加载完数据设置为不刷新状态，将下拉进度收起来
            }, 1200);
        });
        groupRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
        groupAdapter = new GroupAdapter(getContext(), datas);
        groupRecyclerView.setAdapter(groupAdapter);
        groupAdapter.setOnItemClickListener(new GroupAdapter.OnItemClickListener() {
            @Override
            public void onFolderClick(View view, int depId, String name, boolean isTempGroup) {
            }

            @Override
            public void onGroupClick(Group group) {
                if (group != null) {
                    TerminalFactory.getSDK().getGroupManager().changeGroup(group.getNo());
                }
            }
        });

        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                //关闭软键盘
                InputMethodUtil.hideInputMethod(this.getContext(), etSearch);
                doSearch(true);
                return true;
            }
            return false;
        });
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                     keyWords = s.toString().trim();
                     if(TextUtils.isEmpty(keyWords)){
                         checkShowEmptyView();
                     }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        checkShowEmptyView();
    }

    /**
     * 添加监听
     */
    private void initListener() {
        MyTerminalFactory.getSDK().registReceiveHandler(receiveChangeGroupHandler);//转组
        MyTerminalFactory.getSDK().registReceiveHandler(receiveResponseChangeTempGroupProcessingStateHandler);//警情组结束，刷新组列表
        MyTerminalFactory.getSDK().registReceiveHandler(receiveMemberAboutTempGroupHandler);//临时组改变
        MyTerminalFactory.getSDK().registReceiveHandler(receivegUpdateGroupHandler);//获取组信息
    }

    /**
     * 搜索
     */
    private void doSearch(boolean show) {
        keyWords = etSearch.getText().toString().trim();
        if(TextUtils.isEmpty(keyWords)&&show){
            ToastUtil.showToast(getContext(),getString(R.string.text_search_group_by_name_and_id));
            return;
        }

        TerminalFactory.getSDK().getThreadPool().execute(() -> {
            List<Group> list = TerminalFactory.getSDK().getConfigManager().searchGroup(keyWords);
                datas.clear();
                for (Group group: list) {
                    if(group!=null){
                        GroupAndDepartment<Group> groupAndDepartment = new GroupAndDepartment<>();
                        groupAndDepartment.setType(Constants.TYPE_GROUP);
                        groupAndDepartment.setBean(group);
                        datas.add(groupAndDepartment);
                    }
                }
                mHandler.post(() -> {
                    checkShowEmptyView();
                    groupAdapter.notifyDataSetChanged();
                });
        });

    }

    @OnClick({R.id.iv_return, R.id.iv_search})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_return:
                //关闭页面
                InputMethodUtil.hideInputMethod(this.getContext(), etSearch);
                OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiverFragmentPopBackStackHandler.class);
                break;
            case R.id.iv_search:
                //搜索
                doSearch(true);
                break;
        }
    }

    /**
     * 转组消息
     */
    private ReceiveChangeGroupHandler receiveChangeGroupHandler = new ReceiveChangeGroupHandler() {

        @Override
        public void handler(int errorCode, String errorDesc) {
            logger.info("收到转组消息：" + errorCode + "/" + errorDesc);
            if (errorCode == 0 || errorCode == SignalServerErrorCode.INVALID_SWITCH_GROUP.getErrorCode()) {
                ToastUtil.showToast(MyApplication.instance, getString(R.string.text_change_group_success));
                mHandler.post(() -> groupAdapter.notifyDataSetChanged());
            } else {
                ToastUtil.showToast(MyApplication.instance, errorDesc);
            }
        }
    };

//    /**
//     * 临时组
//     */
//    private ReceiveNotifyAboutGroupChangeMessageHandler receiveNotifyAboutGroupChangeMessageHandler = new ReceiveNotifyAboutGroupChangeMessageHandler(){
//        @Override
//        public void handler(int groupId, int groupChangeType, String groupChangeDesc){
//            if(groupChangeType == GroupChangeType.MODIFY_RESPONSE_GROUP_TYPE.getCode()){
//                for(GroupAndDepartment data : datas){
//                    if(data.getBean() instanceof Group){
//                        Group group = (Group) data.getBean();
//                        if(group.getNo() == groupId){
//                            JSONObject jsonObject = JSONObject.parseObject(groupChangeDesc);
//                            int responseGroupType = jsonObject.getIntValue("responseGroupType");
//                            ResponseGroupType instanceByCode = ResponseGroupType.getInstanceByCode(responseGroupType);
//                            if(null !=instanceByCode){
//                                group.setResponseGroupType(instanceByCode.toString());
//                            }
//                            break;
//                        }
//                    }
//                }
//                mHandler.post(()-> groupAdapter.notifyDataSetChanged());
//            }
//        }
//    };

    /**
     * 警情临时组处理完成，终端需要切到主组，刷新通讯录
     */
    private ReceiveResponseChangeTempGroupProcessingStateHandler receiveResponseChangeTempGroupProcessingStateHandler = (resultCode, resultDesc) -> {
        if (resultCode == BaseCommonCode.SUCCESS_CODE) {
            TerminalFactory.getSDK().getThreadPool().execute(() -> TerminalFactory.getSDK().getConfigManager().updateAllGroups());
        }
    };
    /**
     * 临时组改变
     */
    private ReceiveMemberAboutTempGroupHandler receiveMemberAboutTempGroupHandler = new ReceiveMemberAboutTempGroupHandler() {
        @Override
        public void handler(boolean isAdd, boolean isLocked, boolean isScan, boolean isSwitch, int tempGroupNo, String tempGroupName, String tempGroupType) {
            TerminalFactory.getSDK().getThreadPool().execute(() -> TerminalFactory.getSDK().getConfigManager().updateAllGroups());
        }
    };

    /**
     * 获取组信息
     */
    private ReceivegUpdateGroupHandler receivegUpdateGroupHandler = ( depId, depName,departments, groups)->{
        mHandler.post(() -> doSearch(false));
    };

    /**
     * 检查是否显示空数据布局
     */
    private void checkShowEmptyView() {
        if(datas.isEmpty()){
            swipeRefreshLayout.setVisibility(View.GONE);
            groupRecyclerView.setVisibility(View.GONE);
            tvSearchNothing.setVisibility(View.VISIBLE);
            tvSearchNothing.setText(getString(
                    (TextUtils.isEmpty(keyWords))?
                    R.string.text_search_group_by_name_and_id:R.string.text_search_group_empty));
        }else{
            swipeRefreshLayout.setVisibility(View.VISIBLE);
            groupRecyclerView.setVisibility(View.VISIBLE);
            tvSearchNothing.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveChangeGroupHandler);//转组
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveResponseChangeTempGroupProcessingStateHandler);//警情组结束，刷新组列表
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveMemberAboutTempGroupHandler);//临时组改变
        MyTerminalFactory.getSDK().unregistReceiveHandler(receivegUpdateGroupHandler);//获取组信息
    }
}
