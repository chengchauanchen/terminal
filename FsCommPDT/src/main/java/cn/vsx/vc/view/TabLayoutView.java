package cn.vsx.vc.view;

import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.StyleRes;
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
 * 创建日期：2019/10/31
 * 描述：
 * 修订历史：
 */
public class TabLayoutView extends RelativeLayout{

    private TextView mTabName;
    private View mLine;

    public TabLayoutView(Context context){
        super(context);
        initView();
    }

    public TabLayoutView(Context context, AttributeSet attrs){
        super(context, attrs);
        initView();
    }

    public TabLayoutView(Context context, AttributeSet attrs, int defStyleAttr){
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView(){
        String infServie = Context.LAYOUT_INFLATER_SERVICE;
        LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(infServie);
        if(layoutInflater !=null){
            View view = layoutInflater.inflate(R.layout.layout_tab_layout_view, this, true);
            mTabName = (TextView) view.findViewById(R.id.tab_name);
            mLine = (View) view.findViewById(R.id.line);
        }
    }

    public void setmTabName(String name){
        mTabName.setText(name);
    }

    public void setSelect(boolean select){
        if(select){
            mLine.setVisibility(VISIBLE);
            setBoldType();
            setTextAppearance(R.style.contacts_title_checked_text);

        }else {
            mLine.setVisibility(INVISIBLE);
            setNormalType();
            setTextAppearance(R.style.contacts_title_unchecked_text);
        }
    }

    public void setTextAppearance(@StyleRes int resId){
        TextViewCompat.setTextAppearance(mTabName, resId);
    }

    public void setBoldType(){
        mTabName.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
    }

    public void setNormalType(){
        mTabName.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
    }
}
