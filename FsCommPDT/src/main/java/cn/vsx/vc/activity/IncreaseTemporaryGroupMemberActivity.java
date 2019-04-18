package cn.vsx.vc.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.OnClick;
import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveUpdateConfigHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveUpdatePhoneMemberHandler;
import cn.vsx.hamster.terminalsdk.tools.Params;
import cn.vsx.vc.R;
import cn.vsx.vc.fragment.TempGroupMemberFragment;
import ptt.terminalsdk.context.MyTerminalFactory;
import ptt.terminalsdk.tools.ToastUtil;

public class IncreaseTemporaryGroupMemberActivity extends BaseActivity{

    @Bind(R.id.ok_btn)
    Button okBtn;
    @Bind(R.id.bar_title)
    TextView barTitle;
    @Bind(R.id.right_btn)
    ImageView rightBtn;

    private int CREATE_TEMP_GROUP = 0;
    private int INCREASE_MEMBER = 1;

    private Handler myHandler = new Handler();
    //更新警务通成员信息
    private ReceiveUpdatePhoneMemberHandler receiveUpdatePhoneMemberHandler = allMembers -> myHandler.post(() -> {
        //        MemberResponse memberResponse = TerminalFactory.getSDK().getConfigManager().getPhoneMemeberInfo();
        //        List<CatalogBean> catalogBeanList = new ArrayList<>();
        //        CatalogBean bean = new CatalogBean();
        //        bean.setName(memberResponse.getName());
        //        bean.setBean(memberResponse);
        //        catalogBeanList.add(bean);
        //        updateData(memberResponse,catalogBeanList);
    });

    /**
     * 更新配置信息
     */
    private ReceiveUpdateConfigHandler receiveUpdateConfigHandler = () -> {//更新当前组
    };

    private int type;
    private int groupId;
    private TempGroupMemberFragment tempGroupMemberFragment;

    @Override
    public int getLayoutResId(){
        return R.layout.activity_increase_temporary_group_member;
    }

    @Override
    public void initView(){
        type = getIntent().getIntExtra("type", 1);
        groupId = getIntent().getIntExtra("groupId", 0);
        //titlebar初始化
        if(type == CREATE_TEMP_GROUP){
            barTitle.setText(R.string.text_create_temporary_groups);
            okBtn.setText(R.string.text_next);
        }else if(type == INCREASE_MEMBER){
            barTitle.setText(R.string.text_add_group_member);
            okBtn.setText(R.string.text_sure);
        }
        rightBtn.setVisibility(View.GONE);
    }

    @Override
    public void initListener(){
        MyTerminalFactory.getSDK().registReceiveHandler(receiveUpdatePhoneMemberHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveUpdateConfigHandler);
    }

    @Override
    public void initData(){
        tempGroupMemberFragment = TempGroupMemberFragment.newInstance(type);
        getSupportFragmentManager().beginTransaction().add(R.id.container, tempGroupMemberFragment).show(tempGroupMemberFragment).commit();
    }

    @Override
    public void doOtherDestroy(){
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveUpdatePhoneMemberHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveUpdateConfigHandler);
    }

    @OnClick({R.id.news_bar_back, R.id.ok_btn})
    public void onClick(View view){
        switch(view.getId()){
            case R.id.news_bar_back:
                finish();
                break;
            case R.id.ok_btn:
                if(tempGroupMemberFragment.getSelectedMember().isEmpty()){
                    ToastUtil.showToast(IncreaseTemporaryGroupMemberActivity.this,getString(R.string.text_add_at_least_one_member));
                    return;
                }
                if(type == CREATE_TEMP_GROUP){
                    ArrayList<Integer> list=new ArrayList<>(tempGroupMemberFragment.getSelectedMemberNo());
                    CreateTemporaryGroupsActivity.startActivity(this,list);
                }else if(type == INCREASE_MEMBER){
                    MyTerminalFactory.getSDK().getTempGroupManager().addMemberToTempGroup(groupId,MyTerminalFactory.getSDK().getParam(Params.MEMBER_ID,0), TerminalFactory.getSDK().getParam(Params.MEMBER_UNIQUENO,0L),tempGroupMemberFragment.getSelectedMember());
                }
                break;
            default:
                break;
        }
    }

    public static void startActivity(Context context, int type, int groupId){
        Intent intent = new Intent(context, IncreaseTemporaryGroupMemberActivity.class);
        intent.putExtra("type", type);
        intent.putExtra("groupId", groupId);
        context.startActivity(intent);
    }
}
