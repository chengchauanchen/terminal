package cn.vsx.vc.dialog;

import android.app.Dialog;
import android.content.Context;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.view.Display;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.vsx.vc.R;

public class EditThemeDialog extends Dialog {

    private EditText et_live_edit_import_theme;
    private Button btn_live_edit_confirm;
    private ImageView iv_live_edit_return;

    InputFilter emojiFilter = new InputFilter() {
        Pattern emoji = Pattern.compile("[\ud83c\udc00-\ud83c\udfff]|[\ud83d\udc00-\ud83d\udfff]|[\u2600-\u27ff]",
                Pattern.UNICODE_CASE | Pattern.CASE_INSENSITIVE);

        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            Matcher emojiMatcher = emoji.matcher(source);
            if (emojiMatcher.find()) {
                return "";
            }
            return null;
        }
    };

    public EditThemeDialog(Context context) {
		super(context,R.style.change_name_dialog);
		View view = View.inflate(context, R.layout.live_edit_theme, null);

		et_live_edit_import_theme = (EditText) view.findViewById(R.id.et_live_edit_import_theme);
		btn_live_edit_confirm = (Button) view.findViewById(R.id.btn_live_edit_confirm);
		iv_live_edit_return = (ImageView) view.findViewById(R.id.iv_live_edit_return);

        Display display = this.getWindow().getWindowManager().getDefaultDisplay();
        int width = display.getWidth();
        int height = display.getHeight();
        //设置dialog的宽高为屏幕的宽高
        ViewGroup.LayoutParams layoutParams = new  ViewGroup.LayoutParams(width, height);

		setContentView(view, layoutParams);
		setCancelable(false);
        ArrayList<InputFilter> allInputFilters = new ArrayList<>();
        allInputFilters.addAll(Arrays.asList(et_live_edit_import_theme.getFilters()));
        allInputFilters.add(emojiFilter);

        et_live_edit_import_theme.setFilters(allInputFilters.toArray(new InputFilter[]{}));

        et_live_edit_import_theme.setOnEditorActionListener(new OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (event.getKeyCode() == KeyEvent.KEYCODE_ENTER || event.getKeyCode() == KeyEvent.KEYCODE_SPACE){
                    return true;
                }
                return false;
            }
        });
        et_live_edit_import_theme.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            public void afterTextChanged(Editable s) {
                for(int i = s.length(); i > 0; i--){
                    //监听输入的字符，如果是换行的就用“”替换
                    if(s.subSequence(i-1, i).toString().equals("\n"))
                        s.replace(i-1, i, "");
                }
            }
        });
    }

    public EditText getEditText() {
		return et_live_edit_import_theme;
	}
    
    /** 
     * 确定键监听器 
     */  
    public void setOnPositiveListener(View.OnClickListener listener){
		btn_live_edit_confirm.setOnClickListener(listener);
    }  
    /** 
     * 取消键监听器 
     */  
    public void setOnNegativeListener(View.OnClickListener listener){
		iv_live_edit_return.setOnClickListener(listener);
    }

}