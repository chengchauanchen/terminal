package com.vsxin.terminalpad.mvp.ui.widget;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
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

import com.vsxin.terminalpad.R;

import java.util.ArrayList;

import static com.blankj.utilcode.util.StringUtils.getString;

/**
 * Created by zckj on 2017/6/30.
 */

public class XCDropDownListView extends LinearLayout {
    private TextView editText;
    private PopupWindow popupWindow = null;
    private ArrayList<String> dataList =  new ArrayList<>();

    public XCDropDownListView(Context context) {
        this(context,null);
        // TODO Auto-generated constructor stub
    }
    public XCDropDownListView(Context context, AttributeSet attrs) {
        this(context, attrs,0);
        // TODO Auto-generated constructor stub
    }
    public XCDropDownListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        // TODO Auto-generated constructor stub
        initView();
    }

    public void initView(){
        String infServie = Context.LAYOUT_INFLATER_SERVICE;
        LayoutInflater layoutInflater;
        layoutInflater =  (LayoutInflater) getContext().getSystemService(infServie);
        View view  = layoutInflater.inflate(R.layout.dropdownlist_view, this,true);
        editText= (TextView)findViewById(R.id.text);
        ImageView imageView = (ImageView) findViewById(R.id.btn);
        LinearLayout compound = (LinearLayout) findViewById(R.id.compound);

        compound.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(popupWindow == null ){
                    if(showPopWindowListener!=null){
                        showPopWindowListener.onShowPopWindowListener();
                    }
                    showPopWindow();
                }else{
                    closePopWindow();
                }
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
        View contentView  = layoutInflater.inflate(R.layout.dropdownlist_popupwindow, null,false);
        ListView listView = (ListView)contentView.findViewById(R.id.listView);
        listView.setDivider(null);//去底线
        listView.setAdapter(new XCDropDownListAdapter(getContext(), dataList));
        int width = getWidth();
        popupWindow = new PopupWindow(contentView,width, LayoutParams.WRAP_CONTENT);
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
    public void setItemsData(ArrayList<String> list){
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
        ArrayList<String> mData;
        LayoutInflater inflater;
        public XCDropDownListAdapter(Context ctx, ArrayList<String> data){
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
                convertView = inflater.inflate(R.layout.dropdown_list_item, null);

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
            final String text = mData.get(position).toString();
            listItemView.tv.setText(text);
            if(TextUtils.equals(getString(R.string.text_add_unit),text)||TextUtils.equals(getString(R.string.text_selection_unit),text)){
                listItemView.tv.setTextColor(ContextCompat.getColor(getContext(),R.color.regist_edit_text_other_color));
            }else {
                listItemView.tv.setTextColor(ContextCompat.getColor(getContext(),R.color.regist_edit_text_color));
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

    public interface ShowPopWindowListener{
        void onShowPopWindowListener();
    }
    private ShowPopWindowListener showPopWindowListener;
    public void setOnShowPopWindowListener(ShowPopWindowListener showPopWindowListener){
        this.showPopWindowListener = showPopWindowListener;
    }
}
