package com.vsxin.terminalpad.mvp.contract.presenter;

import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.ixiaoma.xiaomabus.architecture.mvp.BasePresenter;
import com.vsxin.terminalpad.R;
import com.vsxin.terminalpad.app.PadApplication;
import com.vsxin.terminalpad.mvp.contract.view.IMainView;
import com.vsxin.terminalpad.receiveHandler.ReceiverActivePushVideoHandler;

import java.util.ArrayList;

import cn.vsx.hamster.common.Authority;
import cn.vsx.hamster.terminalsdk.manager.videolive.VideoLivePlayingState;
import cn.vsx.hamster.terminalsdk.manager.videolive.VideoLivePushingState;
import ptt.terminalsdk.context.MyTerminalFactory;
import ptt.terminalsdk.tools.ToastUtil;

/**
 * Created by PC on 2018/11/1.
 */

public class MainPresenter extends BasePresenter<IMainView> {

    public MainPresenter(Context mContext) {
        super(mContext);
    }

}
