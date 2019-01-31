package com.zectec.imageandfileselector.fragment;

import android.annotation.SuppressLint;
import android.os.Build;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.zectec.imageandfileselector.R;
import com.zectec.imageandfileselector.adapter.TabPagerAdapter;
import com.zectec.imageandfileselector.base.BaseFragment;
import com.zectec.imageandfileselector.base.Constant;
import com.zectec.imageandfileselector.bean.FileInfo;
import com.zectec.imageandfileselector.receivehandler.ReceiverSendFileHandler;
import com.zectec.imageandfileselector.receivehandler.ReceiverShowOrHideFragmentHandler;
import com.zectec.imageandfileselector.utils.FileUtil;
import com.zectec.imageandfileselector.utils.OperateReceiveHandlerUtilSync;
import com.zectec.imageandfileselector.utils.SystemUtil;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiverCheckFileHandler;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

/**
 * Created by CWJ on 2017/3/28.
 */

public class LocalMainFragment extends BaseFragment implements View.OnClickListener {
    TabLayout mTabLayout;
    ViewPager mViewPager;
    TextView tv_all_size;
    TextView tv_send;
    TextView tv_cancel;
    TextView tv_preview;
    List<FileInfo> mListphoto;
    private List<String> mTitleList = new ArrayList<>();
    private List<Fragment> fragments = new ArrayList<>();

    void tv_preview() {
        if (mListphoto.size() != 0) {
            OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiverShowOrHideFragmentHandler.class, "ImagePreviewFragment", true);
        }
    }

    public LocalMainFragment(){}
    @SuppressLint("ValidFragment")
    public LocalMainFragment (List<FileInfo> mListphoto) {
        this.mListphoto = mListphoto;
    }

    @Override
    public boolean isBindEventBusHere() {
        return true;
    }

    @Override
    public int getLayoutResource() {
        return R.layout.fragment_main_local;
    }

    @Override
    public void initView() {
        mTabLayout = (TabLayout) rootView.findViewById(R.id.tl_myfile);
        mViewPager = (ViewPager) rootView.findViewById(R.id.vp_myfile);
        tv_all_size = (TextView) rootView.findViewById(R.id.tv_all_size);
        tv_send = (TextView) rootView.findViewById(R.id.tv_send);
        tv_cancel = (TextView) rootView.findViewById(R.id.tv_cancel);
        tv_preview = (TextView) rootView.findViewById(R.id.tv_preview);
        tv_preview.setOnClickListener(this);

        tv_all_size.setText(getString(R.string.size, "0B"));
        tv_send.setText(getString(R.string.send, "0"));
        updateSizAndCount();
        mTitleList.add(getString(R.string.text_video_and_sound));
        mTitleList.add(getString(R.string.text_picture));
        mTitleList.add(getString(R.string.text_document));
        mTitleList.add(getString(R.string.text_other));

        fragments.add(new AVFragment());
        fragments.add(new PhotoFragment());
        fragments.add(new DocFragment());
        fragments.add(new OtherFragment());
        TabPagerAdapter mAdapter = new TabPagerAdapter(getChildFragmentManager(), mTitleList, fragments);
        mViewPager.setAdapter(mAdapter);
        mViewPager.setCurrentItem(0);
        mViewPager.setOffscreenPageLimit(4);
        mTabLayout.setupWithViewPager(mViewPager);
        mTabLayout.setTabsFromPagerAdapter(mAdapter);
        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(mTabLayout));
        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mViewPager.setCurrentItem(tab.getPosition(), false);
                OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiverCheckFileHandler.class);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        //反射修改宽度
//        setUpIndicatorWidth(mTabLayout);
        OperateReceiveHandlerUtilSync.getInstance().registReceiveHandler(mReceiverCheckFileHandler);

//        tv_send.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiverSendFileHandler.class, ReceiverSendFileHandler.FILE);
//            }
//        });
        tv_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Constant.files.clear();
                OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiverSendFileHandler.class, ReceiverSendFileHandler.FILE);
            }
        });
    }

    public void updateSizAndCount() {
        mListphoto.clear();
        final Set<Map.Entry<String, FileInfo>> entries = Constant.files.entrySet();
        for (Map.Entry<String, FileInfo> entry : entries) {
            if (entry.getValue().getIsPhoto()) {
                mListphoto.add(entry.getValue());
            }
        }
        if (mListphoto.size() == 0) {
            tv_preview.setBackgroundResource(R.drawable.shape_bt_send);
            tv_preview.setTextColor(getResources().getColor(R.color.md_grey_700));
            tv_preview.setVisibility(View.GONE);
        } else {
            tv_preview.setBackgroundResource(R.drawable.shape_bt_send_blue);
            tv_preview.setTextColor(getResources().getColor(R.color.md_white_1000));
//            tv_preview.setVisibility(View.VISIBLE);
        }
        if (entries.size() == 0) {
            tv_send.setBackgroundResource(R.drawable.shape_bt_send);
            tv_send.setTextColor(getResources().getColor(R.color.md_grey_700));
            tv_all_size.setText(getString(R.string.size, "0B"));
            tv_send.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (entries.size() == 0){
                        Toast.makeText(getActivity(),R.string.text_please_select_at_least_one_file,Toast.LENGTH_SHORT).show();
                    }else{
                        OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiverSendFileHandler.class, ReceiverSendFileHandler.FILE);
                    }
                }
            });
        } else {
            tv_send.setBackgroundResource(R.drawable.shape_bt_send_blue);
            tv_send.setTextColor(getResources().getColor(R.color.md_white_1000));
            long count = 0L;
            for (Map.Entry<String, FileInfo> entry : entries) {
                count = count + entry.getValue().getFileSize();
            }
            tv_send.setEnabled(true);
            tv_all_size.setText(getString(R.string.size, FileUtil.FormetFileSize(count)));
        }
        tv_send.setText(getString(R.string.send, "" + entries.size()));
    }

    private void setUpIndicatorWidth(TabLayout mTabLayout) {
        Class<?> tabLayoutClass = mTabLayout.getClass();
        Field tabStrip = null;
        try {
            tabStrip = tabLayoutClass.getDeclaredField("mTabStrip");
            tabStrip.setAccessible(true);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }

        LinearLayout layout = null;
        try {
            if (tabStrip != null) {
                layout = (LinearLayout) tabStrip.get(mTabLayout);
            }
            if (layout ==null)
                return;
            for (int i = 0; i < layout.getChildCount(); i++) {
                View child = layout.getChildAt(i);
                child.setPadding(0, 0, 0, 0);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    params.setMarginStart(SystemUtil.dp(30f));
                    params.setMarginEnd(SystemUtil.dp(30f));
                }
                child.setLayoutParams(params);
                child.invalidate();
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.tv_preview) {
            tv_preview();
        }
    }

    @Override
    public void doOtherDestory() {
        super.doOtherDestory();
        OperateReceiveHandlerUtilSync.getInstance().unregistReceiveHandler(mReceiverCheckFileHandler);
    }

    /**选中文件发送改变*/
    private ReceiverCheckFileHandler mReceiverCheckFileHandler = new ReceiverCheckFileHandler() {
        @Override
        public void handler() {
            rx.Observable.just("")
                    .subscribeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Action1<String>() {
                        @Override
                        public void call(String s) {
                            updateSizAndCount();
                        }
                    });
        }
    };
}
