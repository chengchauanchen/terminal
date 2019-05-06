package cn.vsx.vc.activity;

import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;
import cn.vsx.hamster.common.GroupType;
import cn.vsx.hamster.errcode.BaseCommonCode;
import cn.vsx.hamster.terminalsdk.model.Group;
import cn.vsx.hamster.terminalsdk.model.GroupBean;
import cn.vsx.hamster.terminalsdk.model.GroupResponse;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveSetScanGroupListResultHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveUpdateFoldersAndGroupsHandler;
import cn.vsx.vc.R;
import cn.vsx.vc.adapter.GroupCatalogAdapter;
import cn.vsx.vc.adapter.GroupScanAddAdapter;
import cn.vsx.vc.fragment.ScanGroupSearchFragment;
import cn.vsx.vc.model.GroupCatalogBean;
import cn.vsx.vc.model.GroupItemBean;
import cn.vsx.vc.utils.Constants;
import ptt.terminalsdk.context.MyTerminalFactory;
import ptt.terminalsdk.tools.ToastUtil;

/**
 * Created by Administrator on 2017/3/16 0016.
 */

public class ChangeGroupActivity extends BaseActivity {

    @Bind(R.id.catalog_recyclerview)
    RecyclerView mCatalogRecyclerview;
    @Bind(R.id.recyclerview)
    RecyclerView mRecyclerview;
    @Bind(R.id.bar_title)
    TextView barTitle;
    @Bind(R.id.right_btn)
    ImageView rightBtn;
    @Bind(R.id.ok_btn)
    Button okBtn;
    @Bind(R.id.fl_fragment_container_main)
    FrameLayout fl_fragment_container_main;

    private static Handler myHandler = new Handler();
    private List<GroupCatalogBean> mCatalogList=new ArrayList<>();
    private List<GroupCatalogBean> mInitGroupCatalogList=new ArrayList<>();
    private List<GroupItemBean> mDatas=new ArrayList<>();
    private List<Integer> groupSweeps = new ArrayList<>();
    private GroupResponse mGroupResponse;
    private GroupCatalogAdapter mCatalogAdapter;
    private GroupScanAddAdapter mGroupAdapter;
    //顶级组
    private GroupBean topGroup;

    @Override
    public int getLayoutResId() {
        return R.layout.activity_change_group;
    }

    @Override
    public void initView() {
        barTitle.setText(R.string.text_add_scan_group);
        rightBtn.setVisibility(View.GONE);
        okBtn.setVisibility(View.GONE);

        mCatalogRecyclerview.setLayoutManager(new LinearLayoutManager(this, OrientationHelper.HORIZONTAL,false));
//        mCatalogAdapter =new GroupCatalogAdapter(this,mCatalogList);
        mCatalogRecyclerview.setAdapter(mCatalogAdapter);

        mRecyclerview.setLayoutManager(new LinearLayoutManager(this));
        mGroupAdapter =new GroupScanAddAdapter(this,mDatas);
        mRecyclerview.setAdapter(mGroupAdapter);
    }

    @Override
    public void initListener() {
        MyTerminalFactory.getSDK().registReceiveHandler(mReceiveSetScanGroupListResultHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveUpdateFoldersAndGroupsHandler);

        //横向标题栏容器
        mCatalogAdapter.setOnItemClick((view, position) -> {
//            topGroup=mCatalogList.get(position).getBean();

            List<GroupCatalogBean> catalogList=new ArrayList<>();
            catalogList.addAll(mCatalogList.subList(0,position+1));
            updateData(catalogList);
        });

        //群组条目容器
        mGroupAdapter.setOnItemClickListener((view, postion, type) -> {
            if (type==Constants.TYPE_FOLDER){

                topGroup= (GroupBean) mDatas.get(postion).getBean();
                GroupCatalogBean catalog=new GroupCatalogBean();
                catalog.setName(topGroup.getName());
//                catalog.setBean(topGroup);
                mCatalogList.add(catalog);

                List<GroupCatalogBean> catalogBeanList=new ArrayList<>();
                catalogBeanList.addAll(mCatalogList);
                updateData(catalogBeanList);
            }
        });
    }


    @Override
    public void initData() {
//        mGroupResponse = MyTerminalFactory.getSDK().getConfigManager().getAllGroupInfo();
//
//        groupSweeps.addAll(MyTerminalFactory.getSDK().getConfigManager().loadScanGroup());//获取扫描组列表
//
//        if(mGroupResponse == null){
//            return;
//        }
//        initGroupData();
//        GroupCatalogBean catalog=new GroupCatalogBean();
//        catalog.setName(topGroup.getTitleName());
//        catalog.setBean(topGroup);
//        mInitGroupCatalogList.add(catalog);
//
//        updateData(mInitGroupCatalogList);

    }

    private void initGroupData(){
        List<GroupBean> groupBeans = mGroupResponse.getMemberGroups();
        for(GroupBean groupBean : groupBeans){
            //临时组id为-1
            if(groupBean.getId()!= -1){
                topGroup = groupBean;
                break;
            }
        }
    }

    /**
     * 设置数据
     */
    private void updateData(List<GroupCatalogBean> catalogBeanList){
        mDatas.clear();
        mCatalogList.clear();
        //设置组群上面数据
        mCatalogList.addAll(catalogBeanList);
        //设置组群数据
        setData(topGroup);
    }

    private void setData(GroupBean topGroup){
        if(topGroup !=null){

            //设置组群数据
            addData(topGroup);

            mGroupAdapter.notifyDataSetChanged();
            mCatalogAdapter.notifyDataSetChanged();
            //目录导航滑动到最后
            mCatalogRecyclerview.scrollToPosition(mCatalogList.size()-1);
        }
    }

    private void addData(GroupBean topGroup){
        if (topGroup != null){
            addItemGroup(topGroup);
            addItemDepartment(topGroup);
        }
    }

    /**
     * 添加子组
     */
    @SuppressWarnings("unchecked")
    private void addItemGroup(GroupBean topGroup){
        List<Group> groupList = topGroup.getGroups();
        List<GroupItemBean> itemGroupList=new ArrayList<>();
        if (groupList != null && groupList.size() > 0) {
            for (Group group : groupList) {
                if(group.getName() ==null){
                    continue;
                }
                //响应组不显示
                if(!group.getGroupType().equals(GroupType.RESPONSE.toString()) ){
                    GroupItemBean<Group> bean = new GroupItemBean<>();
                    bean.setBean(group);
                    bean.setType(Constants.TYPE_GROUP);
                    itemGroupList.add(bean);
                }
            }
        }
//        Collections.sort(itemGroupList);
        mDatas.addAll(itemGroupList);
    }

    /**
     * 添加子部门
     */
    @SuppressWarnings("unchecked")
    private void addItemDepartment(GroupBean topGroup){
        List<GroupBean> data = topGroup.getChildren();
        if(data!=null && !data.isEmpty()){
            for(GroupBean next : data){
                if(next.getName() ==null){
                    continue;
                }
                GroupItemBean<GroupBean> bean = new GroupItemBean<>();
                bean.setType(Constants.TYPE_FOLDER);
                bean.setName(next.getName());
                bean.setBean(next);
                mDatas.add(bean);
//                Collections.sort(mDatas);
            }
        }
    }

    @Override
    public void doOtherDestroy() {

        MyTerminalFactory.getSDK().unregistReceiveHandler(mReceiveSetScanGroupListResultHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveUpdateFoldersAndGroupsHandler);

    }
    /**====================================================handler===============================================================================**/

    //响应扫描组设置
    private ReceiveSetScanGroupListResultHandler mReceiveSetScanGroupListResultHandler=new ReceiveSetScanGroupListResultHandler(){

        @Override
        public void handler(final List<Integer> scanGroups, final int errorCode, final String errorDesc) {
            myHandler.post(() -> {
                logger.info("ReceiveSetScanGroupListResultHandler："+errorDesc+"======="+scanGroups);
                if(errorCode==BaseCommonCode.SUCCESS_CODE){
                    ToastUtil.toast(ChangeGroupActivity.this,getString(R.string.text_add_scan_group_success));
                    groupSweeps.clear();
                    groupSweeps.addAll(scanGroups);
                    mGroupAdapter.notifyDataSetChanged();

                }else {
                    ToastUtil.toast(ChangeGroupActivity.this,getString(R.string.text_add_scan_group_fail));
                }
            });


        }
    };

    /**更新文件夹和组列表数据*/
    private ReceiveUpdateFoldersAndGroupsHandler receiveUpdateFoldersAndGroupsHandler = new ReceiveUpdateFoldersAndGroupsHandler(){

        @Override
        public void handler() {
            myHandler.post(() -> {
//                mGroupResponse = MyTerminalFactory.getSDK().getConfigManager().getAllGroupInfo();
//                if(mGroupResponse == null){
//                    return;
//                }
//                initGroupData();
//
//                mInitGroupCatalogList.clear();
//                GroupCatalogBean catalog=new GroupCatalogBean();
//                catalog.setName(topGroup.getTitleName());
//                catalog.setBean(topGroup);
//                mInitGroupCatalogList.add(catalog);
//                updateData(mInitGroupCatalogList);
            });
        }

    };


    /**====================================================handler===============================================================================**/
    @OnClick({R.id.news_bar_back,R.id.iv_search})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.news_bar_back://返回
                finish();
                break;
            case R.id.iv_search://搜索
                if(MyTerminalFactory.getSDK().getConfigManager().getScanGroups().size()>=10){
                    ToastUtil.toast(ChangeGroupActivity.this,getString(R.string.text_add_scan_group_out_of_max_count));
                    return;
                }
                ScanGroupSearchFragment groupSearchFragment = new ScanGroupSearchFragment();
                fl_fragment_container_main.setVisibility(View.VISIBLE);
                getSupportFragmentManager().beginTransaction().addToBackStack(null).replace(R.id.fl_fragment_container_main, groupSearchFragment).commit();
                break;
        }
    }
}
