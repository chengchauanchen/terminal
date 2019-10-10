package com.vsxin.terminalpad.mvp.ui.widget;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.vsxin.terminalpad.R;
import com.vsxin.terminalpad.manager.operation.OperationEnum;
import com.vsxin.terminalpad.mvp.entity.PersonnelBean;
import com.vsxin.terminalpad.mvp.entity.TerminalBean;
import com.vsxin.terminalpad.mvp.ui.adapter.PoliceDeviceAdapter;
import com.vsxin.terminalpad.utils.TerminalUtils;

import java.util.ArrayList;
import java.util.List;

public class PoliceDevicesDialog extends Dialog {

    private RecyclerView recyclerView;
    private List<TerminalBean> terminalBeans = new ArrayList<>();
    private OperationEnum operationEnum;
    private TextView text_title;
    private PersonnelBean personnel;
    private Context context;
    private View.OnClickListener onClickListener;

    public PoliceDevicesDialog(Context context, PersonnelBean personnel, List<TerminalBean> terminalBeans, OperationEnum operationEnum,View.OnClickListener onClickListener) {
        super(context, R.style.dialog);
        this.context =context;
        this.operationEnum = operationEnum;
        this.personnel = personnel;
        this.terminalBeans.clear();
        this.terminalBeans.addAll(terminalBeans);
        this.onClickListener = onClickListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_police_devices);
        initView();
        init();
    }

    private void initView() {
        List<TerminalBean> allowPullLiveList = new ArrayList<>();

        recyclerView = findViewById(R.id.recyclerView);
        text_title = findViewById(R.id.text_title);
        text_title.setText(operationEnum.getRemarks());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        if(operationEnum==OperationEnum.CALL_PHONE){//打电话

        }else if(operationEnum==OperationEnum.MESSAGE){//消息

        }else if(operationEnum==OperationEnum.LIVE){//拉视频
            terminalBeans = getAllowPullLiveList(terminalBeans);
        }else if(operationEnum==OperationEnum.INDIVIDUAL_CALL){//个呼
            terminalBeans = getAllowIndividualCallList(terminalBeans);
        }
        PoliceDeviceAdapter policeDeviceAdapter = new PoliceDeviceAdapter(context, personnel,operationEnum,terminalBeans,onClickListener);
        recyclerView.setAdapter(policeDeviceAdapter);
    }

    /**
     * 该类型设备是否允许个呼
     * @param terminalBeans
     * @return
     */
    private List<TerminalBean> getAllowIndividualCallList(List<TerminalBean> terminalBeans){
        List<TerminalBean> newTerminals = new ArrayList<>();
        for (TerminalBean terminalBean : terminalBeans){
            Boolean allowPullLive = TerminalUtils.isAllowIndividualCall(terminalBean);
            if(allowPullLive){
                newTerminals.add(terminalBean);
            }
        }
        return newTerminals;
    }

    /**
     * 该类型设备是否允许拉视频
     * @param terminalBeans
     * @return
     */
    private List<TerminalBean> getAllowPullLiveList(List<TerminalBean> terminalBeans){
        List<TerminalBean> newTerminals = new ArrayList<>();
        for (TerminalBean terminalBean : terminalBeans){
            Boolean allowPullLive = TerminalUtils.isAllowPullLive(terminalBean);
            if(allowPullLive){
                newTerminals.add(terminalBean);
            }
        }
        return newTerminals;
    }


    private void init() {
        Display display = getWindow().getWindowManager().getDefaultDisplay();
        int width = display.getWidth();
        Window window = getWindow();
        WindowManager.LayoutParams layoutParams = window.getAttributes();
//        layoutParams.width = (int) (width * 0.9);
        layoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setAttributes(layoutParams);
        window.setGravity(Gravity.RIGHT|Gravity.CENTER);
        setCanceledOnTouchOutside(true);
        setCancelable(true);
    }
}
