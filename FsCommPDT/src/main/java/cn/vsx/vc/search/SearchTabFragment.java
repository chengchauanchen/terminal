package cn.vsx.vc.search;

import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.allen.library.observer.CommonObserver;
import com.zectec.imageandfileselector.utils.OperateReceiveHandlerUtilSync;

import java.util.ArrayList;
import java.util.List;

import cn.vsx.hamster.common.Authority;
import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.manager.search.GroupSearchBean;
import cn.vsx.hamster.terminalsdk.manager.search.MemberSearchBean;
import cn.vsx.hamster.terminalsdk.model.Account;
import cn.vsx.hamster.terminalsdk.model.Group;
import cn.vsx.hamster.terminalsdk.model.Member;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveCurrentGroupIndividualCallHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveGetAllAccountHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveGetAllGroupHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveSetMonitorGroupViewHandler;
import cn.vsx.hamster.terminalsdk.tools.DataUtil;
import cn.vsx.hamster.terminalsdk.tools.Params;
import cn.vsx.vc.R;
import cn.vsx.vc.application.MyApplication;
import cn.vsx.vc.dialog.ChooseDevicesDialog;
import cn.vsx.vc.jump.utils.MemberUtil;
import cn.vsx.vc.search.SearchKeyboardView.OnT9TelephoneDialpadView;
import cn.vsx.vc.utils.CommonGroupUtil;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import ptt.terminalsdk.context.MyTerminalFactory;
import ptt.terminalsdk.tools.ToastUtil;

/**
 * 通讯录 搜索
 */
public class SearchTabFragment extends BaseSearchFragment {

    private List<GroupSearchBean> groupDatas;
    private List<MemberSearchBean> memberDatas;
    private List<GroupSearchBean> ListenedGroupDatas;
    private TextView phone;
    private SearchKeyboardView search_keyboard;
    private RecyclerView group_recyclerView;

    private boolean searchKeyboardIsVisible = true;
    private ImageView iv_call;

    @Override
    public int getContentViewId() {
        return R.layout.fragment_search;
    }

    @Override
    public void initView() {
        phone = mRootView.findViewById(R.id.phone);
        iv_call = mRootView.findViewById(R.id.iv_call);
        search_keyboard = mRootView.findViewById(R.id.search_keyboard);
        //打开自定义键盘
        phone.setOnClickListener(v -> {
            search_keyboard.setVisibility(searchKeyboardIsVisible ? View.GONE : View.VISIBLE);
            searchKeyboardIsVisible = !searchKeyboardIsVisible;
        });

        search_keyboard.setOnT9TelephoneDialpadView(new OnT9TelephoneDialpadView() {

            @Override
            public void onDeleteDialCharacter(String deleteCharacter) {
                logger.info("SearchTabFragment:" + deleteCharacter);
            }

            @Override
            public void onDialInputTextChanged(String curCharacter) {
                if (TextUtils.isEmpty(curCharacter)) {
                    iv_call.setImageResource(R.drawable.search_call_hui_ic);
                } else {
                    iv_call.setImageResource(R.drawable.search_call_ic);
                }
                phone.setText(curCharacter);
                if (!TextUtils.isEmpty(curCharacter)) {
                    searchAll(curCharacter);
                } else {
                    disposable();
                    datas.clear();
                    getListenedGroup();
                }
            }
        });

        iv_call.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String s = phone.getText().toString();
                if (TextUtils.isEmpty(s)) {
                    return;
                }
                getAccount(s);
            }
        });

        initRecyclerView();
    }

    private void initRecyclerView() {
        group_recyclerView = mRootView.findViewById(R.id.group_recyclerView);
        group_recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        searchAdapter = new SearchAdapter(getContext(), datas);
        group_recyclerView.setAdapter(searchAdapter);
    }


    @Override
    public void initData() {
        groupDatas = new ArrayList<>();
        memberDatas = new ArrayList<>();
        getDbAllGroup();
        getDbAllAccount();
        getListenedGroup();
    }

    @Override
    public void initListener() {
        TerminalFactory.getSDK().registReceiveHandler(receiveGetAllGroupHandler);
        TerminalFactory.getSDK().registReceiveHandler(receiveGetAllAccountHandler);
        TerminalFactory.getSDK().registReceiveHandler(receiveSetMonitorGroupViewHandler);

        /*---------------------------*/
        registReceiveHandler();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        TerminalFactory.getSDK().unregistReceiveHandler(receiveGetAllGroupHandler);
        TerminalFactory.getSDK().unregistReceiveHandler(receiveGetAllAccountHandler);
        TerminalFactory.getSDK().unregistReceiveHandler(receiveSetMonitorGroupViewHandler);

        /*---------------------------*/
        unregistReceiveHandler();
    }

    //所有组
    private ReceiveGetAllGroupHandler receiveGetAllGroupHandler = new ReceiveGetAllGroupHandler() {
        @Override
        public void handler(List<Group> groups) {
            logger.info("SearchTabFragment获取组数据:" + groups);
            getDbAllGroup();
            getListenedGroup();
//            groupDatas = groups;
        }
    };

    //所有人
    ReceiveGetAllAccountHandler receiveGetAllAccountHandler = new ReceiveGetAllAccountHandler() {
        @Override
        public void handler(List<Account> accounts) {
            logger.info("SearchTabFragment获取人数据:" + accounts);
            getDbAllAccount();
        }
    };

    /**
     * 监听变动后，重新获取监听组
     */
    ReceiveSetMonitorGroupViewHandler receiveSetMonitorGroupViewHandler = new ReceiveSetMonitorGroupViewHandler() {
        @Override
        public void handler() {
            getListenedGroup();
        }
    };


    //切组成功后，更新监听组
    @Override
    public void changeGroupSuccess() {
        super.changeGroupSuccess();
        getListenedGroup();
    }

    private Disposable disposable;

    private void disposable() {
        //取消订阅
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }
    }


    /******************************数据处理*********************************/

    /**
     * 搜索组 + 人 一起干
     *
     * @param curCharacter
     */
    private void searchAll(String curCharacter) {
        Observable.zip(SearchUtil.searchObservable(curCharacter, groupDatas),
                SearchUtil.searchMemberObservable(curCharacter, memberDatas),
                (groupSearchBeans, memberSearchBeans) -> {
                    datas.clear();
                    if (groupSearchBeans != null && groupSearchBeans.size() > 0) {
                        ListenedGroupDatas = null;

                        SearchTitleBean titleBean = new SearchTitleBean("组");
                        datas.add(titleBean);
                        datas.addAll(groupSearchBeans);
                    }

                    if (memberSearchBeans != null && memberSearchBeans.size() > 0) {
                        ListenedGroupDatas = null;
                        SearchTitleBean titleBean2 = new SearchTitleBean("工作人员");
                        datas.add(titleBean2);
                        datas.addAll(memberSearchBeans);
                    }
                    return datas;
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CommonObserver<List<Object>>() {
                    @Override
                    public void doOnSubscribe(Disposable d) {
                        super.doOnSubscribe(d);
                        disposable = d;
                    }

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
    private void getDbAllGroup() {
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
    private void getDbAllAccount() {
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

    private void getListenedGroup() {
        //当前没有监听组的数据，说明没有显示监听组，或显示的是搜索的结果,则不需要更新监听组
        if (datas.size() > 0) {
            Object o = datas.get(0);
            if (o instanceof SearchTitleBean) {
                SearchTitleBean searchTitleBean = (SearchTitleBean) o;
                if (!"监听组".equals(searchTitleBean.getTitle())) {
                    return;
                }
            }
        }
        SearchUtil.getMonitorGroupList()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CommonObserver<List<GroupSearchBean>>() {
                    @Override
                    protected void onError(String errorMsg) {
                    }

                    @Override
                    protected void onSuccess(List<GroupSearchBean> groupSearchBeans) {
//                        for (GroupSearchBean group : groupSearchBeans) {
//                            if (group.getNo() != TerminalFactory.getSDK().getParam(Params.CURRENT_GROUP_ID, 0)
//                                    && !TerminalFactory.getSDK().getConfigManager().getMonitorGroupNo().contains(group.getNo())) {
//                                TerminalFactory.getSDK().getConfigManager().getTempMonitorGroupNos().add(group.getNo());
//                            }
//                            TerminalFactory.getSDK().getConfigManager().updateTempMonitorGroup();
//                        }
                        logger.info("监听组：" + groupSearchBeans);
                        ListenedGroupDatas = groupSearchBeans;
                        datas.clear();
                        if (ListenedGroupDatas != null) {
                            SearchTitleBean titleBean = new SearchTitleBean("监听组");
                            datas.add(titleBean);
                            datas.addAll(ListenedGroupDatas);
                            searchAdapter.notifyDataSetChanged();
                        }
                    }
                });
    }

    private void getAccount(String memberNo) {
        //根据警号找 Account
        TerminalFactory.getSDK().getThreadPool().execute(() -> {
            Account account = DataUtil.getAccountByMemberNo(MemberUtil.checkMemberNo(memberNo), true);
            if (account == null) {
                ToastUtil.showToast(context, "号码异常");
                return;
            }
            indivudualCall(account);
        });
    }

    private Handler myHandler = new Handler();

    private void indivudualCall(Account account) {
        if (!MyTerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_CALL_PRIVATE.name())) {
            ToastUtil.showToast(getContext(), getContext().getString(R.string.text_no_call_permission));
        } else {
            myHandler.post(new Runnable() {
                @Override
                public void run() {
                    new ChooseDevicesDialog(getContext(), ChooseDevicesDialog.TYPE_CALL_PRIVATE, account, (dialog, member) -> {
                        activeIndividualCall(member);
                        dialog.dismiss();
                    }).showDialog();
                }
            });
        }
    }

    private void activeIndividualCall(Member member) {
        MyApplication.instance.isCallState = true;
        boolean network = MyTerminalFactory.getSDK().hasNetwork();
        if (network) {
            OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiveCurrentGroupIndividualCallHandler.class, member);
        } else {
            ToastUtil.showToast(getContext(), getContext().getString(R.string.text_network_connection_abnormal_please_check_the_network));
        }
    }


}
