package cn.vsx.vc.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.tools.Params;
import cn.vsx.vc.R;
import cn.vsx.vc.adapter.SetLivingStopTimeAdapter;
import cn.vsx.vc.model.SetLivingStopTimeBean;
import cn.vsx.vc.receiveHandle.ReceiveResponseSetLivingTimeMessageUIHandler;
import cn.vsx.vc.receiveHandle.ReceiverFragmentPopBackStackHandler;
import ptt.terminalsdk.context.MyTerminalFactory;
import ptt.terminalsdk.tools.AppUtil;
import ptt.terminalsdk.tools.StringUtil;
import ptt.terminalsdk.tools.ToastUtil;

public class SetLivingStopTimeFragment extends Fragment implements View.OnClickListener {


    @Bind(R.id.iv_return)
    ImageView ivReturn;
    @Bind(R.id.tv_title)
    TextView tvTitle;
    @Bind(R.id.tv_sure)
    TextView tvSure;
    @Bind(R.id.rd_recyclerView)
    RecyclerView rdRecyclerView;

    private SetLivingStopTimeAdapter adapter;
    private List<SetLivingStopTimeBean> data = new ArrayList<>();

    private Handler mHandler = new Handler(Looper.getMainLooper());

    public static SetLivingStopTimeFragment newInstance() {
        SetLivingStopTimeFragment fragment = new SetLivingStopTimeFragment();
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
        View view = inflater.inflate(R.layout.fragment_set_living_stop_time, container, false);
        ButterKnife.bind(this, view);
        initData();
        initView();
        initListener();
        return view;
    }

    /**
     * 初始化数据
     */
    private void initData() {
        data.clear();
        long intervalTime =  TerminalFactory.getSDK().getParam(Params.MAX_LIVING_TIME, 0L);
        int[] times = getResources().getIntArray(R.array.living_stop_time);
        for (int time: times) {
            SetLivingStopTimeBean bean =  new SetLivingStopTimeBean();
            bean.setTime(time);
            bean.setChecked(StringUtil.secondsToHour(intervalTime) == time);
            data.add(bean);
        }
    }

    /**
     * 初始化布局
     */
    private void initView() {
        ivReturn.setVisibility(View.VISIBLE);
        tvTitle.setText(getString(R.string.text_set_living_stop_time_title));

        rdRecyclerView.setLayoutManager(new GridLayoutManager(getContext(),AppUtil.getScreenOriention(this.getContext())?3:2));
        adapter = new SetLivingStopTimeAdapter(getContext(), data);
        rdRecyclerView.setAdapter(adapter);
        adapter.setOnItemClickListener(bean -> {
            if(data!=null&&bean!=null){
                for (SetLivingStopTimeBean b: data) {
                    if(b!=null){
                        b.setChecked(bean.getTime()==b.getTime());
                    }
                }
            }
            if(adapter!=null){
                adapter.notifyDataSetChanged();
            }
        });
    }

    /**
     * 初始化监听
     */
    private void initListener() {
        MyTerminalFactory.getSDK().registReceiveHandler(receiveResponseSetLivingTimeMessageUIHandler);
    }

    @OnClick({R.id.iv_return,R.id.tv_title, R.id.tv_sure})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_return:
                //关闭页面
                OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiverFragmentPopBackStackHandler.class);
                break;
            case R.id.tv_title:
                //确定
//                TerminalFactory.getSDK().getThreadPool().execute(() -> {
//                    TerminalFactory.getSDK().getLiveManager().requestSetLivingTimeMessage(60*7,false);
//                });
                break;
            case R.id.tv_sure:
                //确定
                long selectedData = getSelectData();
                if(checkDatas(selectedData)){
                    TerminalFactory.getSDK().getThreadPool().execute(() -> {
                        TerminalFactory.getSDK().getLiveManager().requestSetLivingTimeMessage(selectedData,false);
                    });
                }else{
                    ToastUtil.showToast(SetLivingStopTimeFragment.this.getContext(),String.format(getString(R.string.text_set_living_stop_time_tempt),
                            StringUtil.secondsToHour(selectedData)));
                }
                break;
        }
    }

    /**
     * 检查数据
     * @return
     */
    private boolean checkDatas(long selectedData) {
        long intervalTime = TerminalFactory.getSDK().getParam(Params.MAX_LIVING_TIME, 0L);
        return (selectedData!=0) && intervalTime != selectedData;
    }

    /**
     * 获取选择的数据
     * @return
     */
    private long getSelectData(){
        long result = 0L;
        for (SetLivingStopTimeBean bean:data) {
            if(bean.isChecked()){
                result = StringUtil.hourToSeconds(bean.getTime());
                break;
            }
        }
        return result;
    }

    /**
     * 设置成功终端上报时长时更新UI的通知
     */
    private ReceiveResponseSetLivingTimeMessageUIHandler receiveResponseSetLivingTimeMessageUIHandler = new ReceiveResponseSetLivingTimeMessageUIHandler() {
        @Override
        public void handler(long livingTime ,boolean isResetLiving) {
            if(data!=null){
                for (SetLivingStopTimeBean bean: data) {
                    if(bean!=null){
                        bean.setChecked((StringUtil.hourToSeconds(bean.getTime())==livingTime));
                    }
                }
            }
            mHandler.post(() -> {
                if(adapter!=null){
                    adapter.notifyDataSetChanged();
                }
            });
        }
    };

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveResponseSetLivingTimeMessageUIHandler);
        mHandler.removeCallbacksAndMessages(null);
    }
}
