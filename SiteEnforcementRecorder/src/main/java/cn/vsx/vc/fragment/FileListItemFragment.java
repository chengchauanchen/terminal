package cn.vsx.vc.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.allen.library.observer.CommonObserver;
import com.zectec.imageandfileselector.utils.OperateReceiveHandlerUtilSync;

import org.apache.log4j.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.model.BitStarFileDirectory;
import cn.vsx.vc.R;
import cn.vsx.vc.adapter.FileListAdapter;
import cn.vsx.vc.receiveHandle.ReceiverFragmentShowHandler;
import cn.vsx.vc.utils.Constants;
import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import ptt.terminalsdk.context.MyTerminalFactory;
import ptt.terminalsdk.receiveHandler.ReceiveGenerateFileCompleteHandler;
import ptt.terminalsdk.tools.FileComparatorByTime;
import ptt.terminalsdk.tools.FileTransgerUtil;


public class FileListItemFragment extends Fragment  {

    @Bind(R.id.swipeRefreshLayout)
    SwipeRefreshLayout swipeRefreshLayout;
    @Bind(R.id.recyclerView)
    RecyclerView recyclerView;
    @Bind(R.id.tv_nothing)
    TextView tvNothing;

    private FileListAdapter adapter;
    private List<File> datas = new ArrayList<>();
    //文件的类型
    private String fileType;
    private Handler mHandler = new Handler(Looper.getMainLooper());
    public Logger logger = Logger.getLogger(getClass());

    public static FileListItemFragment newInstance(String fileType) {
        FileListItemFragment fragment = new FileListItemFragment();
        Bundle args = new Bundle();
        args.putString(Constants.FILE_TYPE,fileType);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            fileType = getArguments().getString(Constants.FILE_TYPE);
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_file_list_item, container, false);
        ButterKnife.bind(this, view);
        initView();
        initListener();
        initData();
        return view;
    }

    /**
     * 初始化布局
     */
    private void initView() {
        String emptyString = "";
        if (TextUtils.equals(fileType, FileTransgerUtil.TYPE_VIDEO)) {
            emptyString = getString(R.string.text_file_video_empty);
        }else if(TextUtils.equals(fileType, FileTransgerUtil.TYPE_IMAGE)){
            emptyString = getString(R.string.text_file_picture_empty);
//        }else if(TextUtils.equals(fileType, FileTransgerUtil.TYPE_AUDIO)){
        }
        tvNothing.setText(emptyString);
        tvNothing.setVisibility(View.GONE);

        swipeRefreshLayout.setProgressBackgroundColorSchemeResource(R.color.colorAccent);
        swipeRefreshLayout.setColorSchemeResources(R.color.white);
        swipeRefreshLayout.setOnRefreshListener(this::initData);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
        adapter = new FileListAdapter(getContext(), datas);
        recyclerView.setAdapter(adapter);
        adapter.setOnItemClickListener((position,file) -> {
            TerminalFactory.getSDK().getThreadPool().execute(new Runnable() {
                @Override
                public void run() {
                    if(file!=null&&file.exists()){
                        //点击查看录像、图片
                        String fileType = FileTransgerUtil.getBITFileType(file.getName());
                        String fragment = "";
                        if (TextUtils.equals(fileType, FileTransgerUtil.TYPE_VIDEO)) {
                            fragment = Constants.FRAGMENT_TAG_FILE_VIDEO_SHOW;
                        }else if(TextUtils.equals(fileType, FileTransgerUtil.TYPE_IMAGE)){
                            fragment = Constants.FRAGMENT_TAG_FILE_PICTURE_SHOW;
                        }
                        if(!TextUtils.isEmpty(fragment)){
                            ArrayList<String> list = getPathByFile();
                            String finalFragment = fragment;
                            mHandler.post(() -> {
                                Bundle bundle = new Bundle();
                                bundle.putStringArrayList(Constants.FILE_PATHS,list);
                                bundle.putInt(Constants.FILE_INDEX,position);
                                OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiverFragmentShowHandler.class, finalFragment,bundle);
                            });
                        }
                    }
                }
            });
        });
    }
    /**
     * 添加监听
     */
    private void initListener() {
        //注册生成文件的通知
        MyTerminalFactory.getSDK().registReceiveHandler(receiveGenerateFileCompleteHandler);//生成了新文件
    }

    /**
     * 获取录像文件、图片文件
     */
    private synchronized void initData() {
        Observable.zip(MyTerminalFactory.getSDK().getFileTransferOperation().getFileDataByCode(BitStarFileDirectory.USB.getCode(), fileType),
                MyTerminalFactory.getSDK().getFileTransferOperation().getFileDataByCode(BitStarFileDirectory.SDCARD.getCode(), fileType),
                (usbList, sdCardList) -> {
                    datas.clear();
                    datas.addAll(usbList);
                    datas.addAll(sdCardList);
                    Collections.sort(datas,new FileComparatorByTime());
                    return datas;
                }).subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(new CommonObserver<List<File>>() {

                    @Override protected boolean isHideToast() {
                        return true;
                    }

                    @Override
                    protected void onError(String errorMsg) {
                        logger.error("FileListItemFragment--initData----报错:" + errorMsg);
                    }

                    @Override
                    protected void onSuccess(List<File> allRowSize) {
                        logger.info(allRowSize);
                        mHandler.post(() -> {
                            if(tvNothing!=null){
                                tvNothing.setVisibility(datas.isEmpty()?View.VISIBLE:View.GONE);
                            }
                            if(adapter!=null){
                                adapter.notifyDataSetChanged();
                            }
                           if(swipeRefreshLayout!=null){
                               swipeRefreshLayout.setRefreshing(false);
                           }
                        });
                    }
                });
    }


    /**
     * 生成了新文件
     **/
    private ReceiveGenerateFileCompleteHandler receiveGenerateFileCompleteHandler = new ReceiveGenerateFileCompleteHandler() {
        @Override
        public void handler() {
            mHandler.post(() -> initData());
        }
    };

    /**
     * 获取文件的路径集合
     * @return
     */
    private ArrayList<String> getPathByFile(){
        ArrayList<String> list = new ArrayList<>();
        if(!datas.isEmpty()){
            for (File file: datas) {
                if(file != null && !TextUtils.isEmpty(file.getAbsolutePath())){
                    list.add(file.getAbsolutePath());
                }
            }
        }
        return list;
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
        //注销生成文件的通知
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveGenerateFileCompleteHandler);//生成了新文件
    }
}
