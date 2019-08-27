package cn.vsx.uav.fragment;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.uav.R;
import cn.vsx.uav.adapter.CommonItemDecoration;
import cn.vsx.uav.adapter.FileAdapter;
import cn.vsx.uav.bean.FileBean;
import cn.vsx.uav.receiveHandler.ReceiveSendFileFinishHandler;
import cn.vsx.uav.receiveHandler.ReceiveShowCheckboxHandler;
import cn.vsx.uav.receiveHandler.ReceiveShowPreViewHandler;
import cn.vsx.vc.fragment.BaseFragment;
import cn.vsx.vc.utils.FileUtil;
import ptt.terminalsdk.context.MyTerminalFactory;

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
public class UavAllFileFragment extends BaseFragment implements BaseQuickAdapter.RequestLoadMoreListener{

    private RecyclerView mUavDateRecyclerView;
    private FileAdapter adapter;
    private List<FileBean> data = new ArrayList<>();
    private static final int ADD_DATA = 0;
    private static final int CLEAR_DATA = 1;
    //一次加载3天的数据
    private static final int PAGE = 6;
    //当前加载的次数
    private int currentIndex = 0;
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
        mUavDateRecyclerView = mRootView.findViewById(R.id.uav_date_recycler_view);
    }

    @Override
    public void initListener(){
        TerminalFactory.getSDK().registReceiveHandler(receiveShowCheckboxHandler);
        TerminalFactory.getSDK().registReceiveHandler(receiveSendFileFinishHandler);
    }

    @Override
    public void initData(){
        mUavDateRecyclerView.setLayoutManager(new GridLayoutManager(getContext(),5));
        mUavDateRecyclerView.addItemDecoration(new CommonItemDecoration(20,20));
        adapter = new FileAdapter(data);
        adapter.setShowCheckbox(getArguments().getBoolean("showCheckbox"));
        adapter.setItemClickListener(fileBean -> TerminalFactory.getSDK().notifyReceiveHandler(ReceiveShowPreViewHandler.class,true,fileBean));
        adapter.setEnableLoadMore(true);
        adapter.setOnLoadMoreListener(this,mUavDateRecyclerView);
        //        adapter.disableLoadMoreIfNotFullPage();
        mUavDateRecyclerView.setAdapter(adapter);

        TerminalFactory.getSDK().getThreadPool().execute(() -> {
            getData(currentIndex);
            currentIndex++;
        });
    }

    private synchronized void getData(int pageIndex){
        logger.info("pageIndex:"+pageIndex);
        if(pageIndex == 0){
            mHandler.sendEmptyMessage(CLEAR_DATA);
        }
        //"uavFile"文件夹
        String uavDirectory = MyTerminalFactory.getSDK().getUavDirectory();
        File dir = new File(uavDirectory);
        if(dir.exists() && dir.isDirectory() && dir.listFiles().length>0){
            File[] datefiles = dir.listFiles();
            sortDirs(datefiles);
            totalPage = (int) Math.ceil((double) datefiles.length/PAGE);
            logger.info("totalPage:"+totalPage);
            int currentData;
            if(totalPage > PAGE*(pageIndex+1)){
                currentData = PAGE*(pageIndex+1);
            }else {
                currentData = datefiles.length;
            }
            logger.info("currentData:"+currentData);

            for(int i = PAGE*pageIndex; i < currentData; i++){
                //日期文件夹
                if(datefiles[i].exists() && datefiles[i].isDirectory() && datefiles[i].listFiles().length > 0){
                    //照片或者视频文件夹
                    File[] dirs = datefiles[i].listFiles();
                    boolean first = true;
                    int realFileSize = 0;
                    for(File dir1 : dirs){
                        if(dir1.exists() && dir1.isDirectory() && dir1.listFiles().length > 0){
                            //照片或者视频文件
                            File[] files = dir1.listFiles();
                            for(File file : files){
                                if(file.exists() && file.isFile() && file.length() > 0){
                                    FileBean fileBean = new FileBean();
                                    if(first){
                                        fileBean.setType(TYPE_DATE);
                                        first = false;
                                    }else{
                                        fileBean.setType(TYPE_COMMON);
                                    }
                                    fileBean.setDate(datefiles[i].getName());
                                    fileBean.setPath(file.getPath());
                                    fileBean.setName(file.getName());
                                    fileBean.setFileSize(FileUtil.getFileSize(file));
                                    boolean isVideo = file.getParent().contains("videoRecord");
                                    fileBean.setIsVideo(isVideo);
                                    if(isVideo){
                                        Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(file.getPath(), MediaStore.Images.Thumbnails.MICRO_KIND);
                                        int videoDuration = FileUtil.getVideoDuration(file);
                                        if(videoDuration > 0){
                                            fileBean.setWidth(bitmap.getWidth());
                                            fileBean.setHeight(bitmap.getHeight());
                                            bitmap.recycle();
                                            fileBean.setDuration(videoDuration);
                                            Message message = Message.obtain();
                                            message.what = ADD_DATA;
                                            message.obj = fileBean;
                                            mHandler.sendMessage(message);
                                            realFileSize++;
                                        }else{
                                            file.delete();
                                        }
                                    }else{
                                        Bitmap bitmap = BitmapFactory.decodeFile(file.getPath());
                                        fileBean.setWidth(bitmap.getWidth());
                                        fileBean.setHeight(bitmap.getHeight());
                                        bitmap.recycle();
                                        Message message = Message.obtain();
                                        message.what = ADD_DATA;
                                        message.obj = fileBean;
                                        mHandler.sendMessage(message);
                                        realFileSize++;
                                    }

                                }
                            }

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
        }
    }

    @SuppressLint("SimpleDateFormat")
    private void sortDirs(File[] files){
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Arrays.sort(files, (o1, o2) -> {
            String path = o1.getPath();
            int index = path.lastIndexOf("/");
            String date1 = path.substring(index+1);

            String path2 = o2.getPath();
            int index2 = path2.lastIndexOf("/");
            String date2 = path2.substring(index2+1);
            long result = 0;
            try{
                result = dateFormat.parse(date2).getTime() - dateFormat.parse(date1).getTime();
            }catch(ParseException e){
                e.printStackTrace();
            }
//            logger.info("result:"+result);
            return (int) result;
        });
    }

    @Override
    public void onDestroyView(){
        super.onDestroyView();
        mHandler.removeCallbacksAndMessages(null);
        TerminalFactory.getSDK().unregistReceiveHandler(receiveShowCheckboxHandler);
        TerminalFactory.getSDK().unregistReceiveHandler(receiveSendFileFinishHandler);
    }

    private ReceiveShowCheckboxHandler receiveShowCheckboxHandler = show -> {
        if(adapter != null){
            mHandler.post(()->{
                adapter.setShowCheckbox(show);
                adapter.notifyDataSetChanged();
            });
        }
    };

    private ReceiveSendFileFinishHandler receiveSendFileFinishHandler = () -> mHandler.post(()->{
        adapter.setShowCheckbox(false);
        adapter.notifyDataSetChanged();
    });

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
                    getData(currentIndex);
                    currentIndex++;
                    mHandler.post(()-> adapter.loadMoreComplete());
                });
            }
        });
    }
}
