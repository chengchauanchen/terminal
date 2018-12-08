package cn.vsx.vc.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.vsx.hamster.common.GroupScanType;
import cn.vsx.vc.R;
import ptt.terminalsdk.tools.ToastUtil;
import ptt.terminalsdk.context.MyTerminalFactory;

/**
 * 组扫描组件化
 * Created by gt358 on 2017/8/9.
 */
public class SweepGroupLayout extends LinearLayout {
    private Boolean IsOpenSweep = false;
    private Context context;
    @Bind(R.id.open_sweep)
    MToggleButton openSweep;
    public SweepGroupLayout(Context context) {
        this(context, null);
    }

    public SweepGroupLayout(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SweepGroupLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        initView();
        initData();
        initLintener();
    }

    private void initView () {
        String infServie = Context.LAYOUT_INFLATER_SERVICE;
        LayoutInflater layoutInflater;
        layoutInflater =  (LayoutInflater) getContext().getSystemService(infServie);
        View view = layoutInflater.inflate(R.layout.layout_sweepgroup, this, true);
        ButterKnife.bind(this, view);
    }

    private void initData () {

    }

    private void initLintener () {
        //是否打开组扫描
        openSweep.setOnBtnClick(new MToggleButton.OnBtnClickListener() {
            @Override
            public void onBtnClick(boolean currState) {
                IsOpenSweep = currState;
                if (MyTerminalFactory.getSDK().hasNetwork()){
                    MyTerminalFactory.getSDK().getGroupScanManager().groupScan(currState, GroupScanType.GROUP_SCANNING.getCode());
                } else {
                    ToastUtil.showToast(context, "网络连接异常，请检查网络！");
                }
            }
        });
    }

    @OnClick(R.id.set_sweep)
    public void onClick (View view) {
        switch (view.getId()) {

        }
    }
}
