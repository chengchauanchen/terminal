package cn.vsx.vc.fragment;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.zectec.imageandfileselector.utils.OperateReceiveHandlerUtilSync;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.vsx.vc.R;
import cn.vsx.vc.adapter.AppListAdapter;
import cn.vsx.vc.receiveHandle.ReceiverFragmentPopBackStackHandler;

public class AppListFragment extends Fragment implements View.OnClickListener {


    @Bind(R.id.iv_return)
    ImageView ivReturn;
    @Bind(R.id.tv_title)
    TextView tvTitle;
    @Bind(R.id.tv_sure)
    TextView tvSure;
    @Bind(R.id.rd_recyclerView)
    RecyclerView rdRecyclerView;

    private AppListAdapter adapter;
    private List<PackageInfo> data = new ArrayList<>();

    private Handler mHandler = new Handler(Looper.getMainLooper());

    public static AppListFragment newInstance() {
        AppListFragment fragment = new AppListFragment();
        Bundle args = new Bundle();
//        args.putInt(TYPE, type);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
//            jumpType = getArguments().getInt(TYPE);
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_app_list, container, false);
        ButterKnife.bind(this, view);
        initData();
        initView();
        return view;
    }
    /**
     * 初始化数据
     */
    private void initData() {
        data.clear();
        String packageName = getActivity().getPackageName();
        PackageManager pm = this.getContext().getPackageManager();
        List<PackageInfo> packages = pm.getInstalledPackages(0);
        for (PackageInfo info : packages) {
            if (info != null) {
                if (!TextUtils.equals(info.packageName, packageName)) {
                    data.add(info);
                }
            }
        }
    }

    /**
     * 初始化布局
     */
    private void initView() {
        ivReturn.setVisibility(View.VISIBLE);
        tvTitle.setText(getString(R.string.text_app_list));
        tvSure.setVisibility(View.GONE);

        rdRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        adapter = new AppListAdapter(getContext(), data);
        rdRecyclerView.setAdapter(adapter);
        adapter.setOnItemClickListener(bean -> {
            if (data != null && bean != null) {
                String packageName = bean.packageName;
                if (!TextUtils.isEmpty(packageName)) {
                    Intent minIntent = getContext().getPackageManager().getLaunchIntentForPackage(packageName);
                    startActivity(minIntent);
                }
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


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
        mHandler.removeCallbacksAndMessages(null);
    }
}
