package cn.vsx.vc.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;
import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.model.VideoMeetingDataBean;
import cn.vsx.hamster.terminalsdk.model.VideoMeetingMessage;
import cn.vsx.hamster.terminalsdk.model.VideoMeetingUserDataBean;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveNotifyVideoMeetingMessageAddOrOutCompleteHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveNotifyVideoMeetingMessageUpdateCompleteHandler;
import cn.vsx.hamster.terminalsdk.tools.Params;
import cn.vsx.vc.R;
import cn.vsx.vc.adapter.VideoMeetingListAdapter;
import cn.vsx.vc.application.MyApplication;
import cn.vsx.vc.utils.Constants;
import cn.vsx.vc.utils.ToastUtil;
import com.alibaba.fastjson.JSONObject;
import com.chad.library.adapter.base.BaseQuickAdapter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.apache.log4j.Logger;
import ptt.terminalsdk.context.MyTerminalFactory;

public class VideoMeetingListActivity extends BaseActivity
    implements SwipeRefreshLayout.OnRefreshListener, BaseQuickAdapter.RequestLoadMoreListener, VideoMeetingListAdapter.OnItemClickListerner, View.OnClickListener{

  TextView barTitle;

  SwipeRefreshLayout layoutSrl;

  RecyclerView contentView;
  private VideoMeetingListAdapter adapter;
  //获取数据的index
  private long index = 0;
  //每页显示条数
  private static final int mPageSize = 10;

  protected Logger logger = Logger.getLogger(this.getClass());

  protected Handler handler = new Handler(Looper.getMainLooper()) {
    @Override
    public void handleMessage(Message msg) {
      super.handleMessage(msg);
    }
  };

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    //  一个公共的方法查询正在进行会商的结束状态，查完并更新完数据库，再显示UI
    TerminalFactory.getSDK().getVideoMeetingManager().checkVideoMeetingState();
  }

  @Override
  public int getLayoutResId() {
    return R.layout.activity_video_meeting_list;
  }

  @Override
  public void initView() {
    barTitle = (TextView) findViewById(R.id.bar_title);
    layoutSrl = (SwipeRefreshLayout) findViewById(R.id.layout_srl);
    contentView = (RecyclerView) findViewById(R.id.contentView);
    findViewById(R.id.news_bar_back).setOnClickListener(this);
    barTitle.setText(getResources().getString( R.string.text_video_meeting));

    adapter = new VideoMeetingListAdapter();
    adapter.setOnItemClickListerner(this);
    LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
    linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
    contentView.setLayoutManager(linearLayoutManager);
    adapter.setOnLoadMoreListener(this, contentView);
    adapter.setEnableLoadMore(false);
    contentView.setAdapter(adapter);
    layoutSrl.setColorSchemeResources(R.color.colorPrimary);
    layoutSrl.setProgressViewOffset(false, 0, (int) TypedValue
        .applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, getResources()
            .getDisplayMetrics()));
    layoutSrl.setOnRefreshListener(this);
  }

  @Override
  public void initListener() {
    MyTerminalFactory.getSDK().registReceiveHandler(receiveNotifyVideoMeetingMessageUpdateCompleteHandler);
    MyTerminalFactory.getSDK().registReceiveHandler(receiveNotifyVideoMeetingMessageAddOrOutCompleteHandler);
  }

  @Override
  public void initData() {
  }

  @Override
  public void doOtherDestroy() {
    dismissProgressDialog();
    MyTerminalFactory.getSDK().unregistReceiveHandler(receiveNotifyVideoMeetingMessageUpdateCompleteHandler);
    MyTerminalFactory.getSDK().unregistReceiveHandler(receiveNotifyVideoMeetingMessageAddOrOutCompleteHandler);
    handler.removeCallbacksAndMessages(null);
  }


  @Override public void onClick(View view) {
    int i = view.getId();
    if(i == R.id.news_bar_back){
      finish();
    }
  }


  @Override
  public void onRefresh() {
    //  一个公共的方法查询正在进行会商的结束状态，查完并更新完数据库，再显示UI
    TerminalFactory.getSDK().getVideoMeetingManager().checkVideoMeetingState();
  }

  @Override
  public void onLoadMoreRequested() {
    if(adapter!=null&&!adapter.getData().isEmpty()){
      index = adapter.getData().size();
      loadData(false);
    }else{
      index = 0;
      ToastUtil.showToast(this,getString(R.string.text_no_more_data));
    }
  }

  /**
   * 加载数据
   *
   * @param isRefresh
   */
  private void loadData(boolean isRefresh) {
    CopyOnWriteArrayList<VideoMeetingMessage> list =  TerminalFactory.getSDK().getSQLiteDBManager().getVideoMeetingMessageBySendTime(index,mPageSize);
    handler.post(() -> {
      layoutSrl.setRefreshing(false);
      if (!list.isEmpty()) {
        if (index == 0) {
          adapter.setNewData(list);
          //adapter.loadMoreComplete();
          adapter.setEnableLoadMore(!(list.size() < mPageSize));
        } else {
          adapter.addData(list);
          //adapter.loadMoreComplete();
          if (list.size() < mPageSize) {
            adapter.loadMoreEnd();
          } else {
            adapter.loadMoreComplete();
            //adapter.setEnableLoadMore(true);
          }
        }
        adapter.notifyDataSetChanged();
      } else {
        if (index == 0) {
          adapter.getData().clear();
          adapter.loadMoreEnd(true);
          //adapter.loadMoreComplete();
          adapter.setEnableLoadMore(false);
          adapter.notifyDataSetChanged();
        } else {
          //adapter.loadMoreComplete();
          adapter.loadMoreEnd();
          //adapter.setEnableLoadMore(false);
        }
        ToastUtil.showToast(VideoMeetingListActivity.this, getString(R.string.text_no_more_data));
      }

    });
  }

  @Override
  public void goToWatch(VideoMeetingMessage item) {
    // 获取状态，判断是否可以进入视频会商页面
    if(item!=null&&item.getRoomId()>0){
      showProgressDialog();
      TerminalFactory.getSDK().getThreadPool().execute(() -> {
        boolean isMeeting = checkVideoMeetingState(item);
        if(isMeeting){
          boolean canJoin =  checkCanJoinMeeting(item);
          if(canJoin){
            //判断业务逻辑
            handler.post(() -> {
              MyApplication.instance.stopAllBusiness();
              Intent intent = new Intent(VideoMeetingListActivity.this, VideoMeetingActivity.class);
              intent.putExtra(Constants.ROOM_ID,item.getRoomId());
              startActivity(intent);
            });
          }
        }
      });
    }else{
      ToastUtil.showToast(VideoMeetingListActivity.this,getString(R.string.text_video_meeting_data_error));
    }
  }

  /**
   * 检查视频会商是否在进行中
   * @param item
   * @return
   */
  private boolean checkVideoMeetingState(VideoMeetingMessage item) {
    if(item!=null){
      VideoMeetingDataBean bean = TerminalFactory.getSDK().getVideoMeetingManager().getVideoMeetingStateByRoomId(item.getRoomId()+"");
      if(bean!=null){
        if(bean.getStatus() == 1){
          return true;
        }else{
          handler.post(() -> dismissProgressDialog());
          ToastUtil.showToast(VideoMeetingListActivity.this,getString(R.string.text_video_meeting_end));
          //更新本地数据
          List<VideoMeetingDataBean> list  = new ArrayList<>();
          list.add(bean);
          TerminalFactory.getSDK().getSQLiteDBManager().updateVideoMeetingMessageList(list);

          //更新UI
          item.setMeeting(false);
          item.setMeetingDescribe(JSONObject.toJSON(bean).toString());
          handler.post(() -> {
            if(adapter!=null){
              adapter.notifyDataSetChanged();
            }
          });
        }
      }else{
        handler.post(() -> dismissProgressDialog());
        ToastUtil.showToast(VideoMeetingListActivity.this,getString(R.string.text_video_meeting_data_error));
      }
    }else{
      handler.post(() -> dismissProgressDialog());
      ToastUtil.showToast(VideoMeetingListActivity.this,getString(R.string.text_video_meeting_data_error));
    }
    return false;
  }

  /**
   * 检查是否可以进入视频会商
   * @param item
   * @return
   */
  private boolean checkCanJoinMeeting(VideoMeetingMessage item) {
    if(item!=null){
      List<VideoMeetingUserDataBean> list = TerminalFactory.getSDK().getVideoMeetingManager().getVideoMeetingUserListByRoomId(item.getRoomId()+"");
      handler.post(() -> dismissProgressDialog());
      if(list!=null){
        if(!list.isEmpty()){
          VideoMeetingUserDataBean bean = new VideoMeetingUserDataBean(
              MyTerminalFactory.getSDK().getParam(Params.MEMBER_ID,0)+"",
              MyTerminalFactory.getSDK().getParam(Params.MEMBER_UNIQUENO,0L)+"");
          if(list.contains(bean)){
            return true;
          }else{
            ToastUtil.showToast(VideoMeetingListActivity.this,getString(R.string.text_can_not_join_in_video_meeting));
            ////删除这条消息
            //TerminalFactory.getSDK().getSQLiteDBManager().removeVideoMeetingMessageByRoomId(item.getRoomId());
            ////更新UI
            //TerminalFactory.getSDK().notifyReceiveHandler(ReceiveNotifyVideoMeetingMessageAddOrOutCompleteHandler.class,item,false);
            //removeMessage(item);
          }
        }else {
          ToastUtil.showToast(VideoMeetingListActivity.this,getString(R.string.text_can_not_join_in_video_meeting));
          ////删除这条消息
          //TerminalFactory.getSDK().getSQLiteDBManager().removeVideoMeetingMessageByRoomId(item.getRoomId());
          ////更新UI
          //TerminalFactory.getSDK().notifyReceiveHandler(ReceiveNotifyVideoMeetingMessageAddOrOutCompleteHandler.class,item,false);
          //removeMessage(item);
        }
      }else{
        ToastUtil.showToast(VideoMeetingListActivity.this,getString(R.string.text_video_meeting_data_error));
      }
    }else{
      ToastUtil.showToast(VideoMeetingListActivity.this,getString(R.string.text_video_meeting_data_error));
    }
    return false;
  }

  /**
   * 更新完数据库之后更新UI
   */
  private ReceiveNotifyVideoMeetingMessageUpdateCompleteHandler receiveNotifyVideoMeetingMessageUpdateCompleteHandler = ( ) ->{
    //开始从数据库中加载数据
    index = 0;
    handler.post(() -> {
      contentView.scrollToPosition(0);
    });
    loadData(true);
  };

  /**
   * 更新完数据库之后更新UI
   */
  private ReceiveNotifyVideoMeetingMessageAddOrOutCompleteHandler receiveNotifyVideoMeetingMessageAddOrOutCompleteHandler = (message,update,addUnreadCount) ->{
    //更新UI
    if(update){
      if(message!=null){
        if(adapter!=null&&!adapter.getData().isEmpty()){
          List<VideoMeetingMessage> list = adapter.getData();
          if(list.contains(message)){
            int index = list.indexOf(message);
            if(index>=0&&index<list.size()){
              list.set(index,message);
              collectionsList(list);
              handler.post(() -> {
                adapter.notifyDataSetChanged();
              });
            }
          }else{
            if(message.isMeeting()){
              list.add(message);
              collectionsList(list);
              handler.post(() -> {
                adapter.notifyDataSetChanged();
              });
            }
          }
        }
      }
    }
  };

  private void collectionsList(List<VideoMeetingMessage> list) {
    if(list!=null){
      Collections.sort(list, new Comparator<VideoMeetingMessage>() {
        @Override public int compare(VideoMeetingMessage o1, VideoMeetingMessage o2) {
          if(o1.isMeeting()&&!o2.isMeeting()){
            return -1;
          }else if(!o1.isMeeting()&&o2.isMeeting()){
            return 1;
          }else {
            return (o1.sendTime) > (o2.sendTime) ? -1 : 1;
          }
        }
      });
    }
  }

  /**
   * 不能加人视频会商
   * @param item
   */
  private void canNotJoinVideoMeeting(VideoMeetingMessage item){
    try{
      if(item!=null){
        if(adapter!=null&&adapter.getData().contains(item)){
          adapter.getData().remove(item);
          handler.post(() -> adapter.notifyDataSetChanged());
        }
      }
    }catch (Exception e){
      e.printStackTrace();
    }
  }
  /**
   * 删除消息
   * @param item
   */
  private synchronized void removeMessage(VideoMeetingMessage item) {
    try{
      if(item!=null){
        if(adapter!=null&&adapter.getData().contains(item)){
          adapter.getData().remove(item);
          handler.post(() -> adapter.notifyDataSetChanged());
        }
      }
    }catch (Exception e){
      e.printStackTrace();
    }
  }
}
