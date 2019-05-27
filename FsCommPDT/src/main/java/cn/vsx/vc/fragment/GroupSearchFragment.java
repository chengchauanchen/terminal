package cn.vsx.vc.fragment;

import android.app.Activity;
import android.content.Context;
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

import com.zectec.imageandfileselector.utils.OperateReceiveHandlerUtilSync;

import java.util.ArrayList;
import java.util.List;

import cn.vsx.hamster.errcode.BaseCommonCode;
import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.model.Group;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveChangeGroupHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceivePopBackStackHandler;
import cn.vsx.vc.R;
import cn.vsx.vc.adapter.GroupSearchAdapter;
import cn.vsx.vc.receiveHandle.ReceiverCloseKeyBoardHandler;
import cn.vsx.vc.utils.InputMethodUtil;
import ptt.terminalsdk.context.MyTerminalFactory;
import ptt.terminalsdk.tools.ToastUtil;

/**
 *  通讯录组搜索
 * Created by gt358 on 2017/10/21.
 */

public class GroupSearchFragment extends BaseFragment {

    LinearLayout ll_search_pop;

    ImageView iv_goback_contacts;

    ImageView iv_delete_edittext;

    Button btn_search_allcontacts;

    TextView tv_search_nothing;

    RelativeLayout rl_search_result;

    ListView lv_search_allcontacts;

    EditText et_search_allcontacts;

    private String keyWord;
    //搜索到的所有组
    private List<Group> searchGroups = new ArrayList<>();
    private GroupSearchAdapter groupSearchAdapter;
    private Handler myHandler = new Handler();

    @Override
    public int getContentViewId() {
        return R.layout.fragment_group_search;
    }

    @Override
    public void initView() {
        et_search_allcontacts = (EditText) mRootView.findViewById(R.id.et_search_allcontacts);
        lv_search_allcontacts = (ListView) mRootView.findViewById(R.id.lv_search_allcontacts);
        rl_search_result = (RelativeLayout) mRootView.findViewById(R.id.rl_search_result);
        tv_search_nothing = (TextView) mRootView.findViewById(R.id.tv_search_nothing);
        btn_search_allcontacts = (Button) mRootView.findViewById(R.id.btn_search_allcontacts);
        iv_delete_edittext = (ImageView) mRootView.findViewById(R.id.iv_delete_edittext);
        iv_goback_contacts = (ImageView) mRootView.findViewById(R.id.iv_goback_contacts);
        ll_search_pop = (LinearLayout) mRootView.findViewById(R.id.ll_search_pop);
        showSoftInputFromWindow(getActivity(),et_search_allcontacts);
        iv_delete_edittext.setVisibility(View.GONE);
        btn_search_allcontacts.setBackgroundResource(R.drawable.rectangle_with_corners_shape1);
        btn_search_allcontacts.setTextColor(ContextCompat.getColor(context,R.color.search_button_text_color1));
        btn_search_allcontacts.setEnabled(false);
    }

    @Override
    public void initData() {
        groupSearchAdapter = new GroupSearchAdapter(context, searchGroups);
        lv_search_allcontacts.setAdapter(groupSearchAdapter);
    }

    @Override
    public void initListener() {
        iv_delete_edittext.setOnClickListener(new OnClickListenerImpDeleteEditText());
        iv_goback_contacts.setOnClickListener(new OnClickListenerImpGoBackContactsList());
        btn_search_allcontacts.setOnClickListener(new OnClickListenerImpSearchContats());
        lv_search_allcontacts.setOnItemClickListener(new OnItemClickListenerImpAddCall());
        MyTerminalFactory.getSDK().registReceiveHandler(mReceiveChangeGroupHandler);
        OperateReceiveHandlerUtilSync.getInstance().registReceiveHandler(receiverCloseKeyBoardHandler);


        et_search_allcontacts.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(et_search_allcontacts !=null && context !=null){

                    if (s.toString().contains(" ")) {
                        String[] str = s.toString().split(" ");
                        String str1 = "";
                        for (String aStr : str) {
                            str1 += aStr;
                        }

                        et_search_allcontacts.setText(str1);

                        et_search_allcontacts.setSelection(start);

                    }
                    if(TextUtils.isEmpty(s.toString())){
                        iv_delete_edittext.setVisibility(View.GONE);
                        btn_search_allcontacts.setBackgroundResource(R.drawable.rectangle_with_corners_shape1);
                        btn_search_allcontacts.setTextColor(ContextCompat.getColor(context,R.color.search_button_text_color1));
                        btn_search_allcontacts.setEnabled(false);
                    }else {
                        iv_delete_edittext.setVisibility(View.VISIBLE);
                        btn_search_allcontacts.setBackgroundResource(R.drawable.rectangle_with_corners_shape2);
                        btn_search_allcontacts.setTextColor(ContextCompat.getColor(context,R.color.white));
                        btn_search_allcontacts.setEnabled(true);
                    }
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

        groupSearchAdapter.setOnItemBtnClick(() -> {
            et_search_allcontacts.setText("");
            InputMethodUtil.hideInputMethod(context, et_search_allcontacts);

            searchGroups.clear();
            MyTerminalFactory.getSDK().notifyReceiveHandler(ReceivePopBackStackHandler.class);
        });
    }

    @Override
    public void unRegistListener() {
        super.unRegistListener();
        MyTerminalFactory.getSDK().unregistReceiveHandler(mReceiveChangeGroupHandler);
        OperateReceiveHandlerUtilSync.getInstance().unregistReceiveHandler(receiverCloseKeyBoardHandler);
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

    /**
     * 搜索
     */
    private void doSearch(){
        InputMethodUtil.hideInputMethod(getContext(), et_search_allcontacts);
        keyWord = et_search_allcontacts.getText().toString();
        groupSearchAdapter.setFilterKeyWords(keyWord);

        tv_search_nothing.setVisibility(View.VISIBLE);
        tv_search_nothing.setText(R.string.text_search_group);
        rl_search_result.setVisibility(View.GONE);
        searchGroups.clear();

        if (TextUtils.isEmpty(keyWord)) {
            ToastUtil.showToast(context, getString(R.string.text_search_content_can_not_empty));
        }else {
            searchMemberFromGroup(keyWord);
        }
    }

    /**  搜索 **/
    private void searchMemberFromGroup (String keyWord){
        searchGroups.clear();
        searchGroups.addAll(TerminalFactory.getSDK().getConfigManager().searchGroup(keyWord));
        if (searchGroups.size() == 0) {
            tv_search_nothing.setText(R.string.text_group_is_not_exist);
            rl_search_result.setVisibility(View.GONE);
        } else {
            tv_search_nothing.setVisibility(View.GONE);
            rl_search_result.setVisibility(View.VISIBLE);

            if (groupSearchAdapter != null) {
                groupSearchAdapter.notifyDataSetChanged();
                lv_search_allcontacts.setSelection(0);
            }
        }
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

            searchGroups.clear();
            MyTerminalFactory.getSDK().notifyReceiveHandler(ReceivePopBackStackHandler.class);

        }
    }

    /**点击搜索按钮进行搜索*/
    private final class OnClickListenerImpSearchContats implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            doSearch();
        }
    }

    /**搜索页面的list的条目点击事件，以及添加和个呼的按钮；*/
    private final class OnItemClickListenerImpAddCall implements
            AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view,
                                int position, long id) {
            //刷新数据
            groupSearchAdapter.notifyDataSetChanged();
        }
    }

    /**  切组回调 **/
    private ReceiveChangeGroupHandler mReceiveChangeGroupHandler = (errorCode, errorDesc) -> myHandler.post(() -> {
        if(null != et_search_allcontacts && null != context){
            if(errorCode == BaseCommonCode.SUCCESS_CODE){
                et_search_allcontacts.setText("");
                InputMethodUtil.hideInputMethod(context, et_search_allcontacts);
                searchGroups.clear();
            }else {
                ToastUtil.groupChangedFailToast(context, errorCode);
            }
        }
    });

    private ReceiverCloseKeyBoardHandler receiverCloseKeyBoardHandler = new ReceiverCloseKeyBoardHandler() {
        @Override
        public void handler() {
            logger.info("关闭键盘handler");
            if(null != getContext()){
                InputMethodUtil.hideInputMethod(getContext(),et_search_allcontacts);
            }
        }
    };
}
