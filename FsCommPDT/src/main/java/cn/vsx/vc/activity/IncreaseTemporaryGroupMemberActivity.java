package cn.vsx.vc.activity;

import android.content.Intent;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.text.TextPaint;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Iterator;
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
import cn.vsx.vc.adapter.CatalogAdapter;
import cn.vsx.vc.adapter.IncreaseTemporaryGroupMemberAdapter;
import cn.vsx.vc.fragment.TempGroupSearchFragment;
import cn.vsx.vc.model.CatalogBean;
import cn.vsx.vc.model.ContactItemBean;
import cn.vsx.vc.receiveHandle.ReceiverSelectTempGroupMemberHandler;
import cn.vsx.vc.utils.CommonGroupUtil;
import cn.vsx.vc.utils.Constants;
import cn.vsx.vc.utils.DataUtil;
import ptt.terminalsdk.context.MyTerminalFactory;
import ptt.terminalsdk.tools.ToastUtil;

public class IncreaseTemporaryGroupMemberActivity extends BaseActivity  {
    @Bind(R.id.ll_select_member)
    LinearLayout ll_select_member;
    @Bind(R.id.catalog_recyclerview)
    RecyclerView mCatalogRecyclerview;
    @Bind(R.id.swipeRefreshLayout)
    SwipeRefreshLayout swipeRefreshLayout;
    @Bind(R.id.recyclerview)
    RecyclerView mRecyclerview;
    @Bind(R.id.ok_btn)
    Button okBtn;
    @Bind(R.id.bar_title)
    TextView barTitle;
    @Bind(R.id.right_btn)
    ImageView rightBtn;
    @Bind(R.id.horizonMenu)
    HorizontalScrollView horizonMenu;
    @Bind(R.id.tv_checktext)
    TextView tv_checktext;

    //上部分
    private List<CatalogBean> mCatalogList = new ArrayList<>();

    //下部分
    private List<ContactItemBean> mDatas = new ArrayList<>();

    private Handler myHandler = new Handler();
    private CatalogAdapter mCatalogAdapter;//上部分横向
    private IncreaseTemporaryGroupMemberAdapter mIncreaseTemporaryGroupMemberAdapter;//下部门竖直
    private List<CatalogBean> mInitCatalogList=new ArrayList<>();
    private int CREATE_TEMP_GROUP=0;
    private int INCREASE_MEMBER=1;
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

    private ReceiverSelectTempGroupMemberHandler receiverSelectTempGroupMemberHandler=new ReceiverSelectTempGroupMemberHandler(){

        @Override
        public void handler(final int memberNo, boolean isAdd) {
            myHandler.post(() -> {
                List<Integer> selectMember = mIncreaseTemporaryGroupMemberAdapter.getSelectItem();
                if(selectMember.contains(memberNo)){
                    ToastUtil.showToast(IncreaseTemporaryGroupMemberActivity.this,"您已经添加过该成员");
                    return;
                }
                mIncreaseTemporaryGroupMemberAdapter.setSelectItem(memberNo);
            });
        }
    };
    private int screenWidth;
    private int type;

    @Override
    public int getLayoutResId() {
        return R.layout.activity_increase_temporary_group_member;
    }

    @Override
    public void initView() {
        WindowManager windowManager = (WindowManager) this.getSystemService(WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(dm);
        screenWidth = dm.widthPixels;

        mCatalogRecyclerview.setLayoutManager(new LinearLayoutManager(this, OrientationHelper.HORIZONTAL, false));
        mCatalogAdapter = new CatalogAdapter(this, mCatalogList);
        mCatalogRecyclerview.setAdapter(mCatalogAdapter);

        mRecyclerview.setLayoutManager(new LinearLayoutManager(this));
        mIncreaseTemporaryGroupMemberAdapter= new IncreaseTemporaryGroupMemberAdapter(this, mDatas);
        mRecyclerview.setAdapter(mIncreaseTemporaryGroupMemberAdapter);

        type = getIntent().getIntExtra("type", 1);
        //titlebar初始化
        if(type ==CREATE_TEMP_GROUP){
            barTitle.setText("创建临时组");
            okBtn.setText("下一步");
        }else if(type ==INCREASE_MEMBER){
            barTitle.setText("添加组员");
            okBtn.setText("确定");
        }
        rightBtn.setVisibility(View.GONE);

    }

    @Override
    public void initListener() {
        MyTerminalFactory.getSDK().registReceiveHandler(receiveUpdatePhoneMemberHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveUpdateConfigHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiverSelectTempGroupMemberHandler);
        mCatalogAdapter.setOnItemClick((view, position) -> {
            MemberResponse memberResponse = mCatalogList.get(position).getBean();

            List<CatalogBean> catalogList = new ArrayList<>();
            catalogList.addAll(mCatalogList.subList(0, position + 1));
            updateData(memberResponse,catalogList);
        });

        mIncreaseTemporaryGroupMemberAdapter.setOnItemClickListener((view, postion, itemType) -> {
            if (itemType == Constants.TYPE_DEPARTMENT) {
                MemberResponse memberResponse = (MemberResponse) mDatas.get(postion).getBean();
                if (memberResponse != null) {
                    CatalogBean catalog = new CatalogBean();
                    catalog.setName(memberResponse.getName());
                    catalog.setBean(memberResponse);
                    mCatalogList.add(catalog);
                    List<CatalogBean> catalogBeanList = new ArrayList<>();
                    catalogBeanList.addAll(mCatalogList);
                    updateData(memberResponse, catalogBeanList);
                }
            } else if(itemType == Constants.TYPE_USER){

                if(!mIncreaseTemporaryGroupMemberAdapter.getSelectMember().isEmpty()){
                    okBtn.setText("确定(" + mIncreaseTemporaryGroupMemberAdapter.getSelectMember().size() + ")");
                    okBtn.setBackgroundResource(R.drawable.live_theme_confirm_bg);
                    ll_select_member.setVisibility(View.VISIBLE);
                }else {
                    if(type ==CREATE_TEMP_GROUP){
                        okBtn.setText("下一步");
                    }else {
                        okBtn.setText("确定");
                    }
                    okBtn.setBackgroundResource(R.drawable.live_theme_confirm_bg);
                    ll_select_member.setVisibility(View.GONE);
                }


                StringBuffer sb = new StringBuffer();
                Log.e("OnInvitaListViewItemCli", "selectItem:" + mIncreaseTemporaryGroupMemberAdapter.getSelectMember());
                for (Member m : mIncreaseTemporaryGroupMemberAdapter.getSelectMember()) {
                    sb.append(m.getName() + "  ");
                }
                tv_checktext.setText(sb);
                //获取textview宽度
                TextPaint textPaint = new TextPaint();
                textPaint = tv_checktext.getPaint();
                float textPaintWidth = textPaint.measureText(sb.toString());

                if (textPaintWidth >= screenWidth - (screenWidth / 4)) {
                    horizonMenu.setLayoutParams(new LinearLayout.LayoutParams(screenWidth - (screenWidth / 4), ViewGroup.LayoutParams.WRAP_CONTENT));
                    logger.info("textView的宽度达到了屏幕的五分之四");
                } else {
                    horizonMenu.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                }
                //滚动到最右边
                horizonMenu.post(() -> horizonMenu.fullScroll(HorizontalScrollView.FOCUS_RIGHT));
            }
        });

        swipeRefreshLayout.setOnRefreshListener(() -> {
            TerminalFactory.getSDK().getConfigManager().updataPhoneMemberInfo();
            myHandler.postDelayed(() -> {
                // 加载完数据设置为不刷新状态，将下拉进度收起来
                swipeRefreshLayout.setRefreshing(false);

            }, 1200);
        });
    }

    @Override
    public void initData() {

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
        mIncreaseTemporaryGroupMemberAdapter.notifyDataSetChanged();
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

        if(null != memberList && !memberList.isEmpty()){
            List<ContactItemBean> itemMemberList = new ArrayList<>();
            Iterator<Member> iterator = memberList.iterator();
            while(iterator.hasNext()){
                //不要直接使用MemberResponse里的数据，会一直保存在内存里,下次进来时还是选中状态
                Member member = (Member) iterator.next().clone();
                for(Member selectMember : mIncreaseTemporaryGroupMemberAdapter.getSelectMember()){
                    if(selectMember.getNo() == member.getNo()){
                        member.setChecked(true);
                        break;
                    }
                }
                //过滤掉自己
                if(member.getNo() == MyTerminalFactory.getSDK().getParam(Params.MEMBER_ID,0)){
                    iterator.remove();
                    continue;
                }
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
//                Collections.sort(mDatas);
            }
        }
    }

    @Override
    public void doOtherDestroy() {
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveUpdatePhoneMemberHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveUpdateConfigHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiverSelectTempGroupMemberHandler);
        mIncreaseTemporaryGroupMemberAdapter.getSelectMember().clear();
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
            this.finish();
        }
    }
    private long lastSearchTime=0;

    @OnClick({R.id.news_bar_back,R.id.iv_search,R.id.ok_btn})
    public void onClick(View view) {
        long currentTime = System.currentTimeMillis();
        if(currentTime - lastSearchTime<1000){
            return;
        }
        lastSearchTime= currentTime;
        switch (view.getId()) {
            case R.id.news_bar_back:
                finish();
                break;
            case R.id.iv_search:
                TempGroupSearchFragment tempGroupSearchFragment = new TempGroupSearchFragment();
                tempGroupSearchFragment.setGroupMember(DataUtil.getAllMembersExceptMe(MyTerminalFactory.getSDK().getConfigManager().getPhoneMembers()));
                tempGroupSearchFragment.setInterGroup(true);
                getSupportFragmentManager().beginTransaction().addToBackStack(null).replace(R.id.fl_fragment_container_temp_group_member, tempGroupSearchFragment).commit();
                break;
            case R.id.ok_btn:
                if(mIncreaseTemporaryGroupMemberAdapter.getSelectMember().isEmpty()){
                    ToastUtil.showToast(IncreaseTemporaryGroupMemberActivity.this,"至少添加一名成员");
                    return;
                }
                if(type ==CREATE_TEMP_GROUP){
                    ArrayList<Integer> list=new ArrayList<>();
                    list.addAll(mIncreaseTemporaryGroupMemberAdapter.getSelectItem());
                    Intent intent = new Intent(IncreaseTemporaryGroupMemberActivity.this, CreateTemporaryGroupsActivity.class);
                    intent.putIntegerArrayListExtra("list",list);
                    startActivity(intent);
                }else if(type ==INCREASE_MEMBER){
                    //添加组员
                    MyTerminalFactory.getSDK().getTempGroupManager().addMemberToTempGroup(MyTerminalFactory.getSDK().getParam(Params.CURRENT_GROUP_ID,0),MyTerminalFactory.getSDK().getParam(Params.MEMBER_ID,0),mIncreaseTemporaryGroupMemberAdapter.getSelectMember());
                    finish();
                }
                break;
        }
    }
}
