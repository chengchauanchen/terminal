package cn.vsx.vc.dialog;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ImageView;

import cn.vsx.vc.R;

/**
 * 作者：ly-xuxiaolong
 * 版本：1.0
 * 创建日期：2018/9/21
 * 描述：
 * 修订历史：
 */

public class ChooseCameraDialog extends Dialog{

    private ImageView phoneCamera;
    private ImageView outCamera;

    public ChooseCameraDialog(@NonNull Context context){
        super(context);
        init(context);
    }

    public ChooseCameraDialog(@NonNull Context context, int themeResId){
        super(context, themeResId);
        init(context);
    }

    private void init(Context context){
        View view = View.inflate(context, R.layout.choose_camara, null);
        setContentView(view);
        getWindow().setBackgroundDrawableResource(R.color.TRANSPARENT);
        phoneCamera = view.findViewById(R.id.iv_phone_camera);
        outCamera = view.findViewById(R.id.iv_out_camera);
        setCancelable(true);
    }

    public void setPhoneCameraClickListener(View.OnClickListener clickListener){
        phoneCamera.setOnClickListener(clickListener);
    }

    public void setOutCameraClickListener(View.OnClickListener clickListener){
        outCamera.setOnClickListener(clickListener);
    }
}
