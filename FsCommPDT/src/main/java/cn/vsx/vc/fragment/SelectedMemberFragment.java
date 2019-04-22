package cn.vsx.vc.fragment;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.vc.R;
import cn.vsx.vc.adapter.SelectedItemListAdapter;
import cn.vsx.vc.model.ContactItemBean;
import cn.vsx.vc.receiveHandle.ReceiveRemoveSelectedMemberHandler;

public class SelectedMemberFragment extends BaseFragment{
    private static final String DATA = "data";
    private List<ContactItemBean> mData;
    private RecyclerView mRvSelected;
    private ImageView mNewsBarBack;
    private View mNewsBarLine;
    private TextView mBarTitle;
    private ImageView mLeftBtn;
    private ImageView mRightBtn;
    private Button mOkBtn;
    private BackListener backListener;

    public SelectedMemberFragment(){
        // Required empty public constructor
    }

    /**
     */
    public static SelectedMemberFragment newInstance(ArrayList<ContactItemBean> mData){
        SelectedMemberFragment fragment = new SelectedMemberFragment();
        Bundle args = new Bundle();
        args.putSerializable(DATA, mData);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        if(getArguments() != null){
            mData = (List<ContactItemBean>) getArguments().getSerializable(DATA);
        }
    }

    @Override
    public int getContentViewId(){
        return R.layout.layout_selected_member;
    }

    @Override
    public void initView(){
        mNewsBarBack = mRootView.findViewById(R.id.news_bar_back);
        mNewsBarLine = mRootView.findViewById(R.id.news_bar_line);
        mBarTitle = mRootView.findViewById(R.id.bar_title);
        mLeftBtn = mRootView.findViewById(R.id.left_btn);
        mRightBtn = mRootView.findViewById(R.id.right_btn);
        mOkBtn = mRootView.findViewById(R.id.ok_btn);
        mRvSelected = mRootView.findViewById(R.id.rv_selected);
        mBarTitle.setText(R.string.text_choose);
        mLeftBtn.setVisibility(View.GONE);
        mRightBtn.setVisibility(View.GONE);
        mOkBtn.setVisibility(View.GONE);
    }

    @Override
    public void initListener(){
        mNewsBarBack.setOnClickListener(v -> {
            if(backListener !=null){
                backListener.onBack();
            }
        });
    }

    @Override
    public void initData(){
        SelectedItemListAdapter selectedListAdapter = new SelectedItemListAdapter(getContext(),mData);
        selectedListAdapter.setItemClickListener(position -> {
            if(position>=0&&position<mData.size()){
                TerminalFactory.getSDK().notifyReceiveHandler(ReceiveRemoveSelectedMemberHandler.class,mData.get(position).getBean());
                mData.remove(position);
                selectedListAdapter.notifyDataSetChanged();
            }
        });
        mRvSelected.setAdapter(selectedListAdapter);
        mRvSelected.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    public void setBackListener(BackListener backListener){
        this.backListener = backListener;
    }
    public interface BackListener{
        void onBack();
    }
}
