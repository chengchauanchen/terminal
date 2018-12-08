package cn.vsx.vc.fragment;

import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
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

import butterknife.Bind;
import cn.vsx.hamster.common.GroupType;
import cn.vsx.hamster.errcode.BaseCommonCode;
import cn.vsx.hamster.terminalsdk.model.Group;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveSetScanGroupListResultHandler;
import cn.vsx.hamster.terminalsdk.tools.Util;
import cn.vsx.vc.R;
import cn.vsx.vc.adapter.ScanGroupSearchAdapter;
import cn.vsx.vc.receiveHandle.ReceiverCloseKeyBoardHandler;
import cn.vsx.vc.receiveHandle.ReceiverFragmentDestoryHandler;
import cn.vsx.vc.utils.InputMethodUtil;
import ptt.terminalsdk.tools.ToastUtil;
import ptt.terminalsdk.context.MyTerminalFactory;

/**
 * 作者：ly-xuxiaolong
 * 版本：1.0
 * 创建日期：2018/6/28
 * 描述：
 * 修订历史：
 */

public class ScanGroupSearchFragment extends BaseFragment{

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

    private String keyWord;
    //搜索到的所有组
    private List<Group> searchGroups = new ArrayList<>();
    private ScanGroupSearchAdapter scanGroupSearchAdapter;
    private Handler myHandler = new Handler();

    @Override
    public int getContentViewId(){
        return R.layout.fragment_group_search;
    }

    @Override
    public void initView(){
    }

    @Override
    public void initData(){
        scanGroupSearchAdapter = new ScanGroupSearchAdapter(getContext(), searchGroups);
        lv_search_allcontacts.setAdapter(scanGroupSearchAdapter);
        et_search_allcontacts.setFocusable(true);
    }

    @Override
    public void initListener(){
        iv_delete_edittext.setOnClickListener(new OnClickListenerImpDeleteEditText());
        iv_goback_contacts.setOnClickListener(new OnClickListenerImpGoBackContactsList());
        btn_search_allcontacts.setOnClickListener(new OnClickListenerImpSearchContats());
//        lv_search_allcontacts.setOnItemClickListener(new OnItemClickListenerImpAddCall());
        OperateReceiveHandlerUtilSync.getInstance().registReceiveHandler(receiverCloseKeyBoardHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(mReceiveSetScanGroupListResultHandler);
        et_search_allcontacts.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().contains(" ")) {
                    String[] str = s.toString().split(" ");
                    String str1 = "";
                    for (int i = 0; i < str.length; i++) {
                        str1 += str[i];
                    }
                    et_search_allcontacts.setText(str1);

                    et_search_allcontacts.setSelection(start);

                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        et_search_allcontacts.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if(i == EditorInfo.IME_ACTION_SEARCH) {
                    doSearch();
                }
                return false;
            }
        });
    }


    @Override
    public void unRegistListener() {
        super.unRegistListener();
        MyTerminalFactory.getSDK().unregistReceiveHandler(mReceiveSetScanGroupListResultHandler);
        OperateReceiveHandlerUtilSync.getInstance().unregistReceiveHandler(receiverCloseKeyBoardHandler);
        OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiverFragmentDestoryHandler.class);
        OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiveSetScanGroupListResultHandler.class);
    }

    /**
     * 搜索
     */
    private void doSearch(){
        InputMethodUtil.hideInputMethod(getContext(), et_search_allcontacts);
        keyWord = et_search_allcontacts.getText().toString();
        scanGroupSearchAdapter.setFilterKeyWords(keyWord);

        tv_search_nothing.setVisibility(View.VISIBLE);
        tv_search_nothing.setText("搜索组");
        rl_search_result.setVisibility(View.GONE);
        searchGroups.clear();

        if (TextUtils.isEmpty(keyWord)) {
            ToastUtil.showToast(context, "搜索的内容不能为空");
        }else {
            searchMemberFromGroup();
        }
    }

    /**  搜索 **/
    private void searchMemberFromGroup (){
        for (Group group : MyTerminalFactory.getSDK().getConfigManager().getAllGroups()) {
            //去掉响应组
            if(group.getGroupType()!= GroupType.RESPONSE && !Util.isEmpty(group.name) && !Util.isEmpty(keyWord) && group.name.toLowerCase().contains(keyWord.toLowerCase())) {
                searchGroups.add(group);
            }
        }
        if (searchGroups.size() == 0) {
            tv_search_nothing.setText("组不存在");
            rl_search_result.setVisibility(View.GONE);
        } else {
            tv_search_nothing.setVisibility(View.GONE);
            rl_search_result.setVisibility(View.VISIBLE);

            if (scanGroupSearchAdapter != null) {
                scanGroupSearchAdapter.notifyDataSetChanged();;
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
            InputMethodUtil.showInputMethod(getContext());
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
            getFragmentManager().popBackStack();

        }
    }

    /**点击搜索按钮进行搜索*/
    private final class OnClickListenerImpSearchContats implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            doSearch();
        }
    }

    private ReceiverCloseKeyBoardHandler receiverCloseKeyBoardHandler = new ReceiverCloseKeyBoardHandler() {
        @Override
        public void handler() {
            logger.info("关闭键盘handler");
            InputMethodUtil.hideInputMethod(getContext(),et_search_allcontacts);
        }
    };

    private ReceiveSetScanGroupListResultHandler mReceiveSetScanGroupListResultHandler=new ReceiveSetScanGroupListResultHandler() {

        @Override
        public void handler(final List<Integer> scanGroups, final int errorCode, final String errorDesc) {
            myHandler.post(new Runnable() {
                @Override
                public void run() {
                    if(errorCode== BaseCommonCode.SUCCESS_CODE){
                        ToastUtil.toast(getActivity(),"添加扫描组成功");
                        scanGroupSearchAdapter.notifyDataSetChanged();
                    }else {
                        ToastUtil.toast(getActivity(),"添加扫描组失败");
                    }
                }
            });
        }
    };
}
