package com.vsxin.terminalpad.utils;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.vsxin.terminalpad.R;
import com.vsxin.terminalpad.mvp.ui.activity.MainMapActivity;

public class FragmentManage {

    /**
     * 打开Fragment
     * @param fragmentActivity
     * @param newFragment
     */
    public static void startFragment(FragmentActivity fragmentActivity, Fragment newFragment) {
        //拿到fragment的manager对象
        FragmentManager supportFragmentManager = fragmentActivity.getSupportFragmentManager();
        //事务(防止花屏)
        FragmentTransaction fragmentTransaction = supportFragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fl_vsx, newFragment);
        fragmentTransaction.addToBackStack(fragmentActivity.getClass().getName());
        fragmentTransaction.commit();
    }

    /**
     * 回退到之前栈
     * @param fragmentActivity
     */
    public static void finishFragment(FragmentActivity fragmentActivity){
        fragmentActivity.getSupportFragmentManager().popBackStack();
    }

    /**
     * 返回主页
     * 回退栈中VsxFragment之上的所有Fragment
     * @param fragmentActivity
     */
    public static void startVsxFragment(FragmentActivity fragmentActivity){
        FragmentManager fragmentManager = fragmentActivity.getSupportFragmentManager();
        fragmentManager.popBackStackImmediate(
                MainMapActivity.class.getName(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
    }

}
