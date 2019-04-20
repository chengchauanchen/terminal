package cn.vsx.vc.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.tools.Params;
import cn.vsx.vc.R;
import cn.vsx.vc.activity.CreateTemporaryGroupsActivity;
import cn.vsx.vc.utils.DataUtil;
import ptt.terminalsdk.context.MyTerminalFactory;
import ptt.terminalsdk.tools.ToastUtil;

/**
 * 创建临时组
 */
public class EstablishTempGroupFragment extends Fragment implements View.OnClickListener{
    private static final String TYPE = "type";
    private static final String GROUP_ID = "groupId";

    private int type;
    private int groupId;

    private ImageView mNewsBarBack;
    private View mNewsBarLine;
    private TextView mBarTitle;
    private ImageView mLeftBtn;
    private ImageView mRightBtn;
    private Button mOkBtn;
    private FrameLayout mContainer;
    private int CREATE_TEMP_GROUP = 0;
    private int INCREASE_MEMBER = 1;
    private TempGroupMemberFragment tempGroupMemberFragment;

    public EstablishTempGroupFragment(){
        // Required empty public constructor
    }

    public static EstablishTempGroupFragment newInstance(int type, int groupId){
        EstablishTempGroupFragment fragment = new EstablishTempGroupFragment();
        Bundle args = new Bundle();
        args.putInt(TYPE, type);
        args.putInt(GROUP_ID, groupId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        if(getArguments() != null){
            type = getArguments().getInt(TYPE);
            groupId = getArguments().getInt(GROUP_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_establish_temp_group, container, false);
        findView(view);
        initView();
        initListener();
        initData();
        return view;
    }

    private void findView(View view){
        mNewsBarBack = view.findViewById(R.id.news_bar_back);
        mBarTitle = view.findViewById(R.id.bar_title);
        mLeftBtn = view.findViewById(R.id.left_btn);
        mRightBtn = view.findViewById(R.id.right_btn);
        mOkBtn = view.findViewById(R.id.ok_btn);
        mContainer = view.findViewById(R.id.container);
    }

    private void initView(){
        //titlebar初始化
        if(type == CREATE_TEMP_GROUP){
            mBarTitle.setText(R.string.text_create_temporary_groups);
            mOkBtn.setText(R.string.text_next);
        }else if(type == INCREASE_MEMBER){
            mBarTitle.setText(R.string.text_add_group_member);
            mOkBtn.setText(R.string.text_sure);
        }
        mRightBtn.setVisibility(View.GONE);
    }

    private void initListener(){
        mNewsBarBack.setOnClickListener(this);
        mOkBtn.setOnClickListener(this);
    }

    private void initData(){
        tempGroupMemberFragment = TempGroupMemberFragment.newInstance();
        getChildFragmentManager().beginTransaction().add(R.id.container, tempGroupMemberFragment).show(tempGroupMemberFragment).commit();
    }

    @Override
    public void onClick(View view){
        switch(view.getId()){
            case R.id.news_bar_back:
                getActivity().finish();
                break;
            case R.id.ok_btn:
                if(tempGroupMemberFragment.getSelectedMember().isEmpty()){
                    ToastUtil.showToast(getContext(), getString(R.string.text_add_at_least_one_member));
                    return;
                }
                if(type == CREATE_TEMP_GROUP){
                    CreateTemporaryGroupsActivity.startActivity(getContext(), tempGroupMemberFragment.getSelectedMember());
                }else if(type == INCREASE_MEMBER){
                    MyTerminalFactory.getSDK().getTempGroupManager().addMemberToTempGroup(groupId, MyTerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0), TerminalFactory.getSDK().getParam(Params.MEMBER_UNIQUENO, 0L), DataUtil.getUniqueNos(tempGroupMemberFragment.getSelectedMember()));
                }
                break;
            default:
                break;
        }
    }
}
