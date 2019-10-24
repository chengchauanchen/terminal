package cn.vsx.uav.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;

import com.chad.library.adapter.base.BaseQuickAdapter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.model.BitStarFileRecord;
import cn.vsx.uav.R;
import cn.vsx.uav.adapter.CommonItemDecoration;
import cn.vsx.uav.adapter.FileAdapter;
import cn.vsx.uav.bean.FileBean;
import cn.vsx.uav.receiveHandler.ReceiveFileSelectChangeHandler;
import cn.vsx.uav.receiveHandler.ReceiveSendFileFinishHandler;
import cn.vsx.uav.receiveHandler.ReceiveShowCheckboxHandler;
import cn.vsx.uav.receiveHandler.ReceiveShowPreViewHandler;
import cn.vsx.vc.fragment.BaseFragment;
import cn.vsx.vc.utils.FileUtil;
import ptt.terminalsdk.tools.FileTransgerUtil;

import static cn.vsx.uav.constant.Constants.TYPE_COMMON;
import static cn.vsx.uav.constant.Constants.TYPE_DATE;
import static cn.vsx.uav.constant.Constants.TYPE_NULL;

/**
 * @author ：ly-xuxiaolong
 * 版本：1.0
 * 创建日期：2019/8/20
 * 描述：所有文件
 * 修订历史：
 */
public class UavAllFileFragment extends BaseFragment implements BaseQuickAdapter.RequestLoadMoreListener, SwipeRefreshLayout.OnRefreshListener{

    private SwipeRefreshLayout mUavDateSwipeRefreshLayout;
    private RecyclerView mUavDateRecyclerView;
    private FileAdapter adapter;
    private boolean showCheckbox;
    private List<FileBean> data = new ArrayList<>();
    private static final int ADD_DATA = 0;
    private static final int CLEAR_DATA = 1;
    //一次加载3天的数据
    private static final int PAGE = 10;
    //当前加载的次数
    private int currentIndex = 1;
    //总次数
    private int totalPage;
    @SuppressWarnings("handlerLeak")
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            super.handleMessage(msg);
            if(msg.what == ADD_DATA){
                FileBean file = (FileBean) msg.obj;
                data.add(file);
                logger.info("file:"+file+"---position:"+data.indexOf(file));
                adapter.notifyItemChanged(data.size()-1);
            }else if(msg.what == CLEAR_DATA){
                data.clear();
            }
        }
    };

    public static UavAllFileFragment newInstance(boolean showCheckbox){
         Bundle args = new Bundle();
         args.putBoolean("showCheckbox",showCheckbox);
         UavAllFileFragment fragment = new UavAllFileFragment();
        fragment.setArguments(args);
        return fragment;
    }
    @Override
    public int getContentViewId(){
        return R.layout.layout_file_list;
    }

    @Override
    public void initView(){
        mUavDateSwipeRefreshLayout = mRootView.findViewById(R.id.uav_swipeRefreshLayout);
        mUavDateRecyclerView = mRootView.findViewById(R.id.uav_date_recycler_view);
    }

    @Override
    public void initListener(){
        mUavDateSwipeRefreshLayout.setOnRefreshListener(this);
        TerminalFactory.getSDK().registReceiveHandler(receiveFileSelectChangeHandler);
        TerminalFactory.getSDK().registReceiveHandler(receiveShowCheckboxHandler);
        TerminalFactory.getSDK().registReceiveHandler(receiveSendFileFinishHandler);
    }

    @Override
    public void initData(){
        mUavDateRecyclerView.setLayoutManager(new GridLayoutManager(getContext(),5));
        mUavDateRecyclerView.addItemDecoration(new CommonItemDecoration(20,20));
        adapter = new FileAdapter(data);
        showCheckbox = getArguments().getBoolean("showCheckbox",false);
        adapter.setShowCheckbox(showCheckbox);
        adapter.setItemClickListener(fileBean -> TerminalFactory.getSDK().notifyReceiveHandler(ReceiveShowPreViewHandler.class,true,fileBean));
        adapter.setEnableLoadMore(true);
        adapter.setOnLoadMoreListener(this,mUavDateRecyclerView);
        //        adapter.disableLoadMoreIfNotFullPage();
        mUavDateRecyclerView.setAdapter(adapter);

        TerminalFactory.getSDK().getThreadPool().execute(() -> {
            getNewData(currentIndex);
//            getData(currentIndex);
            currentIndex++;
        });
    }

    private synchronized void getNewData(int pageIndex){
        CopyOnWriteArrayList<String> fileDates = TerminalFactory.getSDK().getSQLiteDBManager().getFileDates(pageIndex, PAGE);
        for(String fileDate : fileDates){
            boolean first = true;
            int realFileSize = 0;
            CopyOnWriteArrayList<BitStarFileRecord> bitStarFileRecords = TerminalFactory.getSDK().getSQLiteDBManager().getBitStarFileRecords(fileDate, "");
            for(BitStarFileRecord bitStarFileRecord : bitStarFileRecords){
                String filePath = bitStarFileRecord.getFilePath();
                File file = new File(filePath);
                if(file.exists() && file.isFile()){
                    FileBean fileBean = new FileBean();
                    if(first){
                        fileBean.setType(TYPE_DATE);
                        first = false;
                    }else {
                        fileBean.setType(TYPE_COMMON);
                    }
                    fileBean.setDate(fileDate);
                    fileBean.setPath(bitStarFileRecord.getFilePath());
                    fileBean.setHeight(bitStarFileRecord.getHeight());
                    fileBean.setWidth(bitStarFileRecord.getWidth());
                    fileBean.setDuration(bitStarFileRecord.getDuration());
                    fileBean.setName(bitStarFileRecord.getFileName());
                    fileBean.setFileSize(FileUtil.getFileSize(file));
                    fileBean.setIsVideo(TextUtils.equals(bitStarFileRecord.getFileType(),FileTransgerUtil.TYPE_VIDEO));
                    Message message = Message.obtain();
                    message.what = ADD_DATA;
                    message.obj = fileBean;
                    mHandler.sendMessage(message);
                    realFileSize++;
                }
            }
            //次数应为当天的有效数据
            int size = 5 - realFileSize % 5;
            if(size != 0 &&  size !=5){
                for(int k = 0; k < size; k++){
                    FileBean emptyBean = new FileBean();
                    emptyBean.setType(TYPE_NULL);
                    Message message = Message.obtain();
                    message.what = ADD_DATA;
                    message.obj = emptyBean;
                    mHandler.sendMessage(message);
                }
            }
        }

    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        mHandler.removeCallbacksAndMessages(null);
        TerminalFactory.getSDK().unregistReceiveHandler(receiveFileSelectChangeHandler);
        TerminalFactory.getSDK().unregistReceiveHandler(receiveShowCheckboxHandler);
        TerminalFactory.getSDK().unregistReceiveHandler(receiveSendFileFinishHandler);
    }

    private ReceiveShowCheckboxHandler receiveShowCheckboxHandler = show -> {
        if(adapter != null){
            if(!show){
                for(FileBean bean : data){
                    bean.setSelected(false);
                }
            }
            showCheckbox = show;
            mHandler.post(()->{
                adapter.setShowCheckbox(show);
                adapter.notifyDataSetChanged();
            });
        }
    };

    private ReceiveSendFileFinishHandler receiveSendFileFinishHandler = (selectFiles) -> mHandler.post(()->{
        for(FileBean fileBean : data){
            fileBean.setSelected(false);
        }
        showCheckbox = false;
        adapter.setShowCheckbox(false);
        adapter.notifyDataSetChanged();
    });

    private ReceiveFileSelectChangeHandler receiveFileSelectChangeHandler = (selected, fileBean) -> {
        if(data.contains(fileBean)){
            int index = data.indexOf(fileBean);
            data.get(index).setSelected(selected);
            if(isHidden()){
                adapter.notifyItemChanged(index);
            }
        }
    };

    public void setShowCheckbox(boolean showCheckbox){
        if(adapter !=null){
            adapter.setShowCheckbox(showCheckbox);
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onLoadMoreRequested(){
        logger.info("onLoadMoreRequested:"+currentIndex);
        if(currentIndex == 0){
            mHandler.postDelayed(()->{
                adapter.loadMoreEnd();
            },1000);
            return;
        }
        mHandler.post(() -> {
            if (currentIndex >= totalPage) {
                adapter.loadMoreEnd();
            } else {
                TerminalFactory.getSDK().getThreadPool().execute(()->{
                    getNewData(currentIndex);
//                    getData(currentIndex);
                    currentIndex++;
                    mHandler.post(()-> adapter.loadMoreComplete());
                });
            }
        });
    }

    @Override
    public void onRefresh(){
        currentIndex = 1;
        data.clear();
        TerminalFactory.getSDK().getThreadPool().execute(() -> {
            getNewData(currentIndex);
            currentIndex++;
        });
        mHandler.postDelayed(()->{
            mUavDateSwipeRefreshLayout.setRefreshing(false);
        },500);
    }
}
