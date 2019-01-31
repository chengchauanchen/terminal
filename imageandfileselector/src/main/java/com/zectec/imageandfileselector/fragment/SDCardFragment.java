package com.zectec.imageandfileselector.fragment;

import android.annotation.SuppressLint;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.zectec.imageandfileselector.R;
import com.zectec.imageandfileselector.adapter.MultipleItem;
import com.zectec.imageandfileselector.adapter.MultipleItemQuickAdapter;
import com.zectec.imageandfileselector.base.BaseFragment;
import com.zectec.imageandfileselector.base.Constant;
import com.zectec.imageandfileselector.bean.FileInfo;
import com.zectec.imageandfileselector.receivehandler.ReceiverSendFileHandler;
import com.zectec.imageandfileselector.utils.FileUtil;
import com.zectec.imageandfileselector.utils.OperateReceiveHandlerUtilSync;
import com.zectec.imageandfileselector.view.CheckBox;
import com.zectec.imageandfileselector.view.DividerItemDecoration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiverCheckFileHandler;
import com.zectec.imageandfileselector.receivehandler.ReceiverSendFileHandler;

import static com.zectec.imageandfileselector.utils.FileUtil.fileFilter;

public class SDCardFragment extends BaseFragment implements View.OnClickListener {
    RecyclerView rlv_sd_card;
    TextView tv_path;
    TextView tv_all_size;
    TextView tv_send;
    TextView tv_cancel;
    TextView tv_title_middle;
    private List<FileInfo> fileInfos = new ArrayList<>();
    private List<MultipleItem> mMultipleItems = new ArrayList<>();
    private MultipleItemQuickAdapter mAdapter;
    private File mCurrentPathFile = null;
    private File mSDCardPath = null;
    private String path;
    private String name;

    public SDCardFragment(){}
    @SuppressLint("ValidFragment")
    public SDCardFragment(String path, String name) {
        this.path = path;
        this.name = name;
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
                        Toast.makeText(getActivity(),R.string.text_please_select_at_least_one_file,Toast.LENGTH_SHORT).show();
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

    private void showFiles(File folder) {
        mMultipleItems.clear();
        tv_path.setText(folder.getAbsolutePath());
        mCurrentPathFile = folder;
        File[] files = fileFilter(folder);
        if (null == files || files.length == 0) {
            mAdapter.setEmptyView(getEmptyView());
            Log.e("files", "files::为空啦");
        } else {
            //获取文件信息
            fileInfos = FileUtil.getFileInfosFromFileArray(files);
            for (int i = 0; i < fileInfos.size(); i++) {
                if (fileInfos.get(i).isDirectory) {
                    mMultipleItems.add(new MultipleItem(MultipleItem.FOLD, fileInfos.get(i)));
                } else {
                    mMultipleItems.add(new MultipleItem(MultipleItem.FILE, fileInfos.get(i)));
                }

            }
            //查询本地数据库，如果之前有选择的就显示打钩
            Set<Map.Entry<String, FileInfo>> entries = Constant.files.entrySet();
            for (int i = 0; i < fileInfos.size(); i++) {
                for (Map.Entry<String, FileInfo> entry : entries) {
                    if (entry.getValue().getFileName().equals(fileInfos.get(i).getFileName())) {
                        fileInfos.get(i).setIsCheck(true);
                    }
                }
            }
        }
        mAdapter.notifyDataSetChanged();
    }

    private View getEmptyView() {
        return getActivity().getLayoutInflater().inflate(R.layout.empty_view, (ViewGroup) rlv_sd_card.getParent(), false);
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.iv_title_back) {
            iv_title_back();
        }
    }

    void iv_title_back() {
        if (mSDCardPath.getAbsolutePath().equals(mCurrentPathFile.getAbsolutePath())) {
//            OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiverShowOrHideFragmentHandler.class, "", false);
            getFragmentManager().popBackStack();
        } else {
            mCurrentPathFile = mCurrentPathFile.getParentFile();
            showFiles(mCurrentPathFile);
        }
    }

    @Override
    protected boolean isBindEventBusHere() {
        return false;
    }

    @Override
    public int getLayoutResource() {
        return R.layout.activity_sdcard;
    }

    @Override
    public void initView() {
        rlv_sd_card = (RecyclerView) rootView.findViewById(R.id.rlv_sd_card);
        tv_path = (TextView) rootView.findViewById(R.id.tv_path);
        tv_all_size = (TextView) rootView.findViewById(R.id.tv_all_size);
        tv_send = (TextView) rootView.findViewById(R.id.tv_send);
        tv_cancel = (TextView) rootView.findViewById(R.id.tv_cancel);
        tv_title_middle = (TextView) rootView.findViewById(R.id.tv_title_middle);
        rootView.findViewById(R.id.iv_title_back).setOnClickListener(this);
        tv_all_size.setText(getString(R.string.size, "0B"));
        tv_send.setText(getString(R.string.send, "0"));
        tv_title_middle.setText(name);
        mSDCardPath = new File(path);
        rlv_sd_card.setLayoutManager(new LinearLayoutManager(getActivity()));
        rlv_sd_card.addItemDecoration(new DividerItemDecoration(getActivity(), LinearLayoutManager.VERTICAL, R.drawable.divide_line));
        mAdapter = new MultipleItemQuickAdapter(mMultipleItems);
        rlv_sd_card.setAdapter(mAdapter);
        showFiles(mSDCardPath);
        updateSizAndCount();
        rlv_sd_card.addOnItemTouchListener(new OnItemClickListener() {
            @Override
            public void onSimpleItemClick(BaseQuickAdapter adapter, View view, int position) {

                if (adapter.getItemViewType(position) == MultipleItem.FILE) {
                    boolean isCheck = fileInfos.get(position).getIsCheck();
                    CheckBox checkBox = (CheckBox) view.findViewById(R.id.cb_file);
                    if (isCheck) {
                        //之前已经选择-现在执行取消选择
                        fileInfos.get(position).setIsCheck(!isCheck);
                        Constant.files.remove(fileInfos.get(position).getFilePath());
                        if(checkBox != null)
                            checkBox.setChecked(false, true);
                    } else {
                        //之前没有选择-现在执行选择
                        if (Constant.files.size() >= Constant.FILE_COUNT_MAX) {
                            //如果已经选择的数量大于最大数量，提示
                            Toast.makeText(SDCardFragment.this.getActivity(), Constant.FILE_COUNT_MAX_PROMPT, Toast.LENGTH_SHORT).show();
                        }else {
                            //如果已经选择的数量不大于最大数量，添加并选择
                            fileInfos.get(position).setIsCheck(!isCheck);
                            Constant.files.put(fileInfos.get(position).getFilePath(), fileInfos.get(position));
                            if(checkBox != null)
                                checkBox.setChecked(true, true);
                        }
                    }
                    OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiverCheckFileHandler.class);
                    updateSizAndCount();
                } else {
                    showFiles(new File(fileInfos.get(position).getFilePath()));
                }

            }
        });

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
}
