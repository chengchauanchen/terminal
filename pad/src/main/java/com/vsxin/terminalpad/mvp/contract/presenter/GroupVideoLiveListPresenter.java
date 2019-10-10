package com.vsxin.terminalpad.mvp.contract.presenter;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.ixiaoma.xiaomabus.architecture.mvp.refresh.RefreshPresenter;
import com.vsxin.terminalpad.R;
import com.vsxin.terminalpad.mvp.contract.view.IGroupVideoLiveList;
import com.vsxin.terminalpad.mvp.entity.MediaBean;
import com.vsxin.terminalpad.receiveHandler.ReceiveGetHistoryLiveUrlsHandler2;
import com.vsxin.terminalpad.receiveHandler.ReceiveGoWatchLiveHandler2;

import java.util.List;

import cn.vsx.hamster.terminalsdk.model.TerminalMessage;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveGetGroupLivingHistoryListHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveGetGroupLivingListHandler;
import ptt.terminalsdk.context.MyTerminalFactory;

/**
 * @author qzw
 * <p>
 * app模块-通讯录模块
 */
public class GroupVideoLiveListPresenter extends RefreshPresenter<TerminalMessage, IGroupVideoLiveList> {
    private static final String TAG = "GroupVideoLiveListPresenter";
    protected Handler mHandler = new Handler(Looper.getMainLooper());

    public GroupVideoLiveListPresenter(Context mContext) {
        super(mContext);
    }

    /**
     * 加载数据
     */
    public void initData( int groupId,boolean isGroupVideoLiving,int page,int mPageSize) {
        if (isGroupVideoLiving) {
            //正在上报
            long groupUniqueNo = MyTerminalFactory.getSDK().getTerminalMessageManager().getGroupUniqueNo(groupId);
            MyTerminalFactory.getSDK().getGroupManager().getGroupLivingList(String.valueOf(groupUniqueNo), false);
        } else {
            //上报历史
            long groupUniqueNo = MyTerminalFactory.getSDK().getTerminalMessageManager().getGroupUniqueNo(groupId);
            MyTerminalFactory.getSDK().getGroupManager().getGroupHistoryLiveList(String.valueOf(groupUniqueNo), page, mPageSize);
        }
    }

    /**
     * 获取组内正在直播列表
     */
    private ReceiveGetGroupLivingListHandler receiveGetGroupLivingListHandler = (beanList, resultCode, resultDesc, forNumber) -> {
        getView().getGroupLivingList(beanList, resultCode, resultDesc, forNumber);
    };

    /**
     * 获取组内直播历史列表
     */
    private ReceiveGetGroupLivingHistoryListHandler receiveGetGroupLivingHistoryListHandler = (beanList, resultCode, resultDesc) -> {
        getView().getGroupLivingHistoryList(beanList, resultCode, resultDesc);
    };

    /**
     * 获取历史上报图像列表
     */
    private ReceiveGetHistoryLiveUrlsHandler2 mReceiveGetHistoryLiveUrlsHandler = new ReceiveGetHistoryLiveUrlsHandler2() {
        @Override
        public void handler(int code, List<MediaBean> liveUrl, String name, int memberId) {
            getView().getLogger().info("ReceiveGetHistoryLiveUrlsHandler--name:"+name+",member:"+memberId+",code:"+code);
            if(code == 0){
                MyTerminalFactory.getSDK().notifyReceiveHandler(ReceiveGoWatchLiveHandler2.class,liveUrl,name,memberId);
                //OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiveGoWatchLiveHandler.class,liveUrl,name,memberId);
                //OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(HistoryReportPlayerHandler.class);
            }else{
                mHandler.post(() -> {
                    getView().showMsg(R.string.text_get_video_info_fail);
                });
            }
        }
    };

    public void registReceiveHandler() {
        MyTerminalFactory.getSDK().registReceiveHandler(receiveGetGroupLivingListHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveGetGroupLivingHistoryListHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(mReceiveGetHistoryLiveUrlsHandler);
    }

    public void unRegistReceiveHandler() {
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveGetGroupLivingListHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveGetGroupLivingHistoryListHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(mReceiveGetHistoryLiveUrlsHandler);
    }


}
