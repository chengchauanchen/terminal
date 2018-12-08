package com.zectec.imageandfileselector.fragment;

import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import java.util.ArrayList;
import java.util.List;

import com.zectec.imageandfileselector.R;
import com.zectec.imageandfileselector.adapter.TabPagerAdapter;
import com.zectec.imageandfileselector.base.BaseFragment;
import com.zectec.imageandfileselector.base.Constant;
import com.zectec.imageandfileselector.bean.FileInfo;
import com.zectec.imageandfileselector.utils.FileUtil;

import com.zectec.imageandfileselector.receivehandler.ReceiverShowOrHideFragmentHandler;
import com.zectec.imageandfileselector.utils.OperateReceiveHandlerUtilSync;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

/**
 * 
 */
public class FileMainFragment extends BaseFragment {
    private List<String> mTitleList = new ArrayList<>();
    private List<Fragment> fragments = new ArrayList<>();
    ViewPager main_viewpager;
    RadioGroup main_top_rg;
    RadioButton top_rg_a;
    RadioButton top_rg_b;

    List<FileInfo> mListphoto = new ArrayList<>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public boolean shieldBack;

    @Override
    protected boolean isBindEventBusHere() {
        return false;
    }

    @Override
    public int getLayoutResource() {
        Constant.files.clear();
        return R.layout.fragment_file_main;
    }

    @Override
    public void initView() {
        OperateReceiveHandlerUtilSync.getInstance().registReceiveHandler(mReceiverShowOrHideFragmentHandler);
        main_viewpager = (ViewPager) rootView.findViewById(R.id.main_viewpager);
        main_top_rg = (RadioGroup) rootView.findViewById(R.id.main_top_rg);
        top_rg_a = (RadioButton) rootView.findViewById(R.id.top_rg_a);
        top_rg_b = (RadioButton) rootView.findViewById(R.id.top_rg_b);

//        //6.0权限适配
//        requestReadAndWriteSDPermission(new baseActivity.PermissionHandler() {
//            @Override
//            public void onGranted() {
//                Alerter.create(.this)
//                        .setTitle("通知")
//                        .setText("谢谢您打开权限！")
//                        .show();
//            }
//        });
        Log.e("cwj", "外置SD卡路径 = " + FileUtil.getStoragePath(getContext()));
        Log.e("cwj", "内置SD卡路径 = " + Environment.getExternalStorageDirectory().getAbsolutePath());
        Log.e("cwj", "手机内存根目录路径  = " + Environment.getDataDirectory().getParentFile().getAbsolutePath());
        fragments.add(new AllMainFragment());//全部
        fragments.add(new LocalMainFragment(mListphoto));//本机
        TabPagerAdapter pagerAdapter = new TabPagerAdapter(getChildFragmentManager(), mTitleList, fragments);
        main_viewpager.setAdapter(pagerAdapter);
        main_top_rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == top_rg_a.getId())
                    main_viewpager.setCurrentItem(0);
                else if (checkedId == top_rg_b.getId())
                    main_viewpager.setCurrentItem(1);
            }
        });

        //设置默认选中页
        main_viewpager.setCurrentItem(0);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        OperateReceiveHandlerUtilSync.getInstance().unregistReceiveHandler(mReceiverShowOrHideFragmentHandler);
    }

    @Override
    public void doOtherDestory() {
        super.doOtherDestory();
    }

    /**
     * Fragment的显示和隐藏
     */
    private ReceiverShowOrHideFragmentHandler mReceiverShowOrHideFragmentHandler = new ReceiverShowOrHideFragmentHandler() {
        @Override
        public void handler(final String FragmentName, final boolean show) {
            Observable.just("")
                    .subscribeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Action1<String>() {
                        @Override
                        public void call(String s) {
                            if("ImagePreviewFragment".equals(FragmentName)) {

                            }
                            if("手机内存".equals(FragmentName)) {
                                if(show) {
                                    getActivity().getSupportFragmentManager().beginTransaction().addToBackStack(null).
                                            replace(R.id.fl_fragment_container_filemain, new SDCardFragment(Environment.getDataDirectory().getParentFile().getAbsolutePath(), "手机内存"), "SDCardFragment")
                                            .commit();
                                }
                                shieldBack = show;
                            }
                            else if("扩展卡内存".equals(FragmentName)) {
                                if(show) {
                                    getActivity().getSupportFragmentManager().beginTransaction().addToBackStack(null).
                                            replace(R.id.fl_fragment_container_filemain, new SDCardFragment(FileUtil.getStoragePath(getActivity()), "扩展卡内存"), "SDCardFragment")
                                            .commit();
                                }
                                shieldBack = show;

                            }
                            else if("SD卡".equals(FragmentName)) {
                                if(show) {
                                    getActivity().getSupportFragmentManager().beginTransaction().addToBackStack(null).
                                            replace(R.id.fl_fragment_container_filemain, new SDCardFragment(Environment.getExternalStorageDirectory().getAbsolutePath(), "SD卡"), "SDCardFragment")
                                            .commit();
                                }
                                shieldBack = show;

                            }

                        }
                    });

        }
    };

    public void popBackStack () {
        getFragmentManager().popBackStack();
    }

}
