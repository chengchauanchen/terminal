package cn.vsx.vc.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;

import java.util.ArrayList;
import java.util.List;

import cn.vsx.hamster.common.TerminalMemberType;
import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.model.Account;
import cn.vsx.hamster.terminalsdk.model.Group;
import cn.vsx.hamster.terminalsdk.model.Member;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveMemberSelectedHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveSearchAccountResultHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveSearchMemberResultHandler;
import cn.vsx.vc.R;
import cn.vsx.vc.adapter.SearchAdapter;
import cn.vsx.vc.model.ContactItemBean;
import cn.vsx.vc.utils.Constants;
import cn.vsx.vc.utils.ToastUtil;
import ptt.terminalsdk.context.MyTerminalFactory;

/**
 * 搜索的界面
 */
public class SearchFragment extends BaseFragment implements BaseQuickAdapter.OnItemChildClickListener, BaseQuickAdapter.RequestLoadMoreListener, SwipeRefreshLayout.OnRefreshListener{
    private static final String TYPE = "type";
    private static final String SELECTED_NOS = "selectedNos";
    private static final String TERMINALMEMBERTYPES = "terminalMemberTypes";
    private int type;
    private ImageView mIvBack;
    private EditText mEtSearchAllcontacts;
    private ImageView mIvDeleteEdittext;
    private Button mBtnSearchAllcontacts;
    private TextView mTvSearchNothing;
    private RelativeLayout mRlSearchResult;
    private RecyclerView mRecyclerview;
    private SwipeRefreshLayout mLayoutSrl;
    private SearchAdapter searchAdapter;
    private BackListener backListener;
    private LinearLayout mLlDelete;
    private List<ContactItemBean> mData = new ArrayList<>();
    private Handler mhandler = new Handler(Looper.getMainLooper());
    private int currentPage = 0;
    private static final int PAGE_SIZE = 20;
    private List<Integer>selectedNos;
    private ArrayList<String> terminalMemberTypes;

    private ReceiveSearchMemberResultHandler receiveSearchMemberResultHandler = new ReceiveSearchMemberResultHandler() {

        @Override
        public void handler(int page, int totalPages, int size, List<Member> members){
            mhandler.post(() -> {
                mLayoutSrl.setRefreshing(false);
                if(totalPages == 0){
                    mTvSearchNothing.setVisibility(View.VISIBLE);
                    mTvSearchNothing.setText(R.string.text_contact_is_not_exist);
                    searchAdapter.loadMoreEnd(true);
                    mRlSearchResult.setVisibility(View.GONE);
                }else{
                    mTvSearchNothing.setVisibility(View.GONE);
                    mRlSearchResult.setVisibility(View.VISIBLE);
                    searchAdapter.setEnableLoadMore(totalPages -1 > page);
                    if(page == totalPages-1){
                        //最后一页
                        searchAdapter.loadMoreEnd(true);
                    }else {
                        searchAdapter.loadMoreComplete();
                    }

                    if(page <= 1){
                        mData.clear();
                    }

                    for(Member member : members){
                        for(Integer selectedNo : selectedNos){
                            if(member.getNo() == selectedNo){
                                member.setChecked(true);
                                break;
                            }
                        }
                        ContactItemBean<Member> contactItemBean = new ContactItemBean<>();
                        contactItemBean.setType(type);
                        contactItemBean.setBean(member);
                        mData.add(contactItemBean);
                    }

                    searchAdapter.notifyDataSetChanged();
                }
            });
        }
    };

    private ReceiveSearchAccountResultHandler receiveSearchAccountResultHandler = new ReceiveSearchAccountResultHandler() {

        @Override
        public void handler(int page, int totalPages, int size, List<Account> accounts){
            mhandler.post(() -> {
                mLayoutSrl.setRefreshing(false);
                if(totalPages == 0){
                    mTvSearchNothing.setVisibility(View.VISIBLE);
                    if(Constants.TYPE_CONTRACT_GROUP == type || Constants.TYPE_CHECK_SEARCH_GROUP == type){
                        mTvSearchNothing.setText(R.string.text_group_is_not_exist);
                    }else {
                        mTvSearchNothing.setText(R.string.text_contact_is_not_exist);
                    }
                    searchAdapter.loadMoreEnd(true);
                    mRlSearchResult.setVisibility(View.GONE);
                }else{
                    mTvSearchNothing.setVisibility(View.GONE);
                    mRlSearchResult.setVisibility(View.VISIBLE);
                    searchAdapter.setEnableLoadMore(totalPages -1 > page);
                    if(page == totalPages-1){
                        //最后一页
                        searchAdapter.loadMoreEnd(true);
                    }else {
                        searchAdapter.loadMoreComplete();
                    }

                    if(page <= 1){
                        mData.clear();
                    }

                    for(Account account : accounts){
                        for(Integer selectedNo : selectedNos){
                            if(account.getNo() == selectedNo){
                                account.setChecked(true);
                                break;
                            }
                        }
                        ContactItemBean<Account> contactItemBean = new ContactItemBean<>();
                        contactItemBean.setType(type);
                        contactItemBean.setBean(account);
                        mData.add(contactItemBean);
                    }

                    searchAdapter.notifyDataSetChanged();
                }
            });
        }
    };

    public SearchFragment(){
    }

    public static SearchFragment newInstance(int type, ArrayList<Integer> selectedNos,ArrayList<String> terminalMemberTypes){
        SearchFragment fragment = new SearchFragment();
        Bundle args = new Bundle();
        args.putInt(TYPE, type);
        args.putStringArrayList(TERMINALMEMBERTYPES, terminalMemberTypes);
        args.putIntegerArrayList(SELECTED_NOS,selectedNos);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        if(getArguments() != null){
            type = getArguments().getInt(TYPE);
            selectedNos = getArguments().getIntegerArrayList(SELECTED_NOS);
            terminalMemberTypes = getArguments().getStringArrayList(TERMINALMEMBERTYPES);
        }
    }

    @Override
    public int getContentViewId(){
        return R.layout.layout_search_fragment;
    }

    @Override
    public void initView(){
        mIvBack = mRootView.findViewById(R.id.iv_back);
        mEtSearchAllcontacts = mRootView.findViewById(R.id.et_search_allcontacts);
        mLlDelete = mRootView.findViewById(R.id.ll_delete);
        mIvDeleteEdittext = mRootView.findViewById(R.id.iv_delete_edittext);
        mBtnSearchAllcontacts = mRootView.findViewById(R.id.btn_search_allcontacts);
        mTvSearchNothing = mRootView.findViewById(R.id.tv_search_nothing);
        mRlSearchResult = mRootView.findViewById(R.id.rl_search_result);
        mRecyclerview = mRootView.findViewById(R.id.recyclerview);
        mLayoutSrl = mRootView.findViewById(R.id.layout_srl);
        mLlDelete.setVisibility(View.GONE);
    }

    @Override
    public void initListener(){
        MyTerminalFactory.getSDK().registReceiveHandler(receiveSearchAccountResultHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveSearchMemberResultHandler);
        mIvBack.setOnClickListener(v -> getActivity().getSupportFragmentManager().popBackStack());
        mLlDelete.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                mEtSearchAllcontacts.setText("");
                mData.clear();
                if(searchAdapter != null){
                    searchAdapter.notifyDataSetChanged();
                }
                mRlSearchResult.setVisibility(View.GONE);
                mTvSearchNothing.setVisibility(View.VISIBLE);
                if(Constants.TYPE_CONTRACT_GROUP == type || Constants.TYPE_CHECK_SEARCH_GROUP == type){
                    mTvSearchNothing.setText(getResources().getString(R.string.text_search_by_group_name));
                }else {
                    mTvSearchNothing.setText(getResources().getString(R.string.text_search_by_name_or_number));
                }
            }
        });
        mIvBack.setOnClickListener(v -> {
            if(backListener != null){
                backListener.onBack();
            }
        });
        mBtnSearchAllcontacts.setOnClickListener(v -> {
            if(TextUtils.isEmpty(mEtSearchAllcontacts.getText().toString())){
                ToastUtil.showToast(context, context.getResources().getString(R.string.input_search_content));
                return;
            }
            searchAdapter.setFilterKeyWords(mEtSearchAllcontacts.getText().toString());
            doSearch(mEtSearchAllcontacts.getText().toString());
        });
        mEtSearchAllcontacts.setOnEditorActionListener((v, actionId, event) -> {
            if(actionId == EditorInfo.IME_ACTION_SEARCH) {
                if(!TextUtils.isEmpty(mEtSearchAllcontacts.getText().toString())){
                    searchAdapter.setFilterKeyWords(mEtSearchAllcontacts.getText().toString());
                    doSearch(mEtSearchAllcontacts.getText().toString());
                }
            }
            return false;
        });
        mEtSearchAllcontacts.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(mEtSearchAllcontacts !=null && context !=null){

                    if (s.toString().contains(" ")) {
                        String[] str = s.toString().split(" ");
                        String str1 = "";
                        for (String aStr : str) {
                            str1 += aStr;
                        }

                        mEtSearchAllcontacts.setText(str1);

                        mEtSearchAllcontacts.setSelection(start);

                    }
                    if(TextUtils.isEmpty(s.toString())){
                        mLlDelete.setVisibility(View.GONE);
                        mBtnSearchAllcontacts.setBackgroundResource(R.drawable.rectangle_with_corners_shape1);
                        mBtnSearchAllcontacts.setTextColor(ContextCompat.getColor(context,R.color.search_button_text_color1));
                        mBtnSearchAllcontacts.setEnabled(false);
                    }else {
                        mLlDelete.setVisibility(View.VISIBLE);
                        mBtnSearchAllcontacts.setBackgroundResource(R.drawable.rectangle_with_corners_shape2);
                        mBtnSearchAllcontacts.setTextColor(ContextCompat.getColor(context,R.color.white));
                        mBtnSearchAllcontacts.setEnabled(true);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void doSearch(String keywords){
        switch(type){
            case Constants.TYPE_CHECK_SEARCH_GROUP:
            case Constants.TYPE_CHECK_SEARCH_BUTTON_GROUP:
            case Constants.TYPE_CONTRACT_GROUP:
                mData.clear();
                List<Group> groups = TerminalFactory.getSDK().getConfigManager().searchGroup(keywords);
                mLayoutSrl.setRefreshing(false);
                for(Group group : groups){
                    boolean isCheck = false;
                    for(Integer selectedNo : selectedNos){
                        if(group.getNo() == selectedNo){
                            isCheck = true;
                            break;
                        }
                    }
                    group.setChecked(isCheck);
                    ContactItemBean<Group> contactItemBean = new ContactItemBean<>();
                    contactItemBean.setType(type);
                    contactItemBean.setBean(group);
                    mData.add(contactItemBean);
                }
                if(mData.isEmpty()){
                    mTvSearchNothing.setVisibility(View.VISIBLE);
                    mRlSearchResult.setVisibility(View.GONE);
                }else{
                    mTvSearchNothing.setVisibility(View.GONE);
                    mRlSearchResult.setVisibility(View.VISIBLE);
                    searchAdapter.notifyDataSetChanged();
                    searchAdapter.loadMoreEnd(true);
                }
                break;
            case Constants.TYPE_CONTRACT_PDT:
                TerminalFactory.getSDK().getConfigManager().searchMember(currentPage,PAGE_SIZE,TerminalMemberType.TERMINAL_PDT.toString(), keywords);
                break;
            case Constants.TYPE_CONTRACT_LTE:
                TerminalFactory.getSDK().getConfigManager().searchMember(currentPage,PAGE_SIZE,TerminalMemberType.TERMINAL_LTE.toString(), keywords);
                break;
            case Constants.TYPE_CONTRACT_RECORDER:
                TerminalFactory.getSDK().getConfigManager().searchMember(currentPage,PAGE_SIZE,TerminalMemberType.TERMINAL_BODY_WORN_CAMERA.toString(), keywords);
                break;
            case Constants.TYPE_CHECK_SEARCH_ACCOUNT:
            case Constants.TYPE_CONTRACT_MEMBER:
                TerminalFactory.getSDK().getConfigManager().searchAccount(currentPage,PAGE_SIZE, keywords);
                break;
            case Constants.TYPE_CHECK_SEARCH_PC:
                TerminalFactory.getSDK().getConfigManager().searchMember(currentPage,PAGE_SIZE,TerminalMemberType.TERMINAL_PC.toString(), keywords);
                break;
            case Constants.TYPE_CHECK_SEARCH_POLICE:
                TerminalFactory.getSDK().getConfigManager().searchMember(currentPage,PAGE_SIZE,TerminalMemberType.TERMINAL_PHONE.toString(), keywords);
                break;
            case Constants.TYPE_CHECK_SEARCH_RECODER:
                TerminalFactory.getSDK().getConfigManager().searchMember(currentPage,PAGE_SIZE,TerminalMemberType.TERMINAL_BODY_WORN_CAMERA.toString(), keywords);
                break;
            case Constants.TYPE_CHECK_SEARCH_HDMI:
                TerminalFactory.getSDK().getConfigManager().searchMember(currentPage,PAGE_SIZE,TerminalMemberType.TERMINAL_HDMI.toString(), keywords);
                break;
            case Constants.TYPE_CHECK_SEARCH_UAV:
                TerminalFactory.getSDK().getConfigManager().searchMember(currentPage,PAGE_SIZE,TerminalMemberType.TERMINAL_UAV.toString(), keywords);
                break;
            case Constants.TYPE_CONTRACT_TERMINAL:
                TerminalFactory.getSDK().getDataManager().findByDeptAndKeyList(currentPage,PAGE_SIZE,terminalMemberTypes, keywords);
                break;
            default:
                break;
        }
    }

    @Override
    public void initData(){

        searchAdapter = new SearchAdapter(mData);
        searchAdapter.setOnItemChildClickListener(this);
        searchAdapter.enableLoadMoreEndClick(true);
        searchAdapter.setOnLoadMoreListener(this, mRecyclerview);
        searchAdapter.setOnItemClickListener(() -> {
            if(backListener !=null){
                backListener.onBack();
            }
        });
        mRecyclerview.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerview.setAdapter(searchAdapter);
        mLayoutSrl.setColorSchemeResources(R.color.colorPrimary);
        mLayoutSrl.setProgressViewOffset(false, 0, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, getResources().getDisplayMetrics()));
        mLayoutSrl.setOnRefreshListener(this);
        switch(type){
            case Constants.TYPE_CONTRACT_GROUP:
            case Constants.TYPE_CHECK_SEARCH_GROUP:
                mTvSearchNothing.setText(getResources().getString(R.string.text_search_by_group_name));
                break;
            case Constants.TYPE_CONTRACT_MEMBER:
            case Constants.TYPE_CONTRACT_PDT:
            case Constants.TYPE_CHECK_SEARCH_PC:
            case Constants.TYPE_CHECK_SEARCH_POLICE:
            case Constants.TYPE_CHECK_SEARCH_RECODER:
            case Constants.TYPE_CHECK_SEARCH_HDMI:
            case Constants.TYPE_CHECK_SEARCH_UAV:
            case Constants.TYPE_CHECK_SEARCH_ACCOUNT:
                mTvSearchNothing.setText(getResources().getString(R.string.text_search_by_name_or_number));

                break;
            default:
                mTvSearchNothing.setText(getResources().getString(R.string.text_search_by_name_or_number));
                break;
        }
    }



    @Override
    public void onDestroyView(){
        super.onDestroyView();
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveSearchAccountResultHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveSearchMemberResultHandler);
    }

    public void setBacklistener(BackListener backListener){
        this.backListener = backListener;
    }

    @Override
    public void onItemChildClick(BaseQuickAdapter adapter, View view, int position){
        ContactItemBean contactItemBean = mData.get(position);
        if(contactItemBean.getBean() instanceof Member){
            Member member = (Member) contactItemBean.getBean();
            member.setChecked(!member.isChecked());
            TerminalFactory.getSDK().notifyReceiveHandler(ReceiveMemberSelectedHandler.class, member, !member.isChecked(),TerminalMemberType.getInstanceByCode(member.getType()).toString());
        }else if(contactItemBean.getBean() instanceof Group){
        }
        searchAdapter.notifyDataSetChanged();
    }

    @Override
    public void onLoadMoreRequested(){
        currentPage++;
        if(TextUtils.isEmpty(mEtSearchAllcontacts.getText().toString())){
            ToastUtil.showToast(context, context.getResources().getString(R.string.input_search_content));
            return;
        }
        doSearch(mEtSearchAllcontacts.getText().toString());
    }

    @Override
    public void onRefresh(){
        currentPage = 0;
        mRecyclerview.scrollToPosition(0);
        if(TextUtils.isEmpty(mEtSearchAllcontacts.getText().toString())){
            ToastUtil.showToast(context, context.getResources().getString(R.string.input_search_content));
            return;
        }
        doSearch(mEtSearchAllcontacts.getText().toString());
    }


    public interface BackListener{
        void onBack();
    }
}
