package cn.vsx.vc.activity;

import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.model.Department;
import cn.vsx.hamster.terminalsdk.model.Group;
import cn.vsx.hamster.terminalsdk.model.GroupAndDepartment;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceivegUpdateGroupHandler;
import cn.vsx.hamster.terminalsdk.tools.Params;
import cn.vsx.vc.R;
import cn.vsx.vc.adapter.GroupCatalogAdapter;
import cn.vsx.vc.adapter.GroupListAdapter;
import cn.vsx.vc.model.CatalogBean;
import cn.vsx.vc.model.ContactItemBean;
import cn.vsx.vc.utils.Constants;

/**
 * 作者：ly-xuxiaolong
 * 版本：1.0
 * 创建日期：2019/9/19
 * 描述：
 * 修订历史：
 */
public class SetSecondGroupActivity extends BaseActivity implements GroupCatalogAdapter.ItemClickListener{

    private ImageView mNewsBarBack;
    private TextView mBarTitle;
    private Button mOkBtn;
    private RecyclerView mRecyclerview;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView mParentRecyclerview;
    private ImageView mIvSearch;
    private GroupListAdapter groupListAdapter;
    private GroupCatalogAdapter parentRecyclerAdapter;

    //显示在recyclerview上的所有数据
    private List<ContactItemBean> datas = new ArrayList<>();
    //临时组的数据
    private List<ContactItemBean> tempGroupDatas = new ArrayList<>();
    //普通部门组的数据
    private List<ContactItemBean> commonGroupDatas = new ArrayList<>();
    //点进子部门之前的数据
    private List<ContactItemBean> lastGroupDatas = new ArrayList<>();

    private List<CatalogBean> catalogNames = new ArrayList<>();
    private List<CatalogBean> tempCatalogNames = new ArrayList<>();

    @Override
    public int getLayoutResId(){
        return R.layout.activity_set_second_group;
    }

    @Override
    public void initView(){
        mNewsBarBack = (ImageView) findViewById(R.id.news_bar_back);
        mBarTitle = (TextView) findViewById(R.id.bar_title);
        mOkBtn = (Button) findViewById(R.id.ok_btn);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        mRecyclerview = (RecyclerView) findViewById(R.id.recyclerview);
        mParentRecyclerview = (RecyclerView) findViewById(R.id.parent_recyclerview);
        mIvSearch = (ImageView) findViewById(R.id.iv_search);
    }

    @Override
    public void initListener(){
        TerminalFactory.getSDK().registReceiveHandler(receivegUpdateGroupHandler);
        swipeRefreshLayout.setProgressBackgroundColorSchemeResource(R.color.white);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorAccent, R.color.colorPrimary, R.color.colorPrimaryDark);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            catalogNames.clear();
            tempCatalogNames.clear();

            CatalogBean groupCatalogBean = new CatalogBean(TerminalFactory.getSDK().getParam(Params.DEP_NAME,""),TerminalFactory.getSDK().getParam(Params.DEP_ID,0));
            catalogNames.add(groupCatalogBean);
            TerminalFactory.getSDK().getConfigManager().updateAllGroups();
            TerminalFactory.getSDK().getConfigManager().updateAllGroupInfo(false);
            myHandler.postDelayed(() -> {
                // 加载完数据设置为不刷新状态，将下拉进度收起来
                swipeRefreshLayout.setRefreshing(false);
                // 加载完数据设置为不刷新状态，将下拉进度收起来
            }, 1200);
        });
    }

    @Override
    public void initData(){
        mBarTitle.setText(R.string.text_choose);

        mRecyclerview.setLayoutManager(new LinearLayoutManager(this));
        groupListAdapter = new GroupListAdapter(this, datas);
        mRecyclerview.setAdapter(groupListAdapter);

        mParentRecyclerview.setLayoutManager(new LinearLayoutManager(this, OrientationHelper.HORIZONTAL, false));
        parentRecyclerAdapter = new GroupCatalogAdapter(this,catalogNames);
        parentRecyclerAdapter.setOnItemClick(this);
        mParentRecyclerview.setAdapter(parentRecyclerAdapter);

        CatalogBean groupCatalogBean = new CatalogBean(TerminalFactory.getSDK().getParam(Params.DEP_NAME,""),TerminalFactory.getSDK().getParam(Params.DEP_ID,0));
        catalogNames.add(groupCatalogBean);
        TerminalFactory.getSDK().getConfigManager().updateGroup(TerminalFactory.getSDK().getParam(Params.DEP_ID, 0), TerminalFactory.getSDK().getParam(Params.DEP_NAME, ""));
    }

    @Override
    public void doOtherDestroy(){
        TerminalFactory.getSDK().unregistReceiveHandler(receivegUpdateGroupHandler);
    }

    @Override
    public void onItemClick(View view, int position){

    }

    private ReceivegUpdateGroupHandler receivegUpdateGroupHandler = (depId, depName, departments, groups) -> myHandler.post(() -> updateData( depId, departments, groups));

    /**
     * 更新数据（组）
     * @param depId
     * @param departments
     * @param groups
     */
    private void updateData(int depId, List<Department> departments, List<Group> groups){
        //请求一个添加一个部门标题
        commonGroupDatas.clear();
        //部门标题
        ContactItemBean<Object> Title = new ContactItemBean<>();
        Title.setType(Constants.TYPE_TITLE);
        Title.setBean(new Object());
        commonGroupDatas.add(Title);
        //添加组
        for(Group group : groups){
            ContactItemBean<Group> groupAndDepartment = new ContactItemBean<>();
            groupAndDepartment.setType(Constants.TYPE_GROUP);
            groupAndDepartment.setBean(group);
            commonGroupDatas.add(groupAndDepartment);
        }
        //添加部门
        for(Department department : departments){
            ContactItemBean<Department> groupAndDepartment = new ContactItemBean<>();
            groupAndDepartment.setType(Constants.TYPE_FOLDER);
            groupAndDepartment.setBean(department);
            commonGroupDatas.add(groupAndDepartment);
        }
        groupListAdapter.notifyDataSetChanged();
    }
}
