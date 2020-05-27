package cn.vsx.vc.view;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

/**
 * @author martian on 2020/4/8.
 */
public class RecyclerViewNoBugLinearLayoutManager extends LinearLayoutManager {
  public RecyclerViewNoBugLinearLayoutManager(Context context) {
    super(context);
  }

  public RecyclerViewNoBugLinearLayoutManager(Context context, int orientation,
      boolean reverseLayout) {
    super(context, orientation, reverseLayout);
  }

  public RecyclerViewNoBugLinearLayoutManager(Context context, AttributeSet attrs,
      int defStyleAttr,
      int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
  }

  @Override public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
    try{
      super.onLayoutChildren(recycler, state);
    }catch (Exception e){
      e.printStackTrace();
    }
  }
}
