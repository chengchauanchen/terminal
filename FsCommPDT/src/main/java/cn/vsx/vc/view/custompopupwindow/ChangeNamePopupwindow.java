package cn.vsx.vc.view.custompopupwindow;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.PopupWindow;

import cn.vsx.hamster.terminalsdk.tools.DataUtil;
import cn.vsx.hamster.terminalsdk.tools.Params;
import cn.vsx.hamster.terminalsdk.tools.Util;
import cn.vsx.vc.R;
import ptt.terminalsdk.context.MyTerminalFactory;
import ptt.terminalsdk.tools.ToastUtil;

/**
 * Created by gt358 on 2017/9/26.
 */

public class ChangeNamePopupwindow extends PopupWindow implements View.OnClickListener{


    EditText et_change_name;

    private Context context;
    public ChangeNamePopupwindow(Context context) {
        this.context = context;
        initView(context);
    }

    private void initView (Context context) {
        View popupWindowView = View.inflate(context, R.layout.popu_change_name, null);
        setContentView(popupWindowView);
        et_change_name = popupWindowView.findViewById(R.id.et_change_name);
        popupWindowView.findViewById(R.id.ib_back_change_name).setOnClickListener(this);
        popupWindowView.findViewById(R.id.tv_save).setOnClickListener(this);
        setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        setHeight(ViewGroup.LayoutParams.MATCH_PARENT);
        //是否响应touch事件
        setTouchable(true);
        //是否具有获取焦点的能力
        setFocusable(true);
        //外部是否可以点击
        setOutsideTouchable(false);//android6.0按返回键还是能关闭popuwindow

        et_change_name.setText(MyTerminalFactory.getSDK().getParam(Params.MEMBER_NAME, ""));
        et_change_name.setSelection(et_change_name.getEditableText().length());
        et_change_name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().contains(" ")) {
                    String[] str = s.toString().split(" ");
                    String str1 = "";
                    for (String aStr : str) {
                        str1 += aStr;
                    }
                    et_change_name.setText(str1);

                    et_change_name.setSelection(start);

                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }


    public void onClick (View view) {
        int i = view.getId();
        if(i == R.id.ib_back_change_name){
            dismiss();
        }else if(i == R.id.tv_save){
            String newName = et_change_name.getText().toString();
            if(Util.isEmpty(newName)){
                ToastUtil.showToast(context, context.getString(R.string.text_please_input_name));
            }else if(!DataUtil.isLegalName(newName) || newName.length() > 7 || newName.length() < 2){
                ToastUtil.showToast(context, context.getString(R.string.text_please_input_correct_name));
            }else{
                //上传名字
                if(newName.equals(MyTerminalFactory.getSDK().getParam(Params.MEMBER_NAME, ""))){
                }else{
                    MyTerminalFactory.getSDK().getConfigManager().changeName(newName);
                }
                dismiss();
            }
        }
    }

}
