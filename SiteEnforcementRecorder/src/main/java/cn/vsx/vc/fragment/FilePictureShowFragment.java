package cn.vsx.vc.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bm.library.PhotoView;
import com.bumptech.glide.Glide;
import com.zectec.imageandfileselector.utils.OperateReceiveHandlerUtilSync;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.vsx.vc.R;
import cn.vsx.vc.receiveHandle.ReceiverFragmentPopBackStackHandler;
import cn.vsx.vc.utils.Constants;

/**
 * Created by gt358 on 2017/10/20.
 */

public class FilePictureShowFragment extends Fragment implements View.OnClickListener {

    @Bind(R.id.iv_return)
    ImageView ivReturn;
    @Bind(R.id.tv_title)
    TextView tvTitle;
    @Bind(R.id.iv_close)
    ImageView ivClose;

    @Bind(R.id.preview_pager)
    ViewPager viewPager;


    private ArrayList<String> filePaths = new ArrayList<>();
    private int fileIndex = 0;
    public Logger logger = Logger.getLogger(getClass());

    public static FilePictureShowFragment newInstance() {
        return new FilePictureShowFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try{
            if (getArguments() != null) {
                ArrayList<String> filePathsList = getArguments().getStringArrayList(Constants.FILE_PATHS);
                fileIndex = getArguments().getInt(Constants.FILE_INDEX);
                filePaths.clear();
                if(filePathsList!=null){
                    filePaths.addAll(filePathsList);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_file_picture_show, container, false);
        ButterKnife.bind(this, view);
        initView();
        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
        } catch (Exception e) {
           e.printStackTrace();
        }
    }

    /**
     * 初始化布局
     */
    private void initView() {
        try{
            ivReturn.setVisibility(View.VISIBLE);
            tvTitle.setPadding(0, 0, 0, 0);
            ivClose.setVisibility(View.INVISIBLE);
            //判断数据是否为空
            if(filePaths.isEmpty()){
                tvTitle.setText(String.format(getString(R.string.text_show_picture_index), 0, 0));
                return;
            }
            //判断异常情况
            if(fileIndex<0|| fileIndex >= filePaths.size()){
                fileIndex = 0;
            }
            tvTitle.setText(String.format(getString(R.string.text_show_picture_index), (fileIndex+1), filePaths.size()));

            viewPager.setAdapter(new MyAdapter(filePaths, getActivity()));
            viewPager.setCurrentItem(fileIndex);
            viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                }

                @Override
                public void onPageSelected(int position) {
                    try{
                        tvTitle.setText(String.format(getString(R.string.text_show_picture_index), (position + 1), filePaths.size()));
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }

                @Override
                public void onPageScrollStateChanged(int state) {
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }
    }



    @OnClick({R.id.iv_return})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_return:
                //关闭页面
                OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiverFragmentPopBackStackHandler.class);
                break;
                default:break;
        }
    }

    class MyAdapter extends PagerAdapter {
        private List<String> mImageList;
        private Context mContext;

        MyAdapter(List<String> data, Context mContext){
            this.mImageList = data;
            this.mContext = mContext;
        }

        @Override
        public int getCount(){
            return mImageList.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object){
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position){
            PhotoView view = new PhotoView(mContext);
            view.enable();
            view.setScaleType(ImageView.ScaleType.FIT_CENTER);
            Glide.with(getActivity())
                    .load(mImageList.get(position))
//                    .placeholder(R.drawable.default_image)
                    //.diskCacheStrategy(DiskCacheStrategy.NONE)
                    //.skipMemoryCache(true)
                    .into(view);
            container.addView(view);
//            view.setOnClickListener(view1 -> desBitmap(true));
            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object){
            container.removeView((View) object);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
//        mHandler.removeCallbacksAndMessages(null);
    }
}
