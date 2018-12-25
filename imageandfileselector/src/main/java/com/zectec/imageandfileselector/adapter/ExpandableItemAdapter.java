package com.zectec.imageandfileselector.adapter;

import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.chad.library.adapter.base.entity.MultiItemEntity;
import com.zectec.imageandfileselector.R;
import com.zectec.imageandfileselector.base.Constant;
import com.zectec.imageandfileselector.bean.FileInfo;
import com.zectec.imageandfileselector.bean.SubItem;
import com.zectec.imageandfileselector.utils.FileUtil;
import com.zectec.imageandfileselector.utils.OperateReceiveHandlerUtilSync;
import com.zectec.imageandfileselector.view.CheckBox;


import java.util.List;

import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiverCheckFileHandler;

/**
 * Created by CWJ on 2017/3/22.
 */

public class ExpandableItemAdapter extends BaseMultiItemQuickAdapter<MultiItemEntity, BaseViewHolder> {
    public static final int HEAD = 0;
    public static final int CONTENT = 1;
    private boolean isPhoto = false;

    public ExpandableItemAdapter(List<MultiItemEntity> data, boolean isPhoto) {
        super(data);
        this.isPhoto = isPhoto;
        addItemType(HEAD, R.layout.item_head);
        if (isPhoto) {
            addItemType(CONTENT, R.layout.item_content_photo);
        } else {
            addItemType(CONTENT, R.layout.item_content);
        }

    }

    @Override
    protected void convert(final BaseViewHolder helper, MultiItemEntity item) {

        switch (helper.getItemViewType()) {
            case HEAD:
                final SubItem subItem = (SubItem) item;
                if (null == subItem.getSubItems() || subItem.getSubItems().size() == 0) {
                    helper.setText(R.id.tv_count, mContext.getString(R.string.count, "" + 0));
                } else {
                    helper.setText(R.id.tv_count, mContext.getString(R.string.count, "" + subItem.getSubItems().size()));
                }

                helper.setText(R.id.tv_title, subItem.getTitle());
                helper.setImageResource(R.id.expanded_menu, subItem.isExpanded() ? R.drawable.ic_arrow_drop_down_grey_700_24dp : R.drawable.ic_arrow_drop_up_grey_700_24dp);
                helper.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int pos = helper.getAdapterPosition();
                        if (subItem.isExpanded()) {
                            collapse(pos);
                        } else {
                            expand(pos);
                        }
                    }
                });
                break;
            case CONTENT:
                final FileInfo f = (FileInfo) item;
                helper.setText(R.id.tv_content, f.getFileName())
                        .setText(R.id.tv_size, FileUtil.FormetFileSize(f.getFileSize()))
                        .setText(R.id.tv_time, f.getTime());
                if (isPhoto) {
                    Glide.with(mContext).load(f.getFilePath()).into((ImageView) helper.getView(R.id.iv_cover));
                } else {
                    Glide.with(mContext).load(FileUtil.getFileTypeImageId(mContext, f.getFilePath())).fitCenter().into((ImageView) helper.getView(R.id.iv_cover));
                }
                helper.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (isPhoto) {
                            boolean IsPhoto = f.getIsPhoto();
                            f.setIsPhoto(!IsPhoto);
                        } else {
                            f.setIsPhoto(false);
                        }

                        boolean isCheck = f.getIsCheck();
                        if (isCheck) {
                            //之前已经选择-现在执行取消选择
                            f.setIsCheck(!isCheck);
                            Constant.files.remove(f.getFilePath());
                            ((CheckBox) helper.getView(R.id.cb_file)).setChecked(false, true);
                        } else {
                            //之前没有选择-现在执行选择
                            if (Constant.files.size() >= Constant.FILE_COUNT_MAX) {
                                //如果已经选择的数量大于最大数量，提示
                                Toast.makeText(mContext, Constant.FILE_COUNT_MAX_PROMPT, Toast.LENGTH_SHORT).show();
                            }else {
                                //如果已经选择的数量不大于最大数量，添加并选择
                                f.setIsCheck(!isCheck);
                                Constant.files.put(f.getFilePath(), f);
                                ((CheckBox) helper.getView(R.id.cb_file)).setChecked(true, true);
                            }


                        }
                        OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiverCheckFileHandler.class);
                    }
                });
                if (f.getIsCheck()) {
                    ((CheckBox) helper.getView(R.id.cb_file)).setChecked(true, true);
                } else {
                    ((CheckBox) helper.getView(R.id.cb_file)).setChecked(false, true);
                }
                break;
        }
    }
}
