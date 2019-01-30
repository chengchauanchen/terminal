package com.zectec.imageandfileselector.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zectec.imageandfileselector.R;
import com.zectec.imageandfileselector.bean.FaceRecognitionBean;
import com.zectec.imageandfileselector.receivehandler.ReceiverToFaceRecognitionHandler;
import com.zectec.imageandfileselector.utils.OperateReceiveHandlerUtilSync;
import com.zectec.imageandfileselector.utils.PhotoUtils;

import java.util.ArrayList;
import java.util.List;

/**
 *  人脸识别列表的adapter
 * Created by gt358 on 2017/10/11.
 */

public class FaceRecognitionAdapter extends BaseAdapter {

    private final int FIRST = 0;
    private final int OTHERS = 1;

    Context context;
    List<FaceRecognitionBean> list = new ArrayList<>();

    public FaceRecognitionAdapter(Context context, List<FaceRecognitionBean> list) {
        this.context = context;
        this.list.addAll(list);
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        if(position == 0) {
            return FIRST;
        }
        else {
            return OTHERS;
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final FaceRecognitionBean faceRecognitionBean = list.get(position);
        ViewHolder holder = null;
        final int viewType = getItemViewType(position);

        if(convertView == null) {
            convertView = getViewByType(viewType, parent);
            holder = new ViewHolder();
            holder.tv_title_face_pair = (TextView)convertView.findViewById(R.id.tv_title_face_pair);
            holder.tv_matched_degree = (TextView)convertView.findViewById(R.id.tv_matched_degree);
            holder.tv_content_face_pair = (TextView)convertView.findViewById(R.id.tv_content_face_pair);
            holder.iv_photo_face_pair = (ImageView)convertView.findViewById(R.id.iv_photo_face_pair);
            holder.ll_face_pair = (LinearLayout)convertView.findViewById(R.id.ll_face_pair);
            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.tv_title_face_pair.setText(faceRecognitionBean.getTitle());
        holder.tv_matched_degree.setText(faceRecognitionBean.getMatcheDegree()+"%");
        holder.tv_content_face_pair.setText(faceRecognitionBean.getContent());
        PhotoUtils.getInstance().loadNetBitmap2(context, faceRecognitionBean.getPictureUrl(), holder.iv_photo_face_pair);
        if(position == 0) {
            holder.tv_matched_degree.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.matched1));
        }
        else if(position == 1) {
            holder.tv_matched_degree.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.matched2));
        }
        else {
            holder.tv_matched_degree.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.matched3));
        }
        holder.ll_face_pair.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiverToFaceRecognitionHandler.class, faceRecognitionBean.getDetailedHtml(), faceRecognitionBean.getTitle());
            }
        });
        return convertView;
    }

    private View getViewByType (int viewType, ViewGroup parent) {
        View convertView = null;
        switch (viewType) {
            case FIRST:
                convertView = View.inflate(context, R.layout.item_face_pair1, null);
                break;
            case OTHERS:
                convertView = View.inflate(context, R.layout.item_face_pair, null);
                break;
        }
        return convertView;
    }

    class ViewHolder {
        TextView tv_title_face_pair;
        TextView tv_matched_degree;
        TextView tv_content_face_pair;
        ImageView iv_photo_face_pair;
        LinearLayout ll_face_pair;
    }
}
