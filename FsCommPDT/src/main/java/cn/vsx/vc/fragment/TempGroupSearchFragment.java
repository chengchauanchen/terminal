package cn.vsx.vc.fragment;

import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zectec.imageandfileselector.receivehandler.ReceiverIndividualCallForAddressBookHandler;
import com.zectec.imageandfileselector.receivehandler.ReceiverIndividualMsgForAddressBookHandler;
import com.zectec.imageandfileselector.utils.OperateReceiveHandlerUtilSync;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

import cn.vsx.hamster.common.MemberChangeType;
import cn.vsx.hamster.errcode.BaseCommonCode;
import cn.vsx.hamster.terminalsdk.model.Member;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveCurrentGroupIndividualCallHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveNotifyMemberChangeHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveSearchContactsHandler;
import cn.vsx.hamster.terminalsdk.tools.DataUtil;
import cn.vsx.hamster.terminalsdk.tools.Util;
import cn.vsx.vc.R;
import cn.vsx.vc.activity.IndividualNewsActivity;
import cn.vsx.vc.adapter.TempGroupSearchAdapter;
import cn.vsx.vc.application.MyApplication;
import cn.vsx.vc.receiveHandle.ReceiverCloseKeyBoardHandler;
import cn.vsx.vc.receiveHandle.ReceiverFragmentDestoryHandler;
import cn.vsx.vc.receiveHandle.ReceiverSelectTempGroupMemberHandler;
import cn.vsx.vc.utils.InputMethodUtil;
import ptt.terminalsdk.context.MyTerminalFactory;
import ptt.terminalsdk.tools.ToastUtil;

/**
 * Created by gt358 on 2017/10/11.
 */

public class TempGroupSearchFragment extends BaseFragment implements View.OnClickListener{

    LinearLayout ll_search_pop;

    LinearLayout iv_goback_contacts;

    LinearLayout iv_delete_edittext;

    Button btn_search_allcontacts;

    TextView tv_search_nothing;

    RelativeLayout rl_search_result;

    TextView tv_search_contactscount;

    TextView tv_search_notdata;

    ListView lv_search_allcontacts;

    EditText et_search_allcontacts;

    private TempGroupSearchAdapter tempGroupSearchAdapter;

    private boolean isLocal;
    private boolean isInterGroup;//是否是组内搜索
    private List<Member> groupMember = new ArrayList<>();
    private List<Member> searchMemberListExceptMe = new ArrayList<>();/**搜索到的结果集合*/
    private String keyWord;
    private int pageIndex = 0;
    private int totalPages;
    protected int visibleItemCount;
    private Logger logger = Logger.getLogger(TempGroupSearchFragment.class);

    private Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case 0:
                    if (tv_search_notdata != null)
                        tv_search_notdata.setVisibility(View.GONE);
                    break;
            }
        }
    };

    public void setGroupMember (List<Member> groupMember) {
        this.groupMember = groupMember;
        isLocal = true;
    }

    public void setInterGroup(boolean interGroup) {
        isInterGroup = interGroup;
        if(tv_search_nothing != null)
            tv_search_nothing.setText(R.string.text_search_contact);
    }
    @Override
    public int getContentViewId() {
        return R.layout.search_all_contacts;
    }

    @Override
    public void initView() {
        et_search_allcontacts = (EditText) mRootView.findViewById(R.id.et_search_allcontacts);
        lv_search_allcontacts = (ListView) mRootView.findViewById(R.id.lv_search_allcontacts);
        tv_search_notdata = (TextView) mRootView.findViewById(R.id.tv_search_notdata);
        tv_search_contactscount = (TextView) mRootView.findViewById(R.id.tv_search_contactscount);
        rl_search_result = (RelativeLayout) mRootView.findViewById(R.id.rl_search_result);
        tv_search_nothing = (TextView) mRootView.findViewById(R.id.tv_search_nothing);
        btn_search_allcontacts = (Button) mRootView.findViewById(R.id.btn_search_allcontacts);
        iv_delete_edittext = (LinearLayout) mRootView.findViewById(R.id.iv_delete_edittext);
        iv_goback_contacts = (LinearLayout) mRootView.findViewById(R.id.iv_goback_contacts);
        ll_search_pop = (LinearLayout) mRootView.findViewById(R.id.ll_search_pop);
        tv_search_nothing.setText(R.string.text_search_contact);
        mRootView.findViewById(R.id.iv_delete).setOnClickListener(this);
        rl_search_result.setVisibility(View.GONE);

        tempGroupSearchAdapter = new TempGroupSearchAdapter(context, searchMemberListExceptMe);
        lv_search_allcontacts.setAdapter(tempGroupSearchAdapter);
        btn_search_allcontacts.setBackgroundResource(R.drawable.rectangle_with_corners_shape1);
        btn_search_allcontacts.setTextColor(ContextCompat.getColor(getContext(),R.color.search_button_text_color1));
        btn_search_allcontacts.setEnabled(false);
    }

    @Override
    public void initListener() {
        lv_search_allcontacts.setOnItemClickListener((parent, view, position, id) -> {

            MyTerminalFactory.getSDK().notifyReceiveHandler(ReceiverSelectTempGroupMemberHandler.class,searchMemberListExceptMe.get(position).getNo(),true);
            et_search_allcontacts.setText("");
            InputMethodUtil.hideInputMethod(context, et_search_allcontacts);

            searchMemberListExceptMe.clear();
            tv_search_notdata.setVisibility(View.GONE);
            getFragmentManager().popBackStack();
        });
        iv_goback_contacts.setOnClickListener(new OnClickListenerImpGoBackContactsList());
        iv_delete_edittext.setOnClickListener(new OnClickListenerImpDeleteEditText());
        btn_search_allcontacts.setOnClickListener(new OnClickListenerImpSearchContats());
        lv_search_allcontacts.setOnScrollListener(new OnScrollListenerImpSearchList());
        et_search_allcontacts.addTextChangedListener(new TextWatcherImpSearch());
        et_search_allcontacts.setOnEditorActionListener((textView, i, keyEvent) -> {
            if(i == EditorInfo.IME_ACTION_SEARCH) {
                doSearch();
            }
            return false;
        });

        MyTerminalFactory.getSDK().registReceiveHandler(receiveSearchContactsHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(mReceiveNotifyMemberChangeHandler);
        OperateReceiveHandlerUtilSync.getInstance().registReceiveHandler(mReceiverIndividualCallForAddressBookHandler);
        OperateReceiveHandlerUtilSync.getInstance().registReceiveHandler(mReceiverIndividualMsgForAddressBookHandler);
        OperateReceiveHandlerUtilSync.getInstance().registReceiveHandler(receiverCloseKeyBoardHandler);
    }

    @Override
    public void initData() {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveSearchContactsHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(mReceiveNotifyMemberChangeHandler);
        OperateReceiveHandlerUtilSync.getInstance().unregistReceiveHandler(mReceiverIndividualCallForAddressBookHandler);
        OperateReceiveHandlerUtilSync.getInstance().unregistReceiveHandler(mReceiverIndividualMsgForAddressBookHandler);
        OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiverFragmentDestoryHandler.class);
        OperateReceiveHandlerUtilSync.getInstance().unregistReceiveHandler(receiverCloseKeyBoardHandler);
    }


    public void clearKeyWord(){
        et_search_allcontacts.setText("");
        searchMemberListExceptMe.clear();
    }

    private void activeIndividualCall(int position) {
        MyApplication.instance.isCallState = true;
        logger.info("tag--先响铃，再停止");
        boolean network = MyTerminalFactory.getSDK().hasNetwork();
        if (network){
            if ( searchMemberListExceptMe.size() > 0) {
//                Member member = DataUtil.getMemberByMemberNo(currentGroupMembers.get(position).no);
//                List<Member> list = new ArrayList<>();
//                new ChooseDevicesDialog(mContext,ChooseDevicesDialog.TYPE_CALL_PRIVATE, list, (view1, position12) -> {
//                    long uniqueNo = 0l;
//                    OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiveCurrentGroupIndividualCallHandler.class, member,uniqueNo);
//                }).show();

                OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiveCurrentGroupIndividualCallHandler.class, searchMemberListExceptMe.get(position));

            }
        } else {
            ToastUtil.showToast(context, "网络连接异常，请检查网络！");
        }
    }

    @Override
    public void onClick(View v){
        switch(v.getId()){
            case R.id.iv_delete:
            clearKeyWord();
            break;
        }
    }
    /*************************************************************Handler和其他监听**************************************************************************************/
    /**关闭搜索页，返回到个呼通讯录*/
    private final class OnClickListenerImpGoBackContactsList implements
            View.OnClickListener {
        @Override
        public void onClick(View v) {
            et_search_allcontacts.setText("");
            InputMethodUtil.hideInputMethod(context, et_search_allcontacts);

            searchMemberListExceptMe.clear();
            tv_search_notdata.setVisibility(View.GONE);
            getFragmentManager().popBackStack();

        }
    }

    /**清除搜索框中的内容*/
    private final class OnClickListenerImpDeleteEditText implements
            View.OnClickListener {
        @Override
        public void onClick(View v) {
            et_search_allcontacts.setText("");
            InputMethodUtil.showInputMethod(getContext());
        }
    }

    /**点击搜索按钮进行搜索*/
    private final class OnClickListenerImpSearchContats implements
            View.OnClickListener {
        @Override
        public void onClick(View v) {
           doSearch();
        }
    }

    //搜索
    private void doSearch(){
        InputMethodUtil.hideInputMethod(getContext(), et_search_allcontacts);
        keyWord = et_search_allcontacts.getText().toString();
        tempGroupSearchAdapter.setFilterKeyWords(keyWord);

        tv_search_nothing.setVisibility(View.VISIBLE);
        tv_search_nothing.setText(R.string.text_search_contact);
        rl_search_result.setVisibility(View.GONE);
        searchMemberListExceptMe.clear();
        pageIndex = 0;
        tv_search_notdata.setVisibility(View.GONE);

        if (TextUtils.isEmpty(keyWord)) {
            ToastUtil.showToast(context, getString(R.string.text_search_content_can_not_empty));
        }else {
            if(!isLocal) {
                MyTerminalFactory.getSDK().getContactsManager().searchContacts(keyWord, 1);
            }
            else {
                searchMemberFromGroup();
            }
        }
    }

    /***  进行组内搜索 **/
    private void searchMemberFromGroup () {
        searchMemberListExceptMe.clear();
        for(Member member : groupMember) {
            String name = member.getName();
            String id = String.valueOf(member.getNo());
            if(!Util.isEmpty(name) && !Util.isEmpty(keyWord) && name.toLowerCase().contains(keyWord.toLowerCase())) {
                searchMemberListExceptMe.add(member);
            }
            else if(!Util.isEmpty(id) && !Util.isEmpty(keyWord) && id.contains(keyWord)) {
                searchMemberListExceptMe.add(member);
            }
        }
        if (searchMemberListExceptMe.size() == 0) {
            tv_search_nothing.setText(R.string.text_contact_is_not_exist);
            rl_search_result.setVisibility(View.GONE);
        } else {
            tv_search_nothing.setVisibility(View.GONE);
            rl_search_result.setVisibility(View.VISIBLE);

            if (tempGroupSearchAdapter != null) {
                tempGroupSearchAdapter.refreshSearchContactsAdapter();
                lv_search_allcontacts.setSelection(0);
            }
        }
    }

    private final class OnScrollListenerImpSearchList implements
            AbsListView.OnScrollListener {
        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            int lastVisiblePosition = lv_search_allcontacts.getLastVisiblePosition();
            int itemCount = lv_search_allcontacts.getCount();
//            logger.info("----->onScrollStateChanged    lastVisiblePosition="+lastVisiblePosition+"    itemCount="+itemCount);
            if ( lastVisiblePosition == itemCount - 1) {// && scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE
                if (pageIndex < totalPages) {//没加载完，加载数据
                    tv_search_notdata.setVisibility(View.GONE);
                    if (TextUtils.isEmpty(keyWord)) {
                        ToastUtil.showToast(context, getString(R.string.text_search_content_can_not_empty));
                    }else if( !isLocal ){
                        MyTerminalFactory.getSDK().getContactsManager().searchContacts(keyWord, pageIndex+1);
                    }
                }else {//加载完了，提示文字
                    tv_search_notdata.setVisibility(View.VISIBLE);
                    handler.sendEmptyMessageDelayed(0, 1000);
                }
            }
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem,
                             int visibleItemCount, int totalItemCount) {
            TempGroupSearchFragment.this.visibleItemCount = visibleItemCount;
        }
    }

    private ReceiveSearchContactsHandler receiveSearchContactsHandler = new ReceiveSearchContactsHandler() {
        @Override
        public void handler(final List<Member> searchMemberList, final int pageIndex,
                            final int totalPages, final int totalMember, final int errorCode, final String errorDesc) {
            logger.info("搜索联系人结果：pageIndex = "+pageIndex+"  totalPages = "+totalPages+"  totalMember = "+totalMember+"  searchMemberList = "+searchMemberList.toString());

            handler.post(() -> {
                if (errorCode == BaseCommonCode.SUCCESS_CODE) {//请求成功
                    if (searchMemberList == null || searchMemberList.size() == 0) {
                        if (searchMemberListExceptMe.size() == 0) {
                            tv_search_nothing.setText(R.string.text_user_does_not_exist);
                        }
                        return;
                    }
                    if (TempGroupSearchFragment.this.pageIndex != pageIndex) {
                        TempGroupSearchFragment.this.searchMemberListExceptMe.addAll(DataUtil.getAllMembersExceptMe(searchMemberList));
                        TempGroupSearchFragment.this.pageIndex = pageIndex;
                    }
                    TempGroupSearchFragment.this.totalPages = totalPages;

                    if (searchMemberListExceptMe.size() == 0) {
                        tv_search_nothing.setText(R.string.text_user_does_not_exist);
                        rl_search_result.setVisibility(View.GONE);
                    } else {
                        tv_search_nothing.setVisibility(View.GONE);
                        rl_search_result.setVisibility(View.VISIBLE);

                        if (tempGroupSearchAdapter != null) {
                            tempGroupSearchAdapter.refreshSearchContactsAdapter();
                        }
                    }
                } else {
                    ToastUtil.showToast(context, errorDesc);
                }
            });
        }
    };

    private final class TextWatcherImpSearch implements TextWatcher {
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (s.toString().contains(" ")) {
                String[] str = s.toString().split(" ");
                StringBuilder sb = new StringBuilder();
                for (String aStr : str) {
                    sb.append(aStr);
                }

                et_search_allcontacts.setText(sb.toString());

                et_search_allcontacts.setSelection(start);

            }
            if(TextUtils.isEmpty(s.toString())){
                iv_delete_edittext.setVisibility(View.INVISIBLE);
                btn_search_allcontacts.setBackgroundResource(R.drawable.rectangle_with_corners_shape1);
                btn_search_allcontacts.setTextColor(ContextCompat.getColor(getContext(),R.color.search_button_text_color1));
                btn_search_allcontacts.setEnabled(false);
            }else {
                iv_delete_edittext.setVisibility(View.VISIBLE);
                btn_search_allcontacts.setBackgroundResource(R.drawable.rectangle_with_corners_shape2);
                btn_search_allcontacts.setTextColor(ContextCompat.getColor(getContext(),R.color.white));
                btn_search_allcontacts.setEnabled(true);
            }
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count,
                                      int after) {}

        @Override
        public void afterTextChanged(Editable s) {}
    }

    private ReceiveNotifyMemberChangeHandler mReceiveNotifyMemberChangeHandler = new ReceiveNotifyMemberChangeHandler() {
        @Override
        public void handler(final MemberChangeType memberChangeType) {
            handler.post(() -> {
                if (memberChangeType == MemberChangeType.MEMBER_ADD ||
                        memberChangeType == MemberChangeType.MEMBER_REMOVE||
                        memberChangeType == MemberChangeType.MEMBER_NAME_MODIFY) {
                    tempGroupSearchAdapter.notifyDataSetChanged();
                }
            });
        }
    };




    private ReceiverCloseKeyBoardHandler receiverCloseKeyBoardHandler = new ReceiverCloseKeyBoardHandler() {
        @Override
        public void handler() {
            logger.info("sjl_收到来自服务的关闭键盘handler");
            InputMethodUtil.hideInputMethod(getContext(),et_search_allcontacts);
        }
    };

    /** 列表中个呼按钮点击消息监听 */
    private ReceiverIndividualCallForAddressBookHandler mReceiverIndividualCallForAddressBookHandler = (where, position) -> {
        if(where == 2) {
            activeIndividualCall(position);
        }
    };

    private ReceiverIndividualMsgForAddressBookHandler mReceiverIndividualMsgForAddressBookHandler = (where, position) -> {
        if(where == 2) {
            IndividualNewsActivity.startCurrentActivity(context, searchMemberListExceptMe.get(position).no, searchMemberListExceptMe.get(position).getName());
        }
    };
    /*************************************************************Handler和其他监听**************************************************************************************/
}

