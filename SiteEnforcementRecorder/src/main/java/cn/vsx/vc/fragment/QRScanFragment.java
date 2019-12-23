package cn.vsx.vc.fragment;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.google.gson.Gson;
import com.uuzuche.lib_zxing.DisplayUtil;
import com.zectec.imageandfileselector.utils.OperateReceiveHandlerUtilSync;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.vsx.hamster.terminalsdk.model.RecorderBindTranslateBean;
import cn.vsx.hamster.terminalsdk.tools.Params;
import cn.vsx.vc.R;
import cn.vsx.vc.receiveHandle.ReceiverFragmentPopBackStackHandler;
import cn.vsx.vc.utils.CodeUtils;
import ptt.terminalsdk.context.MyTerminalFactory;

/**
 * Created by gt358 on 2017/10/20.
 */

public class QRScanFragment extends Fragment implements View.OnClickListener {


    @Bind(R.id.iv_qr)
    ImageView ivQr;

    //生成二维码的图片大小
    private int bitmapSize = 0;
    public static QRScanFragment newInstance() {
        QRScanFragment fragment = new QRScanFragment();
        Bundle args = new Bundle();
//        args.putInt(TYPE, type);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
//            jumpType = getArguments().getInt(TYPE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_qr_scan, container, false);
        ButterKnife.bind(this, view);

        bitmapSize = DisplayUtil.dip2px(this.getContext(),300);
        initView();
        return view;
    }

    /**
     * 初始化布局
     */
    private void initView() {
        //生成二维码
//        int memberId = MyTerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0);
        long uniqueNo = MyTerminalFactory.getSDK().getParam(Params.MEMBER_UNIQUENO,0L);
        String content = new Gson().toJson(new RecorderBindTranslateBean(0,uniqueNo,0,"",""));
        Bitmap mBitmap = CodeUtils.createImage(content, bitmapSize, bitmapSize, null);
        ivQr.setImageBitmap(mBitmap);
    }

    @OnClick({R.id.iv_close})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_close:
                //关闭页面
                OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiverFragmentPopBackStackHandler.class);
                break;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }
}
