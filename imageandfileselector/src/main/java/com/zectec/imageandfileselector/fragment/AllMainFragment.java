package com.zectec.imageandfileselector.fragment;

import android.os.Environment;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.tapadoo.alerter.Alerter;
import com.zectec.imageandfileselector.R;
import com.zectec.imageandfileselector.base.BaseFragment;
import com.zectec.imageandfileselector.base.Constant;
import com.zectec.imageandfileselector.bean.FileInfo;
import com.zectec.imageandfileselector.utils.FileUtil;
import com.zectec.imageandfileselector.utils.OperateReceiveHandlerUtilSync;
import com.zectec.imageandfileselector.utils.SystemUtil;

import java.util.Map;
import java.util.Set;

import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiverCheckFileHandler;
import com.zectec.imageandfileselector.receivehandler.ReceiverSendFileHandler;
import com.zectec.imageandfileselector.receivehandler.ReceiverShowOrHideFragmentHandler;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

/**
 * Created by CWJ on 2017/3/28.
 */

public class AllMainFragment extends BaseFragment implements View.OnClickListener {
    TextView tv_all_size;
    TextView tv_send;
    TextView tv_cancel;


    private boolean checkSDEnvironment() {
        boolean sdCardExist = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED); // 判断sd卡是否存在
        return sdCardExist;

    }

    private boolean checkExtentEnvironment() {
        if (checkSDEnvironment() && TextUtils.isEmpty(FileUtil.getStoragePath(getActivity()))) {
            return false;
        } else {
            return true;
        }
    }

    @Override
    public boolean isBindEventBusHere() {
        return true;
    }


    public void updateSizAndCount() {
        final Set<Map.Entry<String, FileInfo>> entries = Constant.files.entrySet();
        if (entries.size() == 0) {
            tv_send.setBackgroundResource(R.drawable.shape_bt_send);
            tv_send.setTextColor(getResources().getColor(R.color.md_grey_700));
            tv_all_size.setText(getString(R.string.size, "0B"));
            tv_send.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (entries.size() == 0){
                        Toast.makeText(getActivity(),"请选择至少一个文件",Toast.LENGTH_SHORT).show();
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


    @Override
    public int getLayoutResource() {
        return R.layout.fragment_main_all;
    }

    @Override
    public void initView() {
        tv_all_size = (TextView) rootView.findViewById(R.id.tv_all_size);
        tv_send = (TextView) rootView.findViewById(R.id.tv_send);
        tv_cancel = (TextView) rootView.findViewById(R.id.tv_cancel);
        rootView.findViewById(R.id.rl_mobile_memory).setOnClickListener(this);
        rootView.findViewById(R.id.rl_extended_memory).setOnClickListener(this);
        rootView.findViewById(R.id.rl_sd_card).setOnClickListener(this);
        tv_all_size.setText(getString(R.string.size, "0B"));
        tv_send.setText(getString(R.string.send, "0"));
        SystemUtil.init(getActivity());
        updateSizAndCount();
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

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.rl_mobile_memory) {
            rl_mobile_memory();
        }
        else if(v.getId() == R.id.rl_extended_memory) {
            rl_extended_memory();
        }
        else if(v.getId() == R.id.rl_sd_card) {
            rl_sd_card();
        }
    }

    void rl_mobile_memory() {
        OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiverShowOrHideFragmentHandler.class, "手机内存", true);
    }


    void rl_extended_memory() {
        if (checkExtentEnvironment()) {
           OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiverShowOrHideFragmentHandler.class, "扩展卡内存", true);
        } else {
            Alerter.create(getActivity())
                    .setTitle("通知")
                    .setText("您手机没有外置SD卡！")
                    .show();
        }
    }

    void rl_sd_card() {

        if (checkSDEnvironment()) {
            OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiverShowOrHideFragmentHandler.class, "SD卡", true);
        } else {
            Alerter.create(getActivity())
                    .setTitle("通知")
                    .setText("您手机没有内置SD卡！")
                    .show();
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
