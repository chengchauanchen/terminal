package cn.vsx.vc.view;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import java.util.ArrayList;

import cn.vsx.vc.R;
import cn.vsx.vc.model.DongHuTerminalType;

/**
 * Created by zckj on 2017/6/30.
 */

public class XCDropDownListWhiteBgView extends LinearLayout {
    private TextView editText;
    private PopupWindow popupWindow = null;
    private ArrayList<DongHuTerminalType> dataList =  new ArrayList<>();

    public XCDropDownListWhiteBgView(Context context) {
        this(context,null);
        // TODO Auto-generated constructor stub
    }
    public XCDropDownListWhiteBgView(Context context, AttributeSet attrs) {
        this(context, attrs,0);
        // TODO Auto-generated constructor stub
    }
    public XCDropDownListWhiteBgView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        // TODO Auto-generated constructor stub
        initView();
    }

    public void initView(){
        String infServie = Context.LAYOUT_INFLATER_SERVICE;
        LayoutInflater layoutInflater;
        layoutInflater =  (LayoutInflater) getContext().getSystemService(infServie);
        View view  = layoutInflater.inflate(R.layout.dropdownlist_white_bg_view, this,true);
        editText= (TextView)findViewById(R.id.text);
        ImageView imageView = (ImageView) findViewById(R.id.btn);
        LinearLayout compound = (LinearLayout) findViewById(R.id.compound);
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
        View contentView  = layoutInflater.inflate(R.layout.dropdownlist_popupwindow_white_bg, null,false);
        ListView listView = (ListView)contentView.findViewById(R.id.listView);
        listView.setDivider(null);//去底线
        listView.setAdapter(new XCDropDownListAdapter(getContext(), dataList));
        int width = getWidth();
        popupWindow = new PopupWindow(contentView,width,LayoutParams.WRAP_CONTENT);
        popupWindow.setBackgroundDrawable(getResources().getDrawable(R.color.transparent));
        popupWindow.setOutsideTouchable(true);
        popupWindow.showAsDropDown(this);
    }
    /**
     * 关闭下拉列表弹窗
     */
    private void closePopWindow(){
        popupWindow.dismiss();
        popupWindow = null;
    }
    /**
     * 设置数据
     * @param list
     */
    public void setItemsData(ArrayList<DongHuTerminalType> list){
        dataList = list;
        editText.setText(list.get(0).toString());
    }

    public void setText(String text){
        editText.setText(text);
    }

    public String getText(){
        return editText.getText().toString();
    }

    /**
     * 获取文本框中的数据
     */
    public String getItemsData(){
        return editText.getText().toString().trim();
    }
    /**
     * 数据适配器
     * @author
     *
     */
    class XCDropDownListAdapter extends BaseAdapter {

        Context mContext;
        ArrayList<DongHuTerminalType> mData;
        LayoutInflater inflater;
        public XCDropDownListAdapter(Context ctx,ArrayList<DongHuTerminalType> data){
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
                convertView = inflater.inflate(R.layout.dropdown_list_item_white_bg, null);

                listItemView = new ListItemView();
                // 获取控件对象
                listItemView.tv = (TextView) convertView
                        .findViewById(R.id.tv);

                listItemView.layout = (LinearLayout) convertView.findViewById(R.id.layout_container);
                // 设置控件集到convertView
                convertView.setTag(listItemView);
            } else {
                listItemView = (ListItemView) convertView.getTag();
            }

            // 设置数据
            final String text = mData.get(position).getValue();
            listItemView.tv.setText(text);
            if(text.equals("添加单位")){
                listItemView.tv.setTextColor(ContextCompat.getColor(getContext(),R.color.regist_add_host_name));
            }else {
                listItemView.tv.setTextColor(ContextCompat.getColor(getContext(),R.color.regist_host_name));
            }
            listItemView.layout.setOnClickListener(v -> {
                // TODO Auto-generated method stub
                editText.setText(text);
                closePopWindow();
                if(xcDropDownListViewClickListeren != null){
                    xcDropDownListViewClickListeren.onXCDropDownListViewClickListeren(position);
                }
            });
            return convertView;
        }

    }
    private static class ListItemView{
        TextView tv;
        LinearLayout layout;
    }

    public interface XCDropDownListViewClickListeren{
        void onXCDropDownListViewClickListeren(int position);
    }
    private XCDropDownListViewClickListeren xcDropDownListViewClickListeren;
    public void setOnXCDropDownListViewClickListeren(XCDropDownListViewClickListeren xcDropDownListViewClickListeren){
        this.xcDropDownListViewClickListeren = xcDropDownListViewClickListeren;
    }
}
