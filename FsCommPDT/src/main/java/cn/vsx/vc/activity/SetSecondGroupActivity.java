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

import cn.vsx.vc.R;
import cn.vsx.vc.adapter.GroupCatalogAdapter;
import cn.vsx.vc.adapter.GroupListAdapter;
import cn.vsx.vc.model.CatalogBean;
import cn.vsx.vc.model.ContactItemBean;

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

    private List<CatalogBean> catalogNames=new ArrayList<>();
    private List<ContactItemBean> mData = new ArrayList<>();

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
    }

    @Override
    public void initData(){
        mBarTitle.setText(R.string.text_choose);

        mRecyclerview.setLayoutManager(new LinearLayoutManager(this));
        groupListAdapter = new GroupListAdapter(this, mData);
        mRecyclerview.setAdapter(groupListAdapter);

        mParentRecyclerview.setLayoutManager(new LinearLayoutManager(this, OrientationHelper.HORIZONTAL, false));
        parentRecyclerAdapter = new GroupCatalogAdapter(this,catalogNames);
        parentRecyclerAdapter.setOnItemClick(this);
        mParentRecyclerview.setAdapter(parentRecyclerAdapter);
    }

    @Override
    public void doOtherDestroy(){
    }

    @Override
    public void onItemClick(View view, int position){

    }
}
