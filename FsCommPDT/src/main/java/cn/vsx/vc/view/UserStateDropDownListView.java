package cn.vsx.vc.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

import cn.vsx.hamster.common.UserStatus;
import cn.vsx.vc.R;
import cn.vsx.vc.utils.DensityUtil;
import cn.vsx.vc.view.cameralibrary.util.ScreenUtils;

/**
 * Created by zckj on 2017/6/30.
 */

public class UserStateDropDownListView extends LinearLayout {
    private TextView editText;
    private PopupWindow popupWindow = null;
    private ArrayList<UserStatus> dataList =  new ArrayList<>();
    private int popWindowWidth;
    private int xPos;
    private int yPos;
    private View bindView;

    public UserStateDropDownListView(Context context) {
        this(context,null);
        // TODO Auto-generated constructor stub
    }
    public UserStateDropDownListView(Context context, AttributeSet attrs) {
        this(context, attrs,0);
        // TODO Auto-generated constructor stub
    }
    public UserStateDropDownListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        // TODO Auto-generated constructor stub
        initView();
        initData();
    }

    private void initData() {
        dataList.clear();
        dataList.add(UserStatus.WAITING_ON_DUTY);
        dataList.add(UserStatus.POLICE);
        dataList.add(UserStatus.ARRIVED_AT_THE_SCENE);
        dataList.add(UserStatus.PATROL);
        dataList.add(UserStatus.ABSENCES);
        popWindowWidth = DensityUtil.dip2px(getContext(),240);
        xPos = (ScreenUtils.getScreenWidth(getContext())-popWindowWidth)/2;
        yPos = DensityUtil.dip2px(getContext(),0);
    }

    public void initView(){
        String infServie = Context.LAYOUT_INFLATER_SERVICE;
        LayoutInflater layoutInflater;
        layoutInflater =  (LayoutInflater) getContext().getSystemService(infServie);
        View view  = layoutInflater.inflate(R.layout.dropdownlist_view_user_state, this,true);
        editText= (TextView)findViewById(R.id.text);
        ImageView imageView = (ImageView) findViewById(R.id.btn);
        RelativeLayout compound = (RelativeLayout) findViewById(R.id.compound);
        compound.setOnClickListener(v -> {
            // TODO Auto-generated method stub
            if(popupWindow == null ){
                showPopWindow();
            }else{
                closePopWindow();
            }
        });
    }
    /**
     * 打开下拉列表弹窗
     */
    private void showPopWindow() {
        // 加载popupWindow的布局文件
        String infServie = Context.LAYOUT_INFLATER_SERVICE;
        LayoutInflater layoutInflater;
        layoutInflater =  (LayoutInflater) getContext().getSystemService(infServie);
        View contentView  = layoutInflater.inflate(R.layout.dropdownlist_popupwindow_user_state, null,false);
        ListView listView = (ListView)contentView.findViewById(R.id.listView);
        listView.setDivider(null);//去底线
        listView.setAdapter(new XCDropDownListAdapter(getContext(), dataList));
        popupWindow = new PopupWindow(contentView,popWindowWidth,LayoutParams.WRAP_CONTENT);
        popupWindow.setBackgroundDrawable(getResources().getDrawable(R.color.transparent));
        popupWindow.setOutsideTouchable(true);
        popupWindow.showAsDropDown((bindView!=null)?bindView:this,xPos,yPos);
    }
    /**
     * 关闭下拉列表弹窗
     */
    private void closePopWindow(){
        popupWindow.dismiss();
        popupWindow = null;
    }

    public void setText(String text){
        editText.setText(text);
    }

    public String getText(){
        return editText.getText().toString();
    }

    public void setView(View bindView){
        this.bindView = bindView;
    }

    /**
     * 数据适配器
     * @author
     *
     */
    class XCDropDownListAdapter extends BaseAdapter {

        Context mContext;
        ArrayList<UserStatus> mData;
        LayoutInflater inflater;
        public XCDropDownListAdapter(Context ctx,ArrayList<UserStatus> data){
            mContext  = ctx;
            mData = data;
            inflater = LayoutInflater.from(mContext);
        }
        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return mData.size();
        }

        @Override
        public Object getItem(int position) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            // TODO Auto-generated method stub
            // 自定义视图
            ListItemView listItemView = null;
            if (convertView == null) {
                // 获取list_item布局文件的视图
                convertView = inflater.inflate(R.layout.dropdown_list_item_user_state, null);

                listItemView = new ListItemView();
                // 获取控件对象
                listItemView.tv = (TextView) convertView
                        .findViewById(R.id.tv);

                listItemView.layout = (LinearLayout) convertView.findViewById(R.id.layout_container);
                listItemView.line = (View) convertView.findViewById(R.id.line);
                // 设置控件集到convertView
                convertView.setTag(listItemView);
            } else {
                listItemView = (ListItemView) convertView.getTag();
            }

            // 设置数据
            final UserStatus userStatus = mData.get(position);
            listItemView.tv.setText(userStatus.getValue());
            listItemView.line.setVisibility(position == (mData.size()-1)?View.GONE:View.VISIBLE);
            listItemView.layout.setOnClickListener(v -> {
                // TODO Auto-generated method stub
                closePopWindow();
                if(itemClickListener != null){
                    itemClickListener.onItemClickListener(userStatus);
                }
            });
            return convertView;
        }

    }
    private static class ListItemView{
        TextView tv;
        View line;
        LinearLayout layout;
    }

    public interface ItemClickListener{
        void onItemClickListener(UserStatus userStatus);
    }
    private ItemClickListener itemClickListener;
    public void setMyItemClickListener(ItemClickListener itemClickListener){
        this.itemClickListener = itemClickListener;
    }
}
