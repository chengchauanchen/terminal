package cn.vsx.vc.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.blankj.utilcode.util.ScreenUtils;
import com.bumptech.glide.Glide;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cn.vsx.vc.R;

/**
 * Created by Administrator on 2017/3/14 0014.
 * 录像文件adapter
 */

public class FileListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private Context context;
    private List<File> files = new ArrayList<>();
    private int width = 0;
    private OnItemClickListener onItemClickListener;
    public FileListAdapter(Context context, List<File>files){
        this.context = context;
        this.files = files;
        width = ScreenUtils.getScreenWidth()/3;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
            View view = LayoutInflater.from(context).inflate(R.layout.item_file_list,parent,false);
            return new VideoListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position){
        try{
            VideoListViewHolder vHolder = (VideoListViewHolder) holder;
            final File file = files.get(position);
            if (file!=null&&file.exists()) {
                Glide.with(context).load(file.getAbsolutePath()).centerCrop().into(vHolder.iv_file);
                vHolder.itemView.setOnClickListener(view -> {
                    if(onItemClickListener!=null){
                        onItemClickListener.onFileListClick(position,file);
                    }
                });
            }else{
                vHolder.itemView.setOnClickListener(null);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount(){
        return files.size();
    }

    class VideoListViewHolder extends RecyclerView.ViewHolder{
        ImageView iv_file;
        VideoListViewHolder(View itemView) {
            super(itemView);
            iv_file = itemView.findViewById(R.id.iv_file);
            android.widget.LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) iv_file.getLayoutParams();
            lp.width = width;
            lp.height = width*3/4;
            iv_file.setLayoutParams(lp);
        }
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener){
        this.onItemClickListener = onItemClickListener;
    }
    public interface OnItemClickListener{
        void onFileListClick(int position,File file);
    }
}
