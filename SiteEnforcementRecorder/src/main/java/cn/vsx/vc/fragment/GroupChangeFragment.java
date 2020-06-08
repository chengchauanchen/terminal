package cn.vsx.vc.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import cn.vsx.hamster.terminalsdk.model.Department;
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
import cn.vsx.vc.model.CatalogBean;
import cn.vsx.vc.receiveHandle.ReceiverFragmentBackPressedByGroupChangeHandler;
import cn.vsx.vc.receiveHandle.ReceiverFragmentPopBackStackHandler;
import cn.vsx.vc.receiveHandle.ReceiverFragmentShowHandler;
import cn.vsx.vc.utils.Constants;
import ptt.terminalsdk.context.MyTerminalFactory;
import ptt.terminalsdk.tools.ToastUtil;

/**
 * Created by gt358 on 2017/10/20.
 */

public class GroupChangeFragment extends Fragment implements View.OnClickListener {

    @Bind(R.id.iv_return)
    ImageView ivReturn;
    @Bind(R.id.tv_title)
    TextView tvTitle;
    @Bind(R.id.iv_close)
    ImageView ivClose;
    @Bind(R.id.group_recyclerView)
    RecyclerView groupRecyclerView;
    @Bind(R.id.swipeRefreshLayout)
    SwipeRefreshLayout swipeRefreshLayout;

    private GroupAdapter groupAdapter;
    //显示在recyclerview上的所有数据
    private List<GroupAndDepartment> datas = new ArrayList<>();
    //临时组的数据
    private List<GroupAndDepartment> tempGroupDatas = new ArrayList<>();
    //普通部门组的数据
    private List<GroupAndDepartment> commonGroupDatas = new ArrayList<>();
    private List<CatalogBean> catalogNames = new ArrayList<>();


    private boolean tempGroupUpdateCompleted,groupUpdateCompleted;

    private Handler mHandler = new Handler(Looper.getMainLooper());
    public Logger logger = Logger.getLogger(getClass());

    public static GroupChangeFragment newInstance() {
        GroupChangeFragment fragment = new GroupChangeFragment();
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
        View view = inflater.inflate(R.layout.fragment_group_change, container, false);
        ButterKnife.bind(this, view);
        initView();
        initListener();
        initData();
        return view;
    }

    /**
     * 初始化布局
     */
    private void initView() {
        ivReturn.setVisibility(View.VISIBLE);
        tvTitle.setText(getString(R.string.text_change_group));
        tvTitle.setPadding(0, 0, 0, 0);
        ivClose.setImageResource(R.drawable.ico_search);

        swipeRefreshLayout.setProgressBackgroundColorSchemeResource(R.color.colorAccent);
        swipeRefreshLayout.setColorSchemeResources(R.color.white);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            groupUpdateCompleted = false;
            tempGroupUpdateCompleted = false;
            catalogNames.clear();

            CatalogBean groupCatalogBean = new CatalogBean(TerminalFactory.getSDK().getParam(Params.DEP_NAME,""),TerminalFactory.getSDK().getParam(Params.DEP_ID,0));
            catalogNames.add(groupCatalogBean);
            TerminalFactory.getSDK().getConfigManager().updateAllGroups();
            TerminalFactory.getSDK().getConfigManager().updateAllGroupInfo(false);
            mHandler.postDelayed(() -> {
                // 加载完数据设置为不刷新状态，将下拉进度收起来
                swipeRefreshLayout.setRefreshing(false);
                // 加载完数据设置为不刷新状态，将下拉进度收起来
            }, 1200);
        });
        groupRecyclerView.setLayoutManager(new GridLayoutManager(getContext(),3));
        groupAdapter = new GroupAdapter(getContext(), datas);
        groupRecyclerView.setAdapter(groupAdapter);
        groupAdapter.setOnItemClickListener(new GroupAdapter.OnItemClickListener() {
            @Override
            public void onFolderClick(View view, int depId, String name, boolean isTempGroup) {
                tempGroupUpdateCompleted = true;
                groupUpdateCompleted = false;
                CatalogBean groupCatalogBean = new CatalogBean(name,depId);
                if(!catalogNames.contains(groupCatalogBean)){
                    catalogNames.add(groupCatalogBean);
                    tvTitle.setText(name);
                }
                TerminalFactory.getSDK().getConfigManager().updateGroup(depId,name);
            }

            @Override
            public void onGroupClick(Group group) {
                if(group!=null){
                    TerminalFactory.getSDK().getGroupManager().changeGroup(group.getNo());
                }
            }
        });
    }

    /**
     * 添加监听
     */
    private void initListener() {
        MyTerminalFactory.getSDK().registReceiveHandler(receiveChangeGroupHandler);//转组
        MyTerminalFactory.getSDK().registReceiveHandler(receiveResponseChangeTempGroupProcessingStateHandler);//警情组结束，刷新组列表
        MyTerminalFactory.getSDK().registReceiveHandler(receiveMemberAboutTempGroupHandler);//临时组改变
        MyTerminalFactory.getSDK().registReceiveHandler(receivegUpdateGroupHandler);//获取组信息
        OperateReceiveHandlerUtilSync.getInstance().registReceiveHandler(receiverFragmentBackPressedByGroupChangeHandler);//收到onKeyDown
    }

    /**
     * 获取数据
     */
    private void initData() {
        catalogNames.clear();
        TerminalFactory.getSDK().getConfigManager().updateAllGroups();
        CatalogBean groupCatalogBean = new CatalogBean(TerminalFactory.getSDK().getParam(Params.DEP_NAME,""),TerminalFactory.getSDK().getParam(Params.DEP_ID,0));
        catalogNames.add(groupCatalogBean);
    }

    @OnClick({R.id.iv_return, R.id.iv_close})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_return:
                //关闭页面
                OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiverFragmentPopBackStackHandler.class);
                break;
            case R.id.iv_close:
                //搜索
                OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiverFragmentShowHandler.class, Constants.FRAGMENT_TAG_GROUP_SEARCH,new Bundle());
                break;
        }
    }

    /**
     * 收到fragment popBackStack
     */
    private ReceiverFragmentBackPressedByGroupChangeHandler receiverFragmentBackPressedByGroupChangeHandler = this::onBack;

    /**
     * 转组消息
     */
    private ReceiveChangeGroupHandler receiveChangeGroupHandler = new ReceiveChangeGroupHandler(){

        @Override
        public void handler(int errorCode, String errorDesc){
            logger.info("收到转组消息：" + errorCode + "/" + errorDesc);
            mHandler.post(() -> {
                if(errorCode == 0 || errorCode == SignalServerErrorCode.INVALID_SWITCH_GROUP.getErrorCode()){
                    ToastUtil.showToast(MyApplication.instance, getString(R.string.text_change_group_success));
                    ptt.terminalsdk.manager.Prompt.PromptManager.getInstance().changeGroupSuccess();
                    groupAdapter.notifyDataSetChanged();
                }else{
                    ToastUtil.showToast(MyApplication.instance, errorDesc);
                    ptt.terminalsdk.manager.Prompt.PromptManager.getInstance().changeGroupFail();
                }
            });
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
        if(resultCode == BaseCommonCode.SUCCESS_CODE){
            groupUpdateCompleted = false;
            tempGroupUpdateCompleted = false;
            catalogNames.clear();
            tvTitle.setText(getString(R.string.text_change_group));
            CatalogBean groupCatalogBean = new CatalogBean(TerminalFactory.getSDK().getParam(Params.DEP_NAME,""),TerminalFactory.getSDK().getParam(Params.DEP_ID,0));
            catalogNames.add(groupCatalogBean);
            TerminalFactory.getSDK().getConfigManager().updateAllGroups();
        }
    };
    /**
     * 临时组改变
     */
    private ReceiveMemberAboutTempGroupHandler receiveMemberAboutTempGroupHandler = new ReceiveMemberAboutTempGroupHandler() {
        @Override
        public void handler(boolean isAdd, boolean isLocked, boolean isScan, boolean isSwitch, int tempGroupNo, String tempGroupName, String tempGroupType) {
            mHandler.post(() -> {
                groupUpdateCompleted = false;
                tempGroupUpdateCompleted = false;
                catalogNames.clear();
                tvTitle.setText(getString(R.string.text_change_group));
                CatalogBean groupCatalogBean = new CatalogBean(TerminalFactory.getSDK().getParam(Params.DEP_NAME,""),TerminalFactory.getSDK().getParam(Params.DEP_ID,0));
                catalogNames.add(groupCatalogBean);
                TerminalFactory.getSDK().getConfigManager().updateAllGroups();
            });
        }
    };

    /**
     * 获取组信息
     */
    private ReceivegUpdateGroupHandler receivegUpdateGroupHandler = this::updateData;

    public synchronized void updateData(int depId, String depName, List<Department> departments, List<Group> groups){
        if(depId == -1){
            tempGroupDatas.clear();
            if(!groups.isEmpty()){
                for(Group group : groups){
                    GroupAndDepartment<Group> groupAndDepartment = new GroupAndDepartment<>();
                    groupAndDepartment.setType(Constants.TYPE_TEMP_GROUP);
                    groupAndDepartment.setBean(group);
                    tempGroupDatas.add(groupAndDepartment);
                }
            }
            tempGroupUpdateCompleted = true;
        }else{
            //请求一个添加一个部门标题
            commonGroupDatas.clear();
            //添加组
            for(Group group : groups){
                GroupAndDepartment<Group> groupAndDepartment = new GroupAndDepartment<>();
                groupAndDepartment.setType(Constants.TYPE_GROUP);
                groupAndDepartment.setBean(group);
                commonGroupDatas.add(groupAndDepartment);
            }
            //添加部门
            for(Department department : departments){
                GroupAndDepartment<Department> groupAndDepartment = new GroupAndDepartment<>();
                groupAndDepartment.setType(Constants.TYPE_FOLDER);
                groupAndDepartment.setBean(department);
                commonGroupDatas.add(groupAndDepartment);
            }
            groupUpdateCompleted = true;
        }
        if(updateCompleted()){
            //请求完成排序
            sortList();
        }
    }

    /**
     * 排序
     */
    private void sortList(){
        datas.clear();
        datas.addAll(tempGroupDatas);
        datas.addAll(commonGroupDatas);
        mHandler.post(() -> {
            if(groupAdapter !=null){
                groupAdapter.notifyDataSetChanged();
            }
            if(groupRecyclerView!=null){
                groupRecyclerView.scrollToPosition(0);
            }
        });
    }

    private boolean updateCompleted(){
        return tempGroupUpdateCompleted && groupUpdateCompleted;
    }

    /**
     * 返回操作
     */
    public void onBack(){
        if(catalogNames.size() > 1){
            //返回到上一级
            catalogNames.remove(catalogNames.size()-1);
            tempGroupUpdateCompleted = true;
            groupUpdateCompleted = false;
            int position = catalogNames.size()-1;
            TerminalFactory.getSDK().getConfigManager().updateGroup(catalogNames.get(position).getId(),catalogNames.get(position).getName());
            tvTitle.setText((catalogNames.size() == 1)?getString(R.string.text_change_group):catalogNames.get(position).getName());
            groupRecyclerView.scrollToPosition(0);
        }else{
            //关闭页面
            tvTitle.setText(getString(R.string.text_change_group));
            OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiverFragmentPopBackStackHandler.class);
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
        OperateReceiveHandlerUtilSync.getInstance().unregistReceiveHandler(receiverFragmentBackPressedByGroupChangeHandler);//收到onKeyDown
    }
}
