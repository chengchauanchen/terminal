package cn.vsx.vc.search;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.allen.library.observer.CommonObserver;

import java.util.ArrayList;
import java.util.List;

import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.manager.search.GroupSearchBean;
import cn.vsx.hamster.terminalsdk.manager.search.MemberSearchBean;
import cn.vsx.hamster.terminalsdk.model.Account;
import cn.vsx.hamster.terminalsdk.model.Group;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveGetAllAccountHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveGetAllGroupHandler;
import cn.vsx.vc.R;
import cn.vsx.vc.fragment.BaseFragment;
import cn.vsx.vc.search.SearchKeyboardView.OnT9TelephoneDialpadView;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * 通讯录 搜索
 */
public class SearchTabFragment extends BaseFragment{

    private List<GroupSearchBean> groupDatas;
    private List<MemberSearchBean> memberDatas;
    private List<GroupSearchBean> ListenedGroupDatas;
    private TextView phone;
    private SearchKeyboardView search_keyboard;
    private RecyclerView group_recyclerView;
    private SearchAdapter searchAdapter;

    private boolean searchKeyboardIsVisible = false;

    private List<Object> datas = new ArrayList<>();

    @Override
    public int getContentViewId(){
        return R.layout.fragment_search;
    }

    @Override
    public void initView(){
        phone = mRootView.findViewById(R.id.phone);
        search_keyboard = mRootView.findViewById(R.id.search_keyboard);
        //打开自定义键盘
        phone.setOnClickListener(v -> {
            search_keyboard.setVisibility(searchKeyboardIsVisible?View.GONE:View.VISIBLE);
            searchKeyboardIsVisible = !searchKeyboardIsVisible;
        });

        search_keyboard.setOnT9TelephoneDialpadView(new OnT9TelephoneDialpadView() {

            @Override
            public void onDeleteDialCharacter(String deleteCharacter) {
                logger.info("SearchTabFragment:"+deleteCharacter);
            }

            @Override
            public void onDialInputTextChanged(String curCharacter) {
                phone.setText(curCharacter);
//                searchGroup(curCharacter);
                searchAll(curCharacter);
            }
        });
        initRecyclerView();
    }

    private void initRecyclerView() {
        group_recyclerView = mRootView.findViewById(R.id.group_recyclerView);
        group_recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        searchAdapter = new SearchAdapter(getContext(),datas);
        group_recyclerView.setAdapter(searchAdapter);
    }


    @Override
    public void initData(){
        groupDatas = new ArrayList<>();
        memberDatas = new ArrayList<>();
        TerminalFactory.getSDK().getConfigManager().getGroupsAll(false,0L);
        TerminalFactory.getSDK().getConfigManager().getDeptAllData(false,0L);

        getDbAllGroup();
        getDbAllAccount();
    }

    @Override
    public void initListener(){
        TerminalFactory.getSDK().registReceiveHandler(receiveGetAllGroupHandler);
//        TerminalFactory.getSDK().registReceiveHandler(receiveGetAllAccountHandler);
    }

    @Override
    public void onDestroyView(){
        super.onDestroyView();
        TerminalFactory.getSDK().unregistReceiveHandler(receiveGetAllGroupHandler);
//        TerminalFactory.getSDK().unregistReceiveHandler(receiveGetAllAccountHandler);
    }

    //所有组
    private ReceiveGetAllGroupHandler receiveGetAllGroupHandler = new ReceiveGetAllGroupHandler() {
        @Override
        public void handler(List<Group> groups) {
            logger.info("SearchTabFragment获取组数据:"+groups);
            getListenedGroup();
//            groupDatas = groups;
        }
    };

    //所有人
    ReceiveGetAllAccountHandler receiveGetAllAccountHandler = new ReceiveGetAllAccountHandler() {
        @Override
        public void handler(List<Account> accounts) {
            logger.info("SearchTabFragment获取人数据:"+accounts);
//            memberDatas = accounts;
        }
    };

    /******************************数据处理*********************************/






    /**
     * 搜索组 + 人 一起干
     * @param curCharacter
     */
    private void searchAll(String curCharacter){

        Observable.zip(SearchUtil.searchObservable(curCharacter, groupDatas),
                SearchUtil.searchMemberObservable(curCharacter, memberDatas),
                (groupSearchBeans, memberSearchBeans) -> {
                    datas.clear();
                    if(groupSearchBeans!=null && groupSearchBeans.size()>0){
                        SearchTitleBean titleBean = new SearchTitleBean("组");
                        datas.add(titleBean);
                        datas.addAll(groupSearchBeans);
                    }

                    if(memberSearchBeans!=null && memberSearchBeans.size()>0){
                        SearchTitleBean titleBean2 = new SearchTitleBean("工作人员");
                        datas.add(titleBean2);
                        datas.addAll(memberSearchBeans);
                    }
                    return datas;
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CommonObserver<List<Object>>() {
                    @Override
                    protected String setTag() {
                        return "";
                    }

                    @Override
                    protected void onError(String errorMsg) {
                        logger.error("searchAll----请求报错:" + errorMsg);
                    }

                    @Override
                    protected void onSuccess(List<Object> allRowSize) {
                        logger.info(allRowSize);
                        searchAdapter.notifyDataSetChanged();
                    }
                });
    }

    /**
     * 获取本地数据库 组数据
     */
    private void getDbAllGroup(){
        SearchUtil.getDbAllGroup()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CommonObserver<List<GroupSearchBean>>() {
                    @Override
                    protected String setTag() {
                        return "";
                    }

                    @Override
                    protected void onError(String errorMsg) {
                        logger.error("getTotalCountPolice----请求报错:" + errorMsg);
                    }

                    @Override
                    protected void onSuccess(List<GroupSearchBean> allRowSize) {
                        logger.info(allRowSize);
                        groupDatas = allRowSize;
                    }
                });
    }

    /**
     * 获取本地数据库 人数据
     */
    private void getDbAllAccount(){
        SearchUtil.getDbAllAccount()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CommonObserver<List<MemberSearchBean>>() {
                    @Override
                    protected String setTag() {
                        return "";
                    }

                    @Override
                    protected void onError(String errorMsg) {
                        logger.error("getTotalCountPolice----请求报错:" + errorMsg);
                    }

                    @Override
                    protected void onSuccess(List<MemberSearchBean> allRowSize) {
                        logger.info(allRowSize);
                        memberDatas = allRowSize;
                    }
                });
    }

    private void getListenedGroup(){
        SearchUtil.getListenedGroup()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CommonObserver<List<GroupSearchBean>>(){
                    @Override
                    protected void onError(String errorMsg){
                    }

                    @Override
                    protected void onSuccess(List<GroupSearchBean> groupSearchBeans){
                        logger.info("监听组："+groupSearchBeans);
                        ListenedGroupDatas = groupSearchBeans;
                        datas.clear();
                        if(ListenedGroupDatas!=null && ListenedGroupDatas.size()>0){
                            SearchTitleBean titleBean = new SearchTitleBean("监听组");
                            datas.add(titleBean);
                            datas.addAll(ListenedGroupDatas);
                            searchAdapter.notifyDataSetChanged();
                        }
                    }
                });
    }

}
