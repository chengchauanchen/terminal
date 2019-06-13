package cn.vsx.vc.fragment;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.bm.library.PhotoView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.zectec.imageandfileselector.base.BaseFragment;
import com.zectec.imageandfileselector.bean.ImageBean;
import com.zectec.imageandfileselector.receivehandler.ReceiverSaveImgHandler;
import com.zectec.imageandfileselector.utils.OperateReceiveHandlerUtilSync;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

import cn.vsx.vc.R;
import cn.vsx.vc.activity.BaseActivity;
import cn.vsx.vc.activity.ChatBaseActivity;
import cn.vsx.vc.utils.ToastUtil;

/**
 * Created by CWJ on 2017/3/28.
 */

public class ImagePreviewItemFragment extends BaseFragment{
    public static final String PATH = "path";
    public static final String NEED_STORE = "neddstore";
    public static final String DATA = "data";
    public static final String POSITION = "position";
    public static final String CONTENER = "fragment_contener";
    public Logger logger = Logger.getLogger(getClass());
    private FrameLayout fragment_contener;
    private Bitmap bitmap;
    private String storrPath;
    private Handler myHandler = new Handler();
    private List<ImageBean> mImageList = new ArrayList<>();


    public static ImagePreviewItemFragment getInstance(List<ImageBean> imgs, int postion){
        ImagePreviewItemFragment fragment = new ImagePreviewItemFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(POSITION, postion);
        bundle.putParcelableArrayList(DATA, (ArrayList<? extends Parcelable>) imgs);
        fragment.setArguments(bundle);
        return fragment;
    }

    public void setFragment_contener(FrameLayout fragment_contener){
        this.fragment_contener = fragment_contener;
    }


    @Override
    protected boolean isBindEventBusHere(){
        return false;
    }

    @Override
    public int getLayoutResource(){
        return R.layout.fragment_item_image_preview;
    }

    @Override
    public void initView(){
        ViewPager mViewPager = rootView.findViewById(R.id.vp);
        mImageList = getArguments().getParcelableArrayList(DATA);
        int pos = getArguments().getInt(POSITION);
        MyAdapter adapter = new MyAdapter(mImageList, getActivity());
        mViewPager.setAdapter(adapter);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener(){
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels){
            }

            @Override
            public void onPageSelected(int position){
                storrPath = mImageList.get(position).getPath();
            }

            @Override
            public void onPageScrollStateChanged(int state){
            }
        });
        mViewPager.setCurrentItem(pos);
        OperateReceiveHandlerUtilSync.getInstance().registReceiveHandler(receiverSaveImgHandler);
        ((BaseActivity) getActivity()).setBackListener(() -> {
            if(null !=fragment_contener){
                fragment_contener.setVisibility(View.GONE);
            }
            if(null != getActivity() && !isDetached()){
                getActivity().getSupportFragmentManager().beginTransaction().remove(ImagePreviewItemFragment.this).commit();
                getActivity().getSupportFragmentManager().popBackStack();
                ((BaseActivity) getActivity()).setBackListener(null);
            }
        });
    }

    private ReceiverSaveImgHandler receiverSaveImgHandler = (isSave, path) -> myHandler.post(() -> {
        if(isSave){
            ToastUtil.showToast(getActivity(),String.format(getString(R.string.text_file_save_path),path));
        }else{
            ToastUtil.showToast(getActivity(),getString(R.string.text_save_fail));
        }
    });

    private void desBitmap(boolean isPopBack){
        if(bitmap != null && !bitmap.isRecycled()){
            bitmap.recycle();
            bitmap = null;
        }
        if(isPopBack && null !=getActivity() &&(!isDetached())){
            getActivity().getSupportFragmentManager().beginTransaction().remove(this).commit();
            getActivity().getSupportFragmentManager().popBackStack();
            ((ChatBaseActivity) getActivity()).setBackListener(null);
        }
        System.gc();
        if(fragment_contener != null){
            fragment_contener.setVisibility(View.GONE);
        }
    }

    private int getInSampleSize(BitmapFactory.Options opts, String absolutePath){
        opts.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(absolutePath, opts);
        // 获取到这个图片的原始宽度和高度
        int picWidth = opts.outWidth;
        int picHeight = opts.outHeight;
        // 获取屏的宽度和高度
        WindowManager windowManager = getActivity().getWindowManager();
        Display display = windowManager.getDefaultDisplay();
        int screenWidth = display.getWidth();
        int screenHeight = display.getHeight();
        // isSampleSize是表示对图片的缩放程度，比如值为2图片的宽度和高度都变为以前的1/2
        opts.inSampleSize = 1;
        // 根据屏的大小和图片大小计算出缩放比例
        if(picWidth > picHeight){
            if(picWidth > screenWidth)
                opts.inSampleSize = picWidth / screenWidth;
        }else{
            if(picHeight > screenHeight)
                opts.inSampleSize = picHeight / screenHeight;
        }
        logger.info("计算到的压缩率：" + opts.inSampleSize);
        return opts.inSampleSize;
    }

    private boolean handleBackPressed;

    public boolean onBackPressed(){
        if(!handleBackPressed){
            desBitmap(false);
            handleBackPressed = true;
            return true;
        }else{
            return false;
        }
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        OperateReceiveHandlerUtilSync.getInstance().unregistReceiveHandler(receiverSaveImgHandler);
    }

    class MyAdapter extends PagerAdapter{
        private List<ImageBean> mImageList;
        private Context mContext;

        MyAdapter(List<ImageBean> data, Context mContext){
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
                    .load(mImageList.get(position).getPath())
//                    .placeholder(R.drawable.default_image)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .into(view);
            container.addView(view);
            view.setOnClickListener(view1 -> desBitmap(true));
            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object){
            container.removeView((View) object);
        }
    }
}
