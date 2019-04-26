package cn.vsx.vc.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import org.apache.log4j.Logger;

import butterknife.ButterKnife;

/**
 * Created by gt358 on 2017/8/9.
 */

public abstract class BaseLinearLayout extends LinearLayout {
    public Logger logger = Logger.getLogger(getClass());
    public BaseLinearLayout(Context context) {
        this(context, null);
    }

    public BaseLinearLayout(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BaseLinearLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
        initData();
        initLitener();
    }

    public void initView () {
        String infServie = Context.LAYOUT_INFLATER_SERVICE;
        LayoutInflater layoutInflater;
        layoutInflater =  (LayoutInflater) getContext().getSystemService(infServie);
        View view = layoutInflater.inflate(getContentResId(), this, true);
        ButterKnife.bind(this, view);
    }

    public abstract void initData ();

    public abstract void initLitener ();

    public abstract int getContentResId ();

    public abstract void unRegistListener ();
}
