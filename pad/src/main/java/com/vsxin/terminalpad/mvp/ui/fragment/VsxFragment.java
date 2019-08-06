package com.vsxin.terminalpad.mvp.ui.fragment;

import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RadioButton;

import com.ixiaoma.xiaomabus.architecture.mvp.lifecycle.MvpFragment;
import com.vsxin.terminalpad.R;
import com.vsxin.terminalpad.mvp.contract.constant.FragmentTagConstants;
import com.vsxin.terminalpad.mvp.contract.presenter.MainPresenter;
import com.vsxin.terminalpad.mvp.contract.view.IMainView;

import butterknife.BindView;

/**
 * @author app模块
 */
public class VsxFragment extends MvpFragment<IMainView, MainPresenter> implements IMainView {

    @BindView(R.id.tab_content)
    FrameLayout tab_content;

    @BindView(R.id.btn_message_page)
    RadioButton btn_message_page;

    @BindView(R.id.btn_contects_page)
    RadioButton btn_contects_page;

    @BindView(R.id.btn_mine_page)
    RadioButton btn_mine_page;

    private MessageListFragment messageListFragment;
    private ContactsFragment contactsFragment;
    private MeFragment meFragment;


    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_vsx;
    }

    @Override
    protected void initViews(View view) {
        initFragment();
    }

    @Override
    protected void initData() {
        switchFragment(0);
    }

    /**
     * 初始化Fragment
     */
    private void initFragment() {
        btn_message_page.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchFragment(0);//消息
            }
        });
        btn_contects_page.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchFragment(1);//通信录
            }
        });
        btn_mine_page.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchFragment(2);//我
            }
        });
    }


    private void setChecked(int currentPage) {
        switch (currentPage) {
            case 0:
                btn_message_page.setChecked(true);
                btn_contects_page.setChecked(false);
                btn_mine_page.setChecked(false);
                break;
            case 1:
                btn_message_page.setChecked(false);
                btn_contects_page.setChecked(true);
                btn_mine_page.setChecked(false);
                break;
            case 2:
                btn_message_page.setChecked(false);
                btn_contects_page.setChecked(false);
                btn_mine_page.setChecked(true);
                break;
            default:
                break;
        }
    }


    public void switchFragment(int currentPage) {
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        hideFragments(transaction);
        setChecked(currentPage);
        switch (currentPage) {
            case 0:
                if (messageListFragment == null) {
                    messageListFragment = new MessageListFragment();
                    transaction.add(R.id.tab_content, messageListFragment, FragmentTagConstants.MESSAGE);
                } else {
                    transaction.show(messageListFragment);
                }
                break;
            case 1:
                if (contactsFragment == null) {
                    contactsFragment = new ContactsFragment();
                    transaction.add(R.id.tab_content, contactsFragment, FragmentTagConstants.CONTACTS);
                } else {
                    transaction.show(contactsFragment);
                }
                break;
            case 2:
                if (meFragment == null) {
                    meFragment = new MeFragment();
                    transaction.add(R.id.tab_content, meFragment, FragmentTagConstants.ME);
                } else {
                    transaction.show(meFragment);
                }
                break;
            default:
                break;

        }
        transaction.commitAllowingStateLoss();
    }

    private void hideFragments(FragmentTransaction transaction) {
        if (messageListFragment != null) {
            transaction.hide(messageListFragment);
        }
        if (contactsFragment != null) {
            transaction.hide(contactsFragment);
        }
        if (meFragment != null) {
            transaction.hide(meFragment);
        }
    }

    @Override
    public MainPresenter createPresenter() {
        return new MainPresenter(getContext());
    }
}
