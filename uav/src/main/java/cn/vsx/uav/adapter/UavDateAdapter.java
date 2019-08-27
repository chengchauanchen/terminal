package cn.vsx.uav.adapter;

import android.content.Context;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.File;
import java.util.List;

import cn.vsx.uav.R;
import cn.vsx.uav.bean.FileBean;

/**
 * 作者：ly-xuxiaolong
 * 版本：1.0
 * 创建日期：2019/8/21
 * 描述：
 * 修订历史：
 */
public class UavDateAdapter extends RecyclerView.Adapter<UavDateAdapter.ViewHolder>{

    private List<File> data;
    private LayoutInflater inflater;
    private Context context;
    private FileClickListener fileClickListener;
    private boolean showCheckbox;
    private Handler mHandler = new Handler();

    public UavDateAdapter(List<File> data, Context context){
        this.data = data;
        this.context = context;
        this.showCheckbox = showCheckbox;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        return new ViewHolder(inflater.inflate(R.layout.layout_date_item, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position){
        File date = data.get(position);
        holder.mTvDate.setText(date.getName());
//        holder.mUavFileRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(5, StaggeredGridLayoutManager.VERTICAL));
//        holder.mUavFileRecyclerView.addItemDecoration(new CommonItemDecoration(20,20));
//        List<FileBean> itemData = new ArrayList<>();
//        UavFileAdapter uavFileAdapter = new UavFileAdapter(itemData,context,showCheckbox);
//        holder.mUavFileRecyclerView.setAdapter(uavFileAdapter);
//        uavFileAdapter.setItemClickListener(fileBean -> {
//            if(fileClickListener != null){
//                fileClickListener.onFileClick(fileBean);
//            }
//        });
//        TerminalFactory.getSDK().getThreadPool().execute(()-> setData(itemData,uavFileAdapter,date));
    }

    private void setData(List<FileBean> itemData,UavFileAdapter uavFileAdapter,File date){


    }

    @Override
    public int getItemCount(){
        return data.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{

        private TextView mTvDate;
//        private RecyclerView mUavFileRecyclerView;

        ViewHolder(View itemView){
            super(itemView);
            mTvDate = itemView.findViewById(R.id.tv_date);
//            mUavFileRecyclerView = itemView.findViewById(R.id.uav_file_recycler_view);
        }
    }

    public void setFileClickListener(FileClickListener listener){
        this.fileClickListener = listener;
    }

    public interface FileClickListener{
        void onFileClick(FileBean fileBean);
    }
}
