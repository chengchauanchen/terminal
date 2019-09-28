package com.vsxin.terminalpad.mvp.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.vsxin.terminalpad.R;
import com.vsxin.terminalpad.manager.PullLiveManager;
import com.vsxin.terminalpad.manager.StartCallManager;
import com.vsxin.terminalpad.manager.operation.OperationEnum;
import com.vsxin.terminalpad.mvp.contract.constant.TerminalType;
import com.vsxin.terminalpad.mvp.entity.PersonnelBean;
import com.vsxin.terminalpad.mvp.entity.TerminalBean;
import com.vsxin.terminalpad.utils.NumberUtil;
import com.vsxin.terminalpad.utils.TerminalUtils;

import java.util.List;

import ptt.terminalsdk.tools.ToastUtil;

/**
 * 作者：xuxiaolong
 * 版本：1.0
 * 创建日期：2019/1/4
 * 描述：
 * 修订历史：
 */
public class PoliceDeviceAdapter extends RecyclerView.Adapter<PoliceDeviceAdapter.ViewHolder> {

    //显示的数据
    private List<TerminalBean> datas;
    private Context context;
    private LayoutInflater inflater;
    private final PullLiveManager pullLiveManager;
    private PersonnelBean personnel;
    private OperationEnum operationEnum;
    private final StartCallManager startCallManager;

    public PoliceDeviceAdapter(Context context, PersonnelBean personnel, OperationEnum operationEnum, List<TerminalBean> terminalBeans) {
        inflater = LayoutInflater.from(context);
        this.context = context;
        this.datas = terminalBeans;
        this.personnel = personnel;
        this.operationEnum = operationEnum;
        pullLiveManager = new PullLiveManager(context);
        startCallManager = new StartCallManager(context);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(inflater.inflate(R.layout.item_police_devices, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        TerminalBean terminalBean = datas.get(position);
        int imageRid = TerminalUtils.getDialogImageForTerminalType(terminalBean.getTerminalType());
        holder.iv_icon.setImageResource(imageRid);
        String terminalName = TerminalUtils.getNameForTerminalType(terminalBean.getTerminalType());
        holder.tv_context.setText(terminalName);

        holder.ll_all.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(operationEnum==OperationEnum.LIVE){//拉视频
                    getPullLive(terminalBean);
                }else if(operationEnum==OperationEnum.INDIVIDUAL_CALL){//个呼
                    individualCall(terminalBean);
                }
            }
        });
        //拉视频
        //pullLiveManager.pullVideo(personnelNo, terminalBean.getTerminalType(), terminalBean.getTerminalUniqueNo());
    }

    /**
     * 拉视频
     * @param terminalBean
     */
    private void getPullLive(TerminalBean terminalBean){
        String terminalUniqueNo = TerminalUtils.getPullLiveUniqueNo(terminalBean);
        pullLiveManager.pullVideo(terminalBean.getAccount(), terminalBean.getTerminalType(), terminalUniqueNo);
    }

    /**
     * 个呼
     * @param terminalBean
     */
    private void individualCall(TerminalBean terminalBean){
        //手台个呼
        if(terminalBean!=null){
            if(terminalBean.getTerminalType().equals(TerminalType.TERMINAL_PDT)){
                Long uniqueNo = NumberUtil.strToLong(terminalBean.getPdtNo());
                //startCallManager.startIndividualCall("手台", terminalBean.getPdtNo(), uniqueNo);
                startCallManager.startIndividualCall("手台", "72020855", uniqueNo);
            }else if(terminalBean.getTerminalType().equals(TerminalType.TERMINAL_LTE)){//lte暂不能个呼.因为取不到uniqueNo
//                    Long uniqueNo = NumberUtil.strToLong(terminalBean.getPdtNo());
//                    startCallManager.startIndividualCall("手台", terminalBean.getPdtNo(), uniqueNo);
            }else if(terminalBean.getTerminalType().equals(TerminalType.TERMINAL_PHONE)){//正常情况,警务通就是民警,不会走这
                    Long uniqueNo = NumberUtil.strToLong(terminalBean.getTerminalUniqueNo());
                    startCallManager.startIndividualCall(personnel.getPersonnelName(), personnel.getPersonnelNo(),uniqueNo );
            }else{
                ToastUtil.showToast(context,"暂不支持该设备个呼");
            }
            //getPresenter().startIndividualCall("72020850", TerminalMemberType.TERMINAL_PDT);
        }else{
            ToastUtil.showToast(context,"暂不支持该设备个呼");
        }
    }


    @Override
    public int getItemCount() {
        return datas.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final LinearLayout ll_all;
        private final ImageView iv_icon;
        private final TextView tv_context;

        public ViewHolder(View itemView) {
            super(itemView);
            ll_all = itemView.findViewById(R.id.ll_all);
            iv_icon = itemView.findViewById(R.id.iv_icon);//设备图标
            tv_context = itemView.findViewById(R.id.tv_context);//设备名称
        }
    }


}
