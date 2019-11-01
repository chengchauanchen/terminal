package cn.vsx.vc.activity;

import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.blankj.utilcode.util.ToastUtils;

import java.util.ArrayList;
import java.util.List;

import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.model.Department;
import cn.vsx.hamster.terminalsdk.model.Group;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveGroupSelectedHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceivegUpdateGroupHandler;
import cn.vsx.hamster.terminalsdk.tools.Params;
import cn.vsx.vc.R;
import cn.vsx.vc.adapter.GroupCatalogAdapter;
import cn.vsx.vc.adapter.GroupListAdapter;
import cn.vsx.vc.fragment.SearchFragment;
import cn.vsx.vc.model.CatalogBean;
import cn.vsx.vc.model.ContactItemBean;
import cn.vsx.vc.utils.Constants;
import cn.vsx.vc.utils.WuTieUtil;

/**
 * 作者：ly-xuxiaolong
 * 版本：1.0
 * 创建日期：2019/9/19
 * 描述：
 * 修订历史：
 */
public class SetSecondGroupActivity extends BaseActivity implements GroupCatalogAdapter.ItemClickListener, View.OnClickListener, GroupListAdapter.ItemClickListener{

    private ImageView mNewsBarBack;
    private TextView mBarTitle;
    private ImageView right_btn;
    private Button mOkBtn;
    private RecyclerView mRecyclerview;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView mParentRecyclerview;
    private ImageView mIvSearch;
    private FrameLayout flFragmentContainer;
    private GroupListAdapter groupListAdapter;
    private GroupCatalogAdapter parentRecyclerAdapter;

    //显示在recyclerview上的所有数据
    private List<ContactItemBean> datas = new ArrayList<>();

    private List<CatalogBean> catalogNames = new ArrayList<>();

    private int selectGroupNo;

    @Override
    public int getLayoutResId(){
        return R.layout.activity_set_second_group;
    }

    @Override
    public void initView(){
        mNewsBarBack = (ImageView) findViewById(R.id.news_bar_back);
        mBarTitle = (TextView) findViewById(R.id.bar_title);
        right_btn = (ImageView) findViewById(R.id.right_btn);
        mOkBtn = (Button) findViewById(R.id.ok_btn);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        mRecyclerview = (RecyclerView) findViewById(R.id.recyclerview);
        mParentRecyclerview = (RecyclerView) findViewById(R.id.parent_recyclerview);
        mIvSearch = (ImageView) findViewById(R.id.iv_search);
        flFragmentContainer = (FrameLayout) findViewById(R.id.fl_fragment_container);
    }

    @Override
    public void initListener(){
        mNewsBarBack.setOnClickListener(this);
        mOkBtn.setOnClickListener(this);
        TerminalFactory.getSDK().registReceiveHandler(receiveGroupSelectedHandler);
        TerminalFactory.getSDK().registReceiveHandler(receivegUpdateGroupHandler);
        swipeRefreshLayout.setProgressBackgroundColorSchemeResource(R.color.white);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorAccent, R.color.colorPrimary, R.color.colorPrimaryDark);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            catalogNames.clear();
            catalogNames.add(WuTieUtil.addRootDetpCatalogNames());
            TerminalFactory.getSDK().getConfigManager().updateGroup(TerminalFactory.getSDK().getParam(Params.DEP_ID, 0), TerminalFactory.getSDK().getParam(Params.DEP_NAME, ""));
            myHandler.postDelayed(() -> {
                // 加载完数据设置为不刷新状态，将下拉进度收起来
                swipeRefreshLayout.setRefreshing(false);
                // 加载完数据设置为不刷新状态，将下拉进度收起来
            }, 1200);
        });
        //搜索按钮的点击事件
        mIvSearch.setOnClickListener(v -> {
            ArrayList<Integer> selectedNos = new ArrayList<>();
            if(selectGroupNo !=0){
                selectedNos.add(selectGroupNo);
            }
            SearchFragment searchFragment = SearchFragment.newInstance(Constants.TYPE_CHECK_SEARCH_BUTTON_GROUP, selectedNos,null);
            searchFragment.setBacklistener(() -> {
                flFragmentContainer.setVisibility(View.GONE);
                getSupportFragmentManager().beginTransaction().remove(searchFragment).commit();
            });
            flFragmentContainer.setVisibility(View.VISIBLE);
            getSupportFragmentManager().beginTransaction().addToBackStack(null).replace(R.id.fl_fragment_container, searchFragment).commit();
//            myHandler.postDelayed(() -> ll_content.setVisibility(View.GONE),500);
        });
    }

    @Override
    public void initData(){
        mBarTitle.setText(R.string.text_choose);
        right_btn.setVisibility(View.GONE);
        selectGroupNo = TerminalFactory.getSDK().getParam(Params.SECOND_GROUP_ID,0);
        mRecyclerview.setLayoutManager(new LinearLayoutManager(this));
        groupListAdapter = new GroupListAdapter(this, datas);
        groupListAdapter.checkGroupNo(selectGroupNo);
        mRecyclerview.setAdapter(groupListAdapter);
        groupListAdapter.setItemClickListener(this);
        mParentRecyclerview.setLayoutManager(new LinearLayoutManager(this, OrientationHelper.HORIZONTAL, false));
        parentRecyclerAdapter = new GroupCatalogAdapter(this,catalogNames);
        parentRecyclerAdapter.setOnItemClick(this);
        mParentRecyclerview.setAdapter(parentRecyclerAdapter);
        catalogNames.clear();
        catalogNames.add(WuTieUtil.addRootDetpCatalogNames());
        TerminalFactory.getSDK().getConfigManager().updateGroup(TerminalFactory.getSDK().getParam(Params.DEP_ID, 0), TerminalFactory.getSDK().getParam(Params.DEP_NAME, ""));
    }

    @Override
    public void doOtherDestroy(){
        TerminalFactory.getSDK().unregistReceiveHandler(receivegUpdateGroupHandler);
        TerminalFactory.getSDK().unregistReceiveHandler(receiveGroupSelectedHandler);
    }

    @Override
    public void onItemClick(View view, int position){
        if(position>=0 && position < catalogNames.size()-1){
            synchronized(SetSecondGroupActivity.this){
                TerminalFactory.getSDK().getConfigManager().updateGroup(catalogNames.get(position).getId(),catalogNames.get(position).getName());
                List<CatalogBean> groupCatalogBeans = new ArrayList<>(catalogNames.subList(0, position));
                catalogNames.clear();
                catalogNames.addAll(groupCatalogBeans);
            }
        }
    }

    private ReceivegUpdateGroupHandler receivegUpdateGroupHandler = (depId, depName, departments, groups) -> myHandler.post(() -> updateData( depId,depName, departments, groups));

    private ReceiveGroupSelectedHandler receiveGroupSelectedHandler = new ReceiveGroupSelectedHandler(){
        @Override
        public void handler(Group group, boolean selected){
            if(group.getNo() != selectGroupNo){
                if(selected){
                    selectGroupNo = group.getNo();
                }
            }else {
                if(!selected){
                    selectGroupNo = 0;
                }
            }
            groupListAdapter.checkGroupNo(selectGroupNo);
            groupListAdapter.notifyDataSetChanged();
        }
    };
    /**
     * 更新数据（组）
     * @param depId
     * @param departments
     * @param groups
     */
    private void updateData(int depId, String depName,List<Department> departments, List<Group> groups){
        datas.clear();
        //根部门的数据，清空之前的数据
        if(depId == TerminalFactory.getSDK().getParam(Params.DEP_ID, 0)){
            catalogNames.clear();
        }
        //请求一个添加一个部门标题
        CatalogBean catalogBean = WuTieUtil.getCatalogBean( depId, depName);
        catalogNames.add(catalogBean);
        //过滤根部门中的组
        List<Group> groupList = WuTieUtil.filterRootDeptment(depId,groups);
        //添加组
        for(Group group : groupList){
            ContactItemBean<Group> groupAndDepartment = new ContactItemBean<>();
            groupAndDepartment.setType(Constants.TYPE_GROUP);
            groupAndDepartment.setBean(group);
            datas.add(groupAndDepartment);
        }
        //添加部门
        for(Department department : departments){
            ContactItemBean<Department> groupAndDepartment = new ContactItemBean<>();
            groupAndDepartment.setType(Constants.TYPE_FOLDER);
            groupAndDepartment.setBean(department);
            datas.add(groupAndDepartment);
        }
        parentRecyclerAdapter.notifyDataSetChanged();
        groupListAdapter.notifyDataSetChanged();
        mRecyclerview.scrollToPosition(0);
    }

    @Override
    public void onClick(View view){
        int id = view.getId();
        if(id == R.id.news_bar_back){
            onBackPressed();
        }else if(id == R.id.ok_btn){
            if(selectGroupNo !=0){
                TerminalFactory.getSDK().putParam(Params.SECOND_GROUP_ID,selectGroupNo);
                TerminalFactory.getSDK().putParam(Params.VOLUME_DOWN, true);
                finish();
            }else {
                ToastUtils.showShort(R.string.text_please_select_group);
            }
        }
    }

    @Override
    public void itemClick(int type, int position){
        if(type == Constants.TYPE_GROUP){
            Group group = (Group) datas.get(position).getBean();
            selectGroupNo = group.getNo();
        }else if(type == Constants.TYPE_FOLDER){
            TerminalFactory.getSDK().getConfigManager().updateGroup(((Department) datas.get(position).getBean()).getId(),((Department) datas.get(position).getBean()).getName());
        }
    }

    @Override
    public void onBackPressed(){
        if(catalogNames.size() > 1){
            //返回上一层
            TerminalFactory.getSDK().getConfigManager().updateGroup(catalogNames.get(catalogNames.size()-2).getId(),catalogNames.get(catalogNames.size()-2).getName());
            List<CatalogBean> groupCatalogBeans = new ArrayList<>(catalogNames.subList(0, catalogNames.size()-2));
            catalogNames.clear();
            catalogNames.addAll(groupCatalogBeans);
        }else {
            super.onBackPressed();
        }
    }
}
