package cn.vsx.vc.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;

import java.util.ArrayList;

import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveUpdateConfigHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveUpdatePhoneMemberHandler;
import cn.vsx.vc.R;
import cn.vsx.vc.fragment.EstablishTempGroupFragment;
import cn.vsx.vc.fragment.SearchFragment;
import cn.vsx.vc.fragment.SelectedMemberFragment;
import cn.vsx.vc.model.ContactItemBean;
import cn.vsx.vc.receiveHandle.ReceiveShowSearchFragmentHandler;
import cn.vsx.vc.receiveHandle.ReceiveShowSelectedFragmentHandler;
import ptt.terminalsdk.context.MyTerminalFactory;

public class IncreaseTemporaryGroupMemberActivity extends BaseActivity{

    private Handler myHandler = new Handler();
    private EstablishTempGroupFragment establishTempGroupFragment;
    //更新警务通成员信息
    private ReceiveUpdatePhoneMemberHandler receiveUpdatePhoneMemberHandler = allMembers -> myHandler.post(() -> {
        //        MemberResponse memberResponse = TerminalFactory.getSDK().getConfigManager().getPhoneMemeberInfo();
        //        List<CatalogBean> catalogBeanList = new ArrayList<>();
        //        CatalogBean bean = new CatalogBean();
        //        bean.setName(memberResponse.getTitleName());
        //        bean.setBean(memberResponse);
        //        catalogBeanList.add(bean);
        //        updateData(memberResponse,catalogBeanList);
    });

    /**
     * 更新配置信息
     */
    private ReceiveUpdateConfigHandler receiveUpdateConfigHandler = () -> {//更新当前组
    };

    private ReceiveShowSelectedFragmentHandler receiveShowSelectedFragmentHandler = new ReceiveShowSelectedFragmentHandler(){
        @Override
        public void handler(ArrayList<ContactItemBean> selectedContacts){
            SelectedMemberFragment selectedMemberFragment = SelectedMemberFragment.newInstance(selectedContacts);
            selectedMemberFragment.setBackListener(() -> onBackPressed());
            getSupportFragmentManager().beginTransaction().hide(establishTempGroupFragment).add(R.id.search_framelayout, selectedMemberFragment).addToBackStack(null).show(selectedMemberFragment).commit();
        }
    };

    private ReceiveShowSearchFragmentHandler receiveShowSearchFragmentHandler = (type,selectedNos) -> {
        SearchFragment searchFragment = SearchFragment.newInstance(type,selectedNos);
        searchFragment.setBacklistener(() -> onBackPressed());
        getSupportFragmentManager().beginTransaction().hide(establishTempGroupFragment).add(R.id.search_framelayout, searchFragment).addToBackStack(null).show(searchFragment).commit();
    };

    @Override
    public int getLayoutResId(){
        return R.layout.activity_increase_temporary_group_member;
    }

    @Override
    public void initView(){
        int type = getIntent().getIntExtra("type", 1);
        int groupId = getIntent().getIntExtra("groupId", 0);
        establishTempGroupFragment = EstablishTempGroupFragment.newInstance(type, groupId);
        getSupportFragmentManager().beginTransaction().add(R.id.search_framelayout, establishTempGroupFragment).show(establishTempGroupFragment).commit();
    }

    @Override
    public void initListener(){
        MyTerminalFactory.getSDK().registReceiveHandler(receiveShowSelectedFragmentHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveShowSearchFragmentHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveUpdatePhoneMemberHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveUpdateConfigHandler);
    }

    @Override
    public void initData(){
    }

    @Override
    public void doOtherDestroy(){
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveShowSelectedFragmentHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveShowSearchFragmentHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveUpdatePhoneMemberHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveUpdateConfigHandler);
    }

    public static void startActivity(Context context, int type, int groupId){
        Intent intent = new Intent(context, IncreaseTemporaryGroupMemberActivity.class);
        intent.putExtra("type", type);
        intent.putExtra("groupId", groupId);
        context.startActivity(intent);
    }

    @Override
    public void onBackPressed(){
        // TODO: 2019/4/19 返回键和搜索界面返回的处理
        super.onBackPressed();
    }
}
