package cn.vsx.vc.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

import cn.vsx.vc.R;
import dji.ux.internal.camera.CameraSettingListView;
import dji.ux.panel.CameraSettingAdvancedPanel;

/**
 * 作者：ly-xuxiaolong
 * 版本：1.0
 * 创建日期：2019/3/29
 * 描述：
 * 修订历史：
 */
public class MyCameraSettingAdvancedPanel extends CameraSettingAdvancedPanel{
    public MyCameraSettingAdvancedPanel(Context context){
        super(context);
    }

    public MyCameraSettingAdvancedPanel(Context context, AttributeSet attributeSet){
        super(context, attributeSet);
    }

    public MyCameraSettingAdvancedPanel(Context context, AttributeSet attributeSet, int i){
        super(context, attributeSet, i);
    }

    @Override
    public void initView(Context context, AttributeSet attributeSet, int i){
        super.initView(context, attributeSet, i);
        CameraSettingListView cameraSettingListView = findViewById(R.id.camera_setting_content_other);
        cameraSettingListView.setVisibility(GONE);
        ImageView cameraTabOther = findViewById(R.id.camera_tab_other);
        cameraTabOther.setVisibility(GONE);
    }
}
