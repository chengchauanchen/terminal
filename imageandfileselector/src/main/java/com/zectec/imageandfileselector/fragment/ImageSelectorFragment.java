package com.zectec.imageandfileselector.fragment;

import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.zectec.imageandfileselector.R;
import com.zectec.imageandfileselector.adapter.ImageAdapter;
import com.zectec.imageandfileselector.base.BaseFragment;
import com.zectec.imageandfileselector.base.Constant;
import com.zectec.imageandfileselector.bean.Image;
import com.zectec.imageandfileselector.callback.DataCallback;
import com.zectec.imageandfileselector.receivehandler.ReceiverSendFileCheckMessageHandler;
import com.zectec.imageandfileselector.receivehandler.ReceiverSendFileHandler;
import com.zectec.imageandfileselector.utils.FileUtil;
import com.zectec.imageandfileselector.utils.OperateReceiveHandlerUtilSync;

import java.util.ArrayList;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

/**
 * Created by gt358 on 2017/8/24.
 */

public class ImageSelectorFragment extends BaseFragment implements DataCallback<ArrayList<Image>>{
    ImageView ib_back;
    TextView tv_confirm;
    RecyclerView rv_image;

    ImageAdapter imageAdapter;

    @Override
    protected boolean isBindEventBusHere() {
        return false;
    }

    @Override
    public int getLayoutResource() {
        Constant.images.clear();
        return R.layout.fragment_image_selector;
    }

    @Override
    public void initView() {
        ib_back = (ImageView) rootView.findViewById(R.id.ib_back);
        tv_confirm = (TextView) rootView.findViewById(R.id.tv_confirm);
        rv_image = (RecyclerView) rootView.findViewById(R.id.rv_image);
        imageAdapter = new ImageAdapter(getContext(), 5, false);
        rv_image.setLayoutManager(new GridLayoutManager(getContext(), 3));
        rv_image.setAdapter(imageAdapter);
        ((SimpleItemAnimator) rv_image.getItemAnimator()).setSupportsChangeAnimations(false);
        ib_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popBackStack();
                Constant.images.clear();
                OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiverSendFileCheckMessageHandler.class, 0, false, 0);
            }
        });
        tv_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Constant.images.size() > 0){
                    OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiverSendFileHandler.class, ReceiverSendFileHandler.PHOTO_ALBUM);
                }else {
                    Toast.makeText(getActivity(),"请选择至少一张图片",Toast.LENGTH_SHORT).show();
                }

            }
        });

        FileUtil.loadImageForSDCard(getContext(), this);
        imageAdapter.setOnImageSelectListener(new ImageAdapter.OnImageSelectListener() {
            @Override
            public void OnImageSelect(Image image, boolean isSelect, int selectCount) {
                if(Constant.images.size() > 0) {
                    tv_confirm.setBackgroundResource(R.drawable.shape_bt_send_blue);

                }
                else {
                    tv_confirm.setBackgroundResource(R.drawable.shape_bt_send_blue_nofile);

                }
                tv_confirm.setText("发送(" + Constant.images.size() + "/5)");
            }
        });
    }

    @Override
    public void onSuccess(final ArrayList<Image> images) {
        Observable.just("")
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String s) {
                        imageAdapter.refresh(images);
                    }
                });
    }

    public void popBackStack () {
        getFragmentManager().popBackStack();
    }

}
