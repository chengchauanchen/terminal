package cn.vsx.vc.adapter;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import cn.vsx.vc.R;
import cn.vsx.vc.model.ShouTaiBean;
import cn.vsx.vc.model.ShouTaiBean.BuMenBean;
import cn.vsx.vc.view.CustomExpandableListView;

/**
 * Created by jamie on 2017/10/28.
 */

public class HandPlatformSimpleExpandableListViewAdapter extends BaseExpandableListAdapter {
    private List<ShouTaiBean> memberList;
    private Activity activity;
    private ViewHolderShiJu viewHolderShiJu;
    private ViewHolderBuMen viewHolderBuMen;
    private HandPlatformAdapter adapter;

    public HandPlatformSimpleExpandableListViewAdapter(List<ShouTaiBean> memberList, Activity activity) {
        this.memberList = memberList;
        this.activity = activity;
    }
    @Override
    public int getGroupCount() {
        if (memberList == null){
            return 0;
        }
        return memberList.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return 1;
    }

    @Override
    public Object getGroup(int groupPosition) {
        return memberList.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return memberList.get(groupPosition).bumenList;
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        viewHolderShiJu =null;
        if (convertView == null){
            convertView = View.inflate(activity,R.layout.shoutai_item_shiju,null);
            viewHolderShiJu = new ViewHolderShiJu(convertView);
            convertView.setTag(viewHolderShiJu);
        }else {
            viewHolderShiJu = (ViewHolderShiJu) convertView.getTag();
        }
        viewHolderShiJu.shoutai_shuju_name.setText(memberList.get(groupPosition).shijuName);
        int memberNumber = 0;
        for (BuMenBean b : memberList.get(groupPosition).bumenList){
            if (b.memberList != null)
                memberNumber += b.memberList.size();
        }
        viewHolderShiJu.shoutai_group_size.setText("("+memberNumber+")");
        if (isExpanded) {
            viewHolderShiJu.is_shoutai_shiju.setBackgroundResource(R.drawable.new_folder_open);

        } else {
            viewHolderShiJu.is_shoutai_shiju.setBackgroundResource(R.drawable.new_folder_close);
        }
//        Collections.sort(test1Been.get(groupPosition).name);
        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        return getGenericExpandableListView(groupPosition, childPosition);
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
    public static class ViewHolderShiJu {
        @Bind(R.id.shoutai_shuju_name)
        TextView shoutai_shuju_name;
        @Bind(R.id.shoutai_group_size)
        TextView shoutai_group_size;
        @Bind(R.id.is_shoutai_shiju)
        ImageView is_shoutai_shiju;

        public ViewHolderShiJu(View rootView) {
            ButterKnife.bind(this, rootView);
        }
    }
    public static class ViewHolderBuMen {
        @Bind(R.id.shoutai_bumen_name)
        TextView shoutai_bumen_name;

        public ViewHolderBuMen(View rootView) {
            ButterKnife.bind(this, rootView);
        }
    }


    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public ExpandableListView getGenericExpandableListView(int groupPosition, int childPosition){
        CustomExpandableListView view = new CustomExpandableListView(activity);
        

        List<BuMenBean> bumens = memberList.get(groupPosition).bumenList;
//        Iterator<BuMenBean> iterator = bumens.iterator();
//        while (iterator.hasNext()){
//            int number = 10;
//            int nextpage = 0;
//            int maxPage;
//            List<Member> members = bumens.get(childPosition).memberList;
//            maxPage = (members.size() % number) == 0 ? (members.size() / number) : (members.size() / number) + 1;
//            iterator.next().memberList = DataUtil.getMemberInList(iterator.next().memberList, 0, 10);
//        }
        HandPlatformAdapter adapter = new HandPlatformAdapter(bumens, activity);
        view.setAdapter(adapter);

//        view.setOnScrollListener(new OnScrollListener() {
//            @Override
//            public void onScrollStateChanged(AbsListView view, int scrollState) {
//
//            }
//
//            @Override
//            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
//                //得到listview最后一项的id
//                int lastItemId = view.getLastVisiblePosition();
//                //判断用户是否滑动到最后一项，因为索引值从零开始所以要加上1
//                if((lastItemId + 1) == totalItemCount){
//                    /**
//                     * 计算当前页，因为每一页只加载十条数据，所以总共加载的数据除以每一页的数据的个数
//                     * 如果余数为零则当前页为currentPage=totalItemCount/number；
//                     * 如果不能整除则当前页为(int)(totalItemCount/number)+1;
//                     * 下一页则是当前页加1
//                     */
//                    int currentPage = totalItemCount % number;
//                    if(currentPage == 0){
//                        currentPage = totalItemCount / number;
//                    }else{
//                        currentPage = (totalItemCount / number) + 1;
//                    }
//                    System.out.println("当前页为："+currentPage);
//                    nextpage = currentPage + 1;
//                    //当总共的数据大于0是才加载数据
//                    if(totalItemCount > 0){
//                        //判断当前页是否超过最大页，以及上一页的数据是否加载完成
//                        if(nextpage <= maxPage){
//                            new Thread(new Runnable(){
//                                public void run(){
//                                    try {
//                                        Thread.sleep(2000);
//                                    } catch (InterruptedException e) {
//                                        e.printStackTrace();
//                                    }
//                                    //获取当前加载页的数据
//                                    data=DataServer.getData(totalItemCount, 10);
//                                    //通知listview改变UI中的数据
//                                    handler.sendEmptyMessage(0);
//                                }
//                            }).start();
//                        }
//                    }
//
//                }
//            }
//        });

        view.setPadding(20,0,0,0);
        return view;
    }

    private Handler handler=new Handler(){
        @SuppressLint("HandlerLeak")
        public void handleMessage(Message msg){
            if(msg.what==0){
                adapter.notifyDataSetChanged();
            }
            super.handleMessage(msg);
        }
    };

}
