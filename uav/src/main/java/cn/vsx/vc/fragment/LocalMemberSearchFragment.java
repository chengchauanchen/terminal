package cn.vsx.vc.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zectec.imageandfileselector.receivehandler.ReceiverIndividualCallForAddressBookHandler;
import com.zectec.imageandfileselector.receivehandler.ReceiverIndividualMsgForAddressBookHandler;
import com.zectec.imageandfileselector.receivehandler.ReceiverLinkManDeleteHandler;
import com.zectec.imageandfileselector.utils.OperateReceiveHandlerUtilSync;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import cn.vsx.hamster.common.MemberAddressList;
import cn.vsx.hamster.common.MemberChangeType;
import cn.vsx.hamster.terminalsdk.model.Member;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveCurrentGroupIndividualCallHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveNotifyMemberChangeHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceivePopBackStackHandler;
import cn.vsx.hamster.terminalsdk.tools.Util;
import cn.vsx.vc.R;
import cn.vsx.vc.activity.IndividualNewsActivity;
import cn.vsx.vc.adapter.LocalMemberSearchAdapter;
import cn.vsx.vc.application.MyApplication;
import cn.vsx.vc.dialog.LinkmanDeleteDialog;
import cn.vsx.vc.receiveHandle.ReceiverDialogDimissHandler;
import cn.vsx.vc.receiveHandle.ReceiverFragmentDestoryHandler;
import cn.vsx.vc.utils.DataUtil;
import cn.vsx.vc.utils.InputMethodUtil;
import ptt.terminalsdk.context.MyTerminalFactory;
import ptt.terminalsdk.tools.ToastUtil;

/**
 * Created by gt358 on 2017/10/25.
 */

public class LocalMemberSearchFragment extends BaseFragment{

    @Bind(R.id.ll_search_pop)
    LinearLayout ll_search_pop;
    @Bind(R.id.iv_goback_contacts)
    ImageView iv_goback_contacts;
    @Bind(R.id.iv_delete_edittext)
    ImageView iv_delete_edittext;
    @Bind(R.id.btn_search_allcontacts)
    Button btn_search_allcontacts;
    @Bind(R.id.tv_search_nothing)
    TextView tv_search_nothing;
    @Bind(R.id.rl_search_result)
    RelativeLayout rl_search_result;
    @Bind(R.id.lv_search_allcontacts)
    ListView lv_search_allcontacts;
    @Bind(R.id.et_search_allcontacts)
    EditText et_search_allcontacts;

    String keyWord;

    List<Member> memberList = new ArrayList<>();

    List<Member> searchList = new ArrayList<>();

    private LocalMemberSearchAdapter personContactsAdapter;
    LinkmanDeleteDialog mLinkmanDeleteDialog;
    Handler myHandler = new Handler();
    int mposition = -1;
    /**  设置组集合数据 **/
    public void setMemberList (List<Member> memberList) {
        this.memberList = memberList;
    }
    @Override
    public int getContentViewId() {
        return R.layout.fragment_local_member_search;
    }

    @Override
    public void initView() {
        personContactsAdapter = new LocalMemberSearchAdapter(getContext(), searchList);
        lv_search_allcontacts.setAdapter(personContactsAdapter);
    }

    @Override
    public void initListener() {
        et_search_allcontacts.addTextChangedListener(new TextWatcherImpSearch());
        iv_delete_edittext.setOnClickListener(new OnClickListenerImpDeleteEditText());
        iv_goback_contacts.setOnClickListener(new OnClickListenerImpGoBackContactsList());
        btn_search_allcontacts.setOnClickListener(new OnClickListenerImpSearchContats());
        //        lv_search_allcontacts.setOnItemLongClickListener(new OnItemLongClickListenerImp());
        OperateReceiveHandlerUtilSync.getInstance().registReceiveHandler(mReceiverIndividualCallForAddressBookHandler);
        OperateReceiveHandlerUtilSync.getInstance().registReceiveHandler(mReceiverIndividualMsgForAddressBookHandler);
        OperateReceiveHandlerUtilSync.getInstance().registReceiveHandler(mReceiverLinkManDeleteHandler);
        OperateReceiveHandlerUtilSync.getInstance().registReceiveHandler(mReceiverDialogDimissHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(mReceiveNotifyMemberChangeHandler);
        et_search_allcontacts.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().contains(" ")) {
                    String[] str = s.toString().split(" ");
                    String str1 = "";
                    for (String aStr : str) {
                        str1 += aStr;
                    }
                    et_search_allcontacts.setText(str1);

                    et_search_allcontacts.setSelection(start);

                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        et_search_allcontacts.setOnEditorActionListener((textView, i, keyEvent) -> {
            if(i == EditorInfo.IME_ACTION_SEARCH) {
                doSearch();
            }
            return false;
        });

        personContactsAdapter.setOnItemBtnClick(() -> {
            et_search_allcontacts.setText("");
            InputMethodUtil.hideInputMethod(context, et_search_allcontacts);

            searchList.clear();
            MyTerminalFactory.getSDK().notifyReceiveHandler(ReceivePopBackStackHandler.class);
        });
    }

    @Override
    public void initData() {
        showSoftInputFromWindow(getActivity(),et_search_allcontacts);
        iv_delete_edittext.setVisibility(View.GONE);
        btn_search_allcontacts.setBackgroundResource(R.drawable.rectangle_with_corners_shape1);
        btn_search_allcontacts.setTextColor(ContextCompat.getColor(getContext(),R.color.search_button_text_color1));
        btn_search_allcontacts.setEnabled(false);
    }

    public static void showSoftInputFromWindow(Activity activity, final EditText editText) {
        editText.setFocusable(true);
        editText.setFocusableInTouchMode(true);
        editText.requestFocus();
        final InputMethodManager inputMethodManager = ((InputMethodManager)activity.getSystemService(Context.INPUT_METHOD_SERVICE));
        if(inputMethodManager!=null){
            editText.postDelayed(() -> inputMethodManager.showSoftInput(editText, 0),50);
        }
    }

    @Override
    public void unRegistListener() {
        super.unRegistListener();
        OperateReceiveHandlerUtilSync.getInstance().unregistReceiveHandler(mReceiverIndividualCallForAddressBookHandler);
        OperateReceiveHandlerUtilSync.getInstance().unregistReceiveHandler(mReceiverIndividualMsgForAddressBookHandler);
        OperateReceiveHandlerUtilSync.getInstance().unregistReceiveHandler(mReceiverLinkManDeleteHandler);
        OperateReceiveHandlerUtilSync.getInstance().unregistReceiveHandler(mReceiverDialogDimissHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(mReceiveNotifyMemberChangeHandler);
        OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiverFragmentDestoryHandler.class);
    }

    /**
     * 搜索
     */
    private void doSearch(){
        InputMethodUtil.hideInputMethod(getContext(), et_search_allcontacts);
        keyWord = et_search_allcontacts.getText().toString();
        personContactsAdapter.setFilterKeyWords(keyWord);

        tv_search_nothing.setVisibility(View.VISIBLE);
        tv_search_nothing.setText(R.string.text_search_contact);
        rl_search_result.setVisibility(View.GONE);
        searchList.clear();

        if (TextUtils.isEmpty(keyWord)) {
            ToastUtil.showToast(context, getString(R.string.text_search_content_can_not_empty));
        }else {
            searchMemberFromGroup();
        }
    }

    /**  搜索 **/
    private void searchMemberFromGroup () {
        searchList.clear();
        for(Member member : memberList) {
            String name = member.getName();
            String id = String.valueOf(member.getNo());
            if(!Util.isEmpty(name) && !Util.isEmpty(keyWord) && name.toLowerCase().contains(keyWord.toLowerCase())) {
                searchList.add(member);
            }
            else if(!Util.isEmpty(id) && !Util.isEmpty(keyWord) && id.contains(keyWord)) {
                searchList.add(member);
            }
        }
        if (searchList.size() == 0) {
            tv_search_nothing.setText(R.string.text_contact_is_not_exist);
            rl_search_result.setVisibility(View.GONE);
        } else {
            tv_search_nothing.setVisibility(View.GONE);
            rl_search_result.setVisibility(View.VISIBLE);

            if (personContactsAdapter != null) {
                personContactsAdapter.notifyDataSetChanged();
                lv_search_allcontacts.setSelection(0);
            }
        }
    }

    private void activeIndividualCall(int position) {
        MyApplication.instance.isCallState = true;
        logger.info("tag--先响铃，再停止");
        boolean network = MyTerminalFactory.getSDK().hasNetwork();
        if (network){
            if ( searchList.size() > 0) {

//                Member member = DataUtil.getMemberByMemberNo(currentGroupMembers.get(position).no);
//                List<Member> list = new ArrayList<>();
//                new ChooseDevicesDialog(mContext,ChooseDevicesDialog.TYPE_CALL_PRIVATE, list, (view1, position12) -> {
//                    long uniqueNo = 0l;
//                    OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiveCurrentGroupIndividualCallHandler.class, member,uniqueNo);
//                }).show();
                OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiveCurrentGroupIndividualCallHandler.class, searchList.get(position));

            }
        } else {
            ToastUtil.showToast(context, getString(R.string.text_network_connection_abnormal_please_check_the_network));
        }
    }

    private final class TextWatcherImpSearch implements TextWatcher {
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if ( s.length() > 0 && !DataUtil.isLegalSearch(s)) {
                ToastUtil.showToast(context, getString(R.string.text_search_content_is_illegal));
            }
            if(TextUtils.isEmpty(s.toString())){
                iv_delete_edittext.setVisibility(View.GONE);
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

    /**清除搜索框中的内容*/
    private final class OnClickListenerImpDeleteEditText implements
            View.OnClickListener {
        @Override
        public void onClick(View v) {
            et_search_allcontacts.setText("");
        }
    }

    /**关闭搜索页，返回到个呼通讯录*/
    private final class OnClickListenerImpGoBackContactsList implements
            View.OnClickListener {
        @Override
        public void onClick(View v) {
            et_search_allcontacts.setText("");
            InputMethodUtil.hideInputMethod(context, et_search_allcontacts);

            searchList.clear();
            MyTerminalFactory.getSDK().notifyReceiveHandler(ReceivePopBackStackHandler.class);

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

    private final class OnItemLongClickListenerImp implements
            AdapterView.OnItemLongClickListener {

        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view,
                                       int position, long id) {
            if (position >= 0) {
                mposition = position;
                if(mLinkmanDeleteDialog == null) {
                    mLinkmanDeleteDialog = new LinkmanDeleteDialog(context);
                }
                mLinkmanDeleteDialog.show();
                personContactsAdapter.setLongClickPos(position);
            }
            return true;
        }

    }

    private ReceiverIndividualCallForAddressBookHandler mReceiverIndividualCallForAddressBookHandler = (where, position) -> {
        if(where == 4) {
            activeIndividualCall(position);
        }
    };

    private ReceiverIndividualMsgForAddressBookHandler mReceiverIndividualMsgForAddressBookHandler = (where, position) -> {
        if(where == 4) {
            Intent intent = new Intent(context, IndividualNewsActivity.class);
            intent.putExtra("isGroup", false);
            intent.putExtra("userId", searchList.get(position).id);
            intent.putExtra("userName", searchList.get(position).getName());
            context.startActivity(intent);
        }
    };

    private ReceiverLinkManDeleteHandler mReceiverLinkManDeleteHandler = new ReceiverLinkManDeleteHandler() {
        @Override
        public void handler() {
            MyTerminalFactory.getSDK().getContactsManager().modifyContacts(MemberAddressList.REMOVE.getCode(), searchList.get(mposition ).id);
            if(mLinkmanDeleteDialog != null)
                mLinkmanDeleteDialog.dismiss();
        }
    };

    private ReceiverDialogDimissHandler mReceiverDialogDimissHandler = new ReceiverDialogDimissHandler() {
        @Override
        public void handler(String dialogName) {
            if (dialogName.endsWith(LinkmanDeleteDialog.class.getName()))
                personContactsAdapter.setLongClickPos(-1);
        }
    };

    private ReceiveNotifyMemberChangeHandler mReceiveNotifyMemberChangeHandler = new ReceiveNotifyMemberChangeHandler() {
        @Override
        public void handler(MemberChangeType memberChangeType) {
            myHandler.post(() -> {
                if (mposition >= 0) {
                    searchList.remove(mposition);
                    if (searchList.size() == 0) {
                        tv_search_nothing.setText(R.string.text_contact_is_not_exist);
                        rl_search_result.setVisibility(View.GONE);
                    } else {
                        tv_search_nothing.setVisibility(View.GONE);
                        rl_search_result.setVisibility(View.VISIBLE);

                        if (personContactsAdapter != null) {
                            personContactsAdapter.notifyDataSetChanged();
                        }
                    }
                    mposition = -1;
                }
            });
        }
    };

    public void setType(int type){

    }
}
