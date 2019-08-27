package cn.vsx.uav.adapter;

import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.blankj.utilcode.util.ScreenUtils;
import com.bumptech.glide.Glide;
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.util.List;

import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.tools.DateUtils;
import cn.vsx.uav.R;
import cn.vsx.uav.bean.FileBean;
import cn.vsx.uav.constant.Constants;
import cn.vsx.uav.receiveHandler.ReceiveFileSelectChangeHandler;

/**
 * 作者：ly-xuxiaolong
 * 版本：1.0
 * 创建日期：2019/8/25
 * 描述：
 * 修订历史：
 */
public class FileAdapter extends BaseMultiItemQuickAdapter<FileBean, BaseViewHolder>{

    private boolean showCheckbox;
    private ItemClickListener itemClickListener;

    public boolean isShowCheckbox(){
        return showCheckbox;
    }

    public void setShowCheckbox(boolean showCheckbox){
        this.showCheckbox = showCheckbox;
    }

    public FileAdapter(List<FileBean> data){
        super(data);
        addItemType(Constants.TYPE_DATE, R.layout.layout_file_item);
        addItemType(Constants.TYPE_COMMON, R.layout.layout_file_item);
        addItemType(Constants.TYPE_NULL, R.layout.layout_file_item);
    }

    @Override
    protected void convert(BaseViewHolder helper, FileBean item){
        TextView mTvDate = helper.getView(R.id.tv_date);
        ImageView mIvPicture = helper.getView(R.id.iv_picture);
        ImageView mIvVideo = helper.getView(R.id.iv_video);
        TextView mTvDuration = helper.getView(R.id.tv_duration);
        CheckBox mCbSelectFile = helper.getView(R.id.cb_select_file);

        if(item.getType() == Constants.TYPE_NULL){
            mIvVideo.setVisibility(View.INVISIBLE);
            mTvDuration.setVisibility(View.INVISIBLE);
            mTvDate.setVisibility(View.INVISIBLE);
            mIvPicture.setVisibility(View.INVISIBLE);
            mCbSelectFile.setVisibility(View.INVISIBLE);
        }else {
            if(item.getType() == Constants.TYPE_DATE){
                mTvDate.setVisibility(View.VISIBLE);
                mTvDate.setText(item.getDate());
            }else {
                mTvDate.setVisibility(View.INVISIBLE);
            }
            mIvPicture.setVisibility(View.VISIBLE);
            setLayoutParams(mIvPicture,item);
            Glide.with(mContext).load(item.getPath())
                    .override(item.getWidth(),item.getHeight())
                    .into(mIvPicture);


            if(!item.isVideo()){
                //图片
                mIvVideo.setVisibility(View.INVISIBLE);
                mTvDuration.setVisibility(View.INVISIBLE);
            }else{
                //视频
                mTvDuration.setText(DateUtils.getTime(item.getDuration()));
                mIvVideo.setVisibility(View.VISIBLE);
                mTvDuration.setVisibility(View.VISIBLE);
            }
            if(showCheckbox){
                mCbSelectFile.setVisibility(View.VISIBLE);
                if(item.isSelected()){
                    mCbSelectFile.setChecked(true);
                }else {
                    mCbSelectFile.setChecked(false);
                }
            }else {
                mCbSelectFile.setVisibility(View.INVISIBLE);
            }
            mIvPicture.setOnClickListener(v -> {
                if(showCheckbox){
                    if(item.isSelected()){
                        item.setSelected(false);
                        mCbSelectFile.setChecked(false);
                    }else {
                        item.setSelected(true);
                        mCbSelectFile.setChecked(true);
                    }
                    notifyItemChanged(mData.indexOf(item));
                    TerminalFactory.getSDK().notifyReceiveHandler(ReceiveFileSelectChangeHandler.class,item.isSelected(),item);
                }else {
                    if(itemClickListener != null){
                        itemClickListener.onItemClick(item);
                    }
                }
            });

//            mCbSelectFile.setOnCheckedChangeListener((buttonView, isChecked) -> {
//                if(isChecked){
//                    item.setSelected(false);
//                    mCbSelectFile.setChecked(false);
//                }else {
//                    item.setSelected(true);
//                    mCbSelectFile.setChecked(true);
//                }
//                notifyItemChanged(mData.indexOf(item));
//                TerminalFactory.getSDK().notifyReceiveHandler(ReceiveFileSelectChangeHandler.class,isChecked,item);
//            });
        }

    }

    private void setLayoutParams(ImageView mIvPicture, FileBean fileBean){
        //重新设置图片的宽高
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mIvPicture.getLayoutParams();
        float itemWidth = (ScreenUtils.getScreenWidth() + 0.0f) / 5;
        layoutParams.width = ((int) itemWidth);
        float scale = (itemWidth + 0.0f) / fileBean.getHeight();
        layoutParams.height = ((int) (fileBean.getHeight() * scale));
        mIvPicture.setLayoutParams(layoutParams);
    }

    public void setItemClickListener(ItemClickListener listener){
        this.itemClickListener = listener;
    }

    public interface ItemClickListener{
        void onItemClick(FileBean fileBean);
    }
}
