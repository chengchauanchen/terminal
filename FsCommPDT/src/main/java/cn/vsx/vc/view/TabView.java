package cn.vsx.vc.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.Nullable;
import android.support.v4.widget.TextViewCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import cn.vsx.vc.R;

/**
 * 作者：ly-xuxiaolong
 * 版本：1.0
 * 创建日期：2019/4/17
 * 描述：
 * 修订历史：
 */
public class TabView extends RelativeLayout{

    private TextView mtvName;
    private View mLine;


    public TabView(Context context){
        super(context);
        init();
        initCustomAttrs(context,null);
    }

    public TabView(Context context, @Nullable AttributeSet attrs){
        super(context, attrs);
        init();
        initCustomAttrs(context,attrs);
    }

    public TabView(Context context, @Nullable AttributeSet attrs, int defStyleAttr){
        super(context, attrs, defStyleAttr);
        init();
        initCustomAttrs(context,attrs);
    }

    private void init(){
        String infServie = Context.LAYOUT_INFLATER_SERVICE;
        LayoutInflater layoutInflater;
        layoutInflater = (LayoutInflater) getContext().getSystemService(infServie);
        if(layoutInflater !=null){
            View view = layoutInflater.inflate(R.layout.layout_tab_view, this, true);
            mtvName = view.findViewById(R.id.tv_name);
            mLine = view.findViewById(R.id.line);
        }
    }

    private void initCustomAttrs(Context context, AttributeSet attrs){
        //获取自定义属性。
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.TabView);
        boolean selected = ta.getBoolean(R.styleable.TabView_tabView_selected, false);
        //获取字体大小,默认大小是12dp
        int textSize = (int) ta.getDimension(R.styleable.TabView_tabView_textSize, 12);
        //获取文字内容
        String text = ta.getString(R.styleable.TabView_tabView_text);
        ta.recycle();
        mtvName.setText(text);
        mtvName.setTextSize(textSize);
        mtvName.setSelected(selected);
        setChecked(selected);
    }

    public void setName(String name){
        if(mtvName !=null){
            mtvName.setText(name);
        }
    }

    public void showIndicator(){
        if(null != mLine){
            mLine.setVisibility(VISIBLE);
        }
    }

    public void hideIndicator(){
        if(null != mLine){
            mLine.setVisibility(GONE);
        }
    }

    public void setChecked(boolean selected){
        if(selected){
            TextViewCompat.setTextAppearance(mtvName,R.style.tab_checked);
            showIndicator();
        }else {
            TextViewCompat.setTextAppearance(mtvName,R.style.tab_unchecked);
            hideIndicator();
        }
    }
}
