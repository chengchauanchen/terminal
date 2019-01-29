package cn.vsx.vc.view.custompopupwindow;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import cn.vsx.vc.R;

/**
 * Author：Bro0cL on 2016/12/26.
 */
public class TRMenuAdapter extends RecyclerView.Adapter<TRMenuAdapter.TRMViewHolder> {
    private Context mContext;
    private List<MenuItem> menuItemList;
    private boolean showIcon;
    private TopRightMenu mTopRightMenu;
    private TopRightMenu.OnMenuItemClickListener onMenuItemClickListener;

    public TRMenuAdapter(Context context, TopRightMenu topRightMenu, List<MenuItem> menuItemList, boolean show) {
        this.mContext = context;
        this.mTopRightMenu = topRightMenu;
        this.menuItemList = menuItemList;
        this.showIcon = show;
    }

    public void setData(List<MenuItem> data){
        menuItemList = data;
        notifyDataSetChanged();
    }

    public void setShowIcon(boolean showIcon) {
        this.showIcon = showIcon;
        notifyDataSetChanged();
    }

    @Override
    public TRMViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.trm_item_popup_menu_list, parent, false);
//        view.setBackgroundColor(mContext.getResources().getColor(R.color.observe));
        return new TRMViewHolder(view);
    }

    @Override
    public void onBindViewHolder(TRMViewHolder holder, int position) {
        final MenuItem menuItem = menuItemList.get(position);
        if (showIcon){
            holder.icon.setVisibility(View.VISIBLE);
            int resId = menuItem.getIcon();
            holder.icon.setBackground(mContext.getResources().getDrawable(resId < 0 ? 0 : resId));
        }else{
            holder.icon.setVisibility(View.GONE);
        }
        holder.text.setText(menuItem.getText());
//        holder.text.setTextColor(mContext.getResources().getColor(R.color.video_text_color));
        if (position==1){
            holder.viewDivider.setVisibility(View.VISIBLE);
        }else {
            holder.viewDivider.setVisibility(View.INVISIBLE);
        }

//        if (position == 0){
//            holder.container.setBackgroundDrawable(addStateDrawable(mContext, -1, R.drawable.trm_popup_top_pressed));
//        }else if (position == menuItemList.size() - 1){
//            holder.container.setBackgroundDrawable(addStateDrawable(mContext, -1, R.drawable.trm_popup_bottom_pressed));
//        }else {
//            holder.container.setBackgroundDrawable(addStateDrawable(mContext, -1, R.drawable.trm_popup_middle_pressed));
//        }
//        holder.container.setBackgroundDrawable(addStateDrawable(mContext, -1, R.drawable.trm_popup_middle_pressed));
        final int pos = holder.getAdapterPosition();
        holder.container.setOnClickListener(v -> {
            if (onMenuItemClickListener != null) {
                mTopRightMenu.dismiss();
                onMenuItemClickListener.onMenuItemClick(pos);
            }
        });
    }

    private StateListDrawable addStateDrawable(Context context, int normalId, int pressedId){
        StateListDrawable sd = new StateListDrawable();
        Drawable normal = normalId == -1 ? null : context.getResources().getDrawable(normalId);
        Drawable pressed = pressedId == -1 ? null : context.getResources().getDrawable(pressedId);
        sd.addState(new int[]{android.R.attr.state_pressed}, pressed);
        sd.addState(new int[]{}, normal);
        return sd;
    }

    @Override
    public int getItemCount() {
        return menuItemList == null ? 0 : menuItemList.size();
    }

    class TRMViewHolder extends RecyclerView.ViewHolder{
        ViewGroup container;
        ImageView icon;
        TextView text;
        View viewDivider;

        TRMViewHolder(View itemView) {
            super(itemView);
            container = (ViewGroup) itemView;
            icon = (ImageView) itemView.findViewById(R.id.trm_menu_item_icon);
            text = (TextView) itemView.findViewById(R.id.trm_menu_item_text);
            viewDivider=itemView.findViewById(R.id.view_divider);
        }
    }

    public void setOnMenuItemClickListener(TopRightMenu.OnMenuItemClickListener listener){
        this.onMenuItemClickListener = listener;
    }
}
