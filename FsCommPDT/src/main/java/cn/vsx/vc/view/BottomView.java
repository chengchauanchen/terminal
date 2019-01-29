package cn.vsx.vc.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.Nullable;
import android.support.v4.widget.TextViewCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import cn.vsx.vc.R;

/**
 * 作者：ly-xuxiaolong
 * 版本：1.0
 * 创建日期：2018/4/9
 * 描述：
 * 修订历史：
 */

public class BottomView extends LinearLayout{

    private ImageView iv_image;
    private TextView tv_text;
    private boolean selected;
    private TextView tv_badge;
    private Context context;

    public BottomView(Context context){
        super(context);
        this.context = context;
        initView();
        initCustomAttrs(context, null);
    }

    public BottomView(Context context, @Nullable AttributeSet attrs){
        super(context, attrs);
        this.context = context;
        initView();
        initCustomAttrs(context, attrs);
    }

    public BottomView(Context context, @Nullable AttributeSet attrs, int defStyleAttr){
        super(context, attrs, defStyleAttr);
        this.context = context;
        initView();
        initCustomAttrs(context, attrs);
    }

    private void initView(){
        String infServie = Context.LAYOUT_INFLATER_SERVICE;
        LayoutInflater layoutInflater =  (LayoutInflater) getContext().getSystemService(infServie);
        View view = layoutInflater.inflate(R.layout.layout_bottom_view, this, true);
        iv_image =  view.findViewById(R.id.iv_image);
        tv_text =  view.findViewById(R.id.tv_text);
        tv_badge =  view.findViewById(R.id.tv_badge);
    }

    private void initCustomAttrs(Context context, AttributeSet attrs){
        //获取自定义属性。
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.BottomView);
        boolean selected = ta.getBoolean(R.styleable.BottomView_selected, false);
        this.selected = selected;
        //获取字体大小,默认大小是16dp
        int textSize = (int) ta.getDimension(R.styleable.BottomView_textSize, 12);
        //获取文字内容
        String text = ta.getString(R.styleable.BottomView_text);
        //获取文字颜色，默认颜色是BLUE
        int src = ta.getResourceId(R.styleable.BottomView_src, R.drawable.talkback1);
        ta.recycle();
        tv_text.setText(text);
        tv_text.setTextSize(textSize);
        iv_image.setImageResource(src);
        iv_image.setSelected(selected);
        setTextColor(selected);
    }

    private void setTextColor(boolean selected){
        if(selected){
            iv_image.setSelected(true);
            tv_text.setSelected(true);
            TextViewCompat.setTextAppearance(tv_text,R.style.bottom_select_text_color);
        }else {
            iv_image.setSelected(false);
            tv_text.setSelected(false);
            TextViewCompat.setTextAppearance(tv_text,R.style.bottom_unselect_text_color);
        }
    }

    @Override
    public void setSelected(boolean isSelected){
        super.setSelected(isSelected);
        this.selected = isSelected;
        setTextColor(selected);
    }

    @Override
    public boolean isSelected(){
        return selected;
    }

    public void setBadgeViewCount(int unreadMessageCount){
        if (unreadMessageCount > 99) {
            tv_badge.setVisibility(View.VISIBLE);
            tv_badge.setText("99+");
        } else if (unreadMessageCount > 0 && unreadMessageCount <= 99) {
            tv_badge.setVisibility(View.VISIBLE);
            tv_badge.setText(String.valueOf(unreadMessageCount));
        } else {
            tv_badge.setVisibility(View.GONE);
        }
    }
}
