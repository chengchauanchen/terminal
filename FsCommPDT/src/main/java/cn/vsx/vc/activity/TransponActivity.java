package cn.vsx.vc.activity;

import android.content.Intent;

import java.util.ArrayList;

import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveUpdateConfigHandler;
import cn.vsx.vc.R;
import cn.vsx.vc.fragment.SearchFragment;
import cn.vsx.vc.fragment.SelectedMemberFragment;
import cn.vsx.vc.fragment.TransponNewFragment;
import cn.vsx.vc.model.ContactItemBean;
import cn.vsx.vc.model.TransponSelectedBean;
import cn.vsx.vc.receiveHandle.ReceiveShowSearchFragmentHandler;
import cn.vsx.vc.receiveHandle.ReceiveShowSelectedFragmentHandler;
import cn.vsx.vc.receiveHandle.ReceiveTransponBackPressedHandler;
import cn.vsx.vc.utils.Constants;
import ptt.terminalsdk.context.MyTerminalFactory;

public class TransponActivity extends BaseActivity implements TransponNewFragment.BackListener {

    private TransponNewFragment transponNewFragment;
    private int transponType = 0;
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
            getSupportFragmentManager().beginTransaction().hide(transponNewFragment).add(R.id.search_framelayout, selectedMemberFragment).addToBackStack(null).show(selectedMemberFragment).commit();
        }
    };

    private ReceiveShowSearchFragmentHandler receiveShowSearchFragmentHandler = (type,selectedNos) -> {
        SearchFragment searchFragment = SearchFragment.newInstance(type,selectedNos,null);
        searchFragment.setBacklistener(() -> onBackPressed());
        getSupportFragmentManager().beginTransaction().hide(transponNewFragment).add(R.id.search_framelayout, searchFragment).addToBackStack(null).show(searchFragment).commit();
    };

    @Override
    public int getLayoutResId(){
        return R.layout.activity_transpon;
    }

    @Override
    public void initView(){
        transponNewFragment = new TransponNewFragment();
        transponNewFragment.setBacklistener(this);
        getSupportFragmentManager().beginTransaction().add(R.id.search_framelayout, transponNewFragment).show(transponNewFragment).commit();
    }

    @Override
    public void initListener(){
        MyTerminalFactory.getSDK().registReceiveHandler(receiveShowSelectedFragmentHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveShowSearchFragmentHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveUpdateConfigHandler);
    }

    @Override
    public void initData(){
        transponType = getIntent().getIntExtra(Constants.TRANSPON_TYPE,Constants.TRANSPON_TYPE_ONE);
    }

    @Override
    public void doOtherDestroy(){
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveShowSelectedFragmentHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveShowSearchFragmentHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveUpdateConfigHandler);
    }

    @Override
    public void onBackPressed(){
        // TODO: 2019/4/19 返回键和搜索界面返回的处理
        if(transponNewFragment.isVisible()){
            TerminalFactory.getSDK().notifyReceiveHandler(ReceiveTransponBackPressedHandler.class,TransponActivity.this);
        }else{
            super.onBackPressed();
        }
    }

    @Override
    public void onBack() {
        onBackPressed();
    }

    @Override
    public void onResult(ArrayList<ContactItemBean> list) {
        Intent intent = new Intent();
        intent.putExtra(Constants.TRANSPON_SELECTED_BEAN,new TransponSelectedBean(list));
        intent.putExtra(Constants.TRANSPON_TYPE,transponType);
        setResult(RESULT_OK,intent);
        finish();
    }
}
