package com.zectec.imageandfileselector.fragment;

import android.app.ProgressDialog;
import android.os.Environment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.chad.library.adapter.base.entity.MultiItemEntity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.zectec.imageandfileselector.R;
import com.zectec.imageandfileselector.adapter.ExpandableItemAdapter;
import com.zectec.imageandfileselector.base.BaseFragment;
import com.zectec.imageandfileselector.bean.FileInfo;
import com.zectec.imageandfileselector.bean.SubItem;
import com.zectec.imageandfileselector.utils.FileUtil;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by CWJ on 2017/3/21.
 */

public class OtherFragment extends BaseFragment {
    RecyclerView rlv_other;
    private List<FileInfo> fileInfos = new ArrayList<>();
    ExpandableItemAdapter mExpandableItemAdapter;
    private ArrayList<MultiItemEntity> mEntityArrayList = new ArrayList<>();
    FrameLayout fl_progress_bar;

    @Override
    public boolean isBindEventBusHere() {
        return true;
    }

    @Override
    public int getLayoutResource() {
        return R.layout.fragment_other;
    }

    @Override
    public void initView() {
        rlv_other = (RecyclerView) getActivity().findViewById(R.id.rlv_other);
        fl_progress_bar = (FrameLayout) rootView.findViewById(R.id.fl_progress_bar);
        fl_progress_bar.setVisibility(View.VISIBLE);//将进度条显示出来
        ReadOtherFile();
        rlv_other.setLayoutManager(new LinearLayoutManager(getActivity()));
        mExpandableItemAdapter = new ExpandableItemAdapter(mEntityArrayList, false);
        rlv_other.setAdapter(mExpandableItemAdapter);
    }

    private void ReadOtherFile() {
        List<File> m = new ArrayList<>();
        m.add(new File(Environment.getExternalStorageDirectory() + "/tencent/"));//微信QQ
        m.add(new File(Environment.getExternalStorageDirectory() + "/dzsh/"));//自定义
        Observable.from(m)
                .flatMap(new Func1<File, Observable<File>>() {
                    @Override
                    public Observable<File> call(File file) {
                        return listFiles(file);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        new Subscriber<File>() {
                            @Override
                            public void onCompleted() {
                                fl_progress_bar.setVisibility(View.GONE);
                                if (fileInfos.size() > 0) {
                                    SubItem ZipItem = new SubItem("ZIP文件");
                                    SubItem APPItem = new SubItem("APP文件");
                                    for (int j = 0; j < fileInfos.size(); j++) {
                                        if (FileUtil.checkSuffix(fileInfos.get(j).getFilePath(), new String[]{"zip"})) {
                                            ZipItem.addSubItem(fileInfos.get(j));
                                        } else if (FileUtil.checkSuffix(fileInfos.get(j).getFilePath(), new String[]{"apk"})) {
                                            APPItem.addSubItem(fileInfos.get(j));
                                        }
                                    }

                                    mEntityArrayList.add(ZipItem);
                                    mEntityArrayList.add(APPItem);
                                    mExpandableItemAdapter.setNewData(mEntityArrayList);
                                    mExpandableItemAdapter.notifyDataSetChanged();
                                } else {
                                    Toast.makeText(getActivity(), getString(R.string.text_sorry_no_file_read), Toast.LENGTH_LONG).show();
                                }
                            }

                            @Override
                            public void onError(Throwable e) {
                                fl_progress_bar.setVisibility(View.GONE);
                            }

                            @Override
                            public void onNext(File file) {
                                FileInfo fileInfo = FileUtil.getFileInfoFromFile(file);
                                Log.e("文件路径", "文件路径：：：" + fileInfo.getFilePath());
                                fileInfos.add(fileInfo);

                            }
                        }
                );
    }

    public static Observable<File> listFiles(final File f) {
        if (f.isDirectory()) {
            return Observable.from(f.listFiles()).flatMap(new Func1<File, Observable<File>>() {
                @Override
                public Observable<File> call(File file) {
                    return listFiles(file);
                }
            });
        } else {
            return Observable.just(f).filter(new Func1<File, Boolean>() {
                @Override
                public Boolean call(File file) {
                    return f.exists() && f.canRead() && FileUtil.checkSuffix(f.getAbsolutePath(), new String[]{"zip", "apk"});
                }
            });
        }
    }
}
