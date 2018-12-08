package com.zectec.imageandfileselector.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zectec.imageandfileselector.R;
import com.zectec.imageandfileselector.bean.FileInfo;
import com.zectec.imageandfileselector.utils.OperateReceiveHandlerUtilSync;
import com.zectec.imageandfileselector.view.CustomViewPager;

import java.util.ArrayList;
import java.util.List;

import com.zectec.imageandfileselector.receivehandler.ReceiverHideBarForImagePreviewHandler;
import com.zectec.imageandfileselector.receivehandler.ReceiverShowOrHideFragmentHandler;


/**
 * 图片预览界面
 * Created by CWJ on 2017/3/28.
 */

public class ImagePreviewFragment extends Fragment {
    LinearLayout ll_top_bar;
    ImageButton ib_back;
    TextView tv_photo_count;
    private LinearLayout barLayout;
    private boolean isShowBar = true;
    private CustomViewPager viewPager;
    private int position = 0;
    private List<FileInfo> images = new ArrayList<>();

    private View rootView;

    public ImagePreviewFragment(){}
    @SuppressLint("ValidFragment")
    public ImagePreviewFragment (List<FileInfo> images) {
        this.images = images;
    }
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (null == rootView) {
            rootView = inflater.inflate(R.layout.fragment_image_preview, container, false);
        }
        initViewAndEvent();
        registerListener();
        return rootView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        OperateReceiveHandlerUtilSync.getInstance().unregistReceiveHandler(mReceiverHideBarForImagePreviewHandler);
    }

    public void initViewAndEvent() {
        ll_top_bar = (LinearLayout) rootView.findViewById(R.id.ll_top_bar2);
        ib_back = (ImageButton) rootView.findViewById(R.id.ib_back);
        tv_photo_count = (TextView) rootView.findViewById(R.id.tv_photo_count);
        barLayout = (LinearLayout) rootView.findViewById(R.id.bar_layout);
        tv_photo_count.setText((position + 1) + "/" + images.size());
        viewPager = (CustomViewPager) rootView.findViewById(R.id.preview_pager);
        viewPager.setAdapter(new SimpleFragmentAdapter(getChildFragmentManager()));
        viewPager.setCurrentItem(position);
        viewPager.setIsPagingEnabled(true);
        registerListener();
    }

    public class SimpleFragmentAdapter extends FragmentPagerAdapter {
        ImagePreviewItemFragment mFragment;

        public SimpleFragmentAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
//            mFragment = ImagePreviewItemFragment.getInstance(images.get(position).getFilePath());
            return null;
        }

        @Override
        public int getCount() {
            return images.size();
        }
    }

    public void registerListener() {
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                tv_photo_count.setText(position + 1 + "/" + images.size());
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
        ib_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                finish();
                OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiverShowOrHideFragmentHandler.class, "ImagePreviewItemFragment", false);
                getFragmentManager().popBackStack();
            }
        });
        OperateReceiveHandlerUtilSync.getInstance().registReceiveHandler(mReceiverHideBarForImagePreviewHandler);
    }

    /**
     *显示和隐藏标题栏和状态栏
     */
    public void switchBarVisibility() {
        barLayout.setVisibility(isShowBar ? View.GONE : View.VISIBLE);
        ll_top_bar.setVisibility(isShowBar ? View.GONE : View.VISIBLE);
        if (isShowBar) {
            hideStatusBar();
        } else {
            showStatusBar();
        }
        isShowBar = !isShowBar;
    }

    private void hideStatusBar() {
        WindowManager.LayoutParams attrs = getActivity().getWindow().getAttributes();
        attrs.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
        getActivity().getWindow().setAttributes(attrs);
    }

    private void showStatusBar() {
        WindowManager.LayoutParams attrs = getActivity().getWindow().getAttributes();
        attrs.flags &= ~WindowManager.LayoutParams.FLAG_FULLSCREEN;
        getActivity().getWindow().setAttributes(attrs);
    }

    private ReceiverHideBarForImagePreviewHandler mReceiverHideBarForImagePreviewHandler = new ReceiverHideBarForImagePreviewHandler() {
        @Override
        public void handler() {
            switchBarVisibility();
        }
    };
}
