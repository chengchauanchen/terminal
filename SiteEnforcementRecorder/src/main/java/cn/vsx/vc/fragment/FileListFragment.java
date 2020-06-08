package cn.vsx.vc.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.zectec.imageandfileselector.utils.OperateReceiveHandlerUtilSync;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.vsx.vc.R;
import cn.vsx.vc.receiveHandle.ReceiverFragmentPopBackStackHandler;
import cn.vsx.vc.view.MyTabLayout.MyTabLayout;
import ptt.terminalsdk.tools.FileTransgerUtil;

/**
 * Created by gt358 on 2017/10/20.
 */

public class FileListFragment extends Fragment implements View.OnClickListener {

    @Bind(R.id.iv_return)
    ImageView ivReturn;
    @Bind(R.id.tv_title)
    TextView tvTitle;
    @Bind(R.id.iv_close)
    ImageView ivClose;
    @Bind(R.id.tabLayout)
    MyTabLayout tabLayout;

    private List<Fragment> fragments = new ArrayList<>();
    private Fragment lastFragment;
    private Fragment currentFragment;

    public Logger logger = Logger.getLogger(getClass());

    public static FileListFragment newInstance() {
        FileListFragment fragment = new FileListFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_file_list, container, false);
        ButterKnife.bind(this, view);
        initView();
        initListener();
        initData();
        return view;
    }

    /**
     * 初始化布局
     */
    private void initView() {
        ivReturn.setVisibility(View.VISIBLE);
        tvTitle.setText(getString(R.string.text_video_and_picture));
        tvTitle.setPadding(0, 0, 0, 0);
        ivClose.setVisibility(View.INVISIBLE);
    }
    /**
     * 添加监听
     */
    private void initListener() {
    }

    /**
     * 获取数据
     */
    private void initData() {
        FragmentManager childFragmentManager = getChildFragmentManager();
        FragmentTransaction transaction = childFragmentManager.beginTransaction();
        //添加tab
        fragments.clear();
        tabLayout.removeAllTabs();
        //录像
        FileListItemFragment videoFragment = FileListItemFragment.newInstance(FileTransgerUtil.TYPE_VIDEO);
        MyTabLayout.Tab videoTab = tabLayout.newTab();
        videoTab.setText(R.string.text_video);
        fragments.add(videoFragment);
        tabLayout.addTab(videoTab);
        //图片
        FileListItemFragment  pictureFragment = FileListItemFragment.newInstance(FileTransgerUtil.TYPE_IMAGE);
        MyTabLayout.Tab pictureTab = tabLayout.newTab();
        pictureTab.setText(R.string.text_picture);
        fragments.add(pictureFragment);
        tabLayout.addTab(pictureTab);

        //设置模式
        tabLayout.setTabMode(MyTabLayout.MODE_SCROLLABLE);
        tabLayout.setTabGravity(MyTabLayout.GRAVITY_FILL);
        transaction.add(R.id.contacts_viewPager, videoFragment).show(videoFragment).commit();
        lastFragment = videoFragment;

        tabLayout.addOnTabSelectedListener(new MyTabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(MyTabLayout.Tab tab) {
                logger.info("onTabSelected");
                int position = tab.getPosition();
                Fragment currentFrgment = fragments.get(position);
                if (lastFragment != currentFrgment) {
                    tab.setSelected(true);
                    switchFragment(lastFragment, currentFrgment);
                    lastFragment = currentFrgment;
                }
            }

            @Override
            public void onTabUnselected(MyTabLayout.Tab tab) {
                tab.setSelected(false);
                logger.info("onTabUnselected");
            }

            @Override
            public void onTabReselected(MyTabLayout.Tab tab) {
                logger.info("onTabReselected");
            }
        });
    }

    @OnClick({R.id.iv_return})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_return:
                //关闭页面
                OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiverFragmentPopBackStackHandler.class);
                break;
        }
    }


    public void switchFragment(Fragment from, Fragment to) {
        FragmentManager childFragmentManager = getChildFragmentManager();
        if (currentFragment != to) {
            currentFragment = to;
            FragmentTransaction transaction = childFragmentManager.beginTransaction();
            if (!currentFragment.isAdded()) {    // 先判断是否被add过
                transaction.hide(from).add(R.id.contacts_viewPager, to).commit(); // 隐藏当前的fragment，add下一个Fragment
            } else {
                transaction.hide(from).show(to).commit(); // 隐藏当前的fragment，显示下一个
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
        logger.info("FileListFragment---onDestroyView");
    }
}
