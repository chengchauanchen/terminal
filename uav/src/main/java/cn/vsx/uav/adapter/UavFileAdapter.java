package cn.vsx.uav.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.blankj.utilcode.util.ScreenUtils;

import org.apache.log4j.Logger;

import java.util.List;

import cn.vsx.uav.R;
import cn.vsx.uav.bean.FileBean;

import static cn.vsx.uav.constant.Constants.TYPE_COMMON;
import static cn.vsx.uav.constant.Constants.TYPE_DATE;
import static cn.vsx.uav.constant.Constants.TYPE_NULL;

/**
 * 作者：ly-xuxiaolong
 * 版本：1.0
 * 创建日期：2019/8/21
 * 描述：
 * 修订历史：
 */
public class UavFileAdapter extends RecyclerView.Adapter<UavFileAdapter.ViewHolder>{

    private List<FileBean> data;
    private LayoutInflater inflater;
    private Context context;
    private ItemClickListener itemClickListener;
    private boolean showCheckbox;
    private Logger logger = Logger.getLogger(getClass());



    public UavFileAdapter(List<FileBean> data, Context context,boolean showCheckbox){
        this.data = data;
        this.context = context;
        this.showCheckbox = showCheckbox;
        inflater = LayoutInflater.from(context);
    }

    public boolean isShowCheckbox(){
        return showCheckbox;
    }

    public void setShowCheckbox(boolean showCheckbox){
        this.showCheckbox = showCheckbox;
    }

    @Override
    public int getItemViewType(int position){
        FileBean fileBean = data.get(position);
        if(TextUtils.isEmpty(fileBean.getDate())){
            return TYPE_NULL;
        }else{
            //当前位置的下一个和当前位置的日期不同，则显示日期
            if(position + 1 <= data.size() - 1){
                FileBean nextBean = data.get(position + 1);
                if(!TextUtils.isEmpty(nextBean.getDate())){
                    if(TextUtils.equals(nextBean.getDate(),fileBean.getDate())){
                        return TYPE_COMMON;
                    }else {
                        return TYPE_DATE;
                    }
                }else {
                    return TYPE_NULL;
                }
            }else {
                return TYPE_NULL;
            }
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        View view = inflater.inflate(R.layout.layout_file_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position){
        int itemViewType = getItemViewType(position);



    }

    private void setLayoutParams(ViewHolder holder,FileBean fileBean){
        //重新设置图片的宽高
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) holder.mIvPicture.getLayoutParams();
        float itemWidth = (ScreenUtils.getScreenWidth() + 0.0f) / 5;
        layoutParams.width = ((int) itemWidth);
        float scale = (itemWidth + 0.0f) / fileBean.getHeight();
        layoutParams.height = ((int) (fileBean.getHeight() * scale));
        holder.mIvPicture.setLayoutParams(layoutParams);
    }

    @Override
    public int getItemCount(){
        logger.info("data.size():"+data.size());
        return data.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{

        private ImageView mIvPicture;
        private ImageView mIvVideo;
        private TextView mTvDuration;
        private TextView mTvDate;
        private CheckBox mCbSelectFile;

        ViewHolder(View itemView){
            super(itemView);

        }
    }

    public void setItemClickListener(ItemClickListener listener){
        this.itemClickListener = listener;
    }

    public interface ItemClickListener{
        void onItemClick(FileBean fileBean);
    }
}
