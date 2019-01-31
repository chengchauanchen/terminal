package cn.vsx.vc.fragment;

import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;

import com.zectec.imageandfileselector.utils.OperateReceiveHandlerUtilSync;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;
import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.model.Member;
import cn.vsx.hamster.terminalsdk.model.MemberResponse;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveUpdateConfigHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveUpdatePhoneMemberHandler;
import cn.vsx.hamster.terminalsdk.tools.Params;
import cn.vsx.vc.R;
import cn.vsx.vc.activity.NewMainActivity;
import cn.vsx.vc.adapter.CatalogAdapter;
import cn.vsx.vc.adapter.ContactAdapter;
import cn.vsx.vc.model.CatalogBean;
import cn.vsx.vc.model.ContactItemBean;
import cn.vsx.vc.receiveHandle.ReceiverShowPersonFragmentHandler;
import cn.vsx.vc.utils.CommonGroupUtil;
import cn.vsx.vc.utils.Constants;
import ptt.terminalsdk.context.MyTerminalFactory;
import ptt.terminalsdk.tools.ToastUtil;

/**
 * 警务通
 */
public class NewPoliceAffairsFragment extends BaseFragment {

    @Bind(R.id.catalog_recyclerview)
    RecyclerView mCatalogRecyclerview;
    @Bind(R.id.swipeRefreshLayout)
    SwipeRefreshLayout swipeRefreshLayout;
    @Bind(R.id.recyclerview)
    RecyclerView mRecyclerview;

    private NewMainActivity mActivity;
    private List<CatalogBean> mCatalogList=new ArrayList<>();

    private List<ContactItemBean> mDatas=new ArrayList<>();
    private Handler myHandler = new Handler();
    private CatalogAdapter mCatalogAdapter;
    private ContactAdapter mContactAdapter;
    private List<CatalogBean> mInitCatalogList=new ArrayList<>();

    //更新警务通成员信息
    private ReceiveUpdatePhoneMemberHandler receiveUpdatePhoneMemberHandler = allMembers -> myHandler.post(() -> {
        MemberResponse memberResponse = TerminalFactory.getSDK().getConfigManager().getPhoneMemeberInfo();
        List<CatalogBean> catalogBeanList = new ArrayList<>();
        CatalogBean bean = new CatalogBean();
        bean.setName(memberResponse.getName());
        bean.setBean(memberResponse);
        catalogBeanList.add(bean);
        updateData(memberResponse,catalogBeanList);
    });

    /**
     * 更新配置信息
     */
    private ReceiveUpdateConfigHandler receiveUpdateConfigHandler = () -> {//更新当前组
        CommonGroupUtil.setCatchGroupIdList(MyTerminalFactory.getSDK().getParam(Params.CURRENT_GROUP_ID, 0));
        myHandler.post(() -> {
            MemberResponse memberResponse = TerminalFactory.getSDK().getConfigManager().getPhoneMemeberInfo();
            List<CatalogBean> catalogBeanList = new ArrayList<>();
            CatalogBean bean = new CatalogBean();
            bean.setName(memberResponse.getName());
            bean.setBean(memberResponse);
            catalogBeanList.add(bean);
            updateData(memberResponse,catalogBeanList);
        });
    };

    @Override
    public int getContentViewId() {
        return R.layout.fragment_new_police_affairs;
    }

    @Override
    public void initView() {
        mCatalogRecyclerview.setLayoutManager(new LinearLayoutManager(getActivity(), OrientationHelper.HORIZONTAL,false));
        mCatalogAdapter=new CatalogAdapter(getActivity(),mCatalogList);
        mCatalogRecyclerview.setAdapter(mCatalogAdapter);

        mRecyclerview.setLayoutManager(new LinearLayoutManager(getActivity()));
        mContactAdapter=new ContactAdapter(getActivity(),mDatas,true);
        mRecyclerview.setAdapter(mContactAdapter);

    }

    @Override
    public void initData() {
        mActivity= (NewMainActivity) getActivity();
        MemberResponse mMemberResponse = TerminalFactory.getSDK().getConfigManager().getPhoneMemeberInfo();
        if(mMemberResponse ==null){
            return;
        }
        CatalogBean catalog=new CatalogBean();
        catalog.setName(mMemberResponse.getName());
        catalog.setBean(mMemberResponse);
        mInitCatalogList.add(catalog);

        updateData(mMemberResponse,mInitCatalogList);

    }


    /**
     * 设置数据
     */
    private void updateData(MemberResponse memberResponse,List<CatalogBean> catalogBeanList){
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
        List<Member> memberList = memberResponse.getMembers();
        //添加子成员
        if(memberList != null && !memberList.isEmpty()){
            List<ContactItemBean> itemMemberList = new ArrayList<>();
            for(Member member : memberList){
                if(member.getName()==null){
                    continue;
                }
                ContactItemBean<Member> bean = new ContactItemBean<>();
                bean.setBean(member);
                bean.setType(Constants.TYPE_USER);
                itemMemberList.add(bean);
            }
            mDatas.addAll(itemMemberList);
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
            }
        }
    }


    @Override
    public void initListener() {

        MyTerminalFactory.getSDK().registReceiveHandler(receiveUpdatePhoneMemberHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveUpdateConfigHandler);

        mCatalogAdapter.setOnItemClick((view, position) -> {
            MemberResponse memberResponse=mCatalogList.get(position).getBean();

            List<CatalogBean> catalogList=new ArrayList<>();
            catalogList.addAll(mCatalogList.subList(0,position+1));

            updateData(memberResponse,catalogList);

        });


        mContactAdapter.setOnItemClickListener((view, postion, type) -> {
            if (type==Constants.TYPE_DEPARTMENT){
                MemberResponse memberResponse= (MemberResponse) mDatas.get(postion).getBean();
                CatalogBean catalog=new CatalogBean();
                catalog.setName(memberResponse.getName());
                catalog.setBean(memberResponse);
                mCatalogList.add(catalog);

                List<CatalogBean> catalogBeanList=new ArrayList<>();
                catalogBeanList.addAll(mCatalogList);

                updateData(memberResponse,catalogBeanList);
            }
        });

        swipeRefreshLayout.setOnRefreshListener(() -> {
            TerminalFactory.getSDK().getConfigManager().updataPhoneMemberInfo();
            myHandler.postDelayed(() -> {
                // 加载完数据设置为不刷新状态，将下拉进度收起来
                swipeRefreshLayout.setRefreshing(false);
                // 加载完数据设置为不刷新状态，将下拉进度收起来

            }, 1200);
        });
    }

    private long lastSearchTime;
    /**
     * 搜索
     */
    @OnClick(R.id.iv_search)
    public void onViewClicked() {
        if(System.currentTimeMillis() - lastSearchTime<1000){
            return;
        }
        List<Member> memberList=TerminalFactory.getSDK().getConfigManager().getPhoneMembers();
        if (memberList!=null) {
            OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiverShowPersonFragmentHandler.class, memberList);
        }else {
            ToastUtil.showToast(getActivity(),getString(R.string.text_no_general_police_users));
        }
        lastSearchTime = System.currentTimeMillis();
    }

    @Override
    public void onDestroy() {
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveUpdatePhoneMemberHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveUpdateConfigHandler);
        super.onDestroy();
    }

    /**
     * 返回操作
     */
   public void onBack(){
       if (mCatalogList.size() > 1) {
           MemberResponse memberResponse = mCatalogList.get(mCatalogList.size() - 2).getBean();

           mCatalogList.remove(mCatalogList.size() - 1);

           List<CatalogBean> catalogBeanList = new ArrayList<>();
           catalogBeanList.addAll(mCatalogList);

           updateData(memberResponse, catalogBeanList);

       } else {
           mActivity.exit();
       }
   }
}
