package com.zectec.imageandfileselector.fragment;

import android.app.ProgressDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.FrameLayout;

import com.chad.library.adapter.base.entity.MultiItemEntity;

import java.util.ArrayList;
import java.util.List;

import com.zectec.imageandfileselector.R;
import com.zectec.imageandfileselector.adapter.ExpandableItemAdapter;
import com.zectec.imageandfileselector.base.BaseFragment;
import com.zectec.imageandfileselector.bean.FolderInfo;
import com.zectec.imageandfileselector.bean.SubItem;
import com.zectec.imageandfileselector.utils.LocalMediaLoader;

/**
 * Created by CWJ on 2017/3/21.
 */

public class PhotoFragment extends BaseFragment {
    private RecyclerView rlv_photo;
    private ExpandableItemAdapter mPhotoExpandableItemAdapter;
    private ArrayList<MultiItemEntity> mEntityArrayList = new ArrayList<>();
    FrameLayout fl_progress_bar;

    @Override
    public boolean isBindEventBusHere() {
        return true;
    }

    @Override
    public int getLayoutResource() {
        return R.layout.fragment_photo;
    }

    @Override
    public void initView() {
        rlv_photo = (RecyclerView) getActivity().findViewById(R.id.rlv_photo);
        rlv_photo.setLayoutManager(new LinearLayoutManager(getActivity()));
        mPhotoExpandableItemAdapter = new ExpandableItemAdapter(mEntityArrayList, true);
        rlv_photo.setAdapter(mPhotoExpandableItemAdapter);
        fl_progress_bar = (FrameLayout) rootView.findViewById(R.id.fl_progress_bar);
        fl_progress_bar.setVisibility(View.VISIBLE);//将进度条显示出来
        new LocalMediaLoader(getActivity(), LocalMediaLoader.TYPE_IMAGE).loadAllImage(new LocalMediaLoader.LocalMediaLoadListener() {
            @Override
            public void loadComplete(List<FolderInfo> folders) {
                fl_progress_bar.setVisibility(View.GONE);
                for (int i = 0; i < folders.size(); i++) {
                    SubItem subItem = new SubItem(folders.get(i).getName());
                    for (int j = 0; j < folders.get(i).getImages().size(); j++) {
                        subItem.addSubItem(folders.get(i).getImages().get(j));
                    }
                    mEntityArrayList.add(subItem);
                }
                mPhotoExpandableItemAdapter.notifyDataSetChanged();
            }
        });
    }
}
