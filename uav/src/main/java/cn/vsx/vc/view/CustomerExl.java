package cn.vsx.vc.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ExpandableListView;

/**
 * Created by Administrator on 2017/3/14 0014.
 */

public class CustomerExl extends ExpandableListView {

    public CustomerExl(Context context) {
        super(context);
    }

    public CustomerExl(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomerExl(Context context, AttributeSet attrs,
                       int defStyle) {
        super(context, attrs, defStyle);

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2,

                MeasureSpec.AT_MOST);

        super.onMeasure(widthMeasureSpec, expandSpec);
    }
}
