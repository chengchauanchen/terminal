package cn.vsx.vc.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import cn.vsx.vc.R;

public class CreateTemporaryGroupsDialog extends Dialog {

    private ImageView icon;
    private TextView content;
    private TextView toJump;

    //创建临时组-创建中
    public static final int CREATE_GROUP_STATE_CREATTING = 1;
    //创建临时组-创建成功
    public static final int CREATE_GROUP_STATE_SUCCESS = 2;
    //创建临时组-创建失败
    public static final int CREATE_GROUP_STATE_FAIL = 3;

    public CreateTemporaryGroupsDialog(Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_create_temporary_group);
        initView();
        init();
    }

    private void initView() {
         icon = (ImageView)findViewById(R.id.dialog_icon);
         content = (TextView)findViewById(R.id.text_context);
         toJump = (TextView)findViewById(R.id.text_toJump);
    }

    private void init() {
        Display display = getWindow().getWindowManager().getDefaultDisplay();
        int heigth = display.getWidth();
        int width = display.getHeight();
        Window window =getWindow();
        WindowManager.LayoutParams layoutParams = window.getAttributes();
        layoutParams.width=width/2;
        layoutParams.height=heigth/2;
        window.setAttributes(layoutParams);
    }

    /**
     * 更新创建临时组的弹窗
     * @param type
     * @param failMessage
     */
    public void updateTemporaryGroupDialog(int type,String failMessage,boolean forceSwitchGroup){
        show();
        switch (type){
            case CREATE_GROUP_STATE_CREATTING:
                //创建临时组-创建中
                icon.setImageResource(R.drawable.dialog_icon_creatting);
                content.setText("正在创建...");
                toJump.setVisibility(View.INVISIBLE);
                break;
            case CREATE_GROUP_STATE_SUCCESS:
                //创建临时组-创建成功
                icon.setImageResource(R.drawable.dialog_icon);
                content.setText("创建成功!");
                if(forceSwitchGroup){
                    toJump.setVisibility(View.VISIBLE);
                    toJump.setText("正在跳转....");
                }else {
                    toJump.setVisibility(View.INVISIBLE);
                }
                break;
            case CREATE_GROUP_STATE_FAIL:
                //创建临时组-创建失败
                icon.setImageResource(R.drawable.dialog_icon_fail);
                content.setText("创建失败!");
                failMessage = TextUtils.isEmpty(failMessage)?"":failMessage;
                toJump.setText(failMessage);
                toJump.setVisibility(View.VISIBLE);
                break;
        }
    }

}
