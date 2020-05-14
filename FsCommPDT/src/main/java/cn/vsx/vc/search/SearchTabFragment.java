package cn.vsx.vc.search;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
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
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveForceChangeGroupHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveGetAllGroupHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveSetMonitorGroupViewHandler;
import cn.vsx.hamster.terminalsdk.tools.DataUtil;
import cn.vsx.vc.R;
import cn.vsx.vc.application.MyApplication;
import cn.vsx.vc.dialog.ChooseDevicesDialog;
import cn.vsx.vc.jump.utils.MemberUtil;
import cn.vsx.vc.search.SearchKeyboardView.HideOnClick;
import cn.vsx.vc.search.SearchKeyboardView.OnT9TelephoneDialpadView;
import cn.vsx.vc.view.RecyclerViewNoBugLinearLayoutManager;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import ptt.terminalsdk.bean.SearchTitleBean;
import ptt.terminalsdk.context.MyTerminalFactory;
import ptt.terminalsdk.manager.search.SearchUtil;
import ptt.terminalsdk.receiveHandler.ReceiverUpdateTopContactsHandler;
import ptt.terminalsdk.tools.StringUtil;
import ptt.terminalsdk.tools.ToastUtil;

/**
 * 通讯录 搜索
 */
public class SearchTabFragment extends BaseSearchFragment {

    private List<GroupSearchBean> listenedGroupDatas = new ArrayList<>();
    private List<MemberSearchBean> topContactsDatas = new ArrayList<>();
    private TextView phone;
    private SearchKeyboardView search_keyboard;
    private RecyclerView group_recyclerView;
    private ImageView iv_hint_search;
    private static final int HANDLE_CODE_LOAD_ALL_DATA = 1  ;//获取所有数据
    private static final int HANDLE_CODE_LOAD_LISTENER_DATA = 2  ;//获取监听组数据
    private static final int HANDLE_CODE_LOAD_TOP_DATA = 3  ;//获取常用联系人数据
    private static final int HANDLE_CODE_UPDATE_UI = 4  ;//更新UI
    private static final int UPDATE_DELAYED_TIME = 500  ;//延时的时间
    private boolean searchKeyboardIsVisible = true;
    private ImageView iv_call;

    private Handler mHandler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case HANDLE_CODE_LOAD_ALL_DATA:
                    //获取所有数据
                    removeMessages(HANDLE_CODE_LOAD_ALL_DATA);
                    getListenedGroupAndTop5Contacts();
                    break;
                case HANDLE_CODE_LOAD_LISTENER_DATA:
                    //获取监听组数据
                    removeMessages(HANDLE_CODE_LOAD_LISTENER_DATA);
                    getListenerGroupList();
                    break;
                case HANDLE_CODE_LOAD_TOP_DATA:
                    //获取常用联系人数据
                    removeMessages(HANDLE_CODE_LOAD_TOP_DATA);
                    getTopContactsList();
                    break;
                case HANDLE_CODE_UPDATE_UI:
                    //更新UI
                    removeMessages(HANDLE_CODE_UPDATE_UI);
                    updateUI();
                    break;
                default:break;
            }
        }
    };

    @Override
    public int getContentViewId() {
        return R.layout.fragment_search;
    }

    @Override
    public void initView() {
        phone = mRootView.findViewById(R.id.phone);
        iv_call = mRootView.findViewById(R.id.iv_call);
        iv_hint_search = mRootView.findViewById(R.id.iv_hint_search);
        search_keyboard = mRootView.findViewById(R.id.search_keyboard);
        //打开自定义键盘
        phone.setOnClickListener(v -> {
            showOrHintKeyboard();
        });

        //打开自定义键盘
        iv_hint_search.setOnClickListener(v -> {
            showOrHintKeyboard();
        });

        search_keyboard.setOnHideOnClick(new HideOnClick() {
            @Override
            public void onClick() {
                showOrHintKeyboard();
            }
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
                    //只更新UI，不重新获取
                    datas.clear();
                    mHandler.sendEmptyMessageDelayed(HANDLE_CODE_UPDATE_UI,UPDATE_DELAYED_TIME);
                }
            }
        });

        iv_call.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String s = phone.getText().toString();
                //效验拨打账号的限制（只能输入1~8位长度的数字，输入的编号不能全是0）
                if(checkCallData(s)){
                    getAccount(s);
                }
            }
        });
        initRecyclerView();
    }

    /**
     * 效验（只能输入1~8位长度的数字，输入的编号不能全是0）
     * @param s
     */
    private boolean checkCallData(String s) {
        if (TextUtils.isEmpty(s)) {
            return false;
        }

        if(s.length()<=0||s.length()>8){
            ToastUtil.showToast(getString(R.string.text_call_length_error));
            return false;
        }

        if(StringUtil.stringToInt(s) == 0){
            ToastUtil.showToast(getString(R.string.text_call_zero_error));
            return false;
        }
        return true;
    }

    private void initRecyclerView() {
        group_recyclerView = mRootView.findViewById(R.id.group_recyclerView);
        group_recyclerView.setLayoutManager(new RecyclerViewNoBugLinearLayoutManager(getContext()));
        searchAdapter = new SearchAdapter(getContext(), datas);
        group_recyclerView.setAdapter(searchAdapter);
    }


    @Override
    public void initData() {
        MyTerminalFactory.getSDK().getSearchDataManager().getDbAllGroup();
        MyTerminalFactory.getSDK().getSearchDataManager().getDbAllAccount();
        mHandler.sendEmptyMessage(HANDLE_CODE_LOAD_ALL_DATA);
    }

    @Override
    public void initListener() {
        TerminalFactory.getSDK().registReceiveHandler(receiveGetAllGroupHandler);
        TerminalFactory.getSDK().registReceiveHandler(receiveSetMonitorGroupViewHandler);
        TerminalFactory.getSDK().registReceiveHandler(receiverUpdateTopContactsHandler);//常用联系人
        TerminalFactory.getSDK().registReceiveHandler(receiveForceChangeGroupHandler);//强制切组
        /*---------------------------*/
        registReceiveHandler();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mHandler.removeCallbacksAndMessages(null);
        TerminalFactory.getSDK().unregistReceiveHandler(receiveGetAllGroupHandler);
        TerminalFactory.getSDK().unregistReceiveHandler(receiveSetMonitorGroupViewHandler);
        TerminalFactory.getSDK().unregistReceiveHandler(receiverUpdateTopContactsHandler);//常用联系人
        TerminalFactory.getSDK().unregistReceiveHandler(receiveForceChangeGroupHandler);//强制切组
        /*---------------------------*/
        unregistReceiveHandler();
    }

    /**
     * 点击按钮显示或隐藏键盘
     */
    private void showOrHintKeyboard(){
        search_keyboard.setVisibility(searchKeyboardIsVisible ? View.GONE : View.VISIBLE);
        iv_hint_search.setVisibility(searchKeyboardIsVisible?View.VISIBLE:View.INVISIBLE);
        searchKeyboardIsVisible = !searchKeyboardIsVisible;
    }

    //所有组
    private ReceiveGetAllGroupHandler receiveGetAllGroupHandler = new ReceiveGetAllGroupHandler() {
        @Override
        public void handler(List<Group> groups) {
            mHandler.sendEmptyMessageDelayed(HANDLE_CODE_LOAD_LISTENER_DATA,UPDATE_DELAYED_TIME);
        }
    };

    /**
     * 监听变动后，重新获取监听组
     */
    ReceiveSetMonitorGroupViewHandler receiveSetMonitorGroupViewHandler = new ReceiveSetMonitorGroupViewHandler() {
        @Override
        public void handler() {
            mHandler.sendEmptyMessageDelayed(HANDLE_CODE_LOAD_LISTENER_DATA,UPDATE_DELAYED_TIME);
        }
    };

    /**
     * 收到強制转组
     */
    private ReceiveForceChangeGroupHandler receiveForceChangeGroupHandler = new ReceiveForceChangeGroupHandler() {
        @Override
        public void handler(int memberId, int toGroupId, boolean forceSwitchGroup, String tempGroupType) {
            if (!forceSwitchGroup) {
                return;
            }
            mHandler.sendEmptyMessageDelayed(HANDLE_CODE_LOAD_LISTENER_DATA,UPDATE_DELAYED_TIME);
        }
    };

    /**
     * 常用联系人 更新通知
     */
    private ReceiverUpdateTopContactsHandler receiverUpdateTopContactsHandler = new ReceiverUpdateTopContactsHandler() {
        @Override
        public void handler() {
            //更新常用联系人，准确来说，只需更新常用联系人，不需要更新监听组，先这样实现吧
            mHandler.sendEmptyMessageDelayed(HANDLE_CODE_LOAD_TOP_DATA,UPDATE_DELAYED_TIME);
        }
    };

    //切组成功后，更新监听组
    @Override
    public void changeGroupSuccess() {
        super.changeGroupSuccess();
        mHandler.sendEmptyMessageDelayed(HANDLE_CODE_LOAD_LISTENER_DATA,UPDATE_DELAYED_TIME);
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
        Observable.zip(SearchUtil.searchObservable(curCharacter, MyTerminalFactory.getSDK().getSearchDataManager().getGroupSreachDatas()),
                SearchUtil.searchMemberObservable(curCharacter, MyTerminalFactory.getSDK().getSearchDataManager().getAccountSreachDatas()),
                (groupSearchBeans, memberSearchBeans) -> {
                    datas.clear();
                    if (groupSearchBeans != null && groupSearchBeans.size() > 0) {
                        SearchTitleBean titleBean = new SearchTitleBean("组");
                        datas.add(titleBean);
                        datas.addAll(groupSearchBeans);
                    }

                    if (memberSearchBeans != null && memberSearchBeans.size() > 0) {
                        SearchTitleBean titleBean2 = new SearchTitleBean("工作人员");
                        datas.add(titleBean2);
                        datas.addAll(memberSearchBeans);
                    }
                    return datas;
                }).subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(new CommonObserver<List<Object>>() {

                    @Override protected boolean isHideToast() {
                        return true;
                    }
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
                        mHandler.post(() -> {
                            searchAdapter.notifyDataSetChanged();
                        });
                    }
                });
    }

    /**
     * 默认获取 监听组或常用联系人
     */
    private void getListenedGroupAndTop5Contacts(){
        Observable.zip(SearchUtil.getMonitorGroupList(), SearchUtil.getTop5ContactsAccount(),
                (groupSearchBeans, memberSearchBeans) -> {
                    logger.error("监听组----:" + groupSearchBeans +"用联系人----:"+memberSearchBeans);
                    listenedGroupDatas.clear();
                    if (groupSearchBeans != null && groupSearchBeans.size() > 0) {
                        //ListenedGroupDatas = null;
                        listenedGroupDatas.addAll(groupSearchBeans);
                    }
                    topContactsDatas.clear();
                    if (memberSearchBeans != null && memberSearchBeans.size() > 0) {
                        //ListenedGroupDatas = null;
                        topContactsDatas.addAll(memberSearchBeans);
                    }
                    return new ArrayList<Object>();
                }).subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
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
                        mHandler.sendEmptyMessageDelayed(HANDLE_CODE_UPDATE_UI,UPDATE_DELAYED_TIME);
                    }
                });
    }

    /**
     * 获取监听组的数据
     */
    private void getListenerGroupList(){
        SearchUtil.getMonitorGroupList()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(new CommonObserver<List<GroupSearchBean>>() {
                    @Override
                    protected void onError(String errorMsg) {
                    }

                    @Override
                    protected void onSuccess(List<GroupSearchBean> groupSearchBeans) {
                        logger.info("监听组：" + groupSearchBeans);
                        if(groupSearchBeans!=null){
                            listenedGroupDatas.clear();
                            listenedGroupDatas.addAll(groupSearchBeans);
                        }
                        mHandler.sendEmptyMessageDelayed(HANDLE_CODE_UPDATE_UI,UPDATE_DELAYED_TIME);
                    }
                });
    }

    /**
     * 获取常用联系人的数据
     */
    private void getTopContactsList(){
        SearchUtil.getTop5ContactsAccount()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(new CommonObserver<List<MemberSearchBean>>() {
                    @Override
                    protected void onError(String errorMsg) {
                    }

                    @Override
                    protected void onSuccess(List<MemberSearchBean> memberSearchBean) {
                        logger.info("常用联系人：" + memberSearchBean);
                        if(memberSearchBean!=null){
                            topContactsDatas.clear();
                            topContactsDatas.addAll(memberSearchBean);
                        }
                        mHandler.sendEmptyMessageDelayed(HANDLE_CODE_UPDATE_UI,UPDATE_DELAYED_TIME);
                    }
                });
    }

    /**
     * 更新UI
     */
    private void updateUI() {
        try{
            TerminalFactory.getSDK().getThreadPool().execute(() -> {
                //当前没有监听组的数据，说明没有显示监听组，或显示的是搜索的结果,则不需要更新监听组
                if (datas.size() > 0) {
                    Object o = datas.get(0);
                    if (o instanceof SearchTitleBean) {
                        SearchTitleBean searchTitleBean = (SearchTitleBean) o;
                        if (!TextUtils.equals("监听组",searchTitleBean.getTitle())&&
                                !TextUtils.equals("常用联系人",searchTitleBean.getTitle())) {
                            mHandler.post(() -> {
                                if(searchAdapter!=null){
                                    searchAdapter.notifyDataSetChanged();
                                }
                            });
                            return;
                        }
                    }
                }
                datas.clear();
                //添加监听组
                SearchTitleBean titleBean = new SearchTitleBean("监听组");
                datas.add(titleBean);
                //添加当前组
                GroupSearchBean currentGroup = SearchUtil.getCurrentGroupInfo();
                if(currentGroup!=null&&!listenedGroupDatas.contains(currentGroup)){
                    listenedGroupDatas.add(currentGroup);
                }
                if(listenedGroupDatas.size()>0){
                    datas.addAll(listenedGroupDatas);
                }
                //常用联系人
                if(topContactsDatas.size()>0){
                    SearchTitleBean titleBean2 = new SearchTitleBean("常用联系人");
                    datas.add(titleBean2);
                    datas.addAll(topContactsDatas);
                }

                mHandler.post(() -> {
                    if(searchAdapter!=null){
                        searchAdapter.notifyDataSetChanged();
                    }
                });
            });

        }catch (Exception e){
            e.printStackTrace();
        }

    }

    private void getAccount(String memberNo) {
        //根据警号找 Account
        TerminalFactory.getSDK().getThreadPool().execute(() -> {
            Account account = DataUtil.getAccountByMemberNo(MemberUtil.checkMemberNo(memberNo), true);
            indivudualCall(account,memberNo);
        });
    }


    private void indivudualCall(Account account,String memberNo) {
        if (!MyTerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_CALL_PRIVATE.name())) {
            ToastUtil.showToast(getContext(), getContext().getString(R.string.text_no_call_permission));
        } else {
            mHandler.post(() -> {
                if (account != null) {
                    new ChooseDevicesDialog(getContext(), ChooseDevicesDialog.TYPE_CALL_PRIVATE, account, (dialog, member) -> {
                        activeIndividualCall(member);
                        dialog.dismiss();
                    }).showDialog();
                }else{
                    //直接拨打
                    Member member = new Member();
                    member.setName(memberNo);
                    member.setNo(StringUtil.stringToInt(memberNo));
                    member.setUniqueNo(StringUtil.stringToInt(memberNo));
                    activeIndividualCall(member);
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
